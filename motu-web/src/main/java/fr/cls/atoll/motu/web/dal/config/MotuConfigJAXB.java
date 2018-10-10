package fr.cls.atoll.motu.web.dal.config;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.config.xml.model.MotuConfig;

public class MotuConfigJAXB {

    public static MotuConfigJAXB s_instance = null;
    private static final Logger LOGGER = LogManager.getLogger();

    private Unmarshaller unmarshaller;

    public static MotuConfigJAXB getInstance() {
        if (s_instance == null) {
            s_instance = new MotuConfigJAXB();
        }
        return s_instance;
    }

    /**
     * Inits the JAXB motu msg.
     * 
     * @throws JAXBException
     * 
     * @throws MotuException the motu exception
     */
    public void init() throws JAXBException {
        if (getUnmarshaller() == null) {
            try {
                JAXBContext jc = JAXBContext.newInstance(MotuConfig.class.getPackage().getName());
                setUnmarshaller(jc.createUnmarshaller());
            } catch (JAXBException e) {
                LOGGER.error("Error while initializing MotuConfigJAXB", e);
            }
        }
    }

    private MotuConfigJAXB() {
    }

    public Unmarshaller getUnmarshaller() {
        return unmarshaller;
    }

    private void setUnmarshaller(Unmarshaller u) {
        unmarshaller = u;
    }
}
