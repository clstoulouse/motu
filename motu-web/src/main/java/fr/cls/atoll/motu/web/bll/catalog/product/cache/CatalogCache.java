package fr.cls.atoll.motu.web.bll.catalog.product.cache;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;

public class CatalogCache implements ICatalogCache {

    /**
     * Key is the catalog service name
     */
    private Map<String, CatalogData> catalogDataMap;

    public CatalogCache() {
        catalogDataMap = new HashMap<>();
        MapUtils.synchronizedMap(catalogDataMap);
    }

    @Override
    public void putCatalog(String configServiceName, CatalogData cd) {
        catalogDataMap.put(configServiceName, cd);
    }

    @Override
    public CatalogData getCatalog(String configServiceName) {
        return catalogDataMap.get(configServiceName);
    }

    @Override
    public Map<String, CatalogData> getCatalogDataMap() {
        return catalogDataMap;
    }

    @Override
    public void clear() {
        catalogDataMap.clear();
    }

    @Override
    public void update(CatalogCache newCatalogCache) {
        catalogDataMap.putAll(newCatalogCache.getCatalogDataMap());
    }

}
