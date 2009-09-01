package fr.cls.atoll.motu.library.xml;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

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

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.intfce.Organizer;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2008. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.5 $ - $Date: 2009-09-01 14:24:21 $
 */
public class XMLUtils {

     /** The Constant JAXP_SCHEMA_LANGUAGE. */
     static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    
     /** The Constant JAXP_SCHEMA_SOURCE. */
     static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";     
    
    /**
     * The Constructor.
     */
    protected XMLUtils() {

    }
    
    /**
     * Validate xml.
     * 
     * @param inSchema the in schema
     * @param inXml the in xml
     * 
     * @return the xML error handler
     * 
     * @throws MotuException the motu exception
     */
    public static XMLErrorHandler validateXML(InputStream inSchema, InputStream inXml) throws MotuException {
        
        return XMLUtils.validateXML(inSchema, inXml, XMLConstants.W3C_XML_SCHEMA_NS_URI);
    }
    
    /**
     * Validate xml.
     * 
     * @param inSchemas the in schemas
     * @param inXml the in xml
     * 
     * @return the xML error handler
     * 
     * @throws MotuException the motu exception
     */
    public static XMLErrorHandler validateXML(InputStream[] inSchemas, InputStream inXml) throws MotuException {
        
        return XMLUtils.validateXML(inSchemas, inXml, XMLConstants.W3C_XML_SCHEMA_NS_URI);
    }
    
    /**
     * Validate xml.
     * 
     * @param inSchemas the in schemas
     * @param inXml the in xml
     * 
     * @return the xML error handler
     * 
     * @throws MotuException the motu exception
     */
    public static XMLErrorHandler validateXML(String[] inSchemas, InputStream inXml) throws MotuException {
        
        return XMLUtils.validateXML(inSchemas, inXml, XMLConstants.W3C_XML_SCHEMA_NS_URI);
    }
    
    /**
     * Validate xml.
     * 
     * @param inSchemas the in schemas
     * @param inXml the in xml
     * 
     * @return the xML error handler
     * 
     * @throws MotuException the motu exception
     */
    public static XMLErrorHandler validateXML(String[] inSchemas, String inXml) throws MotuException {
        
        return XMLUtils.validateXML(inSchemas, inXml, XMLConstants.W3C_XML_SCHEMA_NS_URI);
    }
    
    /**
     * Validate XML.
     * 
     * @param inXml the in xml
     * @param inSchema the in schema
     * @param schemaLanguage the schema language
     * 
     * @return the XML error handler
     * 
     * @throws MotuException the motu exception
     */
    public static XMLErrorHandler validateXML(InputStream inSchema, InputStream inXml, String schemaLanguage) throws MotuException {
        InputStream[] inSchemas = new InputStream[1];
        inSchemas[0] = inSchema;
        return  XMLUtils.validateXML(inSchemas, inXml, schemaLanguage);
//        // parse an XML document into a DOM tree
//        Document document;
//        // create a Validator instance, which can be used to validate an instance document
//        Validator validator;
//        XMLErrorHandler errorHandler = new XMLErrorHandler();
//
//        try {
//
//            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
//            documentBuilderFactory.setNamespaceAware(true); // Must enable namespace processing!!!!!
//            try {
//                documentBuilderFactory.setXIncludeAware(true);
//            } catch (Exception e) {
//                // Do Nothing
//            }
//            //documentBuilderFactory.setExpandEntityReferences(true);
//            
//            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
//            // document = documentBuilder.parse(new File(xmlUrl.toURI()));
//            document = documentBuilder.parse(inXml);
//
//            // create a SchemaFactory capable of understanding WXS schemas
//            SchemaFactory schemaFactory = SchemaFactory.newInstance(schemaLanguage);
//            schemaFactory.setErrorHandler(errorHandler);
//            // load a WXS schema, represented by a Schema instance
//            Source schemaFile = new StreamSource(inSchema);
//            Schema schema = schemaFactory.newSchema(schemaFile);
//
//            validator = schema.newValidator();
//            validator.setErrorHandler(errorHandler);
//            validator.validate(new DOMSource(document));
//
//        } catch (Exception e) {
//            throw new MotuException(e);
//            // instance document is invalid!
//        }
//
//        return errorHandler;
    }
    
    /**
     * Validate xml.
     * 
     * @param inSchemas the in schemas
     * @param inXml the in xml
     * @param schemaLanguage the schema language
     * 
     * @return the xML error handler
     * 
     * @throws MotuException the motu exception
     */
    public static XMLErrorHandler validateXML(InputStream[] inSchemas, InputStream inXml, String schemaLanguage) throws MotuException {
        // parse an XML document into a DOM tree
        Document document;
        // create a Validator instance, which can be used to validate an instance document
        Validator validator;
        XMLErrorHandler errorHandler = new XMLErrorHandler();

        try {

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true); // Must enable namespace processing!!!!!
            try {
                documentBuilderFactory.setXIncludeAware(true);
            } catch (Exception e) {
                // Do Nothing
            }
            //documentBuilderFactory.setExpandEntityReferences(true);

            
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            // document = documentBuilder.parse(new File(xmlUrl.toURI()));
            documentBuilder.setErrorHandler(errorHandler);
            document = documentBuilder.parse(inXml);

            // create a SchemaFactory capable of understanding WXS schemas
            SchemaFactory schemaFactory = SchemaFactory.newInstance(schemaLanguage);
            schemaFactory.setErrorHandler(errorHandler);
            
            
            // load a WXS schema, represented by a Schema instance
            
            Source[] schemaFiles = new Source[inSchemas.length];
            
            //InputStream inShema = null;
            int i = 0;
            for (InputStream inSchema : inSchemas) {
                schemaFiles[i] = new StreamSource(inSchema);
                i++;
            }

            Schema schema = schemaFactory.newSchema(schemaFiles);

            validator = schema.newValidator();
            validator.setErrorHandler(errorHandler);
            validator.validate(new DOMSource(document));

        } catch (Exception e) {
            throw new MotuException(e);
            // instance document is invalid!
        }

        return errorHandler;
    }
        
    
    /**
     * Validate xml.
     * 
     * @param inSchemas the in schemas
     * @param inXml the in xml
     * @param schemaLanguage the schema language
     * 
     * @return the xML error handler
     * 
     * @throws MotuException the motu exception
     */
    public static XMLErrorHandler validateXML(String[] inSchemas, InputStream inXml, String schemaLanguage) throws MotuException {

        XMLErrorHandler errorHandler = new XMLErrorHandler();

        try {

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true); // Must enable namespace processing!!!!!
            try {
                documentBuilderFactory.setXIncludeAware(true);
            } catch (Exception e) {
                // Do Nothing
            }
            //documentBuilderFactory.setExpandEntityReferences(true);

            documentBuilderFactory.setAttribute(XMLUtils.JAXP_SCHEMA_LANGUAGE, schemaLanguage);
//            final String[] srcSchemas = {"http://schemas.opengis.net/iso/19139/20060504/srv/serviceMetadata.xsd",
//                     };
            
//            final String[] srcSchemas = {"http://opendap.aviso.oceanobs.com/data/ISO_19139/srv/serviceMetadata.xsd",
//                    "http://opendap.aviso.oceanobs.com/data/ISO_19139/gco/gco.xsd", };
            
//            C:\Documents and Settings\dearith\Mes documents\Atoll\SchemaIso\gml
//            final String[] srcSchemas = {"C:/Documents and Settings/dearith/Mes documents/Atoll/SchemaIso/srv/serviceMetadata.xsd",
//            };
//            final String[] srcSchemas = {"schema/iso/srv/serviceMetadata.xsd",
//            };

            documentBuilderFactory.setAttribute(XMLUtils.JAXP_SCHEMA_SOURCE, inSchemas);
            //URL url = Organizer.findResource("schema/iso/srv/srv.xsd");
            //URL url = Organizer.findResource("iso/19139/20070417/srv/serviceMetadata.xsd");
            //documentBuilderFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", url.toString());
            documentBuilderFactory.setValidating(true);
            
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            // document = documentBuilder.parse(new File(xmlUrl.toURI()));
            documentBuilder.setErrorHandler(errorHandler);
            documentBuilder.parse(inXml);


        } catch (Exception e) {
            throw new MotuException(e);
            // instance document is invalid!
        }

        return errorHandler;
    }
    
    /**
     * Validate xml.
     * 
     * @param inSchemas the in schemas
     * @param inXml the in xml
     * @param schemaLanguage the schema language
     * 
     * @return the xML error handler
     * 
     * @throws MotuException the motu exception
     */
    public static XMLErrorHandler validateXML(String[] inSchemas, String inXml, String schemaLanguage) throws MotuException {

        XMLErrorHandler errorHandler = new XMLErrorHandler();
        try {

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true); // Must enable namespace processing!!!!!
            try {
                documentBuilderFactory.setXIncludeAware(true);
            } catch (Exception e) {
                // Do Nothing
            }
            documentBuilderFactory.setExpandEntityReferences(true);

            documentBuilderFactory.setAttribute(XMLUtils.JAXP_SCHEMA_LANGUAGE, schemaLanguage);
//            final String[] srcSchemas = {"http://schemas.opengis.net/iso/19139/20060504/srv/serviceMetadata.xsd",
//                     };
            
//            final String[] srcSchemas = {"http://opendap.aviso.oceanobs.com/data/ISO_19139/srv/serviceMetadata.xsd",
//                    "http://opendap.aviso.oceanobs.com/data/ISO_19139/gco/gco.xsd", };
            
//            C:\Documents and Settings\dearith\Mes documents\Atoll\SchemaIso\gml
//            final String[] srcSchemas = {"C:/Documents and Settings/dearith/Mes documents/Atoll/SchemaIso/srv/serviceMetadata.xsd",
//            };
//            final String[] srcSchemas = {"schema/iso/srv/serviceMetadata.xsd",
//            };

            documentBuilderFactory.setAttribute(XMLUtils.JAXP_SCHEMA_SOURCE, inSchemas);
            //URL url = Organizer.findResource("schema/iso/srv/srv.xsd");
            //URL url = Organizer.findResource("iso/19139/20070417/srv/serviceMetadata.xsd");
            //documentBuilderFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", url.toString());
            documentBuilderFactory.setValidating(true);
            
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            // document = documentBuilder.parse(new File(xmlUrl.toURI()));
            documentBuilder.setErrorHandler(errorHandler);
            documentBuilder.parse(inXml);


        } catch (Exception e) {
            throw new MotuException(e);
            // instance document is invalid!
        }

        return errorHandler;
    }
    
}
