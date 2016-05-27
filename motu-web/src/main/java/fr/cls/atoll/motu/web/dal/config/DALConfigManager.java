package fr.cls.atoll.motu.web.dal.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import fr.cls.atoll.motu.library.misc.utils.PropertiesUtilities;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
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
public class DALConfigManager implements IDALConfigManager {

    /** Application configuration. */
    private static MotuConfig motuConfig = null;

    /** {@inheritDoc} */
    @Override
    public void init() throws MotuException {
        try {
            initMotuConfig();
        } catch (FileNotFoundException e) {
            throw new MotuException("Error while initializing Motu configuration: ", e);
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

    private String getMotuConfigurationFolderPath() {
        return System.getProperty("motu-config-dir", null);
    }

    private void initMotuConfig() throws MotuException, FileNotFoundException {
        InputStream in = new FileInputStream(new File(getMotuConfigurationFolderPath(), "motuConfiguration.xml"));

        try {
            JAXBContext jc = JAXBContext.newInstance(MotuConfig.class.getPackage().getName());
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            motuConfig = (MotuConfig) unmarshaller.unmarshal(in);
            motuConfig.setExtractionPath(PropertiesUtilities.replaceSystemVariable(motuConfig.getExtractionPath()));
            motuConfig.setDownloadHttpUrl(PropertiesUtilities.replaceSystemVariable(motuConfig.getDownloadHttpUrl()));
            motuConfig.setHttpDocumentRoot(PropertiesUtilities.replaceSystemVariable(motuConfig.getHttpDocumentRoot()));
        } catch (Exception e) {
            throw new MotuException("Error in getMotuConfigInstance", e);
        }

        if (motuConfig == null) {
            throw new MotuException("Unable to load Motu configuration (motuConfig is null)");
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
}
