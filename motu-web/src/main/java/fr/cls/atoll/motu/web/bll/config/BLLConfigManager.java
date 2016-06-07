package fr.cls.atoll.motu.web.bll.config;

import java.util.List;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.DALManager;
import fr.cls.atoll.motu.web.dal.config.IDALConfigManager;
import fr.cls.atoll.motu.web.dal.config.stdname.xml.model.StandardName;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.config.xml.model.MotuConfig;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;

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

    public BLLConfigManager() {
        dalConfigManager = DALManager.getInstance().getConfigManager();
        bllQueueServerConfigManager = new BLLQueueServerConfigManager();
    }

    @Override
    public void init() throws MotuException {
        dalConfigManager.init();
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
    public boolean isStatusAsFile() {
        // TODO SMA To implement, this value was read in the WEB.xml file (=> PARAM_STATUS_AS_FILE =
        // "statusAsFile";), now we have to move it to another
        // config file
        return false;
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

    /**
     * {@inheritDoc}
     * 
     * @throws MotuException
     */
    @Override
    public Product getProduct(String productLocation) throws MotuException {
        Product productResult = null;
        for (ConfigService c : getMotuConfig().getConfigService()) {
            String currentProductLocation = c.getCatalog().getUrlSite() + c.getCatalog().getName();
            if (currentProductLocation.equalsIgnoreCase(productLocation)) {
                productResult = BLLManager.getInstance().getCatalogManager().getProductManager().getProduct(c.getName(), c.getCatalog().getName());
                break;
            }
        }
        return productResult;
    }

    /** {@inheritDoc} */
    @Override
    public String getMotuConfigurationFolderPath() {
        return dalConfigManager.getMotuConfigurationFolderPath();
    }

}
