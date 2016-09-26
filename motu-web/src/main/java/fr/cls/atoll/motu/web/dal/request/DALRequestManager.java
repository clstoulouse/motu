package fr.cls.atoll.motu.web.dal.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuExceedingCapacityException;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDateRangeException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidLatLonRangeException;
import fr.cls.atoll.motu.web.bll.exception.MotuNoVarException;
import fr.cls.atoll.motu.web.bll.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfVariableException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfVariableNotFoundException;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaDatetime;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaDepth;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaLatLon;
import fr.cls.atoll.motu.web.common.format.OutputFormat;
import fr.cls.atoll.motu.web.common.utils.ProcessOutputLogguer;
import fr.cls.atoll.motu.web.common.utils.ProcessOutputLogguer.Type;
import fr.cls.atoll.motu.web.dal.DALManager;
import fr.cls.atoll.motu.web.dal.config.DALConfigManager;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.DatasetGrid;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.dal.tds.ncss.NetCdfSubsetService;
import ucar.ma2.Array;

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
public class DALRequestManager implements IDALRequestManager {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void downloadProduct(ConfigService cs, Product p, OutputFormat dataOutputFormat, Long requestId) throws MotuException {
        boolean ncssStatus = false;
        String ncssValue = cs.getCatalog().getNcss();
        if ("enabled".equalsIgnoreCase(ncssValue)) {
            ncssStatus = true;
        }

        // Detect NCSS or OpenDAP
        try {
            if (ncssStatus) {
                downloadWithNCSS(p, dataOutputFormat, requestId);
            } else {
                downloadWithOpenDap(p, dataOutputFormat);
            }
        } catch (MotuException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error while downloading product ncss=" + ncssStatus, e);
            throw new MotuException(ErrorType.SYSTEM, "Error while downloading product ncss=" + ncssStatus, e);
        }
    }

    private void downloadWithOpenDap(Product p, OutputFormat dataOutputFormat) throws MotuInvalidDateRangeException, MotuExceedingCapacityException,
            MotuNotImplementedException, MotuInvalidDepthRangeException, MotuInvalidLatLonRangeException, NetCdfVariableException, MotuNoVarException,
            NetCdfVariableNotFoundException, MotuException, IOException {
        p.getDataset().extractData(dataOutputFormat);
    }

    private void downloadWithNCSS(Product p, OutputFormat dataOutputFormat, Long requestId)
            throws MotuInvalidDepthRangeException, NetCdfVariableException, MotuException, IOException, InterruptedException {
        // Extract criteria collect
        ExtractCriteriaDatetime time = p.getCriteriaDateTime();
        ExtractCriteriaLatLon latlon = p.getCriteriaLatLon();
        ExtractCriteriaDepth depth = p.getCriteriaDepth();
        Set<String> var = p.getDataset().getVariables().keySet();

        // Create output NetCdf file to deliver to the user (equivalent to opendap)
        // String fname = NetCdfWriter.getUniqueNetCdfFileName(p.getProductId());
        String fname = computeDownloadFileName(p.getProductId(), requestId);
        p.setExtractFilename(fname);
        String extractDirPath = BLLManager.getInstance().getConfigManager().getMotuConfig().getExtractionPath();

        // Create and initialize selection
        NetCdfSubsetService ncss = new NetCdfSubsetService();
        ncss.setTimeSubset(time);
        ncss.setDepthSubset(depth);
        ncss.setVariablesSubset(var);
        ncss.setOutputFormat(dataOutputFormat);
        ncss.setncssURL(p.getLocationDataNCSS());

        // Check if the left longitude is greater than the right longitude
        if (latlon.getLowerLeftLon() > latlon.getLowerRightLon()) {
            boolean canRequest = true;
            if (p.getProductMetaData().hasZAxis()) {
                // Check if only one depth is requested. This is because the merge of two netcdf file needs a
                // lot
                // of RAM resources, so to avoid the server miss RAM we do not authorized request on several
                // depths.
                int fromDepthIndex = searchDepthIndex(p.getZAxisRoundedDownDataAsString(2), depth.getFrom());
                int toDepthIndex = searchDepthIndex(p.getZAxisRoundedUpDataAsString(2), depth.getTo());
                canRequest = fromDepthIndex != -1 && toDepthIndex != -1 && fromDepthIndex == toDepthIndex;
            }
            if (canRequest) {
                runRequestWithCDOMergeTool(p, ncss, latlon, extractDirPath, fname);
            } else {
                throw new MotuException(ErrorType.TOO_DEPTH_REQUESTED, "There is more than one depth in this request which needs merge procedure.");
            }
        } else {
            ncss.setOutputFile(fname);
            ncss.setOutputDir(extractDirPath);
            ncss.setGeoSubset(latlon);
            ncssRequest(p, ncss);
        }
    }

    /**
     * .
     * 
     * @param latlon
     * @param ncss
     * @param p
     * @param fname
     * @param extractDirPath
     * @throws IOException
     * @throws InterruptedException
     * @throws MotuException
     * @throws NetCdfVariableException
     * @throws MotuInvalidDepthRangeException
     */
    private void runRequestWithCDOMergeTool(Product p, NetCdfSubsetService ncss, ExtractCriteriaLatLon latlon, String extractDirPath, String fname)
            throws IOException, MotuInvalidDepthRangeException, NetCdfVariableException, MotuException, InterruptedException {
        // In this case, thredds needs 2 requests to retrieve the data.
        List<ExtractCriteriaLatLon> rangesToRequest = ComputeRangeOutOfLimit(p, latlon);

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
            ncssRequest(p, ncss);
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
    }

    private int searchDepthIndex(List<String> listOfDepth, double depthToSearch) {
        int i = -1;
        for (String currentDepth : listOfDepth) {
            i++;
            if (Double.valueOf(currentDepth).equals(depthToSearch)) {
                return i;
            }
        }
        return i;
    }

    private String computeDownloadFileName(String productId, Long requestId) {
        String fileName = DALManager.getInstance().getConfigManager().getMotuConfig().getDownloadFileNameFormat();
        fileName = fileName.replace(DALConfigManager.FILENAME_FORMAT_PRODUCT_ID, productId);
        fileName = fileName.replace(DALConfigManager.FILENAME_FORMAT_REQUESTID, requestId.toString());
        return fileName;
    }

    /**
     * Compute the ranges of requests to do if leftLon is upper than rightlon .
     * 
     * @param p The product to request
     * @param latLon the coordinates to request
     * @return The different ranges to request
     */
    private List<ExtractCriteriaLatLon> ComputeRangeOutOfLimit(Product p, ExtractCriteriaLatLon latLon) {
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

    // private boolean is360systemCoordinates(Product p) {
    // if (p.getProductMetaData().getLonNormalAxisMinValue() < 0) {
    // return false;
    // } else {
    // return true;
    // }
    // }

    private void ncssRequest(Product p, NetCdfSubsetService ncss)
            throws MotuInvalidDepthRangeException, NetCdfVariableException, MotuException, IOException, InterruptedException {
        // Run rest query (unitary or concat depths)
        Array zAxisData = null;
        if (p.getProductMetaData().hasZAxis() && p.isDatasetGrid()) {
            // Z-Range selection update
            DatasetGrid d = (DatasetGrid) p.getDataset();
            d.setProductMetadata(p.getProductMetaData());
            int zlev = d.getZRange().length();

            // Dataset available depths
            zAxisData = p.getDataset().getProduct().getZAxisData();
            long alev = zAxisData.getSize();

            // Pass data to TDS-NCSS subsetter
            ncss.setDepthAxis(zAxisData);
            ncss.setDepthRange(d.getZRange());

            if (zlev == 1 || zlev == alev) {
                ncss.unitRequestNCSS(); // 1-level or ALL levels (can be done with TDS-NCSS)
            } else {
                ncss.concatDepths(); // True depth Subset with CDO operators (needs concatenation)
            }
        } else {
            ncss.unitRequestNCSS(); // No depth axis -> request without depths
        }
        p.addReadingTime(ncss.getReadingTimeInNanoSec());
        p.addWritingTime(ncss.getWritingTimeInNanoSec());
    }
}
