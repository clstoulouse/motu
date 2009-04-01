package fr.cls.atoll.motu.processor.wps;

import java.net.Authenticator;
import java.util.ArrayList;
import java.util.List;

import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.BoundingBoxInput;
import org.deegree.services.wps.input.LiteralInput;
import org.deegree.services.wps.input.ProcessletInput;
import org.deegree.services.wps.output.LiteralOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cls.atoll.motu.library.configuration.MotuConfig;
import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.atoll.motu.library.intfce.SimpleAuthenticator;
import fr.cls.atoll.motu.library.queueserver.QueueServerManagement;
import fr.cls.atoll.motu.library.queueserver.RequestManagement;
import fr.cls.atoll.motu.msg.xml.ErrorType;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.6 $ - $Date: 2009-04-01 14:13:38 $
 */
public abstract class MotuWPSProcess implements Processlet {

    private static final Logger LOG = LoggerFactory.getLogger(MotuWPSProcess.class);

    /** The Constant PARAM_FORWARDED_FOR (Real user Ip). */
    public static final String PARAM_ANONYMOUS = "anonymous";

    /** The Constant PARAM_ANONYMOUS_USER_VALUE. */
    public static final String PARAM_ANONYMOUS_USER_VALUE = "isanonymous";

    /** The Constant PARAM_BATCH. */
    public static final String PARAM_BATCH = "batch";

    /** Process output return code parameter name. */
    public static final String PARAM_CODE = "code";

    public static final String PARAM_DATAFORMAT = "dataformat";

    public static final String PARAM_ENDTIME = "endtime";
    
    /** Low latitude servlet paremeter name. */
    public static final String PARAM_GEOBBOX = "geobbox";

    public static final String PARAM_HIGHDEPTH = "highdepth";

    /** Language servlet paremeter name. */
    public static final String PARAM_LANGUAGE = "lang";

    /** Login parameter name. */
    public static final String PARAM_LOGIN = "login";

    public static final String PARAM_LOWDEPTH = "lowdepth";

    /** Process output message parameter name. */
    public static final String PARAM_MESSAGE = "message";
    
    /** Mode parameter name. */
    public static final String PARAM_MODE = "mode";
    

    /** Mode Status parameter value. */
    public static final String PARAM_MODE_CONSOLE = "console";
    
    /** Mode Status parameter value. */
    public static final String PARAM_MODE_STATUS = "status";
    
    /** Mode Url parameter value. */
    public static final String PARAM_MODE_URL = "url";

    /** Priority parameter name. */
    public static final String PARAM_PRIORITY = "priority";

    /** Product servlet parameter name. */
    public static final String PARAM_PRODUCT = "product";

    /** Service servlet parameter name. */
    public static final String PARAM_SERVICE = "service";

    public static final String PARAM_STARTTIME = "starttime";

    /** Url parameter name. */
    public static final String PARAM_URL = "url";

    /** Variable servlet paremeter name. */
    public static final String PARAM_VARIABLE = "variable";

    /** The Constant PARAM_MAX_POOL_ANONYMOUS. */
    public static final String PARAM_MAX_POOL_ANONYMOUS = "maxpoolanonymous";

    /** The Constant PARAM_MAX_POOL_AUTHENTICATE. */
    public static final String PARAM_MAX_POOL_AUTHENTICATE = "maxpoolauth";

    protected ProcessletInputs processletInputs;
    protected ProcessletOutputs processletOutputs;
    protected ProcessletExecutionInfo processletExecutionInfo;
    /** The request management. */
    protected RequestManagement requestManagement = null;

    /**
     * Gets the queue server management.
     * 
     * @return the queue server management
     */
    protected QueueServerManagement getQueueServerManagement() {
        return requestManagement.getQueueServerManagement();

    };

    /**
     * Constructeur.
     */
    public MotuWPSProcess() {
    }

    public void setLanguageParameter(ProcessletInputs in) throws ProcessletException {

        LiteralInput languageParam = (LiteralInput) in.getParameter(MotuWPSProcess.PARAM_LANGUAGE);

        if (MotuWPSProcess.isNullOrEmpty(languageParam)) {
            return;
        }

        Organizer organizer = getOrganizer();
        try {
            organizer.setCurrentLanguage(languageParam.getValue());
        } catch (MotuExceptionBase e) {
            throw new ProcessletException(e.notifyException());
        } catch (Exception e) {
            throw new ProcessletException(e.getMessage());
        }

    }

    /** {@inheritDoc} */
    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    @Override
    public void init() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("MotuWPSProcess#init() called");
        }

        try {
            initProxyLogin();
        } catch (ProcessletException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("END MotuWPSProcess.initProxyLogin() -- ProcessletException");
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void process(ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info) throws ProcessletException {
        processletExecutionInfo = info;
        processletInputs = in;
        processletOutputs = out;
    }

    /**
     * Sets parameters for proxy connection if a proxy is used.
     * 
     * @throws ProcessletException
     * 
     */
    protected void initProxyLogin() throws ProcessletException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("BEGIN MotuWPSProcess.initProxyLogin()");
            LOG.debug("BEGIN ProductTimeCoverageProcess.initProxyLogin()");
            LOG.debug("proxyHost:");
            LOG.debug(System.getProperty("proxyHost"));
            LOG.debug("proxyPort:");
            LOG.debug(System.getProperty("proxyPort"));
            LOG.debug("socksProxyHost:");
            LOG.debug(System.getProperty("socksProxyHost"));
            LOG.debug("socksProxyPort:");
            LOG.debug(System.getProperty("socksProxyPort"));
            LOG.debug("properties:");
            LOG.debug(System.getProperties().toString());
        }

        try {
            MotuConfig motuConfig = Organizer.getMotuConfigInstance();
            if (motuConfig.isUseProxy()) {
                String user = Organizer.getMotuConfigInstance().getProxyLogin();
                String pwd = Organizer.getMotuConfigInstance().getProxyPwd();
                System.setProperty("proxyHost", Organizer.getMotuConfigInstance().getProxyHost());
                System.setProperty("proxyPort", Organizer.getMotuConfigInstance().getProxyPort());
                if (user != null && pwd != null) {
                    if (!user.equals("") && !pwd.equals("")) {
                        Authenticator.setDefault(new SimpleAuthenticator(user, pwd));
                    }
                }
            }
        } catch (MotuException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("END MotuWPSProcess.initProxyLogin() -- MotuException");
                throw new ProcessletException(String.format("Proxy initialisation failure - %s", e.notifyException()));
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("END MotuWPSProcess.initProxyLogin()");
        }
    }

    /**
     * Test if a string is null or empty.
     * 
     * @param value string to be tested.
     * 
     * @return true if string is null or empty, otherwise false.
     */
    protected static boolean isNullOrEmpty(String value) {
        if (value == null) {
            return true;
        }
        if (value.equals("")) {
            return true;
        }
        return false;
    }

    /**
     * Checks if is null or empty.
     * 
     * @param value the value
     * 
     * @return true, if is null or empty
     */
    protected static boolean isNullOrEmpty(LiteralInput value) {
        if (value == null) {
            return true;
        }

        return isNullOrEmpty(value.getValue());
    }

    protected void setReturnCode(ErrorType code, String msg) {

        LiteralOutput codeParam = (LiteralOutput) processletOutputs.getParameter(MotuWPSProcess.PARAM_CODE);
        LiteralOutput msgParam = (LiteralOutput) processletOutputs.getParameter(MotuWPSProcess.PARAM_MESSAGE);

        if ((codeParam != null) && (code != null)) {
            codeParam.setValue(code.toString());
        }
        if ((msgParam != null) && (msg != null)) {
            msgParam.setValue(msg);
        }

    }
    protected void setReturnCode(Exception e) {

        setReturnCode(ErrorType.SYSTEM, e);

    }
    protected void setReturnCode(MotuExceptionBase e) {

        setReturnCode(ErrorType.SYSTEM, e);

    }
    
    /**
     * Sets the return code.
     * 
     * @param code the code
     * @param e the e
     */
    protected void setReturnCode(ErrorType code, MotuExceptionBase e) {

        setReturnCode(code, e.notifyException());

    }
    protected void setReturnCode(ErrorType code, Exception e) {

        setReturnCode(code, e.getMessage());

    }

    /**
     * Create new Organizer object.
     * 
     * @return Organizer object.
     */
    protected Organizer getOrganizer() {

        Organizer organizer = null;
        try {
            organizer = new Organizer();
        } catch (MotuExceptionBase e) {
            String msg = String.format("ERROR: - MotuWPSProcess.getOrganizer - Unable to create a new organiser. Native Error: %s", e
                    .notifyException());
            setReturnCode(ErrorType.SYSTEM, msg);
        }

        return organizer;
    }

    protected void getProductInfo(LiteralInput serviceNameParam, LiteralInput locationDataParam, LiteralInput productIdParam)
            throws ProcessletException {

        serviceNameParam = (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_SERVICE);
        locationDataParam = (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_URL);
        productIdParam = (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_PRODUCT);

        if (MotuWPSProcess.isNullOrEmpty(locationDataParam) && MotuWPSProcess.isNullOrEmpty(productIdParam)) {
            if (LOG.isDebugEnabled()) {
                LOG.info(" empty locationData and empty productId");
                LOG.debug("END MotuWPSProcess.getProductInfo()");
            }

            String msg = String.format("ERROR: neither '%s' nor '%s' parameters are filled - Choose one of them",
                                       MotuWPSProcess.PARAM_URL,
                                       PARAM_PRODUCT);

            setReturnCode(ErrorType.INCONSISTENCY, msg);
            throw new ProcessletException(msg);
        }

        if (!MotuWPSProcess.isNullOrEmpty(locationDataParam) && !MotuWPSProcess.isNullOrEmpty(productIdParam)) {
            if (LOG.isDebugEnabled()) {
                LOG.info(" non empty locationData and non empty productId");
                LOG.debug("END MotuWPSProcess.process()");
            }
            String msg = String.format("ERROR: '%s' and '%s' parameters are not compatible - Choose only one of them",
                                       MotuWPSProcess.PARAM_URL,
                                       MotuWPSProcess.PARAM_PRODUCT);

            setReturnCode(ErrorType.INCONSISTENCY, msg);
            throw new ProcessletException(msg);
        }

        if (MotuWPSProcess.isNullOrEmpty(serviceNameParam) && !MotuWPSProcess.isNullOrEmpty(productIdParam)) {
            if (LOG.isDebugEnabled()) {
                LOG.info("empty serviceName  and non empty productId");
                LOG.debug("END MotuWPSProcess.process()");
            }
            String msg = String.format("ERROR: '%s' parameter is filled but '%s' is empty. You have to fill it.", PARAM_PRODUCT, PARAM_SERVICE);

            setReturnCode(ErrorType.INCONSISTENCY, msg);
            throw new ProcessletException(msg);
        }
    }

    /**
     * Gets the temporal coverage from the request.
     * 
     * 
     * @return a list of temporable coverage, first start date, and then end date (they can be empty string)
     */
    protected List<String> getTemporalCoverage() {
        LiteralInput startDateParam = (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_STARTTIME);
        LiteralInput endDateParam = (LiteralInput) processletInputs.getParameter(PARAM_ENDTIME);
        List<String> listTemporalCoverage = new ArrayList<String>();

        if (startDateParam != null) {
            listTemporalCoverage.add(startDateParam.getValue());
        }
        if (endDateParam != null) {
            listTemporalCoverage.add(endDateParam.getValue());
        }
        return listTemporalCoverage;
    }
    
    protected Organizer.Format getDataFormat() {
        
        Organizer.Format dataFormat = Organizer.Format.getDefault();
        
        LiteralInput dataFormatParam = (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_DATAFORMAT);
        if (dataFormatParam == null) {
            return dataFormat;
        }
        
        try {
            dataFormat = Organizer.Format.valueOf(dataFormatParam.getValue());
        } catch (Exception e) {
           setReturnCode(e);
        }
        return dataFormat;
    }

    /**
     * Gets the geographical coverage from the request.
     * 
     * 
     * @return a list of geographical coverage : Lat min, Lon min, Lat max, Lon max
     */
    protected List<String> getGeoCoverage() {
        BoundingBoxInput geobboxParam = (BoundingBoxInput) processletInputs.getParameter(MotuWPSProcess.PARAM_GEOBBOX);
        List<String> listLatLonCoverage = new ArrayList<String>();

        if (geobboxParam == null) {
            listLatLonCoverage.add("-90");
            listLatLonCoverage.add("-180");
            listLatLonCoverage.add("90");
            listLatLonCoverage.add("180");
            return listLatLonCoverage;
        }
        
        double[] lower = geobboxParam.getLower();
        
        switch (lower.length) {
        case 1:
            listLatLonCoverage.add(Double.toString(lower[0]));
            listLatLonCoverage.add("-180");
            break;
        case 2:
            listLatLonCoverage.add(Double.toString(lower[0]));
            listLatLonCoverage.add(Double.toString(lower[1]));            
            break;

        default:
            listLatLonCoverage.add("-90");
            listLatLonCoverage.add("-180");
            break;
        }
        
        double[] upper = geobboxParam.getLower();
        
        switch (upper.length) {
        case 1:
            listLatLonCoverage.add(Double.toString(upper[0]));
            listLatLonCoverage.add("180");
            break;
        case 2:
            listLatLonCoverage.add(Double.toString(upper[0]));
            listLatLonCoverage.add(Double.toString(upper[1]));            
            break;

        default:
            listLatLonCoverage.add("90");
            listLatLonCoverage.add("180");
            break;
        }

        return listLatLonCoverage;
    }
    
    /**
     * Gets the depth coverage from the request.
     * 
     * @return a list of deph coverage : first depth min, then depth max
     */
    protected List<String> getDepthCoverage() {
        // -------------------------------------------------
        // Gets Depth coverage
        // -------------------------------------------------
        LiteralInput lowDepthParam = (LiteralInput) processletInputs.getParameter(PARAM_LOWDEPTH);
        LiteralInput highDepthParam = (LiteralInput) processletInputs.getParameter(PARAM_HIGHDEPTH);
        List<String> listDepthCoverage = new ArrayList<String>();

        if (lowDepthParam != null) {
            listDepthCoverage.add(lowDepthParam.getValue());
        }

        if (highDepthParam != null) {
            listDepthCoverage.add(highDepthParam.getValue());
        }
        return listDepthCoverage;
    }
    /**
     * Checks if is mode console.
     * 
     * @param mode the mode
     * 
     * @return true, if is mode console
     */
    public static boolean isModeConsole(String mode) {
        boolean isMode = false;
        if (MotuWPSProcess.hasMode(mode)) {
            isMode = mode.equalsIgnoreCase(MotuWPSProcess.PARAM_MODE_CONSOLE);
        }
        return isMode;
    }
    /**
     * Checks if is mode url.
     * 
     * @param mode the mode
     * 
     * @return true, if is mode url
     */
    public static boolean isModeUrl(String mode) {
        boolean isMode = false;
        if (MotuWPSProcess.hasMode(mode)) {
            isMode = mode.equalsIgnoreCase(MotuWPSProcess.PARAM_MODE_URL);
        }
        return isMode;
    }

    /**
     * Checks if is mode status.
     * 
     * @param mode the mode
     * 
     * @return true, if is mode status
     */
    public static boolean isModeStatus(String mode) {
        boolean isMode = false;
        if (MotuWPSProcess.hasMode(mode)) {
            isMode = mode.equalsIgnoreCase(MotuWPSProcess.PARAM_MODE_STATUS);
        }
        return isMode;
    }
    /**
     * Checks for mode.
     * 
     * @param mode the mode
     * 
     * @return true, if has mode
     */
    public static boolean hasMode(String mode) {
        return !MotuWPSProcess.noMode(mode);
    }
    /**
     * No mode.
     * 
     * @param mode the mode
     * 
     * @return true, if no mode
     */
    public static boolean noMode(String mode) {
        return MotuWPSProcess.isNullOrEmpty(mode);
    }
    
    /**
     * Checks if is anonymous user.
     * 
     * @param userId the user id
     * 
     * @return true, if is anonymous user
     */
    protected boolean isAnonymousUser(String userId) {
        if (MotuWPSProcess.isNullOrEmpty(userId)) {
            return true;
        }
        boolean anonymousUser = userId.equalsIgnoreCase(MotuWPSProcess.PARAM_ANONYMOUS_USER_VALUE);
        if (anonymousUser) {
            return true;
        }
        LiteralInput anonymousUserAsStringParam = (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_ANONYMOUS);
        if (anonymousUserAsStringParam == null) {
            return false;
        }
        String anonymousUserAsString = anonymousUserAsStringParam.getValue().trim();
        return anonymousUserAsString.equalsIgnoreCase("true") || anonymousUserAsString.equalsIgnoreCase("1");
    }
    /**
     * Checks if is batch.
     * 
     * @return true, if is batch
     */
    protected boolean isBatch() {
        LiteralInput batchAsStringParam = (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_BATCH);
        if (MotuWPSProcess.isNullOrEmpty(batchAsStringParam)) {
            return false;
        }
        String batchAsString = batchAsStringParam.getValue().trim();
        return batchAsString.equalsIgnoreCase("true") || batchAsString.equalsIgnoreCase("1");
    }
    /**
     * Gets the login.
     * 
     * @param request the request
     * 
     * @return the login
     */
    protected String getLogin() {
        String login = null;

        LiteralInput loginParam = (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_LOGIN);
        if (loginParam != null) {
            login = loginParam.getValue();
        }
        return login;
    }
    /**
     * Gets the mode parameter from the request.
     * 
     * @return the status mode
     */
    protected String getMode() {
        // -------------------------------------------------
        // Gets Depth coverage
        // -------------------------------------------------
        LiteralInput modeParam = (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_MODE);
        if (modeParam == null) {
            return "";
        }
        return modeParam.getValue();
    }

    /**
     * Gets the request priority.
     * 
     * @param request the request
     * 
     * @return the request priority
     */
    protected int getRequestPriority() {

        short priority = getQueueServerManagement().getDefaultPriority();

        LiteralInput priorityParam = (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_PRIORITY);
        if (MotuWPSProcess.isNullOrEmpty(priorityParam)) {
            return priority;
        }

        try {
            priority = Short.valueOf(priorityParam.getValue());
        } catch (NumberFormatException e) {
            priority = getQueueServerManagement().getDefaultPriority();
        }

        return priority;
    }
    
    protected List<String> getVariables() {
        List<ProcessletInput> variables = processletInputs.getParameters(PARAM_VARIABLE);

        List<String> listVar = new ArrayList<String>();
        if (variables != null) {
            for (ProcessletInput v : variables) {
                if (v instanceof LiteralInput) {
                    LiteralInput var = (LiteralInput) v;
                    listVar.add(var.getValue());
                }
            }
        }
        // List<String> listVar = null;
        // if (variables != null) {
        // if (variables.length > 0) {
        // listVar = Arrays.asList(variables);
        // }
        // }
        return listVar;

    }


    
}
