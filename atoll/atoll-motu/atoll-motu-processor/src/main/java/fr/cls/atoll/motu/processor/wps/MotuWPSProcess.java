package fr.cls.atoll.motu.processor.wps;

import java.io.IOException;
import java.io.InputStream;
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
import org.deegree.services.wps.input.ReferencedComplexInput;
import org.deegree.services.wps.output.ComplexOutput;
import org.deegree.services.wps.output.LiteralOutput;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.intfce.Organizer;
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
 * @version $Revision: 1.16 $ - $Date: 2009-06-03 11:44:23 $
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
    
    /** The Constant PARAM_MAX_ALLOWED_SIZE. */
    public static final String PARAM_MAX_ALLOWED_SIZE = "maxAllowedSize";
    
    /** The Constant PARAM_MAX_POOL_ANONYMOUS. */
    public static final String PARAM_MAX_POOL_ANONYMOUS = "maxpoolanonymous";

    /** The Constant PARAM_MAX_POOL_AUTHENTICATE. */
    public static final String PARAM_MAX_POOL_AUTHENTICATE = "maxpoolauth";

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

    /** The Constant PARAM_REQUESTID. */
    public static final String PARAM_REQUESTID = "requestid";

    /** Service servlet parameter name. */
    public static final String PARAM_SERVICE = "service";

    /** The Constant PARAM_STARTTIME. */
    public static final String PARAM_STARTTIME = "starttime";

    /** The Constant PARAM_SIZE. */
    public static final String PARAM_SIZE = "size";
    
    /** The Constant PARAM_STATUS. */
    public static final String PARAM_STATUS = "status";

    /** Url parameter name. */
    public static final String PARAM_URL = "url";

    /** Variable servlet paremeter name. */
    public static final String PARAM_VARIABLE = "variable";


    // protected RunnableWPS runnableWPS = null;

    /**
     * Valeur de requestManagement.
     * 
     * @return la valeur.
     */
    public RequestManagement getRequestManagement() {
        try {
            return RequestManagement.getInstance();
        } catch (MotuException e) {
            return null;
        }
    }

    /**
     * Gets the wPS request management.
     * 
     * @return the wPS request management
     */
    public WPSRequestManagement getWPSRequestManagement() {
        try {
            return WPSRequestManagement.getInstance();
        } catch (MotuException e) {
            return null;
        }
    }

    /**
     * Gets the queue server management.
     * 
     * @return the queue server management
     */
    protected QueueServerManagement getQueueServerManagement() {
        return getRequestManagement().getQueueServerManagement();

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

        Organizer organizer = getOrganizer(in);
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

    }

    /** {@inheritDoc} */
    @Override
    public void init() {
    }

    /** {@inheritDoc} */
    @Override
    public void process(ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info) throws ProcessletException {

        MotuWPSProcessData motuWPSProcessData = new MotuWPSProcessData();

        motuWPSProcessData.setProcessletInputs(in);
        motuWPSProcessData.setProcessletOutputs(out);
        motuWPSProcessData.setProcessletExecutionInfo(info);

        motuWPSProcessData.setRequestId(getRequestManagement().generateRequestId());

        getWPSRequestManagement().putIfAbsentMotuWPSProcessData(in, motuWPSProcessData);

    }

    /**
     * Before process.
     * 
     * @param in the in
     * @param out the out
     * @param info the info
     * 
     * @throws ProcessletException the processlet exception
     */
    public void beforeProcess(ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info) throws ProcessletException {
    }

    /**
     * After process.
     * 
     * @param in the in
     * @param out the out
     * @param info the info
     * 
     * @throws ProcessletException the processlet exception
     */
    public void afterProcess(ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info) throws ProcessletException {
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

        return MotuWPSProcess.isNullOrEmpty(value.getValue());
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
     * @param throwProcessletException the throw processlet exception
     * 
     * @throws ProcessletException the processlet exception
     */
    public static void setReturnCode(ProcessletOutputs response, MotuExceptionBase e, boolean throwProcessletException) throws ProcessletException {

        MotuWPSProcess.setReturnCode(response, ErrorType.SYSTEM, e, throwProcessletException);

    }

    /**
     * Sets the return code.
     * 
     * @param response the response
     * @param e the e
     * @param throwProcessletException the throw processlet exception
     * 
     * @throws ProcessletException the processlet exception
     */
    public static void setReturnCode(ProcessletOutputs response, Exception e, boolean throwProcessletException) throws ProcessletException {

        MotuWPSProcess.setReturnCode(response, ErrorType.SYSTEM, e, throwProcessletException);

    }

    /**
     * Sets the return code.
     * 
     * @param response the response
     * @param msg the msg
     * @param throwProcessletException the throw processlet exception
     * 
     * @throws ProcessletException the processlet exception
     */
    public static void setReturnCode(ProcessletOutputs response, String msg, boolean throwProcessletException) throws ProcessletException {

        MotuWPSProcess.setReturnCode(response, ErrorType.SYSTEM, msg, throwProcessletException);

    }

    /**
     * Sets the return code.
     * 
     * @param response the response
     * @param code the code
     * @param msg the msg
     * @param throwProcessletException the throw processlet exception
     * 
     * @throws ProcessletException the processlet exception
     */
    public static void setReturnCode(ProcessletOutputs response, ErrorType code, String msg, boolean throwProcessletException)
            throws ProcessletException {

        MotuWPSProcess.setReturnCode(response, code.toString(), msg, throwProcessletException);
    }

    /**
     * Sets the return code.
     * 
     * @param response the response
     * @param code the code
     * @param msg the msg
     * @param throwProcessletException the throw processlet exception
     * 
     * @throws ProcessletException the processlet exception
     */
    public static void setReturnCode(ProcessletOutputs response, String code, String msg, boolean throwProcessletException)
            throws ProcessletException {
        synchronized (response) {
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
        ErrorType errorType = ErrorType.fromValue(code);
        if (throwProcessletException && (errorType != ErrorType.OK)) {
            throw new ProcessletException(String.format("ERROR - Code: %s, Message: %s", code, msg));
        }

    }

    /**
     * Sets the return code.
     * 
     * @param response the response
     * @param statusModeResponse the status mode response
     * @param throwProcessletException the throw processlet exception
     * 
     * @throws ProcessletException the processlet exception
     */
    public static void setReturnCode(ProcessletOutputs response, StatusModeResponse statusModeResponse, boolean throwProcessletException)
            throws ProcessletException {

        MotuWPSProcess.setReturnCode(response, statusModeResponse.getCode(), statusModeResponse.getMsg(), throwProcessletException);

    }

    /**
     * Sets the return code.
     * 
     * @param code the code
     * @param e the e
     * @param response the response
     * @param throwProcessletException the throw processlet exception
     * 
     * @throws ProcessletException the processlet exception
     */

    public static void setReturnCode(ProcessletOutputs response, ErrorType code, MotuExceptionBase e, boolean throwProcessletException)
            throws ProcessletException {

        setReturnCode(response, code, e.notifyException(), throwProcessletException);

    }

    /**
     * Sets the return code.
     * 
     * @param response the response
     * @param code the code
     * @param e the e
     * @param throwProcessletException the throw processlet exception
     * 
     * @throws ProcessletException the processlet exception
     */
    public static void setReturnCode(ProcessletOutputs response, ErrorType code, Exception e, boolean throwProcessletException)
            throws ProcessletException {

        setReturnCode(response, code, e.getMessage(), throwProcessletException);

    }

    /**
     * Sets the resquest id.
     * 
     * @param response the response
     * @param statusModeResponse the status mode response
     * 
     * @throws MotuException the motu exception
     */
    public static void setUrl(ProcessletOutputs response, StatusModeResponse statusModeResponse) throws MotuException {

        if (response == null) {
            return;
        }
        if (statusModeResponse == null) {
            return;
        }

        MotuWPSProcess.setUrl(response, statusModeResponse.getMsg());

    }

    /**
     * Sets the url.
     * 
     * @param response the response
     * @param url the url
     * 
     * @throws MotuException the motu exception
     */
    public static void setUrl(ProcessletOutputs response, String url) throws MotuException {

        synchronized (response) {

            if (response == null) {
                return;
            }
            ComplexOutput urlParam = (ComplexOutput) response.getParameter(MotuWPSProcess.PARAM_URL);

            if ((urlParam == null) || (url == null)) {
                return;
            }

            try {
                urlParam.getBinaryOutputStream().write(url.getBytes());
            } catch (IOException e) {
                throw new MotuException("ERROR MotuWPSProcess#setUrl", e);
            }
        }

    }

    /**
     * Create new Organizer object.
     * 
     * @param in the in
     * 
     * @return Organizer object.
     * 
     * @throws ProcessletException the processlet exception
     */
    protected Organizer getOrganizer(ProcessletInputs in) throws ProcessletException {
        MotuWPSProcessData motuWPSProcessData = getMotuWPSProcessData(in);

        Organizer organizer = null;
        try {
            organizer = new Organizer();
        } catch (MotuExceptionBase e) {
            String msg = String.format("ERROR: - MotuWPSProcess.getOrganizer - Unable to create a new organiser. Native Error: %s", e
                    .notifyException());
            setReturnCode(motuWPSProcessData.getProcessletOutputs(), ErrorType.SYSTEM, msg, false);
        }

        return organizer;
    }

    /**
     * Gets the motu wps process data.
     * 
     * @param in the in
     * 
     * @return the motu wps process data
     * 
     * @throws ProcessletException the processlet exception
     */
    protected MotuWPSProcessData getMotuWPSProcessData(ProcessletInputs in) throws ProcessletException {
        MotuWPSProcessData motuWPSProcessData = getWPSRequestManagement().getMotuWPSProcessData(in);
        if (motuWPSProcessData == null) {
            throw new ProcessletException("Error - MotuWPSProcess#getMotuWPSProcessData - Unable to find process data");
        }
        return motuWPSProcessData;
    }

    /**
     * Gets the product info parameters.
     * 
     * @param in the in
     * 
     * @return the product info parameters
     * 
     * @throws ProcessletException the processlet exception
     */
    protected MotuWPSProcessData getProductInfoParameters(ProcessletInputs in) throws ProcessletException {
        MotuWPSProcessData motuWPSProcessData = getMotuWPSProcessData(in);

        String serviceName = null;
        String locationData = null;
        String productId = null;

        LiteralInput serviceNameParam = (LiteralInput) motuWPSProcessData.getServiceNameParamIn();
        LiteralInput locationDataParam = (LiteralInput) motuWPSProcessData.getLocationDataParamIn();
        LiteralInput productIdParam = (LiteralInput) motuWPSProcessData.getProductIdParamIn();

        if (MotuWPSProcess.isNullOrEmpty(locationDataParam) && MotuWPSProcess.isNullOrEmpty(productIdParam)) {
            if (LOG.isDebugEnabled()) {
                LOG.info(" empty locationData and empty productId");
                LOG.debug("END MotuWPSProcess.getProductInfo()");
            }

            String msg = String.format("ERROR: neither '%s' nor '%s' parameters are filled - Choose one of them",
                                       MotuWPSProcess.PARAM_URL,
                                       PARAM_PRODUCT);

            setReturnCode(motuWPSProcessData.getProcessletOutputs(), ErrorType.INCONSISTENCY, msg, true);
            return null;
            // throw new ProcessletException(msg);
        }

        if (!MotuWPSProcess.isNullOrEmpty(locationDataParam) && !MotuWPSProcess.isNullOrEmpty(productIdParam)) {
            if (LOG.isDebugEnabled()) {
                LOG.info(" non empty locationData and non empty productId");
                LOG.debug("END MotuWPSProcess.process()");
            }
            String msg = String.format("ERROR: '%s' and '%s' parameters are not compatible - Choose only one of them",
                                       MotuWPSProcess.PARAM_URL,
                                       MotuWPSProcess.PARAM_PRODUCT);

            setReturnCode(motuWPSProcessData.getProcessletOutputs(), ErrorType.INCONSISTENCY, msg, true);
            return null;
            // throw new ProcessletException(msg);
        }

        if (MotuWPSProcess.isNullOrEmpty(serviceNameParam) && !MotuWPSProcess.isNullOrEmpty(productIdParam)) {
            if (LOG.isDebugEnabled()) {
                LOG.info("empty serviceName  and non empty productId");
                LOG.debug("END MotuWPSProcess.process()");
            }
            String msg = String.format("ERROR: '%s' parameter is filled but '%s' is empty. You have to fill it.", PARAM_PRODUCT, PARAM_SERVICE);

            setReturnCode(motuWPSProcessData.getProcessletOutputs(), ErrorType.INCONSISTENCY, msg, true);
            return null;
            // throw new ProcessletException(msg);
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

        motuWPSProcessData.setServiceName(serviceName);
        motuWPSProcessData.setProductId(productId);
        motuWPSProcessData.setLocationData(locationData);

        return motuWPSProcessData;

    }

    /**
     * Gets the temporal coverage from the request.
     * 
     * @param in the in
     * 
     * @return a list of temporable coverage, first start date, and then end date (they can be empty string)
     * 
     * @throws ProcessletException the processlet exception
     */
    protected List<String> getTemporalCoverage(ProcessletInputs in) throws ProcessletException {
        MotuWPSProcessData motuWPSProcessData = getMotuWPSProcessData(in);

        LiteralInput startDateParam = motuWPSProcessData.getStartDateParamIn();
        LiteralInput endDateParam = motuWPSProcessData.getEndDateParamIn();
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
     * @param in the in
     * 
     * @return the request id as long
     * 
     * @throws MotuException the motu exception
     * @throws ProcessletException the processlet exception
     */
    public long getRequestIdAsLong(ProcessletInputs in) throws MotuException, ProcessletException {
        MotuWPSProcessData motuWPSProcessData = getMotuWPSProcessData(in);

        long requestId = -1;
        try {
            requestId = MotuWPSProcess.getComplexInputValueAsLongFromBinaryStream(motuWPSProcessData.getRequestIdParamIn());
        } catch (MotuExceptionBase e) {
            if (motuWPSProcessData.getRequestIdParamIn() instanceof ReferencedComplexInput) {
                throw new ProcessletException(e.notifyException());
            }
        }

        return requestId;
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
    public static long getComplexInputValueAsLongFromBinaryStream(ComplexInput complexInput) throws MotuException {
        String value = MotuWPSProcess.getComplexInputValueFromBinaryStream(complexInput);
        if (value == null) {
            return Long.MAX_VALUE;
        }

        return Long.parseLong(value);
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
            throw new MotuException(String.format("Error in MotuWPSProcess#getComplexInputValueFromBinaryStream (ComplexInput identifier:%s.",
                                                  complexInput.getIdentifier()), e);
        }

        return value;
    }

    /**
     * Gets the data format.
     * 
     * @param in the in
     * 
     * @return the data format
     * 
     * @throws ProcessletException the processlet exception
     */
    protected Organizer.Format getDataFormat(ProcessletInputs in) throws ProcessletException {

        MotuWPSProcessData motuWPSProcessData = getMotuWPSProcessData(in);
        Organizer.Format dataFormat = Organizer.Format.getDefault();

        LiteralInput dataFormatParam = motuWPSProcessData.getDataFormatParamIn();
        if (dataFormatParam == null) {
            return dataFormat;
        }

        try {
            dataFormat = Organizer.Format.valueOf(dataFormatParam.getValue());
        } catch (Exception e) {
            setReturnCode(motuWPSProcessData.getProcessletOutputs(), e, false);
        }
        return dataFormat;
    }

    /**
     * Gets the geographical coverage from the request.
     * 
     * @param in the in
     * 
     * @return a list of geographical coverage : Lat min, Lon min, Lat max, Lon max
     * 
     * @throws ProcessletException the processlet exception
     */
    protected List<String> getGeoCoverage(ProcessletInputs in) throws ProcessletException {

        MotuWPSProcessData motuWPSProcessData = getMotuWPSProcessData(in);

        BoundingBoxInput geobboxParam = (BoundingBoxInput) motuWPSProcessData.getGeobboxParamIn();
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
     * @param in the in
     * 
     * @return a list of deph coverage : first depth min, then depth max
     * 
     * @throws ProcessletException the processlet exception
     */
    protected List<String> getDepthCoverage(ProcessletInputs in) throws ProcessletException {

        MotuWPSProcessData motuWPSProcessData = getMotuWPSProcessData(in);

        // -------------------------------------------------
        // Gets Depth coverage
        // -------------------------------------------------
        LiteralInput lowDepthParam = motuWPSProcessData.getLowDepthParamIn();
        LiteralInput highDepthParam = motuWPSProcessData.getHighDepthParamIn();
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
     * @param in the in
     * 
     * @return true, if is anonymous user
     * 
     * @throws ProcessletException the processlet exception
     */
    protected boolean isAnonymousUser(ProcessletInputs in, String userId) throws ProcessletException {
        if (MotuWPSProcess.isNullOrEmpty(userId)) {
            return true;
        }
        boolean anonymousUser = userId.equalsIgnoreCase(MotuWPSProcess.PARAM_ANONYMOUS_USER_VALUE);
        if (anonymousUser) {
            return true;
        }

        MotuWPSProcessData motuWPSProcessData = getMotuWPSProcessData(in);

        LiteralInput anonymousUserAsStringParam = (LiteralInput) motuWPSProcessData.getAnonymousUserAsStringParamIn();
        if (anonymousUserAsStringParam == null) {
            return false;
        }
        String anonymousUserAsString = anonymousUserAsStringParam.getValue().trim();
        return anonymousUserAsString.equalsIgnoreCase("true") || anonymousUserAsString.equalsIgnoreCase("1");
    }

    /**
     * Checks if is batch.
     * 
     * @param in the in
     * 
     * @return true, if is batch
     * 
     * @throws ProcessletException the processlet exception
     */
    protected boolean isBatch(ProcessletInputs in) throws ProcessletException {

        MotuWPSProcessData motuWPSProcessData = getMotuWPSProcessData(in);

        LiteralInput batchAsStringParam = (LiteralInput) motuWPSProcessData.getIsBatchParamIn();
        if (MotuWPSProcess.isNullOrEmpty(batchAsStringParam)) {
            return false;
        }
        String batchAsString = batchAsStringParam.getValue().trim();
        return batchAsString.equalsIgnoreCase("true") || batchAsString.equalsIgnoreCase("1");
    }

    /**
     * Gets the login.
     * 
     * @param in the in
     * 
     * @return the login
     * 
     * @throws ProcessletException the processlet exception
     */
    protected String getLogin(ProcessletInputs in) throws ProcessletException {
        String login = null;

        MotuWPSProcessData motuWPSProcessData = getMotuWPSProcessData(in);

        LiteralInput loginParam = (LiteralInput) motuWPSProcessData.getLoginParamIn();
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

    /**
     * Gets the request priority.
     * 
     * @param in the in
     * 
     * @return the request priority
     * 
     * @throws ProcessletException the processlet exception
     */
    protected int getRequestPriority(ProcessletInputs in) throws ProcessletException {

        short priority = getQueueServerManagement().getDefaultPriority();

        MotuWPSProcessData motuWPSProcessData = getMotuWPSProcessData(in);

        LiteralInput priorityParam = (LiteralInput) motuWPSProcessData.getPriorityParamIn();
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
     * @param in the in
     * 
     * @return the variables
     * 
     * @throws ProcessletException the processlet exception
     */
    protected List<String> getVariables(ProcessletInputs in) throws ProcessletException {

        MotuWPSProcessData motuWPSProcessData = getMotuWPSProcessData(in);

        List<ProcessletInput> variables = motuWPSProcessData.getVariablesParamIn();

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

    /**
     * Wait for response.
     * 
     * @param in the in
     * @param requestId the request id
     * 
     * @return the status mode response
     */
    protected StatusModeResponse waitForResponse(ComplexInput in, long requestId) {
        StatusModeResponse statusModeResponse = getRequestManagement().getResquestStatusMap(requestId);

        if (statusModeResponse == null) {
            return statusModeResponse;
        }

        // If Request id parameter is getting by another WPS process,
        // wait the end of this WPS process while status is PENDING or INPROGRESS.
        if (in instanceof ReferencedComplexInput) {
            while (MotuWPSProcess.isStatusPendingOrInProgress(statusModeResponse)) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        return statusModeResponse;

    }

    /**
     * Sets the status.
     * 
     * @param in the in
     * @param status the status
     * 
     * @throws MotuException the motu exception
     * @throws ProcessletException the processlet exception
     */
    public void setStatus(ProcessletInputs in, StatusModeType status) throws MotuException, ProcessletException {
        MotuWPSProcessData motuWPSProcessData = getMotuWPSProcessData(in);

        MotuWPSProcess.setStatus(motuWPSProcessData.getProcessletOutputs(), status);

    }

    /**
     * Sets the status.
     * 
     * @param in the in
     * @param status the status
     * 
     * @throws MotuException the motu exception
     * @throws ProcessletException the processlet exception
     */
    public void setStatus(ProcessletInputs in, String status) throws MotuException, ProcessletException {
        MotuWPSProcessData motuWPSProcessData = getMotuWPSProcessData(in);
        MotuWPSProcess.setStatus(motuWPSProcessData.getProcessletOutputs(), status);
    }

    /**
     * Sets the resquest id.
     * 
     * @param response the response
     * @param requestId the request id
     * 
     * @throws MotuException the motu exception
     */
    public static void setRequestId(ProcessletOutputs response, long requestId) throws MotuException {

        MotuWPSProcess.setRequestId(response, Long.toString(requestId));

    }

    /**
     * Sets the resquest id.
     * 
     * @param response the response
     * @param requestId the request id
     * 
     * @throws MotuException the motu exception
     */
    public static void setRequestId(ProcessletOutputs response, String requestId) throws MotuException {

        synchronized (response) {

            if (response == null) {
                return;
            }

            ComplexOutput requestIdParam = (ComplexOutput) response.getParameter(MotuWPSProcess.PARAM_REQUESTID);

            if ((requestIdParam == null) || (requestId == null)) {
                return;
            }

            try {
                requestIdParam.getBinaryOutputStream().write(requestId.getBytes());
            } catch (IOException e) {
                throw new MotuException("ERROR MotuWPSProcess#setResquestId", e);
            }
        }

    }

    /**
     * Sets the status.
     * 
     * @param response the response
     * @param status the status
     */
    public static void setStatus(ProcessletOutputs response, StatusModeType status) {
        MotuWPSProcess.setStatus(response, Integer.toString(status.value()));
        //MotuWPSProcess.setStatus(response, status.toString());

    }

    /**
     * Sets the status.
     * 
     * @param response the response
     * @param status the status
     */
    public static void setStatus(ProcessletOutputs response, String status) {
        synchronized (response) {

            if (response == null) {
                return;
            }
            LiteralOutput statusParam = (LiteralOutput) response.getParameter(MotuWPSProcess.PARAM_STATUS);

            if ((statusParam == null) || (status == null)) {
                return;
            }

            statusParam.setValue(status);
        }

    }
    
    

}
