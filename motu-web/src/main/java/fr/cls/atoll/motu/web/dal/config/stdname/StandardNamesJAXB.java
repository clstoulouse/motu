package fr.cls.atoll.motu.web.dal.config.stdname;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.config.stdname.xml.model.StandardNames;

public class StandardNamesJAXB {

    public static StandardNamesJAXB s_instance = null;
    private static final Logger LOGGER = LogManager.getLogger();

    private Unmarshaller unmarshaller;

    public static StandardNamesJAXB getInstance() {
        if (s_instance == null) {
            s_instance = new StandardNamesJAXB();
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
                JAXBContext jc = JAXBContext.newInstance(StandardNames.class.getPackage().getName());
                setUnmarshaller(jc.createUnmarshaller());
            } catch (JAXBException e) {
                LOGGER.error("Error while initializing StandardNamesJAXB", e);
            }
        }
    }

    private StandardNamesJAXB() {
    }

    public Unmarshaller getUnmarshaller() {
        return unmarshaller;
    }

    private void setUnmarshaller(Unmarshaller u) {
        unmarshaller = u;
    }
}
