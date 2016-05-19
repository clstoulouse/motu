package fr.cls.atoll.motu.web.usl.request.actions;

import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_ANONYMOUS;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_BATCH;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_END_DATE;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_LANGUAGE;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_PRIORITY;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_PWD;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_START_DATE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.common.format.OutputFormat;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.usl.common.utils.HTTPUtils;
import fr.cls.atoll.motu.web.usl.request.parameter.CommonHTTPParameters;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
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

    private static final Logger LOGGER = LogManager.getLogger();

    /** The Constant CONTENT_TYPE_PLAIN. */
    public static final String CONTENT_TYPE_PLAIN = "text/plain";

    /** The Constant CONTENT_TYPE_XML. */
    public static final String CONTENT_TYPE_XML = "text/xml";

    /** The Constant CONTENT_TYPE_HTML. */
    public static final String CONTENT_TYPE_HTML = "text/html";

    private String actionName;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    public AbstractAction(String actionName_, HttpServletRequest request_, HttpServletResponse response_) {
        this(actionName_, request_, response_, null);
    }

    public AbstractAction(String actionName_, HttpServletRequest request_, HttpServletResponse response_, HttpSession session_) {
        actionName = actionName_;
        request = request_;
        response = response_;
        session = session_;
    }

    public void doAction() throws IOException, InvalidHTTPParameterException {
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

    protected abstract void process() throws IOException;

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

    public String getLanguageFromRequest() {
        return getRequest().getParameter(PARAM_LANGUAGE);
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
     * Gets the request priority.
     * 
     * @param request the request
     * 
     * @return the request priority
     */
    public int getRequestPriorityFromRequest() {
        String priorityStr = getRequest().getParameter(PARAM_PRIORITY);
        int priority;
        try {
            priority = Integer.valueOf(priorityStr);
        } catch (Exception e) {
            priority = BLLManager.getInstance().getConfigManager().getQueueServerConfigManager().getRequestDefaultPriority();
        }
        return priority;
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

    public String getPasswordFromRequest() {
        return request.getParameter(PARAM_PWD);
    }

    public String getLoginOrUserHostname() {
        String userLoginOrHostName = getLoginFromRequest();
        if (userLoginOrHostName == null || userLoginOrHostName.trim().length() <= 0) {
            String userHost = getUserHostName();
            userLoginOrHostName = userHost;
        }
        return userLoginOrHostName;
    }

    public String getAnonymousParameterFromRequest() {
        return getRequest().getParameter(PARAM_ANONYMOUS);
    }

    private boolean isAnonymousParameter() {
        String anonymousUserAsString = getAnonymousParameterFromRequest();
        return anonymousUserAsString != null && (anonymousUserAsString.equalsIgnoreCase("true") || anonymousUserAsString.equalsIgnoreCase("1"));
    }

    /**
     * Checks if is anonymous user.
     *
     * @param request the request
     * @param userId the user id
     * @return true, if is anonymous user
     */
    public boolean isAnAnonymousUser() {
        String userId = getLoginFromRequest();
        return (userId == null || userId.trim().length() <= 0 || userId.equalsIgnoreCase(MotuRequestParametersConstant.PARAM_ANONYMOUS_USER_VALUE)
                || isAnonymousParameter());
    }

    /**
     * Gets the forwarded for.
     * 
     * @param request the request
     * 
     * @return the forwarded for
     */
    private String getForwardedForHostnameFromRequest() {
        String forwardedFor = getRequest().getParameter(MotuRequestParametersConstant.PARAM_FORWARDED_FOR);
        return HTTPUtils.getHostName(forwardedFor);
    }

    /** The Constant proxyHeaders. */
    private static final List<String> PROXY_HEADERS = new ArrayList<String>();

    static {
        PROXY_HEADERS.add("x-forwarded-for");
        PROXY_HEADERS.add("HTTP_X_FORWARDED_FOR");
        PROXY_HEADERS.add("HTTP_FORWARDED");
        PROXY_HEADERS.add("HTTP_CLIENT_IP");
    }

    public String getHostFromRequestHeader() {
        String hostName = getRequest().getRemoteAddr();
        for (String ph : PROXY_HEADERS) {
            String v = getRequest().getHeader(ph);
            if (v != null) {
                hostName = v;
                break;
            }
        }
        return hostName;
    }

    /**
     * Gets the user remote host.
     * 
     * @param request the request
     * 
     * @return the remote host
     */
    public String getUserHostName() {
        String hostName = getForwardedForHostnameFromRequest();
        if (hostName == null || hostName.trim().length() <= 0) {
            hostName = getHostFromRequestHeader();
        }
        return HTTPUtils.getHostName(hostName);
    }

    /**
     * Gets Organizer object form the HttpSession.
     *
     * @param session that contains Organizer.
     * @param response the response
     * @return Organizer object.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private Organizer getOrganizer() throws IOException {

        Organizer organizer = null;
        try {
            if (session != null) {
                organizer = SessionManager.getInstance().getOrganizer(getSession());
                isValid(organizer);
            } else {
                organizer = new Organizer();
            }
        } catch (MotuExceptionBase e) {
            response.sendError(500,
                               String.format("ERROR: - MotuServlet.getOrganizer - Unable to create a new organiser. Native Error: %s",
                                             e.notifyException()));
        } catch (ServletException e) {
            response.sendError(500, String.format("ERROR: - MotuServlet.getOrganizer : %s", e.getMessage()));
        }

        return organizer;
    }

    /**
     * Tests if HttpSession object is valid.
     * 
     * @param session instance to be tested.
     * 
     * @throws ServletException the servlet exception
     */
    private void isValid(HttpSession session) throws ServletException {
        if (session == null) {
            throw new ServletException(new MotuException("Error - session is null"));
        }
    }

    /**
     * Tests if Organizer object is valid.
     * 
     * @param organizer instance to be tested.
     * 
     * @throws ServletException the servlet exception
     */
    private void isValid(Organizer organizer) throws ServletException {
        if (organizer == null) {
            throw new ServletException(new MotuException("Error - organizer is null - perhaps session has expired."));
        }
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
        String startDate = getRequest().getParameter(PARAM_START_DATE);
        String endDate = getRequest().getParameter(PARAM_END_DATE);
        List<String> listTemporalCoverage = new ArrayList<String>();

        if (startDate != null) {
            listTemporalCoverage.add(startDate);
        }
        if (endDate != null) {
            listTemporalCoverage.add(endDate);
        }
        return listTemporalCoverage;
    }

    protected String getProductId() throws IOException {
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
    protected String getProductIdFromParamId(String productId) throws IOException, ServletException, MotuException {
        String serviceName = CommonHTTPParameters.getServiceFromRequest(getRequest());

        if ((StringUtils.isNullOrEmpty(serviceName)) || (StringUtils.isNullOrEmpty(productId))) {
            return productId;
        }

        Organizer organizer = getOrganizer();

        return organizer.getDatasetIdFromURI(productId, serviceName);
    }

    protected OutputFormat getOutputFormat() throws IOException {
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

    /**
     * Checks if is batch.
     * 
     * @param request the request
     * 
     * @return true, if is batch
     */
    protected boolean isBatch() {
        String batchAsString = getBatchParameter();
        return batchAsString != null && (batchAsString.trim().equalsIgnoreCase("true") || batchAsString.trim().equalsIgnoreCase("1"));
    }

    private String getBatchParameter() {
        return getRequest().getParameter(PARAM_BATCH);
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
                    String.format("Parameter '%s': invalid value '%s' - Valid values are : %s",
                                  MotuRequestParametersConstant.PARAM_OUTPUT,
                                  dataFormat,
                                  OutputFormat.valuesToString()),
                    e);
        }

        return format;
    }

}
