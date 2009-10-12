package fr.cls.atoll.motu.processor.wps;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchema;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.deegree.commons.utils.HttpUtils;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.Parameter;
import org.isotc211.iso19139.d_2006_05_04.srv.SVOperationMetadataType;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.EdgeReversedGraph;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValue;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.exception.MotuMarshallException;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.atoll.motu.processor.iso19139.OperationMetadata;
import fr.cls.atoll.motu.processor.iso19139.ServiceMetadata;
import fr.cls.atoll.motu.processor.jgraht.OperationRelationshipEdge;
import fr.cls.atoll.motu.processor.opengis.wps100.DescriptionType;
import fr.cls.atoll.motu.processor.opengis.wps100.Execute;
import fr.cls.atoll.motu.processor.opengis.wps100.InputDescriptionType;
import fr.cls.atoll.motu.processor.opengis.wps100.ProcessDescriptionType;
import fr.cls.atoll.motu.processor.opengis.wps100.ProcessDescriptions;
import fr.cls.atoll.motu.processor.wps.framework.WPSFactory;
import fr.cls.atoll.motu.processor.wps.framework.WPSInfo;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.26 $ - $Date: 2009-10-12 14:13:29 $
 */
class StringList extends ArrayList<String> {
}

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

        try {
            Organizer.initProxyLogin();
        } catch (MotuException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Collection<String> tt = new ArrayList<String>();
        // tt.add("qsdfsdf");
        //        
        // System.out.println(tt.getClass());
        // System.out.println(tt.getClass().getGenericSuperclass());
        // ParameterizedType pt = (ParameterizedType) tt.getClass().getGenericSuperclass();
        // System.out.println(pt.getActualTypeArguments()[0]);
        // System.out.println(pt.getOwnerType());
        // System.out.println(pt.getRawType());
        // System.out.println(tt.getClass().getTypeParameters()[0].getName());
        // System.out.println(tt.getClass().getTypeParameters()[0].getGenericDeclaration());
        // System.out.println(tt.getClass().getComponentType());
        //        
        //        
        // Object o = tt.iterator().next();
        // System.out.println(o.getClass());

        // Type type = StringList.class.getGenericSuperclass();
        // System.out.println(type); // java.util.ArrayList<java.lang.String>
        // pt = (ParameterizedType) type;
        // System.out.println(pt.getActualTypeArguments()[0]);

        // System.setProperty("proxyHost", "proxy.cls.fr"); // adresse IP
        // System.setProperty("proxyPort", "8080");
        // System.setProperty("socksProxyHost", "proxy.cls.fr");
        // System.setProperty("socksProxyPort", "1080");
        // Authenticator.setDefault(new MyAuthenticator());

        // testBuildWPS();
        // testBuildChainWPS();
        testBuildAndRunChainWPS();
        // testUnmarshallWPS();

        // for (ErrorType c: ErrorType.values()) {
        // if (c.toString().equalsIgnoreCase("system")) {
        // System.out.println(c.toString());
        // }
        // }

        // testBodyPost();
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

        // testPackageAnnotation();
        // testGetFields();
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

    public static void testBodyPost(String xmlFile, String href) {

        //String href = "http://localhost:8080/atoll-motuservlet/services";
        
        Map<String, String> headers = new HashMap<String, String>();
        try {
            InputStream is = Organizer.getUriAsInputStream(xmlFile);
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
        } catch (MotuException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(e.notifyException());
        }

    }

    public static void testBodyPostDontWaitResponse(String xmlFile, String href) {

        // String href = "http://localhost:8080/atoll-motuservlet/services";

        Map<String, String> headers = new HashMap<String, String>();
        try {
            InputStream is = Organizer.getUriAsInputStream(xmlFile);
            int ret = WPSFactory.postAsync(href, is, headers);
            System.out.println("WPSFactory.post returned code: ");
            System.out.println(ret);
        } catch (HttpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MotuException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(e.notifyException());
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

        for (Field field : type.getDeclaredFields()) {
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
            // ServiceMetadata serviceMetadata = new ServiceMetadata();
            // URL url = null;
            // Set<SVOperationMetadataType> listOperation = new HashSet<SVOperationMetadataType>();
            // // url = new
            // //
            // URL("file:///c:/Documents and Settings/dearith/Mes documents/Atoll/SchemaIso/TestServiceMetadataOK.xml");
            // url =
            // Organizer.findResource("src/main/resources/fmpp/out/serviceMetadata_motu-opendap-mercator.xml");
            // serviceMetadata.getOperations(url, listOperation);


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

            ParameterDescriptor<List<String>> descriptor2 = new DefaultParameterDescriptor<List<String>>("variable", (Class<List<String>>) list
                    .getClass(), null, null);
            parameter = new Parameter<List<String>>(descriptor2);
            parameter.setValue(list);

            System.out.println(descriptor2.getName().getCode());

            dataInputValues.put(descriptor2.getName().getCode(), parameter);

            // Map geobbox = new HashMap<String, String>();
            // String geobbox ="-10, -60, 45, 120";
            double[] geobbox = new double[] { -10d, -60d, 45d, 120d };
            System.out.println(geobbox.getClass());

            ParameterValue<?> parameterValue = WPSFactory.createParameter("geobbox", geobbox.getClass(), geobbox);
            System.out.println(parameterValue.getValue().getClass());

            dataInputValues.put(parameterValue.getDescriptor().getName().getCode(), parameterValue);

            double depth = 0d;
            parameterValue = WPSFactory.createParameter("lowdepth", depth);
            System.out.println(parameterValue.getValue().getClass());

            dataInputValues.put(parameterValue.getDescriptor().getName().getCode(), parameterValue);

            OperationMetadata operationMetadata = new OperationMetadata();
            operationMetadata.setInvocationName("ExtractData");
            operationMetadata.setParameterValueMap(dataInputValues);

            WPSFactory wpsFactory = new WPSFactory();

            Execute execute = wpsFactory.createExecuteProcessRequest(operationMetadata, null);

            FileWriter writer = new FileWriter("WPSExecute.xml");

            WPSInfo wpInfo = WPSFactory.getWpsInfo(serverURL);
            
            String schemaLocationKey = String.format("%s%s", wpInfo.getProcessDescriptions().getService(), wpInfo.getProcessDescriptions().getVersion());
            WPSFactory.marshallExecute(execute, writer, WPSFactory.getSchemaLocations().get(schemaLocationKey));

            dataInputValues.clear();

            Long val = 1023654l;
            // Integer val = 1023654;
            // Double val = 1023654d;
            // String val = "1023654";
            System.out.println(val.getClass());

            parameterValue = WPSFactory.createParameter("requestid", val.getClass(), val);
            System.out.println(parameterValue.getValue().getClass());

            dataInputValues.put(parameterValue.getDescriptor().getName().getCode(), parameterValue);

            operationMetadata.setInvocationName("CompressExtraction");
            operationMetadata.setParameterValueMap(dataInputValues);

            execute = wpsFactory.createExecuteProcessRequest(operationMetadata, null);

            writer = new FileWriter("WPSExecute2.xml");

            WPSFactory.marshallExecute(execute, writer, WPSFactory.getSchemaLocations().get(schemaLocationKey));

        } catch (MotuException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(e.notifyException());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MotuMarshallException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void testBuildChainWPS() {
        try {
            ServiceMetadata serviceMetadata = new ServiceMetadata();
            URL url = null;
            Set<SVOperationMetadataType> listOperation = new HashSet<SVOperationMetadataType>();
            // url = new
            // URL("file:///c:/Documents and Settings/dearith/Mes documents/Atoll/SchemaIso/TestServiceMetadataOK.xml");
            url = Organizer.findResource("src/main/resources/fmpp/out/serviceMetadata_motu-opendap-mercator.xml");
            serviceMetadata.getOperations(url, listOperation);
            ServiceMetadata.dump(listOperation);

            // DirectedGraph<OperationMetadata, DefaultEdge> directedGraph = new
            // DefaultDirectedGraph<OperationMetadata, DefaultEdge>(DefaultEdge.class);
            DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> directedGraph = ServiceMetadata.createDirectedGraph();
            serviceMetadata.getOperations(url, directedGraph);

            List<OperationMetadata> sourceOperations = new ArrayList<OperationMetadata>();
            List<OperationMetadata> sinkOperations = new ArrayList<OperationMetadata>();

            EdgeReversedGraph<OperationMetadata, OperationRelationshipEdge<String>> edgeReversedGraph = new EdgeReversedGraph<OperationMetadata, OperationRelationshipEdge<String>>(
                    directedGraph);

            sourceOperations.clear();
            sinkOperations.clear();

            ServiceMetadata.getSourceOperations(edgeReversedGraph, sourceOperations);
            ServiceMetadata.getSinkOperations(edgeReversedGraph, sinkOperations);

            System.out.println("%%%%%%%% REVERSE GRAPH %%%%%%%%%%%%");
            System.out.println("%%%%%%%% SOURCE %%%%%%%%%%%%");
            System.out.println(sourceOperations);
            System.out.println("%%%%%%%% SINK %%%%%%%%%%%%");
            System.out.println(sinkOperations);

            for (OperationMetadata source : sourceOperations) {
                System.out.print("%%%%%%%% PATHS FROM  %%%%%%%%%%%%");
                System.out.println(source);
                KShortestPaths<OperationMetadata, OperationRelationshipEdge<String>> paths = ServiceMetadata.getOperationPaths(edgeReversedGraph,
                                                                                                                               source,
                                                                                                                               10);

                for (OperationMetadata sink : sinkOperations) {
                    System.out.print(" %%%%%%%%%%%% TO ");
                    System.out.println(sink);
                    List<GraphPath<OperationMetadata, OperationRelationshipEdge<String>>> listPath = ServiceMetadata.getOperationPaths(paths, sink);
                    for (GraphPath<OperationMetadata, OperationRelationshipEdge<String>> gp : listPath) {
                        System.out.println(gp.getEdgeList());
                    }
                }

            }

            sourceOperations.clear();
            sinkOperations.clear();

            ServiceMetadata.getSourceOperations(directedGraph, sourceOperations);
            ServiceMetadata.getSinkOperations(directedGraph, sinkOperations);

            System.out.println("%%%%%%%% SOURCE %%%%%%%%%%%%");
            System.out.println(sourceOperations);
            System.out.println("%%%%%%%% SINK %%%%%%%%%%%%");
            System.out.println(sinkOperations);

            Map<String, Map<String, ParameterValue<?>>> operationsInputValues = new HashMap<String, Map<String, ParameterValue<?>>>();

            Map<String, ParameterValue<?>> dataInputValues = null;

            for (OperationMetadata source : sourceOperations) {
                System.out.print("%%%%%%%% PATHS FROM  %%%%%%%%%%%%");
                System.out.println(source);

                // source.dump();

                // dataInputValues = source.createParameterValues();
                // operationsInputValues.put(source.getOperationName(), dataInputValues);

                KShortestPaths<OperationMetadata, OperationRelationshipEdge<String>> paths = ServiceMetadata.getOperationPaths(directedGraph,
                                                                                                                               source,
                                                                                                                               10);

                for (OperationMetadata sink : sinkOperations) {
                    System.out.print(" %%%%%%%%%%%% TO ");
                    System.out.println(sink);
                    List<GraphPath<OperationMetadata, OperationRelationshipEdge<String>>> listPath = ServiceMetadata.getOperationPaths(paths, sink);

                    for (GraphPath<OperationMetadata, OperationRelationshipEdge<String>> gp : listPath) {

                        System.out.println(gp.getEdgeList());
                        System.out.println("Sink: " + sink.getOperationName());

                        for (OperationRelationshipEdge<String> edge : gp.getEdgeList()) {
                            OperationMetadata operationMetadata1 = directedGraph.getEdgeSource(edge);
                            OperationMetadata operationMetadata2 = directedGraph.getEdgeTarget(edge);
                            System.out.println("StartVertex: " + operationMetadata1.getOperationName());
                            System.out.println("EndVertex: " + operationMetadata2.getOperationName());
                            System.out.println("Parameters Edge: " + edge.getParamOutStartVertex().toString() + " / "
                                    + edge.getParamInStartVertex().toString());

                            dataInputValues = operationMetadata1.createParameterValues(true, false);
                            operationsInputValues.put(operationMetadata1.getOperationName(), dataInputValues);

                            dataInputValues = operationMetadata2.createParameterValues(true, false);
                            operationsInputValues.put(operationMetadata2.getOperationName(), dataInputValues);

                        }

                    }
                }

            }

            for (Map.Entry<String, Map<String, ParameterValue<?>>> pair : operationsInputValues.entrySet()) {
                System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$");
                System.out.println(pair.getKey());
                System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$");

                for (Map.Entry<String, ParameterValue<?>> pair2 : pair.getValue().entrySet()) {
                    System.out.println("µµµµµµµµµµµµµµµµµµµµµµµ");
                    System.out.println(pair2.getKey());
                    System.out.println("µµµµµµµµµµµµµµµµµµµµµµµ");
                    ParameterValue<?> parameterValue = pair2.getValue();
                    System.out.print(parameterValue.getDescriptor().getName());
                    System.out.print(" ");
                    System.out.println(parameterValue.getDescriptor().getValueClass());
                    final Class<?> type = parameterValue.getDescriptor().getValueClass();
                    if (Double.class.equals(type) || Double.TYPE.equals(type)) {
                        parameterValue.setValue(1203.36);
                    }
                    if (Long.class.equals(type) || Long.TYPE.equals(type)) {
                        Long v = 120336954L;
                        parameterValue.setValue(v);
                    }
                    if (Integer.class.equals(type) || Integer.TYPE.equals(type)) {
                        int v = 98564;
                        parameterValue.setValue(v);
                    }
                    if (String.class.equals(type)) {
                        parameterValue.setValue("param value as string");
                    }
                    if (double[].class.equals(type)) {
                        double[] geobbox = new double[] { -10d, -60d, 45d, 120d };
                        parameterValue.setValue(geobbox);
                    }

                }
            }

            Set<OperationMetadata> setSubGraph = new HashSet<OperationMetadata>();
            Set<OperationMetadata> setGraph = directedGraph.vertexSet();

            for (OperationMetadata op : setGraph) {
                if (op.getInvocationName().equalsIgnoreCase("ExtractData")) {
                    setSubGraph.add(op);
                }
                if (op.getInvocationName().equalsIgnoreCase("CompressExtraction")) {
                    setSubGraph.add(op);
                }
            }

            DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> directedSubGraph = ServiceMetadata
                    .createDirectedSubGraph(directedGraph, setSubGraph, null);
            System.out.println(directedSubGraph.toString());

            sourceOperations.clear();
            sinkOperations.clear();

            ServiceMetadata.getSourceOperations(directedSubGraph, sourceOperations);
            ServiceMetadata.getSinkOperations(directedSubGraph, sinkOperations);

            System.out.println("%%%%%%%% SUB GRAPH %%%%%%%%%%%%");
            System.out.println("%%%%%%%% SOURCE %%%%%%%%%%%%");
            System.out.println(sourceOperations);
            System.out.println("%%%%%%%% SINK %%%%%%%%%%%%");
            System.out.println(sinkOperations);

            for (OperationMetadata source : sourceOperations) {
                System.out.print("%%%%%%%% PATHS FROM  %%%%%%%%%%%%");
                System.out.println(source);
                KShortestPaths<OperationMetadata, OperationRelationshipEdge<String>> paths = ServiceMetadata.getOperationPaths(directedSubGraph,
                                                                                                                               source,
                                                                                                                               10);

                for (OperationMetadata sink : sinkOperations) {
                    System.out.print(" %%%%%%%%%%%% TO ");
                    System.out.println(sink);
                    List<GraphPath<OperationMetadata, OperationRelationshipEdge<String>>> listPath = ServiceMetadata.getOperationPaths(paths, sink);
                    for (GraphPath<OperationMetadata, OperationRelationshipEdge<String>> gp : listPath) {
                        System.out.println(gp.getEdgeList());
                        System.out.println(gp.getEdgeList());
                        System.out.println("Sink: " + sink.getOperationName());

                        for (OperationRelationshipEdge<String> edge : gp.getEdgeList()) {
                            OperationMetadata operationMetadata1 = directedSubGraph.getEdgeSource(edge);
                            OperationMetadata operationMetadata2 = directedSubGraph.getEdgeTarget(edge);
                            System.out.println("StartVertex: " + operationMetadata1.getOperationName());
                            System.out.println("EndVertex: " + operationMetadata2.getOperationName());
                            System.out.println("Parameters Edge: " + edge.getParamInStartVertex().toString() + " / "
                                    + edge.getParamOutStartVertex().toString());

                            System.out.println("operationMetadata1: " + operationMetadata1.getParameterValueMap());
                            System.out.println("operationMetadata2: " + operationMetadata2.getParameterValueMap());

                        }

                    }
                }

            }

            String serverURL = "http://atoll-dev.cls.fr:30080/atoll-motuservlet/services";
            WPSFactory wpsFactory = new WPSFactory();

            
            Execute execute = wpsFactory.createExecuteProcessRequest(sourceOperations.get(0), directedSubGraph);

            String wpsXml = "WPSExecuteChain.xml";

            FileWriter writer = new FileWriter(wpsXml);

            WPSInfo wpsInfo =WPSFactory.getWpsInfo(serverURL);
            String schemaLocationKey = String.format("%s%s", wpsInfo.getProcessDescriptions().getService(), 
                    wpsInfo.getProcessDescriptions().getVersion());
            WPSFactory.marshallExecute(execute, writer, WPSFactory.getSchemaLocations().get(schemaLocationKey));

            System.out.println("===============> Validate WPS");

            List<String> errors = WPSFactory.validateWPSExecuteRequest("",
                                                                       "file:///c:/tempVFS/OGC_SCHEMA/",
                                                                       "wps/1.0.0/wpsExecute_request.xsd",
                                                                       wpsXml);
            if (errors.size() > 0) {
                StringBuffer stringBuffer = new StringBuffer();
                for (String str : errors) {
                    stringBuffer.append(str);
                    stringBuffer.append("\n");
                }
                throw new MotuException(String.format("ERROR - XML file '%s' is not valid - See errors below:\n%s", wpsXml, stringBuffer.toString()));
            } else {
                System.out.println(String.format("XML file '%s' is valid", wpsXml));
            }

            System.out.println("===============> Execute WPS");

            testBodyPost(wpsXml, serverURL);

        } catch (MotuException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(e.notifyException());
        } catch (MotuMarshallException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void testUnmarshallWPS() {
        try {
            String serverURL = "http://atoll-dev.cls.fr:30080/atoll-motuservlet/services";
            WPSFactory wpsFactory = new WPSFactory();
            String file = "J:/dev/atoll-v2/atoll-motu/atoll-motu-processor/src/test/resources/client/requests/wps/example/execute/xml/TestExtractedUrlWithCompressExtraction.xml";
            Execute execute = wpsFactory.unmarshallExecute(file);
            System.out.println("END");
        } catch (MotuException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MotuMarshallException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void testBuildAndRunChainWPS() {
        try {
            ServiceMetadata serviceMetadata = new ServiceMetadata();
            URL url = null;
            url = Organizer.findResource("src/main/resources/fmpp/out/serviceMetadata_motu-opendap-mercator.xml");

            DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> directedGraph = ServiceMetadata.createDirectedGraph();
            serviceMetadata.getOperations(url, directedGraph);

            Set<OperationMetadata> setSubGraph = new HashSet<OperationMetadata>();
            Set<OperationRelationshipEdge<String>> setSubEdge = new HashSet<OperationRelationshipEdge<String>>();

            Set<OperationMetadata> setGraph = directedGraph.vertexSet();
            Set<OperationRelationshipEdge<String>> setEdge = directedGraph.edgeSet();

            OperationMetadata opExtractData = null;
            OperationMetadata opCompressExtraction = null;
            OperationMetadata opPush = null;
            OperationMetadata opGetStatus = null;

            for (OperationMetadata op : setGraph) {

                op.createParameterValues(true, false);

//                if (op.getInvocationName().equalsIgnoreCase("GetRequestStatus")) {
//                    opGetStatus = op;
//                    setSubGraph.add(op);
//                    setGetRequestStatusParameterValue(op);
//                }
                if (op.getInvocationName().equalsIgnoreCase("ExtractData")) {
                    opExtractData = op;
                    setSubGraph.add(op);
                    setExtractDataParameterValue(op);
                }
                if (op.getInvocationName().equalsIgnoreCase("CompressExtraction")) {
                    opCompressExtraction = op;
                    setSubGraph.add(op);
                }
                if (op.getInvocationName().equalsIgnoreCase("Push")) {
                    opPush = op;
                    setSubGraph.add(op);
                    setPushDataParameterValue(op);
                }
            }

            setSubEdge.add(directedGraph.getEdge(opPush, opCompressExtraction));
            setSubEdge.add(directedGraph.getEdge(opCompressExtraction, opExtractData));

            // DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> directedSubGraph =
            // ServiceMetadata
            // .createDirectedSubGraph(directedGraph, setSubGraph, null);
            DirectedGraph<OperationMetadata, OperationRelationshipEdge<String>> directedSubGraph = ServiceMetadata
                    .createDirectedSubGraph(directedGraph, setSubGraph, setSubEdge);

            System.out.println(directedSubGraph.toString());

            List<OperationMetadata> sourceOperations = new ArrayList<OperationMetadata>();
            List<OperationMetadata> sinkOperations = new ArrayList<OperationMetadata>();

            ServiceMetadata.getSourceOperations(directedSubGraph, sourceOperations);
            ServiceMetadata.getSinkOperations(directedSubGraph, sinkOperations);

            //String serverURL = "http://atoll-dev.cls.fr:30080/atoll-motuservlet/services";
            String serverURL = sourceOperations.get(0).getConnectPoint(0);
            WPSFactory wpsFactory = new WPSFactory();

            Execute execute = wpsFactory.createExecuteProcessRequest(sourceOperations.get(0), directedSubGraph);

            String wpsXml = "WPSExecuteChain.xml";

            FileWriter writer = new FileWriter(wpsXml);

            WPSInfo wpsInfo = WPSFactory.getWpsInfo(serverURL);
            
            String schemaLocationKey = String.format("%s%s", wpsInfo.getProcessDescriptions().getService(),
                                                     wpsInfo.getProcessDescriptions().getVersion());
            
            WPSFactory.marshallExecute(execute, writer, WPSFactory.getSchemaLocations().get(schemaLocationKey));

            System.out.println("===============> Validate WPS");

            List<String> errors = WPSFactory.validateWPSExecuteRequest("",
                                                                       "file:///c:/tempVFS/OGC_SCHEMA/",
                                                                       "wps/1.0.0/wpsExecute_request.xsd",
                                                                       wpsXml);
            if (errors.size() > 0) {
                StringBuffer stringBuffer = new StringBuffer();
                for (String str : errors) {
                    stringBuffer.append(str);
                    stringBuffer.append("\n");
                }
                throw new MotuException(String.format("ERROR - XML file '%s' is not valid - See errors below:\n%s", wpsXml, stringBuffer.toString()));
            } else {
                System.out.println(String.format("XML file '%s' is valid", wpsXml));
            }

//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            OutputStreamWriter writer2 = new OutputStreamWriter(out);
//
//            WPSFactory.marshallExecute(execute, writer2, WPSFactory.getSchemaLocations().get(schemaLocationKey));
//
//            InputStream inputStream = new ByteArrayInputStream(out.toByteArray());
//
//            byte b[] = new byte[1024];
//
//            int bytesRead = 0;
//
//            while ((bytesRead = inputStream.read(b)) != -1) {
//                String nextLine = new String(b, 0, bytesRead);
//                System.out.println(nextLine);
//            }
//

            System.out.println("===============> Execute WPS");

            testBodyPost(wpsXml, serverURL);
            ///testBodyPostDontWaitResponse(wpsXml, serverURL);

        } catch (MotuExceptionBase e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(e.notifyException());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void setPushDataParameterValue(OperationMetadata op) throws MotuExceptionBase {
        op.setParameterValue("remove", false);
        op.setParameterValue("rename", false);
        op.setParameterValue("to", "ftp://t:t@CLS-EARITH.pc.cls.fr/Dossier4");

    }

    public static void setExtractDataParameterValue(OperationMetadata op) throws MotuExceptionBase {
        op.setParameterValue("service", "mercator");
        op.setParameterValue("product", "mercatorPsy3v2_nat_mean_best_estimate");
        op.setParameterValue("starttime", "2009-04-27T10:00:00");
        op.setParameterValue("endtime", "2009-04-28");

        List<String> variables = new ArrayList<String>();
        variables.add("temperature");
        variables.add("u");
        variables.add("v");

        // op.setParameterValue("variable", variables);
        op.setParameterValue("variable", "u");

        
        double[] geobbox = new double[] { 10d, -60d, 45d, 120d };
        op.setParameterValue("geobbox", geobbox);
        
        op.setParameterValue("lowdepth", "surface");
        op.setParameterValue("highdepth", "150");

    }
    public static void setGetRequestStatusParameterValue(OperationMetadata op) throws MotuExceptionBase {
        op.setParameterValue("requestid", 1255350789354L);
    }    
}
