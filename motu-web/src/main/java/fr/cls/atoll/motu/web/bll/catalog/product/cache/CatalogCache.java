package fr.cls.atoll.motu.web.bll.catalog.product.cache;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;

public class CatalogCache implements ICatalogCache {

    private Map<String, CatalogData> catalogDataMap;
    
    public CatalogCache(){
        catalogDataMap = new HashMap<>();
        MapUtils.synchronizedMap(catalogDataMap);
    }
    
    @Override
    public void putCatalog(String configServiceName, CatalogData cd){
        catalogDataMap.put(configServiceName, cd);
    }
    
    @Override
    public CatalogData getCatalog(String configServiceName){
        return catalogDataMap.get(configServiceName);
    }
    
    @Override
    public Map<String, CatalogData> getCatalogDataMap(){
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

    @Override
    public String getCatalogType(Product product) throws MotuException {
        ConfigService serviceFound = null;

        String locationData = product.getLocationData();

        if (!StringUtils.isNullOrEmpty(locationData)) {
            for (ConfigService c : BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService()) {
                CatalogData cd = getCatalog(c.getName());
                if (cd != null) {
                    Map<String, Product> products = cd.getProducts();
                    for (Map.Entry<String, Product> currentProduct : products.entrySet()) {
                        if (currentProduct.getValue().getProductId().equals(product.getProductId())) {
                            serviceFound = c;
                            break;
                        }
                    }
                    if (serviceFound != null) {
                        break;
                    }
                }
            }
        }
        return BLLManager.getInstance().getCatalogManager().getCatalogType(serviceFound);
    }

}
