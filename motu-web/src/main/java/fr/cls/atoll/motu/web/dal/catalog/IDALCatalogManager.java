package fr.cls.atoll.motu.web.dal.catalog;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.catalog.product.IDALProductManager;
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
public interface IDALCatalogManager {

    /**
     * .
     * 
     * @param cs
     * @return
     * @throws MotuException
     */
    CatalogData getCatalogData(ConfigService cs) throws MotuException;

    /**
     * .
     * 
     * @throws MotuException
     */
    void init() throws MotuException;

    /**
     * .
     * 
     * @return
     */
    IDALProductManager getProductManager();

    /**
     * Return the catalog type of the provided product.
     * 
     * @param product
     * @return
     * @throws MotuException
     */
    String getCatalogType(Product product) throws MotuException;

    /**
     * Return the catalog type of the provided service.
     * 
     * @param service
     * @return
     * @throws MotuException
     */
    String getCatalogType(ConfigService service) throws MotuException;
}
