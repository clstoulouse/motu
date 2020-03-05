package fr.cls.atoll.motu.web.bll.request.status.data;

public abstract class RequestStatus {

    private String actionName = "";
    private String actionCode = "";
    private String userId = "";
    private String userHost = "";
    private String time = "";
    private String status = "";
    private String statusCode = "";
    private String requestId = null;

    /**
     * Gets the value of requestId.
     *
     * @return the value of requestId
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Sets the value of requestId.
     *
     * @param requestId the value to set
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
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

    /**
     * Gets the value of userHost.
     *
     * @return the value of userHost
     */
    public String getUserHost() {
        return userHost;
    }

    /**
     * Sets the value of userHost.
     *
     * @param userHost the value to set
     */
    public void setUserHost(String userHost) {
        this.userHost = userHost;
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
