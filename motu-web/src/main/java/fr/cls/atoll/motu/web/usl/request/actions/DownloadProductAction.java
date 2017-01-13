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
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.model.ExtractionParameters;
import fr.cls.atoll.motu.web.bll.request.model.ProductResult;
import fr.cls.atoll.motu.web.bll.request.model.RequestProduct;
import fr.cls.atoll.motu.web.common.format.OutputFormat;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.config.xml.model.MotuConfig;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.usl.USLManager;
import fr.cls.atoll.motu.web.usl.request.USLRequestManager;
import fr.cls.atoll.motu.web.usl.request.parameter.CommonHTTPParameters;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.DepthHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.LatitudeHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.LongitudeHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.ModeHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.OutputFormatHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.ProductHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.ScriptVersionHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.ServiceHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.TemporalHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.response.xml.converter.XMLConverter;

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
 * <li><b>variable</b>: [0,n]: physical variables to be extracted from the product. When no variable is set,
 * all the variables of the dataset are extracted.</li>
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
 * <li><b>url</b>: URL of the delivery file is directly returned in the HTTP response as an HTML web page.
 * Then Javascript read this URL to download file. The request is processed in a synchronous mode.</li>
 * <li><b>console</b>: the response is a 302 HTTP redirection to the delivery file to be returned as a binary
 * stream. The request is processed in a synchronous mode.</li>
 * <li><b>status</b>: request is submitted and the status of the request processing is immediately returned.
 * The request is processed in an asynchronous mode.<br>
 * Web Portal submits the request to the Dissemination Unit Subsetter and gets an immediate response of the
 * Subsetter. This response contains the identifier and the status of the order (pending, in progress, done,
 * error).<br>
 * So long as the order is not completed (done or error), Web Portal requests the status of the order at
 * regular and fair intervals (> 5 seconds) and gets an immediate response. When the status is “done”, Web
 * Portal retrieves the url of the file to download, from the status response. Then Web Portal redirects
 * response to this url. The Web Browser opens a binary stream of the file to download and shows a dialog box
 * to allow the user saving it as a local file.</li>
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

    private OutputFormatHTTPParameterValidator outputFormatParameterValidator;

    private ScriptVersionHTTPParameterValidator scriptVersionParameterValidator;

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

        outputFormatParameterValidator = new OutputFormatHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_OUTPUT,
                CommonHTTPParameters.getOutputFormatFromRequest(getRequest()),
                OutputFormat.NETCDF.name().toUpperCase());

        scriptVersionParameterValidator = new ScriptVersionHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_SCRIPT_VERSION,
                CommonHTTPParameters.getScriptVersionFromRequest(getRequest()),
                "");
    }

    @Override
    public void process() throws MotuException {
        MotuConfig mc = BLLManager.getInstance().getConfigManager().getMotuConfig();
        ConfigService cs = BLLManager.getInstance().getConfigManager().getConfigService(serviceHTTPParameterValidator.getParameterValueValidated());
        if (checkConfigService(cs, serviceHTTPParameterValidator)) {
            CatalogData cd = BLLManager.getInstance().getCatalogManager().getCatalogAndProductCacheManager().getCatalogCache().getCatalog(cs.getName());
            if (cd != null) {
                String productId = productHTTPParameterValidator.getParameterValueValidated();
                Product p = BLLManager.getInstance().getCatalogManager().getProductManager().getProduct(productId);
                if (checkProduct(p, productId)) {
                    RequestProduct rp = new RequestProduct(p, createExtractionParameters());
                    downloadProduct(mc, cs, cd, rp);
                }
            } else {
                throw new MotuException(ErrorType.SYSTEM, "Error while get catalog data for config service " + cs.getName());
            }
        }

    }

    @Override
    protected void onArgumentError(MotuException motuException) throws MotuException {
        try {
            getResponse().setContentType(CONTENT_TYPE_XML);
            String response = XMLConverter.toXMLString(motuException, getActionCode(), scriptVersionParameterValidator.getParameterValueValidated());
            getResponse().getWriter().write(response);
        } catch (IOException e) {
            throw new MotuException(ErrorType.SYSTEM, "Error while writing HTTP response ", e);
        }
    }

    /**
     * .
     * 
     * @throws MotuException
     */
    private void downloadProduct(MotuConfig mc, ConfigService cs, CatalogData cd, RequestProduct requestProduct)
            throws MotuException {
        String mode = modeHTTPParameterValidator.getParameterValueValidated();

        if (mode.equalsIgnoreCase(MotuRequestParametersConstant.PARAM_MODE_STATUS)) {
            // Asynchronous mode
            onAsynchronousMode(cs, requestProduct);
        } else {
            ProductResult pr = BLLManager.getInstance().getRequestManager().download(cs, requestProduct, this);
            if (pr.getRunningException() != null) {
                setProductException(requestProduct, pr.getRunningException());
                onError(mc, cs, cd, requestProduct, pr.getRunningException());
            } else {
                String productURL = BLLManager.getInstance().getCatalogManager().getProductManager()
                        .getProductDownloadHttpUrl(pr.getProductFileName());
                // Synchronous mode
                if (mode.equalsIgnoreCase(MotuRequestParametersConstant.PARAM_MODE_CONSOLE)) {
                    onSynchronousRedirectMode(productURL);
                } else { // Default mode MotuRequestParametersConstant.PARAM_MODE_URL
                    onSynchronousURLMode(mc, cs, cd, requestProduct);
                }
            }
        }
    }
    
    private void onAsynchronousMode(ConfigService cs, RequestProduct requestProduct) throws MotuException{
        long requestId = BLLManager.getInstance().getRequestManager().downloadAsynchonously(cs, requestProduct, this);
        try {
            getResponse().setContentType(CONTENT_TYPE_XML);
            String response = XMLConverter.toXMLString(requestId, getActionCode(), scriptVersionParameterValidator.getParameterValueValidated());
            getResponse().getWriter().write(response);
        } catch (IOException e) {
            throw new MotuException(ErrorType.SYSTEM, "Error while writing HTTP response ", e);
        }
    }
    
    private void onSynchronousRedirectMode(String productURL) throws MotuException{
        try {
            getResponse().sendRedirect(productURL);
        } catch (IOException e) {
            throw new MotuException(ErrorType.SYSTEM, "Error while sending download redirection PARAM_MODE_CONSOLE", e);
        }
    }
    
    private void onSynchronousURLMode(MotuConfig mc, ConfigService cs, CatalogData cd, RequestProduct requestProduct) throws MotuException{
        try {
            ProductDownloadHomeAction.writeResponseWithVelocity(mc, cs, cd, requestProduct, getResponse().getWriter());
        } catch (IOException e) {
            throw new MotuException(ErrorType.SYSTEM, "Error while using velocity template", e);
        }
    }

    private void setProductException(RequestProduct requestProduct, MotuException runningException) throws MotuException {
        ErrorType errorType = runningException.getErrorType();
        String errMsg = StringUtils.getLogMessage(getActionCode(),
                                                  errorType,
                                                  BLLManager.getInstance().getMessagesErrorManager().getMessageError(errorType, runningException));
        if (outputFormatParameterValidator.getParameterValueValidated().equalsIgnoreCase(OutputFormat.NETCDF4.name())) {
            errMsg = StringUtils.getLogMessage(getActionCode(),
                                               errorType,
                                               " (" + OutputFormat.NETCDF4.name() + " - "
                                                       + StringUtils.getErrorCode(getActionCode(), ErrorType.NETCDF4_NOT_SUPPORTED_BY_TDS) + ") "
                                                       + BLLManager.getInstance().getMessagesErrorManager()
                                                               .getMessageError(ErrorType.NETCDF4_NOT_SUPPORTED_BY_TDS, runningException));
        }
        requestProduct.setLastError(errMsg);
    }

    /**
     * .
     * 
     * @throws MotuException
     */
    private void onError(MotuConfig mc_, ConfigService cs, CatalogData cd, RequestProduct reqProduct, MotuException e_) throws MotuException {
        try {
            if (e_ != null && USLRequestManager.isErrorTypeToLog(e_.getErrorType())) {
                LOGGER.error(e_);
            }
            ProductDownloadHomeAction.writeResponseWithVelocity(mc_, cs, cd, reqProduct, getResponse().getWriter());
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
                USLManager.getInstance().getUserManager().getLoginOrUserHostname(getRequest()),
                USLManager.getInstance().getUserManager().getUserHostName(getRequest()),
                USLManager.getInstance().getUserManager().isUserAnonymous(),
                scriptVersionParameterValidator.getParameterValueValidated());

        // Set assertion to manage CAS.
        extractionParameters.setAssertion(AssertionHolder.getAssertion());
        return extractionParameters;
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

        outputFormatParameterValidator.validate();
        scriptVersionParameterValidator.validate();
    }

}
