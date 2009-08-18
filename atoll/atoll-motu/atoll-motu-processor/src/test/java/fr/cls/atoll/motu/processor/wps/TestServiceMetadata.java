package fr.cls.atoll.motu.processor.wps;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.JarResourceLoader;
import org.isotc211.iso19139.d_2007_04_17.gmd.CIOnlineResourcePropertyType;
import org.isotc211.iso19139.d_2007_04_17.srv.SVOperationMetadataPropertyType;
import org.isotc211.iso19139.d_2007_04_17.srv.SVOperationMetadataType;
import org.isotc211.iso19139.d_2007_04_17.srv.SVParameterPropertyType;
import org.isotc211.iso19139.d_2007_04_17.srv.SVParameterType;
import org.isotc211.iso19139.d_2007_04_17.srv.SVServiceIdentificationType;
import org.opengis.service.OperationMetadata;
import org.opengis.service.ServiceIdentification;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.atoll.motu.library.xml.XMLErrorHandler;
import fr.cls.atoll.motu.library.xml.XMLUtils;

/**
 * <br><br>Copyright : Copyright (c) 2009.
 * <br><br>Société : CLS (Collecte Localisation Satellites)
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-08-18 15:12:19 $
 */
public class TestServiceMetadata {

    /**
     * .
     * @param args
     */
    public static void main(String[] args) {

        //testLoadGeomatysServiceMetadata();
        //testLoadOGCServiceMetadata();
        try {
            Organizer.getMotuConfigSchema();
            getServiceMetadataSchema();
        } catch (MotuException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
            JAXBElement<?> element =  (JAXBElement<?>) unmarshaller.unmarshal(in);
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

        InputStream in = null;
        try {
            in = Organizer.getUriAsInputStream("J:/dev/atoll-v2/atoll-motu/atoll-motu-processor/src/test/resources/xml/TestServiceMetadata.xml");
        } catch (MotuException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        JAXBContext jc = null;
        SVServiceIdentificationType serviceIdentificationType = null;
        try {
//            jc = JAXBContext.newInstance("org.isotc211.iso19139.d_2006_05_04.srv");
            jc = JAXBContext.newInstance("org.isotc211.iso19139.d_2007_04_17.srv");
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            JAXBElement<?> element =  (JAXBElement<?>) unmarshaller.unmarshal(in);
            serviceIdentificationType = (SVServiceIdentificationType) element.getValue();
//            serviceIdentificationType =  (SVServiceIdentificationType) unmarshaller.unmarshal(in);
            System.out.println(serviceIdentificationType.toString());
            
            List<SVOperationMetadataPropertyType> operationMetadataPropertyTypeList = serviceIdentificationType.getContainsOperations();
            
            for (SVOperationMetadataPropertyType operationMetadataPropertyType : operationMetadataPropertyTypeList) {
                
                SVOperationMetadataType operationMetadataType = operationMetadataPropertyType.getSVOperationMetadata(); 
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
                    
                    System.out.println(parameterType.getName().getAName().getCharacterString().getValue());
                }
                


            }
            System.out.println("End testLoadOGCServiceMetadata");
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        
    }
    public static InputStream getServiceMetadataSchema() throws MotuException {

//        JarResourceLoader jar = new JarResourceLoader();
//        System.out.println(jar.toString());
//        try {
//            jar.getResourceStream("iso/19139/20070417/srv/srv.xsd");
//        } catch (ResourceNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }

        
        String configSchema = "file:srv.xsd";
        return Organizer.getUriAsInputStream(configSchema);
    }

    public static List<String> validateServiceMetadata() throws MotuException {

        InputStream inSchema = Organizer.getMotuConfigSchema();
        if (inSchema == null) {
            throw new MotuException(String.format("ERROR in Organiser.validateMotuConfig - Motu configuration schema ('%s') not found:", Organizer
                    .getMotuConfigSchemaName()));
        }
        InputStream inXml = Organizer.getMotuConfigXml();
        if (inXml == null) {
            throw new MotuException(String.format("ERROR in Organiser.validateMotuConfig - Motu configuration xml ('%s') not found:", Organizer
                    .getMotuConfigXmlName()));
        }

        XMLErrorHandler errorHandler = XMLUtils.validateXML(inSchema, inXml);

        if (errorHandler == null) {
            throw new MotuException("ERROR in Organiser.validateMotuConfig - Motu configuration schema : XMLErrorHandler is null");
        }
        return errorHandler.getErrors();

    }

}
