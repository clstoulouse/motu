package fr.cls.atoll.motu.web.bll.catalog.product.cache;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.common.utils.Status;
import fr.cls.atoll.motu.web.dal.DALManager;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;

public abstract class CacheUpdateService {

    private static final Logger LOGGER = LogManager.getLogger();

    private ICatalogCache catalogCache;
    private IProductCache productCache;

    private boolean isFirstStart = false;

    public CacheUpdateService(ICatalogCache catalogCache_, IProductCache productCache_) {
        catalogCache = catalogCache_;
        productCache = productCache_;
    }

    /** {@inheritDoc} */
    public void update(Status stopStatus) {
        long startRefresh = System.currentTimeMillis();

        long startRefreshCS = System.currentTimeMillis();
        List<ConfigService> services = BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService();
        int i = 0;
        Map<Long, String> statisticsRefreshMap = new TreeMap<Long, String>(Collections.reverseOrder());
        isFirstStart = catalogCache.getCatalogDataMap().size() <= 0;
        CatalogCache curCatalogCache = new CatalogCache();
        while (!stopStatus.isOK() && i < services.size()) {
            startRefreshCS = System.currentTimeMillis();
            ConfigService configService = services.get(i);
            if (isProductToUpdate(configService)) {
                processConfigService(configService, curCatalogCache);
                long updateDurationMSec = System.currentTimeMillis() - startRefreshCS;
                if (isFirstStart) {
                    LOGGER.info("First start, cache ready for: " + configService.getName() + ": "
                            + fr.cls.atoll.motu.web.common.utils.DateUtils.getDurationMinSecMsec(updateDurationMSec));
                    catalogCache.update(curCatalogCache);
                }
                statisticsRefreshMap.put(updateDurationMSec, configService.getName() + "@" + (i + 1));
            }
            i++;
        }
        catalogCache.clear();
        catalogCache.update(curCatalogCache);
        LOGGER.info("Product and catalog caches refreshed in "
                + fr.cls.atoll.motu.web.common.utils.DateUtils.getDurationMinSecMsec(System.currentTimeMillis() - startRefresh));

        StringBuffer sb = new StringBuffer();
        for (Entry<Long, String> kv : statisticsRefreshMap.entrySet()) {
            sb.append(kv.getValue() + "=" + fr.cls.atoll.motu.web.common.utils.DateUtils.getDurationMinSecMsec(kv.getKey()) + ", ");
        }
        LOGGER.info("Refreshed statistics: " + sb.toString());
    }

    /**
     * .
     * 
     * @param configService
     */
    private void processConfigService(ConfigService configService, CatalogCache curCatalogCache) {
        try {
            CatalogData cd = DALManager.getInstance().getCatalogManager().getCatalogData(configService);
            if (cd != null) {
                curCatalogCache.putCatalog(configService.getName(), cd);
                Map<String, Product> products = cd.getProducts();
                for (Map.Entry<String, Product> currentProductEntry : products.entrySet()) {
                    Product currentProduct = currentProductEntry.getValue();

                    try {
                        DALManager.getInstance().getCatalogManager().getProductManager()
                                .updateMetadata(BLLManager.getInstance().getCatalogManager().getCatalogType(configService),
                                                currentProduct.getProductId(),
                                                currentProduct.getLocationData(),
                                                currentProduct.getProductMetaData());
                        productCache.setProduct(configService.getName(), currentProduct);
                    } catch (Exception e) {
                        LOGGER.error("Error during refresh of the describe product cache, config service=" + configService.getName() + ", productId="
                                + currentProduct.getProductId(), e);
                    }
                }
            } else {
                LOGGER.error("Unable to read catalog data for config service " + configService.getName());
            }
        } catch (MotuException e) {
            LOGGER.error("Error during refresh of the describe product cache", e);
        }
    }

    abstract boolean isProductToUpdate(ConfigService configService);

    public boolean isFirstStart() {
        return isFirstStart;
    }

}
