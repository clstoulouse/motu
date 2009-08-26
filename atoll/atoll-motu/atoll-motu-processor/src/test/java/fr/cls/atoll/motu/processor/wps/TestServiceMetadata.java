package fr.cls.atoll.motu.processor.wps;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.isotc211.iso19139.d_2006_05_04.gmd.CIOnlineResourcePropertyType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVOperationMetadataPropertyType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVOperationMetadataType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVParameterPropertyType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVParameterType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVServiceIdentificationType;
import org.opengis.service.OperationMetadata;
import org.opengis.service.ServiceIdentification;

import fr.cls.atoll.motu.library.exception.MotuException;
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
 * @version $Revision: 1.4 $ - $Date: 2009-08-26 14:57:19 $
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
        testLoadOGCServiceMetadata();

        // try {
        // getServiceMetadataSchemaAsString();
        // } catch (MotuException e1) {
        // // TODO Auto-generated catch block
        // e1.printStackTrace();
        // }
        //        

    }

    public static void testLoadGeomatysServiceMetadata() {

        InputStream in = null;
        try {
            in = Organizer.getUriAsInputStream("J:/dev/atoll-v2/atoll-motu/atoll-motu-processor/src/test/resources/xml/TestServiceMetadata.xml");
        } catch (MotuException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        JAXBContext jc = null;
        ServiceIdentification serviceIdentification = null;
        try {
            jc = JAXBContext.newInstance("org.geotools.service");
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            JAXBElement<?> element = (JAXBElement<?>) unmarshaller.unmarshal(in);
            serviceIdentification = (ServiceIdentification) element.getValue();
            System.out.println(serviceIdentification.toString());

        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Collection<OperationMetadata> operationMetadataPropertyTypeList = serviceIdentification.getContainsOperations();

        for (OperationMetadata operationMetadata : operationMetadataPropertyTypeList) {

            System.out.println(operationMetadata.getOperationName());
            System.out.println(operationMetadata.getInvocationName());
            System.out.println(operationMetadata.getOperationDescription());

        }

        System.out.println("End testLoadOGCServiceMetadata");

    }

    public static void testLoadOGCServiceMetadata() {
        //String xmlFile = "J:/dev/atoll-v2/atoll-motu/atoll-motu-processor/src/test/resources/xml/TestServiceMetadata.xml";
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
        SVServiceIdentificationType serviceIdentificationType = null;
        try {
            // jc = JAXBContext.newInstance("org.isotc211.iso19139.d_2006_05_04.srv");
            jc = JAXBContext.newInstance("org.isotc211.iso19139.d_2006_05_04.srv");
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            Source srcFile = new StreamSource(xmlFile);
            JAXBElement<?> element = (JAXBElement<?>) unmarshaller.unmarshal(srcFile);
            //JAXBElement<?> element = (JAXBElement<?>) unmarshaller.unmarshal(in);
            serviceIdentificationType = (SVServiceIdentificationType) element.getValue();
            // serviceIdentificationType = (SVServiceIdentificationType) unmarshaller.unmarshal(in);
            System.out.println(serviceIdentificationType.toString());

            List<SVOperationMetadataPropertyType> operationMetadataPropertyTypeList = serviceIdentificationType.getContainsOperations();

            for (SVOperationMetadataPropertyType operationMetadataPropertyType : operationMetadataPropertyTypeList) {

                SVOperationMetadataType operationMetadataType = operationMetadataPropertyType.getSVOperationMetadata();
                System.out.println("---------------------------------------------");
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
            System.out.println("End testLoadOGCServiceMetadata");
        } catch (JAXBException e) {
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

        FileObject dest = Organizer.resolveFile("file:c:/tempVFS/testISO");
        Organizer.deleteDirectory(dest);

        Organizer.copyFile(jarFile, dest);

        // stringList.add(url.toString());
        // stringList.add("J:/dev/iso/19139/20070417/schema/src/main/resources/iso/19139/20070417/srv/srv.xsd");
        stringList.add(dest.getName() + "/srv/srv.xsd");
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
//        InputStream inXml = Organizer.getUriAsInputStream(xmlFile);
//        if (inXml == null) {
//            throw new MotuException(String.format("ERROR in validateServiceMetadata - Motu configuration xml ('%s') not found:", Organizer
//                    .getMotuConfigXmlName()));
//        }

        //XMLErrorHandler errorHandler = XMLUtils.validateXML(inSchema, inXml);
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

}
