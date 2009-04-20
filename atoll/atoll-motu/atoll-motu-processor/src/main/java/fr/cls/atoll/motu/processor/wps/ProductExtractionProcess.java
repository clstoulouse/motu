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

/**
 * The purpose of this {@link Processlet} is to provide the time coverage of a product.
 * 
 * @author last edited by: $Author: dearith $
 * @version $Revision: 1.5 $, $Date: 2009-04-20 14:08:20 $
 */
public class ProductExtractionProcess extends MotuWPSProcess {

    /**
     * Constructeur.
     */
    public ProductExtractionProcess() {
    }

    /** The Constant LOG. */
    private static final Logger LOG = Logger.getLogger(ProductExtractionProcess.class);

    /** {@inheritDoc} */
    @Override
    public void process(ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info) throws ProcessletException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("BEGIN ProductExtractionProcess.process(), context: " + OGCFrontController.getContext());
        }

        super.process(in, out, info);

        try {
            setResquestId(-1);
        } catch (MotuExceptionBase e) {
            LOG.error("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse)", e);
            setReturnCode(e);
            if (LOG.isDebugEnabled()) {
                LOG.debug("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse) - exiting");
            }
            return;
        }
        
        Organizer.Format responseFormat = null;

        // String mode = getMode();
        String mode = MotuWPSProcess.PARAM_MODE_STATUS;

        int priority = getRequestPriority();

        String userId = getLogin();
        boolean anonymousUser = isAnonymousUser(userId);

        if (MotuWPSProcess.isNullOrEmpty(userId)) {
            userId = MotuWPSProcess.PARAM_ANONYMOUS;
        }

        serviceNameParam = null;
        locationDataParam = null;
        productIdParam = null;

        getProductInfoParameters();

        ExtractionParameters extractionParameters = new ExtractionParameters(
                serviceNameParam.getValue(),
                locationDataParam.getValue(),
                getVariables(),
                getTemporalCoverage(),
                getGeoCoverage(),
                getDepthCoverage(),
                productIdParam.getValue(),
                getDataFormat(),
                null,
                responseFormat,
                userId,
                anonymousUser);

        extractionParameters.setBatchQueue(isBatch());

        productDownload(extractionParameters, mode, priority);

        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionProductDownload(String, HttpServletRequest, HttpSession, HttpServletResponse) - exiting");
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
     * Product download.
     * 
     * @param priority the priority
     * @param extractionParameters the extraction parameters
     * @param mode the mode
     * 
     * @throws IOException the IO exception
     */
    private void productDownload(ExtractionParameters extractionParameters, String mode, int priority) {
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
            return;
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
            setResquestId(requestId);
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
        } catch (MotuMarshallException e) {
            LOG.error("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse)", e);
            setReturnCode(e);

        } catch (MotuExceptionBase e) {
            LOG.error("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse)", e);
            runnableWPSExtraction.aborted();

        } catch (Exception e) {
            LOG.error("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse)", e);
            runnableWPSExtraction.aborted();

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
    public void setResquestId(long requestId) throws MotuException {

        ProductExtractionProcess.setResquestId(processletOutputs, Long.toString(requestId));

    }

    /**
     * Sets the resquest id.
     * 
     * @param requestId the new resquest id
     * @throws MotuException 
     */
    public void setResquestId(String requestId) throws MotuException {

        ProductExtractionProcess.setResquestId(processletOutputs, requestId);

    }

    /**
     * Sets the resquest id.
     * 
     * @param response the response
     * @param requestId the request id
     * @throws MotuException 
     */
    public static void setResquestId(ProcessletOutputs response, long requestId) throws MotuException {

        ProductExtractionProcess.setResquestId(response, Long.toString(requestId));

    }

    /**
     * Sets the resquest id.
     * 
     * @param response the response
     * @param requestId the request id
     * @throws MotuException 
     */
    public static void setResquestId(ProcessletOutputs response, String requestId) throws MotuException {

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

}
