package fr.cls.atoll.motu.web.bll.config.updater;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.config.comparator.ConfigServiceComparator;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.config.xml.model.MotuConfig;

public class ConfigServiceUpdater {

    private static final Logger LOGGER = LogManager.getLogger();
    private ConfigServiceComparator configServiceComparator;

    public ConfigServiceUpdater() {
        configServiceComparator = new ConfigServiceComparator();
    }

    public void onMotuConfigUpdated(MotuConfig newMotuConfig) {
        List<ConfigService> newConfigServiceList = newMotuConfig.getConfigService();
        if (newConfigServiceList != null) {
            // Remove all configService which have been removed in the new configuration file
            removeConfigServiceWhichAreNotInThisList(newConfigServiceList);

            List<ConfigService> csToUpdateCache = addOrUpdateNewConfigService(newConfigServiceList);
            if (!csToUpdateCache.isEmpty()) {
                BLLManager.getInstance().getCatalogManager().getCatalogAndProductCacheManager().updateCache(csToUpdateCache);
            }
        }

        BLLManager.getInstance().getRequestManager().getQueueServerManager().onConfigUpdated(newMotuConfig.getQueueServerConfig());
    }

    private List<ConfigService> addOrUpdateNewConfigService(List<ConfigService> newConfigServiceList) {
        List<ConfigService> csToUpdateCache = new ArrayList<>();
        for (ConfigService newCS : newConfigServiceList) {
            ConfigService oldCS = BLLManager.getInstance().getConfigManager().getConfigService(newCS.getName());
            if (oldCS == null) {
                BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService().add(newCS);
                LOGGER.info("Add new config service: " + newCS.getName());
                csToUpdateCache.add(newCS);
            } else if (configServiceComparator.compare(newCS, oldCS) != 0) {
                BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService().remove(oldCS);
                BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService().add(newCS);
                LOGGER.info("Update new config service: " + newCS.getName());
                csToUpdateCache.add(newCS);
            }
        }
        return csToUpdateCache;
    }

    private void removeConfigServiceWhichAreNotInThisList(List<ConfigService> newConfigServiceList) {
        List<ConfigService> configServiceListToRemove = new ArrayList<>();
        for (ConfigService oldCS : BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService()) {
            int i = 0;
            while (i < newConfigServiceList.size() && !newConfigServiceList.get(i).getName().equals(oldCS.getName())) {
                i++;
            }

            if (!(i < newConfigServiceList.size())) {
                configServiceListToRemove.add(oldCS);
            }
        }

        for (ConfigService cs : configServiceListToRemove) {
            removeConfigService(cs);
            LOGGER.info("Remove old config service: " + cs.getName());
        }
    }

    private void removeConfigService(ConfigService cs) {
        if (!BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService().remove(cs)) {
            LOGGER.warn("Unable to remove old config service: " + cs.getName());
        }
        BLLManager.getInstance().getCatalogManager().getCatalogAndProductCacheManager().onConfigServiceRemoved(cs);
    }
}
