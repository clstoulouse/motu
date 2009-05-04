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
 * @version $Revision: 1.10 $, $Date: 2009-05-04 16:16:35 $
 */
public class ProductExtractionProcess extends MotuWPSProcess {

    /**
     * Constructeur.
     */
    public ProductExtractionProcess() {
    }

    /** The Constant LOG. */
    private static final Logger LOG = Logger.getLogger(ProductExtractionProcess.class);

    //protected boolean isRequestIdSet = false;

    /** {@inheritDoc} */
    @Override
    public void process(ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info) throws ProcessletException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("BEGIN ProductExtractionProcess.process(), context: " + OGCFrontController.getContext());
        }

        super.process(in, out, info);

        // try {
        // setResquestId(-1);
        // } catch (MotuExceptionBase e) {
        // LOG.error("ProductExtractionProcess.process()", e);
        // setReturnCode(e);
        // if (LOG.isDebugEnabled()) {
        // LOG.debug("ProductExtractionProcess.process() - exiting");
        // }
        // return;
        // }
        
        
        Organizer.Format responseFormat = null;

        // String mode = getMode();
        String mode = MotuWPSProcess.PARAM_MODE_STATUS;

        int priority = getRequestPriority(in);

        String userId = getLogin(in);
        boolean anonymousUser = isAnonymousUser(in, userId);

        if (MotuWPSProcess.isNullOrEmpty(userId)) {
            userId = MotuWPSProcess.PARAM_ANONYMOUS;
        }

        MotuWPSProcessData motuWPSProcessData = getProductInfoParameters(in);

        ExtractionParameters extractionParameters = new ExtractionParameters(
                motuWPSProcessData.getServiceName(),
                motuWPSProcessData.getLocationData(),
                getVariables(in),
                getTemporalCoverage(in),
                getGeoCoverage(in),
                getDepthCoverage(in),
                motuWPSProcessData.getProductId(),
                getDataFormat(in),
                null,
                responseFormat,
                userId,
                anonymousUser);

        extractionParameters.setBatchQueue(isBatch(in));

        try {
            productDownload(in, extractionParameters, mode, priority);
        } catch (Exception e) {
            LOG.error("process(ProcessletInputs, ProcessletOutputs, ProcessletExecutionInfo)", e);

            try {
                ProductExtractionProcess.setRequestId(motuWPSProcessData.getProcessletOutputs(), Long.toString(-1L));
            } catch (MotuException e1) {
                LOG.error("process(ProcessletInputs, ProcessletOutputs, ProcessletExecutionInfo)", e1);
            }
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
     * Product download.
     * 
     * @param priority the priority
     * @param extractionParameters the extraction parameters
     * @param mode the mode
     * @throws Exception
     * @throws IOException the IO exception
     */
    private void productDownload(ProcessletInputs in, ExtractionParameters extractionParameters, String mode, int priority) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("productDownload(ExtractionParameters, String, int) - entering");
        }
        
        MotuWPSProcessData motuWPSProcessData = getMotuWPSProcessData(in);

        // boolean modeConsole = RunnableHttpExtraction.isModeConsole(mode);
        // boolean modeUrl = RunnableHttpExtraction.isModeUrl(mode);
        boolean modeStatus = MotuWPSProcess.isModeStatus(mode);
        // boolean noMode = RunnableHttpExtraction.noMode(mode);

        RunnableWPSExtraction runnableWPSExtraction = null;

        StatusModeResponse statusModeResponse = null;

        final ReentrantLock lock = new ReentrantLock();
        final Condition requestEndedCondition = lock.newCondition();

        String serviceName = extractionParameters.getServiceName();
        Organizer organizer = getOrganizer(in);
        try {

            if (organizer.isGenericService() && !MotuWPSProcess.isNullOrEmpty(serviceName)) {
                organizer.setCurrentService(serviceName);
            }
        } catch (MotuExceptionBase e) {
            LOG.error("MotuWPSProcess.productDownload(ExtractionParameters, String, int)", e);

            setReturnCode(motuWPSProcessData.getProcessletOutputs(), e, false);

            if (LOG.isDebugEnabled()) {
                LOG.debug("productDownload(ExtractionParameters, String, int) - exiting");
            }
            throw e;
        }

        runnableWPSExtraction = new RunnableWPSExtraction(
                priority,
                organizer,
                extractionParameters,
                motuWPSProcessData.getProcessletOutputs(),
                mode,
                requestEndedCondition,
                lock);

        // runnableHttpExtraction.lock = lock;

        //isRequestIdSet = false;
        long requestId = motuWPSProcessData.getRequestId();

        runnableWPSExtraction.setRequestId(requestId);

        statusModeResponse = runnableWPSExtraction.getStatusModeResponse();

        statusModeResponse.setRequestId(requestId);

        try {
            ProductExtractionProcess.setRequestId(motuWPSProcessData.getProcessletOutputs(), Long.toString(requestId));
        } catch (MotuExceptionBase e) {
            LOG.error("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse)", e);
            setReturnCode(motuWPSProcessData.getProcessletOutputs(), e, false);
            if (LOG.isDebugEnabled()) {
                LOG.debug("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse) - exiting");
            }
            return;
        }

        getRequestManagement().putIfAbsentRequestStatusMap(requestId, statusModeResponse);

        try {
            // ------------------------------------------------------
            lock.lock();
            // ------------------------------------------------------

            getQueueServerManagement().execute(runnableWPSExtraction);

            if (modeStatus) {
                // $$$$$ response.setContentType(null);
                // $$$$$ Organizer.marshallStatusModeResponse(statusModeResponse, response.getWriter());               
                ProductExtractionProcess.setStatus(motuWPSProcessData.getProcessletOutputs(), statusModeResponse.getStatus());
                setReturnCode(motuWPSProcessData.getProcessletOutputs(), statusModeResponse);
            } else {
                // --------- wait for the end of the request -----------
                requestEndedCondition.await();
                // ------------------------------------------------------
            }
            // } catch (MotuMarshallException e) {
            // LOG.error("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse)",
            // e);
            // setReturnCode(e);

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

 

    public void setStatus(ProcessletInputs in, StatusModeType status) throws MotuException, ProcessletException {
        MotuWPSProcessData motuWPSProcessData = getMotuWPSProcessData(in);

        ProductExtractionProcess.setStatus(motuWPSProcessData.getProcessletOutputs(), status);

    }

    public void setStatus(ProcessletInputs in, String status) throws MotuException, ProcessletException {
        MotuWPSProcessData motuWPSProcessData = getMotuWPSProcessData(in);
        ProductExtractionProcess.setStatus(motuWPSProcessData.getProcessletOutputs(), status);
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

        synchronized (response) {

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

    }

    public static void setStatus(ProcessletOutputs response, StatusModeType status) {
        ProductExtractionProcess.setStatus(response, Integer.toString(status.value()));

    }

    public static void setStatus(ProcessletOutputs response, String status) {
        synchronized (response) {

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

}
