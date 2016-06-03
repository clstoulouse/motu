package fr.cls.atoll.motu.web.dal.catalog.tds;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.tds.model.Catalog;

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
public class JAXBTDSModel {

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger();

    private static JAXBTDSModel s_instance;

    public static JAXBTDSModel getInstance() {
        if (s_instance == null) {
            s_instance = new JAXBTDSModel();
        }
        return s_instance;
    }

    private Unmarshaller unmarshallerTdsModel;

    private JAXBTDSModel() {

    }

    /**
     * Inits the JAXB motu msg.
     * 
     * @throws JAXBException
     * 
     * @throws MotuException the motu exception
     */
    public void init() throws JAXBException {
        if (unmarshallerTdsModel != null) {
            return;
        }

        JAXBContext jaxbContextTDSModel = JAXBContext.newInstance(Catalog.class.getPackage().getName());
        unmarshallerTdsModel = jaxbContextTDSModel.createUnmarshaller();
    }

    /**
     * Valeur de unmarshallerTdsModel.
     * 
     * @return la valeur.
     */
    public Unmarshaller getUnmarshallerTdsModel() {
        return unmarshallerTdsModel;
    }

}