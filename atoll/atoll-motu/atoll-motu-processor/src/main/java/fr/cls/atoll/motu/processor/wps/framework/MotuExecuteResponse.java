package fr.cls.atoll.motu.processor.wps.framework;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

import org.joda.time.DateTime;
import org.joda.time.Period;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuInvalidDateException;
import fr.cls.atoll.motu.library.utils.ReflectionUtils;
import fr.cls.atoll.motu.library.utils.StaticResourceBackedDynamicEnum;
import fr.cls.atoll.motu.msg.xml.ErrorType;
import fr.cls.atoll.motu.msg.xml.StatusModeType;
import fr.cls.atoll.motu.processor.iso19139.OperationMetadata;
import fr.cls.atoll.motu.processor.iso19139.ServiceMetadata;
import fr.cls.atoll.motu.processor.opengis.ows110.CodeType;
import fr.cls.atoll.motu.processor.opengis.ows110.ExceptionReport;
import fr.cls.atoll.motu.processor.opengis.ows110.ExceptionType;
import fr.cls.atoll.motu.processor.opengis.wps100.ComplexDataType;
import fr.cls.atoll.motu.processor.opengis.wps100.DataType;
import fr.cls.atoll.motu.processor.opengis.wps100.ExecuteResponse;
import fr.cls.atoll.motu.processor.opengis.wps100.LiteralDataType;
import fr.cls.atoll.motu.processor.opengis.wps100.OutputDataType;
import fr.cls.atoll.motu.processor.opengis.wps100.OutputReferenceType;
import fr.cls.atoll.motu.processor.opengis.wps100.ProcessStartedType;
import fr.cls.atoll.motu.processor.opengis.wps100.StatusType;
import fr.cls.atoll.motu.processor.wps.MotuWPSProcess;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.9 $ - $Date: 2009-10-29 10:52:04 $
 */
public class MotuExecuteResponse {

    /**
     * Constructeur.
     * 
     * @param executeResponse the execute response
     * 
     * @throws MotuException the motu exception
     */
    public MotuExecuteResponse(ExecuteResponse executeResponse) throws MotuException {
        if (executeResponse == null) {
            throw new MotuException("MotuExecuteResponse constructor - enable to process - executeResponse parameter is null");
        }
        this.executeResponse = executeResponse;
    }

    /** The status types. */
    protected static StaticResourceBackedDynamicEnum<WPSStatusResponse, MotuWPSStatusType> statusTypes = new StaticResourceBackedDynamicEnum<WPSStatusResponse, MotuWPSStatusType>(
            MotuExecuteResponse.makeStatusTypeList());

    /**
     * Gets the status types.
     * 
     * @return the status types
     */
    public static StaticResourceBackedDynamicEnum<WPSStatusResponse, MotuWPSStatusType> getStatusTypes() {
        return statusTypes;
    }

    /** The execute response. */
    protected ExecuteResponse executeResponse = null;

    /**
     * Gets the execute response.
     * 
     * @return the execute response
     */
    public ExecuteResponse getExecuteResponse() {
        return executeResponse;
    }

    /**
     * The Enum WPSStatusResponse.
     */
    public enum WPSStatusResponse {

        /** The ACCEPTED. */
        ACCEPTED(0),

        /** The STARTED. */
        STARTED(1),

        /** The PAUSED. */
        PAUSED(2),

        /** The SUCCEEDED. */
        SUCCEEDED(3),

        /** The FAILED. */
        FAILED(4);

        /** The value. */
        private final int value;

        /**
         * Instantiates a new format.
         * 
         * @param v the v
         */
        WPSStatusResponse(int v) {
            value = v;
        }

        /**
         * Value.
         * 
         * @return the int
         */
        public int value() {
            return value;
        }

        /**
         * From value.
         * 
         * @param v the v
         * 
         * @return the format
         */
        public static WPSStatusResponse fromValue(int v) {
            for (WPSStatusResponse c : WPSStatusResponse.values()) {
                if (c.value == v) {
                    return c;
                }
            }
            throw new IllegalArgumentException(String.valueOf(v));
        }

    }

    /**
     * The Class MotuProcessAccepted.
     */
    public final class MotuProcessAccepted {

    }

    /**
     * The Class MotuProcessStartedType.
     */
    public final class MotuProcessStartedType extends ProcessStartedType {

    }

    /**
     * Gets the status.
     * 
     * @return the status
     */
    public StatusType getStatus() {
        if (executeResponse == null) {
            return null;
        }

        return executeResponse.getStatus();

    }

    /**
     * Gets the status location.
     * 
     * @return the status location
     */
    public String getStatusLocation() {
        if (executeResponse == null) {
            return null;
        }

        return executeResponse.getStatusLocation();

    }

    /**
     * Gets the status as string.
     * 
     * @return the status as string
     */
    public String getStatusAsString() {

        StatusType statusType = getStatus();
        if (statusType == null) {
            return null;
        }
        String status = "";

        if (statusType.getProcessAccepted() != null) {

            status = statusTypes.backingValueOf(WPSStatusResponse.ACCEPTED).name();
        }
        if (statusType.getProcessStarted() != null) {
            status = statusTypes.backingValueOf(WPSStatusResponse.STARTED).name();
        }
        if (statusType.getProcessPaused() != null) {
            status = statusTypes.backingValueOf(WPSStatusResponse.PAUSED).name();
        }
        if (statusType.getProcessSucceeded() != null) {
            status = statusTypes.backingValueOf(WPSStatusResponse.SUCCEEDED).name();
        }
        if (statusType.getProcessFailed() != null) {
            status = statusTypes.backingValueOf(WPSStatusResponse.FAILED).name();
        }

        return status;
    }

    /**
     * Gets the status as wps status response.
     * 
     * @return the status as wps status response
     */
    public WPSStatusResponse getStatusAsWPSStatusResponse() {
        String status = getStatusAsString();

        if (WPSUtils.isNullOrEmpty(status)) {
            return null;
        }
        return statusTypes.valueOf(status);
    }

    /**
     * Checks if is status accepted.
     * 
     * @return true, if is status accepted
     */
    public boolean isStatusAccepted() {
        WPSStatusResponse statusResponse = getStatusAsWPSStatusResponse();
        if (statusResponse == null) {
            return false;
        }
        return statusResponse.equals(WPSStatusResponse.ACCEPTED);

    }

    /**
     * Checks if is status started.
     * 
     * @return true, if is status started
     */
    public boolean isStatusStarted() {
        WPSStatusResponse statusResponse = getStatusAsWPSStatusResponse();
        if (statusResponse == null) {
            return false;
        }
        return statusResponse.equals(WPSStatusResponse.STARTED);
    }

    /**
     * Checks if is status paused.
     * 
     * @return true, if is status paused
     */
    public boolean isStatusPaused() {
        WPSStatusResponse statusResponse = getStatusAsWPSStatusResponse();
        if (statusResponse == null) {
            return false;
        }
        return statusResponse.equals(WPSStatusResponse.PAUSED);
    }

    /**
     * Checks if is status succeeded.
     * 
     * @return true, if is status succeeded
     */
    public boolean isStatusSucceeded() {
        WPSStatusResponse statusResponse = getStatusAsWPSStatusResponse();
        if (statusResponse == null) {
            return false;
        }
        return statusResponse.equals(WPSStatusResponse.SUCCEEDED);
    }

    /**
     * Checks if is status failed.
     * 
     * @return true, if is status failed
     */
    public boolean isStatusFailed() {
        WPSStatusResponse statusResponse = getStatusAsWPSStatusResponse();
        if (statusResponse == null) {
            return false;
        }
        return statusResponse.equals(WPSStatusResponse.FAILED);
    }

    /**
     * Checks if is process done.
     * 
     * @return true, if is process done
     */
    public boolean isProcessDone() {
        return isStatusFailed() || isStatusSucceeded();
    }

    /**
     * Checks if is process in progress.
     * 
     * @return true, if is process in progress
     */
    public boolean isProcessInProgress() {
        return !isProcessDone();
    }

    /**
     * Gets the status type enum.
     * 
     * @return the status type enum
     */
    @SuppressWarnings("unchecked")
    public static Enumeration<String> getStatusTypeEnum() {
        AnnotatedElement annotatedElement = StatusType.class;

        XmlType xmlType = annotatedElement.getAnnotation(XmlType.class);
        if (xmlType == null) {
            return null;
        }

        return (Enumeration<String>) ReflectionUtils.makeEnumeration(xmlType.propOrder());

    }

    /**
     * Make status type list.
     * 
     * @return the list< motu wps status type>
     */
    protected static List<MotuWPSStatusType> makeStatusTypeList() {
        AnnotatedElement annotatedElement = StatusType.class;

        XmlType xmlType = annotatedElement.getAnnotation(XmlType.class);
        if (xmlType == null) {
            return null;
        }

        List<MotuWPSStatusType> list = new ArrayList<MotuWPSStatusType>();
        String[] status = xmlType.propOrder();
        for (int i = 0; i < status.length; i++) {
            list.add(new MotuWPSStatusType(WPSStatusResponse.fromValue(i), status[i]));
        }

        return list;

    }

    /**
     * Format exception report message.
     * 
     * @param exceptionReport the exception report
     * 
     * @return the string
     */
    public String formatExceptionReportMessage(ExceptionReport exceptionReport) {

        if (exceptionReport == null) {
            return "";
        }

        List<ExceptionType> listExceptionType = exceptionReport.getException();
        StringBuilder stringBuilder = new StringBuilder();

        for (ExceptionType exceptionType : listExceptionType) {
            stringBuilder.append("Exception Code: ");
            stringBuilder.append(exceptionType.getExceptionCode());
            stringBuilder.append(" - Exception Locator: ");
            stringBuilder.append(exceptionType.getLocator());
            stringBuilder.append(" - Exception Text: ");

            for (String text : exceptionType.getExceptionText()) {
                stringBuilder.append(text);
                stringBuilder.append("\n");
            }
        }
        return stringBuilder.toString();

    }

    /**
     * Gets the process status message.
     * 
     * @return the process status message
     */
    public String getProcessStatusMessage() {
        String msg = "No process response";

        if (executeResponse == null) {
            return msg;
        }
        if (isStatusAccepted()) {
            msg = executeResponse.getStatus().getProcessAccepted();
        }
        if (isStatusStarted()) {
            msg = executeResponse.getStatus().getProcessStarted().getValue();
        }
        if (isStatusSucceeded()) {
            msg = executeResponse.getStatus().getProcessSucceeded();
        }
        if (isStatusPaused()) {
            msg = executeResponse.getStatus().getProcessPaused().getValue();
        }
        if (isStatusFailed()) {
            msg = formatExceptionReportMessage(executeResponse.getStatus().getProcessFailed().getExceptionReport());
        }

        return msg;

    }

    /**
     * Gets the motu status message.
     * 
     * @return the motu status message
     * 
     * @throws MotuException the motu exception
     */
    public String getMotuResponseMessage() throws MotuException {
        
        return (String) getResponseValue(MotuWPSProcess.PARAM_MESSAGE);
    }
    
    /**
     * Gets the motu status code.
     * 
     * @return the motu status code
     * 
     * @throws MotuException the motu exception
     */
    public ErrorType getMotuResponseCode() throws MotuException {
        
        return (ErrorType) getResponseValue(MotuWPSProcess.PARAM_CODE);
    }
    public StatusModeType getMotuResponseStatus() throws MotuException {
        
        return (StatusModeType) getResponseValue(MotuWPSProcess.PARAM_MODE_STATUS);
    }
    
    /**
     * Gets the motu response url.
     * 
     * @return the motu response url
     * 
     * @throws MotuException the motu exception
     */
    public String getMotuResponseUrl() throws MotuException {
        
        return (String) getResponseValue(MotuWPSProcess.PARAM_URL);
    }
    
    /**
     * Gets the motu response local url.
     * 
     * @return the motu response local url
     * 
     * @throws MotuException the motu exception
     */
    public String getMotuResponseLocalUrl() throws MotuException {
        
        return (String) getResponseValue(MotuWPSProcess.PARAM_LOCAL_URL);
    }
    
    /**
     * Gets the response value.
     * 
     * @param parameterName the parameter name
     * 
     * @return the response value
     * 
     * @throws MotuException the motu exception
     */
    public Object getResponseValue(String parameterName) throws MotuException {
        
        String msg = "No process response";

        if (executeResponse == null) {
            return msg;
        }

        msg = "No process outputs response";

        ExecuteResponse.ProcessOutputs processOutputs = executeResponse.getProcessOutputs();
        if (processOutputs == null) {
            return msg;
        }

        List<OutputDataType> outputDataTypeList = processOutputs.getOutput();

        if (outputDataTypeList == null) {
            return msg;
        }

        Object value = null;
        for (OutputDataType outputDataType : outputDataTypeList) {
            CodeType identifier = outputDataType.getIdentifier();
            
            if (identifier.getValue().equals(parameterName)) {
                
                value = getResponseValue(outputDataType);
                break;
            }
        }
        
        return value;
        
    }
    
    /**
     * Gets the response value.
     * 
     * @param outputDataType the output data type
     * 
     * @return the response value
     * 
     * @throws MotuException the motu exception
     */
    public Object getResponseValue(OutputDataType outputDataType) throws MotuException {

        if (outputDataType == null) {
            return null;
        }

        // -------- Data output is a Reference (url)
        OutputReferenceType outputReferenceType = outputDataType.getReference();

        if (outputReferenceType != null) {
            return outputReferenceType.getHref();
        }

        // -------- Data output is a NOT a Reference (url)
        DataType dataType = outputDataType.getData();

        Object value = null;

        if (WPSInfo.isLiteralData(dataType)) {
            value = getResponseValue(dataType.getLiteralData());
        } else if (WPSInfo.isComplexData(dataType)) {
            value = getResponseValue(dataType.getComplexData());
            
        } else if (WPSInfo.isBoundingBoxData(dataType)) {
            // TODO
            
        }

        return value;
    }

    /**
     * Gets the response value.
     * 
     * @param literalDataType the literal data type
     * 
     * @return the response value
     * 
     * @throws MotuException the motu exception
     */
    @SuppressWarnings("unchecked")
    public Object getResponseValue(LiteralDataType literalDataType) throws MotuException {

        if (literalDataType == null) {
            return null;
        }
        String valueType = literalDataType.getDataType();
        if (WPSUtils.isNullOrEmpty(valueType)) {
            throw new MotuException("MotuExecuteResponse#getResponseValue - Data type of a literal output data is null.");
        }

        Class<?> clazz = OperationMetadata.XML_JAVA_CLASS_MAPPING.get(valueType);
        if (clazz == null) {
            throw new MotuException(String.format("MotuExecuteResponse#getResponseValue - Data type '%s' is not mapped to a java class.", valueType));
        }

        String nativeValue = literalDataType.getValue();

        Object returnedObject = null;
        
        Class<?> nativeClazz = nativeValue.getClass();
        
        try {

            if (DateTime.class.equals(clazz)) {
                returnedObject = WPSFactory.stringToDateTime(nativeValue);

            } else if (Period.class.equals(clazz)) {
                returnedObject = WPSFactory.stringToPeriod(nativeValue);
            } else if (clazz.isEnum()) {
                returnedObject = Enum.valueOf((Class)clazz, nativeValue);
            } else {
                Constructor<?> ctor = clazz.getConstructor(nativeClazz);
                returnedObject = ctor.newInstance(nativeValue);
            }

//            System.out.print(returnedObject.getClass().getName());
//            System.out.print(" --> ");
//            System.out.println(returnedObject);

        } catch (Exception e) {
            throw new MotuException("ERROR in MotuExecuteResponse#getResponseValue.", e);

        }
        
        return returnedObject;
    }
    
    public Object getResponseValue(ComplexDataType complexDataType) throws MotuException {

        if (complexDataType == null) {
            return null;
        }
        String encoding = complexDataType.getEncoding();
        Object valueType = complexDataType.getContent().get(0);

        Charset charset = Charset.forName(encoding);
        CharsetDecoder decoder = charset.newDecoder();
        CharsetEncoder encoder = charset.newEncoder();

        Object returnedObject = valueType;

        System.out.print(returnedObject.getClass().getName());
        System.out.print(" --> ");
        System.out.println(returnedObject);

//        
//        charToWrite = charset.decode(valueType).toString();
//        ByteBuffer bbuf = encoder.encode(CharBuffer.wrap("a string"));
//
//
//        if (WPSUtils.isNullOrEmpty(valueType)) {
//            throw new MotuException("MotuExecuteResponse#getResponseValue - Data type of a literal output data is null.");
//        }
//
//        Class<?> clazz = OperationMetadata.XML_JAVA_CLASS_MAPPING.get(valueType);
//        if (clazz == null) {
//            throw new MotuException(String.format("MotuExecuteResponse#getResponseValue - Data type '%s' is not mapped to a java class.", valueType));
//        }
//
//        String nativeValue = literalDataType.getValue();
//
//        
//        Class<?> nativeClazz = nativeValue.getClass();
//        
//        try {
//
//            if (DateTime.class.equals(clazz)) {
//                returnedObject = WPSFactory.stringToDateTime(nativeValue);
//
//            } else if (Period.class.equals(clazz)) {
//                returnedObject = WPSFactory.stringToPeriod(nativeValue);
//            } else if (clazz.isEnum()) {
//                returnedObject = Enum.valueOf((Class)clazz, nativeValue);
//            } else {
//                Constructor<?> ctor = clazz.getConstructor(nativeClazz);
//                returnedObject = ctor.newInstance(nativeValue);
//            }
//
////            System.out.print(returnedObject.getClass().getName());
////            System.out.print(" --> ");
////            System.out.println(returnedObject);
//
//        } catch (Exception e) {
//            throw new MotuException("ERROR in MotuExecuteResponse#getResponseValue.", e);
//
//        }
//        
        return returnedObject;
    }    
}
