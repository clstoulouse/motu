package fr.cls.atoll.motu.web.dal.request;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.request.netcdf.NetCdfWriter;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.DatasetGrid;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.dal.tds.ncss.NetCdfSubsetService;
import ucar.ma2.Array;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;

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

    // /**
    // * Product defered extract netcdf.
    // *
    // * @param organizer the organizer
    // * @param extractionParameters the extraction parameters
    // * @param mode the mode
    // *
    // * @return the status mode response
    // *
    // * @throws MotuException the motu exception
    // */
    // @Override
    // public Product processRequest(RequestDownloadStatus requestDownloadStatus, ExtractionParameters
    // extractionParameters) throws MotuException {
    // return ProductDeferedExtractNetcdfThread.extractData(extractionParameters);
    // }

    @Override
    public void downloadProduct(ConfigService cs, Product p, OutputFormat dataOutputFormat) throws MotuException {
        boolean ncssStatus = false;
        String ncssValue = cs.getCatalog().getNcss();
        if (ncssValue != null || "enabled".equalsIgnoreCase(ncssValue)) {
            ncssStatus = true;
        }

        // Detect NCSS or OpenDAP
        try {
            if (ncssStatus) {
                downloadWithNCSS(p, dataOutputFormat);
            } else {
                downloadWithOpenDap(p, dataOutputFormat);
            }
        } catch (Exception e) {
            throw new MotuException("Error while downloading product ncss=" + ncssStatus, e);
        }

        // Product product = getProductInformation(locationData);
        // // Update ID
        //
        // if (!StringUtils.isNullOrEmpty(productId)) {
        // product.setProductId(productId);
        // }
        // // Update NCSS link
        // product.setLocationDataNCSS(locationDataNCSS);
        //
        // extractData(product, listVar, listTemporalCoverage, listLatLonCoverage, listDepthCoverage,
        // selectData, dataOutputFormat, out, responseFormat);

    }

    private void downloadWithOpenDap(Product p, OutputFormat dataOutputFormat) throws MotuInvalidDateRangeException, MotuExceedingCapacityException,
            MotuNotImplementedException, MotuInvalidDepthRangeException, MotuInvalidLatLonRangeException, NetCdfVariableException, MotuNoVarException,
            NetCdfVariableNotFoundException, MotuException, IOException {
        p.getDataset().extractData(dataOutputFormat);
    }

    private void downloadWithNCSS(Product p, OutputFormat dataOutputFormat)
            throws MotuInvalidDepthRangeException, NetCdfVariableException, MotuException, IOException, InterruptedException {

        List<CoordinateAxis> coordinateAxisList = p.getNetCdfReaderDataset().getCoordinateAxes();
        for (CoordinateAxis coordinateAxis : coordinateAxisList) {
            if (coordinateAxis.getAxisType() != null && coordinateAxis.getAxisType().name().equals(AxisType.Lon.name())) {
                System.out.println("Max : " + coordinateAxis.getValidMax());
                System.out.println("Min : " + coordinateAxis.getValidMin());
            }
        }

        // Extract criteria collect
        ExtractCriteriaDatetime time = p.getCriteriaDateTime();
        ExtractCriteriaLatLon latlon = p.getCriteriaLatLon();
        ExtractCriteriaDepth depth = p.getCriteriaDepth();
        Set<String> var = p.getDataset().getVariables().keySet();

        // Create output NetCdf file to deliver to the user (equivalent to opendap)
        String fname = NetCdfWriter.getUniqueNetCdfFileName(p.getProductId());
        p.setExtractFilename(fname);
        String extractDirPath = BLLManager.getInstance().getConfigManager().getMotuConfig().getExtractionPath();

        // Create and initialize selection
        NetCdfSubsetService ncss = new NetCdfSubsetService();
        ncss.setTimeSubset(time);
        ncss.setDepthSubset(depth);
        ncss.setVariablesSubset(var);
        ncss.setOutputFormat(dataOutputFormat);
        ncss.setncssURL(p.getLocationDataNCSS());

        System.out.println("Right long : " + latlon.getLowerLeftLon());
        System.out.println("Left long : " + latlon.getLowerRightLon());
        System.out.println("Lon Max : " + p.getProductMetaData().getLonNormalAxisMaxValue());
        System.out.println("Lon Min : " + p.getProductMetaData().getLonNormalAxisMinValue());
        // System.out.println(p.getNetCdfReader().get);

        // Check if the Left longitude is greater than the right longitude
        if (latlon.getLowerLeftLon() > latlon.getLowerRightLon()) {
            // In this case, thredds needs 2 requests to retrieve the data.
            List<ExtractCriteriaLatLon> rangesToRequest = ComputeRangeOutOfLimit(p, latlon);

            // Create a temporary directory into tmp directory to save the 2 generated file
            Path tempDirectory = Files.createTempDirectory("LeftAndRightRequest");
            ncss.setOutputDir(tempDirectory.toString());
            System.out.println("Temporary Directory : " + tempDirectory.toString());

            System.out.println("product message error : " + p.getLastError());

            int i = 0;
            for (ExtractCriteriaLatLon currentRange : rangesToRequest) {
                ncss.setGeoSubset(currentRange);
                ncss.setOutputFile(i + "-" + fname);
                System.out.println("Left file name : " + ncss.getOutputFile());
                ncssRequest(p, ncss);
                i++;
            }

            System.out.println("product message error after right request: " + p.getLastError());

            // Concatenate with NCO
            String cmd = "cdo merge " + tempDirectory + "/* " + extractDirPath + "/" + fname;
            System.out.println("Concat Request : " + cmd);
            Process process = Runtime.getRuntime().exec(cmd);
            process.waitFor();

            // Cleanup directory and intermediate files (right away once concat)
            // FileUtils.deleteDirectory(tempDirectory.toFile());

        } else {
            ncss.setOutputFile(fname);
            ncss.setOutputDir(extractDirPath);
            ncss.setGeoSubset(latlon);
            ncssRequest(p, ncss);
        }
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
