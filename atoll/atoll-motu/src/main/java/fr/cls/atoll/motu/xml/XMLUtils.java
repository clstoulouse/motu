package fr.cls.atoll.motu.xml;

import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;

import fr.cls.atoll.motu.exception.MotuException;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2008. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-02-20 13:00:26 $
 */
public class XMLUtils {

    /**
     * The Constructor.
     */
    protected XMLUtils() {

    }

    /**
     * Validate XML.
     * 
     * @param inXml the in xml
     * @param inSchema the in schema
     * 
     * @return the XML error handler
     * 
     * @throws MotuException the motu exception
     */
    public static XMLErrorHandler validateXML(InputStream inSchema, InputStream inXml) throws MotuException {
        // parse an XML document into a DOM tree
        Document document;
        // create a Validator instance, which can be used to validate an instance document
        Validator validator;
        XMLErrorHandler errorHandler = new XMLErrorHandler();

        try {

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true); // Must enable namespace processing!!!!!

            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            // document = documentBuilder.parse(new File(xmlUrl.toURI()));
            document = documentBuilder.parse(inXml);

            // create a SchemaFactory capable of understanding WXS schemas
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // load a WXS schema, represented by a Schema instance
            Source schemaFile = new StreamSource(inSchema);
            Schema schema = schemaFactory.newSchema(schemaFile);

            validator = schema.newValidator();
            validator.setErrorHandler(errorHandler);
            validator.validate(new DOMSource(document));

        } catch (Exception e) {
            throw new MotuException(e);
            // instance document is invalid!
        }

        return errorHandler;
    }

}
