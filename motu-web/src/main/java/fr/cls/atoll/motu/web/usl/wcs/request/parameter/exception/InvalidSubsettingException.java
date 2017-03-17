package fr.cls.atoll.motu.web.usl.wcs.request.parameter.exception;

public class InvalidSubsettingException extends Exception {

    private String parameterName;

    public InvalidSubsettingException(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getParameterName() {
        return parameterName;
    }

}
