package fr.cls.atoll.motu.web.bll.request.status.data;

import fr.cls.atoll.motu.api.message.xml.ErrorType;

public class DownloadStatus extends RequestStatus implements IDownloadStatus {

    private String message = "";
    private String size = "";
    private String dateProc = "";
    private String scriptVersion = "";
    private String outputFileName = "";
    private String remoteUri = "";
    private String localUri = "";
    private ErrorType errorType = ErrorType.OK;

    private long creationDateTime = System.currentTimeMillis();
    private long startProcessingDateTime;
    private long endProcessingDateTime;

    /** {@inheritDoc} */
    @Override
    public String getMessage() {
        return message;
    }

    /** {@inheritDoc} */
    @Override
    public void setMessage(String message) {
        this.message = message;
    }

    /** {@inheritDoc} */
    @Override
    public String getSize() {
        return size;
    }

    /** {@inheritDoc} */
    @Override
    public void setSize(String size) {
        this.size = size;
    }

    /** {@inheritDoc} */
    @Override
    public String getDateProc() {
        return dateProc;
    }

    /** {@inheritDoc} */
    @Override
    public void setDateProc(String dateProc) {
        this.dateProc = dateProc;
    }

    /** {@inheritDoc} */
    @Override
    public String getScriptVersion() {
        return scriptVersion;
    }

    /** {@inheritDoc} */
    @Override
    public void setScriptVersion(String scriptVersion) {
        this.scriptVersion = scriptVersion;
    }

    /** {@inheritDoc} */
    @Override
    public String getOutputFileName() {
        return outputFileName;
    }

    /** {@inheritDoc} */
    @Override
    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    /** {@inheritDoc} */
    @Override
    public long getCreationDateTime() {
        return creationDateTime;
    }

    /** {@inheritDoc} */
    @Override
    public long getStartProcessingDateTime() {
        return startProcessingDateTime;
    }

    /** {@inheritDoc} */
    @Override
    public void setStartProcessingDateTime(long startProcessingDateTime) {
        this.startProcessingDateTime = startProcessingDateTime;
    }

    /** {@inheritDoc} */
    @Override
    public long getEndProcessingDateTime() {
        return endProcessingDateTime;
    }

    /** {@inheritDoc} */
    @Override
    public void setEndProcessingDateTime(long endProcessingDateTime) {
        this.endProcessingDateTime = endProcessingDateTime;
    }

    /** {@inheritDoc} */
    @Override
    public String getRemoteUri() {
        return remoteUri;
    }

    /** {@inheritDoc} */
    @Override
    public void setRemoteUri(String remoteUri) {
        this.remoteUri = remoteUri;
    }

    /** {@inheritDoc} */
    @Override
    public String getLocalUri() {
        return localUri;
    }

    /** {@inheritDoc} */
    @Override
    public void setLocalUri(String localUri) {
        this.localUri = localUri;
    }

    /** {@inheritDoc} */
    @Override
    public ErrorType getErrorType() {
        return errorType;
    }

    /** {@inheritDoc} */
    @Override
    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }

}
