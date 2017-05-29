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
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.common.utils.PropertiesUtilities;
import fr.cls.atoll.motu.web.dal.config.stdname.StdNameReader;
import fr.cls.atoll.motu.web.dal.config.stdname.xml.model.StandardName;
import fr.cls.atoll.motu.web.dal.config.stdname.xml.model.StandardNames;
import fr.cls.atoll.motu.web.dal.config.version.DALVersionManager;
import fr.cls.atoll.motu.web.dal.config.version.IDALVersionManager;
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

    public DALConfigManager() {
        dalVersionManager = new DALVersionManager();
    }

    /** {@inheritDoc} */
    @Override
    public void init() throws MotuException {
        try {
            initMotuConfig();
        } catch (FileNotFoundException e) {
            throw new MotuException(ErrorType.MOTU_CONFIG, "Error while initializing Motu configuration: ", e);
        }

        initStdNames();
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
    private void initStdNames() throws MotuException {
        StandardNames sn = new StdNameReader().getStdNameEquiv();
        if (sn != null) {
            standardNameList = sn.getStandardName();
        } else {
            LOGGER.warn("No standard names loaded from configuration folder");
        }
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

    private void initMotuConfig() throws MotuException, FileNotFoundException {
        File fMotuConfig = new File(getMotuConfigurationFolderPath(), "motuConfiguration.xml");
        InputStream in = null;
        if (fMotuConfig.exists()) {
            in = new FileInputStream(fMotuConfig);
        } else {
            in = DALConfigManager.class.getClassLoader().getResourceAsStream("motuConfiguration.xml");
        }

        try {
            JAXBContext jc = JAXBContext.newInstance(MotuConfig.class.getPackage().getName());
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            motuConfig = (MotuConfig) unmarshaller.unmarshal(in);
            motuConfig.setExtractionPath(PropertiesUtilities.replaceSystemVariable(motuConfig.getExtractionPath()));
            motuConfig.setDownloadHttpUrl(PropertiesUtilities.replaceSystemVariable(motuConfig.getDownloadHttpUrl()));
        } catch (Exception e) {
            throw new MotuException(ErrorType.SYSTEM, "Error in getMotuConfigInstance", e);
        }

        if (motuConfig == null) {
            throw new MotuException(ErrorType.MOTU_CONFIG, "Unable to load Motu configuration (motuConfig is null)");
        }

        ObjectFactory motuConfigObjectFactory = new ObjectFactory();
        MotuConfig blankMotuConfig = motuConfigObjectFactory.createMotuConfig();
        if (motuConfig.getUpdateCachePassPhrase().equals(blankMotuConfig.getUpdateCachePassPhrase())) {
            LOGGER.error("Security breach : The pass phrase for the update of the cache is still set to the default value.\n"
                    + "To improve the security of the server please change this pass phrase into the motuConfiguration.xml file.");
        }

        try {
            in.close();
        } catch (IOException io) {
            // Do nothing
        }
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
}
