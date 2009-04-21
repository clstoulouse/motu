package fr.cls.atoll.motu.processor.wps;

import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.output.ComplexOutput;
import org.deegree.services.wps.output.LiteralOutput;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.exception.MotuInvalidRequestIdException;
import fr.cls.atoll.motu.library.exception.MotuMarshallException;
import fr.cls.atoll.motu.library.intfce.ExtractionParameters;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.atoll.motu.msg.xml.StatusModeResponse;
import fr.cls.atoll.motu.msg.xml.StatusModeType;

/**
 * The purpose of this {@link Processlet} is to provide the time coverage of a product.
 * 
 * @author last edited by: $Author: dearith $
 * @version $Revision: 1.1 $, $Date: 2009-04-21 14:51:45 $
 */
public class ExtractedProductUrl extends MotuWPSProcess {

    /**
     * Constructeur.
     */
    public ExtractedProductUrl() {
    }

    /** The Constant LOG. */
    private static final Logger LOG = Logger.getLogger(ExtractedProductUrl.class);

    
    /** {@inheritDoc} */
    @Override
    public void process(ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info) throws ProcessletException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("BEGIN ExtractedProductUrl.process(), context: " + OGCFrontController.getContext());
        }

        super.process(in, out, info);

        try {
            getExtractedUrl();
        } catch (MotuException e) {
            setReturnCode(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("ProductExtractionProcess#destroy() called");
        }
    }

    
    /** {@inheritDoc} */
    @Override
    public void init() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("ProductExtractionProcess#init() called");
        }
        super.init();
    }
    


    public void setUrl(StatusModeResponse statusModeResponse) throws MotuException {

        ExtractedProductUrl.setUrl(processletOutputs, statusModeResponse);

    }

     private void getExtractedUrl() throws MotuException  {
         
         long requestId = getRequestIdAsLong();
         
         StatusModeResponse statusModeResponse = requestManagement.getResquestStatusMap(requestId);
         if (statusModeResponse == null) {
             setReturnCode(new MotuInvalidRequestIdException(requestId));
             return;            
         }
         
         setReturnCode(statusModeResponse);
         setUrl(statusModeResponse);
    }


    /**
     * Sets the resquest id.
     * 
     * @param response the response
     * @param requestId the request id
     * @throws MotuException
     */
    public static void setUrl(ProcessletOutputs response, StatusModeResponse statusModeResponse) throws MotuException {
        if (response == null) {
            return;
        }
        if (statusModeResponse == null) {
            return;
        }

        setUrl(response, statusModeResponse.getMsg());
        
    }


    public static void setUrl(ProcessletOutputs response, String url) throws MotuException {

        if (response == null) {
            return;
        }
        ComplexOutput urlParam = (ComplexOutput) response.getParameter(MotuWPSProcess.PARAM_URL);

        if ((urlParam == null) || (url == null)) {
            return;
        }

        try {
            urlParam.getBinaryOutputStream().write(url.getBytes());
        } catch (IOException e) {
            throw new MotuException("ERROR ExtractProductUrl#setUrl", e);
        }
        

    }

    public static void setReturnCode(ProcessletOutputs response, StatusModeResponse statusModeResponse) {

        MotuWPSProcess.setReturnCode(response, statusModeResponse.getCode(), statusModeResponse.getMsg());

    }

}
