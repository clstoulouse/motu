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

    /**
     * Constructeur.
     * 
     * @param parameterName_ The HTTP parameter name set in the request
     * @param parameterValue_ The HTTP parameter value set in the request
     * @param parameterBoundaries_ The HTTP parameter definition, example [-90; 90] or [console,url,status]
     */
    public InvalidHTTPParameterException(String parameterName_, String parameterValue_, String parameterBoundaries_) {
        super(
            "Parameter " + parameterName_ + " has not a valid value: " + parameterValue_ + ". It should be in the following boundaries: "
                    + parameterBoundaries_);
        setParameterName(parameterName_);
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
