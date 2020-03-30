package fr.cls.atoll.motu.web.dal.request;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
import fr.cls.atoll.motu.web.bll.request.model.RequestProduct;
import fr.cls.atoll.motu.web.bll.request.status.data.RequestStatus;
import fr.cls.atoll.motu.web.common.format.OutputFormat;
import fr.cls.atoll.motu.web.common.utils.CoordinateUtils;
import fr.cls.atoll.motu.web.dal.DALManager;
import fr.cls.atoll.motu.web.dal.config.DALConfigManager;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.request.cdo.CDOManager;
import fr.cls.atoll.motu.web.dal.request.cdo.ICDOManager;
import fr.cls.atoll.motu.web.dal.request.netcdf.NetCdfWriter;
import fr.cls.atoll.motu.web.dal.request.status.DALLocalStatusManager;
import fr.cls.atoll.motu.web.dal.request.status.DALRedisStatusManager;
import fr.cls.atoll.motu.web.dal.request.status.IDALRequestStatusManager;
import fr.cls.atoll.motu.web.dal.tds.ncss.NetCdfSubsetService;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis2D;

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
    private IDALRequestStatusManager dalRequestStatusManager;

    public DALRequestManager() {
        cdoManager = new CDOManager();
    }

    @Override
    public void init() throws MotuException {
        if (DALManager.getInstance().getConfigManager().getMotuConfig().getRedisConfig() != null) {
            dalRequestStatusManager = new DALRedisStatusManager();
        } else {
            dalRequestStatusManager = new DALLocalStatusManager();
        }
        dalRequestStatusManager.init();
    }

    @Override
    public void downloadProduct(ConfigService cs, RequestProduct rp) throws MotuException {
        String ncssValue = cs.getCatalog().getNcss();
        boolean isNcssStatusEnabled = "enabled".equalsIgnoreCase(ncssValue);

        // Detect NCSS or OpenDAP
        try {
            if (isNcssStatusEnabled) {
                downloadWithNCSS(rp);
            } else {
                downloadWithOpenDap(rp);
            }
        } catch (MotuException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error while downloading product ncss=" + isNcssStatusEnabled, e);
            throw new MotuException(ErrorType.SYSTEM, "Error while downloading product ncss=" + isNcssStatusEnabled, e);
        }
    }

    private void downloadWithOpenDap(RequestProduct rp) throws MotuInvalidDateRangeException, MotuExceedingCapacityException,
            MotuNotImplementedException, MotuInvalidDepthRangeException, MotuInvalidLatLonRangeException, NetCdfVariableException, MotuNoVarException,
            NetCdfVariableNotFoundException, MotuException, IOException {
        rp.getDatasetManager().extractData();
    }

    private void downloadWithNCSS(RequestProduct rp)
            throws MotuInvalidDepthRangeException, NetCdfVariableException, MotuException, IOException, InterruptedException {
        // Extract criteria collect
        ExtractCriteriaDatetime time = rp.getCriteriaDateTime();
        ExtractCriteriaLatLon latlon = rp.getCriteriaLatLon();
        ExtractCriteriaDepth depth = rp.getCriteriaDepth();
        Set<String> var = rp.getRequestProductParameters().getVariables().keySet();

        // Create output NetCdf file to deliver to the user (equivalent to opendap)
        // String fname = NetCdfWriter.getUniqueNetCdfFileName(p.getProductId());
        String fname = computeDownloadFileName(rp.getProduct().getProductId(), rp.getRequestId());
        rp.getRequestProductParameters().setExtractFilename(fname);
        String extractDirPath = BLLManager.getInstance().getConfigManager().getMotuConfig().getExtractionPath();

        // Create and initialize selection
        NetCdfSubsetService ncss = new NetCdfSubsetService();
        ncss.setTimeSubset(time);
        ncss.setDepthSubset(depth);
        ncss.setVariablesSubset(var);
        ncss.setOutputFormat(rp.getExtractionParameters().getDataOutputFormat());
        ncss.setncssURL(rp.getProduct().getLocationDataNCSS());
        ncss.setProductMetadata(rp.getProduct().getProductMetaData());

        double leftLonRequested = latlon.getLowerLeftLon();
        double rightLonRequested = latlon.getLowerRightLon();

        // Check if it is a full world request
        // axisXMin coordinates always < 180
        double axisXMin = CoordinateUtils.getLongitudeM180P180(rp.getProduct().getProductMetaData().getLonAxisMinValue());
        double axisXMax = CoordinateUtils.getLongitudeGreaterOrEqualsThanLongitudeMin(rp.getProduct().getProductMetaData().getLonAxisMaxValue(),
                                                                                      axisXMin);
        double leftLon = CoordinateUtils.getLongitudeJustLowerThanLongitudeMax(CoordinateUtils
                .getLongitudeGreaterOrEqualsThanLongitudeMin(latlon.getLowerLeftLon(), axisXMin), axisXMax);
        double rightLon = CoordinateUtils.getLongitudeGreaterOrEqualsThanLongitudeMin(latlon.getLowerRightLon(), axisXMin);
        CoordinateAxis cAxisLon = rp.getProduct().getProductMetaData().getLonAxis();
        double xInc = 0;
        if (cAxisLon instanceof CoordinateAxis1D) {
            xInc = ((CoordinateAxis1D) cAxisLon).getIncrement();
            if (leftLon < rightLon) {
                runUniqRqt(ncss,
                           fname,
                           extractDirPath,
                           rp,
                           new ExtractCriteriaLatLon(latlon.getLatLonRect().getLatMin(), leftLon, latlon.getLatLonRect().getLatMax(), rightLon));
                // Also compare with Math.abs and xInc, just to avoid using BigDecimal to avoid double
                // precision
            } else if ((leftLon == rightLon && leftLonRequested == rightLonRequested)
                    || (Math.abs(rightLon - leftLon) < xInc && Math.abs(leftLonRequested - rightLonRequested) < xInc)) {
                runUniqRqt(ncss,
                           fname,
                           extractDirPath,
                           rp,
                           new ExtractCriteriaLatLon(
                                   latlon.getLatLonRect().getLatMin(),
                                   leftLon,
                                   latlon.getLatLonRect().getLatMax(),
                                   rightLon + xInc));
                fixSubsetterIssueToKeepOnlyOneLongitude(extractDirPath + "/" + fname);
            } else {
                if (leftLon <= axisXMax) {
                    // [axisXMin] rightLon]] [[leftLon [axisXMax]
                    runWithSeveralRqt(ncss, fname, extractDirPath, rp);
                } else {
                    // Here we cut the easter boundary (axisXMax)
                    // [axisXMin] rightLon]] [axisXMax] [[leftLon
                    runUniqRqt(ncss,
                               fname,
                               extractDirPath,
                               rp,
                               new ExtractCriteriaLatLon(latlon.getLowerLeftLat(), leftLon, latlon.getUpperRightLat(), axisXMax));
                }

            }
        } else if (cAxisLon instanceof CoordinateAxis2D) {
            // Curvilinear projection, mean lat and lon axis depends on XC and YC
            runUniqRqt(ncss,
                       fname,
                       extractDirPath,
                       rp,
                       new ExtractCriteriaLatLon(latlon.getLatLonRect().getLatMin(), leftLon, latlon.getLatLonRect().getLatMax(), rightLon));
        }

    }

    private void fixSubsetterIssueToKeepOnlyOneLongitude(String ncFilePath) throws IOException, MotuException {
        // Hack: Here 2 longitudes are returned whereas only one longitude is asked
        // In order to return one point, nc file has to be updated
        String ncFilePathOrig = ncFilePath + "-orig.nc";
        Files.move(Paths.get(ncFilePath), Paths.get(ncFilePathOrig), StandardCopyOption.REPLACE_EXISTING);

        try (NetcdfFile ncFileOrig = NetcdfFile.open(ncFilePathOrig);) {
            NetCdfWriter ncW = new NetCdfWriter(ncFilePath, OutputFormat.NETCDF);

            for (Attribute att : ncFileOrig.getGlobalAttributes()) {
                ncW.writeGlobalAttribute(att);
            }

            ucar.nc2.Dimension dLongDest = null;
            ucar.nc2.Dimension dLongOrig = null;
            for (ucar.nc2.Dimension dOrig : ncFileOrig.getDimensions()) {
                String dimensionNameOrig = dOrig.getFullName();
                if (dimensionNameOrig.contains("lon")) {
                    if (dOrig.getLength() > 1) {
                        dLongOrig = dOrig;
                        ucar.nc2.Dimension d2 = new ucar.nc2.Dimension(dimensionNameOrig, 1);
                        dLongDest = d2;
                        ncW.putDimension(d2);
                    } else {
                        ncW.putDimension(dOrig);
                    }
                } else {
                    ncW.putDimension(dOrig);
                }
            }

            for (Variable vOrig : ncFileOrig.getVariables()) {
                Variable vDest = new Variable(vOrig);
                if (isLongitudeVar(vOrig) && vOrig.getShape()[0] == 2) {
                    vDest = new Variable(vOrig);
                    vDest.getDimension(0).setLength(1);
                    vDest.resetShape();
                } else if (vOrig.getDimensions().contains(dLongOrig) && !isLongitudeVar(vOrig)) {
                    int longIndex = vOrig.getDimensions().indexOf(dLongOrig);
                    vDest = new Variable(vOrig);
                    vDest.getDimension(longIndex).setLength(1);
                    vDest.resetShape();
                }
                ncW.putVariables(vDest.getFullName(), vDest);
            }

            try {
                ncW.writeVariableInNetCdfFileAndSetNetcdfFileInCreateMode(null);
            } catch (MotuExceedingCapacityException e1) {
                LOGGER.error("Trying to write 1 point", e1);
            }
            for (Variable vOrig : ncFileOrig.getVariables()) {
                try {
                    Array ar = vOrig.read();
                    if (isLongitudeVar(vOrig) && ar.getSize() == 2) {
                        Variable vDest = new Variable(vOrig);
                        vDest.getDimension(0).setLength(1);
                        vDest.resetShape();
                        ar = vOrig.read(null, new int[] { 1 });
                    } else if (vOrig.getDimensions().contains(dLongDest) && !isLongitudeVar(vOrig)) {
                        int longIndex = vOrig.getDimensions().indexOf(dLongDest);
                        int[] arShape = ar.getShape();
                        if (arShape[longIndex] > 1) {
                            arShape[longIndex] = arShape[longIndex] - 1;
                        }
                        ar = vOrig.read(null, arShape);
                    }
                    ncW.writeVariableData(vOrig, ar);
                } catch (InvalidRangeException e) {
                    LOGGER.error("Fixing one point issue: Error while writing", e);
                }
            }
            ncW.getNcfileWriter().flush();
            ncW.getNcfileWriter().close();
        }
        Files.delete(Paths.get(ncFilePathOrig));
    }

    private boolean isLongitudeVar(Variable v) {
        ucar.nc2.Attribute sn = v.findAttribute("standard_name");
        return sn != null && sn.getStringValue().equalsIgnoreCase("longitude");
    }

    private void runUniqRqt(NetCdfSubsetService ncss,
                            String fname,
                            String extractDirPath,
                            RequestProduct rp,
                            ExtractCriteriaLatLon extractCriteriaLatLon)
            throws MotuInvalidDepthRangeException, NetCdfVariableException, MotuException, IOException, InterruptedException {
        ncss.setOutputFile(fname);
        ncss.setOutputDir(extractDirPath);
        ncss.setGeoSubset(extractCriteriaLatLon);
        ncssRequest(rp, ncss);
    }

    private void runWithSeveralRqt(NetCdfSubsetService ncss, String fname, String extractDirPath, RequestProduct rp) throws MotuException {
        ExtractCriteriaLatLon latlon = rp.getCriteriaLatLon();
        ExtractCriteriaDepth depth = rp.getCriteriaDepth();
        boolean canRequest = true;
        if (rp.getProduct().getProductMetaData().hasZAxis()) {
            // Check if only one depth is requested. This is because the merge of two netcdf file needs a
            // lot
            // of RAM resources, so to avoid the server miss RAM we do not authorized request on several
            // depths.
            double[] depths = (double[]) rp.getProduct().getZAxisData().get1DJavaArray(double.class);

            int fromDepthIndex = CoordinateUtils.findMinDepthIndex(depths, depth.getFrom());
            int toDepthIndex = CoordinateUtils.findMaxDepthIndex(depths, depth.getTo());

            canRequest = fromDepthIndex != -1 && toDepthIndex != -1 && fromDepthIndex == toDepthIndex;
        }
        if (canRequest) {
            try {
                runRequestWithCDOMergeTool(rp, ncss, latlon, extractDirPath, fname);
            } catch (MotuException e) {
                throw e;
            } catch (Exception e) {
                throw new MotuException(ErrorType.SYSTEM, "Error while running request with CDO merge tool", e);
            }
        } else {
            throw new MotuException(ErrorType.TOO_DEPTH_REQUESTED, "There is more than one depth in this request which needs merge procedure.");
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
    private void runRequestWithCDOMergeTool(RequestProduct rp,
                                            NetCdfSubsetService ncss,
                                            ExtractCriteriaLatLon latlon,
                                            String extractDirPath,
                                            String fname)
            throws Exception {
        cdoManager.runRequestWithCDOMergeTool(rp, ncss, latlon, extractDirPath, fname, this);
    }

    private String computeDownloadFileName(String productId, String requestId) {
        String fileName = DALManager.getInstance().getConfigManager().getMotuConfig().getDownloadFileNameFormat();
        RequestStatus requestStatus = dalRequestStatusManager.getRequestStatus(requestId);
        fileName = fileName.replace(DALConfigManager.FILENAME_FORMAT_PRODUCT_ID, productId);
        fileName = fileName.replace(DALConfigManager.FILENAME_FORMAT_REQUESTID, requestStatus.getTime());
        return fileName;
    }

    @Override
    public void ncssRequest(RequestProduct rp, NetCdfSubsetService ncss)
            throws MotuInvalidDepthRangeException, NetCdfVariableException, MotuException, IOException, InterruptedException {
        // Run rest query (unitary or concat depths)
        Array zAxisData = null;
        if (rp.getProduct().getProductMetaData().hasZAxis()) {

            // Dataset available depths
            zAxisData = rp.getProduct().getZAxisData();
            long alev = zAxisData.getSize();

            // Pass data to TDS-NCSS subsetter
            ncss.setDepthAxis(zAxisData);
            Range zRange = getZRange(rp);
            ncss.setDepthRange(zRange);

            // Z-Range selection update
            int zlev = zRange.length();
            if ((zlev == 1 && ncss.hasVariablesWithDepthDim()) || zlev == alev) {
                ncss.unitRequestNCSS(ncss.getVariablesSubset()); // 1-level or ALL levels (can be done with
                                                                 // TDS-NCSS)
            } else {
                ncss.concatDepths(); // True depth Subset with CDO operators (needs concatenation)
            }
        } else {
            ncss.unitRequestNCSS(ncss.getVariablesSubset()); // No depth axis -> request without depths
        }
        rp.getDataBaseExtractionTimeCounter().addReadingTime(ncss.getReadingTimeInNanoSec());
        rp.getDataBaseExtractionTimeCounter().addWritingTime(ncss.getWritingTimeInNanoSec());
    }

    /** {@inheritDoc} */
    @Override
    public void stop() {
        cdoManager.stop();
    }

    /**
     * Gets Z range and ranges values to be extracted.
     * 
     * @throws MotuInvalidDepthRangeException
     * @throws MotuException
     * @throws NetCdfVariableException
     * 
     */
    public Range getZRange(RequestProduct requestProduct) throws MotuException, MotuInvalidDepthRangeException {
        Range zRange = null;
        if (requestProduct.getProduct().getProductMetaData().hasZAxis()) {
            Array zAxisData = requestProduct.getProduct().getZAxisData();
            ExtractCriteriaDepth extractCriteriaDepth = requestProduct.getRequestProductParameters().findCriteriaDepth();
            if (extractCriteriaDepth != null) {
                zRange = extractCriteriaDepth.toRange(zAxisData, new double[] { Double.MAX_VALUE, Double.MAX_VALUE });
            }
        }

        return zRange;
    }

    @Override
    public IDALRequestStatusManager getDalRequestStatusManager() {
        return dalRequestStatusManager;
    }
}
