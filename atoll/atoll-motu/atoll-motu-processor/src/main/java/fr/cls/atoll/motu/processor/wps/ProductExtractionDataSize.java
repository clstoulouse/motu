package fr.cls.atoll.motu.processor.wps;

import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.output.ComplexOutput;
import org.deegree.services.wps.output.LiteralOutput;

import fr.cls.atoll.motu.library.data.Product;
import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.exception.MotuMarshallException;
import fr.cls.atoll.motu.library.intfce.ExtractionParameters;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.atoll.motu.msg.xml.RequestSize;
import fr.cls.atoll.motu.msg.xml.StatusModeResponse;
import fr.cls.atoll.motu.msg.xml.StatusModeType;

/**
 * The purpose of this {@link Processlet} is to provide the time coverage of a product.
 * 
 * @author last edited by: $Author: dearith $
 * @version $Revision: 1.1 $, $Date: 2009-06-03 11:44:23 $
 */
public class ProductExtractionDataSize extends MotuWPSProcess {

    /**
     * Constructeur.
     */
    public ProductExtractionDataSize() {
    }

    /** The Constant LOG. */
    private static final Logger LOG = Logger.getLogger(ProductExtractionDataSize.class);

    // protected boolean isRequestIdSet = false;

    /** {@inheritDoc} */
    @Override
    public void process(ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info) throws ProcessletException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("BEGIN ProductExtractionDataSize.process(), context: " + OGCFrontController.getContext());
        }

        super.process(in, out, info);

        MotuWPSProcessData motuWPSProcessData = getProductInfoParameters(in);

        ExtractionParameters extractionParameters = new ExtractionParameters(motuWPSProcessData.getServiceName(), motuWPSProcessData
                .getLocationData(), getVariables(in), getTemporalCoverage(in), getGeoCoverage(in), getDepthCoverage(in), motuWPSProcessData
                .getProductId(), getDataFormat(in), null, null, null, true);

        extractionParameters.setBatchQueue(isBatch(in));

        try {
            getAmountDataSize(in, extractionParameters);
        } catch (MotuExceptionBase e) {
            setReturnCode(out, e, false);
        }

 

    }

    /** {@inheritDoc} */
    @Override
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("ProductExtraction#destroy() called");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void init() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("ProductExtraction#init() called");
        }
        super.init();
    }

    private void getAmountDataSize(ProcessletInputs in, ExtractionParameters extractionParameters) throws MotuExceptionBase, ProcessletException {
        MotuWPSProcessData motuWPSProcessData = getMotuWPSProcessData(in);

        Product product = null;
        RequestSize requestSize = null;
        try {
            Organizer organizer = getOrganizer(in);
            product = organizer.getAmountDataSize(extractionParameters);

            requestSize = Organizer.initRequestSize(product, extractionParameters.isBatchQueue());

            ProductExtractionDataSize.setRequestSize(motuWPSProcessData.getProcessletOutputs(), requestSize);
            ProductExtractionDataSize.setReturnCode(motuWPSProcessData.getProcessletOutputs(), requestSize, false);

        } catch (MotuExceptionBase e) {
            ProductExtractionDataSize.setRequestSize(motuWPSProcessData.getProcessletOutputs(), requestSize);
            MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(), e, false);
            throw e;
        }
    }

    public static void setRequestSize(ProcessletOutputs response, RequestSize requestSize) {

        if (requestSize != null) {
            ProductExtractionDataSize.setRequestSize(response, requestSize.getSize(), requestSize.getMaxAllowedSize());
        } else {
            ProductExtractionDataSize.setRequestSize(response, -1d, -1d);
        }
    }

    public static void setRequestSize(ProcessletOutputs response, double size, double maxAllowedSize) {
        synchronized (response) {

            if (response == null) {
                return;
            }

            LiteralOutput maxAllowedSizeParam = (LiteralOutput) response.getParameter(MotuWPSProcess.PARAM_MAX_ALLOWED_SIZE);

            if ((maxAllowedSizeParam != null)) {
                maxAllowedSizeParam.setValue(Double.toString(maxAllowedSize));
            }

            LiteralOutput sizeParam = (LiteralOutput) response.getParameter(MotuWPSProcess.PARAM_SIZE);

            if ((sizeParam != null)) {
                sizeParam.setValue(Double.toString(size));
            }
        }

    }

    public static void setReturnCode(ProcessletOutputs response, RequestSize requestSize, boolean throwProcessletException)
            throws ProcessletException {

        MotuWPSProcess.setReturnCode(response, requestSize.getCode(), requestSize.getMsg(), throwProcessletException);

    }

    /**
     * Product download.
     * 
     * @param priority the priority
     * @param extractionParameters the extraction parameters
     * @param mode the mode
     * @throws MotuExceptionBase, Exception
     */
    private void productDownload(ProcessletInputs in, ExtractionParameters extractionParameters, String mode, int priority) throws MotuExceptionBase,
            Exception {
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

        // isRequestIdSet = false;
        long requestId = motuWPSProcessData.getRequestId();

        runnableWPSExtraction.setRequestId(requestId);

        statusModeResponse = runnableWPSExtraction.getStatusModeResponse();

        statusModeResponse.setRequestId(requestId);
        motuWPSProcessData.setRequestId(requestId);

        try {
            MotuWPSProcess.setRequestId(motuWPSProcessData.getProcessletOutputs(), Long.toString(requestId));
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
                MotuWPSProcess.setStatus(motuWPSProcessData.getProcessletOutputs(), statusModeResponse.getStatus());
                MotuWPSProcess.setReturnCode(motuWPSProcessData.getProcessletOutputs(), statusModeResponse, false);
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

}
