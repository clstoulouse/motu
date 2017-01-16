package fr.cls.atoll.motu.web.bll.config.version;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.dal.DALManager;
import fr.cls.atoll.motu.web.dal.config.version.IDALVersionManager;

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
public class BLLVersionManager implements IBLLVersionManager {

    private static final Logger LOGGER = LogManager.getLogger();

    private IDALVersionManager dalVersionManager;

    /*
     * Cache to be sure that the version is the running version. It means that if an administrator extract a
     * new configuration archive but does not restart Motu, the configuration version return will be the
     * running one and not the last extracted one.
     */
    private String distributionVersion;
    private String configurationVersion;
    private String productsVersion;

    public BLLVersionManager() {
        dalVersionManager = DALManager.getInstance().getConfigManager().getVersionManager();
    }

    @Override
    public void init() {
        try {
            distributionVersion = dalVersionManager.getDistributionVersion();
        } catch (MotuException e) {
            distributionVersion = "Unknow version";
            LOGGER.error("Error while reading distribution version: ", e);
        }

        try {
            configurationVersion = dalVersionManager.getConfigurationVersion();
        } catch (MotuException e) {
            configurationVersion = "Unknow version";
            LOGGER.error("Error while reading configuration version: ", e);
        }

        try {
            productsVersion = dalVersionManager.getProductsVersion();
        } catch (MotuException e) {
            productsVersion = "Unknow version";
            LOGGER.error("Error while reading products version: ", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getDistributionVersion() {
        return distributionVersion;
    }

    /** {@inheritDoc} */
    @Override
    public String getConfigurationVersion() {
        return configurationVersion;
    }

    /** {@inheritDoc} */
    @Override
    public String getProductsVersion() {
        return productsVersion;
    }

    /** {@inheritDoc} */
    @Override
    public String getStaticFilesVersion() {
        String httpBaseRef = BLLManager.getInstance().getConfigManager().getMotuConfig().getHttpBaseRef();
        if (StringUtils.isNullOrEmpty(httpBaseRef) || httpBaseRef.equalsIgnoreCase(".")) {
            return null;
        } else {
            return httpBaseRef + "/version-static-files.js";
        }
    }

}
