package fr.cls.atoll.motu.web.usl.request.actions;

import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_END_DATE;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_HIGH_Z;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_LOW_Z;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_START_DATE;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jasig.cas.client.util.AssertionHolder;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.api.message.xml.ObjectFactory;
import fr.cls.atoll.motu.api.message.xml.StatusModeResponse;
import fr.cls.atoll.motu.api.message.xml.StatusModeType;
import fr.cls.atoll.motu.api.utils.JAXBWriter;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.messageserror.BLLMessagesErrorManager;
import fr.cls.atoll.motu.web.bll.request.model.ExtractionParameters;
import fr.cls.atoll.motu.web.bll.request.model.ProductResult;
import fr.cls.atoll.motu.web.common.format.OutputFormat;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.config.xml.model.MotuConfig;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ProductMetaData;
import fr.cls.atoll.motu.web.usl.request.parameter.CommonHTTPParameters;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.DepthHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.LatitudeHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.LongitudeHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.ModeHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.OutputFormatParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.PriorityHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.ProductHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.ScriptVersionParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.ServiceHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.TemporalHTTPParameterValidator;

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
    private ProductHTTPParameterValidator productHTTPParameterValidator;

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

    private OutputFormatParameterValidator outputFormatParameterValidator;

    private ScriptVersionParameterValidator scriptVersionParameterValidator;

    /**
     * 
     * @param actionName_
     */
    public DownloadProductAction(String actionCode_, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super(ACTION_NAME, actionCode_, request, response, session);

        serviceHTTPParameterValidator = new ServiceHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_SERVICE,
                CommonHTTPParameters.getServiceFromRequest(getRequest()));
        productHTTPParameterValidator = new ProductHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_PRODUCT,
                CommonHTTPParameters.getProductFromRequest(getRequest()));
        modeHTTPParameterValidator = new ModeHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_MODE,
                getModeFromRequest(),
                MotuRequestParametersConstant.PARAM_MODE_URL);

        latitudeLowHTTPParameterValidator = new LatitudeHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_LOW_LAT,
                CommonHTTPParameters.getLatitudeLowFromRequest(getRequest()),
                "-90");
        latitudeHighHTTPParameterValidator = new LatitudeHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_HIGH_LAT,
                CommonHTTPParameters.getLatitudeHighFromRequest(getRequest()),
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

        outputFormatParameterValidator = new OutputFormatParameterValidator(
                MotuRequestParametersConstant.PARAM_OUTPUT,
                CommonHTTPParameters.getOutputFormatFromRequest(getRequest()),
                OutputFormat.NETCDF.name().toUpperCase());

        scriptVersionParameterValidator = new ScriptVersionParameterValidator(
                MotuRequestParametersConstant.PARAM_SCRIPT_VERSION,
                CommonHTTPParameters.getScriptVersionFromRequest(getRequest()),
                "");
    }

    @Override
    public void process() throws MotuException {
        // Read parameter from request
        // TODO SMA those 3 var were set in the Organizer
        String requestLanguage = CommonHTTPParameters.getLanguageFromRequest(getRequest());
        int priority = priorityHTTPParameterValidator.getParameterValueValidated();

        MotuConfig mc = BLLManager.getInstance().getConfigManager().getMotuConfig();
        ConfigService cs = BLLManager.getInstance().getConfigManager().getConfigService(serviceHTTPParameterValidator.getParameterValueValidated());
        CatalogData cd = BLLManager.getInstance().getCatalogManager().getCatalogData(cs);
        String productId = productHTTPParameterValidator.getParameterValueValidated();
        Product p = cd.getProducts().get(productId);
        ProductMetaData pmd = BLLManager.getInstance().getCatalogManager().getProductManager()
                .getProductMetaData(BLLManager.getInstance().getCatalogManager().getCatalogType(p), productId, p.getLocationData());

        if (pmd != null) {
            p.setProductMetaData(pmd);
        }

        String mode = modeHTTPParameterValidator.getParameterValueValidated();

        if (mode.equalsIgnoreCase(MotuRequestParametersConstant.PARAM_MODE_STATUS)) {
            // Asynchronous mode
            long requestId = BLLManager.getInstance().getRequestManager().downloadAsynchonously(cs, p, createExtractionParameters());
            getResponse().setContentType(CONTENT_TYPE_XML);
            try {
                JAXBWriter.getInstance().write(createStatusModeResponse(requestId), getResponse().getWriter());
            } catch (Exception e) {
                throw new MotuException(ErrorType.SYSTEM, "JAXB error while writing createStatusModeResponse: ", e);
            }
        } else {
            ProductResult pr = BLLManager.getInstance().getRequestManager().download(cs, p, createExtractionParameters());
            if (pr.getRunningException() != null) {
                try {
                    MotuException runningException = pr.getRunningException();
                    ErrorType errorType = runningException.getErrorType();
                    p.setLastError(StringUtils.getErrorCode(getActionCode(), errorType) + "=>"
                            + BLLManager.getInstance().getMessagesErrorManager().getMessageError(errorType));
                    LOGGER.error(StringUtils.getLogMessage(getActionCode(), errorType, runningException.getMessage()), runningException);
                } catch (MotuException errorMessageException) {
                    p.setLastError(StringUtils.getErrorCode(getActionCode(), BLLMessagesErrorManager.SYSTEM_ERROR_CODE) + "=>" + StringUtils
                            .getLogMessage(getActionCode(), BLLMessagesErrorManager.SYSTEM_ERROR_CODE, BLLMessagesErrorManager.SYSTEM_ERROR_MESSAGE));
                    LOGGER.error(StringUtils.getLogMessage(getActionCode(),
                                                           BLLMessagesErrorManager.SYSTEM_ERROR_CODE,
                                                           errorMessageException.getMessage()),
                                 errorMessageException);
                }
                onError(mc, cs, cd, p);
                throw pr.getRunningException();
            } else {
                try {
                    ProductDownloadHomeAction.writeResponseWithVelocity(mc, cs, cd, p, getResponse().getWriter());
                } catch (IOException e) {
                    throw new MotuException(ErrorType.SYSTEM, "Error while using velocity template", e);
                }

                String productURL = BLLManager.getInstance().getCatalogManager().getProductManager()
                        .getProductDownloadHttpUrl(pr.getProductFileName());

                // Synchronous mode
                if (mode.equalsIgnoreCase(MotuRequestParametersConstant.PARAM_MODE_CONSOLE)) {
                    try {
                        getResponse().sendRedirect(productURL);
                    } catch (IOException e) {
                        throw new MotuException(ErrorType.SYSTEM, "Error while sending download redirection PARAM_MODE_CONSOLE", e);
                    }
                } else { // Default mode MotuRequestParametersConstant.PARAM_MODE_URL

                    getResponse().setContentType(CONTENT_TYPE_PLAIN);
                    try {
                        getResponse().getWriter().write(productURL);
                    } catch (IOException e) {
                        throw new MotuException(ErrorType.SYSTEM, "Error while writing download result CONTENT_TYPE_PLAIN", e);
                    }
                }
            }
        }
    }

    /**
     * .
     * 
     * @throws MotuException
     */
    private void onError(MotuConfig mc_, ConfigService cs, CatalogData cd, Product p) throws MotuException {
        try {
            ProductDownloadHomeAction.writeResponseWithVelocity(mc_, cs, cd, p, getResponse().getWriter());
        } catch (IOException e) {
            throw new MotuException(ErrorType.SYSTEM, "Error while using velocity template", e);
        }
    }

    private ExtractionParameters createExtractionParameters() {
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

                productHTTPParameterValidator.getParameterValueValidated(),
                OutputFormat.valueOf(outputFormatParameterValidator.getParameterValueValidated()),
                getLoginOrUserHostname(),
                isAnAnonymousUser(),
                scriptVersionParameterValidator.getParameterValueValidated());
        extractionParameters.setUserHost(getLoginOrUserHostname());

        // Set assertion to manage CAS.
        extractionParameters.setAssertion(AssertionHolder.getAssertion());
        return extractionParameters;
    }

    private StatusModeResponse createStatusModeResponse(long requestId) {
        ObjectFactory objectFactory = new ObjectFactory();
        StatusModeResponse statusModeResponse = objectFactory.createStatusModeResponse();
        statusModeResponse.setCode(StringUtils.getErrorCode(getActionCode(), ErrorType.OK));
        statusModeResponse.setStatus(StatusModeType.INPROGRESS);
        statusModeResponse.setMsg("request in progress");
        statusModeResponse.setRequestId(requestId);
        return statusModeResponse;
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        modeHTTPParameterValidator.validate();
        productHTTPParameterValidator.validate();
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

        outputFormatParameterValidator.validate();
        scriptVersionParameterValidator.validate();
    }

}
