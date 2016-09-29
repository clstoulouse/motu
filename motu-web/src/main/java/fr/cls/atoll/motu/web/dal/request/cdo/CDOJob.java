package fr.cls.atoll.motu.web.dal.request.cdo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaLatLon;
import fr.cls.atoll.motu.web.common.utils.ProcessOutputLogguer;
import fr.cls.atoll.motu.web.common.utils.ProcessOutputLogguer.Type;
import fr.cls.atoll.motu.web.dal.request.IDALRequestManager;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.dal.tds.ncss.NetCdfSubsetService;

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
    private Product p;
    private NetCdfSubsetService ncss;
    private ExtractCriteriaLatLon latlon;
    private String extractDirPath;
    private String fname;
    private IDALRequestManager dalRequestManager;
    private Exception runningException;
    private boolean isJobEnded;

    /**
     * Constructeur.
     * 
     * @param p
     * @param ncss
     * @param latlon
     * @param extractDirPath
     * @param fname
     * @param dalRequestManager
     */
    public CDOJob(
        Product p,
        NetCdfSubsetService ncss,
        ExtractCriteriaLatLon latlon,
        String extractDirPath,
        String fname,
        IDALRequestManager dalRequestManager) {
        super();
        setJobEnded(false);
        this.p = p;
        this.ncss = ncss;
        this.latlon = latlon;
        this.extractDirPath = extractDirPath;
        this.fname = fname;
        this.dalRequestManager = dalRequestManager;
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        onJobStarts();

        try {
            // In this case, thredds needs 2 requests to retrieve the data.
            List<ExtractCriteriaLatLon> rangesToRequest = computeRangeOutOfLimit(p, latlon);

            // Create a temporary directory into tmp directory to save the 2 generated file
            Path tempDirectory = Files.createTempDirectory("LeftAndRightRequest");
            ncss.setOutputDir(tempDirectory.toString());

            List<String> filesPath = new ArrayList<>();

            int i = 0;
            long rangesLength = 0;
            for (ExtractCriteriaLatLon currentRange : rangesToRequest) {
                rangesLength += Math.abs(Math.abs(currentRange.getLatLonRect().getLonMax()) - Math.abs(currentRange.getLatLonRect().getLonMin()));
                ncss.setGeoSubset(currentRange);
                ncss.setOutputFile(i + "-" + fname);
                dalRequestManager.ncssRequest(p, ncss);
                filesPath.add(Paths.get(tempDirectory.toString(), ncss.getOutputFile()).toString());
                i++;
            }

            // Concatenate with NCO
            // Set the merge command
            String cmd = "merge.sh ";
            // Set the output file path
            cmd += extractDirPath + "/" + fname;
            // Set the start point
            cmd += " " + latlon.getLowerLeftLon();
            // Set the length
            cmd += " " + rangesLength;

            // Set the list of files to merge
            for (String path : filesPath) {
                cmd += " " + path;
            }
            LOGGER.info("Start: " + cmd);
            final Process process = Runtime.getRuntime().exec(cmd);

            new Thread(new ProcessOutputLogguer(new BufferedReader(new InputStreamReader(process.getInputStream())), LOGGER, Type.INFO)).start();
            new Thread(new ProcessOutputLogguer(new BufferedReader(new InputStreamReader(process.getErrorStream())), LOGGER, Type.ERROR)).start();

            int exitValue = process.waitFor();
            LOGGER.info("END [Exit code=" + exitValue + "] : " + cmd);

            // Cleanup directory and intermediate files (right away once concat)
            FileUtils.deleteDirectory(tempDirectory.toFile());

            if (exitValue != 0) {
                throw new MotuException(ErrorType.SYSTEM, "The generation of the NC file failled. See the log for more information.");
            }
        } catch (Exception e) {
            runningException = e;
        }

        onJobEnds();
    }

    /**
     * .
     */
    protected void onJobStarts() {
        LOGGER.info("START CDO job, ProductId=" + p.getProductId());
    }

    /**
     * .
     */
    protected void onJobEnds() {
        setJobEnded(true);
        LOGGER.info("END CDO job, ProductId=" + p.getProductId());
    }

    /**
     * Compute the ranges of requests to do if leftLon is upper than rightlon .
     * 
     * @param p The product to request
     * @param latLon the coordinates to request
     * @return The different ranges to request
     */
    private List<ExtractCriteriaLatLon> computeRangeOutOfLimit(Product p, ExtractCriteriaLatLon latLon) {
        List<ExtractCriteriaLatLon> ranges = new ArrayList<>();

        double leftLon = latLon.getLowerLeftLon();
        double rightLon = latLon.getLowerRightLon();

        // If the leftLon value is negative
        if (leftLon < 0) {
            // The rightLon which is smaller than the leftLon is also negative.
            // In this case the only possible alternative is 3 ranges (leftLon ; 0), (0 ; 180), (-180 ;
            // rightLon)
            ranges.add(new ExtractCriteriaLatLon(latLon.getLowerLeftLat(), leftLon, latLon.getUpperRightLat(), 0));
            ranges.add(new ExtractCriteriaLatLon(latLon.getLowerLeftLat(), 0, latLon.getUpperRightLat(), 180));
            ranges.add(new ExtractCriteriaLatLon(latLon.getLowerLeftLat(), -180, latLon.getUpperRightLat(), rightLon));
        } else {
            // If the leftLon value is positive
            if (rightLon > 0) {
                // And the rightLon is positive also, so the alternative is (leftLon , 180), (-180 ; 0)
                // (0 ; rightLon)
                ranges.add(new ExtractCriteriaLatLon(latLon.getLowerLeftLat(), leftLon, latLon.getUpperRightLat(), 180));
                ranges.add(new ExtractCriteriaLatLon(latLon.getLowerLeftLat(), -180, latLon.getUpperRightLat(), 0));
                ranges.add(new ExtractCriteriaLatLon(latLon.getLowerLeftLat(), 0, latLon.getUpperRightLat(), rightLon));
            } else {
                // Or if the rightLon is negative also, so the alternative is (leftLon ; 180)
                // (-180 ; rightLon)
                ranges.add(new ExtractCriteriaLatLon(latLon.getLowerLeftLat(), leftLon, latLon.getUpperRightLat(), 180));
                ranges.add(new ExtractCriteriaLatLon(latLon.getLowerLeftLat(), -180, latLon.getUpperRightLat(), rightLon));
            }
        }

        return ranges;
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
