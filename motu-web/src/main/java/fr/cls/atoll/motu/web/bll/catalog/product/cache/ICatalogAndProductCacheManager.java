package fr.cls.atoll.motu.web.bll.catalog.product.cache;

import java.util.List;

import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;

public interface ICatalogAndProductCacheManager {

    void init();

    void stop();

    /**
     * Retrieve the cache of the product .
     * 
     * @return the product cache instance
     */
    IProductCache getProductCache();

    /**
     * Retrieve the cache of the catalog .
     * 
     * @return the catalog cache instance
     */
    ICatalogCache getCatalogCache();

    /**
     * Refresh the cache only for ConfigService present in the provided list .
     * 
     * @param configServiceList the list of config service to update the cache
     */
    void updateCache(List<ConfigService> configServiceList);

    /**
     * Refresh the cache only for ConfigService defines as automatic refresh .
     */
    void updateCache();

    /**
     * Refresh the cache for all the available ConfigService. .
     */
    void updateAllTheCache();

    void onConfigServiceRemoved(ConfigService cs);
}
