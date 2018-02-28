package fr.cls.atoll.motu.web.bll.request.status.data;

public class NormalStatus extends RequestStatus {

    private String parameters;

    public NormalStatus() {
        parameters = "";
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

}
