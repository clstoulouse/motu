package fr.cls.atoll.motu.web.bll.catalog.product.cache;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.common.thread.StoppableDaemonThread;
import fr.cls.atoll.motu.web.dal.DALManager;
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
 * @author Pierre LACOSTE
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class CatalogAndProductCacheRefreshThread extends StoppableDaemonThread {

    private static final Logger LOGGER = LogManager.getLogger();

    private ICatalogCache catalogCache;
    private IProductCache productCache;

    /**
     * Constructeur.
     */
    public CatalogAndProductCacheRefreshThread(ICatalogCache catalogCache_, IProductCache productCache_) {
        super(
            "Product and Catalog Cache Thread Daemon",
            BLLManager.getInstance().getConfigManager().getMotuConfig().getDescribeProductCacheRefreshInMilliSec());
        catalogCache = catalogCache_;
        productCache = productCache_;
    }

    /** {@inheritDoc} */
    @Override
    public void runProcess() {
        long startRefresh = System.currentTimeMillis();
        List<ConfigService> services = BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService();
        int i = 0;
        CatalogCache curCatalogCache = new CatalogCache();
        while (!isDaemonStoppedASAP() && i < services.size()) {
            ConfigService configService = services.get(i);
            processConfigService(configService, curCatalogCache);
            i++;
        }
        catalogCache.clear();
        catalogCache.update(curCatalogCache);
        LOGGER.info("Product and catalog caches refreshed in "
                + fr.cls.atoll.motu.web.common.utils.DateUtils.getDurationMinSecMsec(System.currentTimeMillis() - startRefresh));
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

                        // if (pmd != null) {
                        // currentProduct.setProductMetaData(pmd);
                        // }
                        productCache.setProduct(configService.getName(), currentProduct);
                    } catch (MotuException e) {
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

}
