package fr.cls.atoll.motu.processor.wps;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.jaxb.JAXBWriter;
import org.isotc211.iso19139.d_2006_05_04.gmd.CIOnlineResourcePropertyType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVOperationMetadataPropertyType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVOperationMetadataType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVParameterPropertyType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVParameterType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVServiceIdentificationType;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.DirectedNeighborIndex;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedSubgraph;
import org.xml.sax.SAXException;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuMarshallException;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.atoll.motu.library.xml.XMLErrorHandler;
import fr.cls.atoll.motu.library.xml.XMLUtils;
import fr.cls.atoll.motu.processor.ant.ServiceMetadataBuilder;
import fr.cls.atoll.motu.processor.iso19139.OperationMetadata;
import fr.cls.atoll.motu.processor.iso19139.ServiceMetadata;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.12 $ - $Date: 2009-09-16 14:22:29 $
 */
public class TestServiceMetadata {

    // public class GetResource {
    //
    // protected String name;
    //        
    // public URL url;
    //        
    // public File file;
    //        
    // public GetResource(String name) {
    // this.name = name;
    // findResource();
    // }
    //        
    // public void findResource() {
    // //first see if the resource is a plain file
    // File f = new File(name);
    // if(f.exists()) {
    // file = f;
    // try {
    // url = f.toURI().toURL();
    // } catch (MalformedURLException e) {
    // System.err.println("Could not crate URL from path: "+f+"\n "+e);
    // }
    // return;
    // }
    //          
    // //search for the resource on the classpath
    //          
    // //get the default class/resource loader
    // ClassLoader cl = getClass().getClassLoader();
    // url = cl.getResource(name);
    // if(url != null) {
    // file = new File( url.getFile() );
    // }
    // }
    //        
    // public String toString() {
    // String str = "Resource name:\t"+name+"\n";
    // str += "File:\t\t"+file+"\n";
    // str += "URL:\t\t"+url+"\n";
    // return str;
    // }
    //    
    //    
    // }
    /**
     * .
     * 
     * @param args
     */
    public static void main(String[] args) {

        // System.setProperty("proxyHost", "proxy.cls.fr"); // adresse IP
        // System.setProperty("proxyPort", "8080");
        // System.setProperty("socksProxyHost", "proxy.cls.fr");
        // System.setProperty("socksProxyPort", "1080");
        // Authenticator.setDefault(new MyAuthenticator());

        // testLoadGeomatysServiceMetadata();
        //testLoadOGCServiceMetadata();
        testServiceMetadataBuilder();
        //testdom4j();
        //testIso19139Operations();

        // try {
        // getServiceMetadataSchemaAsString();
        // } catch (MotuException e1) {
        // // TODO Auto-generated catch block
        // e1.printStackTrace();
        // }
        //        

//        try {
//            Organizer.removeVFSSystemManager();
//        } catch (MotuException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }

    }

    public static void testLoadGeomatysServiceMetadata() {
//
//        InputStream in = null;
//        try {
//            in = Organizer.getUriAsInputStream("J:/dev/atoll-v2/atoll-motu/atoll-motu-processor/src/test/resources/xml/TestServiceMetadata.xml");
//        } catch (MotuException e1) {
//            // TODO Auto-generated catch block
//            e1.printStackTrace();
//        }
//        JAXBContext jc = null;
//        ServiceIdentification serviceIdentification = null;
//        try {
//            jc = JAXBContext.newInstance("org.geotools.service");
//            Unmarshaller unmarshaller = jc.createUnmarshaller();
//            JAXBElement<?> element = (JAXBElement<?>) unmarshaller.unmarshal(in);
//            serviceIdentification = (ServiceIdentification) element.getValue();
//            System.out.println(serviceIdentification.toString());
//
//        } catch (JAXBException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        Collection<OperationMetadata> operationMetadataPropertyTypeList = serviceIdentification.getContainsOperations();
//
//        for (OperationMetadata operationMetadata : operationMetadataPropertyTypeList) {
//
//            System.out.println(operationMetadata.getOperationName());
//            System.out.println(operationMetadata.getInvocationName());
//            System.out.println(operationMetadata.getOperationDescription());
//
//        }
//
//        System.out.println("End testLoadOGCServiceMetadata");

    }

    public static void testLoadOGCServiceMetadata() {
        // String xmlFile =
        // "J:/dev/atoll-v2/atoll-motu/atoll-motu-processor/src/test/resources/xml/TestServiceMetadata.xml";
        String xmlFile = "C:/Documents and Settings/dearith/Mes documents/Atoll/SchemaIso/TestServiceMetadataOK.xml";

        String schemaPath = "schema/iso19139";

        try {
            List<String> errors = validateServiceMetadataFromString(xmlFile, schemaPath);
            if (errors.size() > 0) {
                StringBuffer stringBuffer = new StringBuffer();
                for (String str : errors) {
                    stringBuffer.append(str);
                    stringBuffer.append("\n");
                }
                throw new MotuException(String.format("ERROR - XML file '%s' is not valid - See errors below:\n%s", xmlFile, stringBuffer.toString()));
            } else {
                System.out.println(String.format("XML file '%s' is valid", xmlFile));
            }

        } catch (MotuException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        InputStream in = null;
        try {
            in = Organizer.getUriAsInputStream(xmlFile);
        } catch (MotuException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        JAXBContext jc = null;
        try {
            // jc = JAXBContext.newInstance("org.isotc211.iso19139.d_2006_05_04.srv");
            //jc = JAXBContext.newInstance("org.isotc211.iso19139.d_2006_05_04.srv");
            jc = JAXBContext.newInstance(new Class[] { org.isotc211.iso19139.d_2006_05_04.srv.ObjectFactory.class });
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            Source srcFile = new StreamSource(xmlFile);
            JAXBElement<?> element = (JAXBElement<?>) unmarshaller.unmarshal(srcFile);
            // JAXBElement<?> element = (JAXBElement<?>) unmarshaller.unmarshal(in);
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
            FileWriter writer = new FileWriter("c:/tempVFS/test.xml");

            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(element, writer);

            writer.flush();
            writer.close();

            System.out.println("End testLoadOGCServiceMetadata");
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static InputStream[] getServiceMetadataSchemaAsInputStream() throws MotuException {

        List<InputStream> inputStreamList = new ArrayList<InputStream>();

        URL url = Organizer.findResource("schema/iso/srv/srv.xsd");

        // URL url =
        // Organizer.findResource("J:/dev/iso/19139/20070417/schema/src/main/resources/iso/19139/20070417/srv/srv.xsd");
        System.out.println(url);

        // TestServiceMetadata testServiceMetadata = new TestServiceMetadata();
        // TestServiceMetadata.GetResource gr = testServiceMetadata.new
        // GetResource("iso/19139/20070417/srv/srv.xsd");
        // System.out.println(gr);

        // InputStream inputStream =
        // Organizer.getUriAsInputStream("jar://C:/Documents%20and%20Settings/dearith/.m2/repository/org/jvnet/ogc/iso-19139-d_2006_05_04-schema/1.0.0/iso-19139-d_2006_05_04-schema-1.0.0.jar!/iso/19139/20070417/srv/srv.xsd");
        // InputStream inputStream = Organizer.getUriAsInputStream(gr.url.toString());
        // InputStream inputStream = Organizer.getUriAsInputStream(url.toString());
        // InputStream inputStream = Organizer.getUriAsInputStream("schema/iso/srv/srv.xsd");
        // InputStream inputStream =
        // Organizer.getUriAsInputStream("http://opendap.aviso.oceanobs.com/data/ISO_19139/srv/serviceMetadata.xsd");
        InputStream inputStream = Organizer.getUriAsInputStream("http://schemas.opengis.net/iso/19139/20060504/srv/serviceMetadata.xsd");
        inputStreamList.add(inputStream);
        InputStream inputStream2 = Organizer.getUriAsInputStream("http://schemas.opengis.net/iso/19139/20060504/gco/gco.xsd");
        inputStreamList.add(inputStream2);
        // InputStream inputStream =
        // Organizer.getUriAsInputStream("jar:/C:/Documents%20and%20Settings/dearith/.m2/repository/org/jvnet/ogc/iso-19139-d_2006_05_04-schema/1.0.0/iso-19139-d_2006_05_04-schema-1.0.0.jar");

        // byte b[] = new byte[1024];
        //
        // int bytesRead = 0;
        //
        // try {
        // while ((bytesRead = inputStream.read(b)) != -1) {
        // String nextLine = new String(b, 0, bytesRead);
        // System.out.println(nextLine);
        // }
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

        // return Organizer.getUriAsInputStream(gr.file.toString());
        InputStream[] inS = new InputStream[inputStreamList.size()];
        inS = inputStreamList.toArray(inS);
        return inS;
    }

    public static String[] getServiceMetadataSchemaAsString(String schemaPath) throws MotuException {

        List<String> stringList = new ArrayList<String>();
        String localIso19139SchemaPath = "file:///c:/tempVFS/testISO";
        String localIso19139RootSchemaRelPath = "/srv/srv.xsd";
        String localIso19139RootSchemaPath = String.format("%s%s", localIso19139SchemaPath, localIso19139RootSchemaRelPath);

        FileObject dest = Organizer.resolveFile(localIso19139RootSchemaPath);
        boolean hasIso19139asLocalSchema = false;
        try {
            if (dest != null) {
                hasIso19139asLocalSchema = dest.exists();
            }
        } catch (FileSystemException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        if (hasIso19139asLocalSchema) {
            try {
                dest.close();
            } catch (FileSystemException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {

            // URL url = Organizer.findResource("schema/iso/srv/srv.xsd");
            // URL url =
            // Organizer.findResource("J:/dev/iso/19139/20070417/schema/src/main/resources/iso/19139/20070417/srv/srv.xsd");
            // URL url = Organizer.findResource("iso/19139/20070417/srv/srv.xsd");
            URL url = Organizer.findResource(schemaPath);
            System.out.println(url);

            // String[] arr = url.toString().split("!");

            // FileObject jarFile = Organizer.resolveFile(arr[0]);
            FileObject jarFile = Organizer.resolveFile(url.toString());

            // List the children of the Jar file
            FileObject[] children = null;
            try {
                children = jarFile.getChildren();
            } catch (FileSystemException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println("Children of " + jarFile.getName().getURI());
            for (int i = 0; i < children.length; i++) {
                System.out.println(children[i].getName().getBaseName());
            }

            dest = Organizer.resolveFile(localIso19139SchemaPath);
            Organizer.deleteDirectory(dest);

            Organizer.copyFile(jarFile, dest);
        }

        //stringList.add(url.toString());
        // stringList.add("J:/dev/iso/19139/20070417/schema/src/main/resources/iso/19139/20070417/srv/srv.xsd");
        stringList.add(localIso19139RootSchemaPath);
        // stringList.add("C:/Documents and Settings/dearith/Mes documents/Atoll/SchemaIso/srv/serviceMetadata.xsd");
        String[] inS = new String[stringList.size()];
        inS = stringList.toArray(inS);
        return inS;
    }

    public static List<String> validateServiceMetadataFromStream() throws MotuException {

        InputStream[] inSchema = getServiceMetadataSchemaAsInputStream();
        if (inSchema == null) {
            throw new MotuException(String.format("ERROR in validateServiceMetadata - Motu configuration schema ('%s') not found:", Organizer
                    .getMotuConfigSchemaName()));
        }
        InputStream inXml = Organizer
                .getUriAsInputStream("J:/dev/atoll-v2/atoll-motu/atoll-motu-processor/src/test/resources/xml/TestServiceMetadata.xml");
        if (inXml == null) {
            throw new MotuException(String.format("ERROR in validateServiceMetadata - Motu configuration xml ('%s') not found:", Organizer
                    .getMotuConfigXmlName()));
        }

        XMLErrorHandler errorHandler = XMLUtils.validateXML(inSchema, inXml);

        if (errorHandler == null) {
            throw new MotuException("ERROR in Organiser.validateMotuConfig - Motu configuration schema : XMLErrorHandler is null");
        }
        return errorHandler.getErrors();

    }

    public static List<String> validateServiceMetadataFromString(String xmlFile, String schemaPath) throws MotuException {

        String[] inSchema = getServiceMetadataSchemaAsString(schemaPath);
        if (inSchema == null) {
            throw new MotuException(String.format("ERROR in validateServiceMetadata - Motu configuration schema ('%s') not found:", Organizer
                    .getMotuConfigSchemaName()));
        }
        // InputStream inXml =
        // Organizer.getUriAsInputStream("J:/dev/atoll-v2/atoll-motu/atoll-motu-processor/src/test/resources/xml/TestServiceMetadata.xml");
        // InputStream inXml = Organizer.getUriAsInputStream(xmlFile);
        // if (inXml == null) {
        // throw new
        // MotuException(String.format("ERROR in validateServiceMetadata - Motu configuration xml ('%s') not found:",
        // Organizer
        // .getMotuConfigXmlName()));
        // }

        // XMLErrorHandler errorHandler = XMLUtils.validateXML(inSchema, inXml);
        XMLErrorHandler errorHandler = XMLUtils.validateXML(inSchema, xmlFile);

        if (errorHandler == null) {
            throw new MotuException("ERROR in Organiser.validateMotuConfig - Motu configuration schema : XMLErrorHandler is null");
        }
        return errorHandler.getErrors();

    }

    public static void DetectProxy() throws Exception {
        System.setProperty("proxyHost", "proxy.cls.fr"); // adresse IP
        System.setProperty("proxyPort", "8080");
        System.setProperty("socksProxyHost", "proxy.cls.fr");
        // System.setProperty("http.proxyHost", "http-proxy.ece.fr");
        // System.setProperty("http.proxyPort", "3128");
        // System.setProperty("java.net.useSystemProxies", "true");
        // List<Proxy> proxyList = ProxySelector.getDefault().select(new URI("http://schemas.opengis.net"));
        List<Proxy> proxyList = ProxySelector.getDefault().select(new URI("http://opendap.aviso.oceanobs.com"));

        for (Proxy proxy : proxyList) {
            System.out.println("Proxy type : " + proxy.type());
            InetSocketAddress addr = (InetSocketAddress) proxy.address();
            if (addr == null) {
                System.out.println("DIRECT CONXN");
            } else {
                System.out.println("Proxy hostname : " + addr.getHostName() + ":" + addr.getPort());
            }
        }
    }

    public static void testServiceMetadataBuilder() {
        
        ServiceMetadataBuilder serviceMetadataBuilder = new ServiceMetadataBuilder();
        URL url = null;
        try {
            url = Organizer.findResource("src/main/resources/fmpp/src/base/serviceMetadataTemplateOpendapBase.xml");
        } catch (MotuException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        serviceMetadataBuilder.setXmlTemplate(url.getPath());
        serviceMetadataBuilder.setTempPath("file:///c:/tempVFS");
        serviceMetadataBuilder.setOutputXml("file:///J:/dev/atoll-v2/atoll-motu/atoll-motu-processor/src/main/resources/fmpp/src/ServiceMetadataOpendap.xml");
        serviceMetadataBuilder.setValidate(false);
        serviceMetadataBuilder.setExpand(false);
        serviceMetadataBuilder.setFmpp(false);
        serviceMetadataBuilder.setValidateOutput("src/main/resources/fmpp/out");
        
        
        serviceMetadataBuilder.execute();
    }
    
    public static JAXBElement<?> testdom4j()  {
        
        URL url = null;
        JAXBElement<?> jaxbElement = null;
        try {
//          url = new URL("file:///c:/Documents and Settings/dearith/Mes documents/Atoll/SchemaIso/TestServiceMetadataOK.xml");
            url = Organizer.findResource("src/main/resources/fmpp/src/ServiceMetadataOpendap.xml");
            SAXReader reader = new SAXReader();
            Document document = reader.read(url);
            Element root = document.getRootElement();

            // iterate through child elements of root
//            for ( Iterator<Element> i = root.elementIterator(); i.hasNext(); ) {
//                Element element = (Element) i.next();
//                System.out.println(element);
//                // do something
//            }

            JAXBWriter  jaxbWriter = new JAXBWriter(ServiceMetadata.ISO19139_SHEMA_PACK_NAME);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            jaxbWriter.setOutput(byteArrayOutputStream);
            
//            FileWriter fileWriter = new FileWriter("./testdom4j.xml");
//            jaxbWriter.setOutput(fileWriter);
            jaxbWriter.startDocument();
            jaxbWriter.writeElement(root);
            jaxbWriter.endDocument();
//            fileWriter.close();
            
            ServiceMetadata serviceMetadata = new ServiceMetadata();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            jaxbElement = serviceMetadata.unmarshallIso19139(byteArrayInputStream);
            
            ServiceMetadata.dump(jaxbElement);
            
            
            
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MotuException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MotuMarshallException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jaxbElement;

    
    }
    public static void testIso19139Operations () {
//        
//        JAXBElement<?> element = testdom4j();
//        if (element == null) {
//            return;
//        }
        
        try {
            ServiceMetadata serviceMetadata = new ServiceMetadata();
            URL url = null;
            Set<SVOperationMetadataType> listOperation = new HashSet<SVOperationMetadataType>();
//          url = new URL("file:///c:/Documents and Settings/dearith/Mes documents/Atoll/SchemaIso/TestServiceMetadataOK.xml");
            url = Organizer.findResource("src/main/resources/fmpp/src/ServiceMetadataOpendap.xml");
            serviceMetadata.getOperations(url, listOperation);
            ServiceMetadata.dump(listOperation);
            List<String> listOperationNamesUnique = new ArrayList<String>();

            
            serviceMetadata.getOperationsNameUnique(listOperation, listOperationNamesUnique);
            
            
            for (String name  : listOperationNamesUnique) {

                System.out.println("==================");
                if (name == null) {
                    continue;
                }
                System.out.println(name);
            }
            listOperationNamesUnique.clear();
            serviceMetadata.getOperationsInvocationNameUnique(listOperation, listOperationNamesUnique);
            for (String name  : listOperationNamesUnique) {

                System.out.println("------+++++==================");
                if (name == null) {
                    continue;
                }
                System.out.println(name);
            }

            List<SVOperationMetadataType> listOperationUnique = new ArrayList<SVOperationMetadataType>();

            
            serviceMetadata.getOperationsUnique(listOperation, listOperationUnique);
            for (SVOperationMetadataType operationMetadataType : listOperationUnique) {

                System.out.println("---------------------------------------------");
                if (operationMetadataType == null) {
                    continue;
                }
                System.out.println(operationMetadataType.getOperationName().getCharacterString().getValue());
            }
            
            DirectedGraph<OperationMetadata, DefaultEdge> directedGraph = new DefaultDirectedGraph<OperationMetadata, DefaultEdge>(DefaultEdge.class);
            serviceMetadata.getOperations(url, directedGraph);

            StrongConnectivityInspector<OperationMetadata, DefaultEdge> sci = new StrongConnectivityInspector<OperationMetadata, DefaultEdge>(directedGraph);
            List<DirectedSubgraph<OperationMetadata, DefaultEdge>> stronglyConnectedSubgraphs = sci.stronglyConnectedSubgraphs();
            sci.stronglyConnectedSets();
            System.out.println(sci.isStronglyConnected());
            
            // prints the strongly connected components
            System.out.println("Strongly connected components:");
            for (int i = 0; i < stronglyConnectedSubgraphs.size(); i++) {
                System.out.println(stronglyConnectedSubgraphs.get(i));
            }
            System.out.println();
            
            System.out.println (directedGraph.edgeSet());
            
            ConnectivityInspector<OperationMetadata, DefaultEdge> ci = new ConnectivityInspector<OperationMetadata, DefaultEdge>(directedGraph);
            System.out.println (ci.isGraphConnected());
            
            DirectedNeighborIndex<OperationMetadata, DefaultEdge> ni = new DirectedNeighborIndex<OperationMetadata, DefaultEdge>(directedGraph);

            List<OperationMetadata> sourceOperations = new ArrayList<OperationMetadata>();
            List<OperationMetadata> sinkOperations = new ArrayList<OperationMetadata>();
            
            ServiceMetadata.getSourceOperations(directedGraph, sourceOperations);
            ServiceMetadata.getSinkOperations(directedGraph, sinkOperations);

            System.out.println("%%%%%%%% SOURCE %%%%%%%%%%%%");
            System.out.println(sourceOperations);
            System.out.println("%%%%%%%% SINK %%%%%%%%%%%%");
            System.out.println(sinkOperations);

            for (OperationMetadata source : sourceOperations) {
                System.out.print("%%%%%%%% PATHS FROM  %%%%%%%%%%%%");
                System.out.println(source);
                    KShortestPaths<OperationMetadata, DefaultEdge> paths = ServiceMetadata.getOperationPaths(directedGraph, source);
                    
                    for (OperationMetadata sink : sinkOperations) {
                        System.out.print(" %%%%%%%%%%%% TO ");
                        System.out.println(sink);
                        List<GraphPath<OperationMetadata, DefaultEdge>> listPath = ServiceMetadata.getOperationPaths(paths, sink);
                        for (GraphPath<OperationMetadata, DefaultEdge> gp : listPath) {
                        System.out.println(gp.getEdgeList());
                        }
                    }
                    
            }
            
            
//            // Prints the shortest path from vertex i to vertex c. This certainly
//            // exists for our particular directed graph.
//            System.out.println("Shortest path from i to c:");
//            List path = DijkstraShortestPath.findPathBetween(directedGraph, "i", "c");
//            System.out.println(path + "\n");

//            // Prints the shortest path from vertex c to vertex i. This path does
//            // NOT exist for our particular directed graph. Hence the path is
//            // empty and the variable "path" must be null.
//            System.out.println("Shortest path from c to i:");
//            path = DijkstraShortestPath.findPathBetween(directedGraph, "c", "i");
//            System.out.println(path);
            
            

        } catch (MotuException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MotuMarshallException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
}
