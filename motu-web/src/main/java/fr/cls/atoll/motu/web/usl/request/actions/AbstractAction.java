package fr.cls.atoll.motu.web.usl.request.actions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.api.message.xml.ProductMetadataInfo;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.common.format.OutputFormat;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.usl.USLManager;
import fr.cls.atoll.motu.web.usl.common.utils.HTTPUtils;
import fr.cls.atoll.motu.web.usl.request.parameter.CommonHTTPParameters;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.request.parameter.validator.ServiceHTTPParameterValidator;
import fr.cls.atoll.motu.web.usl.request.session.SessionManager;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public abstract class AbstractAction {

    public static final String UNDETERMINED_ACTION = "001";

    private static final Logger LOGGER = LogManager.getLogger();

    private String actionName;
    private String parameters;
    private String userId;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private String actionCode = "0001";

    public AbstractAction(String actionName_, String actionCode_, HttpServletRequest request_, HttpServletResponse response_) {
        this(actionName_, actionCode_, request_, response_, null);
    }

    public AbstractAction(String actionName_, String actionCode_, HttpServletRequest request_, HttpServletResponse response_, HttpSession session_) {
        actionName = actionName_;
        actionCode = actionCode_;
        request = request_;
        response = response_;
        session = session_;
        userId = USLManager.getInstance().getUserManager().getUserName();// getLoginFromRequest();
        generateParameterString();
    }

    public void doAction() throws MotuException, InvalidHTTPParameterException {
        onActionStarts();
        if (isAuthorized()) {
            checkHTTPParameters();
            process();
        }
        onActionEnds();
    }

    /**
     * .
     */
    abstract protected void checkHTTPParameters() throws InvalidHTTPParameterException;

    /**
     * Checks if is authorized.
     * 
     * @return true, if is authorized
     */
    protected boolean isAuthorized() {
        return true;
    }

    protected abstract void process() throws MotuException;

    /**
     * .
     * 
     * @param responseStr_
     * @param responseContentType_
     * @param headerArrayMultipleOf2Elements : An array of K, V, K, V, K, V, this array has always a pair size
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public void writeResponse(String responseStr_, String responseContentType_, String[] headerArrayMultipleOf2Elements)
            throws UnsupportedEncodingException, IOException {
        HTTPUtils.writeHttpResponse(getResponse(), responseStr_, responseContentType_, headerArrayMultipleOf2Elements);
    }

    public void writeResponse(String responseStr_, String responseContentType_) throws UnsupportedEncodingException, IOException {
        writeResponse(responseStr_, responseContentType_, null);
    }

    public void writeResponse(String responseStr_) throws UnsupportedEncodingException, IOException {
        writeResponse(responseStr_, null);
    }

    protected void onActionStarts() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("START - " + actionName);
        }
    }

    protected void onActionEnds() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("END - " + actionName);
        }
    }

    /**
     * Valeur de response.
     * 
     * @return la valeur.
     */
    public HttpServletResponse getResponse() {
        return response;
    }

    /**
     * Valeur de session.
     * 
     * @return la valeur.
     */
    public HttpSession getSession() {
        return session;
    }

    /**
     * Valeur de request.
     * 
     * @return la valeur.
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * .
     * 
     * @return
     */
    public String getModeFromRequest() {
        return CommonHTTPParameters.getModeFromRequest(getRequest());
    }

    /**
     * Gets the login.
     *
     * @param request the request
     * @param session the session
     * @return the login
     */
    public String getLoginFromRequest() {
        String userId = getRequest().getParameter(MotuRequestParametersConstant.PARAM_LOGIN);

        if ((userId == null || userId.trim().length() <= 0) && session != null) {
            userId = SessionManager.getInstance().getUserId(getSession());
        }
        return userId;
    }

    protected String getDataFromParameter() {
        return getRequest().getParameter(MotuRequestParametersConstant.PARAM_DATA);
    }

    /**
     * Gets the temporal coverage from the request.
     * 
     * @param request servlet request
     * 
     * @return a list of temporable coverage, first start date, and then end date (they can be empty string)
     */
    protected List<String> getTemporalCoverage() {
        String startDate = CommonHTTPParameters.getStartDateFromRequest(getRequest());
        String endDate = CommonHTTPParameters.getEndDateFromRequest(getRequest());
        List<String> listTemporalCoverage = new ArrayList<String>();

        if (startDate != null) {
            listTemporalCoverage.add(startDate);
        }
        if (endDate != null) {
            listTemporalCoverage.add(endDate);
        }
        return listTemporalCoverage;
    }

    protected OutputFormat getOutputFormat() throws IOException {
        OutputFormat dataFormat = null;
        try {
            dataFormat = getDataFormatFromParameter();
        } catch (MotuException e) {
            getResponse().sendError(400, String.format("ERROR: %s", e.getMessage()));
        }
        return dataFormat;
    }

    /**
     * Gets the data format.
     *
     * @param request the request
     * @return the data format
     * @throws MotuException the motu exception
     */
    protected OutputFormat getDataFormatFromParameter() throws MotuException {
        String dataFormat = getRequest().getParameter(MotuRequestParametersConstant.PARAM_OUTPUT);
        OutputFormat format;
        if (StringUtils.isNullOrEmpty(dataFormat)) {
            return OutputFormat.getDefault();
        }

        try {
            format = OutputFormat.valueOf(dataFormat.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new MotuException(
                    ErrorType.SYSTEM,
                    String.format("Parameter '%s': invalid value '%s' - Valid values are : %s",
                                  MotuRequestParametersConstant.PARAM_OUTPUT,
                                  dataFormat,
                                  OutputFormat.valuesToString()),
                    e);
        }

        return format;
    }

    public String getActionCode() {
        return actionCode;
    }

    /**
     * Valeur de actionCode.
     * 
     * @param actionCode nouvelle valeur.
     */
    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public String getActionName() {
        return actionName;
    }

    /**
     * Valeur de parameters.
     * 
     * @return la valeur.
     */
    public String getParameters() {
        return parameters;
    }

    /**
     * Valeur de userId.
     * 
     * @return la valeur.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * This method save the parameters of the request into a unic string. .
     */
    private void generateParameterString() {
        StringBuffer parameterStr = new StringBuffer();
        for (Map.Entry<String, String[]> parameter : request.getParameterMap().entrySet()) {
            parameterStr.append(parameter.getKey());
            parameterStr.append("=");
            String[] values = parameter.getValue();
            StringBuffer valueStr = new StringBuffer();
            for (String value : values) {
                valueStr.append(value);
                valueStr.append(",");
            }
            if (valueStr.length() > 0) {
                valueStr = valueStr.delete(valueStr.length() - 1, valueStr.length());
            }
            parameterStr.append(valueStr);

            parameterStr.append(" ");
        }
        parameters = parameterStr.toString();
    }

    protected boolean checkProduct(Product p, String productId) throws MotuException {
        boolean isValid = (p != null);
        if (!isValid) {
            onArgumentError(new MotuException(ErrorType.UNKNOWN_PRODUCT, productId));
        }
        return isValid;
    }

    protected boolean checkProductMetaDataInfo(ProductMetadataInfo productMetadataInfo, String productId) throws MotuException {
        boolean isValid = (productMetadataInfo != null);
        if (!isValid) {
            onArgumentError(new MotuException(ErrorType.SYSTEM, "Unable to load product medata info for product id=" + productId));
        }
        return isValid;
    }

    protected boolean checkConfigService(ConfigService cs, ServiceHTTPParameterValidator serviceHTTPParameterValidator) throws MotuException {
        boolean isValid = (cs != null);
        if (!isValid) {
            onArgumentError(new MotuException(ErrorType.UNKNOWN_SERVICE, serviceHTTPParameterValidator.getParameterValue()));
        }
        return isValid;

    }

    protected void onArgumentError(MotuException motuException) throws MotuException {
        throw motuException;
    }

}
