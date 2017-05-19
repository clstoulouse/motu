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
import fr.cls.atoll.motu.web.bll.request.model.RequestDownloadStatus;
import fr.cls.atoll.motu.web.common.utils.CoordinateUtils;
import fr.cls.atoll.motu.web.common.utils.ProcessOutputLogguer;
import fr.cls.atoll.motu.web.common.utils.ProcessOutputLogguer.Type;
import fr.cls.atoll.motu.web.dal.request.IDALRequestManager;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.dal.tds.ncss.NetCdfSubsetService;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis1D;

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
    private RequestDownloadStatus rds;
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
        RequestDownloadStatus rds_,
        NetCdfSubsetService ncss,
        ExtractCriteriaLatLon latlon,
        String extractDirPath,
        String fname,
        IDALRequestManager dalRequestManager) {
        super();
        setJobEnded(false);
        this.rds = rds_;
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
            List<ExtractCriteriaLatLon> rangesToRequest = computeRangeOutOfLimit(rds.getRequestProduct().getProduct(), latlon);

            // Create a temporary directory into tmp directory to save the 2 generated files
            Path tempDirectory = Files.createTempDirectory("LeftAndRightRequest");
            try {
                ncss.setOutputDir(tempDirectory.toString());

                List<String> filesPath = new ArrayList<>();

                int i = 0;
                double rangesLength = 0;
                Path currentFilePath = null;
                for (ExtractCriteriaLatLon currentRange : rangesToRequest) {
                    rangesLength += Math.abs(currentRange.getLatLonRect().getLonMax() - currentRange.getLatLonRect().getLonMin());
                    ncss.setGeoSubset(currentRange);
                    ncss.setOutputFile(i + "-" + fname);
                    dalRequestManager.ncssRequest(rds, ncss);
                    currentFilePath = Paths.get(tempDirectory.toString(), ncss.getOutputFile());
                    filesPath.add(currentFilePath.toString());
                    i++;
                }

                if (currentFilePath != null) {

                    // Concatenate with NCO
                    // Set the merge command
                    String cmd = "merge.sh ";
                    // Set the output file path
                    cmd += Paths.get(extractDirPath, fname).toString();
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

                    new Thread(new ProcessOutputLogguer(new BufferedReader(new InputStreamReader(process.getInputStream())), LOGGER, Type.INFO))
                            .start();
                    new Thread(new ProcessOutputLogguer(new BufferedReader(new InputStreamReader(process.getErrorStream())), LOGGER, Type.ERROR))
                            .start();

                    int exitValue = process.waitFor();
                    LOGGER.info("END [Exit code=" + exitValue + "] : " + cmd);

                    if (exitValue != 0) {
                        throw new MotuException(ErrorType.SYSTEM, "The generation of the NC file failled. See the log for more information.");
                    }
                }
            } finally {
                // Cleanup directory and intermediate files (right away once concat)
                FileUtils.deleteDirectory(tempDirectory.toFile());
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
        LOGGER.info("START CDO job, ProductId=" + rds.getRequestProduct().getProduct().getProductId());
    }

    /**
     * .
     */
    protected void onJobEnds() {
        setJobEnded(true);
        LOGGER.info("END CDO job, ProductId=" + rds.getRequestProduct().getProduct().getProductId());
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

        // Check if it is a full world request
        double axisXMin = CoordinateUtils.getLongitudeM180P180(p.getProductMetaData().getLonAxisMinValue());
        double axisXMax = CoordinateUtils.getLongitudeGreaterOrEqualsThanLongitudeMin(p.getProductMetaData().getLonAxisMaxValue(), axisXMin);
        // Get one resolution step
        double xInc = ((CoordinateAxis1D) p.getProductMetaData().getCoordinateAxes(AxisType.Lon)).getIncrement();
        axisXMax += xInc;

        leftLon = CoordinateUtils.getLongitudeJustLowerThanLongitudeMax(CoordinateUtils
                .getLongitudeGreaterOrEqualsThanLongitudeMin(latlon.getLowerLeftLon(), axisXMin), axisXMax);
        rightLon = CoordinateUtils.getLongitudeGreaterOrEqualsThanLongitudeMin(latlon.getLowerRightLon(), axisXMin);

        if (leftLon < rightLon) {
            if (rightLon <= axisXMax) {
                // [axisXMin] [[leftLon rightLon]] [axisXMax]
                ranges.add(new ExtractCriteriaLatLon(latLon.getLowerLeftLat(), leftLon, latLon.getUpperRightLat(), rightLon));
            } else {
                // [[leftLon [axisXMin] [axisXMax] rightLon]]
                // [axisXMin] [[leftLon [axisXMax] rightLon]]
                ranges.add(new ExtractCriteriaLatLon(latLon.getLowerLeftLat(), leftLon, latLon.getUpperRightLat(), axisXMax));
                ranges.add(new ExtractCriteriaLatLon(latLon.getLowerLeftLat(), axisXMax, latLon.getUpperRightLat(), rightLon));
            }
        } else {
            // leftLon >= rightLon
            if (leftLon <= axisXMax) {
                // [axisXMin] rightLon]] [[leftLon [axisXMax]
                ranges.add(new ExtractCriteriaLatLon(latLon.getLowerLeftLat(), leftLon, latLon.getUpperRightLat(), axisXMax));
                ranges.add(new ExtractCriteriaLatLon(latLon.getLowerLeftLat(), axisXMin, latLon.getUpperRightLat(), rightLon + xInc));
            } else {
                // Here we cut the easter boundary (axisXMax)
                // 2 requests
                // [axisXMin] rightLon]] [axisXMax] [[leftLon
                ranges.add(new ExtractCriteriaLatLon(latLon.getLowerLeftLat(), leftLon, latLon.getUpperRightLat(), axisXMax));
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
