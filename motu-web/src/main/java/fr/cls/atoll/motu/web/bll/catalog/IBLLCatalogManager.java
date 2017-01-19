package fr.cls.atoll.motu.web.bll.catalog;

import fr.cls.atoll.motu.web.bll.catalog.product.IBLLProductManager;
import fr.cls.atoll.motu.web.bll.catalog.product.cache.ICatalogAndProductCacheManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;

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
public interface IBLLCatalogManager {

    /**
     * .
     * 
     * @return
     */
    IBLLProductManager getProductManager();

    /**
     * .
     */
    void init() throws MotuException;

    /**
     * Return the catalog type of a service .
     * 
     * @param service the service for which the catalog type is needed
     * @return The catalogType name.
     * @throws MotuException
     */
    String getCatalogType(ConfigService service) throws MotuException;

    /**
     * .
     */
    void stop();

    ICatalogAndProductCacheManager getCatalogAndProductCacheManager();
}
