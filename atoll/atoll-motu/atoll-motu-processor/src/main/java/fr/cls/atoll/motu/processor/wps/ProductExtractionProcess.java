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
import org.deegree.services.wps.output.LiteralOutput;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.exception.MotuMarshallException;
import fr.cls.atoll.motu.library.intfce.ExtractionParameters;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.atoll.motu.msg.xml.StatusModeResponse;
import fr.cls.atoll.motu.msg.xml.TimeCoverage;

/**
 * The purpose of this {@link Processlet} is to provide the time coverage of a product
 * 
 * @author last edited by: $Author: dearith $
 * 
 * @version $Revision: 1.4 $, $Date: 2009-04-02 15:03:44 $
 */
public class ProductExtractionProcess extends MotuWPSProcess {

    /**
     * Constructeur.
     */
    public ProductExtractionProcess() {
    }

    private static final Logger LOG = Logger.getLogger(ProductExtractionProcess.class);

    /** {@inheritDoc} */
    @Override
    public void process(ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info) throws ProcessletException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("BEGIN ProductExtractionProcess.process(), context: " + OGCFrontController.getContext());
        }

        Organizer.Format responseFormat = null;

        String mode = getMode();

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

        Object s;
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

    /**
     * Product get time coverage.
     * 
     * @param response the response
     * @param locationData the location data
     * 
     */
    private void productGetTimeCoverage(String locationData) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("productGetTimeCoverage(String, ProcessletOutputs) - entering");
        }

        Organizer organizer = getOrganizer();
        if (organizer == null) {
            return;
        }

        TimeCoverage timeCoverage = null;
        try {
            timeCoverage = organizer.getTimeCoverage(locationData);
        } catch (MotuException e) {
            LOG.error("productGetTimeCoverage(String, ProcessletOutputs)", e);
            // Do nothing error is in response code
        }

        productGetTimeCoverage(timeCoverage);

        if (LOG.isDebugEnabled()) {
            LOG.debug("productGetTimeCoverage(String, ProcessletOutputs) - exiting");
        }
    }

    /**
     * Product get time coverage.
     * 
     * @param response the response
     * @param serviceName the service name
     * @param productId the product id
     * @throws MotuExceptionBase
     * 
     */
    private void productGetTimeCoverage(String serviceName, String productId) throws MotuExceptionBase {
        if (LOG.isDebugEnabled()) {
            LOG.debug("productGetTimeCoverage(String, String, ProcessletOutputs) - entering");
        }

        Organizer organizer = getOrganizer();
        if (organizer == null) {
            return;
        }

        TimeCoverage timeCoverage = null;
        try {
            timeCoverage = organizer.getTimeCoverage(serviceName, productId);
        } catch (MotuExceptionBase e) {

            LOG.error("productGetTimeCoverage(String, String, ProcessletOutputs", e);
            timeCoverage = Organizer.createTimeCoverage(e);
            throw e;
        }

        productGetTimeCoverage(timeCoverage);

        if (LOG.isDebugEnabled()) {
            LOG.debug("productGetTimeCoverage(String, String, ProcessletOutputs) - exiting");
        }
    }

    /**
     * Product get time coverage.
     * 
     * @param timeCoverage the time coverage
     * @param out the out
     */
    private void productGetTimeCoverage(TimeCoverage timeCoverage) {

        if (timeCoverage == null) {
            return;
        }

        LiteralOutput startParam = (LiteralOutput) processletOutputs.getParameter(MotuWPSProcess.PARAM_STARTTIME);
        LiteralOutput endParam = (LiteralOutput) processletOutputs.getParameter(MotuWPSProcess.PARAM_ENDTIME);
        LiteralOutput codeParam = (LiteralOutput) processletOutputs.getParameter(MotuWPSProcess.PARAM_CODE);
        LiteralOutput msgParam = (LiteralOutput) processletOutputs.getParameter(MotuWPSProcess.PARAM_MESSAGE);

        if (startParam != null) {
            startParam.setValue(timeCoverage.getStart().normalize().toXMLFormat());
        }
        if (endParam != null) {
            endParam.setValue(timeCoverage.getEnd().normalize().toXMLFormat());
        }

        if (codeParam != null) {
            codeParam.setValue(timeCoverage.getCode().toString());
        }
        if (msgParam != null) {
            msgParam.setValue(timeCoverage.getMsg());
        }

    }

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
     * @param response the response
     * @param session the session
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

        requestManagement.putIfAbsentRequestStatusMap(requestId, statusModeResponse);

        try {
            // ------------------------------------------------------
            lock.lock();
            // ------------------------------------------------------

            getQueueServerManagement().execute(runnableWPSExtraction);

            if (modeStatus) {
              //$$$$$ response.setContentType(null);
              //$$$$$ Organizer.marshallStatusModeResponse(statusModeResponse, response.getWriter());
            } else {
                // --------- wait for the end of the request -----------
                requestEndedCondition.await();
                // ------------------------------------------------------
            }
        } catch (MotuMarshallException e) {
            LOG.error("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse)", e);

          //$$$$$ response.sendError(500, String.format("ERROR: %s", e.getMessage()));
        } catch (MotuExceptionBase e) {
            LOG.error("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse)", e);

            runnableWPSExtraction.aborted();
            // Do nothing error is in response error code
            // response.sendError(400, String.format("ERROR: %s", e.notifyException()));
        } catch (Exception e) {
            LOG.error("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse)", e);

            runnableWPSExtraction.aborted();
            // response.sendError(500, String.format("ERROR: %s", e.getMessage()));
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

}
