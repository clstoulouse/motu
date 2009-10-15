package fr.cls.atoll.motu.processor.wps.framework;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.annotation.XmlType;

import opendap.servlet.GetAsciiHandler;
import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.intfce.Organizer.Format;
import fr.cls.atoll.motu.library.utils.ReflectionUtils;
import fr.cls.atoll.motu.library.utils.StaticResourceBackedDynamicEnum;
import fr.cls.atoll.motu.processor.opengis.ows110.ExceptionReport;
import fr.cls.atoll.motu.processor.opengis.ows110.ExceptionType;
import fr.cls.atoll.motu.processor.opengis.wps100.ExecuteResponse;
import fr.cls.atoll.motu.processor.opengis.wps100.ProcessStartedType;
import fr.cls.atoll.motu.processor.opengis.wps100.StatusType;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: jarnaud $
 * @version $Revision: 1.3 $ - $Date: 2009-10-15 15:32:32 $
 */
public class MotuExecuteResponse {

    /**
     * Constructeur.
     * 
     * @throws MotuException
     */
    public MotuExecuteResponse(ExecuteResponse executeResponse) throws MotuException {
        if (executeResponse == null) {
            throw new MotuException("MotuExecuteResponse constructor - enable to process - executeResponse parameter is null");
        }
        this.executeResponse = executeResponse;
    }

    protected static StaticResourceBackedDynamicEnum<WPSStatusResponse, MotuWPSStatusType> statusTypes = new StaticResourceBackedDynamicEnum<WPSStatusResponse, MotuWPSStatusType>(MotuExecuteResponse.makeStatusTypeList());

    public static StaticResourceBackedDynamicEnum<WPSStatusResponse, MotuWPSStatusType> getStatusTypes() {
        return statusTypes;
    }

    protected ExecuteResponse executeResponse = null;

    public ExecuteResponse getExecuteResponse() {
        return executeResponse;
    }

    public enum WPSStatusResponse {
        ACCEPTED(0),
        STARTED(1),
        PAUSED(2),
        SUCCEEDED(3),
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
    public final class MotuProcessAccepted {
        
    }
    public final class MotuProcessStartedType extends ProcessStartedType {
        
    }
    public StatusType getStatus() {
        if (executeResponse == null) {
            return null;
        }

        return executeResponse.getStatus();

    }

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
    
    public WPSStatusResponse getStatusAsWPSStatusResponse() {
        return statusTypes.valueOf(getStatusAsString());
    }

    public boolean isStatusAccepted() {
        return getStatusAsWPSStatusResponse().equals(WPSStatusResponse.ACCEPTED);
    }
    public boolean isStatusStarted() {
        return getStatusAsWPSStatusResponse().equals(WPSStatusResponse.STARTED);
    }
    public boolean isStatusPaused() {
        return getStatusAsWPSStatusResponse().equals(WPSStatusResponse.PAUSED);
    }
    public boolean isStatusSucceeded() {
        return getStatusAsWPSStatusResponse().equals(WPSStatusResponse.SUCCEEDED);
    }
    public boolean isStatusFailed() {
        return getStatusAsWPSStatusResponse().equals(WPSStatusResponse.FAILED);
    }
    public boolean isProcessDone() {
        return isStatusFailed() || isStatusSucceeded();
    }
    public boolean isProcessInProgress() {
        return !isProcessDone();
    }

    @SuppressWarnings("unchecked")
    public static Enumeration<String> getStatusTypeEnum() {
        AnnotatedElement annotatedElement = StatusType.class;

        XmlType xmlType = annotatedElement.getAnnotation(XmlType.class);
        if (xmlType == null) {
            return null;
        }

        return (Enumeration<String>) ReflectionUtils.makeEnumeration(xmlType.propOrder());

    }

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
    
    public String formatExceptionReportMessage(ExceptionReport exceptionReport) {
        
       List<ExceptionType> listExceptionType = exceptionReport.getException();
       
       for (ExceptionType exceptionType : listExceptionType) {
       }
       return "";
        
    }
    public void getProcessStatusMessage() {
        String msg = "";
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

    }

}
