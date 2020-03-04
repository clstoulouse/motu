package fr.cls.atoll.motu.web.bll.request.status.data;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.exception.MotuException;

public interface IDownloadStatus {

    String getMessage();

    void setMessage(String message);

    String getSize();

    void setSize(String size);

    String getDateProc();

    void setDateProc(String dateProc);

    String getScriptVersion();

    void setScriptVersion(String scriptVersion);

    String getOutputFileName();

    void setOutputFileName(String outputFileName);

    /**
     * Gets the value of creationDateTime.
     *
     * @return the value of creationDateTime
     */
    long getCreationDateTime();

    /**
     * Gets the value of startProcessingDateTime.
     *
     * @return the value of startProcessingDateTime
     */
    long getStartProcessingDateTime();

    /**
     * Sets the value of startProcessingDateTime.
     *
     * @param startProcessingDateTime the value to set
     * @throws MotuException
     */
    void setStartProcessingDateTime(long startProcessingDateTime) throws MotuException;

    /**
     * Gets the value of endProcessingDateTime.
     *
     * @return the value of endProcessingDateTime
     */
    long getEndProcessingDateTime();

    /**
     * Sets the value of endProcessingDateTime.
     *
     * @param endProcessingDateTime the value to set
     */
    void setEndProcessingDateTime(long endProcessingDateTime);

    /**
     * Gets the value of remoteUri.
     *
     * @return the value of remoteUri
     */
    String getRemoteUri();

    /**
     * Sets the value of remoteUri.
     *
     * @param remoteUri the value to set
     */
    void setRemoteUri(String remoteUri);

    /**
     * Gets the value of localUri.
     *
     * @return the value of localUri
     */
    String getLocalUri();

    /**
     * Sets the value of localUri.
     *
     * @param localUri the value to set
     */
    void setLocalUri(String localUri);

    /**
     * Gets the value of errorType.
     *
     * @return the value of errorType
     */
    ErrorType getErrorType();

    /**
     * Sets the value of errorType.
     *
     * @param errorType the value to set
     */
    void setErrorType(ErrorType errorType);

}