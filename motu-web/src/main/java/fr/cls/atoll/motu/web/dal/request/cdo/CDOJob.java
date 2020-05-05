package fr.cls.atoll.motu.web.dal.request.cdo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfVariableException;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaLatLon;
import fr.cls.atoll.motu.web.bll.request.model.RequestProduct;
import fr.cls.atoll.motu.web.common.utils.ProcessOutputLogguer;
import fr.cls.atoll.motu.web.common.utils.ProcessOutputLogguer.Type;
import fr.cls.atoll.motu.web.dal.request.IDALRequestManager;
import fr.cls.atoll.motu.web.dal.request.netcdf.NetCdfReader;
import fr.cls.atoll.motu.web.dal.request.netcdf.NetCdfWriter;
import fr.cls.atoll.motu.web.dal.tds.ncss.NetCdfSubsetService;
import ucar.ma2.Array;
import ucar.ma2.MAMath.MinMax;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class CDOJob implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger();
    private RequestProduct rp;
    private NetCdfSubsetService ncss;
    private String extractDirPath;
    private String fname;
    private IDALRequestManager dalRequestManager;
    private Exception runningException;
    private boolean isJobEnded;
    private List<ExtractCriteriaLatLon> rangesToRequest;

    /**
     * Constructeur.
     * 
     * @param p
     * @param ncss
     * @param latlon
     * @param extractDirPath
     * @param fname
     * @param ranges
     * @param dalRequestManager
     */
    public CDOJob(
        RequestProduct rp,
        NetCdfSubsetService ncss,
        String extractDirPath,
        String fname,
        List<ExtractCriteriaLatLon> ranges,
        IDALRequestManager dalRequestManager) {
        super();
        setJobEnded(false);
        this.rp = rp;
        this.ncss = ncss;
        this.extractDirPath = extractDirPath;
        this.fname = fname;
        this.dalRequestManager = dalRequestManager;
        this.rangesToRequest = ranges;
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        onJobStarts();

        Path tempDirectory = null;
        try {
            // Create a temporary directory into tmp directory to save the 2 generated files
            tempDirectory = Files.createTempDirectory("LeftAndRightRequest");
            ncss.setOutputDir(tempDirectory.toString());

            List<String> filesPath = launchNcssRequests(tempDirectory);

            if (!filesPath.isEmpty()) {
                String cmd = buildMergeCommand(filesPath);

                LOGGER.info("Start: " + cmd);
                final Process process = Runtime.getRuntime().exec(cmd);
                int exitValue;
                try (InputStream is = process.getInputStream(); InputStream es = process.getErrorStream()) {
                    new Thread(new ProcessOutputLogguer(new BufferedReader(new InputStreamReader(is)), LOGGER, Type.INFO)).start();
                    new Thread(new ProcessOutputLogguer(new BufferedReader(new InputStreamReader(es)), LOGGER, Type.ERROR)).start();

                    exitValue = process.waitFor();
                    LOGGER.info("END [Exit code=" + exitValue + "] : " + cmd);
                }
                if (exitValue != 0) {
                    throw new MotuException(ErrorType.SYSTEM, "The generation of the NC file failled. See the log for more information.");
                }
            }
        } catch (Exception e) {
            runningException = e;
        } finally {
            if (tempDirectory != null) {
                // Cleanup directory and intermediate files (right away once concat)
                try {
                    FileUtils.deleteDirectory(tempDirectory.toFile());
                } catch (IOException e) {
                    runningException = e;
                }
            }
        }
        
        onJobEnds();
    }

    private String buildMergeCommand(List<String> filesPath) throws IOException {
        // To select what should be the starting longitude for remap several things are possible:
        // - use a parameter (not implemented)
        // - prepare the file with the less NaN filling values (searching for minimum between areas,
        // but might not preserver the requested order of longitude min, longitude max => not
        // implemented)
        // - or what is done => select the indexes closer to the 0° between [x, x+width] and [x+360,
        // x+width+360]

        double rangesLength = ExtractCriteriaLatLon.LONGITUDE_TOTAL - rangesToRequest.get(0).getLonMin() + rangesToRequest.get(1).getLonMax();
        double lowerLeftLon = rangesToRequest.get(0).getLonMin() - ExtractCriteriaLatLon.LONGITUDE_TOTAL;

        boolean shift360 = Math.abs(lowerLeftLon) + Math.abs(lowerLeftLon + rangesLength) < Math.abs(rangesToRequest.get(0).getLonMin())
                + Math.abs(rangesToRequest.get(0).getLonMin() + rangesLength);

        try (NetcdfFile netcdffile = NetcdfFile.open(filesPath.get(0))) {
            Variable longitudeVariable = NetCdfReader.findLongitudeIgnoreCase(netcdffile.getVariables());
            if (longitudeVariable != null) {
                Array longitudeData = longitudeVariable.read();
                MinMax minMax = NetCdfWriter.getMinMaxSkipMissingData(longitudeData, longitudeVariable);
                rangesLength -= Math.abs(lowerLeftLon - minMax.min) % ExtractCriteriaLatLon.LONGITUDE_TOTAL;
                lowerLeftLon = minMax.min;
                if (shift360) {
                    lowerLeftLon -= ExtractCriteriaLatLon.LONGITUDE_TOTAL;
                }
                // MinMax lonMinMax = // FIXME behavior of CDO merge rounds longitude border and change values
                // CoordinateUtils.getMinMaxValueForAxis(rds.getRequestProduct().getProduct().getProductMetaData().getLonAxis());
                // rangesLength += lonMinMax.min + ExtractCriteriaLatLon.LONGITUDE_TOTAL - lonMinMax.max;
            }
        }

        // CFGridCoverageWriter2

        // Concatenate with NCO
        // Set the merge command
        StringBuilder cmd = new StringBuilder("merge.sh ");
        // Set the output file path
        cmd.append(Paths.get(extractDirPath, fname).toString());
        DecimalFormat df = new DecimalFormat("0", new DecimalFormatSymbols(Locale.US));
        df.setMaximumFractionDigits(340);
        // Set the start point
        cmd.append(" ").append(df.format(lowerLeftLon));
        // Set the length
        cmd.append(" ").append(df.format(rangesLength));

        // Set the list of files to merge
        for (String path : filesPath) {
            cmd.append(" ").append(path);
        }
        return cmd.toString();
    }

    private List<String> launchNcssRequests(Path tempDirectory)
            throws MotuInvalidDepthRangeException, NetCdfVariableException, MotuException, IOException, InterruptedException {
        List<String> filesPath = new ArrayList<>();
        int i = 0;
        Path currentFilePath = null;
        for (ExtractCriteriaLatLon currentRange : rangesToRequest) {
            ncss.setGeoSubset(currentRange);
            ncss.setOutputFile(i + "-" + fname);
            dalRequestManager.ncssRequest(rp, ncss);
            currentFilePath = Paths.get(tempDirectory.toString(), ncss.getOutputFile());
            filesPath.add(currentFilePath.toString());
            i++;
        }
        return filesPath;
    }

    /**
     * .
     */
    protected void onJobStarts() {
        LOGGER.info("START CDO job, ProductId=" + rp.getProduct().getProductId());
    }

    /**
     * .
     */
    protected void onJobEnds() {
        setJobEnded(true);
        LOGGER.info("END CDO job, ProductId=" + rp.getProduct().getProductId());
    }

    /**
     * Valeur de runningException.
     * 
     * @return la valeur.
     */
    public Exception getRunningException() {
        return runningException;
    }

    /**
     * Valeur de isJobEnded.
     * 
     * @return la valeur.
     */
    public boolean isJobEnded() {
        return isJobEnded;
    }

    /**
     * Valeur de isJobEnded.
     * 
     * @param isJobEnded nouvelle valeur.
     */
    public void setJobEnded(boolean isJobEnded) {
        this.isJobEnded = isJobEnded;
    }

}
