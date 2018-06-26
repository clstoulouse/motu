package fr.cls.atoll.motu.web.bll.request.status.data;

public abstract class RequestStatus {

    private String actionName;
    private String actionCode;
    private String userId;
    private String time;
    private String status;
    private String statusCode;

    public RequestStatus() {
        actionName = "";
        actionCode = "";
        userId = "";
        time = "";
        status = "";
        statusCode = "";
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
