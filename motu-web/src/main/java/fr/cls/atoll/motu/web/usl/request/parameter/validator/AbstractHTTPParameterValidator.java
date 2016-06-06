package fr.cls.atoll.motu.web.usl.request.parameter.validator;

import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites) <br>
 * <br>
 * The generic type T is the type of the result once the parameter is parsed. All parameter comes as HTTP
 * request parameters. So they are with a type String. But they could represent another type as a Long, a
 * Double, or everything else. The generic type T is so this type.<br>
 * 
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public abstract class AbstractHTTPParameterValidator<T> {

    private T parameterValueValidated;
    private String parameterName;
    private String parameterValue;
    private boolean isParameterOptional;

    /**
     * Constructeur.
     * 
     * @param parameterName_
     * @param parameterValue_
     */
    public AbstractHTTPParameterValidator(String parameterName_, String parameterValue_) {
        parameterName = parameterName_;
        parameterValue = parameterValue_;
        isParameterOptional = false;
    }

    /**
     * .
     * 
     * @param b
     */
    public void setOptional(boolean isParameterOptional_) {
        isParameterOptional = isParameterOptional_;
    }

    /**
     * Valeur de isParameterOptional.
     * 
     * @return la valeur.
     */
    public boolean isParameterOptional() {
        return isParameterOptional;
    }

    /**
     * Used to validate the HTTPd parameters
     * 
     * @return The value validated
     * @throws InvalidHTTPParameterException Exception thrown when an HTTP parameter is not in the boundaries
     *             of its definition
     */
    public T validate() throws InvalidHTTPParameterException {
        T parameterValueValidated = onValidateAction();
        setParameterValueValidated(parameterValueValidated);
        return parameterValueValidated;
    }

    protected abstract T onValidateAction() throws InvalidHTTPParameterException;

    /**
     * Valeur de result.
     * 
     * @return la valeur.
     */
    public T getParameterValueValidated() {
        return parameterValueValidated;
    }

    /**
     * Valeur de result.
     * 
     * @param result nouvelle valeur.
     */
    public void setParameterValueValidated(T parameterValueValidated_) {
        this.parameterValueValidated = parameterValueValidated_;
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
     * Valeur de parameterValue.
     * 
     * @return la valeur.
     */
    public String getParameterValue() {
        return parameterValue;
    }

    /**
     * .
     * 
     * @return A string representing the definition of the parameter value.<br>
     *         Example:<br>
     *         <ul>
     *         <li>For a latitude, this method returns: [-90;90]</li>
     *         <li>For a longitude, this method returns: [-180;180]</li>
     *         <li>For an enum, this method returns: [value1;value2;value3]</li>
     *         <li>For a position long, this method returns: [1;Long.MAX_VALUE]</li>
     *         </ul>
     */
    protected String getParameterBoundaries() {
        return "";
    }

    /**
     * Valeur de parameterValue.
     * 
     * @param parameterValue nouvelle valeur.
     */
    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }

}
