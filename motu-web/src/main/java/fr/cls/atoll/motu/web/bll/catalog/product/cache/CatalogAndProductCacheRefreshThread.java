package fr.cls.atoll.motu.web.bll.catalog.product.cache;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.common.thread.StoppableDaemonThread;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;

/**
 * Manage the automatique cache refresh. <br>
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

    /**
     * Save the list of configService to update automatically
     */
    private Set<ConfigService> configServiceToUpdate;

    /**
     * 
     * Constructor.
     * 
     * @param configServiceToUpdate_ The list of ConfigService that needs to be refresh automatically.
     */
    public CatalogAndProductCacheRefreshThread(Set<ConfigService> configServiceToUpdate_) {
        super(
            "Product and Catalog Cache Thread Daemon",
            BLLManager.getInstance().getConfigManager().getMotuConfig().getDescribeProductCacheRefreshInMilliSec());
        this.configServiceToUpdate = configServiceToUpdate_;
    }

    /** {@inheritDoc} */
    @Override
    public void runProcess() {
        BLLManager.getInstance().getCatalogManager().getCatalogAndProductCacheManager().updateCache();
    }
}
