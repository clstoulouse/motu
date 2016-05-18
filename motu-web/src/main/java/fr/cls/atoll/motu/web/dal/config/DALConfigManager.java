package fr.cls.atoll.motu.web.dal.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import fr.cls.atoll.motu.library.misc.configuration.MotuConfig;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.library.misc.utils.PropertiesUtilities;
import fr.cls.atoll.motu.library.misc.xml.XMLErrorHandler;
import fr.cls.atoll.motu.library.misc.xml.XMLUtils;

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
        initMotuConfig();
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

    private List<String> loadConfig(boolean validate) throws MotuException {
        InputStream inXml = Organizer.getMotuConfigXml();
        if (inXml == null) {
            throw new MotuException(
                    String.format("ERROR in Organiser.validateMotuConfig - Motu configuration xml ('%s') not found:",
                                  Organizer.getMotuConfigXmlName()));
        }

        List<String> errorsList = null;
        if (validate) {
            InputStream inSchema = Organizer.getMotuConfigSchema();
            if (inSchema == null) {
                throw new MotuException(
                        String.format("ERROR in Organiser.validateMotuConfig - Motu configuration schema ('%s') not found:",
                                      Organizer.getMotuConfigSchemaName()));
            }
            XMLErrorHandler errorHandler = XMLUtils.validateXML(inSchema, inXml);
            if (errorHandler == null) {
                throw new MotuException("ERROR in Organiser.validateMotuConfig - Motu configuration schema : XMLErrorHandler is null");
            }
            errorsList = errorHandler.getErrors();
        }
        return errorsList;
    }

    private void initMotuConfig() throws MotuException {
        List<String> errors = loadConfig(true);
        if (errors.size() > 0) {
            StringBuffer stringBuffer = new StringBuffer();
            for (String str : errors) {
                stringBuffer.append(str);
                stringBuffer.append("\n");
            }
            throw new MotuException(
                    String.format("ERROR - Motu configuration file '%s' is not valid - See errors below:\n%s",
                                  Organizer.getMotuConfigXmlName(),
                                  stringBuffer.toString()));
        } else {
            InputStream in = ClassLoader.getSystemResourceAsStream("motuConfiguration.xml");

            try {
                JAXBContext jc = JAXBContext.newInstance("fr.cls.atoll.motu.library.misc.configuration");
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

    }

    @Override
    public MotuConfig getMotuConfig() {
        return motuConfig;
    }
}
