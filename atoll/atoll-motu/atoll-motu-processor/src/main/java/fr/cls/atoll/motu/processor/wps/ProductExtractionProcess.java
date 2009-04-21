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
import fr.cls.atoll.motu.library.exception.MotuMarshallException;
import fr.cls.atoll.motu.library.intfce.ExtractionParameters;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.atoll.motu.msg.xml.StatusModeResponse;
import fr.cls.atoll.motu.msg.xml.StatusModeType;

/**
 * The purpose of this {@link Processlet} is to provide the time coverage of a product.
 * 
 * @author last edited by: $Author: dearith $
 * @version $Revision: 1.6 $, $Date: 2009-04-21 14:51:45 $
 */
public class ProductExtractionProcess extends MotuWPSProcess {

    /**
     * Constructeur.
     */
    public ProductExtractionProcess() {
    }

    /** The Constant LOG. */
    private static final Logger LOG = Logger.getLogger(ProductExtractionProcess.class);

    protected boolean isRequestIdSet = false;
    
    /** {@inheritDoc} */
    @Override
    public void process(ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info) throws ProcessletException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("BEGIN ProductExtractionProcess.process(), context: " + OGCFrontController.getContext());
        }

        super.process(in, out, info);

//        try {
//            setResquestId(-1);
//        } catch (MotuExceptionBase e) {
//            LOG.error("ProductExtractionProcess.process()", e);
//            setReturnCode(e);
//            if (LOG.isDebugEnabled()) {
//                LOG.debug("ProductExtractionProcess.process() - exiting");
//            }
//            return;
//        }

        Organizer.Format responseFormat = null;

        // String mode = getMode();
        String mode = MotuWPSProcess.PARAM_MODE_STATUS;

        int priority = getRequestPriority();

        String userId = getLogin();
        boolean anonymousUser = isAnonymousUser(userId);

        if (MotuWPSProcess.isNullOrEmpty(userId)) {
            userId = MotuWPSProcess.PARAM_ANONYMOUS;
        }

        getProductInfoParameters();

        ExtractionParameters extractionParameters = new ExtractionParameters(
                serviceName,
                locationData,
                getVariables(),
                getTemporalCoverage(),
                getGeoCoverage(),
                getDepthCoverage(),
                productId,
                getDataFormat(),
                null,
                responseFormat,
                userId,
                anonymousUser);

        extractionParameters.setBatchQueue(isBatch());

        try {
            productDownload(extractionParameters, mode, priority);
        } catch (Exception e) {
            setRequestIdToUnknown();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("ProductExtractionProcess.process() - exiting");
        }
        return;

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
    
    /**
     * Sets the request id to unknown value (-1).
     */
    protected void setRequestIdToUnknown() {
      try {
            setRequestId(-1);
        } catch (MotuExceptionBase e) {
            LOG.error("ProductExtractionProcess.process()", e);
            setReturnCode(e);
            if (LOG.isDebugEnabled()) {
                LOG.debug("ProductExtractionProcess.process() - exiting");
            }
            return;
        }
        
    }


    /**
     * Product download.
     * 
     * @param priority the priority
     * @param extractionParameters the extraction parameters
     * @param mode the mode
     * @throws Exception 
     * @throws IOException the IO exception
     */
    private void productDownload(ExtractionParameters extractionParameters, String mode, int priority) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("productDownload(ExtractionParameters, String, int) - entering");
        }

        // boolean modeConsole = RunnableHttpExtraction.isModeConsole(mode);
        // boolean modeUrl = RunnableHttpExtraction.isModeUrl(mode);
        boolean modeStatus = MotuWPSProcess.isModeStatus(mode);
        // boolean noMode = RunnableHttpExtraction.noMode(mode);

        RunnableWPSExtraction runnableWPSExtraction = null;

        StatusModeResponse statusModeResponse = null;

        final ReentrantLock lock = new ReentrantLock();
        final Condition requestEndedCondition = lock.newCondition();

        String serviceName = extractionParameters.getServiceName();
        Organizer organizer = getOrganizer();
        try {

            if (organizer.isGenericService() && !MotuWPSProcess.isNullOrEmpty(serviceName)) {
                organizer.setCurrentService(serviceName);
            }
        } catch (MotuExceptionBase e) {
            LOG.error("MotuWPSProcess.productDownload(ExtractionParameters, String, int)", e);

            setReturnCode(e);

            if (LOG.isDebugEnabled()) {
                LOG.debug("productDownload(ExtractionParameters, String, int) - exiting");
            }
            throw e;
        }

        runnableWPSExtraction = new RunnableWPSExtraction(
                priority,
                organizer,
                extractionParameters,
                processletOutputs,
                mode,
                requestEndedCondition,
                lock);

        // runnableHttpExtraction.lock = lock;

        long requestId = requestManagement.generateRequestId();

        runnableWPSExtraction.setRequestId(requestId);

        statusModeResponse = runnableWPSExtraction.getStatusModeResponse();

        statusModeResponse.setRequestId(requestId);

        try {
            setRequestId(requestId);
        } catch (MotuExceptionBase e) {
            LOG.error("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse)", e);
            setReturnCode(e);
            if (LOG.isDebugEnabled()) {
                LOG.debug("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse) - exiting");
            }
            return;
        }

        requestManagement.putIfAbsentRequestStatusMap(requestId, statusModeResponse);

        try {
            // ------------------------------------------------------
            lock.lock();
            // ------------------------------------------------------

            getQueueServerManagement().execute(runnableWPSExtraction);

            if (modeStatus) {
                // $$$$$ response.setContentType(null);
                // $$$$$ Organizer.marshallStatusModeResponse(statusModeResponse, response.getWriter());
            } else {
                // --------- wait for the end of the request -----------
                requestEndedCondition.await();
                // ------------------------------------------------------
            }
//        } catch (MotuMarshallException e) {
//            LOG.error("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse)", e);
//            setReturnCode(e);

        } catch (MotuExceptionBase e) {
            LOG.error("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse)", e);
            runnableWPSExtraction.aborted();
            throw e;

        } catch (Exception e) {
            LOG.error("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse)", e);
            runnableWPSExtraction.aborted();
            throw e;

        } finally {
            // ------------------------------------------------------
            if (lock.isLocked()) {
                lock.unlock();
            }
            // ------------------------------------------------------
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse) - exiting");
        }
    }

    /**
     * Sets the resquest id.
     * 
     * @param requestId the new resquest id
     * @throws MotuException
     */
    public void setRequestId(long requestId) throws MotuException {

        if (isRequestIdSet) {
            return;
        }
        
        ProductExtractionProcess.setRequestId(processletOutputs, Long.toString(requestId));

        isRequestIdSet = true;
    }

    /**
     * Sets the resquest id.
     * 
     * @param requestId the new resquest id
     * @throws MotuException
     */
    public void setRequestId(String requestId) throws MotuException {

        ProductExtractionProcess.setRequestId(processletOutputs, requestId);

    }

    public void setStatus(StatusModeType status) throws MotuException {
        ProductExtractionProcess.setStatus(processletOutputs, status);

    }

    public void setStatus(String status) throws MotuException {
        ProductExtractionProcess.setStatus(processletOutputs, status);
    }

    /**
     * Sets the resquest id.
     * 
     * @param response the response
     * @param requestId the request id
     * @throws MotuException
     */
    public static void setRequestId(ProcessletOutputs response, long requestId) throws MotuException {

        ProductExtractionProcess.setRequestId(response, Long.toString(requestId));

    }

    /**
     * Sets the resquest id.
     * 
     * @param response the response
     * @param requestId the request id
     * @throws MotuException
     */
    public static void setRequestId(ProcessletOutputs response, String requestId) throws MotuException {
        if (response == null) {
            return;
        }

        ComplexOutput requestIdParam = (ComplexOutput) response.getParameter(MotuWPSProcess.PARAM_REQUESTID);

        if ((requestIdParam == null) || (requestId == null)) {
            return;
        }

        try {
            requestIdParam.getBinaryOutputStream().write(requestId.getBytes());
        } catch (IOException e) {
            throw new MotuException("ERROR ProductExtractionProcess#setResquestId", e);
        }

    }

    public static void setStatus(ProcessletOutputs response, StatusModeType status) {
        ProductExtractionProcess.setStatus(response, Integer.toString(status.value()));

    }

    public static void setStatus(ProcessletOutputs response, String status) {

        if (response == null) {
            return;
        }
        LiteralOutput statusParam = (LiteralOutput) response.getParameter(MotuWPSProcess.PARAM_STATUS);

        if ((statusParam == null) || (status == null)) {
            return;
        }

        statusParam.setValue(status);

    }


}
