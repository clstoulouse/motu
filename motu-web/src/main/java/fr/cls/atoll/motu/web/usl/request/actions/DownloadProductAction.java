package fr.cls.atoll.motu.web.usl.request.actions;

import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_MAX_POOL_ANONYMOUS;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_MAX_POOL_AUTHENTICATE;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jasig.cas.client.util.AssertionHolder;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.api.message.xml.StatusModeResponse;
import fr.cls.atoll.motu.api.message.xml.StatusModeType;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.misc.exception.MotuMarshallException;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.request.ExtractionParameters;
import fr.cls.atoll.motu.web.common.format.OutputFormat;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.dal.request.netcdf.ProductDeferedExtractNetcdfThread;
import fr.cls.atoll.motu.web.servlet.MotuServlet;
import fr.cls.atoll.motu.web.servlet.RunnableHttpExtraction;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.ModeHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.session.SessionManager;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites) <br>
 * <br>
 * This interface is used to download data with subsetting.<br>
 * Operation invocation consists in performing an HTTP GET request.<br>
 * Input parameters are the following: [x,y] is the cardinality<br>
 * <ul>
 * <li><b>action</b>: [1]: {@link #ACTION_NAME}</li>
 * <li><b>service</b>: [1]: identifier of the service that provides the desired data set to order.</li>
 * <li><b>product</b>: [1]: identifier of the desired data set to order.</li>
 * <li><b>variable</b>: [0,n]: physical variables to be extracted from the product. no variable is set, all
 * the variables of the dataset are extracted.</li>
 * <li><b>y_lo</b>: [0,1]: low latitude of a geographic extraction. Default value is -90.</li>
 * <li><b>y_hi</b>: [0,1]: high latitude of a geographic extraction. Default value is 90.</li>
 * <li><b>x_lo</b>: [0,1]: low longitude of a geographic extraction. Default value is -180.</li>
 * <li><b>x_hi</b>: [0,1]: high longitude of a geographic extraction. Default value is 180.</li>
 * <li><b>z_lo</b>: [0,1]: low vertical depth . Default value is 0.</li>
 * <li><b>z_hi</b>: [0,1]: high vertical depth. Default value is 180.</li>
 * <li><b>t_lo</b>: [0,1]: Start date of a temporal extraction. If not set, the default value is the first
 * date/time available for the dataset. Format is yyy-mm-dd or yyyy-dd h:m:s or yyyy-ddTh:m:s.</li>
 * <li><b>t_hi</b>: [0,1]: End date of a temporal extraction. If not set, the default value is the last
 * date/time available for the dataset. Format is yyy-mm-dd or yyyy-dd h:m:s or yyyy-ddTh:m:s.</li>
 * <li><b>mode</b>: [0,1]: Specify the desired result mode. Enumeration value from [url, console, status]
 * represented as a string. If no mode, "url" value is the default mode.<br>
 * <ul>
 * <li><b>url</b>: the delivery file is directly returned in the HTTP response as a binary stream. The request
 * is processed in a synchronous mode.</li>
 * <li><b>console</b>: the delivery file is directly returned in the HTTP response as a binary stream. The
 * request is processed in a synchronous mode.</li>
 * <li><b>status</b>: then request is submitted and the status of the request processing is immediately
 * returned. The request is processed in an asynchronous mode.<br>
 * Web Portal submits the request to the Dissemination Unit Subsetter and gets an immediate response of the
 * Subsetter. This response contains the identifier and the status of the order (pending, in progress, done,
 * error).<br>
 * So long as the order is not completed (done or error), MyOcean Web Portal requests the status of the order
 * at regular and fair intervals (> 5 seconds) and gets an immediate response. When the status is “done”,
 * MyOcean Web Portal retrieves the url of the file to download, from the status response. Then MyOcean Web
 * Portal redirects response to this url. The Web Browser opens a binary stream of the file to download and
 * shows a dialog box to allow the user saving it as a local file.</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class DownloadProductAction extends AbstractAuthorizedAction {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String ACTION_NAME = "productdownload";

    private ModeHTTPParameterValidator modeHTTPParameterValidator;

    /**
     * 
     * @param actionName_
     */
    public DownloadProductAction(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super(ACTION_NAME, request, response, session);

        modeHTTPParameterValidator = new ModeHTTPParameterValidator(MotuRequestParametersConstant.PARAM_MODE, getModeFromRequest());
    }

    @Override
    public void process() throws IOException {
        downloadProduct();
    }

    private ExtractionParameters createExtractionParameters(String mode) throws IOException {
        Writer out = null;
        OutputFormat responseFormat = null;

        if (mode == null || mode.trim().length() <= 0) {
            out = getResponse().getWriter();
            responseFormat = OutputFormat.HTML;
        }

        ExtractionParameters extractionParameters = new ExtractionParameters(
                getServiceHTTPParameterValidator().getParameterValueValidated(),
                getDataFromParameter(),
                getVariables(),
                getTemporalCoverage(),
                getGeoCoverage(),
                getDepthCoverage(),
                getProductId(),
                getOutputFormat(),
                out,
                responseFormat,
                getLoginOrUserHostname(),
                isAnAnonymousUser());
        extractionParameters.setBatchQueue(isBatch());
        extractionParameters.setUserHost(getLoginOrUserHostname());

        // Set assertion to manage CAS.
        extractionParameters.setAssertion(AssertionHolder.getAssertion());
        return extractionParameters;
    }

    private void downloadProduct() throws IOException, ServletException, MotuException {
        // Read parameter from request
        // TODO SMA those 3 var were set in the Organizer
        String requestLanguage = getLanguageFromRequest();
        short maxPoolAnonymous = getMaxPoolAnonymous();
        short maxPoolAuthenticate = getMaxPoolAuthenticate();

        int priority = getRequestPriorityFromRequest();
        String mode = modeHTTPParameterValidator.getParameterValueValidated();
        if (mode == null) {

        }

        productDownload(createExtractionParameters(mode), mode, priority);

        boolean noMode = RunnableHttpExtraction.noMode(mode);
        if (!noMode) {
            SessionManager.getInstance().removeOrganizerSession(getSession());
        }
    }

    /**
     * Override max pool anonymous.
     * 
     * @param request the request
     */
    private short getMaxPoolAnonymous() {
        String maxPoolAnonymousAsString = getRequest().getParameter(PARAM_MAX_POOL_ANONYMOUS);
        short maxPoolAnonymousOverrided = -1;
        try {
            maxPoolAnonymousOverrided = Short.valueOf(maxPoolAnonymousAsString);
            // TODO SMA remove comment
            // getQueueServerManagement().setMaxPoolAnonymousOverrided(maxPoolAnonymousOverrided);
        } catch (NumberFormatException e) {
            // Do nothing
        }
        return maxPoolAnonymousOverrided;
    }

    /**
     * Override max pool authenticate.
     * 
     * @param request the request
     */
    private short getMaxPoolAuthenticate() {
        String maxPoolAuthOverridedAsString = getRequest().getParameter(PARAM_MAX_POOL_AUTHENTICATE);
        short maxPoolAuthOverrided = -1;
        try {
            maxPoolAuthOverrided = Short.valueOf(maxPoolAuthOverridedAsString);
            // TODO SMA remove comment
            // getQueueServerManagement().setMaxPoolAnonymousOverrided(maxPoolAuthOverrided);
        } catch (NumberFormatException e) {
            // Do nothing
        }
        return maxPoolAuthOverrided;
    }

    /**
     * Gets the response format.
     * 
     * @param request the request
     * @return the response format
     * @throws MotuException the motu exception
     */
    private OutputFormat getResponseFormat(HttpServletRequest request) throws MotuException {
        String dataFormat = getRequest().getParameter(MotuRequestParametersConstant.PARAM_RESPONSE_FORMAT);
        OutputFormat format = OutputFormat.HTML;

        if (StringUtils.isNullOrEmpty(dataFormat)) {
            return format;
        }

        try {
            format = OutputFormat.valueOf(dataFormat.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new MotuException(
                    String.format("Parameter '%s': invalid value '%s' - Valid values are : %s",
                                  MotuRequestParametersConstant.PARAM_RESPONSE_FORMAT,
                                  dataFormat,
                                  OutputFormat.valuesToString()),
                    e);
        }

        return format;
    }

    /**
     * Gets the product id.
     *
     * @param productId the product id
     * @param request the request
     * @param response the response
     * @return the product id
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ServletException the servlet exception
     * @throws MotuException the motu exception
     */
    @Override
    protected String getProductIdFromParamId(String productId) throws IOException, ServletException, MotuException {
        String serviceName = serviceHTTPParameterValidator.getParameterValueValidated();

        if ((StringUtils.isNullOrEmpty(serviceName)) || (StringUtils.isNullOrEmpty(productId))) {
            return productId;
        }

        Organizer organizer = getOrganizer();

        return organizer.getDatasetIdFromURI(productId, serviceName);
    }

    /**
     * Product download.
     *
     * @param extractionParameters the extraction parameters
     * @param mode the mode
     * @param priority the priority
     * @param session the session
     * @param response the response
     * @throws IOException the IO exception
     */
    private void productDownload(ExtractionParameters extractionParameters, String mode, int priority) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse) - entering");
        }

        boolean modeStatus = RunnableHttpExtraction.isModeStatus(mode);

        RunnableHttpExtraction runnableHttpExtraction = null;
        StatusModeResponse statusModeResponse = null;

        final ReentrantLock lock = new ReentrantLock();
        final Condition requestEndedCondition = lock.newCondition();

        String serviceName = extractionParameters.getServiceName();
        Organizer organizer = getOrganizer(getSession(), getResponse());
        try {

            if (organizer.isGenericService() && !StringUtils.isNullOrEmpty(serviceName)) {
                organizer.setCurrentService(serviceName);
            }
        } catch (MotuException e) {
            LOGGER.error("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse)", e);

            getResponse().sendError(400, String.format("ERROR: %s", e.notifyException()));

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse) - exiting");
            }
            return;
        }

        runnableHttpExtraction = new RunnableHttpExtraction(
                priority,
                organizer,
                extractionParameters,
                getResponse(),
                mode,
                requestEndedCondition,
                lock);

        long requestId = requestManagement.generateRequestId();
        runnableHttpExtraction.setRequestId(requestId);
        statusModeResponse = runnableHttpExtraction.getStatusModeResponse();
        statusModeResponse.setRequestId(requestId);
        requestManagement.putIfAbsentRequestStatusMap(requestId, statusModeResponse);

        try {
            // ------------------------------------------------------
            lock.lock();
            // ------------------------------------------------------

            getQueueServerManagement().execute(runnableHttpExtraction);

            if (modeStatus) {
                getResponse().setContentType(null);
                Organizer.marshallStatusModeResponse(statusModeResponse, response.getWriter());
            } else {
                // --------- wait for the end of the request -----------
                requestEndedCondition.await();
                // ------------------------------------------------------
            }
        } catch (MotuMarshallException e) {
            LOGGER.error("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse)", e);

            getResponse().sendError(500, String.format("ERROR: %s", e.getMessage()));
        } catch (MotuExceptionBase e) {
            LOGGER.error("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse)", e);

            runnableHttpExtraction.aborted();
            // Do nothing error is in response error code
            // response.sendError(400, String.format("ERROR: %s", e.notifyException()));
        } catch (Exception e) {
            LOGGER.error("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse)", e);

            runnableHttpExtraction.aborted();
            // response.sendError(500, String.format("ERROR: %s", e.getMessage()));
        } finally {
            // ------------------------------------------------------
            if (lock.isLocked()) {
                lock.unlock();
            }
            // ------------------------------------------------------
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse) - exiting");
        }
    }

    /**
     * Product defered extract netcdf with status as file.
     * 
     * @param organizer the organizer
     * @param extractionParameters the extraction parameters
     * @param mode the mode
     * 
     * @return the string
     * 
     * @throws MotuException the motu exception
     */
    private String productDeferedExtractNetcdfWithStatusAsFile(Organizer organizer, ExtractionParameters extractionParameters, String mode)
            throws MotuException {

        String productDeferedExtractNetcdfStatusFileName = getStatusFileName();

        String productDeferedExtractNetcdfStatusFilePathName = Organizer.getMotuConfigInstance().getExtractionPath() + "/"
                + productDeferedExtractNetcdfStatusFileName;
        String productDeferedExtractNetcdfStatusUrl = Organizer.getMotuConfigInstance().getDownloadHttpUrl() + "/"
                + productDeferedExtractNetcdfStatusFileName;
        ProductDeferedExtractNetcdfThread productDeferedExtractNetcdfThread = null;

        productDeferedExtractNetcdfThread = new ProductDeferedExtractNetcdfThread(
                productDeferedExtractNetcdfStatusFilePathName,
                organizer,
                extractionParameters);
        try {
            MotuServlet.printProductDeferedExtractNetcdfStatus(null,
                                                               productDeferedExtractNetcdfThread.createWriter(),
                                                               StatusModeType.INPROGRESS,
                                                               MSG_IN_PROGRESS,
                                                               ErrorType.OK);
        } catch (IOException e) {
            throw new MotuException(e);
        }
        productDeferedExtractNetcdfThread.start();

        return productDeferedExtractNetcdfStatusUrl;

    }

    /**
     * Gets the status file name.
     * 
     * @return the status file name
     */
    private String getStatusFileName() {
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append("pr_defered_");
        stringBuffer.append(BLLManager.getInstance().getRequestManager().getNewRequestId());
        stringBuffer.append("status.xml");

        return stringBuffer.toString();

    }

    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        modeHTTPParameterValidator.validate();
        getServiceHTTPParameterValidator().validate();

        getLatitudeLowHTTPParameterValidator().validate();
        getLatitudeHighHTTPParameterValidator().validate();
        getLongitudeLowHTTPParameterValidator().validate();
        getLongitudeHighHTTPParameterValidator().validate();

        getDepthLowHTTPParameterValidator().validate();
        getDepthHighHTTPParameterValidator().validate();

        getStartDateTemporalHTTPParameterValidator().validate();
        getEndDateTemporalHighHTTPParameterValidator().validate();
    }
}
