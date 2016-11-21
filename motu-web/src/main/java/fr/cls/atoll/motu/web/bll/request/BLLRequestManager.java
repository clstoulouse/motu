package fr.cls.atoll.motu.web.bll.request;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.api.message.xml.StatusModeType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.ExceptionUtils;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuExceptionBase;
import fr.cls.atoll.motu.web.bll.exception.NotEnoughSpaceException;
import fr.cls.atoll.motu.web.bll.request.cleaner.RequestCleanerDaemonThread;
import fr.cls.atoll.motu.web.bll.request.model.ProductResult;
import fr.cls.atoll.motu.web.bll.request.model.RequestDownloadStatus;
import fr.cls.atoll.motu.web.bll.request.model.RequestProduct;
import fr.cls.atoll.motu.web.bll.request.queueserver.IQueueServerManager;
import fr.cls.atoll.motu.web.bll.request.queueserver.QueueServerManager;
import fr.cls.atoll.motu.web.bll.request.queueserver.queue.log.QueueLogError;
import fr.cls.atoll.motu.web.bll.request.queueserver.queue.log.QueueLogInfo;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.common.utils.TimeUtils;
import fr.cls.atoll.motu.web.common.utils.UnitUtils;
import fr.cls.atoll.motu.web.dal.DALManager;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.config.xml.model.QueueServerType;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData.CatalogType;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.usl.request.actions.AbstractAction;

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

    public static final long REQUEST_TIMEOUT_MSEC = 3600000; // 1hour

    private IRequestIdManager requestIdManager;
    private Map<Long, RequestDownloadStatus> requestIdStatusMap;
    private ConcurrentHashMap<Long, AbstractAction> actionMap;
    private ConcurrentHashMap<Long, StatusModeType> actionStatus;
    private IQueueServerManager queueServerManager;
    private UserRequestCounter userRequestCounter;
    private RequestCleanerDaemonThread requestCleanerDaemonThread;

    public BLLRequestManager() {
        requestIdManager = new RequestIdManager();
        requestIdStatusMap = new HashMap<Long, RequestDownloadStatus>();
        actionMap = new ConcurrentHashMap<>();
        actionStatus = new ConcurrentHashMap<>();
        userRequestCounter = new UserRequestCounter();
        queueServerManager = new QueueServerManager();
    }

    @Override
    public void init() throws MotuException {
        queueServerManager.init();

        // requestCleanerDaemonThread must be instantiated here because it uses BLLRequestManager instance in
        // its constructor. It so avoid a infinite stack loop (StackOverflowError).
        requestCleanerDaemonThread = new RequestCleanerDaemonThread() {

            /** {@inheritDoc} */
            @Override
            public void onThreadStopped() {
                super.onThreadStopped();
                synchronized (BLLRequestManager.this) {
                    BLLRequestManager.this.notify();
                }
            }

        };
        requestCleanerDaemonThread.start();
    }

    @Override
    public void stop() {
        requestCleanerDaemonThread.setDaemonStoppingASAP(true);
        synchronized (this) {
            if (!requestCleanerDaemonThread.isDaemonStopped()) {
                try {
                    this.wait(REQUEST_TIMEOUT_MSEC);
                } catch (InterruptedException e) {
                    LOGGER.error("Error during wait while stopping daemon: " + requestCleanerDaemonThread.getName());
                }
            }
        }
        queueServerManager.stop();
    }

    /** {@inheritDoc} */
    @Override
    public List<Long> getRequestIds() {
        return new ArrayList<Long>(actionMap.keySet());
    }

    @Override
    public RequestDownloadStatus getDownloadRequestStatus(Long requestId_) {
        return requestIdStatusMap.get(requestId_);
    }

    @Override
    public StatusModeType getRequestStatus(Long requestId_) {
        return actionStatus.get(requestId_);
    }

    @Override
    public AbstractAction getRequestAction(Long requestId_) {
        return actionMap.get(requestId_);
    }

    /** {@inheritDoc} */
    @Override
    public ProductResult download(ConfigService cs_, RequestProduct reqProduct_, AbstractAction action) {
        long requestId = download(false, cs_, reqProduct_, action);

        ProductResult p = new ProductResult();
        RequestDownloadStatus rds = getDownloadRequestStatus(requestId);
        if (rds != null) {
            if (rds.getRunningException() != null) {
                p.setRunningException(rds.getRunningException());
            }
        } else {
            LOGGER.error("RequestDownloadStatus is null for requestId=" + requestId
                    + ". The parameter \"cleanRequestInterval\" in motuConfig is certainly to low for the current request which has certainly takes more times. So cleaner has remove the request status whereas the request was currently in progress. Solution is to set a value greater for \"cleanRequestInterval\" or understand why this request takes so much time to end.");
        }
        p.setProductFileName(reqProduct_.getRequestProductParameters().getExtractFilename());

        return p;
    }

    /** {@inheritDoc} */
    @Override
    public long downloadAsynchonously(ConfigService cs_, RequestProduct reqProduct_, AbstractAction action) {
        return download(true, cs_, reqProduct_, action);
    }

    private long download(boolean isAsynchronous, final ConfigService cs_, final RequestProduct reqProduct_, AbstractAction action) {
        final RequestDownloadStatus rds = initRequest(reqProduct_, action);
        Thread t = new Thread("download isAsynchRqt=" + Boolean.toString(isAsynchronous) + " - " + rds.getRequestId()) {

            /** {@inheritDoc} */
            @Override
            public void run() {
                download(rds, cs_);
                logQueueInfo(rds);
            }

        };
        t.setDaemon(true);
        t.start();
        if (!isAsynchronous) {
            try {
                t.join();
            } catch (InterruptedException e) {
                LOGGER.error("InterruptedException download thread join inteeruption, requestId=" + rds.getRequestId(), e);
            }
        }

        return rds.getRequestId();
    }

    /**
     * .
     */
    private void logQueueInfo(RequestDownloadStatus rds) {
        QueueLogInfo qli = new QueueLogInfo();
        qli.setAmountDataSize(UnitUtils.toMegaBytes(rds.getSizeInBits()));
        qli.setCompressingTime(TimeUtils.nanoToMillisec(rds.getDataBaseExtractionTimeCounter().getCompressingTime()));
        qli.setCopyingTime(TimeUtils.nanoToMillisec(rds.getDataBaseExtractionTimeCounter().getCopyingTime()));
        qli.setReadingTime(TimeUtils.nanoToMillisec(rds.getDataBaseExtractionTimeCounter().getReadingTime()));
        qli.setWritingTime(TimeUtils.nanoToMillisec(rds.getDataBaseExtractionTimeCounter().getWritingTime()));

        qli.setDownloadUrlPath(BLLManager.getInstance().getCatalogManager().getProductManager()
                .getProductDownloadHttpUrl(rds.getRequestProduct().getRequestProductParameters().getExtractFilename()));
        // qli.setEncoding(""); Set by default
        qli.setExtractionParameters(rds.getRequestProduct().getExtractionParameters());
        qli.setExtractLocationData(rds.getRequestProduct().getRequestProductParameters().getExtractLocationData());

        Calendar c = Calendar.getInstance();

        c.setTimeInMillis(rds.getCreationDateTime());
        qli.setInQueueTime(c.getTime());

        if (rds.getStartProcessingDateTime() > 0) {
            c.setTimeInMillis(rds.getStartProcessingDateTime());
            qli.setStartTime(c.getTime());
        }

        if (rds.getEndProcessingDateTime() > 0) {
            c.setTimeInMillis(rds.getEndProcessingDateTime());
            qli.setEndTime(c.getTime());
        }

        // SMA: Not sure that this field as a real sense, keep it for retro compatibility between Motu
        // versions 2.x and 3.x
        qli.setPreparingTime(TimeUtils.nanoToMillisec(rds.getDataBaseExtractionTimeCounter().getReadingTime()));

        qli.setQueueId(rds.getQueueId());
        qli.setQueueDesc(rds.getQueueDescription());

        MotuException me = rds.getRunningException();
        if (me != null) {
            qli.setQueueLogError(new QueueLogError(me.getErrorType(), me.getMessage()));
        }

        qli.setRequestId(rds.getRequestId());

        qli.setScriptVersion(rds.getRequestProduct().getExtractionParameters().getScriptVersion());
        LOGGER.info(qli);
    }

    private RequestDownloadStatus initRequest(RequestProduct requestProduct_, AbstractAction action) {
        Long requestId = initRequest(action);
        RequestDownloadStatus requestDownloadStatus = new RequestDownloadStatus(requestId, requestProduct_);
        requestIdStatusMap.put(requestId, requestDownloadStatus);
        return requestDownloadStatus;
    }

    @Override
    public Long initRequest(AbstractAction action) {
        final long requestId = getNewRequestId();
        actionMap.putIfAbsent(requestId, action);
        actionStatus.putIfAbsent(requestId, StatusModeType.PENDING);
        return requestId;
    }

    @Override
    public void setActionStatus(Long requestId, StatusModeType status) {
        if (actionMap.containsKey(requestId) && actionStatus.containsKey(requestId)) {
            actionStatus.put(requestId, status);
        }
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
                    ErrorType.EXCEEDING_USER_CAPACITY,
                    "Maximum number of running request reached" + userIdMsg + ", x"
                            + (userId_ == null
                                    ? BLLManager.getInstance().getConfigManager().getMotuConfig().getQueueServerConfig().getMaxPoolAnonymous()
                                    : BLLManager.getInstance().getConfigManager().getMotuConfig().getQueueServerConfig().getMaxPoolAuth()));
        } else {
            userRequestCounter.onNewRequestForUser(userId_);
        }
    }

    private void download(RequestDownloadStatus rds_, ConfigService cs_) {
        RequestDownloadStatus requestDownloadStatus = requestIdStatusMap.get(rds_.getRequestId());

        // If too much request for this user, throws MotuExceedingUserCapacityException
        String userId = rds_.getRequestProduct().getExtractionParameters().isAnonymousUser() ? null
                : rds_.getRequestProduct().getExtractionParameters().getUserId();
        try {
            try {
                double requestSizeInByte = getProductDataSizeIntoByte(rds_);
                rds_.setSizeInBits(new Double(requestSizeInByte * 8).longValue());
                double requestSizeInMB = UnitUtils.toMegaBytes(requestSizeInByte);

                checkNumberOfRunningRequestForUser(userId);
                checkMaxSizePerFile(cs_.getCatalog().getType(), requestSizeInMB);
                checkFreeSpace(requestSizeInMB);

                downloadSafe(requestDownloadStatus, requestSizeInMB, cs_);
            } finally {
                userRequestCounter.onRequestStoppedForUser(userId);
            }
        } catch (MotuException e) {
            requestDownloadStatus.setRunningException(e);
        } catch (Exception e) {
            requestDownloadStatus.setRunningException(new MotuException(ExceptionUtils.getErrorType(e), e));
        }
    }

    private void checkFreeSpace(double fileSize) throws MotuException {
        File extractionDirectory = new File(BLLManager.getInstance().getConfigManager().getMotuConfig().getExtractionPath());
        if (!extractionDirectory.exists()) {
            LOGGER.error("The extraction folder does not exists: " + extractionDirectory.exists());
            throw new MotuException(ErrorType.SYSTEM, "The extraction folder does not exists: " + extractionDirectory.exists());
        } else {
            if (UnitUtils.toMegaBytes(extractionDirectory.getFreeSpace()) <= fileSize) {
                throw new NotEnoughSpaceException("There is not enough disk space available to generate the file result and to satisfy this request");
            }
        }
    }

    private void checkMaxSizePerFile(String catalogType, double fileSize) throws MotuException {
        double maxSizePerFile = -1;
        if (CatalogType.FILE.name().toUpperCase().equals(catalogType.toUpperCase())) {
            maxSizePerFile = BLLManager.getInstance().getConfigManager().getMotuConfig().getMaxSizePerFile().doubleValue();
        } else {
            maxSizePerFile = BLLManager.getInstance().getConfigManager().getMotuConfig().getMaxSizePerFileSub().doubleValue();
        }
        if (maxSizePerFile < fileSize) {
            throw new MotuException(
                    ErrorType.EXCEEDING_CAPACITY,
                    "The result file size " + fileSize + "Mb shall be less than " + maxSizePerFile + "Mb",
                    new String[] { Integer.toString(new Double(fileSize).intValue()), Integer.toString(new Double(maxSizePerFile).intValue()) });
        }
    }

    private void downloadSafe(RequestDownloadStatus requestDownloadStatus, double requestSizeInMB, ConfigService cs_) throws MotuException {
        // The request download is delegated to a download request manager
        queueServerManager.execute(requestDownloadStatus, cs_, requestSizeInMB);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws MotuExceptionBase
     */
    @Override
    public double getProductDataSizeIntoByte(RequestProduct requestProduct_) throws MotuException {
        return getProductDataSizeIntoByte(new RequestDownloadStatus(-1L, requestProduct_));
    }

    private double getProductDataSizeIntoByte(RequestDownloadStatus rds_) throws MotuException {
        return UnitUtils.toBytes(DALManager.getInstance().getCatalogManager().getProductManager().getProductDataSizeRequest(rds_));
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
        actionMap.remove(requestId);
        actionStatus.remove(requestId);
    }

}
