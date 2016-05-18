package fr.cls.atoll.motu.web.usl.request.parameter.exception;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)<br>
 * <br>
 * <br>
 * 
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class InvalidHTTPParameterException extends Exception {

    private String parameterName;

    /**
     * .
     */
    private static final long serialVersionUID = 1L;

    public InvalidHTTPParameterException(String parameterName_, String errMsg_) {
        super(errMsg_);
        setParameterName(parameterName_);
    }

    /** {@inheritDoc} */
    @Override
    public String getMessage() {
        return "Parameter " + getParameterName() + " is not valid: " + super.getMessage();
    }

    /**
     * Valeur de parameterName.
     * 
     * @return la valeur.
     */
    public String getParameterName() {
        return parameterName;
    }

    /**
     * Valeur de parameterName.
     * 
     * @param parameterName nouvelle valeur.
     */
    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

}
