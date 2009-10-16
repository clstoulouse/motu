package fr.cls.atoll.motu.processor.wps.framework;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.utils.ReflectionUtils;
import fr.cls.atoll.motu.library.utils.StaticResourceBackedDynamicEnum;
import fr.cls.atoll.motu.processor.iso19139.OperationMetadata;
import fr.cls.atoll.motu.processor.iso19139.ServiceMetadata;
import fr.cls.atoll.motu.processor.opengis.ows110.CodeType;
import fr.cls.atoll.motu.processor.opengis.ows110.ExceptionReport;
import fr.cls.atoll.motu.processor.opengis.ows110.ExceptionType;
import fr.cls.atoll.motu.processor.opengis.wps100.DataType;
import fr.cls.atoll.motu.processor.opengis.wps100.ExecuteResponse;
import fr.cls.atoll.motu.processor.opengis.wps100.OutputDataType;
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
 * @version $Revision: 1.4 $ - $Date: 2009-10-16 13:06:54 $
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
    protected static StaticResourceBackedDynamicEnum<WPSStatusResponse, MotuWPSStatusType> statusTypes = new StaticResourceBackedDynamicEnum<WPSStatusResponse, MotuWPSStatusType>(MotuExecuteResponse.makeStatusTypeList());

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
        
        if (ServiceMetadata.isNullOrEmpty(status)) {
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
        for (int i = 0 ;  i < status.length ; i++) {
            list.add(new MotuWPSStatusType(WPSStatusResponse.fromValue(i), status[i] ));
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
     */
    public String getMotuStatusMessage() {
        String msg = "No process response";

        if (executeResponse == null) {
            return msg;
        }

        msg = "No process outputs response";

        ExecuteResponse.ProcessOutputs processOutputs = executeResponse.getProcessOutputs();
        if (processOutputs == null) {
            return msg;
        }

        List<OutputDataType>  outputDataTypeList = processOutputs.getOutput();
        
        if (outputDataTypeList == null) {
            return msg;
        }
        
        for (OutputDataType outputDataType : outputDataTypeList) {
            CodeType identifier = outputDataType.getIdentifier();
            if (identifier.getValue().equals(MotuWPSProcess.PARAM_MESSAGE)) {
                DataType dataType = outputDataType.getData();
                if (WPSInfo.isLiteralData(dataType)) {
                    String valueType = dataType.getLiteralData().getDataType();
                    Class<?> clazz = OperationMetadata.XML_JAVA_CLASS_MAPPING.get(valueType);
                    try {
                        Constructor<?> ctor = clazz.getConstructor();
                        Object object = ctor.newInstance();
                        object = dataType.getLiteralData().getValue();
                    } catch (SecurityException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        
                    } catch (NoSuchMethodException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }
            
        }
        return msg;

    }

}
