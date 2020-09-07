package fr.cls.atoll.motu.web.bll.catalog.product.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.common.thread.StoppableDaemonThread;
import fr.cls.atoll.motu.web.common.utils.DateUtils;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;

/**
 * This class is the scheduler of the cache refresh. When the Motu system needs to refresh the cache, it's
 * this class which have to manage this request. This class implements the pattern singleton. . <br>
 * <br>
 * Copyright : Copyright (c) 2017 <br>
 * <br>
 * Company : CLS (Collecte Localisation Satellites)
 * 
 * @author Pierre LACOSTE
 * @version $Revision: 1456 $ - $Date: 2011-04-08 18:37:34 +0200 $
 */
public class CacheRefreshScheduler extends StoppableDaemonThread {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * The list of ConfigService which are waiting to be updated. It's used as a FIFO stack.
     */
    private LinkedList<ConfigService> waitingConfigServiceToUpdate;

    /**
     * This is the unique instance of the class.
     */
    private static CacheRefreshScheduler instance = null;
    /**
     * This is the instance of the service which refresh a ConfigService
     */
    private CacheUpdateService refreshService;

    /**
     * This is the catalog cache to refresh
     */
    private ICatalogCache catalogCache;
    /**
     * This is the product cache to refresh
     */
    private IProductCache productCache;

    private List<Object> listeners;

    private HashMap<String, ConfigServiceState> serviceStates;

    private int configServiceRefreshedOK;
    private int configServiceRefreshedKO;

    private static Instant lastUpdate;
    private static Duration lastUpdateDuration;

    /**
     * Private constructor of the class. Call the super constructor to initialize the StopableDaemonThread
     * part.
     */
    private CacheRefreshScheduler() {
        super(
            "CacheRefreshScheduler Thread Daemon",
            BLLManager.getInstance().getConfigManager().getMotuConfig().getDescribeProductCacheRefreshInMilliSec());
        waitingConfigServiceToUpdate = new LinkedList<>();
        catalogCache = new CatalogCache();
        productCache = new ProductCache();
        refreshService = new CacheUpdateService(catalogCache, productCache);
        listeners = new ArrayList<>();
        serviceStates = new HashMap<>();
        configServiceRefreshedOK = 0;
        configServiceRefreshedKO = 0;
    }

    @Override
    public long getRefreshDelayInMSec() {
        return BLLManager.getInstance().getConfigManager().getMotuConfig().getDescribeProductCacheRefreshInMilliSec();
    }

    /**
     * Initialize if needed and return the unique instance of the singleton class. .
     * 
     * @return The unique class instance
     */
    public static final CacheRefreshScheduler getInstance() {
        if (instance == null) {
            instance = new CacheRefreshScheduler();
        }
        return instance;
    }

    /**
     * Return the cache of the catalog .
     * 
     * @return the catalog cache
     */
    public ICatalogCache getCatalogCache() {
        return catalogCache;
    }

    /**
     * This method is used to add new ConfigService to refresh to the cache refresh scheduler. If a
     * ConfigService on the provided list is already on the waiting list of the scheduler, only one occurrence
     * it's saved. .
     * 
     * @param configServiceToUpadte The list of ConfigService to update
     */
    public void update(List<ConfigService> configServiceToUpadte) {
        synchronized (waitingConfigServiceToUpdate) {
            for (ConfigService configService : configServiceToUpadte) {
                if (!waitingConfigServiceToUpdate.contains(configService)) {
                    LOGGER.info("Add '" + configService.getName() + "' to the cache refresh list");
                    waitingConfigServiceToUpdate.add(configService);
                    String name = configService.getName();
                    serviceStates.computeIfAbsent(name, ConfigServiceState::new);
                }
            }
        }
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * Return the cache of the product .
     * 
     * @return the product cache
     */
    public IProductCache getProductCache() {
        return productCache;
    }

    @Override
    protected void runProcess() {
        synchronized (waitingConfigServiceToUpdate) {
            if (!waitingConfigServiceToUpdate.isEmpty()) {
                CacheRefreshScheduler.setLastUpdate(Instant.now());
                ConfigService currentConfigService = null;
                do {
                    // Retrieve the next ConfigService to update in the FIFO stack
                    currentConfigService = waitingConfigServiceToUpdate.pollFirst();
                    if (currentConfigService != null) {
                        ConfigServiceState currentServiceState = serviceStates.get(currentConfigService.getName());
                        boolean updateOK = updateConfigService(currentConfigService, currentServiceState);
                        updateStatus(updateOK, currentServiceState);
                    }
                } while (currentConfigService != null && !isDaemonStoppedASAP());
                if (isCacheRefreshed()) {
                    CacheRefreshScheduler.setLastUpdateDuration(Duration.between(lastUpdate, Instant.now()));
                    LOGGER.info("Total refresh cache duration (x" + Long.toString(configServiceRefreshedOK) + "): "
                            + DateUtils.getDurationMinSecMsec(lastUpdateDuration.toMillis()));
                } else {
                    LOGGER.warn("Exiting refresh cache loop before of full caching of service (" + getAddedConfigServiceNumber() + " missing).");
                }
            }
        }
    }

    private void updateStatus(boolean updateOK, ConfigServiceState state) {
        if (updateOK) {
            if (!ConfigServiceState.SUCCESS.equals(state.getStatus())) {
                if (ConfigServiceState.FAILURE.equals(state.getStatus())) {
                    configServiceRefreshedKO--;
                }
                configServiceRefreshedOK++;
                state.setStatus(ConfigServiceState.SUCCESS);
            }
        } else {
            if (!ConfigServiceState.FAILURE.equals(state.getStatus())) {
                if (ConfigServiceState.SUCCESS.equals(state.getStatus())) {
                    configServiceRefreshedOK--;
                }
                configServiceRefreshedKO++;
                state.setStatus(ConfigServiceState.FAILURE);
            }
        }
    }

    private boolean updateConfigService(ConfigService currentConfigService, ConfigServiceState currentServiceState) {
        int nbRetryWhenNotOK = 3;
        int retryIndex = 0;
        boolean isUpdatedOK = false;
        while (retryIndex < nbRetryWhenNotOK && !isUpdatedOK) {
            retryIndex++;
            // Launch the refresh of the ConfigService
            currentServiceState.setLastUpdate(Instant.now());
            isUpdatedOK = refreshService.updateConfigService(currentConfigService);
            if (isUpdatedOK) {
                Duration updateService = Duration.between(currentServiceState.getLastUpdate(), Instant.now());
                LOGGER.info("Refresh '" + currentConfigService.getName() + "' cache duration: "
                        + DateUtils.getDurationMinSecMsec(updateService.toMillis()));
                currentServiceState.setLastUpdateDuration(updateService);
            } else {
                long waitTimeMsec = (long) retryIndex * 3000;
                LOGGER.info("Refresh KO: '" + currentConfigService.getName() + ", try " + retryIndex + "/" + nbRetryWhenNotOK + " wait  "
                        + waitTimeMsec + " msec before retry.");
                try {
                    Thread.sleep(waitTimeMsec);
                } catch (InterruptedException e) {
                    // noop
                    Thread.currentThread().interrupt();
                }
            }
        }
        return isUpdatedOK;
    }

    public void addListener(Object listener) {
        listeners.add(listener);
    }

    @Override
    public void onThreadStopped() {
        super.onThreadStopped();
        for (Object currentListener : listeners) {
            synchronized (currentListener) {
                currentListener.notifyAll();
            }
        }
    }

    /**
     * Return <code>true</code> if the Cache has been refreshed at least once.
     * 
     * @return Is the Cache ready ?
     */
    public boolean isCacheRefreshed() {
        return getAddedConfigServiceNumber() == 0;
    }

    public int getAddedConfigServiceNumber() {
        int result = 0;
        for (ConfigServiceState state : serviceStates.values()) {
            if (ConfigServiceState.ADDED.equals(state.getStatus())) {
                result++;
            }
        }
        return result;
    }

    /**
     * Gets the value of lastUpdate.
     *
     * @return the value of lastUpdate
     */
    public static Instant getLastUpdate() {
        return lastUpdate;
    }

    private static void setLastUpdate(Instant lastUpdate) {
        CacheRefreshScheduler.lastUpdate = lastUpdate;
    }

    /**
     * Gets the value of lastUpdateDuration.
     *
     * @return the value of lastUpdateDuration
     */
    public static Duration getLastUpdateDuration() {
        return lastUpdateDuration;
    }

    private static void setLastUpdateDuration(Duration lastUpdateDuration) {
        CacheRefreshScheduler.lastUpdateDuration = lastUpdateDuration;
    }

    /**
     * Gets the value of configServiceRefeshedOK.
     *
     * @return the value of configServiceRefeshedOK
     */
    public int getConfigServiceRefeshedOK() {
        return configServiceRefreshedOK;
    }

    /**
     * Gets the value of configServiceRefeshedKO.
     *
     * @return the value of configServiceRefeshedKO
     */
    public int getConfigServiceRefeshedKO() {
        return configServiceRefreshedKO;
    }

    public ConfigServiceState getConfigServiceState(String name) {
        return serviceStates.get(name);
    }
}
