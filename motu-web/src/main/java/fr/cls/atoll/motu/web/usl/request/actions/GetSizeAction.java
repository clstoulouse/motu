package fr.cls.atoll.motu.web.usl.request.actions;

import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_END_DATE;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_HIGH_Z;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_LOW_Z;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_START_DATE;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jasig.cas.client.util.AssertionHolder;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.web.bll.request.ExtractionParameters;
import fr.cls.atoll.motu.web.common.format.OutputFormat;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.usl.request.parameter.CommonHTTPParameters;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.DepthHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.LatitudeHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.LongitudeHTTPParameterValidator;
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
 * </ul>
 * 
 * @author Pierre LACOSTE
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class GetSizeAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String ACTION_NAME = "getsize";

    private DepthHTTPParameterValidator depthLowHTTPParameterValidator;
    private DepthHTTPParameterValidator depthHighHTTPParameterValidator;
    private LatitudeHTTPParameterValidator latitudeLowHTTPParameterValidator;
    private LatitudeHTTPParameterValidator latitudeHighHTTPParameterValidator;
    private LongitudeHTTPParameterValidator longitudeLowHTTPParameterValidator;
    private LongitudeHTTPParameterValidator longitudeHighHTTPParameterValidator;

    private TemporalHTTPParameterValidator startDateTemporalHTTPParameterValidator;
    private TemporalHTTPParameterValidator endDateTemporalHighHTTPParameterValidator;

    private ServiceHTTPParameterValidator serviceHTTPParameterValidator;

    /**
     * 
     * @param actionName_
     */
    public GetSizeAction(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super(ACTION_NAME, request, response, session);

        serviceHTTPParameterValidator = new ServiceHTTPParameterValidator(
                MotuRequestParametersConstant.PARAM_SERVICE,
                CommonHTTPParameters.getServiceFromRequest(getRequest()));

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
    }

    /** {@inheritDoc} */
    @Override
    protected void process() throws IOException {
        retrieveSize();
    }

    private void retrieveSize() throws IOException {
        createExtractionParameters();
    }

    private ExtractionParameters createExtractionParameters() throws IOException {
        Writer out = null;
        OutputFormat responseFormat = null;

        out = getResponse().getWriter();
        responseFormat = OutputFormat.HTML;

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

        // Set assertion to manage CAS.
        extractionParameters.setAssertion(AssertionHolder.getAssertion());
        return extractionParameters;
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
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

    /**
     * Valeur de depthLowHTTPParameterValidator.
     * 
     * @return la valeur.
     */
    public DepthHTTPParameterValidator getDepthLowHTTPParameterValidator() {
        return depthLowHTTPParameterValidator;
    }

    /**
     * Valeur de depthHighHTTPParameterValidator.
     * 
     * @return la valeur.
     */
    public DepthHTTPParameterValidator getDepthHighHTTPParameterValidator() {
        return depthHighHTTPParameterValidator;
    }

    /**
     * Valeur de latitudeLowHTTPParameterValidator.
     * 
     * @return la valeur.
     */
    public LatitudeHTTPParameterValidator getLatitudeLowHTTPParameterValidator() {
        return latitudeLowHTTPParameterValidator;
    }

    /**
     * Valeur de latitudeHighHTTPParameterValidator.
     * 
     * @return la valeur.
     */
    public LatitudeHTTPParameterValidator getLatitudeHighHTTPParameterValidator() {
        return latitudeHighHTTPParameterValidator;
    }

    /**
     * Valeur de longitudeLowHTTPParameterValidator.
     * 
     * @return la valeur.
     */
    public LongitudeHTTPParameterValidator getLongitudeLowHTTPParameterValidator() {
        return longitudeLowHTTPParameterValidator;
    }

    /**
     * Valeur de longitudeHighHTTPParameterValidator.
     * 
     * @return la valeur.
     */
    public LongitudeHTTPParameterValidator getLongitudeHighHTTPParameterValidator() {
        return longitudeHighHTTPParameterValidator;
    }

    /**
     * Valeur de startDateTemporalHTTPParameterValidator.
     * 
     * @return la valeur.
     */
    public TemporalHTTPParameterValidator getStartDateTemporalHTTPParameterValidator() {
        return startDateTemporalHTTPParameterValidator;
    }

    /**
     * Valeur de endDateTemporalHighHTTPParameterValidator.
     * 
     * @return la valeur.
     */
    public TemporalHTTPParameterValidator getEndDateTemporalHighHTTPParameterValidator() {
        return endDateTemporalHighHTTPParameterValidator;
    }

    /**
     * Valeur de serviceHTTPParameterValidator.
     * 
     * @return la valeur.
     */
    public ServiceHTTPParameterValidator getServiceHTTPParameterValidator() {
        return serviceHTTPParameterValidator;
    }
}
