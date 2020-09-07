package fr.cls.atoll.motu.web.bll.request;

import java.io.File;
import java.util.Map;
import java.util.Set;

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
import fr.cls.atoll.motu.web.bll.request.queueserver.queue.log.QueueLogInfo;
import fr.cls.atoll.motu.web.bll.request.status.BLLRequestStatusManager;
import fr.cls.atoll.motu.web.bll.request.status.IBLLRequestStatusManager;
import fr.cls.atoll.motu.web.bll.request.status.data.DownloadStatus;
import fr.cls.atoll.motu.web.bll.request.status.data.NormalStatus;
import fr.cls.atoll.motu.web.bll.request.status.data.RequestStatus;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.common.utils.UnitUtils;
import fr.cls.atoll.motu.web.dal.DALManager;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.config.xml.model.QueueServerType;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData.CatalogType;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.dal.request.status.IDALRequestStatusManager;
import fr.cls.atoll.motu.web.usl.request.actions.AbstractAction;
import fr.cls.atoll.motu.web.usl.request.actions.DownloadProductAction;
import fr.cls.atoll.motu.web.usl.wcs.request.actions.WCSGetCoverageAction;

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
    public static final String ANONYMOUS_USERID = "anonymous";

    private IBLLRequestStatusManager bllRequestStatusManager;

    private IQueueServerManager queueServerManager;
    private RequestCleanerDaemonThread requestCleanerDaemonThread;

    public BLLRequestManager() {
        queueServerManager = new QueueServerManager();
        bllRequestStatusManager = new BLLRequestStatusManager();
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
    public Set<String> getRequestIds() {
        return bllRequestStatusManager.getAllRequestId();
    }

    /**
     * {@inheritDoc}
     * 
     * @throws MotuException
     */
    @Override
    public ProductResult download(ConfigService cs, RequestProduct reqProduct, AbstractAction action) throws MotuException {
        RequestDownloadStatus rds = download(false, cs, reqProduct, action);
        ProductResult p = new ProductResult();
        if (rds.getRunningException() != null) {
            p.setRunningException(rds.getRunningException());
        }
        p.setProductFileName(reqProduct.getRequestProductParameters().getExtractFilename());

        return p;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws MotuException
     */
    @Override
    public RequestDownloadStatus downloadAsynchronously(ConfigService cs, RequestProduct reqProduct, AbstractAction action) throws MotuException {
        return download(true, cs, reqProduct, action);
    }

    private RequestDownloadStatus download(boolean isAsynchronous, final ConfigService cs, final RequestProduct reqProduct, AbstractAction action)
            throws MotuException {
        final RequestDownloadStatus rds = initRequest(reqProduct, action);
        Thread t = new Thread("download isAsynchRqt=" + Boolean.toString(isAsynchronous) + " - " + rds.getRequestId()) {

            /** {@inheritDoc} */
            @Override
            public void run() {
                download(rds, cs);
                LOGGER.info(new QueueLogInfo(rds));
            }

        };
        t.setDaemon(true);
        t.start();
        if (!isAsynchronous) {
            try {
                t.join();
            } catch (InterruptedException e) {
                LOGGER.error("InterruptedException download thread join interruption, requestId=" + rds.getRequestId(), e);
            }
        }

        return rds;
    }

    private RequestDownloadStatus initRequest(RequestProduct requestProduct, AbstractAction action) throws MotuException {

        DownloadStatus requestStatus = (DownloadStatus) initRequestStatus(action);
        requestStatus.setScriptVersion(requestProduct.getExtractionParameters().getScriptVersion());
        requestStatus.setLocalUri(BLLManager.getInstance().getConfigManager().getMotuConfig().getExtractionPath() + "/"
                + requestProduct.getRequestProductParameters().getExtractFilename());
        requestStatus.setRemoteUri(BLLManager.getInstance().getConfigManager().getMotuConfig().getDownloadHttpUrl() + "/"
                + requestProduct.getRequestProductParameters().getExtractFilename());

        IDALRequestStatusManager requestStatusManager = DALManager.getInstance().getRequestManager().getDalRequestStatusManager();
        requestProduct.setRequestId(requestStatusManager.addNewRequestStatus(requestStatus));
        return new RequestDownloadStatus(requestProduct, requestStatus);
    }

    @Override
    public RequestStatus initRequest(AbstractAction action) throws MotuException {
        RequestStatus requestStatus = initRequestStatus(action);
        IDALRequestStatusManager requestStatusManager = DALManager.getInstance().getRequestManager().getDalRequestStatusManager();
        requestStatusManager.addNewRequestStatus(requestStatus);
        return requestStatus;
    }

    @Override
    public void setActionStatus(RequestStatus requestStatus, StatusModeType status) {
        if (requestStatus != null) {
            requestStatus.setStatus(status.name());
            requestStatus.setStatusCode(Integer.toString(status.ordinal()));
            IDALRequestStatusManager requestStatusManager = DALManager.getInstance().getRequestManager().getDalRequestStatusManager();
            requestStatusManager.updateRequestStatus(requestStatus);
        }
    }

    private RequestStatus initRequestStatus(AbstractAction action) {
        RequestStatus requestStatus;

        if (action.getActionCode().equals(DownloadProductAction.ACTION_CODE) || action.getActionCode().equals(WCSGetCoverageAction.ACTION_CODE)) {
            requestStatus = new DownloadStatus();
        } else {
            NormalStatus normalStatus = new NormalStatus();
            normalStatus.setParameters(action.getParameters());
            requestStatus = normalStatus;
        }
        requestStatus.setStatus(StatusModeType.PENDING.name());
        requestStatus.setStatusCode(Integer.toString(StatusModeType.PENDING.value()));
        requestStatus.setActionName(action.getActionName());
        requestStatus.setActionCode(action.getActionCode());

        String userId = action.getUserId();
        if (userId == null) {
            userId = ANONYMOUS_USERID;
        }
        requestStatus.setUserId(userId);

        requestStatus.setTime(Long.toString(System.currentTimeMillis()));

        return requestStatus;
    }

    /**
     * .
     * 
     * @param userId
     * @return true if too much
     */
    private boolean isNumberOfRequestTooHighForUser(String userId) {
        long countRequest = getRequestCount(userId);
        boolean isAnonymousUser = (userId == null);
        boolean isNumberOfRequestTooHighForUser = (isAnonymousUser && getQueueServerConfig().getMaxPoolAnonymous() > 0
                && countRequest > getQueueServerConfig().getMaxPoolAnonymous())
                || (!isAnonymousUser && getQueueServerConfig().getMaxPoolAuth() > 0 && countRequest > getQueueServerConfig().getMaxPoolAuth());
        String logUserId = isAnonymousUser ? ANONYMOUS_USERID : userId;
        LOGGER.info("Check active request number for [userId=" + logUserId + "]: x" + countRequest + ", isNumberOfRequestTooHighForUser="
                + isNumberOfRequestTooHighForUser);
        return isNumberOfRequestTooHighForUser;
    }

    private long getRequestCount(String userId) {
        Map<String, DownloadStatus> request = DALManager.getInstance().getRequestManager().getDalRequestStatusManager().getDownloadRequestStatus();
        return request.entrySet().parallelStream().filter(entry -> isRequestPendingOrInProgress(userId, entry.getValue())).count();
    }

    private boolean isRequestPendingOrInProgress(String userId, DownloadStatus ds) {
        if (userId == null) {
            userId = ANONYMOUS_USERID;
        }
        boolean result = userId.equals(ds.getUserId());
        if (result) {
            int statusCode = Integer.parseInt(ds.getStatusCode());
            result = statusCode == StatusModeType.INPROGRESS.value() || statusCode == StatusModeType.PENDING.value();
        }
        return result;
    }

    /**
     * .
     * 
     * @return
     */
    private QueueServerType getQueueServerConfig() {
        return BLLManager.getInstance().getConfigManager().getMotuConfig().getQueueServerConfig();
    }

    public synchronized void checkNumberOfRunningRequestForUser(String userId) throws MotuException {
        if (isNumberOfRequestTooHighForUser(userId)) {
            String userIdMsg = "";
            if (userId != null) {
                userIdMsg = " for user: " + userId;
            }
            throw new MotuException(
                    ErrorType.EXCEEDING_USER_CAPACITY,
                    "Maximum number of running request reached" + userIdMsg + ", x"
                            + (userId == null
                                    ? BLLManager.getInstance().getConfigManager().getMotuConfig().getQueueServerConfig().getMaxPoolAnonymous()
                                    : BLLManager.getInstance().getConfigManager().getMotuConfig().getQueueServerConfig().getMaxPoolAuth()));
        }
    }

    private void download(RequestDownloadStatus rds, ConfigService cs) {
        RequestProduct reqProduct = rds.getRequestProduct();
        // If too much request for this user, throws MotuExceedingUserCapacityException
        String userId = reqProduct.getExtractionParameters().isAnonymousUser() ? null : reqProduct.getExtractionParameters().getUserId();
        try {
            double requestSizeInByte = getProductDataSizeIntoByte(reqProduct);
            double requestSizeInMBytes = UnitUtils.byteToMegaByte(requestSizeInByte);
            rds.setSize(Double.toString(requestSizeInMBytes));

            checkNumberOfRunningRequestForUser(userId);
            checkMaxSizePerFile(cs.getCatalog().getType(), requestSizeInMBytes);
            checkFreeSpace(requestSizeInMBytes);

            downloadSafe(rds, requestSizeInMBytes, cs);
        } catch (MotuException e) {
            rds.setRunningException(e);
        } catch (Exception e) {
            rds.setRunningException(new MotuException(ExceptionUtils.getErrorType(e), e));
        }
    }

    private void checkFreeSpace(double fileSizeInMegabyte) throws MotuException {
        File extractionDirectory = new File(BLLManager.getInstance().getConfigManager().getMotuConfig().getExtractionPath());
        if (!extractionDirectory.exists()) {
            LOGGER.error("The extraction folder does not exist: " + extractionDirectory.exists());
            throw new MotuException(ErrorType.SYSTEM, "The extraction folder does not exist: " + extractionDirectory.exists());
        } else {
            if (UnitUtils.byteToMegaByte(extractionDirectory.getFreeSpace()) <= fileSizeInMegabyte) {
                throw new NotEnoughSpaceException("There is not enough disk space available to generate the file result and to satisfy this request");
            }
        }
    }

    private void checkMaxSizePerFile(String catalogType, double fileSizeInMegaBytes) throws MotuException {
        double maxSizePerFileInMegaBytes;
        if (CatalogType.FILE.name().equalsIgnoreCase(catalogType.toUpperCase())) {
            maxSizePerFileInMegaBytes = BLLManager.getInstance().getConfigManager().getMotuConfig().getMaxSizePerFile().doubleValue();
        } else {
            maxSizePerFileInMegaBytes = BLLManager.getInstance().getConfigManager().getMotuConfig().getMaxSizePerFileSub().doubleValue();
        }
        if (fileSizeInMegaBytes > maxSizePerFileInMegaBytes) {
            throw new MotuException(
                    ErrorType.EXCEEDING_CAPACITY,
                    "",
                    new String[] { Double.toString(Math.ceil(fileSizeInMegaBytes)), Double.toString(maxSizePerFileInMegaBytes) });
        }
    }

    private void downloadSafe(RequestDownloadStatus requestDownloadStatus, double requestSizeInMB, ConfigService cs) throws MotuException {
        // The request download is delegated to a download request manager
        queueServerManager.execute(requestDownloadStatus, cs, requestSizeInMB);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws MotuExceptionBase
     */
    @Override
    public double getProductDataSizeIntoByte(RequestProduct requestProduct) throws MotuException {
        return UnitUtils
                .megabyteToByte(DALManager.getInstance().getCatalogManager().getProductManager().getProductDataSizeRequestInMegabyte(requestProduct));
    }

    /** {@inheritDoc} */
    @Override
    public double getProductMaxAllowedDataSizeIntoByte(Product product) throws MotuException {
        return UnitUtils.megabyteToByte(BLLManager.getInstance().getRequestManager().getQueueServerManager().getMaxDataThresholdInMegabyte());
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
    public void deleteRequest(String requestId) {
        DALManager.getInstance().getRequestManager().getDalRequestStatusManager().removeRequestStatus(requestId);
    }

    @Override
    public IBLLRequestStatusManager getBllRequestStatusManager() {
        return bllRequestStatusManager;
    }

}
