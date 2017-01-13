package fr.cls.atoll.motu.web.bll.catalog.product.cache;

import java.util.Map;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Pierre LACOSTE
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public interface ICatalogCache {

    CatalogData getCatalog(String configServiceName);

    void putCatalog(String configServiceName, CatalogData cd);

    void clear();

    void update(CatalogCache newCatalogCache);

    Map<String, CatalogData> getCatalogDataMap();

    String getCatalogType(Product product) throws MotuException;
}
