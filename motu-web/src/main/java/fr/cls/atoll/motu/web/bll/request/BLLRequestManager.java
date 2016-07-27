package fr.cls.atoll.motu.web.bll.request;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuExceptionBase;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDateException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDepthException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidLatitudeException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidLongitudeException;
import fr.cls.atoll.motu.web.bll.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.web.bll.exception.NotEnoughSpaceException;
import fr.cls.atoll.motu.web.bll.request.cleaner.RequestCleanerDaemonThread;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteria;
import fr.cls.atoll.motu.web.bll.request.model.ExtractionParameters;
import fr.cls.atoll.motu.web.bll.request.model.ProductResult;
import fr.cls.atoll.motu.web.bll.request.model.RequestDownloadStatus;
import fr.cls.atoll.motu.web.bll.request.queueserver.IQueueServerManager;
import fr.cls.atoll.motu.web.bll.request.queueserver.QueueServerManager;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.common.utils.UnitUtils;
import fr.cls.atoll.motu.web.dal.DALManager;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.request.ProductSizeRequest;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;

/**
 * <br>
 * Manage incoming requests:<br>
 * - check that not too much request are sent for a same authenticated user<br>
 * - check that if the request is processed, its result will not fall down TDS or Motu due to a lack of memory
 * for example<br>
 * - ...<br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class BLLRequestManager implements IBLLRequestManager {

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger();

    private IRequestIdManager requestIdManager;
    private Map<Long, RequestDownloadStatus> requestIdStatusMap;
    private IQueueServerManager queueServerManager;

    public BLLRequestManager() {
        requestIdManager = new RequestIdManager();
        requestIdStatusMap = new HashMap<Long, RequestDownloadStatus>();
        queueServerManager = new QueueServerManager();
    }

    @Override
    public void init() throws MotuException {
        queueServerManager.init();

        new RequestCleanerDaemonThread().start();
    }

    /** {@inheritDoc} */
    @Override
    public List<Long> getRequestIds() {
        return new ArrayList<Long>(requestIdStatusMap.keySet());
    }

    /** {@inheritDoc} */
    @Override
    public RequestDownloadStatus getResquestStatus(Long requestId_) {
        return requestIdStatusMap.get(requestId_);
    }

    /** {@inheritDoc} */
    @Override
    public ProductResult download(ConfigService cs_, Product product_, ExtractionParameters extractionParameters) {
        long requestId = download(false, cs_, product_, extractionParameters);

        ProductResult p = new ProductResult();
        RequestDownloadStatus rds = getResquestStatus(requestId);
        if (rds.getRunningException() != null) {
            p.setRunningException(rds.getRunningException());
        }
        rds.setProductFileName(product_.getExtractFilename());
        p.setProductFileName(product_.getExtractFilename());

        return p;
    }

    /** {@inheritDoc} */
    @Override
    public long downloadAsynchonously(ConfigService cs_, Product product_, ExtractionParameters extractionParameters) {
        return download(true, cs_, product_, extractionParameters);
    }

    private long download(boolean isAsynchronous, final ConfigService cs_, final Product product_, final ExtractionParameters extractionParameters) {
        final RequestDownloadStatus rds = initRequest(extractionParameters.getUserId(), extractionParameters.getUserHost());
        final long requestId = rds.getRequestId();

        Thread t = new Thread("download isAsynchRqt=" + Boolean.toString(isAsynchronous) + " - " + requestId) {

            /** {@inheritDoc} */
            @Override
            public void run() {
                download(rds, extractionParameters, cs_, product_, requestId);
            }

        };
        t.setDaemon(true);
        t.start();
        if (!isAsynchronous) {
            try {
                t.join();
            } catch (InterruptedException e) {
                LOGGER.error(e);
            }
        }

        return requestId;
    }

    private RequestDownloadStatus initRequest(String userId, String userHost) {
        final long requestId = getNewRequestId();
        RequestDownloadStatus requestDownloadStatus = new RequestDownloadStatus(requestId, userId, userHost);
        requestIdStatusMap.put(requestId, requestDownloadStatus);
        return requestDownloadStatus;
    }

    public void checkNumberOfRunningRequestForUser(String userId_) throws MotuException {
        if (queueServerManager.isNumberOfRequestTooHighForUser(userId_)) {
            throw new MotuException(
                    "Maximum number of running request reached for user: " + userId_ + ", "
                            + (userId_ == null
                                    ? BLLManager.getInstance().getConfigManager().getMotuConfig().getQueueServerConfig().getMaxPoolAnonymous()
                                    : BLLManager.getInstance().getConfigManager().getMotuConfig().getQueueServerConfig().getMaxPoolAuth()));
        }
    }

    private void download(RequestDownloadStatus rds_,
                          ExtractionParameters extractionParameters,
                          ConfigService cs_,
                          Product product_,
                          long requestId) {
        RequestDownloadStatus requestDownloadStatus = requestIdStatusMap.get(requestId);

        try {
            // If too much request for this user, throws MotuExceedingUserCapacityException
            checkNumberOfRunningRequestForUser(extractionParameters.getUserId());

            double requestSizeInByte = getRequestSizeInByte(extractionParameters, product_);
            rds_.setSizeInBits(new Double(requestSizeInByte * 8).longValue());
            double requestSizeInMB = UnitUtils.toMegaBytes(requestSizeInByte);

            File extractionDirectory = new File(BLLManager.getInstance().getConfigManager().getMotuConfig().getExtractionPath());

            if (UnitUtils.toMegaBytes(extractionDirectory.getFreeSpace()) > requestSizeInMB) {

                // Clear and update the product in case of the instance have already been used for other
                // calculation.
                clearAndUpdateProductDataSet(product_,
                                             extractionParameters.getListVar(),
                                             extractionParameters.getListTemporalCoverage(),
                                             extractionParameters.getListLatLonCoverage(),
                                             extractionParameters.getListDepthCoverage());
                // The request download is delegated to a download request manager
                queueServerManager.execute(requestDownloadStatus, cs_, product_, extractionParameters, requestSizeInMB);
            } else {
                throw new NotEnoughSpaceException("There is not enough disk space available to generate the file result and to satisfy this request");
            }
        } catch (MotuException e) {
            requestDownloadStatus.setRunningException(e);
        }
    }

    private double getRequestSizeInByte(ExtractionParameters extractionParameters, Product product_) throws MotuException {
        return BLLManager.getInstance().getRequestManager().getProductDataSizeIntoByte(product_,
                                                                                       extractionParameters.getListVar(),
                                                                                       extractionParameters.getListTemporalCoverage(),
                                                                                       extractionParameters.getListLatLonCoverage(),
                                                                                       extractionParameters.getListDepthCoverage());
    }

    /**
     * {@inheritDoc}
     * 
     * @throws MotuExceptionBase
     */
    @Override
    public double getProductDataSizeIntoByte(Product product,
                                             List<String> listVar,
                                             List<String> listTemporalCoverage,
                                             List<String> listLatLongCoverage,
                                             List<String> listDepthCoverage) throws MotuException {
        return DALManager.getInstance().getCatalogManager().getProductManager()
                .getProductDataSizeRequest(product, listVar, listTemporalCoverage, listLatLongCoverage, listDepthCoverage);
    }

    /**
     * Hack due to the previous implementation of Motu. This method clear and update the dataset of the
     * provided product in contemplation of a new calculation on the product. .
     * 
     * @param product the product to clear and update
     * @param listVar the list of variable for the update
     * @param listTemporalCoverage the list of temporal coverage for the update
     * @param listLatLongCoverage the list of lat/long coverage for the update
     * @param listDepthCoverage the list of depth coverage for the update
     * @throws MotuException
     */
    private void clearAndUpdateProductDataSet(Product product,
                                              List<String> listVar,
                                              List<String> listTemporalCoverage,
                                              List<String> listLatLongCoverage,
                                              List<String> listDepthCoverage) throws MotuException {
        try {
            product.resetDataset();
            product.updateVariables(listVar);
            List<ExtractCriteria> criteria = new ArrayList<ExtractCriteria>();
            ProductSizeRequest.createCriteriaList(listTemporalCoverage, listLatLongCoverage, listDepthCoverage, criteria);
            product.updateCriteria(criteria);
        } catch (MotuNotImplementedException | MotuInvalidDateException | MotuInvalidDepthException | MotuInvalidLatitudeException
                | MotuInvalidLongitudeException e) {
            throw new MotuException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public long getNewRequestId() {
        return requestIdManager.getNewRequestId();
    }

    /** {@inheritDoc} */
    @Override
    public double getProductMaxAllowedDataSizeIntoByte(Product product) throws MotuException {
        return UnitUtils.toBytes(BLLManager.getInstance().getRequestManager().getQueueServerManager().getMaxDataThreshold());
    }

    /** {@inheritDoc} */
    @Override
    public IQueueServerManager getQueueServerManager() {
        return queueServerManager;
    }

    /** {@inheritDoc} */
    @Override
    public boolean[] deleteFiles(String[] urls) {
        boolean[] fileDeletionStatus = new boolean[urls.length];
        int cpteFile = 0;

        String extractionPath = BLLManager.getInstance().getConfigManager().getMotuConfig().getExtractionPath();
        String downloadHttpUrl = BLLManager.getInstance().getConfigManager().getMotuConfig().getDownloadHttpUrl();

        for (String url : urls) {

            if (StringUtils.isNullOrEmpty(url)) {
                continue;
            }
            String fileName = url.replace(downloadHttpUrl, extractionPath);

            File file = new File(fileName);
            fileDeletionStatus[cpteFile] = file.delete();
            cpteFile++;
        }

        return fileDeletionStatus;
    }

    /** {@inheritDoc} */
    @Override
    public void deleteRequest(Long requestId) {
        requestIdStatusMap.remove(requestId);
    }

}
