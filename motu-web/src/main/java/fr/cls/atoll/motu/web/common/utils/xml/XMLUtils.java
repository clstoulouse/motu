/* 
 * Motu, a high efficient, robust and Standard compliant Web Server for Geographic
 * Data Dissemination.
 *
 * http://cls-motu.sourceforge.net/
 *
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites) - 
 * http://www.cls.fr - and  Contributors
 *
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
package fr.cls.atoll.motu.web.common.utils.xml;

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

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.dom4j.jaxb.JAXBWriter;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuExceptionBase;

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

            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            documentBuilder.setErrorHandler(errorHandler);
            document = documentBuilder.parse(inXml);

            // create a SchemaFactory capable of understanding WXS schemas
            SchemaFactory schemaFactory = SchemaFactory.newInstance(schemaLanguage);
            schemaFactory.setErrorHandler(errorHandler);

            // load a WXS schema, represented by a Schema instance

            Source[] schemaFiles = new Source[inSchemas.length];

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

            documentBuilderFactory.setAttribute(XMLUtils.JAXP_SCHEMA_LANGUAGE, schemaLanguage);

            documentBuilderFactory.setAttribute(XMLUtils.JAXP_SCHEMA_SOURCE, inSchemas);
            documentBuilderFactory.setValidating(true);

            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
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

            documentBuilderFactory.setAttribute(XMLUtils.JAXP_SCHEMA_SOURCE, inSchemas);
            documentBuilderFactory.setValidating(true);

            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
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
            throw new MotuExceptionBase("ERROR in XMLUtils#dom4jToIntputStream", e);
        } catch (SAXException e) {
            throw new MotuExceptionBase("ERROR in XMLUtils#dom4jToIntputStream", e);
        }
        return byteArrayInputStream;

    }

    /**
     * return true if the String passed in is something like XML
     *
     *
     * @param inString a string that might be XML
     * @return true of the string is XML, false otherwise
     */
    public static boolean isXML(String xml) {
        if (StringUtils.isBlank(xml)) {
            return false;
        }

        boolean isXml = true;
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        final DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
        } catch (Exception e) {
            isXml = false;
        }
        return isXml;
    }

}
