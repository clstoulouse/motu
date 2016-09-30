package fr.cls.atoll.motu.web.dal.request;

import java.io.IOException;
import java.util.List;
import java.util.Set;

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
import fr.cls.atoll.motu.web.dal.DALManager;
import fr.cls.atoll.motu.web.dal.config.DALConfigManager;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.request.cdo.CDOManager;
import fr.cls.atoll.motu.web.dal.request.cdo.ICDOManager;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.DatasetGrid;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.dal.tds.ncss.NetCdfSubsetService;
import ucar.ma2.Array;
import ucar.unidata.geoloc.LatLonPointImpl;

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

    private ICDOManager cdoManager;

    public DALRequestManager() {
        cdoManager = new CDOManager();
    }

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

        // Check if it is an antimeridien request
        double rightLon = latlon.getLowerRightLon();
        if (p.getProductMetaData().getLonAxisMaxValue() > 350) {
            rightLon = LatLonPointImpl.lonNormal360(rightLon);
            ncss.setRightLonIn360(rightLon);
        }

        // Check if the left longitude is greater than the right longitude
        if (latlon.getLowerLeftLon() > rightLon) {
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
                try {
                    runRequestWithCDOMergeTool(p, ncss, latlon, extractDirPath, fname);
                } catch (MotuException e) {
                    throw e;
                } catch (Exception e) {
                    throw new MotuException(ErrorType.SYSTEM, "Error while running request with CDO merge tool", e);
                }
            } else {
                throw new MotuException(ErrorType.TOO_DEPTH_REQUESTED, "There is more than one depth in this request which needs merge procedure.");
            }
        } else {
            ncss.setOutputFile(fname);
            ncss.setOutputDir(extractDirPath);
            ncss.setGeoSubset(new ExtractCriteriaLatLon(
                    latlon.getLatLonRect().getLatMin(),
                    latlon.getLatLonRect().getLonMin(),
                    latlon.getLatLonRect().getLatMax(),
                    rightLon));

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
            throws Exception {
        cdoManager.runRequestWithCDOMergeTool(p, ncss, latlon, extractDirPath, fname, this);
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

    @Override
    public void ncssRequest(Product p, NetCdfSubsetService ncss)
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

    /** {@inheritDoc} */
    @Override
    public void stop() {
        cdoManager.stop();
    }
}
