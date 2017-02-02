package fr.cls.atoll.motu.web.bll.catalog.product.cache;

public interface ICatalogAndProductCacheManager {

    void init();

    void stop();

    IProductCache getProductCache();

    ICatalogCache getCatalogCache();

}
