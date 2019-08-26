package fr.cls.atoll.motu.web.bll.catalog.product.cache;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;

/**
 * Provide methods to retrieve or to refresh the catalog and product cache of Motu <br>
 * <br>
 * Copyright : Copyright (c) 2017 <br>
 * <br>
 * Company : CLS (Collecte Localisation Satellites)
 * 
 * @author Pierre LACOSTE
 * @version $Revision: 1456 $ - $Date: 2011-04-08 18:37:34 +0200 $
 */
public class CatalogAndProductCacheManager implements ICatalogAndProductCacheManager {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * The Daemon which launch regularly a refresh of the cache
     */
    private CacheRefreshSchedulerWaitingListAppenderThread productCacheDaemonThread;

    /**
     * The default constructor of the class.
     */
    public CatalogAndProductCacheManager() {
    }

    @Override
    /**
     * {@inheritDoc} <br/>
     * Launch the cache refresh scheduler daemon. <br/>
     * Launch the automatic cache refresh manager daemon.
     */
    public synchronized void init() {
        if (productCacheDaemonThread == null) {
            CacheRefreshScheduler.getInstance().addListener(this);

            // Initialize the refresh scheduler with all the config service to initialize all the cache
            // The start of CacheRefreshSchedulerWaitingListAppenderThread is equivalent to the commented line below:
            // CacheRefreshScheduler.getInstance().update(BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService());
            // This daemon appends in a list all the config services which have to be refreshed
            // the first time, all the config servces are appended
            // then only config without refreshCacheAutomaticallyEnabled="false" are appended to the list
            productCacheDaemonThread = new CacheRefreshSchedulerWaitingListAppenderThread() {

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

            // The refresh scheduler, then polls config services from this list and refresh them
            CacheRefreshScheduler.getInstance().start();
        }
    }

    /**
     * Retrieve the configured ConfigService to initialize the instance ConfigService list.
     */
    private List<ConfigService> getConfigServiceListToRefreshAutomatically() {
        List<ConfigService> configServiceToRefreshAutomatically = new ArrayList<>();
        for (ConfigService configService : BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService()) {
            if (configService.getRefreshCacheAutomaticallyEnabled()) {
                configServiceToRefreshAutomatically.add(configService);
            }
        }
        return configServiceToRefreshAutomatically;
    }

    @Override
    /**
     * {@inheritDoc}<br/>
     * Stop the cache refresh schedule daemon. <br/>
     * Stop the automatic cache refresh manager daemon.
     * 
     */
    public void stop() {
        if (productCacheDaemonThread != null) {
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
        CacheRefreshScheduler.getInstance().setDaemonStoppingASAP(true);
        synchronized (this) {
            if (!CacheRefreshScheduler.getInstance().isDaemonStopped()) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    LOGGER.error("Error during wait while stopping daemon: " + CacheRefreshScheduler.getInstance().getName());
                }
            }
        }
    }

    @Override
    public IProductCache getProductCache() {
        return CacheRefreshScheduler.getInstance().getProductCache();
    }

    @Override
    public ICatalogCache getCatalogCache() {
        return CacheRefreshScheduler.getInstance().getCatalogCache();
    }

    @Override
    public void updateCache(List<ConfigService> configServiceList) {
        CacheRefreshScheduler.getInstance().update(configServiceList);
    }

    @Override
    public void updateCache() {
        updateCache(getConfigServiceListToRefreshAutomatically());
    }

    @Override
    public void updateAllTheCache() {
        updateCache(BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService());
    }

    @Override
    public void onConfigServiceRemoved(ConfigService cs) {
        getCatalogCache().getCatalogDataMap().remove(cs.getName());
        getProductCache().removeProduct(cs.getName());
    }
}
