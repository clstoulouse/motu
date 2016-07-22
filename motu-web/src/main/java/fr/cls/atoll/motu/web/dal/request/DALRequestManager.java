package fr.cls.atoll.motu.web.dal.request;

import java.io.IOException;
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
        boolean ncss = cs.getCatalog().getNcss() != null && cs.getCatalog().getNcss().equalsIgnoreCase("enabled");

        // Detect NCSS or OpenDAP
        try {
            if (ncss) {
                downloadWithNCSS(p, dataOutputFormat);
            } else {
                downloadWithOpenDap(p, dataOutputFormat);
            }
        } catch (Exception e) {
            throw new MotuException("Error while downloading product ncss=" + ncss, e);
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
        ncss.setGeoSubset(latlon);
        ncss.setTimeSubset(time);
        ncss.setDepthSubset(depth);
        ncss.setVariablesSubset(var);
        ncss.setOutputFormat(dataOutputFormat);
        ncss.setOutputDir(extractDirPath);
        ncss.setOutputFile(fname);
        ncss.setncssURL(p.getLocationDataNCSS());

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
