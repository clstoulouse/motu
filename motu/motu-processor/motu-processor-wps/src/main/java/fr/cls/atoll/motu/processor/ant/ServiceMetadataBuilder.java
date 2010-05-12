package fr.cls.atoll.motu.processor.ant;

import fmpp.tools.AntTask;

import fr.cls.atoll.motu.library.misc.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.processor.iso19139.ServiceMetadata;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

import org.apache.tools.ant.BuildException;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class ServiceMetadataBuilder extends AntTask {

    protected ServiceMetadata serviceMetadata = null;

    protected String outputXml = "ServiceMetadata.xml";

    protected final static String ISO19139 = "iso19139";
    protected String xmlTemplate;
    protected String iso19139Schema = "schema/iso19139";
    protected String tempPath;
    protected String localIso19139SchemaPath;
    protected String localIso19139RootSchemaRelPath = "/srv/srv.xsd";

    // private String destSrcPath;
    // private String fmppSrc;

    protected boolean validate = true;
    protected String validateOutput = "";

    protected boolean expand = true;
    protected boolean fmpp = true;

    protected JAXBContext jc = null;

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

    public void setValidateOutput(String validateOutput) {
        this.validateOutput = validateOutput;
    }

    // public void setLocalIso19139SchemaPath(String localIso19139SchemaPath) {
    // this.localIso19139SchemaPath = localIso19139SchemaPath;
    // }

    public void setLocalIso19139RootSchemaRelPath(String localIso19139RootSchemaRelPath) {
        this.localIso19139RootSchemaRelPath = localIso19139RootSchemaRelPath;
    }

    // public void setDestSrcPath(String destSrcPath) {
    // this.destSrcPath = destSrcPath;
    // }
    //
    // public void setFmppSrc(String fmppSrc) {
    // this.fmppSrc = fmppSrc;
    // }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }

    public void setExpand(boolean expand) {
        this.expand = expand;
    }

    public void setFmpp(boolean fmpp) {
        this.fmpp = fmpp;
    }

    // private String msg;
    //
    // // The setter for the "message" attribute
    // public void setMessage(String msg) {
    // this.msg = msg;
    // }

    /** {@inheritDoc} */
    @Override
    public void execute() throws BuildException {
        // System.out.println(msg);

        try {
            localIso19139SchemaPath = String.format("%s/%s", tempPath, ISO19139);

            jc = JAXBContext.newInstance(new Class[] { org.isotc211.iso19139.d_2006_05_04.srv.ObjectFactory.class });

            if (expand) {
                serviceMetadata = new ServiceMetadata();

                System.out.println(String.format("Step : Expand from template '%s'\n", xmlTemplate));

                if (validate) {
                    System.out.println(String.format("Step : XML validation using schema located in '%s'\n", localIso19139SchemaPath));
                    List<String> errors = serviceMetadata.validateServiceMetadataFromString(iso19139Schema,
                                                                                            localIso19139SchemaPath,
                                                                                            localIso19139RootSchemaRelPath,
                                                                                            xmlTemplate);
                    if (errors.size() > 0) {
                        StringBuffer stringBuffer = new StringBuffer();
                        for (String str : errors) {
                            stringBuffer.append(str);
                            stringBuffer.append("\n");
                        }
                        throw new BuildException(String.format("ERROR - XML file '%s' is not valid - See errors below:\n%s",
                                                               xmlTemplate,
                                                               stringBuffer.toString()));
                    } else {
                        System.out.println(String.format("XML file '%s' is valid\n", xmlTemplate));
                    }

                } else {
                    System.out.println("Step XML validation  is skipped\n");
                }

                System.out.println(String.format("Step : unmarshall XML from '%s'\n", xmlTemplate));
                JAXBElement<?> element = serviceMetadata.unmarshallIso19139(xmlTemplate);

                System.out.println(String.format("Step : marshall XML to '%s'\n", outputXml));
                serviceMetadata.marshallIso19139(element, outputXml);

            } else {
                System.out.println("Step Expand is skipped\n");
            }

            if (fmpp) {
                System.out.println("Step : substitution (fmpp task)\n");
                super.execute();

            }

            validateOutputs();

        } catch (MotuExceptionBase e) {
            throw new BuildException(e.notifyException(), e);
        } catch (Exception e) {
            throw new BuildException(e);
        }

    }

    public void validateOutputs() {

        if (validateOutput.isEmpty()) {
            System.out.println("Step output xml validation is skipped \n");
            return;
        }

        System.out.println("Step : output xml validation\n");

        try {
            URL url = Organizer.findResource(validateOutput);
            File scannerBase = new File(url.getPath());
            List<String> files = new ArrayList<String>();
            Organizer.getFilesAsString(scannerBase, files, false);
            List<String> allErrors = new ArrayList<String>();

            if (serviceMetadata == null) {
                serviceMetadata = new ServiceMetadata();
            }

            for (String file : files) {
                System.out.println(String.format("Step : validation of '%s' using schema located in '%s'\n", file, localIso19139SchemaPath));
                List<String> errors = serviceMetadata.validateServiceMetadataFromString(iso19139Schema,
                                                                                        localIso19139SchemaPath,
                                                                                        localIso19139RootSchemaRelPath,
                                                                                        file);
                if (errors.size() > 0) {
                    StringBuffer stringBuffer = new StringBuffer();
                    for (String str : errors) {
                        stringBuffer.append(str);
                        stringBuffer.append("\n");
                    }
                    allErrors.add(String.format("ERROR - XML file '%s' is not valid - See errors below:\n%s", file, stringBuffer.toString()));
                } else {
                    System.out.println(String.format("XML file '%s' is valid\n", file));
                }

            }
            if (allErrors.size() > 0) {
                StringBuffer stringBuffer = new StringBuffer();
                for (String str : allErrors) {
                    stringBuffer.append(str);
                    stringBuffer.append("\n");
                }
                throw new BuildException(stringBuffer.toString());

            }

        } catch (MotuExceptionBase e) {
            throw new BuildException(String.format("Error during output XML validation:\n%s\n", e.notifyException()), e);

        } catch (Exception e) {
            throw new BuildException("Error during output XML validation", e);
        }

    }

    // private List<String> validateServiceMetadataFromString() throws FileSystemException, MotuException {
    //
    // String[] inSchema = getServiceMetadataSchemaAsString(iso19139Schema);
    // if (inSchema == null) {
    // throw new MotuException("ERROR in validateServiceMetadata - No schema(s) found.");
    // }
    //
    // XMLErrorHandler errorHandler = XMLUtils.validateXML(inSchema, xmlTemplate);
    //
    // if (errorHandler == null) {
    // throw new
    // MotuException("ERROR in Organiser.validateMotuConfig - Motu configuration schema : XMLErrorHandler is null");
    // }
    // return errorHandler.getErrors();
    //
    // }

    // public String[] getServiceMetadataSchemaAsString(String schemaPath) throws MotuException,
    // FileSystemException {
    // // System.out.println("getServiceMetadataSchemaAsString");
    //
    // List<String> stringList = new ArrayList<String>();
    // String localIso19139RootSchemaPath = String.format("%s%s", localIso19139SchemaPath,
    // localIso19139RootSchemaRelPath);
    //
    // FileObject dest = Organizer.resolveFile(localIso19139RootSchemaPath);
    // boolean hasIso19139asLocalSchema = false;
    // if (dest != null) {
    // hasIso19139asLocalSchema = dest.exists();
    // }
    //
    // if (hasIso19139asLocalSchema) {
    // dest.close();
    //
    // } else {
    //
    // URL url = Organizer.findResource(schemaPath);
    // // System.out.println(url);
    //
    // FileObject jarFile = Organizer.resolveFile(url.toString());
    //
    // // List the children of the Jar file
    // // FileObject[] children = null;
    // // try {
    // // children = jarFile.getChildren();
    // // } catch (FileSystemException e) {
    // // // TODO Auto-generated catch block
    // // e.printStackTrace();
    // // }
    // // System.out.println("Children of " + jarFile.getName().getURI());
    // // for (int i = 0; i < children.length; i++) {
    // // System.out.println(children[i].getName().getBaseName());
    // // }
    //
    // dest = Organizer.resolveFile(localIso19139SchemaPath);
    // // FileObject old = Organizer.resolveFile(localIso19139SchemaPath + ".old");
    // //
    // // System.out.println("Dest exists ?");
    // // System.out.println(dest.toString());
    // //
    // // System.out.println(old.toString());
    // // System.out.println("old");
    // // Organizer.deleteDirectory(old);
    // // System.out.println("Delete old");
    // // Organizer.copyFile(dest, old);
    // // System.out.println("Copy to old");
    // // Organizer.deleteDirectory(dest);
    // // System.out.println("delete dest");
    //
    // Organizer.copyFile(jarFile, dest);
    // System.out.println(String.format("Step : Copy Iso19139 schemas from  '%s' to '%s'\n",
    // jarFile.getName().toString(), dest.getName()
    // .toString()));
    // }
    //
    // stringList.add(localIso19139RootSchemaPath);
    // String[] inS = new String[stringList.size()];
    // inS = stringList.toArray(inS);
    // // System.out.println("exit getServiceMetadataSchemaAsString");
    //
    // return inS;
    // }

    // public JAXBElement<?> unmarshallXmlTemplate() throws MotuException, FileSystemException, JAXBException
    // {
    //
    // System.out.println(String.format("Step : unmarshall XML from '%s'\n", xmlTemplate));
    //
    // Unmarshaller unmarshaller = jc.createUnmarshaller();
    // Source srcFile = new StreamSource(xmlTemplate);
    //
    // JAXBElement<?> element = (JAXBElement<?>) unmarshaller.unmarshal(srcFile);
    //
    // //
    // // SVServiceIdentificationType serviceIdentificationType = (SVServiceIdentificationType)
    // // element.getValue();
    // // System.out.println(serviceIdentificationType.toString());
    // //
    // // List<SVOperationMetadataPropertyType> operationMetadataPropertyTypeList =
    // // serviceIdentificationType.getContainsOperations();
    // //
    // // for (SVOperationMetadataPropertyType operationMetadataPropertyType :
    // // operationMetadataPropertyTypeList) {
    // //
    // // SVOperationMetadataType operationMetadataType =
    // // operationMetadataPropertyType.getSVOperationMetadata();
    // // System.out.println("---------------------------------------------");
    // // if (operationMetadataType == null) {
    // // continue;
    // // }
    // // System.out.println(operationMetadataType.getOperationName().getCharacterString().getValue());
    // // System.out.println(operationMetadataType.getInvocationName().getCharacterString().getValue());
    // // System.out.println(operationMetadataType.getOperationDescription().getCharacterString().getValue());
    // //
    // // CIOnlineResourcePropertyType onlineResourcePropertyType =
    // // operationMetadataType.getConnectPoint().get(0);
    // // if (onlineResourcePropertyType != null) {
    // //
    // System.out.println(operationMetadataType.getConnectPoint().get(0).getCIOnlineResource().getLinkage().getURL());
    // // }
    // //
    // // List<SVParameterPropertyType> parameterPropertyTypeList = operationMetadataType.getParameters();
    // //
    // // for (SVParameterPropertyType parameterPropertyType : parameterPropertyTypeList) {
    // // SVParameterType parameterType = parameterPropertyType.getSVParameter();
    // //
    // // if (parameterType.getName().getAName().getCharacterString() != null) {
    // // System.out.println(parameterType.getName().getAName().getCharacterString().getValue());
    // // } else {
    // // System.out.println("WARNING - A parameter has no name");
    // //
    // // }
    // // if (parameterType.getDescription() != null) {
    // // if (parameterType.getDescription().getCharacterString() != null) {
    // // System.out.println(parameterType.getDescription().getCharacterString().getValue());
    // // } else {
    // // System.out.println("WARNING - A parameter has no description");
    // //
    // // }
    // // } else {
    // // System.out.println("WARNING - A parameter has no description");
    // //
    // // }
    // // }
    // //
    // // }
    //
    // return element;
    // }

    // public void marshallXmlTemplate(JAXBElement<?> element) throws MotuException, JAXBException,
    // IOException, URISyntaxException {
    // // FileWriter writer = new FileWriter(String.format("%s/%s/%s.xml", tempPath, SERVICE_METADATA,
    // // SERVICE_METADATA));
    // // FileObject fileobj = Organizer.resolveFile(String.format("%s/%s", tempPath, outputXml));
    // System.out.println(String.format("Step : marshall XML to '%s'\n", outputXml));
    //
    // FileObject fileobj = Organizer.resolveFile(outputXml);
    // // URI uri = new URI(String.format("%s/%s/%s.xml", tempPath, SERVICE_METADATA, SERVICE_METADATA));
    // System.out.println(fileobj.getName().toString());
    // // file.createNewFile();
    // fileobj.createFile();
    // // FileWriter writer = new FileWriter(fileobj.getName().getURI());
    //
    // Marshaller marshaller = jc.createMarshaller();
    // marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    // // marshaller.marshal(element, writer);
    //
    // marshaller.marshal(element, fileobj.getContent().getOutputStream());
    //
    // fileobj.close();
    // // writer.flush();
    // // writer.close();
    //
    // }

    public static void fmpp() {

    }

}
