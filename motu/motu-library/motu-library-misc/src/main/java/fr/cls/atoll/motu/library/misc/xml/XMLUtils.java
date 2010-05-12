package fr.cls.atoll.motu.library.misc.xml;

import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuExceptionBase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

import org.dom4j.Element;
import org.dom4j.jaxb.JAXBWriter;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
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
        return XMLUtils.validateXML(inSchemas, inXml, schemaLanguage);
        // // parse an XML document into a DOM tree
        // Document document;
        // // create a Validator instance, which can be used to validate an instance document
        // Validator validator;
        // XMLErrorHandler errorHandler = new XMLErrorHandler();
        //
        // try {
        //
        // DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        // documentBuilderFactory.setNamespaceAware(true); // Must enable namespace processing!!!!!
        // try {
        // documentBuilderFactory.setXIncludeAware(true);
        // } catch (Exception e) {
        // // Do Nothing
        // }
        // //documentBuilderFactory.setExpandEntityReferences(true);
        //            
        // DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        // // document = documentBuilder.parse(new File(xmlUrl.toURI()));
        // document = documentBuilder.parse(inXml);
        //
        // // create a SchemaFactory capable of understanding WXS schemas
        // SchemaFactory schemaFactory = SchemaFactory.newInstance(schemaLanguage);
        // schemaFactory.setErrorHandler(errorHandler);
        // // load a WXS schema, represented by a Schema instance
        // Source schemaFile = new StreamSource(inSchema);
        // Schema schema = schemaFactory.newSchema(schemaFile);
        //
        // validator = schema.newValidator();
        // validator.setErrorHandler(errorHandler);
        // validator.validate(new DOMSource(document));
        //
        // } catch (Exception e) {
        // throw new MotuException(e);
        // // instance document is invalid!
        // }
        //
        // return errorHandler;
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
            // documentBuilderFactory.setExpandEntityReferences(true);

            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            // document = documentBuilder.parse(new File(xmlUrl.toURI()));
            documentBuilder.setErrorHandler(errorHandler);
            document = documentBuilder.parse(inXml);

            // create a SchemaFactory capable of understanding WXS schemas
            SchemaFactory schemaFactory = SchemaFactory.newInstance(schemaLanguage);
            schemaFactory.setErrorHandler(errorHandler);

            // load a WXS schema, represented by a Schema instance

            Source[] schemaFiles = new Source[inSchemas.length];

            // InputStream inShema = null;
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
            // documentBuilderFactory.setExpandEntityReferences(true);

            documentBuilderFactory.setAttribute(XMLUtils.JAXP_SCHEMA_LANGUAGE, schemaLanguage);
            // final String[] srcSchemas =
            // {"http://schemas.opengis.net/iso/19139/20060504/srv/serviceMetadata.xsd",
            // };

            // final String[] srcSchemas =
            // {"http://opendap.aviso.oceanobs.com/data/ISO_19139/srv/serviceMetadata.xsd",
            // "http://opendap.aviso.oceanobs.com/data/ISO_19139/gco/gco.xsd", };

            // C:\Documents and Settings\dearith\Mes documents\Atoll\SchemaIso\gml
            // final String[] srcSchemas =
            // {"C:/Documents and Settings/users documents/Atoll/SchemaIso/srv/serviceMetadata.xsd",
            // };
            // final String[] srcSchemas = {"schema/iso/srv/serviceMetadata.xsd",
            // };

            documentBuilderFactory.setAttribute(XMLUtils.JAXP_SCHEMA_SOURCE, inSchemas);
            // URL url = Organizer.findResource("schema/iso/srv/srv.xsd");
            // URL url = Organizer.findResource("iso/19139/20070417/srv/serviceMetadata.xsd");
            // documentBuilderFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource",
            // url.toString());
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
            // final String[] srcSchemas =
            // {"http://schemas.opengis.net/iso/19139/20060504/srv/serviceMetadata.xsd",
            // };

            // final String[] srcSchemas =
            // {"http://opendap.aviso.oceanobs.com/data/ISO_19139/srv/serviceMetadata.xsd",
            // "http://opendap.aviso.oceanobs.com/data/ISO_19139/gco/gco.xsd", };

            // C:\Documents and Settings\dearith\Mes documents\Atoll\SchemaIso\gml
            // final String[] srcSchemas =
            // {"C:/Documents and Settings/us/userocuments/Atoll/SchemaIso/srv/serviceMetadata.xsd",
            // };
            // final String[] srcSchemas = {"schema/iso/srv/serviceMetadata.xsd",
            // };

            documentBuilderFactory.setAttribute(XMLUtils.JAXP_SCHEMA_SOURCE, inSchemas);
            // URL url = Organizer.findResource("schema/iso/srv/srv.xsd");
            // URL url = Organizer.findResource("iso/19139/20070417/srv/serviceMetadata.xsd");
            // documentBuilderFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource",
            // url.toString());
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
     * Dom4j to intput stream.
     * 
     * @param document the document
     * @param contextPath the context path
     * 
     * @return the input stream
     * 
     * @throws MotuExceptionBase the motu exception base
     */
    public static InputStream dom4jToIntputStream(org.dom4j.Document document, String contextPath) throws MotuExceptionBase {
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            Element root = document.getRootElement();
            JAXBWriter jaxbWriter = new JAXBWriter(contextPath);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            jaxbWriter.setOutput(byteArrayOutputStream);

            jaxbWriter.startDocument();
            jaxbWriter.writeElement(root);
            jaxbWriter.endDocument();

            byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            throw new MotuException("ERROR in XMLUtils#dom4jToIntputStream", e);
        } catch (SAXException e) {
            throw new MotuException("ERROR in XMLUtils#dom4jToIntputStream", e);
        }
        return byteArrayInputStream;

    }

}
