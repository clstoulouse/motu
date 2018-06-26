package fr.cls.atoll.motu.web.bll.request.status.data;

public class DownloadStatus extends RequestStatus {

    private String message;
    private String size;
    private String dateProc;
    private String scriptVersion;
    private String outputFileName;

    public DownloadStatus() {
        message = "";
        size = "";
        dateProc = "";
        scriptVersion = "";
        outputFileName = "";
    }

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
}
