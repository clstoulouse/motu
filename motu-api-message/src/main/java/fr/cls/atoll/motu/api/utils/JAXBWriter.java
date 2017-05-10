package fr.cls.atoll.motu.api.utils;

import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.MotuMsgConstant;

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
public class JAXBWriter {

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger();

    private static JAXBWriter s_instance;

    public static JAXBWriter getInstance() {
        if (s_instance == null) {
            s_instance = new JAXBWriter();
        }
        return s_instance;
    }

    /** The jaxb context motu msg. */
    private JAXBContext jaxbContextMotuMsg = null;

    /** The marshaller motu msg. */
    private Marshaller marshallerMotuMsg = null;

    private JAXBWriter() {

    }

    /**
     * Inits the JAXB motu msg.
     * 
     * @throws JAXBException
     * 
     * @throws MotuException the motu exception
     */
    public void init() throws JAXBException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("initJAXBMotuMsg() - entering");
        }
        if (jaxbContextMotuMsg != null) {
            return;
        }

        jaxbContextMotuMsg = JAXBContext.newInstance(MotuMsgConstant.MOTU_MSG_SCHEMA_PACK_NAME);
        marshallerMotuMsg = jaxbContextMotuMsg.createMarshaller();
        marshallerMotuMsg.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshallerMotuMsg.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("initJAXBMotuMsg() - exiting");
        }
    }

    /**
     * .
     * 
     * @param statusModeResponse
     * @param writer
     * @throws JAXBException
     */
    public synchronized void write(Object jaxbObject, Writer writer) throws JAXBException {
        marshallerMotuMsg.marshal(jaxbObject, writer);
    }

}
