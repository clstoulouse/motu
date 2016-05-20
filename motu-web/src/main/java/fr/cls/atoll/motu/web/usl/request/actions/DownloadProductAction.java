package fr.cls.atoll.motu.web.usl.request.actions;

import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_BATCH;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_END_DATE;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_HIGH_Z;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_LOW_Z;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_MAX_POOL_ANONYMOUS;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_MAX_POOL_AUTHENTICATE;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_START_DATE;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jasig.cas.client.util.AssertionHolder;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.api.message.xml.StatusModeType;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.request.ExtractionParameters;
import fr.cls.atoll.motu.web.common.format.OutputFormat;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.dal.request.netcdf.ProductDeferedExtractNetcdfThread;
import fr.cls.atoll.motu.web.servlet.MotuServlet;
import fr.cls.atoll.motu.web.servlet.RunnableHttpExtraction;
import fr.cls.atoll.motu.web.usl.request.parameter.CommonHTTPParameters;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.DepthHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.LatitudeHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.LongitudeHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.ModeHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.PriorityHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.ServiceHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.TemporalHTTPParameterValidator;
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

    private ServiceHTTPParameterValidator serviceHTTPParameterValidator;

    private ModeHTTPParameterValidator modeHTTPParameterValidator;

    private LatitudeHTTPParameterValidator latitudeLowHTTPParameterValidator;
    private LatitudeHTTPParameterValidator latitudeHighHTTPParameterValidator;
    private LongitudeHTTPParameterValidator longitudeLowHTTPParameterValidator;
    private LongitudeHTTPParameterValidator longitudeHighHTTPParameterValidator;

    private DepthHTTPParameterValidator depthLowHTTPParameterValidator;
    private DepthHTTPParameterValidator depthHighHTTPParameterValidator;

    private TemporalHTTPParameterValidator startDateTemporalHTTPParameterValidator;
    private TemporalHTTPParameterValidator endDateTemporalHighHTTPParameterValidator;

    private PriorityHTTPParameterValidator priorityHTTPParameterValidator;

    /**
     * 
     * @param actionName_
     */
    public DownloadProductAction(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super(ACTION_NAME, request, response, session);

        serviceHTTPParameterValidator = new ServiceHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_SERVICE,
                CommonHTTPParameters.getServiceFromRequest(getRequest()));

        modeHTTPParameterValidator = new ModeHTTPParameterValidator(MotuRequestParametersConstant.PARAM_MODE, getModeFromRequest());

        latitudeLowHTTPParameterValidator = new LatitudeHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_LOW_LAT,
                CommonHTTPParameters.getLatitudeLowFromRequest(getRequest()),
                "-90");
        latitudeHighHTTPParameterValidator = new LatitudeHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_HIGH_LAT,
                CommonHTTPParameters.getLatitudeLowFromRequest(getRequest()),
                "90");
        longitudeLowHTTPParameterValidator = new LongitudeHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_LOW_LON,
                CommonHTTPParameters.getLongitudeLowFromRequest(getRequest()),
                "-180");
        longitudeHighHTTPParameterValidator = new LongitudeHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_HIGH_LON,
                CommonHTTPParameters.getLongitudeHighFromRequest(getRequest()),
                "180");

        depthLowHTTPParameterValidator = new DepthHTTPParameterValidator(PARAM_LOW_Z, CommonHTTPParameters.getDepthLowFromRequest(getRequest()), "0");
        String depthHighParameterValue = CommonHTTPParameters.getDepthHighFromRequest(getRequest());
        if (StringUtils.isNullOrEmpty(depthHighParameterValue)) {
            depthHighParameterValue = depthLowHTTPParameterValidator.getParameterValue();
        }
        depthHighHTTPParameterValidator = new DepthHTTPParameterValidator(PARAM_HIGH_Z, depthHighParameterValue);

        startDateTemporalHTTPParameterValidator = new TemporalHTTPParameterValidator(
                PARAM_START_DATE,
                CommonHTTPParameters.getStartDateFromRequest(getRequest()));
        endDateTemporalHighHTTPParameterValidator = new TemporalHTTPParameterValidator(
                PARAM_END_DATE,
                CommonHTTPParameters.getEndDateFromRequest(getRequest()));

        priorityHTTPParameterValidator = new PriorityHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_PRIORITY,
                CommonHTTPParameters.getPriorityFromRequest(getRequest()),
                Short.toString(BLLManager.getInstance().getConfigManager().getQueueServerConfigManager().getRequestDefaultPriority()));
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
                serviceHTTPParameterValidator.getParameterValueValidated(),
                CommonHTTPParameters.getDataFromParameter(getRequest()),
                CommonHTTPParameters.getVariablesAsListFromParameter(getRequest()),

                startDateTemporalHTTPParameterValidator.getParameterValue(),
                endDateTemporalHighHTTPParameterValidator.getParameterValue(),

                longitudeLowHTTPParameterValidator.getParameterValueValidated(),
                longitudeHighHTTPParameterValidator.getParameterValueValidated(),
                latitudeLowHTTPParameterValidator.getParameterValueValidated(),
                latitudeHighHTTPParameterValidator.getParameterValueValidated(),

                depthLowHTTPParameterValidator.getParameterValueValidated(),
                depthHighHTTPParameterValidator.getParameterValueValidated(),

                getProductId(),

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

        int priority = priorityHTTPParameterValidator.getParameterValueValidated();
        String mode = modeHTTPParameterValidator.getParameterValueValidated();
        if (mode == null) {

        }

        productDownload(createExtractionParameters(mode), mode, priority);

        boolean noMode = RunnableHttpExtraction.noMode(mode);
        if (!noMode) {
            SessionManager.getInstance().removeOrganizerSession(getSession());
        }
    }

    @Override
    private OutputFormat getOutputFormat() throws IOException {
        OutputFormat dataFormat = null;
        try {
            dataFormat = getDataFormatFromParameter();
        } catch (MotuExceptionBase e) {
            getResponse().sendError(400, String.format("ERROR: %s", e.notifyException()));
        } catch (Exception e) {
            getResponse().sendError(400, String.format("ERROR: %s", e.getMessage()));
        }
        return dataFormat;
    }

    @Override
    private String getProductId() throws IOException {
        String productId = null;
        try {
            productId = getProductIdFromParamId(getRequest().getParameter(MotuRequestParametersConstant.PARAM_PRODUCT));
        } catch (MotuException e) {
            getResponse().sendError(400, String.format("ERROR: '%s' ", e.notifyException()));
        } catch (Exception e) {
            getResponse().sendError(400, String.format("ERROR: '%s' ", e.getMessage()));
        }
        return productId;

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
     * Gets the data format.
     *
     * @param request the request
     * @return the data format
     * @throws MotuException the motu exception
     */
    @Override
    private OutputFormat getDataFormatFromParameter() throws MotuException {
        String dataFormat = getRequest().getParameter(MotuRequestParametersConstant.PARAM_OUTPUT);
        OutputFormat format;
        if (StringUtils.isNullOrEmpty(dataFormat)) {
            return OutputFormat.getDefault();
        }

        try {
            format = OutputFormat.valueOf(dataFormat.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new MotuException(
                    String.format("Parameter '%s': invalid value '%s' - Valid values are : %s",
                                  MotuRequestParametersConstant.PARAM_OUTPUT,
                                  dataFormat,
                                  OutputFormat.valuesToString()),
                    e);
        }

        return format;
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
     * Gets the depth coverage from the request.
     * 
     * @param request servlet request
     * 
     * @return a list of deph coverage : first depth min, then depth max
     */
    private List<String> getDepthCoverage() {
        String lowdepth = Double.toString(depthLowHTTPParameterValidator.getParameterValueValidated());
        String highDepth = Double.toString(depthHighHTTPParameterValidator.getParameterValueValidated());

        List<String> listDepthCoverage = new ArrayList<String>();
        listDepthCoverage.add(lowdepth);
        listDepthCoverage.add(highDepth);
        return listDepthCoverage;
    }

    /**
     * Gets the geographical coverage from the request.
     * 
     * @param request servlet request
     * 
     * @return a list of geographical coverage : Lat min, Lon min, Lat max, Lon max
     */
    private List<String> getGeoCoverage() {
        List<String> listLatLonCoverage = new ArrayList<String>();
        listLatLonCoverage.add(Double.toString(latitudeLowHTTPParameterValidator.getParameterValueValidated()));
        listLatLonCoverage.add(Double.toString(longitudeLowHTTPParameterValidator.getParameterValueValidated()));
        listLatLonCoverage.add(Double.toString(latitudeHighHTTPParameterValidator.getParameterValueValidated()));
        listLatLonCoverage.add(Double.toString(longitudeHighHTTPParameterValidator.getParameterValueValidated()));
        return listLatLonCoverage;
    }

    private String getBatchParameter() {
        return getRequest().getParameter(PARAM_BATCH);
    }

    /**
     * Checks if is batch.
     * 
     * @param request the request
     * 
     * @return true, if is batch
     */
    @Override
    private boolean isBatch() {
        String batchAsString = getBatchParameter();
        return batchAsString != null && (batchAsString.trim().equalsIgnoreCase("true") || batchAsString.trim().equalsIgnoreCase("1"));
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

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        modeHTTPParameterValidator.validate();
        serviceHTTPParameterValidator.validate();

        latitudeLowHTTPParameterValidator.validate();
        latitudeHighHTTPParameterValidator.validate();
        longitudeLowHTTPParameterValidator.validate();
        longitudeHighHTTPParameterValidator.validate();

        depthLowHTTPParameterValidator.validate();
        depthHighHTTPParameterValidator.validate();

        startDateTemporalHTTPParameterValidator.validate();
        endDateTemporalHighHTTPParameterValidator.validate();

        priorityHTTPParameterValidator.validate();
    }

}
