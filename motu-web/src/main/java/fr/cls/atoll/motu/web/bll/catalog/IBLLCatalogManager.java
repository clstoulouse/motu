package fr.cls.atoll.motu.web.bll.catalog;

import fr.cls.atoll.motu.web.bll.catalog.product.IBLLProductManager;
import fr.cls.atoll.motu.web.bll.catalog.product.cache.IProductCacheManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;
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
public interface IBLLCatalogManager {

    /**
     * .
     * 
     * @param cs
     * 
     * @return
     * @throws MotuException
     */
    CatalogData getCatalogData(ConfigService cs) throws MotuException;

    /**
     * .
     * 
     * @return
     */
    IBLLProductManager getProductManager();

    /**
     * 
     * .
     * 
     * @return
     */
    IProductCacheManager getProductCacheManager();

    /**
     * .
     */
    void init() throws MotuException;

    /**
     * Return the catalog type of a product .
     * 
     * @param product the product for which the catalog type is needed
     * @return The catalogType name.
     * @throws MotuException
     */
    String getCatalogType(Product product) throws MotuException;

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
}
