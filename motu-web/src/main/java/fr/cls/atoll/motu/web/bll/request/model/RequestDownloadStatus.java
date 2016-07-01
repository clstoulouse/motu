package fr.cls.atoll.motu.web.bll.request.model;

import fr.cls.atoll.motu.web.bll.exception.MotuException;

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

    private long requestId;
    private String userId;
    private String userHost;

    private long creationDateTime;
    private long startProcessingDateTime;
    private long endProcessingDateTime;

    private long sizeInBits;

    private MotuException runningException;
    private String extractFilename;

    /**
     * Constructeur.
     * 
     * @param requestId
     * @param userId
     * @param userHost
     */
    public RequestDownloadStatus(long requestId, String userId, String userHost) {
        super();
        setRequestId(requestId);
        setUserId(userId);
        setUserHost(userHost);
        setCreationDateTime(System.currentTimeMillis());
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
    public long getRequestId() {
        return requestId;
    }

    /**
     * Valeur de requestId.
     * 
     * @param requestId nouvelle valeur.
     */
    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    /**
     * Valeur de userId.
     * 
     * @return la valeur.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Valeur de userId.
     * 
     * @param userId nouvelle valeur.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Valeur de userHost.
     * 
     * @return la valeur.
     */
    public String getUserHost() {
        return userHost;
    }

    /**
     * Valeur de userHost.
     * 
     * @param userHost nouvelle valeur.
     */
    public void setUserHost(String userHost) {
        this.userHost = userHost;
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
     */
    public void setStartProcessingDateTime(long startProcessingDateTime) {
        this.startProcessingDateTime = startProcessingDateTime;
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
    }

    /**
     * Valeur de sizeInBits.
     * 
     * @return la valeur.
     */
    public long getSizeInBits() {
        return sizeInBits;
    }

    /**
     * Valeur de sizeInBits.
     * 
     * @param sizeInBits nouvelle valeur.
     */
    public void setSizeInBits(long sizeInBits) {
        this.sizeInBits = sizeInBits;
    }

    /**
     * .
     * 
     * @param extractFilename
     */
    public void setProductFileName(String extractFilename_) {
        extractFilename = extractFilename_;
    }

    /**
     * Valeur de extractFilename.
     * 
     * @return la valeur.
     */
    public String getExtractFilename() {
        return extractFilename;
    }

}
