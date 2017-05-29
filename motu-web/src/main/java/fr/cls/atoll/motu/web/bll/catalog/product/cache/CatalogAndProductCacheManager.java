package fr.cls.atoll.motu.web.bll.catalog.product.cache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.common.utils.Status;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;

public class CatalogAndProductCacheManager implements ICatalogAndProductCacheManager {

    private static final Logger LOGGER = LogManager.getLogger();

    private ICatalogCache catalogCache;
    private IProductCache productCache;

    private CatalogAndProductCacheRefreshThread productCacheDaemonThread;
    private CacheUpdateService productCacheUpdate;
    private CacheUpdateService productCacheForceAllUpdate;

    private Status updateStatus;

    public CatalogAndProductCacheManager() {
        catalogCache = new CatalogCache();
        productCache = new ProductCache();

        updateStatus = new Status() {

            @Override
            public boolean isOK() {
                return true;
            }
        };
    }

    @Override
    public void init() {
        productCacheDaemonThread = new CatalogAndProductCacheRefreshThread(catalogCache, productCache) {

            /** {@inheritDoc} */
            @Override
            public void onThreadStopped() {
                super.onThreadStopped();
                synchronized (CatalogAndProductCacheManager.this) {
                    CatalogAndProductCacheManager.this.notify();
                }
            }

        };
        productCacheDaemonThread.start();

        productCacheUpdate = new CacheUpdateService(catalogCache, productCache) {
            @Override
            boolean isProductToUpdate(ConfigService configService) {
                return configService.getRefreshCacheAutomaticallyEnabled();
            }
        };

        productCacheForceAllUpdate = new CacheUpdateService(catalogCache, productCache) {
            @Override
            boolean isProductToUpdate(ConfigService configService) {
                return true;
            }
        };
    }

    @Override
    public void stop() {
        productCacheDaemonThread.setDaemonStoppingASAP(true);
        synchronized (this) {
            if (!productCacheDaemonThread.isDaemonStopped()) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    LOGGER.error("Error during wait while stopping daemon: " + productCacheDaemonThread.getName());
                }
            }
        }
    }

    @Override
    public IProductCache getProductCache() {
        return productCache;
    }

    @Override
    public ICatalogCache getCatalogCache() {
        return catalogCache;
    }

    @Override
    public void updateCache() {
        productCacheUpdate.update(updateStatus);
    }

    @Override
    public void updateAllTheCache() {
        productCacheForceAllUpdate.update(updateStatus);
    }
}
