package fr.cls.atoll.motu.web.bll.catalog.product.cache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CatalogAndProductCacheManager implements ICatalogAndProductCacheManager {

    private static final Logger LOGGER = LogManager.getLogger();

    private ICatalogCache catalogCache;
    private IProductCache productCache;

    private CatalogAndProductCacheRefreshThread productCacheDaemonThread;

    public CatalogAndProductCacheManager() {
        catalogCache = new CatalogCache();
        productCache = new ProductCache();
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
}
