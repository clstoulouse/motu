package fr.cls.atoll.motu.processor.wps.framework;

import java.io.InputStream;

import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.processor.opengis.wps100.ObjectFactory;
import fr.cls.atoll.motu.processor.opengis.wps100.ProcessDescriptions;
import fr.cls.atoll.motu.processor.wps.MotuWPSProcess;

/**
 * <br><br>Copyright : Copyright (c) 2009.
 * <br><br>Société : CLS (Collecte Localisation Satellites)
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-08-06 14:28:57 $
 */
public class WPSFactory {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(WPSFactory.class);
    
    private static JAXBContext jaxbContextWPS = null;
    private static Marshaller marshallerWPS = null;
    private static ObjectFactory objectFactoryWPS = null;

    protected WPSInfo wpsInfo = null;

    public WPSFactory(String url) throws MotuException {   
        
        WPSFactory.initJAXBWPS();
        this.wpsInfo = new WPSInfo(url);

    }
    

    private static void initJAXBWPS() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initJAXBWPS() - entering");
        }
        if (WPSFactory.jaxbContextWPS != null) {
            return;
        }

        try {
            WPSFactory.jaxbContextWPS = JAXBContext.newInstance(MotuWPSProcess.WPS100_SHEMA_PACK_NAME);
            WPSFactory.marshallerWPS = WPSFactory.jaxbContextWPS.createMarshaller();
            WPSFactory.marshallerWPS.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        } catch (JAXBException e) {
            LOG.error("initJAXBWPS()", e);
            throw new MotuException("Error in WPSInfo - initJAXBWPS ", e);

        }
        
        objectFactoryWPS = new ObjectFactory();

        if (LOG.isDebugEnabled()) {
            LOG.debug("initJAXBWPS() - exiting");
        }
    }


}
