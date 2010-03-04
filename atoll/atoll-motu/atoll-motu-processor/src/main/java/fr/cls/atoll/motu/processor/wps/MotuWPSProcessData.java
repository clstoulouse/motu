package fr.cls.atoll.motu.processor.wps;

import java.util.List;

import org.apache.log4j.Logger;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.BoundingBoxInput;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.input.LiteralInput;
import org.deegree.services.wps.input.ProcessletInput;
import org.deegree.services.wps.output.LiteralOutput;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.5 $ - $Date: 2010-03-04 16:02:54 $
 */
public class MotuWPSProcessData {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(MotuWPSProcessData.class);

    // /** The service name param. */
    // protected LiteralInput serviceNameParam = null;
    //
    // /** The location data param. */
    // protected LiteralInput locationDataParam = null;
    //
    // /** The product id param. */
    // protected LiteralInput productIdParam = null;

    /** The service name. */
    protected String serviceName = null;

    /** The location data. */
    protected String locationData = null;

    /** The product id. */
    protected String productId = null;

    /** The processlet inputs. */
    protected ProcessletInputs processletInputs;

    /** The processlet outputs. */
    protected ProcessletOutputs processletOutputs;

    /** The processlet execution info. */
    protected ProcessletExecutionInfo processletExecutionInfo;

    protected long requestId = -1;

    public MotuWPSProcessData() {
    }

    public LiteralInput getStartDateParamIn() {
        return (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_STARTTIME);
    }

    public LiteralInput getEndDateParamIn() {
        return (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_ENDTIME);
    }

    public LiteralInput getServiceNameParamIn() {
        return (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_SERVICE);

    }

    public LiteralInput getLocationDataParamIn() {
        return (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_URL);
    }

    public LiteralInput getProductIdParamIn() {
        return (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_PRODUCT);
    }

    public ComplexInput getRequestIdParamIn() {
        return (ComplexInput) processletInputs.getParameter(MotuWPSProcess.PARAM_REQUESTID);
    }

    public LiteralInput getDataFormatParamIn() {
        return (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_DATAFORMAT);
    }

    public BoundingBoxInput getGeobboxParamIn() {
        return (BoundingBoxInput) processletInputs.getParameter(MotuWPSProcess.PARAM_GEOBBOX);
    }

    public LiteralInput getLowDepthParamIn() {
        return (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_LOWDEPTH);
    }

    public LiteralInput getHighDepthParamIn() {
        return (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_HIGHDEPTH);
    }

    public LiteralInput getAnonymousUserAsStringParamIn() {
        return (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_ANONYMOUS);
    }

    public LiteralInput getIsBatchParamIn() {
        return (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_BATCH);
    }

    public LiteralInput getLoginParamIn() {
        return (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_LOGIN);
    }

    public LiteralInput getPriorityParamIn() {
        return (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_PRIORITY);
    }
    
    public ComplexInput getFromParamIn() {
        return (ComplexInput) processletInputs.getParameter(MotuWPSProcess.PARAM_FROM);
    }

    public ComplexInput getToParamIn() {
        return (ComplexInput) processletInputs.getParameter(MotuWPSProcess.PARAM_TO);
    }

    public LiteralInput getUserFromParamIn() {
        return (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_USERFROM);
    }
    
    public LiteralInput getUserToParamIn() {
        return (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_USERTO);
    }
    
    public LiteralInput getPwdFromParamIn() {
        return (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_PWDFROM);
    }
    
    public LiteralInput getPwdToParamIn() {
        return (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_PWDTO);
    }
    
    public LiteralInput getRemoveParamIn() {
        return (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_REMOVE);
    }
    
    public LiteralInput getRenameParamIn() {
        return (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_RENAME);
    }

    public List<ProcessletInput> getVariablesParamIn() {
        return processletInputs.getParameters(MotuWPSProcess.PARAM_VARIABLE);
    }

//    public void setReturnCode(String code, String msg) {
//        synchronized (processletOutputs) {
//            if (processletOutputs == null) {
//                return;
//            }
//
//            LiteralOutput codeParam = (LiteralOutput) processletOutputs.getParameter(MotuWPSProcess.PARAM_CODE);
//            LiteralOutput msgParam = (LiteralOutput) processletOutputs.getParameter(MotuWPSProcess.PARAM_MESSAGE);
//
//            if ((codeParam != null) && (code != null)) {
//                codeParam.setValue(code);
//            }
//            if ((msgParam != null) && (msg != null)) {
//                // Message can contains some "invalid" char which must no be parse par XMl (Jaxb).
//                // Set message value into a  CDATA section 
//                
//                StringBuffer  stringBuffer = new StringBuffer();
//                stringBuffer.append("<![CDATA[");
//                stringBuffer.append(msg);
//                stringBuffer.append("]]>");               
//                
//                msgParam.setValue(stringBuffer.toString());
//            }
//        }
//
//    }

    /**
     * Valeur de serviceName.
     * 
     * @return la valeur.
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Valeur de serviceName.
     * 
     * @param serviceName nouvelle valeur.
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Valeur de locationData.
     * 
     * @return la valeur.
     */
    public String getLocationData() {
        return locationData;
    }

    /**
     * Valeur de locationData.
     * 
     * @param locationData nouvelle valeur.
     */
    public void setLocationData(String locationData) {
        this.locationData = locationData;
    }

    /**
     * Valeur de productId.
     * 
     * @return la valeur.
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Valeur de productId.
     * 
     * @param productId nouvelle valeur.
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * Valeur de processletInputs.
     * 
     * @return la valeur.
     */
    public ProcessletInputs getProcessletInputs() {
        return processletInputs;
    }

    /**
     * Valeur de processletInputs.
     * 
     * @param processletInputs nouvelle valeur.
     */
    public void setProcessletInputs(ProcessletInputs processletInputs) {
        this.processletInputs = processletInputs;
    }

    /**
     * Valeur de processletOutputs.
     * 
     * @return la valeur.
     */
    public ProcessletOutputs getProcessletOutputs() {
        return processletOutputs;
    }

    /**
     * Valeur de processletOutputs.
     * 
     * @param processletOutputs nouvelle valeur.
     */
    public void setProcessletOutputs(ProcessletOutputs processletOutputs) {
        this.processletOutputs = processletOutputs;
    }

    /**
     * Valeur de processletExecutionInfo.
     * 
     * @return la valeur.
     */
    public ProcessletExecutionInfo getProcessletExecutionInfo() {
        return processletExecutionInfo;
    }

    /**
     * Valeur de processletExecutionInfo.
     * 
     * @param processletExecutionInfo nouvelle valeur.
     */
    public void setProcessletExecutionInfo(ProcessletExecutionInfo processletExecutionInfo) {
        this.processletExecutionInfo = processletExecutionInfo;
    }

    public LiteralOutput getStartDateParamOut() {
        return (LiteralOutput) processletOutputs.getParameter(MotuWPSProcess.PARAM_STARTTIME);
    }
    public LiteralOutput getEndDateParamOut() {
        return (LiteralOutput) processletOutputs.getParameter(MotuWPSProcess.PARAM_ENDTIME);
    }

    /**
     * Valeur de requestId.
     * 
     * @return la valeur.
     */
    public long getRequestId() {
        return requestId;
    }

    /**
     * Valeur de requestId.
     * 
     * @param requestId nouvelle valeur.
     */
    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

}
