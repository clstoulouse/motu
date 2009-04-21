package fr.cls.atoll.motu.processor.wps;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.BoundingBoxInput;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.input.LiteralInput;
import org.deegree.services.wps.input.ProcessletInput;
import org.deegree.services.wps.output.ComplexOutput;
import org.deegree.services.wps.output.LiteralOutput;

import fr.cls.atoll.motu.library.configuration.MotuConfig;
import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.atoll.motu.library.intfce.SimpleAuthenticator;
import fr.cls.atoll.motu.library.queueserver.QueueServerManagement;
import fr.cls.atoll.motu.library.queueserver.RequestManagement;
import fr.cls.atoll.motu.msg.xml.ErrorType;
import fr.cls.atoll.motu.msg.xml.StatusModeResponse;
import fr.cls.atoll.motu.msg.xml.StatusModeType;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.9 $ - $Date: 2009-04-21 14:51:45 $
 */
public abstract class MotuWPSProcess implements Processlet {

    /** The Constant LOG. */
    private static final Logger LOG = Logger.getLogger(MotuWPSProcess.class);

    /** The Constant PARAM_FORWARDED_FOR (Real user Ip). */
    public static final String PARAM_ANONYMOUS = "anonymous";

    /** The Constant PARAM_ANONYMOUS_USER_VALUE. */
    public static final String PARAM_ANONYMOUS_USER_VALUE = "isanonymous";

    /** The Constant PARAM_BATCH. */
    public static final String PARAM_BATCH = "batch";

    /** Process output return code parameter name. */
    public static final String PARAM_CODE = "code";

    /** The Constant PARAM_DATAFORMAT. */
    public static final String PARAM_DATAFORMAT = "dataformat";

    /** The Constant PARAM_ENDTIME. */
    public static final String PARAM_ENDTIME = "endtime";

    /** Low latitude servlet paremeter name. */
    public static final String PARAM_GEOBBOX = "geobbox";

    /** The Constant PARAM_HIGHDEPTH. */
    public static final String PARAM_HIGHDEPTH = "highdepth";

    /** Language servlet paremeter name. */
    public static final String PARAM_LANGUAGE = "lang";

    /** Login parameter name. */
    public static final String PARAM_LOGIN = "login";

    /** The Constant PARAM_LOWDEPTH. */
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

    public static final String PARAM_REQUESTID = "requestid";

    /** Service servlet parameter name. */
    public static final String PARAM_SERVICE = "service";

    /** The Constant PARAM_STARTTIME. */
    public static final String PARAM_STARTTIME = "starttime";

    public static final String PARAM_STATUS = "status";

    /** Url parameter name. */
    public static final String PARAM_URL = "url";

    /** Variable servlet paremeter name. */
    public static final String PARAM_VARIABLE = "variable";

    /** The Constant PARAM_MAX_POOL_ANONYMOUS. */
    public static final String PARAM_MAX_POOL_ANONYMOUS = "maxpoolanonymous";

    /** The Constant PARAM_MAX_POOL_AUTHENTICATE. */
    public static final String PARAM_MAX_POOL_AUTHENTICATE = "maxpoolauth";

    /** The processlet inputs. */
    protected ProcessletInputs processletInputs;

    /** The processlet outputs. */
    protected ProcessletOutputs processletOutputs;

    /** The processlet execution info. */
    protected ProcessletExecutionInfo processletExecutionInfo;

    /** The request management. */
    protected RequestManagement requestManagement = null;

    /**
     * Valeur de requestManagement.
     * 
     * @return la valeur.
     */
    public RequestManagement getRequestManagement() {
        return requestManagement;
    }

    /** The service name param. */
    LiteralInput serviceNameParam = null;

    /** The location data param. */
    LiteralInput locationDataParam = null;

    /** The product id param. */
    LiteralInput productIdParam = null;

    String serviceName = null;
    String locationData = null;
    String productId = null;

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

    /**
     * Sets the language parameter.
     * 
     * @param in the new language parameter
     * 
     * @throws ProcessletException the processlet exception
     */
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
            // Initialisation Queue Server
            initRequestManagement();

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
     * Inits the request management.
     */
    private void initRequestManagement() throws ProcessletException {
        try {

            requestManagement = RequestManagement.getInstance();
            if (requestManagement.isShutdown()) {
                throw new ProcessletException("ERROR while request management initialization: request management is shutdown");
            }
        } catch (MotuException e) {
            throw new ProcessletException(String.format("ERROR while request management initialization.\n%s", e.notifyException()));
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

    /**
     * Sets the return code.
     * 
     * @param code the code
     * @param msg the msg
     */
    protected void setReturnCode(ErrorType code, String msg) {

        MotuWPSProcess.setReturnCode(this.processletOutputs, code, msg);

    }

    protected void setReturnCode(String msg) {

        MotuWPSProcess.setReturnCode(this.processletOutputs, msg);

    }

    /**
     * Sets the return code.
     * 
     * @param e the new return code
     */
    protected void setReturnCode(Exception e) {

        MotuWPSProcess.setReturnCode(this.processletOutputs, ErrorType.SYSTEM, e);

    }

    /**
     * Sets the return code.
     * 
     * @param e the new return code
     */
    protected void setReturnCode(MotuExceptionBase e) {

        MotuWPSProcess.setReturnCode(this.processletOutputs, ErrorType.SYSTEM, e);

    }

    /**
     * Sets the return code.
     * 
     * @param code the code
     * @param e the e
     */
    protected void setReturnCode(ErrorType code, MotuExceptionBase e) {

        MotuWPSProcess.setReturnCode(this.processletOutputs, code, e.notifyException());

    }

    /**
     * Sets the return code.
     * 
     * @param code the code
     * @param e the e
     */
    protected void setReturnCode(ErrorType code, Exception e) {

        MotuWPSProcess.setReturnCode(this.processletOutputs, code, e.getMessage());

    }
    
    public void setReturnCode(StatusModeResponse statusModeResponse) {

        MotuWPSProcess.setReturnCode(processletOutputs, statusModeResponse.getCode(), statusModeResponse.getMsg());

    }
    public static void setReturnCode(ProcessletOutputs response, StatusModeResponse statusModeResponse) {

        MotuWPSProcess.setReturnCode(response, statusModeResponse.getCode(), statusModeResponse.getMsg());

    }
    
    /**
     * Checks if is status done.
     * 
     * @param statusModeResponse the status mode response
     * 
     * @return true, if is status done
     */
    public static boolean isStatusDone(StatusModeResponse statusModeResponse) {
        if (statusModeResponse == null) {
            return false;
        }
        return (statusModeResponse.getStatus().compareTo(StatusModeType.DONE) == 0);
    }

    /**
     * Checks if is status in progress.
     * 
     * @param statusModeResponse the status mode response
     * 
     * @return true, if is status in progress
     */
    public static boolean isStatusInProgress(StatusModeResponse statusModeResponse) {
        if (statusModeResponse == null) {
            return false;
        }
        return (statusModeResponse.getStatus().compareTo(StatusModeType.INPROGRESS) == 0);
    }

    /**
     * Checks if is status in pending.
     * 
     * @param statusModeResponse the status mode response
     * 
     * @return true, if is status in pending
     */
    public static boolean isStatusPending(StatusModeResponse statusModeResponse) {
        if (statusModeResponse == null) {
            return false;
        }
        return (statusModeResponse.getStatus().compareTo(StatusModeType.PENDING) == 0);
    }

    /**
     * Checks if is status error.
     * 
     * @param statusModeResponse the status mode response
     * 
     * @return true, if is status error
     */
    public static boolean isStatusError(StatusModeResponse statusModeResponse) {
        if (statusModeResponse == null) {
            return false;
        }
        return (statusModeResponse.getStatus().compareTo(StatusModeType.ERROR) == 0);
    }

    /**
     * Checks if is status done or error.
     * 
     * @param statusModeResponse the status mode response
     * 
     * @return true, if is status done or error
     */
    public static boolean isStatusDoneOrError(StatusModeResponse statusModeResponse) {
        if (statusModeResponse == null) {
            return false;
        }
        return ((statusModeResponse.getStatus().compareTo(StatusModeType.DONE) == 0) || (statusModeResponse.getStatus()
                .compareTo(StatusModeType.ERROR) == 0));
    }

    /**
     * Checks if is status pending or in progress.
     * 
     * @param statusModeResponse the status mode response
     * 
     * @return true, if is status pending or in progress
     */
    public static boolean isStatusPendingOrInProgress(StatusModeResponse statusModeResponse) {
        if (statusModeResponse == null) {
            return false;
        }
        return ((statusModeResponse.getStatus().compareTo(StatusModeType.PENDING) == 0) || (statusModeResponse.getStatus()
                .compareTo(StatusModeType.INPROGRESS) == 0));
    }


    /**
     * Sets the return code.
     * 
     * @param response the response
     * @param e the e
     */
    public static void setReturnCode(ProcessletOutputs response, MotuExceptionBase e) {

        MotuWPSProcess.setReturnCode(response, ErrorType.SYSTEM, e);

    }

    /**
     * Sets the return code.
     * 
     * @param response the response
     * @param e the e
     */
    public static void setReturnCode(ProcessletOutputs response, Exception e) {

        MotuWPSProcess.setReturnCode(response, ErrorType.SYSTEM, e);

    }

    public static void setReturnCode(ProcessletOutputs response, String msg) {

        MotuWPSProcess.setReturnCode(response, ErrorType.SYSTEM, msg);

    }

    /**
     * Sets the return code.
     * 
     * @param response the response
     * @param code the code
     * @param msg the msg
     */
    public static void setReturnCode(ProcessletOutputs response, ErrorType code, String msg) {

        setReturnCode(response, code.toString(), msg);
    }

    public static void setReturnCode(ProcessletOutputs response, String code, String msg) {
        if (response == null) {
            return;
        }

        LiteralOutput codeParam = (LiteralOutput) response.getParameter(MotuWPSProcess.PARAM_CODE);
        LiteralOutput msgParam = (LiteralOutput) response.getParameter(MotuWPSProcess.PARAM_MESSAGE);

        if ((codeParam != null) && (code != null)) {
            codeParam.setValue(code);
        }
        if ((msgParam != null) && (msg != null)) {
            msgParam.setValue(msg);
        }

    }

    /**
     * Sets the return code.
     * 
     * @param code the code
     * @param e the e
     * @param response the response
     */

    public static void setReturnCode(ProcessletOutputs response, ErrorType code, Exception e) {

        setReturnCode(response, code, e.getMessage());

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

    /**
     * Gets the product info parameters.
     * 
     * @return the product info parameters
     * 
     * @throws ProcessletException the processlet exception
     */
    protected void getProductInfoParameters() throws ProcessletException {

        serviceName = null;
        locationData = null;
        productId = null;

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

        if (serviceNameParam != null) {
            serviceName = serviceNameParam.getValue();
        }
        if (locationDataParam != null) {
            locationData = locationDataParam.getValue();
        }
        if (productIdParam != null) {
            productId = productIdParam.getValue();
        }
    }

    /**
     * Gets the temporal coverage from the request.
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

    /**
     * Gets the request id as long.
     * 
     * @return the request id as long
     * 
     * @throws MotuException the motu exception
     */
    public long getRequestIdAsLong() throws MotuException {
        return getComplexInputValueasLongFromBinaryStream(MotuWPSProcess.PARAM_REQUESTID);
    }
    
    
    /**
     * Gets the complex input valueas long from binary stream.
     * 
     * @param complexInput the complex input
     * 
     * @return the complex input valueas long from binary stream
     * 
     * @throws MotuException the motu exception
     */
    public long getComplexInputValueasLongFromBinaryStream(String complexInputName) throws MotuException {
        String value = getComplexInputValueFromBinaryStream(complexInputName);
        if (value == null) {
            return Long.MAX_VALUE;
        }
        
        return Long.parseLong(value);
    }
    
    public static long getComplexInputValueasLongFromBinaryStream(ComplexInput complexInput) throws MotuException {
        String value = getComplexInputValueFromBinaryStream(complexInput);
        if (value == null) {
            return Long.MAX_VALUE;
        }
        
        return Long.parseLong(value);
    }
    /**
     * Gets the complex input value from binary stream.
     * 
     * @param complexInputName the complex input name
     * 
     * @return the complex input value from binary stream
     * 
     * @throws MotuException the motu exception
     */
    public String getComplexInputValueFromBinaryStream(String complexInputName) throws MotuException {
        ComplexInput complexInput = (ComplexInput) processletInputs.getParameter(MotuWPSProcess.PARAM_REQUESTID);
        if (complexInput == null) {
            return null;
        }
        return MotuWPSProcess.getComplexInputValueFromBinaryStream(complexInput);

        
    }
    /**
     * Gets the complex input value from binary stream.
     * 
     * @param complexInput the complex input
     * 
     * @return the complex input value from binary stream
     * 
     * @throws MotuException the motu exception
     */
    public static String getComplexInputValueFromBinaryStream(ComplexInput complexInput) throws MotuException {
        String value = null;
        try {

            byte[] buffer = new byte[1024];

            InputStream is = complexInput.getValueAsBinaryStream();
            StringBuffer stringBuffer = new StringBuffer();
            int bytesRead = 0;
            while ((bytesRead = is.read(buffer)) != -1) {
                stringBuffer.append(new String(buffer, 0, bytesRead));
            }
            value = stringBuffer.toString();

        } catch (Exception e) {
            throw new MotuException(String.format("Error in MotuWPSProcess#getComplexInputValueFromBinaryStream (ComplexInput identifier:%s.", complexInput.getIdentifier()), e);
        }

        return value;
    }

    /**
     * Gets the data format.
     * 
     * @return the data format
     */
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

    // /**
    // * Gets the mode parameter from the request.
    // *
    // * @return the status mode
    // */
    // protected String getMode() {
    // // -------------------------------------------------
    // // Gets Depth coverage
    // // -------------------------------------------------
    // LiteralInput modeParam = (LiteralInput) processletInputs.getParameter(MotuWPSProcess.PARAM_MODE);
    // if (modeParam == null) {
    // return "";
    // }
    // return modeParam.getValue();
    // }

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

    /**
     * Gets the variables.
     * 
     * @return the variables
     */
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
