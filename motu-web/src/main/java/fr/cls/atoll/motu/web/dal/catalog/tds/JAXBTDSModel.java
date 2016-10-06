package fr.cls.atoll.motu.web.dal.catalog.tds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.tds.ncss.model.Catalog;

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

    /**
     * Searches objects from a jaxbElement object list according to a specific class .
     * 
     * @param listObject list in which one searches
     * @param classObject class to search
     * 
     * @return a list that contains object corresponding to classObject parameter (can be empty)
     */
    public static List<Object> findJaxbElement(List<Object> listObject, Class<?> classObject) {

        if (listObject == null) {
            return null;
        }

        List<Object> listObjectFound = new ArrayList<Object>();

        for (Object elt : listObject) {
            if (elt == null) {
                continue;
            }

            if (classObject.isInstance(elt)) {
                listObjectFound.add(elt);
            }

            if (!(elt instanceof JAXBElement)) {
                continue;
            }

            JAXBElement<?> jabxElement = (JAXBElement<?>) elt;

            Object objectElt = jabxElement.getValue();

            if (classObject.isInstance(objectElt)) {
                listObjectFound.add(objectElt);
            }
        }

        return listObjectFound;

    }

    public static List<Object> findJaxbElementUsingJXPath(Object object, String xPath) {

        List<Object> listObjectFound = new ArrayList<Object>();

        JXPathContext context = JXPathContext.newContext(object);
        context.setLenient(true);
        Iterator<?> it = context.iterate(xPath);
        while (it.hasNext()) {
            listObjectFound.add(it.next());
        }
        return listObjectFound;
    }

    /**
     * Search object from a jaxbElement object list according to a specific tag name.
     * 
     * @param listObject list in which one searches
     * @param tagName tag name to search (ignore case)
     * 
     * @return a list that contains object corresponding to tagName parameter (can be empty)
     */
    public static List<Object> findJaxbElement(List<Object> listObject, String tagName) {

        List<Object> listObjectFound = new ArrayList<Object>();

        for (Object elt : listObject) {
            if (elt == null) {
                continue;
            }
            if (!(elt instanceof JAXBElement)) {
                continue;
            }

            JAXBElement<?> jabxElement = (JAXBElement<?>) elt;

            if (!jabxElement.getName().getLocalPart().equalsIgnoreCase(tagName)) {
                continue;
            }

            listObjectFound.add(jabxElement.getValue());
        }

        return listObjectFound;

    }

    /**
     * Find jaxb element.
     * 
     * @param tagName the tag name
     * @param listJaxbElement the list jaxb element
     * 
     * @return the list< object>
     */
    public static List<Object> findJaxbElement(String tagName, List<JAXBElement<?>> listJaxbElement) {

        List<Object> listObjectFound = new ArrayList<Object>();

        for (Object elt : listJaxbElement) {
            if (elt == null) {
                continue;
            }
            if (!(elt instanceof JAXBElement)) {
                continue;
            }

            JAXBElement<?> jabxElement = (JAXBElement<?>) elt;

            if (!jabxElement.getName().getLocalPart().equalsIgnoreCase(tagName)) {
                continue;
            }

            listObjectFound.add(jabxElement.getValue());
        }

        return listObjectFound;

    }

}