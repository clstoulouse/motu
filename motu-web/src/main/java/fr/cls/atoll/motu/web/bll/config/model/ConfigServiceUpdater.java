package fr.cls.atoll.motu.web.bll.config.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.BLLManager;
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
            List<ConfigService> csToRemove = new ArrayList<>();
            for (ConfigService oldCS : BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService()) {
                int i = 0;
                while (i < newConfigServiceList.size() && !newConfigServiceList.get(i).getName().equals(oldCS.getName())) {
                    i++;
                }

                if (!(i < newConfigServiceList.size())) {
                    csToRemove.add(oldCS);
                }
            }

            for (ConfigService cs : csToRemove) {
                BLLManager.getInstance().getConfigManager().getConfigServiceMap().remove(cs.getName());
                BLLManager.getInstance().getConfigManager().getMotuConfig().getConfigService().remove(cs);
                LOGGER.info("Remove old config service: " + cs.getName());
            }

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

            if (!csToUpdateCache.isEmpty()) {
                BLLManager.getInstance().getCatalogManager().getCatalogAndProductCacheManager().updateCache(csToUpdateCache);
            }
        }

    }
}
