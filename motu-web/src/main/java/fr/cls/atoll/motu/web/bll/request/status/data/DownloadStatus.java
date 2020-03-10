package fr.cls.atoll.motu.web.bll.request.status.data;

import fr.cls.atoll.motu.api.message.xml.ErrorType;

public class DownloadStatus extends RequestStatus {

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDateProc() {
        return dateProc;
    }

    public void setDateProc(String dateProc) {
        this.dateProc = dateProc;
    }

    public String getScriptVersion() {
        return scriptVersion;
    }

    public void setScriptVersion(String scriptVersion) {
        this.scriptVersion = scriptVersion;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public long getCreationDateTime() {
        return creationDateTime;
    }

    public long getStartProcessingDateTime() {
        return startProcessingDateTime;
    }

    public void setStartProcessingDateTime(long startProcessingDateTime) {
        this.startProcessingDateTime = startProcessingDateTime;
    }

    public long getEndProcessingDateTime() {
        return endProcessingDateTime;
    }

    public void setEndProcessingDateTime(long endProcessingDateTime) {
        this.endProcessingDateTime = endProcessingDateTime;
    }

    public String getRemoteUri() {
        return remoteUri;
    }

    public void setRemoteUri(String remoteUri) {
        this.remoteUri = remoteUri;
    }

    public String getLocalUri() {
        return localUri;
    }

    public void setLocalUri(String localUri) {
        this.localUri = localUri;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }

}
