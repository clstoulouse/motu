package fr.cls.atoll.motu.web.bll.request;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
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
import fr.cls.atoll.motu.web.bll.request.queueserver.queue.log.QueueLogError;
import fr.cls.atoll.motu.web.bll.request.queueserver.queue.log.QueueLogInfo;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.common.utils.UnitUtils;
import fr.cls.atoll.motu.web.dal.DALManager;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.config.xml.model.QueueServerType;
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
    private UserRequestCounter userRequestCounter;

    public BLLRequestManager() {
        requestIdManager = new RequestIdManager();
        requestIdStatusMap = new HashMap<Long, RequestDownloadStatus>();
        userRequestCounter = new UserRequestCounter();

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
    public RequestDownloadStatus getRequestStatus(Long requestId_) {
        return requestIdStatusMap.get(requestId_);
    }

    /** {@inheritDoc} */
    @Override
    public ProductResult download(ConfigService cs_, Product product_, ExtractionParameters extractionParameters) {
        long requestId = download(false, cs_, product_, extractionParameters);

        ProductResult p = new ProductResult();
        RequestDownloadStatus rds = getRequestStatus(requestId);
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

        logQueueInfo(rds, product_, extractionParameters);

        return requestId;
    }

    /**
     * .
     */
    private void logQueueInfo(RequestDownloadStatus rds, Product product_, ExtractionParameters extractionParameters) {
        QueueLogInfo qli = new QueueLogInfo();
        // TODO SMA set all qli fields

        qli.setAmountDataSize(UnitUtils.toMegaBytes(rds.getSizeInBits()));
        qli.setCompressingTime(product_.getCompressingTimeAsMilliSeconds());
        qli.setCopyingTime(product_.getCopyingTimeAsMilliSeconds());
        qli.setReadingTime(product_.getReadingTimeAsMilliSeconds());
        qli.setWritingTime(product_.getWritingTimeAsMilliSeconds());

        qli.setDownloadUrlPath(BLLManager.getInstance().getCatalogManager().getProductManager()
                .getProductDownloadHttpUrl(product_.getExtractFilename()));
        // qli.setEncoding(""); Set by default
        qli.setExtractionParameters(extractionParameters);
        qli.setExtractLocationData(product_.getExtractLocationData());

        Calendar c = Calendar.getInstance();

        c.setTimeInMillis(rds.getCreationDateTime());
        qli.setInQueueTime(c.getTime());

        c.setTimeInMillis(rds.getStartProcessingDateTime());
        qli.setStartTime(c.getTime());

        c.setTimeInMillis(rds.getEndProcessingDateTime());
        qli.setEndTime(c.getTime());

        // SMA: Not sure that this field as a real sense, keep it for retro compatibility between Motu
        // versions 2.x and 3.x
        qli.setPreparingTime(product_.getReadingTimeAsMilliSeconds());

        qli.setQueueId(rds.getQueueId());
        qli.setQueueDesc(rds.getQueueDescription());

        MotuException me = rds.getRunningException();
        if (me != null) {
            qli.setQueueLogError(new QueueLogError(me.getErrorType(), me.getMessage()));
        }

        qli.setRequestId(rds.getRequestId());
        LOGGER.info(qli);
    }

    private RequestDownloadStatus initRequest(String userId, String userHost) {
        final long requestId = getNewRequestId();
        RequestDownloadStatus requestDownloadStatus = new RequestDownloadStatus(requestId, userId, userHost);
        requestIdStatusMap.put(requestId, requestDownloadStatus);
        return requestDownloadStatus;
    }

    /**
     * .
     * 
     * @param userId
     * @param queueManagement
     * @return true if too much
     */
    private boolean isNumberOfRequestTooHighForUser(String userId) {
        int countRequest = userRequestCounter.getRequestCount(userId);
        countRequest++; // Add the current request
        boolean isAnonymousUser = (userId == null);
        boolean isNumberOfRequestTooHighForUser = (isAnonymousUser && getQueueServerConfig().getMaxPoolAnonymous() > 0
                && countRequest > getQueueServerConfig().getMaxPoolAnonymous())
                || (!isAnonymousUser && getQueueServerConfig().getMaxPoolAuth() > 0 && countRequest >= getQueueServerConfig().getMaxPoolAuth());
        String logUserId = isAnonymousUser ? "anonymous" : userId;
        LOGGER.info("Check active request number for [userId=" + logUserId + "]: x" + countRequest + ", isNumberOfRequestTooHighForUser="
                + isNumberOfRequestTooHighForUser);
        return isNumberOfRequestTooHighForUser;
    }

    /**
     * .
     * 
     * @return
     */
    private QueueServerType getQueueServerConfig() {
        return BLLManager.getInstance().getConfigManager().getMotuConfig().getQueueServerConfig();
    }

    public synchronized void checkNumberOfRunningRequestForUser(String userId_) throws MotuException {
        if (isNumberOfRequestTooHighForUser(userId_)) {

            String userIdMsg = "";
            if (userId_ != null) {
                userIdMsg = " for user: " + userId_;
            }
            throw new MotuException(
                    ErrorType.EXCEEDING_QUEUE_CAPACITY,
                    "Maximum number of running request reached" + userIdMsg + ", x"
                            + (userId_ == null
                                    ? BLLManager.getInstance().getConfigManager().getMotuConfig().getQueueServerConfig().getMaxPoolAnonymous()
                                    : BLLManager.getInstance().getConfigManager().getMotuConfig().getQueueServerConfig().getMaxPoolAuth()));
        } else {
            userRequestCounter.onNewRequestForUser(userId_);
        }
    }

    private void download(RequestDownloadStatus rds_,
                          ExtractionParameters extractionParameters,
                          ConfigService cs_,
                          Product product_,
                          long requestId) {
        RequestDownloadStatus requestDownloadStatus = requestIdStatusMap.get(requestId);

        // If too much request for this user, throws MotuExceedingUserCapacityException
        String userId = extractionParameters.isAnonymousUser() ? null : extractionParameters.getUserId();
        try {
            try {
                checkNumberOfRunningRequestForUser(userId);

                double requestSizeInByte = getRequestSizeInByte(extractionParameters, product_);
                rds_.setSizeInBits(new Double(requestSizeInByte * 8).longValue());
                double requestSizeInMB = UnitUtils.toMegaBytes(requestSizeInByte);

                File extractionDirectory = new File(BLLManager.getInstance().getConfigManager().getMotuConfig().getExtractionPath());

                if (UnitUtils.toMegaBytes(extractionDirectory.getFreeSpace()) > requestSizeInMB) {
                    downloadSafe(requestDownloadStatus, requestSizeInMB, extractionParameters, cs_, product_);
                } else {
                    throw new NotEnoughSpaceException(
                            "There is not enough disk space available to generate the file result and to satisfy this request");
                }
            } finally {
                userRequestCounter.onRequestStoppedForUser(userId);
            }
        } catch (MotuException e) {
            requestDownloadStatus.setRunningException(e);
        }
    }

    private void downloadSafe(RequestDownloadStatus requestDownloadStatus,
                              double requestSizeInMB,
                              ExtractionParameters extractionParameters,
                              ConfigService cs_,
                              Product product_) throws MotuException {
        // Clear and update the product in case of the instance have already been used for other
        // calculation.
        clearAndUpdateProductDataSet(product_,
                                     extractionParameters.getListVar(),
                                     extractionParameters.getListTemporalCoverage(),
                                     extractionParameters.getListLatLonCoverage(),
                                     extractionParameters.getListDepthCoverage());
        // The request download is delegated to a download request manager
        queueServerManager.execute(requestDownloadStatus, cs_, product_, extractionParameters, requestSizeInMB);
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
            throw new MotuException(ErrorType.SYSTEM, e);
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
