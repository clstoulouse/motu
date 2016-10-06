package fr.cls.atoll.motu.web.dal.config.version;

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
public interface IDALVersionManager {

    /**
     * .
     * 
     * @return
     * @throws MotuException
     */
    String getDistributionVersion() throws MotuException;

    /**
     * .
     * 
     * @return
     */
    String getConfigurationVersion() throws MotuException;

    /**
     * .
     * 
     * @return
     */
    String getProductsVersion() throws MotuException;
}
