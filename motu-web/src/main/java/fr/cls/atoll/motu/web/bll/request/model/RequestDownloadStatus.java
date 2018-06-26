package fr.cls.atoll.motu.web.bll.request.model;

import javax.xml.datatype.XMLGregorianCalendar;

import fr.cls.atoll.motu.api.message.xml.StatusModeType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuExceptionBase;
import fr.cls.atoll.motu.web.bll.request.status.data.DownloadStatus;
import fr.cls.atoll.motu.web.bll.request.status.data.RequestStatus;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.common.utils.UnitUtils;
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
public class RequestDownloadStatus {

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_IN_PROGRESS = 10;
    public static final int STATUS_DONE = 20;
    public static final int STATUS_ERROR = 30;

    private String requestId;
    private RequestProduct requestProduct;

    private long creationDateTime;
    private long startProcessingDateTime;
    private long endProcessingDateTime;

    private long sizeInBits;

    private MotuException runningException;

    // Queue server manager processing info
    private String queueId;
    private String queueDescription;

    private DataBaseExtractionTimeCounter dataBaseExtractionTimeCounter;

    /**
     * Constructeur.
     * 
     * @param requestId
     * @param userId
     * @param userHost
     * @throws MotuException
     */
    public RequestDownloadStatus(String requestId_, RequestProduct requestProduct_) throws MotuException {
        super();
        setRequestId(requestId_);
        setRequestProduct(requestProduct_);

        setCreationDateTime(System.currentTimeMillis());
        setStartProcessingDateTime(0L);
        setEndProcessingDateTime(0L);
        setDataBaseExtractionTimeCounter(new DataBaseExtractionTimeCounter());
    }

    /**
     * Valeur de dataBaseExtractionTimeCounter.
     * 
     * @return la valeur.
     */
    public DataBaseExtractionTimeCounter getDataBaseExtractionTimeCounter() {
        return dataBaseExtractionTimeCounter;
    }

    /**
     * Valeur de dataBaseExtractionTimeCounter.
     * 
     * @param dataBaseExtractionTimeCounter nouvelle valeur.
     */
    public void setDataBaseExtractionTimeCounter(DataBaseExtractionTimeCounter dataBaseExtractionTimeCounter) {
        this.dataBaseExtractionTimeCounter = dataBaseExtractionTimeCounter;
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
     * Valeur de requestProduct.
     * 
     * @param requestProduct nouvelle valeur.
     */
    public void setRequestProduct(RequestProduct requestProduct) {
        this.requestProduct = requestProduct;
    }

    /**
     * Status are Pending: creationDateTime is set Progress: startProcessingDateTime is set Done:
     * endProcessingDateTime is set Error: runningException is set
     * 
     * @return STATUS_PENDING, STATUS_IN_PROGRESS, STATUS_DONE, STATUS_ERROR
     */
    public int getRequestStatus() {
        if (runningException != null) {
            return STATUS_ERROR;
        } else if (endProcessingDateTime != 0) {
            return STATUS_DONE;
        } else if (startProcessingDateTime != 0) {
            return STATUS_IN_PROGRESS;
        } else { // (creationDateTime != 0)
            return STATUS_PENDING;
        }
    }

    /**
     * Valeur de requestId.
     * 
     * @return la valeur.
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Valeur de requestId.
     * 
     * @param requestId nouvelle valeur.
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * Valeur de creationDateTime.
     * 
     * @return la valeur.
     */
    public long getCreationDateTime() {
        return creationDateTime;
    }

    /**
     * Valeur de creationDateTime.
     * 
     * @param creationDateTime nouvelle valeur.
     */
    public void setCreationDateTime(long creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    /**
     * Valeur de startProcessingDateTime.
     * 
     * @return la valeur.
     */
    public long getStartProcessingDateTime() {
        return startProcessingDateTime;
    }

    /**
     * Valeur de startProcessingDateTime.
     * 
     * @param startProcessingDateTime nouvelle valeur.
     * @throws MotuException
     */
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
    public long getEndProcessingDateTime() {
        return endProcessingDateTime;
    }

    /**
     * Valeur de endProcessingDateTime.
     * 
     * @param endProcessingDateTime nouvelle valeur.
     */
    public void setEndProcessingDateTime(long endProcessingDateTime) {
        this.endProcessingDateTime = endProcessingDateTime;
        setRequestStatus();
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
        setRequestStatus();
        setRequestMessage();
    }

    /**
     * Valeur de sizeInBits.
     * 
     * @return la valeur.
     */
    public long getSizeInBit() {
        return sizeInBits;
    }

    /**
     * Valeur de sizeInBits.
     * 
     * @param sizeInBits nouvelle valeur.
     */
    public void setSizeInBits(long sizeInBits) {
        this.sizeInBits = sizeInBits;
        setRequestSize();
    }

    /**
     * .
     * 
     * @param id
     */
    public void setQueueId(String queueId_) {
        this.queueId = queueId_;
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
        RequestStatus requestStatus = getRequestStatusObject();
        if (requestStatus != null) {
            StatusModeType statusModeType = XMLConverter.convertStatusModeResponse(getRequestStatus());
            requestStatus.setStatus(statusModeType.name());
            requestStatus.setStatusCode(Integer.toString(statusModeType.value()));
            updateRequestStatusObject(requestStatus);
        }
    }

    private void setRequestMessage() {
        DownloadStatus requestStatus = getRequestStatusObject();
        if (requestStatus != null) {
            String msg = "";
            if (runningException != null) {
                if (runningException instanceof MotuException) {
                    if (runningException.getCause() instanceof MotuExceptionBase) {
                        msg = StringUtils.getLogMessage(requestStatus.getActionCode(),
                                                        runningException.getErrorType(),
                                                        BLLManager.getInstance().getMessagesErrorManager()
                                                                .getMessageError(runningException.getErrorType(),
                                                                                 runningException.getCause().getMessage()));
                    } else {
                        msg = StringUtils.getLogMessage(requestStatus.getActionCode(),
                                                        runningException.getErrorType(),
                                                        BLLManager.getInstance().getMessagesErrorManager()
                                                                .getMessageError(runningException.getErrorType(), runningException)); // .getMessage()
                    }
                }
            }
            requestStatus.setMessage(msg);
            updateRequestStatusObject(requestStatus);
        }
    }

    private void setRequestSize() {
        DownloadStatus requestStatus = getRequestStatusObject();
        if (requestStatus != null) {
            requestStatus.setSize(Double.toString(UnitUtils.bitToMegaByte(sizeInBits)));
            updateRequestStatusObject(requestStatus);
        }
    }

    private void setRequestDateProc() throws MotuException {
        DownloadStatus requestStatus = getRequestStatusObject();
        if (requestStatus != null) {
            XMLGregorianCalendar dateProc = XMLConverter.dateToXMLGregorianCalendar(getStartProcessingDateTime()).normalize();
            String requestDateProc = "";
            if (dateProc.getYear() == 1970) {
                requestDateProc = "Unknown";
            } else {
                requestDateProc = dateProc.toString();
            }
            requestStatus.setDateProc(requestDateProc);
            updateRequestStatusObject(requestStatus);
        }
    }

    private DownloadStatus getRequestStatusObject() {
        DownloadStatus downloadStatus = null;
        if (requestId != null) {
            IDALRequestStatusManager requestStatusManager = DALManager.getInstance().getRequestManager().getDalRequestStatusManager();
            RequestStatus requestStatus = requestStatusManager.getRequestStatus(requestId);
            if (requestStatus instanceof DownloadStatus) {
                downloadStatus = (DownloadStatus) requestStatus;
            }
        }
        return downloadStatus;
    }

    private void updateRequestStatusObject(RequestStatus requestStatus) {
        if (requestId != null) {
            IDALRequestStatusManager requestStatusManager = DALManager.getInstance().getRequestManager().getDalRequestStatusManager();
            requestStatusManager.updateRequestStatus(requestId, requestStatus);
        }
    }

}
