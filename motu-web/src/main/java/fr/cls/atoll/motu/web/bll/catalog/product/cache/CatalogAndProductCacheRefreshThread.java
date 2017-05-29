package fr.cls.atoll.motu.web.bll.catalog.product.cache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.common.thread.StoppableDaemonThread;
import fr.cls.atoll.motu.web.common.utils.Status;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;

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
public abstract class CatalogAndProductCacheRefreshThread extends StoppableDaemonThread {

    private static final Logger LOGGER = LogManager.getLogger();

    private ICatalogCache catalogCache;
    private IProductCache productCache;

    private CacheUpdateService cacheUpdateService;

    private Status stopStatus;

    /**
     * Constructeur.
     */
    public CatalogAndProductCacheRefreshThread(ICatalogCache catalogCache_, IProductCache productCache_) {
        super(
            "Product and Catalog Cache Thread Daemon",
            BLLManager.getInstance().getConfigManager().getMotuConfig().getDescribeProductCacheRefreshInMilliSec());
        catalogCache = catalogCache_;
        productCache = productCache_;

        cacheUpdateService = new CacheUpdateService(catalogCache_, productCache_) {

            @Override
            boolean isProductToUpdate(ConfigService configService) {
                return isFirstStart() || configService.getRefreshCacheAutomaticallyEnabled();
            }
        };

        stopStatus = new Status() {
            @Override
            public synchronized boolean isOK() {
                return isDaemonStoppedASAP();
            }
        };

    }

    /** {@inheritDoc} */
    @Override
    public void runProcess() {
        cacheUpdateService.update(stopStatus);
    }
}
