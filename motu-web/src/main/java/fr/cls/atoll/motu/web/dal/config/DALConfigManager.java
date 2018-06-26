package fr.cls.atoll.motu.web.dal.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.config.updater.IConfigUpdatedListener;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.common.utils.PropertiesUtilities;
import fr.cls.atoll.motu.web.dal.config.stdname.StdNameReader;
import fr.cls.atoll.motu.web.dal.config.stdname.xml.model.StandardName;
import fr.cls.atoll.motu.web.dal.config.stdname.xml.model.StandardNames;
import fr.cls.atoll.motu.web.dal.config.version.DALVersionManager;
import fr.cls.atoll.motu.web.dal.config.version.IDALVersionManager;
import fr.cls.atoll.motu.web.dal.config.watcher.ConfigWatcherThread;
import fr.cls.atoll.motu.web.dal.config.xml.model.MotuConfig;
import fr.cls.atoll.motu.web.dal.config.xml.model.ObjectFactory;

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
public class DALConfigManager implements IDALConfigManager {

    private IDALVersionManager dalVersionManager;
    /** Application configuration. */
    private MotuConfig motuConfig = null;

    private List<StandardName> standardNameList;
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String FILENAME_FORMAT_REQUESTID = "@@requestId@@";
    public static final String FILENAME_FORMAT_PRODUCT_ID = "@@productId@@";

    private IConfigUpdatedListener configUpdatedListener;

    public DALConfigManager() {
        dalVersionManager = new DALVersionManager();
    }

    /** {@inheritDoc} */
    @Override
    public void init() throws MotuException {
        if (motuConfig == null) {
            File fMotuConfig = new File(getMotuConfigurationFolderPath(), "motuConfiguration.xml");
            motuConfig = loadMotuConfig(fMotuConfig, false);
            startConfigWatcherThread(fMotuConfig);
        }
        if (standardNameList == null) {
            standardNameList = loadStandardNameList();
        }
    }

    /**
     * Valeur de standardNameList.
     * 
     * @return la valeur.
     */
    @Override
    public List<StandardName> getStandardNameList() {
        return standardNameList;
    }

    /**
     * .
     * 
     * @throws MotuException
     */
    private List<StandardName> loadStandardNameList() throws MotuException {
        List<StandardName> curStandardNameList = null;
        StandardNames sn = new StdNameReader().getStdNameEquiv();
        if (sn != null) {
            curStandardNameList = sn.getStandardName();
        } else {
            LOGGER.warn("No standard names loaded from configuration folder");
        }
        return curStandardNameList;
    }

    /** {@inheritDoc} */
    @Override
    public String getCasServerUrl() {
        return System.getProperty("cas-server-url", null);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCasActivated() {
        return Boolean.parseBoolean(System.getProperty("cas-activated", "False"));
    }

    /** {@inheritDoc} */
    @Override
    public String getMotuConfigurationFolderPath() {
        return System.getProperty("motu-config-dir", null);
    }

    private MotuConfig loadMotuConfig(File fMotuConfig, boolean initConfigWatcher) throws MotuException {
        MotuConfig curMotuConfig = null;
        InputStream in = null;
        try {
            if (initConfigWatcher) {
                startConfigWatcherThread(fMotuConfig);
            }
            in = getMotuConfigInputStream(fMotuConfig);
            curMotuConfig = parseMotuConfig(in);
        } catch (IOException io) {
            LOGGER.error("Error while loading motuConfiguration.xml", io);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException io) {
                LOGGER.error("Error while closing input stream motuConfiguration.xml", io);
            }

        }
        return curMotuConfig;
    }

    private void startConfigWatcherThread(File fMotuConfig) {
        ConfigWatcherThread t = new ConfigWatcherThread(fMotuConfig) {

            @Override
            public void onMotuConfigurationUpdated(File configFile) {
                DALConfigManager.this.onMotuConfigurationUpdated(configFile);
            }

        };
        t.start();
    }

    private void onMotuConfigurationUpdated(File configFile) {
        try {
            MotuConfig mc = loadMotuConfig(configFile, false);
            onMotuConfigUpdated(mc);
        } catch (MotuException e) {
            LOGGER.error("Error while loading motuConfiguration from config watcher", e);
        }
    }

    private void onMotuConfigUpdated(MotuConfig newMotuConfig) {
        if (configUpdatedListener != null) {
            configUpdatedListener.onMotuConfigUpdated(newMotuConfig);
        }
        motuConfig = newMotuConfig;
    }

    private InputStream getMotuConfigInputStream(File fMotuConfig) throws FileNotFoundException {
        InputStream in;
        if (fMotuConfig.exists()) {
            in = new FileInputStream(fMotuConfig);
        } else {
            in = DALConfigManager.class.getClassLoader().getResourceAsStream("motuConfiguration.xml");
        }
        return in;
    }

    private MotuConfig parseMotuConfig(InputStream in) throws MotuException {
        MotuConfig curMotuConfig;
        try {
            JAXBContext jc = JAXBContext.newInstance(MotuConfig.class.getPackage().getName());
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            curMotuConfig = (MotuConfig) unmarshaller.unmarshal(in);
            curMotuConfig.setExtractionPath(PropertiesUtilities.replaceSystemVariable(curMotuConfig.getExtractionPath()));
            curMotuConfig.setDownloadHttpUrl(PropertiesUtilities.replaceSystemVariable(curMotuConfig.getDownloadHttpUrl()));
        } catch (Exception e) {
            throw new MotuException(ErrorType.SYSTEM, "Error in getMotuConfigInstance", e);
        }

        ObjectFactory motuConfigObjectFactory = new ObjectFactory();
        MotuConfig blankMotuConfig = motuConfigObjectFactory.createMotuConfig();
        if (curMotuConfig.getRefreshCacheToken().equals(blankMotuConfig.getRefreshCacheToken())) {
            LOGGER.error("Security breach: The token for the update of the cache is still set to the default value.\n"
                    + "To improve the security of the server please change this token into the motuConfiguration.xml file.");
        }
        return curMotuConfig;
    }

    @Override
    public MotuConfig getMotuConfig() {
        return motuConfig;
    }

    /** {@inheritDoc} */
    @Override
    public IDALVersionManager getVersionManager() {
        return dalVersionManager;
    }

    @Override
    public void setConfigUpdatedListener(IConfigUpdatedListener configUpdatedListener_) {
        configUpdatedListener = configUpdatedListener_;
    }
}
