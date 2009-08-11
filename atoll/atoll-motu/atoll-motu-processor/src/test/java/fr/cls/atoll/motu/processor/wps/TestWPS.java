package fr.cls.atoll.motu.processor.wps;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchema;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.deegree.commons.utils.HttpUtils;
import org.geotoolkit.io.wkt.Formatter;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.Parameter;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValue;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.atoll.motu.processor.opengis.wps100.DescriptionType;
import fr.cls.atoll.motu.processor.opengis.wps100.InputDescriptionType;
import fr.cls.atoll.motu.processor.opengis.wps100.ProcessDescriptionType;
import fr.cls.atoll.motu.processor.opengis.wps100.ProcessDescriptions;
import fr.cls.atoll.motu.processor.wps.TestParameter.Liste;
import fr.cls.atoll.motu.processor.wps.framework.WPSFactory;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.10 $ - $Date: 2009-08-11 15:04:08 $
 */
public class TestWPS {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(TestWPS.class);

    public class GetObjectId {
        public GetObjectId() {
        }
    }
    

    /**
     * .
     * 
     * @param args
     */
    public static void main(String[] args) {

        
        testBuildWPS();
        
        // for (ErrorType c: ErrorType.values()) {
        // if (c.toString().equalsIgnoreCase("system")) {
        // System.out.println(c.toString());
        // }
        // }

        //testBodyPost();
        // testUTF8EncodeDecode();

        // TestWPS test = new TestWPS();
        //        
        // TestWPS.GetObjectId id = test.new GetObjectId();
        // System.out.println(id.hashCode());
        // TestWPS.GetObjectId id2 = test.new GetObjectId();
        // System.out.println(id2.hashCode());
        //        
        // TestWPS.GetObjectId id3 = id;
        // System.out.println(id3.hashCode());
        //        
        // String abc = new String("Australia");
        // System.out.println("Hash code for String object: " + abc.hashCode());
        // String abc2 = new String("Australia");
        // System.out.println("Hash code for String object: " + abc2.hashCode());
        //
        //        

        // try {
        // // Generate a DES key
        // KeyGenerator keyGen = KeyGenerator.getInstance("DES");
        // SecretKey key = keyGen.generateKey();
        // System.out.println(key.hashCode());
        // // Get the bytes of the key
        // byte[] keyBytes = key.getEncoded();
        // int numBytes = keyBytes.length;
        // String string = new String(keyBytes);
        // System.out.println(string);
        //            
        //
        // // Generate a Blowfish key
        // keyGen = KeyGenerator.getInstance("Blowfish");
        // key = keyGen.generateKey();
        // System.out.println(key.hashCode());
        //        
        // // Generate a triple DES key
        // keyGen = KeyGenerator.getInstance("DESede");
        // key = keyGen.generateKey();
        // System.out.println(key.hashCode());
        // } catch (java.security.NoSuchAlgorithmException e) {
        // }
        // getDescribeProcess();

        //testPackageAnnotation();
        //testGetFields();
    }

    public static void testBodyPost() {

        String href = "http://localhost:8080/atoll-motuservlet/services";

        String postBodyString = "<wps:Execute  xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" service=\"WPS\" version=\"1.0.0\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">                        <ows:Identifier>TestAdd</ows:Identifier>                        <wps:DataInputs>                            <wps:Input>                             <ows:Identifier>A</ows:Identifier>                              <wps:Data>                                  <wps:ComplexData>10</wps:ComplexData>                               </wps:Data>                         </wps:Input>                            <wps:Input>                             <ows:Identifier>B</ows:Identifier>                              <wps:Data>                                  <wps:ComplexData>8</wps:ComplexData>                                </wps:Data>                         </wps:Input>                        </wps:DataInputs>                   <wps:ResponseForm>                          <wps:RawDataOutput mimeType=\"text/plain\">                             <ows:Identifier>C</ows:Identifier>                          </wps:RawDataOutput>                        </wps:ResponseForm>           </wps:Execute>              </wps:Body>";
        LOG.debug("Using post body '" + postBodyString + "'");
        // TODO what about the encoding here?
        InputStream is = new ByteArrayInputStream(postBodyString.getBytes());
        Map<String, String> headers = new HashMap<String, String>();
        try {
            is = HttpUtils.post(HttpUtils.STREAM, href, is, headers);
            byte b[] = new byte[1024];

            int bytesRead = 0;

            while ((bytesRead = is.read(b)) != -1) {
                String nextLine = new String(b, 0, bytesRead);
                System.out.println(nextLine);
            }

        } catch (HttpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void testUTF8EncodeDecode() {
        int i = 270;
        String str = Integer.toString(i);

        CharBuffer result = byteConvert(str.getBytes(), "UTF-8");
        System.out.println(result);
        // result = byteConvert(str.getBytes(), "base64");
        // System.out.println(result);

    }

    public static void print(ByteBuffer bb) {
        while (bb.hasRemaining())
            System.out.print(bb.get() + " ");
        System.out.println();
        bb.rewind();
    }

    public static CharBuffer byteConvert(byte[] value, String charset) {
        ByteBuffer bb = ByteBuffer.wrap(value);
        CharBuffer charBuffer = null;
        try {
            Charset csets = Charset.forName(charset);
            charBuffer = csets.decode(bb);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String str = charBuffer.toString();
        System.out.println("XXXXXXXX");
        System.out.println(str);

        return charBuffer;
    }

    public static ProcessDescriptions getDescribeProcess() {

        String href = "http://localhost:8080/atoll-motuservlet/services";
        InputStream in = null;
        Map<String, String> headers = new HashMap<String, String>();
        try {
            in = Organizer.getUriAsInputStream("DescribeAll.xml");
            in = HttpUtils.post(HttpUtils.STREAM, href, in, headers);
            // byte b[] = new byte[1024];
            //
            // int bytesRead = 0;
            // if (in.markSupported()) {
            // in.mark(bytesRead);
            // }
            //
            // while ((bytesRead = in.read(b)) != -1) {
            // String nextLine = new String(b, 0, bytesRead);
            // System.out.println(nextLine);
            // }
            //            
            // if (in.markSupported()) {
            // in.reset();
            // }

        } catch (HttpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MotuException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ProcessDescriptions processDescriptions = null;

        if (in == null) {
            return processDescriptions;
        }

        try {
            JAXBContext jc = JAXBContext.newInstance(MotuWPSProcess.WPS100_SHEMA_PACK_NAME);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            processDescriptions = (ProcessDescriptions) unmarshaller.unmarshal(in);
        } catch (Exception e) {
            // throw new MotuException("Error in GetDescribeProcess", e);
            System.out.println(e.getMessage());
        }

        if (processDescriptions == null) {
            // throw new
            // MotuException("Unable to load WPS Process Descriptions (processDescriptions is null)");
            System.out.println("Unable to load WPS Process Descriptions (processDescriptions is null)");
            return processDescriptions;
        }

        List<ProcessDescriptionType> processDescriptionList = processDescriptions.getProcessDescription();

        for (ProcessDescriptionType processDescriptionType : processDescriptionList) {
            // JAXBElement<String> element = new JAXBElement<String>(new QName("Test"), String.class, "tre");
            System.out.println("===================");
            System.out.println(processDescriptionType.getIdentifier().getValue());
            System.out.println("===================");
            List<InputDescriptionType> inputDescriptionTypeList = processDescriptionType.getDataInputs().getInput();
            for (InputDescriptionType inputDescriptionType : inputDescriptionTypeList) {
                Object inputData = null;
                String fieldName = "";
                if (inputDescriptionType.getLiteralData() != null) {
                    inputData = inputDescriptionType.getLiteralData();
                    // JAXBElement<InputDescriptionType> element = inputDescriptionType;
                }
                if (inputDescriptionType.getComplexData() != null) {
                    inputData = inputDescriptionType.getComplexData();
                }
                if (inputDescriptionType.getBoundingBoxData() != null) {
                    inputData = inputDescriptionType.getBoundingBoxData();
                }
                try {
                    Field[] fields = DescriptionType.class.getDeclaredFields();
                    for (Field field : fields) {
                        System.out.println(field.getName());

                    }
                    System.out.println(DescriptionType.class.getDeclaredField("identifier").getAnnotation(XmlElement.class).toString());
                    System.out.println(inputDescriptionType.getIdentifier().getCodeSpace());
                } catch (SecurityException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (NoSuchFieldException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                System.out.print(inputDescriptionType.getIdentifier().getValue());
                System.out.print(" class is '");
                System.out.println(inputData.getClass().getName());
                System.out.print("', XmlElement is '");
                try {
                    System.out.print(InputDescriptionType.class.getDeclaredField("complexData").getAnnotation(XmlElement.class).toString());
                    // System.out.print(InputDescriptionType.class.getDeclaredField("complexData").getAnnotation(XmlElement.class).name());
                } catch (SecurityException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                System.out.println("'");
            }

        }

        return processDescriptions;
    }

    public static void testPackageAnnotation() {
        try {
            Annotation[] annos = DescriptionType.class.getAnnotations();

            System.out.println("All annotations for Meta2:");
            for (Annotation a : annos)
                System.out.println(a);

        } catch (Exception exc) {
        }

        AnnotatedElement pack = DescriptionType.class.getPackage();
        // getPackage isn't guaranteed to return a package
        if (pack == null) {
            return;
        }

        XmlSchema schema = pack.getAnnotation(XmlSchema.class);
        String namespace = null;
        if (schema != null) {
            namespace = schema.namespace();
        } else {
            namespace = "";
        }
        System.out.println(namespace);
    }
    
    public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        if (type.getSuperclass() != null) {
            fields = getAllFields(fields, type.getSuperclass());
        }

        for (Field field: type.getDeclaredFields()) {
            fields.add(field);
        }


        return fields;
    }

    public static void testGetFields() {
        List<Field> list = getAllFields(new LinkedList<Field>(), InputDescriptionType.class);
        for (Field field : list) {
            System.out.println(field.toGenericString());
        }
    }

    public static void testBuildWPS() {
        String serverURL = "http://atoll-dev.cls.fr:30080/atoll-motuservlet/services";
        
        try {
            WPSFactory wpsFactory = new WPSFactory(serverURL);
            
            Map<String, ParameterValue<?>> dataInputValues = new HashMap<String, ParameterValue<?>>();
            
            List<String> list = new ArrayList<String>();
            list.add("a");
            list.add("b");
            list.add("c");

            ParameterDescriptor<String> descriptor = new DefaultParameterDescriptor<String>("service", String.class, null, null);
            ParameterValue<?> parameter = new Parameter<String>(descriptor);
            parameter.setValue("myservice");

            System.out.println(descriptor.getName().getCode());
            
            dataInputValues.put(descriptor.getName().getCode(), parameter);

            ParameterDescriptor<List<String>> descriptor2 = new DefaultParameterDescriptor<List<String>>("variable", (Class<List<String>>) list.getClass(), null, null);
            parameter = new Parameter<List<String>>(descriptor2);
            parameter.setValue(list);
            
            System.out.println(descriptor2.getName().getCode());
            
            dataInputValues.put(descriptor2.getName().getCode(), parameter);
            
            wpsFactory.createExecuteProcessRequest(dataInputValues, "ExtractData");            
            
        } catch (MotuException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

}
