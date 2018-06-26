package fr.cls.atoll.motu.web.dal.catalog;

import fr.cls.atoll.motu.web.dal.config.xml.model.CatalogService;
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
public class AbstractCatalogLoader {

    protected String getCatalogURL(CatalogService catalogService) {
        String url = catalogService.getUrlSite();
        if (!catalogService.getUrlSite().endsWith("/")) {
            url += "/";
        }
        return url + catalogService.getName();
    }

    /**
     * Associates the specified value with the specified key in this map (optional operation).
     * 
     * @param product_ value to be associated with the specified key.
     * @param key key with which the specified value is to be associated.
     * 
     * @return previous value associated with specified key, or <tt>null</tt>
     * 
     * @see java.util.Map#put(Object,Object)
     * @uml.property name="products"
     */
    protected Product putProducts(String key, Product product_, CatalogData cd) {
        if (key == null) {
            return null;
        }

        if (product_ == null) {
            return null;
        }

        cd.getProductsByTdsUrl().put(product_.getTdsUrlPath(), product_);

        return cd.getProducts().put(key.trim(), product_);
    }
}
