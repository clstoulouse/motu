package fr.cls.atoll.motu.web.bll.request.model;

import fr.cls.atoll.motu.web.bll.exception.MotuException;

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
public class ProductResult {

    private String productFileName;
    private MotuException runningException;

    /**
     * Valeur de productFileName.
     * 
     * @return la valeur.
     */
    public String getProductFileName() {
        return productFileName;
    }

    /**
     * Valeur de productFileName.
     * 
     * @param productFileName nouvelle valeur.
     */
    public void setProductFileName(String productFileName) {
        this.productFileName = productFileName;
    }

    /**
     * .
     * 
     * @param runningException
     */
    public void setRunningException(MotuException runningException_) {
        runningException = runningException_;
    }

    /**
     * Valeur de runningException_.
     * 
     * @return la valeur.
     */
    public MotuException getRunningException() {
        return runningException;
    }

}
