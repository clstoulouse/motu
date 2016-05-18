package fr.cls.atoll.motu.web.usl.request.parameter.validator;

import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public abstract class AbstractHTTPParameterValidator<T> {

    private T result;

    public abstract void validate() throws InvalidHTTPParameterException;

    /**
     * Valeur de result.
     * 
     * @return la valeur.
     */
    public T getResult() {
        return result;
    }

    /**
     * Valeur de result.
     * 
     * @param result nouvelle valeur.
     */
    public void setResult(T result) {
        this.result = result;
    }

}
