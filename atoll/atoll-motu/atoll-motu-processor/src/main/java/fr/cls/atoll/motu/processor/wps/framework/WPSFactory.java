package fr.cls.atoll.motu.processor.wps.framework;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.Parameter;
import org.geotoolkit.parameter.Parameters;
import org.opengis.parameter.InvalidParameterTypeException;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValue;

import com.sun.xml.bind.v2.model.core.Adapter;

import fr.cls.atoll.motu.library.data.ExtractCriteriaLatLon;
import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuMarshallException;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.atoll.motu.processor.opengis.ows110.BoundingBoxType;
import fr.cls.atoll.motu.processor.opengis.ows110.CodeType;
import fr.cls.atoll.motu.processor.opengis.wps100.ComplexDataCombinationsType;
import fr.cls.atoll.motu.processor.opengis.wps100.ComplexDataDescriptionType;
import fr.cls.atoll.motu.processor.opengis.wps100.ComplexDataType;
import fr.cls.atoll.motu.processor.opengis.wps100.DataInputsType;
import fr.cls.atoll.motu.processor.opengis.wps100.DataType;
import fr.cls.atoll.motu.processor.opengis.wps100.Execute;
import fr.cls.atoll.motu.processor.opengis.wps100.InputDescriptionType;
import fr.cls.atoll.motu.processor.opengis.wps100.InputType;
import fr.cls.atoll.motu.processor.opengis.wps100.LiteralDataType;
import fr.cls.atoll.motu.processor.opengis.wps100.LiteralInputType;
import fr.cls.atoll.motu.processor.opengis.wps100.ObjectFactory;
import fr.cls.atoll.motu.processor.opengis.wps100.ProcessDescriptionType;
import fr.cls.atoll.motu.processor.opengis.wps100.ProcessDescriptions;
import fr.cls.atoll.motu.processor.opengis.wps100.SupportedCRSsType;
import fr.cls.atoll.motu.processor.opengis.wps100.SupportedComplexDataInputType;
import fr.cls.atoll.motu.processor.wps.MotuWPSProcess;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.7 $ - $Date: 2009-09-01 14:24:39 $
 */
public class WPSFactory {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(WPSFactory.class);

    private static JAXBContext jaxbContextWPS = null;
    private static Marshaller marshallerWPS = null;
    private static ObjectFactory objectFactoryWPS = null;
    private static fr.cls.atoll.motu.processor.opengis.ows110.ObjectFactory objectFactoryOWS = null;

    protected static WPSInfo wpsInfo = null;

    protected static ConcurrentMap<String, String> schemaLocations = new ConcurrentHashMap<String, String>();

    public static ConcurrentMap<String, String> getSchemaLocations() {
        return schemaLocations;
    }

    public WPSInfo getWpsInfoInstance() throws MotuException {
        if (wpsInfo == null) {
            wpsInfo.loadDescribeProcess();
        }
        return wpsInfo;
    }

    public WPSFactory(String url) throws MotuException {

        WPSFactory.initSchemaLocations();
        WPSFactory.initJAXBWPS();
        wpsInfo = new WPSInfo(url);
        wpsInfo.loadDescribeProcess();

    }

    private static void initSchemaLocations() throws MotuException {
        WPSFactory.schemaLocations.putIfAbsent("WPS1.0.0",
                                               "http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd");

    }

    private static void initJAXBWPS() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initJAXBWPS() - entering");
        }
        if (WPSFactory.jaxbContextWPS != null) {
            return;
        }

        try {
            WPSFactory.jaxbContextWPS = JAXBContext.newInstance(MotuWPSProcess.WPS100_SHEMA_PACK_NAME);
            WPSFactory.marshallerWPS = WPSFactory.jaxbContextWPS.createMarshaller();
            WPSFactory.marshallerWPS.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        } catch (JAXBException e) {
            LOG.error("initJAXBWPS()", e);
            throw new MotuException("Error in WPSInfo - initJAXBWPS ", e);

        }

        objectFactoryWPS = new ObjectFactory();
        objectFactoryOWS = new fr.cls.atoll.motu.processor.opengis.ows110.ObjectFactory();

        if (LOG.isDebugEnabled()) {
            LOG.debug("initJAXBWPS() - exiting");
        }
    }

    public Execute createExecuteProcessRequest(Map<String, ParameterValue<?>> dataInputValues, String processName) throws MotuException {

        ProcessDescriptionType processDescriptionType = getWpsInfoInstance().getProcessDescription(processName);

        if (processDescriptionType == null) {
            throw new MotuException(String.format("WPSFactory#createExecuteProcessRequest : Unknown process name '%s'", processName));
        }
        return createExecuteProcessRequest(dataInputValues, processDescriptionType);

    }

    public Execute createExecuteProcessRequest(Map<String, ParameterValue<?>> dataInputValues, ProcessDescriptionType processDescriptionType)
            throws MotuException {

        if (processDescriptionType == null) {
            throw new MotuException("WPSFactory#createExecuteProcessRequest : processDescriptionType is null");
        }

        ProcessDescriptions processDescriptions = wpsInfo.getProcessDescriptions();
        if (processDescriptions == null) {
            throw new MotuException("WPSFactory#createExecuteProcessRequest : list of process descriptions is null");
        }

        // create the execute object
        Execute execute = createExecute(processDescriptionType);

        // loop through each expected input in the describeprocess, and set it
        // based on what we have in the provided input map.
        List<InputDescriptionType> inputs = processDescriptionType.getDataInputs().getInput();

        DataInputsType dataInputsType = objectFactoryWPS.createDataInputsType();

        for (InputDescriptionType inputDescriptionType : inputs) {

            String identifier = inputDescriptionType.getIdentifier().getValue();
            ParameterValue<?> parameterValue = dataInputValues.get(identifier);

            if (parameterValue == null) {
                continue;
            }

            Object inputValue = parameterValue.getValue();
            List<?> valueList = null;

            if (inputValue instanceof Map) {
                valueList = (List<?>) ((Map<?, ?>) inputValue).values();
            } else if (inputValue instanceof List) {
                valueList = (List<?>) inputValue;
            } else if (inputValue instanceof Collection) {
                throw new MotuException(
                        String
                                .format("WPSFactory#createExecuteProcessRequest : the value of the parameter '%s' is a collection which is not supported : '%s'",
                                        identifier,
                                        inputValue.getClass()));
            } else {
                List<Object> list = new ArrayList<Object>();
                list.add(inputValue);
                valueList = list;
            }

            if (valueList.isEmpty()) {
                continue;
            }

            Object object = valueList.get(0);
            @SuppressWarnings("unchecked")
            ParameterDescriptor parameterDescriptor = new DefaultParameterDescriptor(identifier, null, object.getClass(), null, true);
            @SuppressWarnings("unchecked")
            Parameter parameterValueUsed = new Parameter(parameterDescriptor);

            for (Object inValue : valueList) {

                parameterValueUsed.setValue(inValue);

                InputType inputType = createInputType(inputDescriptionType);
                DataType dataType = createInputDataType(inputDescriptionType, parameterValueUsed);

                if (dataType == null) {
                    continue;
                }

                inputType.setData(dataType);
                // inputType.setReference(value)

                dataInputsType.getInput().add(inputType);
            }

        }

        execute.setDataInputs(dataInputsType);

        return execute;
        //
        // Iterator iterator = inputs.iterator();
        // while (iterator.hasNext()) { InputDescriptionType idt =
        // (InputDescriptionType) iterator.next(); String identifier = idt.getIdentifier().getValue(); Object
        // inputValue = input.get(identifier); if (inputValue != null) { // if our value is some sort of
        // collection, then created multiple // dataTypes for this inputdescriptiontype. List<DataType> list =
        // new ArrayList<DataType>(); if (inputValue instanceof Map) { for (Object inVal : ((Map)
        // inputValue).values()) { DataType createdInput = WPSUtils.createInputDataType(inVal, idt);
        // list.add(createdInput); } } else if (inputValue instanceof Collection) { for (Object inVal :
        // (Collection) inputValue) { DataType createdInput = WPSUtils.createInputDataType(inVal, idt);
        // list.add(createdInput); } } else { // our value is a single object so create a single datatype for
        // it DataType createdInput = WPSUtils.createInputDataType(inputValue, idt); list.add(createdInput); }
        // // add the input to the execute request exeRequest.addInput(identifier, list); } }
        //  
        // // send the request and get the response ExecuteProcessResponse response; try { response =
        // wps.issueRequest(exeRequest); } catch (ServiceException e) { return null; } catch (IOException e) {
        // return null; }
        //  
        // // if there is an exception in the response, return null // TODO: properly handle the exception? if
        // (response.getExceptionResponse() != null || response.getExecuteResponse() == null) { return null; }
        //  
        // // get response object and create a map of outputs from it ExecuteResponseType executeResponse =
        // response.getExecuteResponse();
        //  
        // // create the result map of outputs Map<String, Object> results = new TreeMap<String, Object>();
        // results = WPSUtils.createResultMap(executeResponse, results);
        //  
        // return results;
        // 

    }

    public Execute createExecute(ProcessDescriptionType processDescriptionType) throws MotuException {

        if (wpsInfo == null) {
            throw new MotuException("WPSFactory#createExecute : WPS info is null");
        }
        if (processDescriptionType == null) {
            throw new MotuException("WPSFactory#createExecute : processDescriptionType is null");
        }

        ProcessDescriptions processDescriptions = wpsInfo.getProcessDescriptions();
        if (processDescriptions == null) {
            throw new MotuException("WPSFactory#createExecuteProcessRequest : list of process descriptions is null");
        }

        Execute execute = objectFactoryWPS.createExecute();
        execute.setIdentifier(cloneCodeType(processDescriptionType.getIdentifier()));
        execute.setService(processDescriptions.getService());
        execute.setVersion(processDescriptions.getVersion());
        execute.setLanguage(processDescriptions.getLang());

        return execute;

    }

    public InputType createInputType(InputDescriptionType inputDescriptionType) throws MotuException {

        if (inputDescriptionType == null) {
            throw new MotuException("WPSFactory#createInputType : inputDescriptionType is null");
        }

        InputType inputType = objectFactoryWPS.createInputType();
        inputType.setIdentifier(cloneCodeType(inputDescriptionType.getIdentifier()));
        inputType.setAbstract(inputDescriptionType.getAbstract());
        inputType.setTitle(inputDescriptionType.getTitle());

        return inputType;
    }

    public CodeType cloneCodeType(CodeType identifier) throws MotuException {
        if (identifier == null) {
            throw new MotuException("WPSFactory#cloneCodeType : identifier is null");
        }

        CodeType newIdentifier = objectFactoryOWS.createCodeType();
        newIdentifier.setValue(identifier.getValue());
        newIdentifier.setCodeSpace(identifier.getCodeSpace());
        return newIdentifier;
    }

    // public DataType createInputDataType(InputDescriptionType inputDescriptionType, ParameterValue<?>
    // parameterValue) throws MotuException {
    //        
    // return createInputDataType(inputDescriptionType, parameterValue.getValue());
    //        
    // }
    public DataType createInputDataType(InputDescriptionType inputDescriptionType, ParameterValue<?> parameterValue) throws MotuException {
        if (inputDescriptionType == null) {
            throw new MotuException("WPSFactory#createInputDataType : createInputDataType is null");
        }

        DataType dataType = null;

        if (WPSInfo.isLiteralData(inputDescriptionType)) {

            dataType = createLiteralDataType(inputDescriptionType.getLiteralData(), parameterValue);

        } else if (WPSInfo.isBoundingBoxData(inputDescriptionType)) {

            dataType = createBoundingBoxInputType(inputDescriptionType.getBoundingBoxData(), parameterValue);

        } else if (WPSInfo.isComplexData(inputDescriptionType)) {

            dataType = createComplexDataType(inputDescriptionType.getComplexData(), parameterValue);

        } else {
            throw new MotuException(String.format("WPSFactory#createInputDataType : Identifer '%s' : Unknown input data type", inputDescriptionType
                    .getIdentifier().getValue()));
        }
        /*
         * int inputtype = 0;
         * 
         * // first try to figure out if the input is a literal or complex based // on the data in the idt
         * LiteralInputType literalData = idt.getLiteralData(); SupportedComplexDataInputType complexData =
         * idt.getComplexData(); if (literalData != null) { inputtype = INPUTTYPE_LITERAL; } else if
         * (complexData != null) { inputtype = INPUTTYPE_COMPLEXDATA; } else { // is the value a literal? Do a
         * very basic test here for common // literal types. TODO: figure out a more thorough test here if
         * (obj instanceof String || obj instanceof Double || obj instanceof Float || obj instanceof Integer)
         * { inputtype = INPUTTYPE_LITERAL; } else { // assume complex data inputtype = INPUTTYPE_COMPLEXDATA;
         * } }
         * 
         * // now create the input based on its type String schema = null; if (inputtype ==
         * INPUTTYPE_COMPLEXDATA) { ComplexDataCombinationsType supported = complexData.getSupported();
         * ComplexDataDescriptionType cddt = (ComplexDataDescriptionType) supported.getFormat().get(0); schema
         * = cddt.getSchema(); }
         * 
         * return createInputDataType(obj, inputtype, schema);
         */

        return dataType;
    }

    public DataType createLiteralDataType(LiteralInputType literalInputType, ParameterValue<?> parameterValue) throws MotuException {
        if (literalInputType == null) {
            throw new MotuException("WPSFactory#createLiteralDataType : literalInputType is null");
        }

        if (parameterValue == null) {
            return null;
        }

        LiteralDataType literalDataType = objectFactoryWPS.createLiteralDataType();
        literalDataType.setDataType(literalInputType.getDataType().getValue());

        try {
            literalDataType.setValue(parameterValue.getValue().toString());
        } catch (InvalidParameterTypeException e) {
            throw new MotuException(String.format("WPSFactory#createLiteralDataType : parameter '%s' : invalid value.", parameterValue.toString()), e);
        }

        DataType dataType = objectFactoryWPS.createDataType();
        dataType.setLiteralData(literalDataType);

        return dataType;

    }

    public DataType createComplexDataType(SupportedComplexDataInputType complexDataInputType, ParameterValue<?> parameterValue) throws MotuException {
        if (complexDataInputType == null) {
            throw new MotuException("WPSFactory#createComplexDataType : literalInputType is null");
        }

        if (parameterValue == null) {
            return null;
        }

        ComplexDataType complexDataType = objectFactoryWPS.createComplexDataType();
        complexDataType.getContent().add(parameterValue.getValue().toString());

        ComplexDataCombinationsType complexDataCombinationsType = complexDataInputType.getSupported();
        ComplexDataDescriptionType complexDataDescriptionType = null;

        if (complexDataCombinationsType != null) {
            complexDataDescriptionType = (ComplexDataDescriptionType) complexDataCombinationsType.getFormat().get(0);
        }

        if (complexDataDescriptionType != null) {
            complexDataType.setSchema(complexDataDescriptionType.getSchema());
            complexDataType.setEncoding(complexDataDescriptionType.getEncoding());
            complexDataType.setMimeType(complexDataDescriptionType.getMimeType());
        }

        DataType dataType = objectFactoryWPS.createDataType();
        dataType.setComplexData(complexDataType);

        return dataType;

    }

    public DataType createBoundingBoxInputType(SupportedCRSsType boundingBoxInputType, ParameterValue<?> parameterValue) throws MotuException {
        if (boundingBoxInputType == null) {
            throw new MotuException("WPSFactory#createBoundingBoxInputType : boundingBoxInputType is null");
        }
        if (parameterValue == null) {
            return null;
        }

        BoundingBoxType boundingBoxType = objectFactoryOWS.createBoundingBoxType();

        double[] values = { 0d };
        try {
            values = parameterValue.doubleValueList();
        } catch (InvalidParameterTypeException e) {
            throw new MotuException(
                    String
                            .format("WPSFactory#createBoundingBoxInputType : parameter '%s' (value type '%s') - Unable to get values : '%s' type was expected.",
                                    parameterValue.getDescriptor().getName(),
                                    parameterValue.getValue().getClass().getName(),
                                    values.getClass().getCanonicalName()),
                    e);
        }

        if (values == null) {
            return null;
        }

        switch (values.length) {
        case 4:
            boundingBoxType.getLowerCorner().add(values[0]);
            boundingBoxType.getLowerCorner().add(values[1]);
            boundingBoxType.getUpperCorner().add(values[2]);
            boundingBoxType.getUpperCorner().add(values[3]);
            break;
        case 3:
            boundingBoxType.getLowerCorner().add(values[0]);
            boundingBoxType.getLowerCorner().add(values[1]);
            boundingBoxType.getUpperCorner().add(values[2]);
            boundingBoxType.getUpperCorner().add(Double.parseDouble(ExtractCriteriaLatLon.LONGITUDE_MAX));
            break;
        case 2:
            boundingBoxType.getLowerCorner().add(values[0]);
            boundingBoxType.getLowerCorner().add(values[1]);
            boundingBoxType.getUpperCorner().add(Double.parseDouble(ExtractCriteriaLatLon.LATITUDE_MAX));
            boundingBoxType.getUpperCorner().add(Double.parseDouble(ExtractCriteriaLatLon.LONGITUDE_MAX));
            break;
        case 1:
            boundingBoxType.getLowerCorner().add(values[0]);
            boundingBoxType.getLowerCorner().add(Double.parseDouble(ExtractCriteriaLatLon.LONGITUDE_MIN));
            boundingBoxType.getUpperCorner().add(Double.parseDouble(ExtractCriteriaLatLon.LATITUDE_MAX));
            boundingBoxType.getUpperCorner().add(Double.parseDouble(ExtractCriteriaLatLon.LONGITUDE_MAX));
            break;
        default:
            return null;
        }

        boundingBoxType.setCrs(boundingBoxInputType.getDefault().getCRS());

        DataType dataType = objectFactoryWPS.createDataType();
        dataType.setBoundingBoxData(boundingBoxType);

        return dataType;

    }

    public static void marshallExecute(Execute execute, Writer writer, String schemaLocation) throws MotuMarshallException {
        if (writer == null) {
            return;
        }

        try {
            synchronized (WPSFactory.marshallerWPS) {
                WPSFactory.marshallerWPS.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "");
                if (!Organizer.isNullOrEmpty(schemaLocation)) {
                    WPSFactory.marshallerWPS.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, schemaLocation);
                }

                WPSFactory.marshallerWPS.marshal(execute, writer);
                writer.flush();
                writer.close();
            }
        } catch (JAXBException e) {
            throw new MotuMarshallException("Error in WPSFactory - marshallExecute", e);
        } catch (IOException e) {
            throw new MotuMarshallException("Error in WPSFactory - marshallExecute", e);
        }

    }

    @SuppressWarnings("unchecked")
    public static Parameter<?> createParameter(final String name, final Class<?> type, final Object value) {
        final ParameterDescriptor<?> descriptor = new DefaultParameterDescriptor(name, null, type, null, true);
        final Parameter<?> parameter = new Parameter(descriptor);
        parameter.setValue(value);
        return parameter;
    }

    public static Parameter<?> createParameter(final String name, final int value) {
        return Parameter.create(name, value);
//        final ParameterDescriptor<Integer> descriptor = new DefaultParameterDescriptor<Integer>(name, Integer.class, null, null);
//        final Parameter<Integer> parameter = new Parameter<Integer>(descriptor);
//        parameter.setValue(value);
//        return parameter;

    }

    public static Parameter<?> createParameter(final String name, final double value) {
        return Parameter.create(name, value, null);
    }

    public static Parameter<?> createParameter(final String name, final long value) {
        final ParameterDescriptor<Long> descriptor = new DefaultParameterDescriptor<Long>(name, Long.class, null, null);
        final Parameter<Long> parameter = new Parameter<Long>(descriptor);
        parameter.setValue(value);
        return parameter;
    }

}
