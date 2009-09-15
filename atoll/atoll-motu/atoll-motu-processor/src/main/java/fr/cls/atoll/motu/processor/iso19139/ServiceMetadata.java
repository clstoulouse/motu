package fr.cls.atoll.motu.processor.iso19139;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import org.dom4j.Element;
import org.dom4j.jaxb.JAXBWriter;
import org.isotc211.iso19139.d_2006_05_04.gmd.CIOnlineResourcePropertyType;
import org.isotc211.iso19139.d_2006_05_04.srv.ObjectFactory;
import org.isotc211.iso19139.d_2006_05_04.srv.SVOperationMetadataPropertyType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVOperationMetadataType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVParameterPropertyType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVParameterType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVServiceIdentificationType;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.alg.DirectedNeighborIndex;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.xml.sax.SAXException;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.exception.MotuMarshallException;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.atoll.motu.library.xml.XMLErrorHandler;
import fr.cls.atoll.motu.library.xml.XMLUtils;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.3 $ - $Date: 2009-09-15 14:28:53 $
 */
public class ServiceMetadata {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(ServiceMetadata.class);

    public static final String ISO19139_SHEMA_PACK_NAME = "org.isotc211.iso19139.d_2006_05_04.srv";

    public ServiceMetadata() throws MotuException {

        ServiceMetadata.initJAXBIso19139();
    }

    private static JAXBContext jaxbContextIso19139 = null;
    private static Marshaller marshallerIso19139 = null;
    private static Unmarshaller unmarshallerIso19139 = null;

    // private static ObjectFactory objectFactoryIso19139 = null;

    private static void initJAXBIso19139() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initJAXBIso19139() - entering");
        }
        if (ServiceMetadata.jaxbContextIso19139 != null) {
            return;
        }

        try {
            ServiceMetadata.jaxbContextIso19139 = JAXBContext.newInstance(new Class[] { ObjectFactory.class });

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

    public void marshallIso19139(JAXBElement<?> element, String xmlFile) throws MotuMarshallException, FileSystemException, MotuException {
        FileObject fileObject = Organizer.resolveFile(xmlFile);
        fileObject.createFile();

        marshallIso19139(element, fileObject);
    }

    public void marshallIso19139(JAXBElement<?> element, FileObject fileObject) throws MotuMarshallException, FileSystemException {

        marshallIso19139(element, fileObject.getContent().getOutputStream());
        fileObject.close();
    }

    public void marshallIso19139(JAXBElement<?> element, OutputStream outputStream) throws MotuMarshallException {

        if (ServiceMetadata.marshallerIso19139 == null) {
            return;
        }
        try {
            synchronized (ServiceMetadata.marshallerIso19139) {

                ServiceMetadata.marshallerIso19139.marshal(element, outputStream);
                outputStream.flush();
                outputStream.close();
            }
        } catch (JAXBException e) {
            throw new MotuMarshallException("Error in ServiceMetadata - marshallIso19139", e);
        } catch (IOException e) {
            throw new MotuMarshallException("Error in ServiceMetadata - marshallIso19139", e);
        }

    }

    public JAXBElement<?> unmarshallIso19139(String xmlFile) throws MotuMarshallException {
        Source srcFile = new StreamSource(xmlFile);

        return unmarshallIso19139(srcFile);
    }

    public JAXBElement<?> unmarshallIso19139(Source xmlSource) throws MotuMarshallException {

        if (ServiceMetadata.unmarshallerIso19139 == null) {
            return null;
        }
        JAXBElement<?> element = null;
        try {
            synchronized (ServiceMetadata.unmarshallerIso19139) {

                element = (JAXBElement<?>) ServiceMetadata.unmarshallerIso19139.unmarshal(xmlSource);
            }
        } catch (JAXBException e) {
            throw new MotuMarshallException("Error in ServiceMetadata - unmarshallIso19139", e);
        }

        return element;

    }

    public JAXBElement<?> unmarshallIso19139(InputStream xmlSource) throws MotuMarshallException {

        if (ServiceMetadata.unmarshallerIso19139 == null) {
            return null;
        }
        JAXBElement<?> element = null;
        try {
            synchronized (ServiceMetadata.unmarshallerIso19139) {

                element = (JAXBElement<?>) ServiceMetadata.unmarshallerIso19139.unmarshal(xmlSource);
            }
        } catch (JAXBException e) {
            throw new MotuMarshallException("Error in ServiceMetadata - unmarshallIso19139", e);
        }

        return element;

    }

    public List<String> validateServiceMetadataFromString(String iso19139Schema,
                                                          String localIso19139SchemaPath,
                                                          String localIso19139RootSchemaRelPath,
                                                          String xmlTemplate) throws FileSystemException, MotuException {

        String[] inSchema = getServiceMetadataSchemaAsString(iso19139Schema, localIso19139SchemaPath, localIso19139RootSchemaRelPath);
        if (inSchema == null) {
            throw new MotuException("ERROR in validateServiceMetadata - No schema(s) found.");
        }

        XMLErrorHandler errorHandler = XMLUtils.validateXML(inSchema, xmlTemplate);

        if (errorHandler == null) {
            throw new MotuException("ERROR in Organiser.validateMotuConfig - Motu configuration schema : XMLErrorHandler is null");
        }
        return errorHandler.getErrors();
    }

    public String[] getServiceMetadataSchemaAsString(String schemaPath, String localIso19139SchemaPath, String localIso19139RootSchemaRelPath)
            throws MotuException, FileSystemException {

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

        return inS;
    }

    public JAXBElement<?> dom4jToJaxb(Document document) throws MotuExceptionBase {
        InputStream inputStream = XMLUtils.dom4jToIntputStream(document, ServiceMetadata.ISO19139_SHEMA_PACK_NAME);

        JAXBElement<?> jaxbElement = unmarshallIso19139(inputStream);

        return jaxbElement;
    }

    public void getOperations(String xmlFile, Collection<SVOperationMetadataType> listOperation) throws MotuMarshallException {
        Source srcFile = new StreamSource(xmlFile);
        getOperations(srcFile, listOperation);

    }

    public void getOperations(URL xmlUrl, Collection<SVOperationMetadataType> listOperation) throws MotuMarshallException {
        getOperations(xmlUrl.getPath(), listOperation);

    }

    public void getOperations(Source xmlFile, Collection<SVOperationMetadataType> listOperation) throws MotuMarshallException {

        JAXBElement<?> element = unmarshallIso19139(xmlFile);
        getOperations(element, listOperation);

    }

    public void getOperations(Document document, Collection<SVOperationMetadataType> listOperation) throws MotuExceptionBase {
        JAXBElement<?> element = dom4jToJaxb(document);
        getOperations(element, listOperation);

    }

    public void getOperations(JAXBElement<?> root, Collection<SVOperationMetadataType> listOperation) {
        SVServiceIdentificationType serviceIdentificationType = (SVServiceIdentificationType) root.getValue();
        getOperations(serviceIdentificationType, listOperation);

    }

    public void getOperations(SVServiceIdentificationType serviceIdentificationType, Collection<SVOperationMetadataType> listOperation) {
        // TODO add source code
        List<SVOperationMetadataPropertyType> operationMetadataPropertyTypeList = serviceIdentificationType.getContainsOperations();
        getOperations(operationMetadataPropertyTypeList, listOperation);
    }

    public void getOperations(Collection<SVOperationMetadataPropertyType> operationMetadataPropertyTypeList,
                              Collection<SVOperationMetadataType> listOperation) {

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

    }

    public void getOperations(String xmlFile, DirectedGraph<OperationMetadata, DefaultEdge> directedGraph) throws MotuMarshallException {
        Source srcFile = new StreamSource(xmlFile);
        getOperations(srcFile, directedGraph);

    }

    public void getOperations(URL xmlUrl, DirectedGraph<OperationMetadata, DefaultEdge> directedGraph) throws MotuMarshallException {
        getOperations(xmlUrl.getPath(), directedGraph);

    }

    public void getOperations(Source xmlFile, DirectedGraph<OperationMetadata, DefaultEdge> directedGraph) throws MotuMarshallException {

        JAXBElement<?> element = unmarshallIso19139(xmlFile);
        getOperations(element, directedGraph);

    }

    public void getOperations(Document document, DirectedGraph<OperationMetadata, DefaultEdge> directedGraph) throws MotuExceptionBase {
        JAXBElement<?> element = dom4jToJaxb(document);
        getOperations(element, directedGraph);

    }

    public void getOperations(JAXBElement<?> root, DirectedGraph<OperationMetadata, DefaultEdge> directedGraph) {
        SVServiceIdentificationType serviceIdentificationType = (SVServiceIdentificationType) root.getValue();
        getOperations(serviceIdentificationType, directedGraph);

    }

    public void getOperations(SVServiceIdentificationType serviceIdentificationType, DirectedGraph<OperationMetadata, DefaultEdge> directedGraph) {
        // TODO add source code
        List<SVOperationMetadataPropertyType> operationMetadataPropertyTypeList = serviceIdentificationType.getContainsOperations();
        getOperations(operationMetadataPropertyTypeList, directedGraph);
    }

    public void getOperations(Collection<SVOperationMetadataPropertyType> operationMetadataPropertyTypeList,
                              DirectedGraph<OperationMetadata, DefaultEdge> directedGraph) {
        getOperations(operationMetadataPropertyTypeList, directedGraph, null);

    }

    public OperationMetadata findVertex(Graph<OperationMetadata, DefaultEdge> graph, OperationMetadata obj) {
        Set<OperationMetadata> set = graph.vertexSet();

        for (OperationMetadata o : set) {
            if (o.equals(obj)) {
                return o;
            }
        }
        return null;
    }

    public void getOperations(Collection<SVOperationMetadataPropertyType> operationMetadataPropertyTypeList,
                              DirectedGraph<OperationMetadata, DefaultEdge> directedGraph,
                              OperationMetadata parent) {

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
                    directedGraph.addEdge(parent, operationMetadata);

                }
            }

            getOperations(operationMetadataType.getDependsOn(), directedGraph, operationMetadata);
        }

    }

    public void getOperationsNameUnique(Collection<SVOperationMetadataType> listOperation, Collection<String> listOperationName) {
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

    }

    public void getOperationsInvocationNameUnique(Collection<SVOperationMetadataType> listOperation, Collection<String> listInvocationName) {
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

    }

    public void getOperationsUnique(Collection<SVOperationMetadataType> listOperation, Collection<SVOperationMetadataType> listOperationUnique) {
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

    }

    public static boolean isSourceOperation(DirectedGraph<OperationMetadata, DefaultEdge> directedGraph, OperationMetadata op) {
        DirectedNeighborIndex<OperationMetadata, DefaultEdge> ni = new DirectedNeighborIndex<OperationMetadata, DefaultEdge>(directedGraph);
        if (directedGraph == null) {
            return false;

        }
        if (op == null) {
            return false;

        }
        if (ni.predecessorsOf(op).isEmpty()) {
            return true;
        }

        return false;

    }

    public static boolean isSinkOperation(DirectedGraph<OperationMetadata, DefaultEdge> directedGraph, OperationMetadata op) {
        DirectedNeighborIndex<OperationMetadata, DefaultEdge> ni = new DirectedNeighborIndex<OperationMetadata, DefaultEdge>(directedGraph);
        if (directedGraph == null) {
            return false;

        }
        if (op == null) {
            return false;

        }
        if (ni.successorsOf(op).isEmpty()) {
            return true;
        }

        return false;

    }

    public static void getSourceOperations(DirectedGraph<OperationMetadata, DefaultEdge> directedGraph, Collection<OperationMetadata> sourceOperations) {
        if (directedGraph == null) {
            return;

        }
        if (sourceOperations == null) {
            return;

        }
        for (OperationMetadata v : directedGraph.vertexSet()) {
            if (ServiceMetadata.isSourceOperation(directedGraph, v)) {
                sourceOperations.add(v);
            }
        }

    }

    public static void getSinkOperations(DirectedGraph<OperationMetadata, DefaultEdge> directedGraph, Collection<OperationMetadata> sinkOperations) {
        if (directedGraph == null) {
            return;

        }
        if (sinkOperations == null) {
            return;

        }
        for (OperationMetadata v : directedGraph.vertexSet()) {
            if (ServiceMetadata.isSinkOperation(directedGraph, v)) {
                sinkOperations.add(v);
            }
        }
    }

    public static void dump(Collection<SVOperationMetadataType> listOperation) {

        if (listOperation == null) {
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
    }

    public static void dump(JAXBElement<?> element) {
        SVServiceIdentificationType serviceIdentificationType = (SVServiceIdentificationType) element.getValue();
        // serviceIdentificationType = (SVServiceIdentificationType) unmarshaller.unmarshal(in);
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
    }

}
