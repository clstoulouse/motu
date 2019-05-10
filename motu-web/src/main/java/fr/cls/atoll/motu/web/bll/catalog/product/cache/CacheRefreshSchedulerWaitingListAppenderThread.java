package fr.cls.atoll.motu.web.bll.catalog.product.cache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.common.thread.StoppableDaemonThread;

/**
 * Manage the automatic cache refresh. <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Pierre LACOSTE
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public abstract class CacheRefreshSchedulerWaitingListAppenderThread extends StoppableDaemonThread {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * 
     * Constructor.
     * 
     */
    public CacheRefreshSchedulerWaitingListAppenderThread() {
        super(
            CacheRefreshSchedulerWaitingListAppenderThread.class.getSimpleName(),
            BLLManager.getInstance().getConfigManager().getMotuConfig().getDescribeProductCacheRefreshInMilliSec(),
            true);
        init();
    }

    public void init() {
        BLLManager.getInstance().getCatalogManager().getCatalogAndProductCacheManager().updateAllTheCache();
    }

    /** {@inheritDoc} */
    @Override
    public void runProcess() {
        BLLManager.getInstance().getCatalogManager().getCatalogAndProductCacheManager().updateCache();
    }
}
