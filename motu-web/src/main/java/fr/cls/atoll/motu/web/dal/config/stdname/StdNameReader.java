package fr.cls.atoll.motu.web.dal.config.stdname;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.config.stdname.xml.model.StandardNames;

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
public class StdNameReader {

    private static final Logger LOGGER = LogManager.getLogger();

    public StdNameReader() {
        try {
            StandardNamesJAXB.getInstance().init();
        } catch (JAXBException e) {
            LOGGER.error("", e);
        }
    }

    public StandardNames getStdNameEquiv() throws MotuException {
        StandardNames stdNameEquiv = null;
        String fileName = "standardNames.xml";
        InputStream in = null;
        try {
            in = new FileInputStream(new File(BLLManager.getInstance().getConfigManager().getMotuConfigurationFolderPath(), fileName));
            if (in != null) {
                stdNameEquiv = (StandardNames) StandardNamesJAXB.getInstance().getUnmarshaller().unmarshal(in);
            }
        } catch (Exception e) {
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                LOGGER.error("Error while loading file from config folder: " + fileName, e);
            }
        }

        return stdNameEquiv;
    }
}
