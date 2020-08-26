package fr.cls.atoll.motu.web.dal.request;

import java.io.IOException;
import java.util.ArrayList;
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
import fr.cls.atoll.motu.web.bll.request.model.RequestProduct;
import fr.cls.atoll.motu.web.bll.request.status.data.RequestStatus;
import fr.cls.atoll.motu.web.common.utils.CoordinateUtils;
import fr.cls.atoll.motu.web.dal.DALManager;
import fr.cls.atoll.motu.web.dal.config.DALConfigManager;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.request.cdo.CDOManager;
import fr.cls.atoll.motu.web.dal.request.cdo.ICDOManager;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ProductMetaData;
import fr.cls.atoll.motu.web.dal.request.status.DALLocalStatusManager;
import fr.cls.atoll.motu.web.dal.request.status.DALRedisStatusManager;
import fr.cls.atoll.motu.web.dal.request.status.IDALRequestStatusManager;
import fr.cls.atoll.motu.web.dal.tds.ncss.NetCdfSubsetService;
import ucar.ma2.Array;
import ucar.ma2.Range;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis2D;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

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

        double axisXMin = rp.getProduct().getProductMetaData().getLonAxisMinValue();
        double axisXMax = rp.getProduct().getProductMetaData().getLonAxisMaxValue();

        // Would be strange but handle dataset with more than 360° of longitude
        if (axisXMax - axisXMin > ExtractCriteriaLatLon.LONGITUDE_TOTAL) {
            if (Math.abs(axisXMax) > Math.abs(axisXMin)) {
                axisXMax = axisXMin + ExtractCriteriaLatLon.LONGITUDE_TOTAL;
            } else {
                axisXMin = axisXMax - ExtractCriteriaLatLon.LONGITUDE_TOTAL;
            }
        }

        LatLonRect productArea = new LatLonRect(
                new LatLonPointImpl(rp.getProduct().getProductMetaData().getLatAxisMinValue(), axisXMin),
                new LatLonPointImpl(rp.getProduct().getProductMetaData().getLatAxisMaxValue(), axisXMax));

        List<ExtractCriteriaLatLon> ranges = new ArrayList<>();

        CoordinateAxis cAxisLon = rp.getProduct().getProductMetaData().getLonAxis();
        if (cAxisLon instanceof CoordinateAxis1D) {
            // Check if it is a full world request
            if (latlon.getLatLonRect() == null || productArea.containedIn(latlon.getLatLonRect())) {
                // Full world => single request
                ranges.add(new ExtractCriteriaLatLon(productArea));
            } else {
                if (Double.isNaN(latlon.getLonMin()) || latlon.getWidth() == ExtractCriteriaLatLon.LONGITUDE_TOTAL) {
                    // No constraint on origin longitude or all longitudes requested, serve all the longitudes
                    // in a single ncss query on axis lon borders
                    ranges.add(new ExtractCriteriaLatLon(latlon.getLowerLeftLat(), axisXMin, latlon.getHeight(), productArea.getWidth()));
                } else {
                    // Compute the longitude min to be requested in the closest range of axisXMin and axisXMax
                    // real values.
                    // The longitude found can be lower than axisXMin, but won't be after axisXMax.
                    double requestedLon = latlon.getLonMin();
                    while (requestedLon + ExtractCriteriaLatLon.LONGITUDE_TOTAL < axisXMin) {
                        requestedLon += ExtractCriteriaLatLon.LONGITUDE_TOTAL;
                    }
                    while (requestedLon > axisXMax || requestedLon - ExtractCriteriaLatLon.LONGITUDE_TOTAL + latlon.getWidth() > axisXMin) {
                        requestedLon -= ExtractCriteriaLatLon.LONGITUDE_TOTAL;
                    }
                    double overlay = axisXMin - requestedLon;
                    double rightWidth = Math.min(latlon.getWidth() + requestedLon, axisXMax);
                    if (overlay >= 0) { // Some longitude are not after axisXMin value (in the dataset
                                        // longitude reference)
                        double leftWidth = axisXMax - requestedLon - ExtractCriteriaLatLon.LONGITUDE_TOTAL;
                        if (leftWidth >= 0) { // Some of those longitude covers are on the left of the
                                              // axisXMax
                            ranges.add(new ExtractCriteriaLatLon(latlon.getLowerLeftLat(), axisXMax - leftWidth, latlon.getHeight(), leftWidth));
                        }
                        rightWidth -= axisXMin;
                    } else {
                        // The longitude min requested is after axisXMin
                        rightWidth -= requestedLon;
                    }
                    if (rightWidth >= 0) { // There are some longitudes within the axis limits
                        double xlow = Math.max(axisXMin, requestedLon);
                        // Find the upper coordinate for this extraction using the last lower or equals value
                        // in the longitude axis to avoid the ncss extraction to round to return a longitude
                        // more
                        int coordIndex = ((CoordinateAxis1D) cAxisLon).findCoordElement(xlow + rightWidth);
                        if (((CoordinateAxis1D) cAxisLon).getCoordValue(coordIndex) > xlow + rightWidth && coordIndex > 0) {
                            coordIndex--;
                            // Could get <0 when requesting a single point
                            rightWidth = Math.max(0, ((CoordinateAxis1D) cAxisLon).getCoordValue(coordIndex) - xlow);
                        }
                        ranges.add(new ExtractCriteriaLatLon(latlon.getLowerLeftLat(), xlow, latlon.getHeight(), rightWidth));
                    }
                }
            }

            // Range can stay empty for single longitude queries
            if (ranges.isEmpty()) {
                // Use the requested latlon for later adaptation
                ranges.add(latlon);
            }
            if (ranges.size() == 1) {
                runUniqRqt(ncss,
                           fname,
                           extractDirPath,
                           rp,
                           enlargeSingleLon(rp.getProduct().getProductMetaData(), axisXMin, axisXMax, ranges.get(0)));
            } else {
                runWithSeveralRqt(ncss, fname, extractDirPath, rp, ranges);
            }
        } else if (cAxisLon instanceof CoordinateAxis2D) {
            // Curvilinear projection, mean lat and lon axis depends on XC and YC
            runUniqRqt(ncss, fname, extractDirPath, rp, new ExtractCriteriaLatLon(productArea.intersect(latlon.getLatLonRect())));
        }
    }

    /**
     * This method is a workaround for the strange behavior of TDS that is not able on NCSS mode to respond to
     * a request with a single longitude. It is fine when there are several latitudes.
     * 
     * @param productMetaData
     * @param axisXMin
     * @param axisXMax
     * @param area
     * @return The modified area with wider longitude if it was a single longitude request.
     */
    private ExtractCriteriaLatLon enlargeSingleLon(ProductMetaData productMetaData, double axisXMin, double axisXMax, ExtractCriteriaLatLon area) {
        // Ensure flat lon query
        if (area.getWidth() == 0) {
            double requestedLon = area.getLonMin();
            if (requestedLon > axisXMax || requestedLon < axisXMin) {
                requestedLon = adaptRequestedLon(axisXMin, axisXMax, requestedLon);
            }
            CoordinateAxis1D lonAxis = (CoordinateAxis1D) productMetaData.getLonAxis();
            int lonIndex = lonAxis.findCoordElementBounded(requestedLon);
            double lonCoord = lonAxis.getCoordValue(lonIndex);
            // Ensure requested longitude within the grid
            if (lonIndex >= 0) {
                double[] lonBounds = lonAxis.getCoordBounds(lonIndex);
                double lonLow;
                double width;
                // Requested point already on the grid
                if (lonCoord == area.getLonMin()) {
                    lonLow = area.getLonMin();
                    // Divide by 4 the width to ensure not rounding to a 2nd point returned by TDS
                    width = (lonBounds[1] - lonBounds[0]) / 4;
                } else {
                    lonLow = lonBounds[0] + (lonBounds[1] - lonBounds[0]) / 4;
                    width = lonCoord - lonLow;
                }
                area = new ExtractCriteriaLatLon(area.getLatMin(), lonLow, area.getHeight(), width);
            }
        }
        return area;
    }

    /**
     * Re-adapt requested longitude to the dataset interval, to the closer as possible, also for requests
     * around the dataset border.
     * 
     * @param axisXMin
     * @param axisXMax
     * @param requestedLon
     * @return The longitude to request from, to ensure having a single point returned by TDS and respecting
     *         the choice of the closest point.
     */
    private double adaptRequestedLon(double axisXMin, double axisXMax, double requestedLon) {
        double lon = requestedLon;
        while (lon > axisXMax) {
            lon -= ExtractCriteriaLatLon.LONGITUDE_TOTAL;
        }
        if (lon < axisXMin && axisXMin - lon > lon + ExtractCriteriaLatLon.LONGITUDE_TOTAL - axisXMax) {
            lon += ExtractCriteriaLatLon.LONGITUDE_TOTAL;
        }
        return lon;
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

    private void runWithSeveralRqt(NetCdfSubsetService ncss,
                                   String fname,
                                   String extractDirPath,
                                   RequestProduct rp,
                                   List<ExtractCriteriaLatLon> ranges)
            throws MotuException {
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
                runRequestWithCDOMergeTool(rp, ncss, extractDirPath, fname, ranges);
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
     * @param ncss
     * @param p
     * @param fname
     * @param extractDirPath
     * @param ranges
     * @throws IOException
     * @throws InterruptedException
     * @throws MotuException
     * @throws NetCdfVariableException
     * @throws MotuInvalidDepthRangeException
     */
    private void runRequestWithCDOMergeTool(RequestProduct rp,
                                            NetCdfSubsetService ncss,
                                            String extractDirPath,
                                            String fname,
                                            List<ExtractCriteriaLatLon> ranges)
            throws Exception {
        cdoManager.runRequestWithCDOMergeTool(rp, ncss, extractDirPath, fname, ranges, this);
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
