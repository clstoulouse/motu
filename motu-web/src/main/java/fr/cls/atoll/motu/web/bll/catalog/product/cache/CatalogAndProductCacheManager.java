package fr.cls.atoll.motu.web.bll.catalog.product.cache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
     * The list of ConfigService to refresh configured as automatic refresh
     */
    private Set<ConfigService> partialConfigServiceToUpdate = new HashSet<>();
    /**
     * The list of all the available ConfigService
     */
    private Set<ConfigService> completeConfigServiceToUpdate = new HashSet<>();

    /**
     * The Daemon which launch regularly a refresh of the cache
     */
    private CatalogAndProductCacheRefreshThread productCacheDaemonThread;

    /**
     * The default constructor of the class.
     */
    public CatalogAndProductCacheManager() {
    }

    /**
     * Retrieve the configured ConfigService to initialize the instance ConfigService list.
     */
    private void initConfigServiceLisToUpdate() {
        List<ConfigService> configServicesList = BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService();

        for (ConfigService configService : configServicesList) {
            if (configService.getRefreshCacheAutomaticallyEnabled()) {
                partialConfigServiceToUpdate.add(configService);
            }
            completeConfigServiceToUpdate.add(configService);
        }
    }

    @Override
    /**
     * {@inheritDoc} <br/>
     * Launch the cache refresh scheduler daemon. <br/>
     * Launch the automatic cache refresh manager daemon.
     */
    public void init() {
        initConfigServiceLisToUpdate();
        CacheRefreshScheduler.getInstance().addListener(this);
        // The refresh scheduler have to be started before the automatic refresh daemon manager
        CacheRefreshScheduler.getInstance().start();
        // Initialize the the refresh scheduler with all the config service to initialize all the cache
        CacheRefreshScheduler.getInstance().update(completeConfigServiceToUpdate);
        productCacheDaemonThread = new CatalogAndProductCacheRefreshThread(partialConfigServiceToUpdate) {

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
    /**
     * {@inheritDoc}<br/>
     * Stop the cache refresh schedule daemon. <br/>
     * Stop the automatic cache refresh manager daemon.
     * 
     */
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
    public void updateCache() {
        CacheRefreshScheduler.getInstance().update(partialConfigServiceToUpdate);
    }

    @Override
    public void updateAllTheCache() {
        CacheRefreshScheduler.getInstance().update(completeConfigServiceToUpdate);
    }
}
