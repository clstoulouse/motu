package fr.cls.atoll.motu.web.bll.config;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.config.xml.model.MotuConfig;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;

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
public interface IBLLConfigManager {

    /**
     * .
     * 
     * @return null if no cas server is used, otherwise the Cas server url
     */
    String getCasServerUrl();

    boolean isCasActivated();

    /**
     * .
     * 
     * @return
     */
    IBLLQueueServerConfigManager getQueueServerConfigManager();

    /**
     * .
     * 
     * @return
     */
    boolean isStatusAsFile();

    /**
     * .
     * 
     * @return
     */
    MotuConfig getMotuConfig();

    /**
     * .
     * 
     * @return
     */
    String getProductDownloadHttpUrl();

    /**
     * .
     * 
     * @param parameterValueValidated
     * @return
     */
    ConfigService getConfigService(String configServiceName);

    /**
     * Return the product associated to the provided Product location. .
     * 
     * @param productLocation
     * @return
     * @throws MotuException
     */
    Product getProduct(String productLocation) throws MotuException;
}
