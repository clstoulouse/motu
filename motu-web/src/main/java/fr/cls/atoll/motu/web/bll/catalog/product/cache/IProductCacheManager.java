package fr.cls.atoll.motu.web.bll.catalog.product.cache;

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
public interface IProductCacheManager {

    void init();

    /**
     * .
     */
    void stop();

    /**
     * .
     * 
     * @param productId
     * @return
     */
    Product getProduct(String productId);
}
