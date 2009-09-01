package fr.cls.atoll.motu.processor.ant;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.isotc211.iso19139.d_2006_05_04.gmd.CIOnlineResourcePropertyType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVOperationMetadataPropertyType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVOperationMetadataType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVParameterPropertyType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVParameterType;
import org.isotc211.iso19139.d_2006_05_04.srv.SVServiceIdentificationType;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuExceptionBase;
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
 * @version $Revision: 1.1 $ - $Date: 2009-09-01 14:24:39 $
 */
public class ServiceMetadataBuilder extends Task {

    private String outputXml = "ServiceMetadata.xml";

    private final static String ISO19139 = "iso19139";
    private String xmlTemplate;
    private String iso19139Schema = "schema/iso19139";
    private String tempPath;
    private String localIso19139SchemaPath;
    private String localIso19139RootSchemaRelPath = "/srv/srv.xsd";

    private String destSrcPath;
    private String fmppSrc;

    private boolean validate = true;
    private JAXBContext jc = null;

    public void setXmlTemplate(String xmlTemplate) {
        this.xmlTemplate = xmlTemplate;
    }

    public void setIso19139Schema(String iso19139Schema) {
        this.iso19139Schema = iso19139Schema;
    }

    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
    }
    
    public void setOutputXml(String outputXml) {
        this.outputXml = outputXml;
    }

//    public void setLocalIso19139SchemaPath(String localIso19139SchemaPath) {
//        this.localIso19139SchemaPath = localIso19139SchemaPath;
//    }

    public void setLocalIso19139RootSchemaRelPath(String localIso19139RootSchemaRelPath) {
        this.localIso19139RootSchemaRelPath = localIso19139RootSchemaRelPath;
    }

    public void setDestSrcPath(String destSrcPath) {
        this.destSrcPath = destSrcPath;
    }

    public void setFmppSrc(String fmppSrc) {
        this.fmppSrc = fmppSrc;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }

//    private String msg;
//
//    // The setter for the "message" attribute
//    public void setMessage(String msg) {
//        this.msg = msg;
//    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws BuildException {
        //System.out.println(msg);

        try {
            localIso19139SchemaPath = String.format("%s/%s", tempPath, ISO19139);
            System.out.println(localIso19139SchemaPath);

            jc = JAXBContext.newInstance(new Class[] { org.isotc211.iso19139.d_2006_05_04.srv.ObjectFactory.class });

            if (validate) {
                System.out.println(validate);
                List<String> errors = validateServiceMetadataFromString();
                if (errors.size() > 0) {
                    StringBuffer stringBuffer = new StringBuffer();
                    for (String str : errors) {
                        stringBuffer.append(str);
                        stringBuffer.append("\n");
                    }
                    throw new BuildException(String.format("ERROR - XML file '%s' is not valid - See errors below:\n%s", xmlTemplate, stringBuffer
                            .toString()));
                } else {
                    System.out.println(String.format("XML file '%s' is valid", xmlTemplate));
                }

            }

            System.out.println("unmarshallXmlTemplate");

            JAXBElement<?> element = unmarshallXmlTemplate();

            marshallXmlTemplate(element);

        } catch (MotuExceptionBase e) {
            throw new BuildException(e.notifyException(), e);
        } catch (Exception e) {
            throw new BuildException(e);
        }

    }

    private List<String> validateServiceMetadataFromString() throws FileSystemException, MotuException {

        String[] inSchema = getServiceMetadataSchemaAsString(iso19139Schema);
        if (inSchema == null) {
            throw new MotuException("ERROR in validateServiceMetadata - No schema(s) found.");
        }
        System.out.println(inSchema.toString());
        System.out.println(xmlTemplate);
        
        XMLErrorHandler errorHandler = XMLUtils.validateXML(inSchema, xmlTemplate);

        if (errorHandler == null) {
            throw new MotuException("ERROR in Organiser.validateMotuConfig - Motu configuration schema : XMLErrorHandler is null");
        }
        return errorHandler.getErrors();

    }

    public String[] getServiceMetadataSchemaAsString(String schemaPath) throws MotuException, FileSystemException {
        System.out.println("getServiceMetadataSchemaAsString");

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
            // System.out.println(url);

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
//            FileObject old = Organizer.resolveFile(localIso19139SchemaPath + ".old");
//
//            System.out.println("Dest exists ?");
//            System.out.println(dest.toString());
//
//            System.out.println(old.toString());
//            System.out.println("old");
//            Organizer.deleteDirectory(old);
//            System.out.println("Delete old");
//            Organizer.copyFile(dest, old);
//            System.out.println("Copy to old");
            Organizer.deleteDirectory(dest);
            System.out.println("delete dest");

            Organizer.copyFile(jarFile, dest);
            System.out.println("copy to dest");
        }

        stringList.add(localIso19139RootSchemaPath);
        String[] inS = new String[stringList.size()];
        inS = stringList.toArray(inS);
//        System.out.println("exit getServiceMetadataSchemaAsString");

        return inS;
    }

    public JAXBElement<?> unmarshallXmlTemplate() throws MotuException, FileSystemException, JAXBException {

        SVServiceIdentificationType serviceIdentificationType = null;
        System.out.println("unmarshall");
        
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        Source srcFile = new StreamSource(xmlTemplate);

        JAXBElement<?> element = (JAXBElement<?>) unmarshaller.unmarshal(srcFile);

        serviceIdentificationType = (SVServiceIdentificationType) element.getValue();
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

        return element;
    }

    public void marshallXmlTemplate(JAXBElement<?> element) throws MotuException, JAXBException, IOException, URISyntaxException {
        //FileWriter writer = new FileWriter(String.format("%s/%s/%s.xml", tempPath, SERVICE_METADATA, SERVICE_METADATA));
        FileObject fileobj = Organizer.resolveFile(String.format("%s/%s", tempPath, outputXml));
        System.out.println("ezrezrezrezr");
        //URI uri = new URI(String.format("%s/%s/%s.xml", tempPath, SERVICE_METADATA, SERVICE_METADATA));
        System.out.println("ezrezrezrezr 2");
        //file.createNewFile();
        fileobj.createFile();
//        FileWriter writer = new FileWriter(fileobj.getName().getURI());
        System.out.println("ezrezrezrezr 3");
        

        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        //marshaller.marshal(element, writer);
        
        marshaller.marshal(element, fileobj.getContent().getOutputStream());
        System.out.println("ezrezrezrezr 3");

        //writer.flush();
        //writer.close();
        

    }

}
