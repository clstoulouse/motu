package fr.cls.atoll.motu.processor.iso19139;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.isotc211.iso19139.d_2006_05_04.gmd.AbstractMDIdentificationType;
import org.isotc211.iso19139.d_2006_05_04.gmd.CIOnlineResourcePropertyType;
import org.isotc211.iso19139.d_2006_05_04.gmd.MDIdentificationPropertyType;
import org.isotc211.iso19139.d_2006_05_04.gmd.MDMetadataType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVOperationMetadataPropertyType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVOperationMetadataType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVParameterPropertyType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVParameterType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVServiceIdentificationType;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.DirectedNeighborIndex;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DirectedSubgraph;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.exception.MotuMarshallException;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.atoll.motu.library.xml.XMLErrorHandler;
import fr.cls.atoll.motu.library.xml.XMLUtils;
import fr.cls.atoll.motu.processor.jgraht.OperationRelationshipEdge;
import fr.cls.atoll.motu.processor.wps.framework.WPSUtils;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.14 $ - $Date: 2009-10-28 15:48:01 $
 */
public class ServiceMetadata {

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(ServiceMetadata.class);

    /** The Constant ISO19139_SHEMA_PACK_NAME. */
    public static final String ISO19139_SHEMA_PACK_NAME = "org.isotc211.iso19139.d_2006_05_04.gmd";

    /**
     * Instantiates a new service metadata.
     * 
     * @throws MotuException the motu exception
     */
    public ServiceMetadata() throws MotuException {

        ServiceMetadata.initJAXBIso19139();
    }

    /** The jaxb context iso19139. */
    private static JAXBContext jaxbContextIso19139 = null;

    /** The marshaller iso19139. */
    private static Marshaller marshallerIso19139 = null;

    /** The unmarshaller iso19139. */
    private static Unmarshaller unmarshallerIso19139 = null;

    // private static ObjectFactory objectFactoryIso19139 = null;

    /**
     * Inits the jaxb iso19139.
     * 
     * @throws MotuException the motu exception
     */
    private static void initJAXBIso19139() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initJAXBIso19139() - entering");
        }
        if (ServiceMetadata.jaxbContextIso19139 != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("initJAXBIso19139() - exiting");
            }
            return;
        }

        try {
            ServiceMetadata.jaxbContextIso19139 = JAXBContext.newInstance(new Class[] { org.isotc211.iso19139.d_2006_05_04.gmd.ObjectFactory.class });

            ServiceMetadata.marshallerIso19139 = ServiceMetadata.jaxbContextIso19139.createMarshaller();
            ServiceMetadata.marshallerIso19139.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            ServiceMetadata.unmarshallerIso19139 = ServiceMetadata.jaxbContextIso19139.createUnmarshaller();

        } catch (JAXBException e) {
            LOG.error("initJAXBIso19139()", e);
            throw new MotuException("Error in ServiceMetadata - initJAXBIso19139 ", e);

        }

        // objectFactoryIso19139 = new ObjectFactory();

        if (LOG.isDebugEnabled()) {
            LOG.debug("initJAXBIso19139() - exiting");
        }
    }

    /**
     * Marshall iso19139.
     * 
     * @param element the element
     * @param xmlFile the xml file
     * 
     * @throws MotuMarshallException the motu marshall exception
     * @throws FileSystemException the file system exception
     * @throws MotuException the motu exception
     */
    public void marshallIso19139(JAXBElement<?> element, String xmlFile) throws MotuMarshallException, FileSystemException, MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("marshallIso19139(JAXBElement<?>, String) - entering");
        }

        FileObject fileObject = Organizer.resolveFile(xmlFile);
        fileObject.createFile();

        marshallIso19139(element, fileObject);

        if (LOG.isDebugEnabled()) {
            LOG.debug("marshallIso19139(JAXBElement<?>, String) - exiting");
        }
    }

    /**
     * Marshall iso19139.
     * 
     * @param element the element
     * @param fileObject the file object
     * 
     * @throws MotuMarshallException the motu marshall exception
     * @throws FileSystemException the file system exception
     */
    public void marshallIso19139(JAXBElement<?> element, FileObject fileObject) throws MotuMarshallException, FileSystemException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("marshallIso19139(JAXBElement<?>, FileObject) - entering");
        }

        marshallIso19139(element, fileObject.getContent().getOutputStream());
        fileObject.close();

        if (LOG.isDebugEnabled()) {
            LOG.debug("marshallIso19139(JAXBElement<?>, FileObject) - exiting");
        }
    }

    /**
     * Marshall iso19139.
     * 
     * @param element the element
     * @param outputStream the output stream
     * 
     * @throws MotuMarshallException the motu marshall exception
     */
    public void marshallIso19139(JAXBElement<?> element, OutputStream outputStream) throws MotuMarshallException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("marshallIso19139(JAXBElement<?>, OutputStream) - entering");
        }

        if (ServiceMetadata.marshallerIso19139 == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("marshallIso19139(JAXBElement<?>, OutputStream) - exiting");
            }
            return;
        }
        try {
            synchronized (ServiceMetadata.marshallerIso19139) {

                ServiceMetadata.marshallerIso19139.marshal(element, outputStream);
                outputStream.flush();
                outputStream.close();
            }
        } catch (JAXBException e) {
            LOG.error("marshallIso19139(JAXBElement<?>, OutputStream)", e);

            throw new MotuMarshallException("Error in ServiceMetadata - marshallIso19139", e);
        } catch (IOException e) {
            LOG.error("marshallIso19139(JAXBElement<?>, OutputStream)", e);

            throw new MotuMarshallException("Error in ServiceMetadata - marshallIso19139", e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("marshallIso19139(JAXBElement<?>, OutputStream) - exiting");
        }
    }

    /**
     * Unmarshall iso19139.
     * 
     * @param xmlFile the xml file
     * 
     * @return the jAXB element<?>
     * 
     * @throws MotuMarshallException the motu marshall exception
     */
    public JAXBElement<?> unmarshallIso19139(String xmlFile) throws MotuMarshallException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("unmarshallIso19139(String) - entering");
        }

        Source srcFile = new StreamSource(xmlFile);

        JAXBElement<?> returnJAXBElement = unmarshallIso19139(srcFile);
        if (LOG.isDebugEnabled()) {
            LOG.debug("unmarshallIso19139(String) - exiting");
        }
        return returnJAXBElement;
    }

    /**
     * Unmarshall iso19139.
     * 
     * @param xmlSource the xml source
     * 
     * @return the jAXB element<?>
     * 
     * @throws MotuMarshallException the motu marshall exception
     */
    public JAXBElement<?> unmarshallIso19139(Source xmlSource) throws MotuMarshallException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("unmarshallIso19139(Source) - entering");
        }

        if (ServiceMetadata.unmarshallerIso19139 == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("unmarshallIso19139(Source) - exiting");
            }
            return null;
        }
        JAXBElement<?> element = null;
        try {
            synchronized (ServiceMetadata.unmarshallerIso19139) {

                element = (JAXBElement<?>) ServiceMetadata.unmarshallerIso19139.unmarshal(xmlSource);
            }
        } catch (JAXBException e) {
            LOG.error("unmarshallIso19139(Source)", e);

            throw new MotuMarshallException("Error in ServiceMetadata - unmarshallIso19139", e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("unmarshallIso19139(Source) - exiting");
        }
        return element;

    }

    /**
     * Unmarshall iso19139.
     * 
     * @param xmlSource the xml source
     * 
     * @return the jAXB element<?>
     * 
     * @throws MotuMarshallException the motu marshall exception
     */
    public JAXBElement<?> unmarshallIso19139(InputStream xmlSource) throws MotuMarshallException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("unmarshallIso19139(InputStream) - entering");
        }

        if (ServiceMetadata.unmarshallerIso19139 == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("unmarshallIso19139(InputStream) - exiting");
            }
            return null;
        }
        JAXBElement<?> element = null;
        try {
            synchronized (ServiceMetadata.unmarshallerIso19139) {

                element = (JAXBElement<?>) ServiceMetadata.unmarshallerIso19139.unmarshal(xmlSource);
            }
        } catch (JAXBException e) {
            LOG.error("unmarshallIso19139(InputStream)", e);

            throw new MotuMarshallException("Error in ServiceMetadata - unmarshallIso19139", e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("unmarshallIso19139(InputStream) - exiting");
        }
        return element;

    }

    /**
     * Validate service metadata from string.
     * 
     * @param iso19139Schema the iso19139 schema
     * @param localIso19139SchemaPath the local iso19139 schema path
     * @param localIso19139RootSchemaRelPath the local iso19139 root schema rel path
     * @param xmlTemplate the xml template
     * 
     * @return the list< string>
     * 
     * @throws FileSystemException the file system exception
     * @throws MotuException the motu exception
     */
    public List<String> validateServiceMetadataFromString(String iso19139Schema,
                                                          String localIso19139SchemaPath,
                                                          String localIso19139RootSchemaRelPath,
                                                          String xmlTemplate) throws FileSystemException, MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateServiceMetadataFromString(String, String, String, String) - entering");
        }

        String[] inSchema = getServiceMetadataSchemaAsString(iso19139Schema, localIso19139SchemaPath, localIso19139RootSchemaRelPath);
        if (inSchema == null) {
            throw new MotuException("ERROR in validateServiceMetadata - No schema(s) found.");
        }

        XMLErrorHandler errorHandler = XMLUtils.validateXML(inSchema, xmlTemplate);

        if (errorHandler == null) {
            throw new MotuException("ERROR in Organiser.validateMotuConfig - Motu configuration schema : XMLErrorHandler is null");
        }
        List<String> returnList = errorHandler.getErrors();
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateServiceMetadataFromString(String, String, String, String) - exiting");
        }
        return returnList;
    }

    /**
     * Gets the service metadata schema as string.
     * 
     * @param schemaPath the schema path
     * @param localIso19139SchemaPath the local iso19139 schema path
     * @param localIso19139RootSchemaRelPath the local iso19139 root schema rel path
     * 
     * @return the service metadata schema as string
     * 
     * @throws MotuException the motu exception
     * @throws FileSystemException the file system exception
     */
    public String[] getServiceMetadataSchemaAsString(String schemaPath, String localIso19139SchemaPath, String localIso19139RootSchemaRelPath)
            throws MotuException, FileSystemException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getServiceMetadataSchemaAsString(String, String, String) - entering");
        }

        List<String> stringList = new ArrayList<String>();
        String localIso19139RootSchemaPath = String.format("%s%s", localIso19139SchemaPath, localIso19139RootSchemaRelPath);

        FileObject dest = Organizer.resolveFile(localIso19139RootSchemaPath);
        boolean hasIso19139asLocalSchema = false;
        if (dest != null) {
            hasIso19139asLocalSchema = dest.exists();
        }

        if (hasIso19139asLocalSchema) {
            dest.close();

        } else {

            URL url = Organizer.findResource(schemaPath);

            FileObject jarFile = Organizer.resolveFile(url.toString());

            // List the children of the Jar file
            // FileObject[] children = null;
            // try {
            // children = jarFile.getChildren();
            // } catch (FileSystemException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
            // System.out.println("Children of " + jarFile.getName().getURI());
            // for (int i = 0; i < children.length; i++) {
            // System.out.println(children[i].getName().getBaseName());
            // }

            dest = Organizer.resolveFile(localIso19139SchemaPath);
            Organizer.copyFile(jarFile, dest);
        }

        stringList.add(localIso19139RootSchemaPath);
        String[] inS = new String[stringList.size()];
        inS = stringList.toArray(inS);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getServiceMetadataSchemaAsString(String, String, String) - exiting");
        }
        return inS;
    }

    /**
     * Dom4j to jaxb.
     * 
     * @param document the document
     * 
     * @return the jAXB element<?>
     * 
     * @throws MotuExceptionBase the motu exception base
     */
    public JAXBElement<?> dom4jToJaxb(Document document) throws MotuExceptionBase {
        if (LOG.isDebugEnabled()) {
            LOG.debug("dom4jToJaxb(Document) - entering");
        }

        InputStream inputStream = XMLUtils.dom4jToIntputStream(document, ServiceMetadata.ISO19139_SHEMA_PACK_NAME);

        JAXBElement<?> jaxbElement = unmarshallIso19139(inputStream);

        if (LOG.isDebugEnabled()) {
            LOG.debug("dom4jToJaxb(Document) - exiting");
        }
        return jaxbElement;
    }

    /**
     * Gets the operations.
     * 
     * @param xmlFile the xml file
     * @param listOperation the list operation
     * 
     * @return the operations
     * 
     * @throws MotuMarshallException the motu marshall exception
     */
    public void getOperations(String xmlFile, Collection<SVOperationMetadataType> listOperation) throws MotuMarshallException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperations(String, Collection<SVOperationMetadataType>) - entering");
        }

        Source srcFile = new StreamSource(xmlFile);
        getOperations(srcFile, listOperation);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperations(String, Collection<SVOperationMetadataType>) - exiting");
        }
    }

    /**
     * Gets the operations.
     * 
     * @param xmlUrl the xml url
     * @param listOperation the list operation
     * 
     * @return the operations
     * 
     * @throws MotuMarshallException the motu marshall exception
     */
    public void getOperations(URL xmlUrl, Collection<SVOperationMetadataType> listOperation) throws MotuMarshallException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperations(URL, Collection<SVOperationMetadataType>) - entering");
        }

        getOperations(xmlUrl.getPath(), listOperation);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperations(URL, Collection<SVOperationMetadataType>) - exiting");
        }
    }

    /**
     * Gets the operations.
     * 
     * @param xmlFile the xml file
     * @param listOperation the list operation
     * 
     * @return the operations
     * 
     * @throws MotuMarshallException the motu marshall exception
     */
    public void getOperations(Source xmlFile, Collection<SVOperationMetadataType> listOperation) throws MotuMarshallException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperations(Source, Collection<SVOperationMetadataType>) - entering");
        }

        JAXBElement<?> element = unmarshallIso19139(xmlFile);
        getOperations(element, listOperation);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperations(Source, Collection<SVOperationMetadataType>) - exiting");
        }
    }

    /**
     * Gets the operations.
     * 
     * @param document the document
     * @param listOperation the list operation
     * 
     * @return the operations
     * 
     * @throws MotuExceptionBase the motu exception base
     */
    public void getOperations(Document document, Collection<SVOperationMetadataType> listOperation) throws MotuExceptionBase {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperations(Document, Collection<SVOperationMetadataType>) - entering");
        }

        JAXBElement<?> element = dom4jToJaxb(document);
        getOperations(element, listOperation);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperations(Document, Collection<SVOperationMetadataType>) - exiting");
        }
    }

    /**
     * Gets the operations.
     * 
     * @param root the root
     * @param listOperation the list operation
     * 
     * @return the operations
     */
    public void getOperations(JAXBElement<?> root, Collection<SVOperationMetadataType> listOperation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperations(JAXBElement<?>, Collection<SVOperationMetadataType>) - entering");
        }

        SVServiceIdentificationType serviceIdentificationType = ServiceMetadata.getServiceIdentificationType(root);
        getOperations(serviceIdentificationType, listOperation);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperations(JAXBElement<?>, Collection<SVOperationMetadataType>) - exiting");
        }
    }

    /**
     * Gets the operations.
     * 
     * @param serviceIdentificationType the service identification type
     * @param listOperation the list operation
     * 
     * @return the operations
     */
    public void getOperations(SVServiceIdentificationType serviceIdentificationType, Collection<SVOperationMetadataType> listOperation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperations(SVServiceIdentificationType, Collection<SVOperationMetadataType>) - entering");
        }

        List<SVOperationMetadataPropertyType> operationMetadataPropertyTypeList = serviceIdentificationType.getContainsOperations();
        getOperations(operationMetadataPropertyTypeList, listOperation);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperations(SVServiceIdentificationType, Collection<SVOperationMetadataType>) - exiting");
        }
    }

    /**
     * Gets the operations.
     * 
     * @param operationMetadataPropertyTypeList the operation metadata property type list
     * @param listOperation the list operation
     * 
     * @return the operations
     */
    public void getOperations(Collection<SVOperationMetadataPropertyType> operationMetadataPropertyTypeList,
                              Collection<SVOperationMetadataType> listOperation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperations(Collection<SVOperationMetadataPropertyType>, Collection<SVOperationMetadataType>) - entering");
        }

        for (SVOperationMetadataPropertyType operationMetadataPropertyType : operationMetadataPropertyTypeList) {

            SVOperationMetadataType operationMetadataType = operationMetadataPropertyType.getSVOperationMetadata();
            if (operationMetadataType == null) {
                continue;
            }
            if (listOperation != null) {
                listOperation.add(operationMetadataType);
            }

            getOperations(operationMetadataType.getDependsOn(), listOperation);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperations(Collection<SVOperationMetadataPropertyType>, Collection<SVOperationMetadataType>) - exiting");
        }
    }

    /**
     * Gets the operations.
     * 
     * @param xmlFile the xml file
     * @param directedGraph the directed graph
     * 
     * @return the operations
     * 
     * @throws MotuMarshallException the motu marshall exception
     * @throws MotuException the motu exception
     */
    public void getOperations(String xmlFile, DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> directedGraph)
            throws MotuMarshallException, MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperations(String, DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>) - entering");
        }

        Source srcFile = new StreamSource(xmlFile);
        getOperations(srcFile, directedGraph);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperations(String, DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>) - exiting");
        }
    }

    /**
     * Gets the operations.
     * 
     * @param xmlUrl the xml url
     * @param directedGraph the directed graph
     * 
     * @return the operations
     * 
     * @throws MotuMarshallException the motu marshall exception
     * @throws MotuException the motu exception
     */
    public void getOperations(URL xmlUrl, DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> directedGraph)
            throws MotuMarshallException, MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperations(URL, DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>) - entering");
        }

        getOperations(xmlUrl.getPath(), directedGraph);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperations(URL, DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>) - exiting");
        }
    }

    /**
     * Gets the operations.
     * 
     * @param xmlFile the xml file
     * @param directedGraph the directed graph
     * 
     * @return the operations
     * 
     * @throws MotuMarshallException the motu marshall exception
     * @throws MotuException the motu exception
     */
    public void getOperations(Source xmlFile, DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> directedGraph)
            throws MotuMarshallException, MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperations(Source, DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>) - entering");
        }

        JAXBElement<?> element = unmarshallIso19139(xmlFile);
        getOperations(element, directedGraph);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperations(Source, DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>) - exiting");
        }
    }

    /**
     * Gets the operations.
     * 
     * @param document the document
     * @param directedGraph the directed graph
     * 
     * @return the operations
     * 
     * @throws MotuExceptionBase the motu exception base
     */
    public void getOperations(Document document, DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> directedGraph)
            throws MotuExceptionBase {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperations(Document, DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>) - entering");
        }

        JAXBElement<?> element = dom4jToJaxb(document);
        getOperations(element, directedGraph);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperations(Document, DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>) - exiting");
        }
    }

    /**
     * Gets the operations.
     * 
     * @param root the root
     * @param directedGraph the directed graph
     * 
     * @return the operations
     * 
     * @throws MotuException the motu exception
     */
    public void getOperations(JAXBElement<?> root, DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> directedGraph)
            throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperations(JAXBElement<?>, DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>) - entering");
        }

        SVServiceIdentificationType serviceIdentificationType = ServiceMetadata.getServiceIdentificationType(root);
        getOperations(serviceIdentificationType, directedGraph);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperations(JAXBElement<?>, DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>) - exiting");
        }
    }

    /**
     * Gets the operations.
     * 
     * @param serviceIdentificationType the service identification type
     * @param directedGraph the directed graph
     * 
     * @return the operations
     * 
     * @throws MotuException the motu exception
     */
    public void getOperations(SVServiceIdentificationType serviceIdentificationType,
                              DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> directedGraph) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperations(SVServiceIdentificationType, DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>) - entering");
        }

        // TODO add source code
        List<SVOperationMetadataPropertyType> operationMetadataPropertyTypeList = serviceIdentificationType.getContainsOperations();
        getOperations(operationMetadataPropertyTypeList, directedGraph);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperations(SVServiceIdentificationType, DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>) - exiting");
        }
    }

    /**
     * Gets the operations.
     * 
     * @param operationMetadataPropertyTypeList the operation metadata property type list
     * @param directedGraph the directed graph
     * 
     * @return the operations
     * 
     * @throws MotuException the motu exception
     */
    public void getOperations(Collection<SVOperationMetadataPropertyType> operationMetadataPropertyTypeList,
                              DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> directedGraph) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG
                    .debug("getOperations(Collection<SVOperationMetadataPropertyType>, DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>) - entering");
        }

        getOperations(operationMetadataPropertyTypeList, directedGraph, null);

        if (LOG.isDebugEnabled()) {
            LOG
                    .debug("getOperations(Collection<SVOperationMetadataPropertyType>, DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>) - exiting");
        }
    }

    /**
     * Find vertex.
     * 
     * @param graph the graph
     * @param obj the obj
     * 
     * @return the operation metadata
     */
    public OperationMetadata findVertex(Graph<OperationMetadata, OperationRelationshipEdge<String>> graph, OperationMetadata obj) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findVertex(Graph<OperationMetadata,OperationRelationshipEdge<String>>, OperationMetadata) - entering");
        }

        Set<OperationMetadata> set = graph.vertexSet();

        for (OperationMetadata o : set) {
            if (o.equals(obj)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("findVertex(Graph<OperationMetadata,OperationRelationshipEdge<String>>, OperationMetadata) - exiting");
                }
                return o;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("findVertex(Graph<OperationMetadata,OperationRelationshipEdge<String>>, OperationMetadata) - exiting");
        }
        return null;
    }

    /**
     * Gets the operations.
     * 
     * @param operationMetadataPropertyTypeList the operation metadata property type list
     * @param directedGraph the directed graph
     * @param parent the parent
     * 
     * @return the operations
     * 
     * @throws MotuException the motu exception
     */
    public void getOperations(Collection<SVOperationMetadataPropertyType> operationMetadataPropertyTypeList,
                              DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> directedGraph,
                              OperationMetadata parent) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG
                    .debug("getOperations(Collection<SVOperationMetadataPropertyType>, DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, OperationMetadata) - entering");
        }

        for (SVOperationMetadataPropertyType operationMetadataPropertyType : operationMetadataPropertyTypeList) {

            SVOperationMetadataType operationMetadataType = operationMetadataPropertyType.getSVOperationMetadata();

            if (operationMetadataType == null) {
                continue;
            }
            OperationMetadata operationMetadataTmp = new OperationMetadata(operationMetadataType);
            OperationMetadata operationMetadata = null;
            if (directedGraph != null) {
                operationMetadata = (OperationMetadata) findVertex(directedGraph, operationMetadataTmp);
                if (operationMetadata == null) {
                    operationMetadata = operationMetadataTmp;
                    directedGraph.addVertex(operationMetadata);
                }
                if (parent != null) {
                    List<String> inOp1 = new ArrayList<String>();
                    List<String> outOp2 = new ArrayList<String>();
                    String parametersEdge = operationMetadataPropertyType.getUuidref();

                    try {
                        getParemetersEdge(parametersEdge, inOp1, outOp2);
                    } catch (MotuExceptionBase e) {
                        LOG
                                .error("getOperations(Collection<SVOperationMetadataPropertyType>, DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, OperationMetadata)",
                                       e);

                        throw new MotuException(
                                String
                                        .format("ERROR - ISO 19139 parameters edge '%s' between two operations have not been set (or not correctly set) : operation : '%s - set 'uuidref' attribute)",
                                                parametersEdge,
                                                operationMetadata.getOperationName()));
                    }

                    if (inOp1.isEmpty() || outOp2.isEmpty()) {
                        throw new MotuException(
                                String
                                        .format("ERROR - ISO 19139 parameters edge '%s' between two operations have not been set (or not correctly set) : operation : '%s - set 'uuidref' attribute)",
                                                parametersEdge,
                                                operationMetadata.getOperationName()));
                    }

                    String edgelabel = String.format("%s/%s", parent.getInvocationName(), operationMetadata.getInvocationName());
                    OperationRelationshipEdge<String> edge = new OperationRelationshipEdge<String>(inOp1, outOp2, edgelabel);
                    directedGraph.addEdge(parent, operationMetadata, edge);

                }
            }

            getOperations(operationMetadataType.getDependsOn(), directedGraph, operationMetadata);
        }

        if (LOG.isDebugEnabled()) {
            LOG
                    .debug("getOperations(Collection<SVOperationMetadataPropertyType>, DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, OperationMetadata) - exiting");
        }
    }

    /**
     * Gets the paremeters edge.
     * 
     * @param parametersEdge the parameters edge
     * @param inOp1 the in op1
     * @param outOp2 the out op2
     * 
     * @return the paremeters edge
     * 
     * @throws MotuException the motu exception
     */
    public void getParemetersEdge(String parametersEdge, List<String> inOp1, List<String> outOp2) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getParemetersEdge(String, List<String>, List<String>) - entering");
        }

        if (WPSUtils.isNullOrEmpty(parametersEdge)) {
            throw new MotuException("ERROR - ISO 19139 parameters edge have not been set");
        }

        String[] paramList = parametersEdge.split(",");

        if (paramList.length <= 0) {
            throw new MotuException("ERROR - ISO 19139 parameters edge have not been set");
        }
        if (paramList.length % 2 != 0) {
            throw new MotuException("ERROR - ISO 19139 parameters edge have not been set correctly : not pairs of 'out,in,out,in ...')");
        }

        for (int i = 0; i < paramList.length; i = i + 2) {
            inOp1.add(paramList[i]);
            outOp2.add(paramList[i + 1]);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getParemetersEdge(String, List<String>, List<String>) - exiting");
        }
    }

    /**
     * Gets the operations name unique.
     * 
     * @param listOperation the list operation
     * @param listOperationName the list operation name
     * 
     * @return the operations name unique
     */
    public void getOperationsNameUnique(Collection<SVOperationMetadataType> listOperation, Collection<String> listOperationName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperationsNameUnique(Collection<SVOperationMetadataType>, Collection<String>) - entering");
        }

        Set<String> names = new HashSet<String>();

        for (SVOperationMetadataType operationMetadataType : listOperation) {

            if (operationMetadataType == null) {
                continue;
            }
            if (operationMetadataType.getOperationName() == null) {
                continue;
            }
            if (operationMetadataType.getOperationName().getCharacterString() == null) {
                continue;
            }
            String value = (String) operationMetadataType.getOperationName().getCharacterString().getValue();
            names.add(value);
        }

        listOperationName.addAll(names);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperationsNameUnique(Collection<SVOperationMetadataType>, Collection<String>) - exiting");
        }
    }

    /**
     * Gets the operations invocation name unique.
     * 
     * @param listOperation the list operation
     * @param listInvocationName the list invocation name
     * 
     * @return the operations invocation name unique
     */
    public void getOperationsInvocationNameUnique(Collection<SVOperationMetadataType> listOperation, Collection<String> listInvocationName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperationsInvocationNameUnique(Collection<SVOperationMetadataType>, Collection<String>) - entering");
        }

        Set<String> names = new HashSet<String>();

        for (SVOperationMetadataType operationMetadataType : listOperation) {

            if (operationMetadataType == null) {
                continue;
            }
            if (operationMetadataType.getInvocationName() == null) {
                continue;
            }
            if (operationMetadataType.getInvocationName().getCharacterString() == null) {
                continue;
            }
            String value = (String) operationMetadataType.getInvocationName().getCharacterString().getValue();
            names.add(value);
        }

        listInvocationName.addAll(names);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperationsInvocationNameUnique(Collection<SVOperationMetadataType>, Collection<String>) - exiting");
        }
    }

    /**
     * Gets the operations unique.
     * 
     * @param listOperation the list operation
     * @param listOperationUnique the list operation unique
     * 
     * @return the operations unique
     */
    public void getOperationsUnique(Collection<SVOperationMetadataType> listOperation, Collection<SVOperationMetadataType> listOperationUnique) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperationsUnique(Collection<SVOperationMetadataType>, Collection<SVOperationMetadataType>) - entering");
        }

        Map<String, SVOperationMetadataType> operations = new HashMap<String, SVOperationMetadataType>();

        for (SVOperationMetadataType operationMetadataType : listOperation) {

            if (operationMetadataType == null) {
                continue;
            }
            if (operationMetadataType.getOperationName() == null) {
                continue;
            }
            if (operationMetadataType.getOperationName().getCharacterString() == null) {
                continue;
            }
            String value = (String) operationMetadataType.getOperationName().getCharacterString().getValue();
            operations.put(value, operationMetadataType);
        }

        listOperationUnique.addAll(operations.values());

        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperationsUnique(Collection<SVOperationMetadataType>, Collection<SVOperationMetadataType>) - exiting");
        }
    }

    /**
     * Checks if is source operation.
     * 
     * @param directedGraph the directed graph
     * @param op the op
     * 
     * @return true, if is source operation
     */
    public static boolean isSourceOperation(DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> directedGraph, OperationMetadata op) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isSourceOperation(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, OperationMetadata) - entering");
        }

        DirectedNeighborIndex<OperationMetadata, OperationRelationshipEdge<String>> ni = new DirectedNeighborIndex<OperationMetadata, OperationRelationshipEdge<String>>(
                directedGraph);
        if (directedGraph == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isSourceOperation(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, OperationMetadata) - exiting");
            }
            return false;

        }
        if (op == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isSourceOperation(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, OperationMetadata) - exiting");
            }
            return false;

        }
        if (ni.predecessorsOf(op).isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isSourceOperation(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, OperationMetadata) - exiting");
            }
            return true;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("isSourceOperation(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, OperationMetadata) - exiting");
        }
        return false;

    }

    /**
     * Checks if is sink operation.
     * 
     * @param directedGraph the directed graph
     * @param op the op
     * 
     * @return true, if is sink operation
     */
    public static boolean isSinkOperation(DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> directedGraph, OperationMetadata op) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isSinkOperation(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, OperationMetadata) - entering");
        }

        DirectedNeighborIndex<OperationMetadata, OperationRelationshipEdge<String>> ni = new DirectedNeighborIndex<OperationMetadata, OperationRelationshipEdge<String>>(
                directedGraph);
        if (directedGraph == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isSinkOperation(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, OperationMetadata) - exiting");
            }
            return false;

        }
        if (op == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isSinkOperation(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, OperationMetadata) - exiting");
            }
            return false;

        }
        if (ni.successorsOf(op).isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isSinkOperation(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, OperationMetadata) - exiting");
            }
            return true;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("isSinkOperation(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, OperationMetadata) - exiting");
        }
        return false;

    }

    public static OperationMetadata getSourceOperation(DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> directedGraph)
            throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSourceOperation(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>) - entering");
        }

        List<OperationMetadata> sourceOperations = new ArrayList<OperationMetadata>();

        ServiceMetadata.getSourceOperations(directedGraph, sourceOperations);
        if (sourceOperations.size() != 1) {
            throw new MotuException(
                    String
                            .format("ServiceMetadta#getSourceOperation - There are either none or multiple source operations int the graph. Only one source operation is allowed to create a WPS process - Source operations are: %s",
                                    sourceOperations.toString()));
        }

        OperationMetadata returnOperationMetadata = sourceOperations.get(0);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSourceOperation(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>) - exiting");
        }
        return returnOperationMetadata;
    }

    /**
     * Gets the source operations.
     * 
     * @param directedGraph the directed graph
     * @param sourceOperations the source operations
     * 
     * @return the source operations
     */
    public static void getSourceOperations(DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> directedGraph,
                                           Collection<OperationMetadata> sourceOperations) {
        if (LOG.isDebugEnabled()) {
            LOG
                    .debug("getSourceOperations(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, Collection<OperationMetadata>) - entering");
        }

        if (directedGraph == null) {
            if (LOG.isDebugEnabled()) {
                LOG
                        .debug("getSourceOperations(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, Collection<OperationMetadata>) - exiting");
            }
            return;

        }
        if (sourceOperations == null) {
            if (LOG.isDebugEnabled()) {
                LOG
                        .debug("getSourceOperations(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, Collection<OperationMetadata>) - exiting");
            }
            return;

        }
        for (OperationMetadata v : directedGraph.vertexSet()) {
            if (ServiceMetadata.isSourceOperation(directedGraph, v)) {
                sourceOperations.add(v);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG
                    .debug("getSourceOperations(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, Collection<OperationMetadata>) - exiting");
        }
    }

    /**
     * Gets the sink operations.
     * 
     * @param directedGraph the directed graph
     * @param sinkOperations the sink operations
     * 
     * @return the sink operations
     */
    public static void getSinkOperations(DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> directedGraph,
                                         Collection<OperationMetadata> sinkOperations) {
        if (LOG.isDebugEnabled()) {
            LOG
                    .debug("getSinkOperations(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, Collection<OperationMetadata>) - entering");
        }

        if (directedGraph == null) {
            if (LOG.isDebugEnabled()) {
                LOG
                        .debug("getSinkOperations(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, Collection<OperationMetadata>) - exiting");
            }
            return;

        }
        if (sinkOperations == null) {
            if (LOG.isDebugEnabled()) {
                LOG
                        .debug("getSinkOperations(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, Collection<OperationMetadata>) - exiting");
            }
            return;

        }
        for (OperationMetadata v : directedGraph.vertexSet()) {
            if (ServiceMetadata.isSinkOperation(directedGraph, v)) {
                sinkOperations.add(v);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG
                    .debug("getSinkOperations(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, Collection<OperationMetadata>) - exiting");
        }
    }

    /**
     * Gets the operation paths.
     * 
     * @param directedGraph the directed graph
     * @param source the source
     * 
     * @return the operation paths
     */
    public static KShortestPaths<OperationMetadata, OperationRelationshipEdge<String>> getOperationPaths(DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> directedGraph,
                                                                                                         OperationMetadata source) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperationPaths(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, OperationMetadata) - entering");
        }

        KShortestPaths<OperationMetadata, OperationRelationshipEdge<String>> returnKShortestPaths = getOperationPaths(directedGraph,
                                                                                                                      source,
                                                                                                                      Integer.MAX_VALUE);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperationPaths(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, OperationMetadata) - exiting");
        }
        return returnKShortestPaths;

    }

    /**
     * Gets the operation paths.
     * 
     * @param directedGraph the directed graph
     * @param source the source
     * @param numPathsToBeComputed the num paths to be computed
     * 
     * @return the operation paths
     */
    public static KShortestPaths<OperationMetadata, OperationRelationshipEdge<String>> getOperationPaths(DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> directedGraph,
                                                                                                         OperationMetadata source,
                                                                                                         int numPathsToBeComputed) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperationPaths(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, OperationMetadata, int) - entering");
        }

        KShortestPaths<OperationMetadata, OperationRelationshipEdge<String>> returnKShortestPaths = new KShortestPaths<OperationMetadata, OperationRelationshipEdge<String>>(
                directedGraph,
                source,
                numPathsToBeComputed);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperationPaths(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, OperationMetadata, int) - exiting");
        }
        return returnKShortestPaths;

    }

    /**
     * Gets the operation paths.
     * 
     * @param paths the paths
     * @param sink the sink
     * 
     * @return the operation paths
     */
    public static List<GraphPath<OperationMetadata, OperationRelationshipEdge<String>>> getOperationPaths(KShortestPaths<OperationMetadata, OperationRelationshipEdge<String>> paths,
                                                                                                          OperationMetadata sink) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperationPaths(KShortestPaths<OperationMetadata,OperationRelationshipEdge<String>>, OperationMetadata) - entering");
        }

        List<GraphPath<OperationMetadata, OperationRelationshipEdge<String>>> returnList = paths.getPaths(sink);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOperationPaths(KShortestPaths<OperationMetadata,OperationRelationshipEdge<String>>, OperationMetadata) - exiting");
        }
        return returnList;

    }

    /**
     * Gets the operation paths.
     * 
     * @param directedGraph the directed graph
     * @param source the source
     * @param sink the sink
     * @param numPathsToBeComputed the num paths to be computed
     * 
     * @return the operation paths
     */
    public static List<GraphPath<OperationMetadata, OperationRelationshipEdge<String>>> getOperationPaths(DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> directedGraph,
                                                                                                          OperationMetadata source,
                                                                                                          OperationMetadata sink,
                                                                                                          int numPathsToBeComputed) {
        if (LOG.isDebugEnabled()) {
            LOG
                    .debug("getOperationPaths(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, OperationMetadata, OperationMetadata, int) - entering");
        }

        KShortestPaths<OperationMetadata, OperationRelationshipEdge<String>> paths = getOperationPaths(directedGraph, source, numPathsToBeComputed);
        List<GraphPath<OperationMetadata, OperationRelationshipEdge<String>>> returnList = paths.getPaths(sink);
        if (LOG.isDebugEnabled()) {
            LOG
                    .debug("getOperationPaths(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, OperationMetadata, OperationMetadata, int) - exiting");
        }
        return returnList;

    }

    /**
     * Find operation by name.
     * 
     * @param directedGraph the directed graph
     * @param operationName the operation name
     * 
     * @return the operation metadata
     */
    public static OperationMetadata findOperationByName(DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> directedGraph,
                                                        String operationName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findOperationByName(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, String) - entering");
        }

        Set<OperationMetadata> set = directedGraph.vertexSet();

        for (OperationMetadata o : set) {
            if (o.getOperationName().equals(operationName)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("findOperationByName(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, String) - exiting");
                }
                return o;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("findOperationByName(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, String) - exiting");
        }
        return null;

    }

    /**
     * Find source operation by name.
     * 
     * @param directedGraph the directed graph
     * @param operationName the operation name
     * 
     * @return the operation metadata
     */
    public static OperationMetadata findSourceOperationByName(DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> directedGraph,
                                                              String operationName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findSourceOperationByName(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, String) - entering");
        }

        List<OperationMetadata> sourceOperations = new ArrayList<OperationMetadata>();
        ServiceMetadata.getSourceOperations(directedGraph, sourceOperations);

        for (OperationMetadata o : sourceOperations) {
            if (o.getOperationName().equals(operationName)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("findSourceOperationByName(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, String) - exiting");
                }
                return o;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("findSourceOperationByName(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, String) - exiting");
        }
        return null;

    }

    /**
     * Find sink operation by name.
     * 
     * @param directedGraph the directed graph
     * @param operationName the operation name
     * 
     * @return the operation metadata
     */
    public static OperationMetadata findSinkOperationByName(DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> directedGraph,
                                                            String operationName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findSinkOperationByName(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, String) - entering");
        }

        List<OperationMetadata> sinkOperations = new ArrayList<OperationMetadata>();
        ServiceMetadata.getSinkOperations(directedGraph, sinkOperations);

        for (OperationMetadata o : sinkOperations) {
            if (o.getOperationName().equals(operationName)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("findSinkOperationByName(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, String) - exiting");
                }
                return o;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("findSinkOperationByName(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, String) - exiting");
        }
        return null;

    }

    /**
     * Dump.
     * 
     * @param listOperation the list operation
     */
    public static void dump(Collection<SVOperationMetadataType> listOperation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("dump(Collection<SVOperationMetadataType>) - entering");
        }

        if (listOperation == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("dump(Collection<SVOperationMetadataType>) - exiting");
            }
            return;
        }

        for (SVOperationMetadataType operationMetadataType : listOperation) {

            System.out.println("---------------------------------------------");
            if (operationMetadataType == null) {
                continue;
            }
            System.out.println(operationMetadataType.getOperationName().getCharacterString().getValue());
            System.out.println(operationMetadataType.getInvocationName().getCharacterString().getValue());
            System.out.println(operationMetadataType.getOperationDescription().getCharacterString().getValue());

            CIOnlineResourcePropertyType onlineResourcePropertyType = operationMetadataType.getConnectPoint().get(0);
            if (onlineResourcePropertyType != null) {
                System.out.println(operationMetadataType.getConnectPoint().get(0).getCIOnlineResource().getLinkage().getURL());
            }

            List<SVParameterPropertyType> parameterPropertyTypeList = operationMetadataType.getParameters();

            for (SVParameterPropertyType parameterPropertyType : parameterPropertyTypeList) {
                SVParameterType parameterType = parameterPropertyType.getSVParameter();

                if (parameterType.getName().getAName().getCharacterString() != null) {
                    System.out.println(parameterType.getName().getAName().getCharacterString().getValue());
                } else {
                    System.out.println("WARNING - A parameter has no name");

                }
                if (parameterType.getDescription() != null) {
                    if (parameterType.getDescription().getCharacterString() != null) {
                        System.out.println(parameterType.getDescription().getCharacterString().getValue());
                    } else {
                        System.out.println("WARNING - A parameter has no description");

                    }
                } else {
                    System.out.println("WARNING - A parameter has no description");

                }
            }

        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("dump(Collection<SVOperationMetadataType>) - exiting");
        }
    }

    /**
     * Gets the service identification type.
     * 
     * @param element the element
     * 
     * @return the service identification type
     */
    public static SVServiceIdentificationType getServiceIdentificationType(JAXBElement<?> element) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getServiceIdentificationType(JAXBElement<?>) - entering");
        }

        if (element == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getServiceIdentificationType(JAXBElement<?>) - exiting");
            }
            return null;
        }

        MDMetadataType metadataType = (MDMetadataType) element.getValue();
        if (metadataType == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getServiceIdentificationType(JAXBElement<?>) - exiting");
            }
            return null;
        }

        List<MDIdentificationPropertyType> identificationPropertyTypeList = metadataType.getIdentificationInfo();
        if (identificationPropertyTypeList == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getServiceIdentificationType(JAXBElement<?>) - exiting");
            }
            return null;
        }

        for (MDIdentificationPropertyType identificationPropertyType : identificationPropertyTypeList) {
            AbstractMDIdentificationType abstractMDIdentificationType = identificationPropertyType.getAbstractMDIdentification().getValue();
            if (abstractMDIdentificationType == null) {
                continue;
            }
            if (abstractMDIdentificationType instanceof SVServiceIdentificationType) {
                SVServiceIdentificationType returnSVServiceIdentificationType = (SVServiceIdentificationType) abstractMDIdentificationType;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getServiceIdentificationType(JAXBElement<?>) - exiting");
                }
                return returnSVServiceIdentificationType;

            }

        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getServiceIdentificationType(JAXBElement<?>) - exiting");
        }
        return null;
    }

    /**
     * Dump.
     * 
     * @param element the element
     */
    public static void dump(JAXBElement<?> element) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("dump(JAXBElement<?>) - entering");
        }

        SVServiceIdentificationType serviceIdentificationType = ServiceMetadata.getServiceIdentificationType(element);
        if (serviceIdentificationType == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("dump(JAXBElement<?>) - exiting");
            }
            return;
        }
        System.out.println(serviceIdentificationType.toString());

        List<SVOperationMetadataPropertyType> operationMetadataPropertyTypeList = serviceIdentificationType.getContainsOperations();

        for (SVOperationMetadataPropertyType operationMetadataPropertyType : operationMetadataPropertyTypeList) {

            SVOperationMetadataType operationMetadataType = operationMetadataPropertyType.getSVOperationMetadata();
            System.out.println("---------------------------------------------");
            if (operationMetadataType == null) {
                continue;
            }
            System.out.println(operationMetadataType.getOperationName().getCharacterString().getValue());
            System.out.println(operationMetadataType.getInvocationName().getCharacterString().getValue());
            System.out.println(operationMetadataType.getOperationDescription().getCharacterString().getValue());

            CIOnlineResourcePropertyType onlineResourcePropertyType = operationMetadataType.getConnectPoint().get(0);
            if (onlineResourcePropertyType != null) {
                System.out.println(operationMetadataType.getConnectPoint().get(0).getCIOnlineResource().getLinkage().getURL());
            }

            List<SVParameterPropertyType> parameterPropertyTypeList = operationMetadataType.getParameters();

            for (SVParameterPropertyType parameterPropertyType : parameterPropertyTypeList) {
                SVParameterType parameterType = parameterPropertyType.getSVParameter();

                if (parameterType.getName().getAName().getCharacterString() != null) {
                    System.out.println(parameterType.getName().getAName().getCharacterString().getValue());
                } else {
                    System.out.println("WARNING - A parameter has no name");

                }
                if (parameterType.getDescription() != null) {
                    if (parameterType.getDescription().getCharacterString() != null) {
                        System.out.println(parameterType.getDescription().getCharacterString().getValue());
                    } else {
                        System.out.println("WARNING - A parameter has no description");

                    }
                } else {
                    System.out.println("WARNING - A parameter has no description");

                }
            }

        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("dump(JAXBElement<?>) - exiting");
        }
    }

    /**
     * Creates the directed graph.
     * 
     * @return the directed graph< operation metadata, operation relationship edge< string>>
     */
    @SuppressWarnings("unchecked")
    public static DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> createDirectedGraph() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createDirectedGraph() - entering");
        }
        
        OperationRelationshipEdge<String> edge = new OperationRelationshipEdge<String>();
        
        ClassBasedEdgeFactory<OperationMetadata, OperationRelationshipEdge<String>> classBasedEdgeFactory = new ClassBasedEdgeFactory<OperationMetadata, OperationRelationshipEdge<String>>(
                (Class<? extends OperationRelationshipEdge<String>>) edge.getClass());
        DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> graph = new DefaultDirectedGraph<OperationMetadata, OperationRelationshipEdge<String>>(
                classBasedEdgeFactory);

        if (LOG.isDebugEnabled()) {
            LOG.debug("createDirectedGraph() - exiting");
        }
        return graph;
    }

    /**
     * Creates the directed sub graph.
     * 
     * @param base the base
     * @param vertexSubset the vertex subset
     * @param edgeSubset the edge subset
     * 
     * @return the directed graph< operation metadata, operation relationship edge< string>>
     */
    public static DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> createDirectedSubGraph(DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> base,
                                                                                                             Set<OperationMetadata> vertexSubset,
                                                                                                             Set<OperationRelationshipEdge<String>> edgeSubset) {
        if (LOG.isDebugEnabled()) {
            LOG
                    .debug("createDirectedSubGraph(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, Set<OperationMetadata>, Set<OperationRelationshipEdge<String>>) - entering");
        }

        DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> returnDirectedGraph = new DirectedSubgraph<OperationMetadata, OperationRelationshipEdge<String>>(
                base,
                vertexSubset,
                edgeSubset);
        if (LOG.isDebugEnabled()) {
            LOG
                    .debug("createDirectedSubGraph(DirectedGraph<OperationMetadata,OperationRelationshipEdge<String>>, Set<OperationMetadata>, Set<OperationRelationshipEdge<String>>) - exiting");
        }
        return returnDirectedGraph;
    }

 
}
