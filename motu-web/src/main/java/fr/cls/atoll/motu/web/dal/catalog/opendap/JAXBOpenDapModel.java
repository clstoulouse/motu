package fr.cls.atoll.motu.web.dal.catalog.opendap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.web.dal.opendap.model.Catalog;

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
public class JAXBOpenDapModel {

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger();

    private static JAXBOpenDapModel s_instance;

    public static JAXBOpenDapModel getInstance() {
        if (s_instance == null) {
            s_instance = new JAXBOpenDapModel();
        }
        return s_instance;
    }

    private Unmarshaller unmarshallerOpendapConfig;

    private JAXBOpenDapModel() {

    }

    /**
     * Inits the JAXB motu msg.
     * 
     * @throws JAXBException
     * 
     * @throws MotuException the motu exception
     */
    public void init() throws JAXBException {
        if (unmarshallerOpendapConfig != null) {
            return;
        }

        JAXBContext jaxbContextOpenDapModel = JAXBContext.newInstance(Catalog.class.getPackage().getName());
        unmarshallerOpendapConfig = jaxbContextOpenDapModel.createUnmarshaller();
    }

    /**
     * Valeur de unmarshallerTdsModel.
     * 
     * @return la valeur.
     */
    public Unmarshaller getUnmarshallerOpenDapModel() {
        return unmarshallerOpendapConfig;
    }

}
