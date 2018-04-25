package fr.cls.atoll.motu.web.bll.config;

import java.util.List;

import fr.cls.atoll.motu.web.bll.config.updater.ConfigServiceUpdater;
import fr.cls.atoll.motu.web.bll.config.updater.IConfigUpdatedListener;
import fr.cls.atoll.motu.web.bll.config.version.BLLVersionManager;
import fr.cls.atoll.motu.web.bll.config.version.IBLLVersionManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.DALManager;
import fr.cls.atoll.motu.web.dal.config.IDALConfigManager;
import fr.cls.atoll.motu.web.dal.config.stdname.xml.model.StandardName;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.config.xml.model.MotuConfig;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class BLLConfigManager implements IBLLConfigManager {

    private IDALConfigManager dalConfigManager;
    private IBLLQueueServerConfigManager bllQueueServerConfigManager;
    private IBLLVersionManager bllVersionManager;
    private ConfigServiceUpdater configServiceUpdater;

    public BLLConfigManager() {
        configServiceUpdater = new ConfigServiceUpdater();
        dalConfigManager = DALManager.getInstance().getConfigManager();
        bllQueueServerConfigManager = new BLLQueueServerConfigManager();
        bllVersionManager = new BLLVersionManager();
    }

    @Override
    public void init() throws MotuException {
        dalConfigManager.setConfigUpdatedListener(new IConfigUpdatedListener() {

            @Override
            public void onMotuConfigUpdated(MotuConfig newMotuConfig) {
                configServiceUpdater.onMotuConfigUpdated(newMotuConfig);
            }

        });
        dalConfigManager.init();
        bllVersionManager.init();
    }

    /** {@inheritDoc} */
    @Override
    public String getCasServerUrl() {
        return dalConfigManager.getCasServerUrl();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCasActivated() {
        return dalConfigManager.isCasActivated();
    }

    /**
     * Valeur de standardNameList.
     * 
     * @return la valeur.
     */
    @Override
    public List<StandardName> getStandardNameList() {
        return dalConfigManager.getStandardNameList();
    }

    /**
     * Valeur de bllQueueServerConfig.
     * 
     * @return la valeur.
     */
    @Override
    public IBLLQueueServerConfigManager getQueueServerConfigManager() {
        return bllQueueServerConfigManager;
    }

    /** {@inheritDoc} */
    @Override
    public MotuConfig getMotuConfig() {
        return dalConfigManager.getMotuConfig();
    }

    /** {@inheritDoc} */
    @Override
    public String getProductDownloadHttpUrl() {
        return dalConfigManager.getMotuConfig().getDownloadHttpUrl();
    }

    /** {@inheritDoc} */
    @Override
    public ConfigService getConfigService(String configServiceName) {
        ConfigService csResult = null;
        for (ConfigService c : getMotuConfig().getConfigService()) {
            if (c.getName().equalsIgnoreCase(configServiceName)) {
                csResult = c;
                break;
            }
        }
        return csResult;
    }

    /** {@inheritDoc} */
    @Override
    public String getMotuConfigurationFolderPath() {
        return dalConfigManager.getMotuConfigurationFolderPath();
    }

    /** {@inheritDoc} */
    @Override
    public IBLLVersionManager getVersionManager() {
        return bllVersionManager;
    }
}