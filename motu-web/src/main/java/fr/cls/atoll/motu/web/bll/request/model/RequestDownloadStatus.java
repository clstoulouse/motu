package fr.cls.atoll.motu.web.bll.request.model;

import javax.xml.datatype.XMLGregorianCalendar;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.api.message.xml.StatusModeType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuExceptionBase;
import fr.cls.atoll.motu.web.bll.request.status.data.DownloadStatus;
import fr.cls.atoll.motu.web.bll.request.status.data.IDownloadStatus;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.dal.DALManager;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.DataBaseExtractionTimeCounter;
import fr.cls.atoll.motu.web.dal.request.status.IDALRequestStatusManager;
import fr.cls.atoll.motu.web.usl.response.xml.converter.XMLConverter;

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
public class RequestDownloadStatus implements IDownloadStatus {

    private RequestProduct requestProduct;

    private long startProcessingDateTime = 0L;
    private long endProcessingDateTime = 0L;

    private MotuException runningException;

    // Queue server manager processing info
    private String queueId;
    private String queueDescription;

    private DownloadStatus ds;

    /**
     * Constructeur.
     * 
     * @param requestId
     * @param requestStatus
     * @param userId
     * @param userHost
     * @throws MotuException
     */
    public RequestDownloadStatus(RequestProduct requestProduct, DownloadStatus requestStatus) {
        this.requestProduct = requestProduct;
        ds = requestStatus;
    }

    /**
     * Valeur de dataBaseExtractionTimeCounter.
     * 
     * @return la valeur.
     */
    public DataBaseExtractionTimeCounter getDataBaseExtractionTimeCounter() {
        return getRequestProduct().getDataBaseExtractionTimeCounter();
    }

    /**
     * Valeur de requestProduct.
     * 
     * @return la valeur.
     */
    public RequestProduct getRequestProduct() {
        return requestProduct;
    }

    /**
     * Status are Pending: creationDateTime is set Progress: startProcessingDateTime is set Done:
     * endProcessingDateTime is set Error: runningException is set
     * 
     * @return STATUS_PENDING, STATUS_IN_PROGRESS, STATUS_DONE, STATUS_ERROR
     */
    public int getRequestStatus() {
        if (runningException != null) {
            return StatusModeType.ERROR.value();
        } else if (endProcessingDateTime != 0) {
            return StatusModeType.DONE.value();
        } else if (startProcessingDateTime != 0) {
            return StatusModeType.INPROGRESS.value();
        } else { // (creationDateTime != 0)
            return StatusModeType.PENDING.value();
        }
    }

    /**
     * Valeur de requestId.
     * 
     * @return la valeur.
     */
    public String getRequestId() {
        return getRequestProduct().getRequestId();
    }

    /**
     * Valeur de requestId.
     * 
     * @param requestId nouvelle valeur.
     */
    public void setRequestId(String requestId) {
        getRequestProduct().setRequestId(requestId);
    }

    /**
     * Valeur de creationDateTime.
     * 
     * @return la valeur.
     */
    @Override
    public long getCreationDateTime() {
        return ds.getCreationDateTime();
    }

    /**
     * Valeur de startProcessingDateTime.
     * 
     * @return la valeur.
     */
    @Override
    public long getStartProcessingDateTime() {
        return startProcessingDateTime;
    }

    /**
     * Valeur de startProcessingDateTime.
     * 
     * @param startProcessingDateTime nouvelle valeur.
     * @throws MotuException
     */
    @Override
    public void setStartProcessingDateTime(long startProcessingDateTime) throws MotuException {
        this.startProcessingDateTime = startProcessingDateTime;
        setRequestStatus();
        setRequestDateProc();
    }

    /**
     * Valeur de endProcessingDateTime.
     * 
     * @return la valeur.
     */
    @Override
    public long getEndProcessingDateTime() {
        return endProcessingDateTime;
    }

    /**
     * Valeur de endProcessingDateTime.
     * 
     * @param endProcessingDateTime nouvelle valeur.
     */
    @Override
    public void setEndProcessingDateTime(long endProcessingDateTime) {
        this.endProcessingDateTime = endProcessingDateTime;
        setRequestStatus();
        saveRequestStatusObject();
    }

    /**
     * Valeur de runningException.
     * 
     * @return la valeur.
     */
    public MotuException getRunningException() {
        return runningException;
    }

    /**
     * Valeur de runningException.
     * 
     * @param runningException nouvelle valeur.
     */
    public void setRunningException(MotuException runningException) {
        this.runningException = runningException;
        setEndProcessingDateTime(System.currentTimeMillis());
        setRequestMessage();
        setRequestErrorType();
        saveRequestStatusObject();
    }

    /**
     * .
     * 
     * @param id
     */
    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }

    /**
     * Valeur de queueDescription.
     * 
     * @return la valeur.
     */
    public String getQueueDescription() {
        return queueDescription;
    }

    /**
     * Valeur de queueDescription.
     * 
     * @param queueDescription nouvelle valeur.
     */
    public void setQueueDescription(String queueDescription) {
        this.queueDescription = queueDescription;
    }

    /**
     * Valeur de queueId.
     * 
     * @return la valeur.
     */
    public String getQueueId() {
        return queueId;
    }

    private void setRequestStatus() {
        if (ds != null) {
            StatusModeType statusModeType = StatusModeType.fromValue(getRequestStatus());
            ds.setStatus(statusModeType.name());
            ds.setStatusCode(Integer.toString(statusModeType.value()));
        }
    }

    private void setRequestMessage() {
        if (ds != null) {
            String msg = "";
            if (runningException instanceof MotuException) {
                if (runningException.getCause() instanceof MotuExceptionBase) {
                    msg = StringUtils.getLogMessage(ds.getActionCode(),
                                                    runningException.getErrorType(),
                                                    BLLManager.getInstance().getMessagesErrorManager()
                                                            .getMessageError(runningException.getErrorType(),
                                                                             runningException.getCause().getMessage()));
                } else {
                    msg = StringUtils.getLogMessage(ds.getActionCode(),
                                                    runningException.getErrorType(),
                                                    BLLManager.getInstance().getMessagesErrorManager()
                                                            .getMessageError(runningException.getErrorType(), runningException)); // .getMessage()
                }
            }
            ds.setMessage(msg);
        }
    }

    private void setRequestErrorType() {
        if (ds != null) {
            ds.setErrorType(XMLConverter.convertErrorCode(getRunningException()));
        }
    }

    private void setRequestDateProc() throws MotuException {
        if (ds != null) {
            XMLGregorianCalendar dateProc = XMLConverter.dateToXMLGregorianCalendar(getStartProcessingDateTime()).normalize();
            String requestDateProc = "";
            if (dateProc.getYear() == 1970) {
                requestDateProc = "Unknown";
            } else {
                requestDateProc = dateProc.toString();
            }
            ds.setDateProc(requestDateProc);
            saveRequestStatusObject();
        }
    }

    private void saveRequestStatusObject() {
        IDALRequestStatusManager requestStatusManager = DALManager.getInstance().getRequestManager().getDalRequestStatusManager();
        requestStatusManager.updateRequestStatus(getRequestId(), ds);
    }

    @Override
    public String getMessage() {
        return ds.getMessage();
    }

    @Override
    public void setMessage(String message) {
        ds.setMessage(message);
        saveRequestStatusObject();
    }

    @Override
    public String getSize() {
        return ds.getSize();
    }

    @Override
    public void setSize(String size) {
        ds.setSize(size);
        saveRequestStatusObject();
    }

    @Override
    public String getDateProc() {
        return ds.getDateProc();
    }

    @Override
    public void setDateProc(String dateProc) {
        ds.setDateProc(dateProc);
        saveRequestStatusObject();
    }

    @Override
    public String getScriptVersion() {
        return ds.getScriptVersion();
    }

    @Override
    public void setScriptVersion(String scriptVersion) {
        ds.setScriptVersion(scriptVersion);
        saveRequestStatusObject();
    }

    @Override
    public String getOutputFileName() {
        return ds.getOutputFileName();
    }

    @Override
    public void setOutputFileName(String outputFileName) {
        ds.setOutputFileName(outputFileName);
        saveRequestStatusObject();
    }

    @Override
    public String getRemoteUri() {
        return ds.getRemoteUri();
    }

    @Override
    public void setRemoteUri(String remoteUri) {
        ds.setRemoteUri(remoteUri);
        saveRequestStatusObject();
    }

    @Override
    public String getLocalUri() {
        return ds.getLocalUri();
    }

    @Override
    public void setLocalUri(String localUri) {
        ds.setLocalUri(localUri);
        saveRequestStatusObject();
    }

    @Override
    public ErrorType getErrorType() {
        return ds.getErrorType();
    }

    @Override
    public void setErrorType(ErrorType errorType) {
        ds.setErrorType(errorType);
        saveRequestStatusObject();
    }

}
