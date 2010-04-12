/**
 * 
 */
package fr.cls.atoll.motu.web.servlet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.jasig.cas.client.util.AssertionHolder;

import fr.cls.atoll.motu.api.MotuRequestParametersConstant;
import fr.cls.atoll.motu.library.configuration.ConfigService;
import fr.cls.atoll.motu.library.configuration.MotuConfig;
import fr.cls.atoll.motu.library.configuration.QueueType;
import fr.cls.atoll.motu.library.data.Product;
import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.exception.MotuInvalidRequestIdException;
import fr.cls.atoll.motu.library.exception.MotuMarshallException;
import fr.cls.atoll.motu.library.intfce.ExtractionParameters;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.atoll.motu.library.queueserver.QueueManagement;
import fr.cls.atoll.motu.library.queueserver.QueueServerManagement;
import fr.cls.atoll.motu.library.queueserver.RequestManagement;
import fr.cls.atoll.motu.msg.MotuMsgConstant;
import fr.cls.atoll.motu.msg.xml.ErrorType;
import fr.cls.atoll.motu.msg.xml.ObjectFactory;
import fr.cls.atoll.motu.msg.xml.StatusModeResponse;
import fr.cls.atoll.motu.msg.xml.StatusModeType;
import fr.cls.commons.log.LogManager;
import fr.cls.commons.util.PropertiesUtilities;

// CSOFF: MultipleStringLiterals : avoid message in constants declaration and trace log.

/**
 * The Class MotuServlet.
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.4 $ - $Date: 2010-02-26 14:15:03 $
 */
public class MotuServlet extends HttpServlet implements MotuRequestParametersConstant {

    /*
     * Thread d'extraction différé de produit
     */
    /**
     * The Class ProductDeferedExtractNetcdfThread.
     */
    static private class ProductDeferedExtractNetcdfThread extends Thread {

        /** Logger for this class. */
        private static final Logger LOG = Logger.getLogger(ProductDeferedExtractNetcdfThread.class);

        /** The extraction parameters. */
        ExtractionParameters extractionParameters = null;

        /** The status mode response. */
        StatusModeResponse statusModeResponse = null;

        /** The organizer. */
        private Organizer organizer = null;

        /** The product defered extract netcdf status file path name. */
        private String productDeferedExtractNetcdfStatusFilePathName = null;

        // /** The location data. */
        // private String locationData;
        //
        // /** The list var. */
        // private List<String> listVar;
        //
        // /** The list temporal coverage. */
        // private List<String> listTemporalCoverage;
        //
        // /** The list lat lon coverage. */
        // private List<String> listLatLonCoverage;
        //
        // /** The list depth coverage. */
        // private List<String> listDepthCoverage;
        //
        // /** The select data. */
        // private SelectData selectData;
        //
        // /** The service name. */
        // private String serviceName;
        //
        // /** The product id. */
        // private String productId;

        // /**
        // * The Constructor.
        // *
        // * @param listVar the list var
        // * @param selectData the select data
        // * @param organizer the organizer
        // * @param locationData the location data
        // * @param productDeferedExtractNetcdfStatusFilePathName the product defered extract netcdf status
        // file
        // * path name
        // * @param listLatLonCoverage the list lat lon coverage
        // * @param listDepthCoverage the list depth coverage
        // * @param listTemporalCoverage the list temporal coverage
        // */
        // public ProductDeferedExtractNetcdfThread(
        // String productDeferedExtractNetcdfStatusFilePathName,
        // Organizer organizer,
        // String locationData,
        // List<String> listVar,
        // List<String> listTemporalCoverage,
        // List<String> listLatLonCoverage,
        // List<String> listDepthCoverage,
        // SelectData selectData) {
        // this.productDeferedExtractNetcdfStatusFilePathName = productDeferedExtractNetcdfStatusFilePathName;
        // this.organizer = organizer;
        // this.locationData = locationData;
        // this.listVar = listVar;
        // this.listTemporalCoverage = listTemporalCoverage;
        // this.listLatLonCoverage = listLatLonCoverage;
        // this.listDepthCoverage = listDepthCoverage;
        // this.selectData = selectData;
        // }
        //
        // /**
        // * The Constructor.
        // *
        // * @param listVar the list var
        // * @param selectData the select data
        // * @param organizer the organizer
        // * @param productDeferedExtractNetcdfStatusFilePathName the product defered extract netcdf status
        // file
        // * path name
        // * @param listLatLonCoverage the list lat lon coverage
        // * @param listDepthCoverage the list depth coverage
        // * @param listTemporalCoverage the list temporal coverage
        // * @param serviceName the service name
        // * @param productId the product id
        // */
        // public ProductDeferedExtractNetcdfThread(
        // String productDeferedExtractNetcdfStatusFilePathName,
        // Organizer organizer,
        // String serviceName,
        // List<String> listVar,
        // List<String> listTemporalCoverage,
        // List<String> listLatLonCoverage,
        // List<String> listDepthCoverage,
        // SelectData selectData,
        // String productId) {
        // this.productDeferedExtractNetcdfStatusFilePathName = productDeferedExtractNetcdfStatusFilePathName;
        // this.organizer = organizer;
        // this.serviceName = serviceName;
        // this.listVar = listVar;
        // this.listTemporalCoverage = listTemporalCoverage;
        // this.listLatLonCoverage = listLatLonCoverage;
        // this.listDepthCoverage = listDepthCoverage;
        // this.selectData = selectData;
        // this.productId = productId;
        // }

        /**
         * The Constructor.
         * 
         * @param organizer the organizer
         * @param extractionParameters the extraction parameters
         * @param statusModeResponse the status mode response
         */
        public ProductDeferedExtractNetcdfThread(StatusModeResponse statusModeResponse, Organizer organizer, ExtractionParameters extractionParameters) {

            this.statusModeResponse = statusModeResponse;
            this.productDeferedExtractNetcdfStatusFilePathName = null;
            this.organizer = organizer;
            this.extractionParameters = extractionParameters;

        }

        /**
         * The Constructor.
         * 
         * @param organizer the organizer
         * @param productDeferedExtractNetcdfStatusFilePathName the product defered extract netcdf status file
         *            path name
         * @param extractionParameters the extraction parameters
         * 
         */
        public ProductDeferedExtractNetcdfThread(
            String productDeferedExtractNetcdfStatusFilePathName,
            Organizer organizer,
            ExtractionParameters extractionParameters) {

            this.statusModeResponse = null;
            this.productDeferedExtractNetcdfStatusFilePathName = productDeferedExtractNetcdfStatusFilePathName;
            this.organizer = organizer;
            this.extractionParameters = extractionParameters;

        }

        /**
         * Gets the writer.
         * 
         * @return the writer
         * 
         * @throws IOException the IO exception
         */
        public Writer createWriter() throws IOException {
            return new FileWriter(productDeferedExtractNetcdfStatusFilePathName);
        }

        /**
         * Run.
         */
        @Override
        public void run() {

            execute();

            if (productDeferedExtractNetcdfStatusFilePathName == null) {
                return;
            }
            try {
                Organizer.marshallStatusModeResponse(statusModeResponse, createWriter());
            } catch (Exception e) {
                try {
                    Organizer.marshallStatusModeResponse(e, createWriter());
                } catch (MotuMarshallException e2) {
                    LOG.error("status writing error - " + e2.notifyException(), e2);
                } catch (Exception e2) {
                    LOG.error("status writing error - " + e2.getMessage(), e2);
                }
            }
        }

        // /**
        // * Sets the status done.
        // *
        // * @param msg the msg
        // */
        // private void setStatusDone(String msg) {
        // if (statusModeResponse == null) {
        // return;
        // }
        // statusModeResponse.setStatus(StatusModeType.DONE);
        // statusModeResponse.setMsg(msg);
        // statusModeResponse.setCode(ErrorType.OK);
        //
        // }

        /**
         * Execute.
         */
        private void execute() {
            Product product = null;
            // String downloadUrlPath = null;
            if (statusModeResponse == null) {
                statusModeResponse = Organizer.createStatusModeResponse();

            }

            try {
                product = organizer.extractData(extractionParameters);
                // downloadUrlPath = product.getDownloadUrlPath();
                // setStatusDone(downloadUrlPath);
                Organizer.setStatusDone(statusModeResponse, product);
            } catch (Exception e) {
                LOG.error("execute()", e);
                Organizer.setError(statusModeResponse, e);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("execute() - exiting");
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("execute() - exiting");
            }

        }

    }

    /** The Constant CONTENT_TYPE_PLAIN. */
    public static final String CONTENT_TYPE_PLAIN = "text/plain";

    /** The Constant CONTENT_TYPE_XML. */
    public static final String CONTENT_TYPE_XML = "text/xml";

    /** The Constant CONTENT_TYPE_HTML. */
    private static final String CONTENT_TYPE_HTML = "text/html";

    /** The jcontext. */
    private static JAXBContext jaxbContextMotuMsg = null;

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(MotuServlet.class);

    /** The Constant MSG_IN_PROGRESS. */
    private static final String MSG_IN_PROGRESS = "request in progress";

    /** The object factory. */
    private static ObjectFactory objectFactory = new ObjectFactory();

    /** The Constant ORGANIZER_SESSION_ATTR. */
    private static final String ORGANIZER_SESSION_ATTR = "organizer";

    /** The Constant PARAM_STATUS_AS_FILE. */
    private static final String PARAM_STATUS_AS_FILE = "statusAsFile";

    /** The Constant PARAM_USE_QUEUE_SERVER. */
    private static final String PARAM_USE_QUEUE_SERVER = "useQueueServer";

    /** The Constant proxyHeaders. */
    private static final List<String> PROXY_HEADERS = new ArrayList<String>();

    /** The Constant RESPONSE_ACTION_PING. */
    private static final String RESPONSE_ACTION_PING = "OK - response action=ping";

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -1L;

    // /** The queue server management. */
    // private QueueServerManagement queueServerManagement = null;

    /** The Constant SESSION_AUTHORIZED_KEY. */
    private static final String SESSION_AUTHORIZED_KEY = "sessionAuthorized";

    // /** The resquest status map. */
    // private ConcurrentMap<Long, StatusModeResponse> resquestStatusMap = new ConcurrentHashMap<Long,
    // StatusModeResponse>();

    /** The Constant SESSION_AUTHORIZED_USER. */
    private static final String SESSION_AUTHORIZED_USER = "sessionAuthorizedUser";

    static {
        PROXY_HEADERS.add("x-forwarded-for");
        PROXY_HEADERS.add("HTTP_X_FORWARDED_FOR");
        PROXY_HEADERS.add("HTTP_FORWARDED");
        PROXY_HEADERS.add("HTTP_CLIENT_IP");
    }
    /** The authentification props. */
    private Properties authentificationProps = null;

    /** The request management. */
    private RequestManagement requestManagement = null;

    /** The status as file. */
    private boolean statusAsFile = false;

    /**
     * Default constructor.
     */
    public MotuServlet() {
    }

    /**
     * Gets the ip by name.
     * 
     * @param ip the ip
     * 
     * @return the host name
     */
    public static String getHostName(String ip) {
        if (MotuServlet.isNullOrEmpty(ip)) {
            return ip;
        }

        StringBuffer stringBuffer = new StringBuffer();
        try {
            // if there are several ip, they can be seperate by ','.
            String[] ipSplit = ip.split(",");
            for (String ipString : ipSplit) {
                stringBuffer.append(InetAddress.getByName(ipString.trim()).getHostName());
                stringBuffer.append(", ");
            }
        } catch (UnknownHostException e) {
            // Do Nothing
        }
        stringBuffer.delete(stringBuffer.length() - 2, stringBuffer.length());
        return stringBuffer.toString();

    }

    /**
     * Test if a string is null or empty.
     * 
     * @param value string to be tested.
     * 
     * @return true if string is null or empty, otherwise false.
     */
    static protected boolean isNullOrEmpty(String value) {
        if (value == null) {
            return true;
        }
        if (value.equals("")) {
            return true;
        }
        return false;
    }

    /**
     * Ecriture du status d'extraction différé de produit dans un fichier XML.
     * 
     * @param errorType the error type
     * @param status status à ecrire
     * @param statusModeResponse the status mode response
     * @param writer the writer
     * @param msg message à ecrire
     */
    static private void printProductDeferedExtractNetcdfStatus(StatusModeResponse statusModeResponse,
                                                               Writer writer,
                                                               StatusModeType status,
                                                               String msg,
                                                               ErrorType errorType) {
        if (statusModeResponse == null) {
            statusModeResponse = Organizer.createStatusModeResponse();

        }

        try {

            statusModeResponse.setCode(errorType);
            statusModeResponse.setStatus(status);
            statusModeResponse.setMsg(msg);

            Organizer.marshallStatusModeResponse(statusModeResponse, writer);

        } catch (Exception e) {
            try {
                Organizer.marshallStatusModeResponse(e, writer);
            } catch (MotuMarshallException e2) {
                LOG.error("status writing error - " + e2.notifyException(), e2);
            }
        }

    }

    /**
     * Deduce service name from path.
     * 
     * @param request the request
     * 
     * @return the service name or empty string if not found
     * 
     * @throws MotuException the motu exception
     */
    public String deduceServiceNameFromPath(HttpServletRequest request) throws MotuException {
        String[] servletPathElts = request.getServletPath().split("/");
        String groupName = "";
        String serviceName = "";
        for (int i = 0; i < servletPathElts.length; i++) {
            if (!servletPathElts[i].equals("")) {
                groupName = servletPathElts[i];
                break;
            }
        }

        if (groupName.equals("")) {
            return "";
        }

        List<ConfigService> listConfServ = Organizer.getMotuConfigInstance().getConfigService();
        for (ConfigService confServ : listConfServ) {
            if (confServ.getGroup().equalsIgnoreCase(groupName) && confServ.isDefaultGroupService()) {
                serviceName = confServ.getName();
                break;
            }
        }
        if (serviceName.equals("")) {
            for (ConfigService confServ : listConfServ) {
                if (confServ.getGroup().equalsIgnoreCase(groupName)) {
                    serviceName = confServ.getName();
                    break;
                }
            }
        }

        return serviceName;
    }

    /**
     * Destroy.
     */
    @Override
    public void destroy() {
        try {
            Thread.sleep(500);
            if (requestManagement != null) {
                requestManagement.shutdown();
            }
        } catch (MotuException e) {
            // Do nothing
        } catch (InterruptedException e) {
            // Do nothing
        }

        super.destroy();
    }

    /**
     * {@inheritDoc}.
     * 
     * @param servletConfig the servlet config
     * 
     * @throws ServletException the servlet exception
     */
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        // Initialisation des logs
        LogManager.getInstance().loadConfiguration("log4j.xml");

        // Initialisation JAXB
        initJAXB();

        // Initialisation du proxy pour connection opendap
        initProxyLogin();

        // Initialisation de la liste des utilisateurs autorisés
        initAuthentification();

        String paramValue = getServletConfig().getInitParameter(PARAM_USE_QUEUE_SERVER);
        if (!MotuServlet.isNullOrEmpty(paramValue)) {
            RequestManagement.setUseQueueServer(Boolean.parseBoolean(paramValue));
        }

        paramValue = getServletConfig().getInitParameter(PARAM_STATUS_AS_FILE);
        if (!MotuServlet.isNullOrEmpty(paramValue)) {
            statusAsFile = Boolean.parseBoolean(paramValue);
        }

        // Initialisation Queue Server
        initRequestManagement();
    }

    /**
     * Sets the language if PARAM_LANGUAGE parameter is in the request list of parameters.
     * 
     * @param response the response
     * @param session request sesssion
     * @param request object that contains the request the client has made of the servlet.
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     */
    public void setLanguageParameter(HttpServletRequest request, HttpSession session, HttpServletResponse response) throws ServletException,
            IOException {

        if (session == null) {
            return;
        }

        String language = request.getParameter(PARAM_LANGUAGE);

        if (MotuServlet.isNullOrEmpty(language)) {
            return;
        }

        Organizer organizer = getOrganizer(session, response);
        try {
            organizer.setCurrentLanguage(language);
        } catch (MotuExceptionBase e) {
            throw new ServletException(e.notifyException(), e);
        } catch (Exception e) {
            throw new ServletException(e);
        }

    }

    /**
     * Delete file.
     * 
     * @param response the response
     * @param urls the urls to delete
     */
    protected void deleteFile(List<String> urls, HttpServletResponse response) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteFile(String, HttpServletResponse) - entering");
        }

        if (urls == null) {
            return;
        }

        Writer writer = null;
        boolean hasErrors = false;
        boolean isDeleted = false;
        StringBuffer messages = new StringBuffer();

        try {

            writer = response.getWriter();
            response.setContentType(null);

            String extractionPath = Organizer.getMotuConfigInstance().getExtractionPath();
            String downloadHttpUrl = Organizer.getMotuConfigInstance().getDownloadHttpUrl();

            for (String url : urls) {

                if (MotuServlet.isNullOrEmpty(url)) {
                    continue;
                }
                String fileName = url.replace(downloadHttpUrl, extractionPath);

                File file = new File(fileName);
                isDeleted = file.delete();

                if (isDeleted) {
                    messages.append(String.format("==>File '%s' is deleted\n", url));
                } else {
                    hasErrors = true;
                    messages.append(String.format("==>Unable to delete file '%s' (internal name '%s')\n", url, fileName));
                }

            }

            StatusModeResponse statusModeResponse = Organizer.createStatusModeResponse();

            if (hasErrors) {
                statusModeResponse.setCode(ErrorType.SYSTEM);
                statusModeResponse.setStatus(StatusModeType.ERROR);
            } else {
                statusModeResponse.setCode(ErrorType.OK);
                statusModeResponse.setStatus(StatusModeType.DONE);
            }

            statusModeResponse.setMsg(messages.toString());

            Organizer.marshallStatusModeResponse(statusModeResponse, writer);

        } catch (Exception e) {
            LOG.error("deleteFile(String, HttpServletResponse)", e);

            try {
                Organizer.marshallStatusModeResponse(e, writer);
            } catch (MotuMarshallException e2) {
                LOG.error("status writing error - " + e2.notifyException(), e2);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteFile(String, HttpServletResponse) - exiting");
        }
    }

    /**
     * Handles a GET request.
     * 
     * @param response object that contains the response the servlet sends to the client
     * @param request object that contains the request the client has made of the servlet.
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     * 
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // HttpSession session = request.getSession(true);
        // response.setContentType(CONTENT_TYPE_HTML);
        // PrintWriter out = response.getWriter();
        // String title = "Motu application";
        // String heading;
        // Integer accessCount = new Integer(0);
        // if (session.isNew()) {
        // Integer intervall = new Integer(session.getMaxInactiveInterval()/60);
        // heading = "Welcome. This session will expire after " +
        // intervall.toString() + " minutes of inactivity.";
        // initSession(session);
        // //listServices(session, response);
        // } else {
        // heading = "Welcome Back";
        // Integer oldAccessCount =
        // (Integer)session.getAttribute("accessCount");
        // Organizer organizer =
        // (Organizer)session.getAttribute(ORGANIZER_SESSION_ATTR);
        // if (oldAccessCount != null) {
        // accessCount =
        // new Integer(oldAccessCount.intValue() + 1);
        // }
        // }
        //
        // Map<String, String> parameters = (Map<String,
        // String>)request.getParameterMap();
        //        
        // Set<String> keySet = parameters.keySet();
        // for(Iterator<String> it = keySet.iterator() ; it.hasNext();) {
        // System.out.println(it.next());
        // }
        //

        // TLog.logger().info(String.format("HttpServletRequest.getContextPath(): %s",
        // request.getContextPath()));
        // TLog.logger().info(String.format("HttpServletRequest.getPathInfo(): %s", request.getPathInfo()));
        // TLog.logger().info(String.format("HttpServletRequest.getPathTranslated(): %s",
        // request.getPathTranslated()));
        // TLog.logger().info(String.format("HttpServletRequest.getRequestURI(): %s",
        // request.getRequestURI()));
        // TLog.logger().info(String.format("HttpServletRequest.getRequestURL(): %s",
        // request.getRequestURL()));
        // TLog.logger().info(String.format("HttpServletRequest.getServletPath(): %s",
        // request.getServletPath()));

        // System.out.print("request.getContextPath():");
        // System.out.println(request.getContextPath());
        // System.out.print("request.getServletPath():");
        // System.out.println(request.getServletPath());
        // System.out.print("request.getPathInfo():");
        // System.out.println(request.getPathInfo());
        // System.out.print("request.getPathTranslated():");
        // System.out.println(request.getPathTranslated());
        // System.out.println(request.getRemoteAddr());
        // System.out.println(request.getRemoteHost());
        // System.out.println(request.getRemoteUser());
        //
        // String ipaddress = getRemoteHost(request);

        debugRequestHeaders(request);

        execRequest(request, response);

    }

    /**
     * Handles a POST request.
     * 
     * @param response object that contains the response the servlet sends to the client
     * @param request object that contains the request the client has made of the servlet.
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     * 
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    /**
     * Executes the default request.
     * 
     * @param response object that contains the response the servlet sends to the client
     * @param session request sesssion
     * @param request object that contains the request the client has made of the servlet.
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     */
    protected void execDefaultRequest(HttpServletRequest request, HttpSession session, HttpServletResponse response) throws ServletException,
            IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("execDefaultRequest() - entering");
        }

        // System.out.println("deduceServiceNameFromPath(request) :");
        // System.out.println(deduceServiceNameFromPath(request));

        // String serviceName = getServletConfig().getInitParameter(PARAM_SERVICE);
        String serviceName;
        try {
            serviceName = deduceServiceNameFromPath(request);
        } catch (MotuExceptionBase e) {
            throw new ServletException(e.notifyException(), e);
        }

        setLanguageParameter(request, session, response);

        if (MotuServlet.isNullOrEmpty(serviceName)) {
            Organizer organizer = getOrganizer(session, response);
            listCatalog(organizer.getDefaultServiceName(), session, response);
        } else {
            listCatalog(serviceName, session, response);
        }

        // if (isMercator()) {
        // setLanguageParameter(request, session);
        // listCatalog(PARAM_SERVICE_MERCATOR, session, response);
        // } else if (isAviso()) {
        // setLanguageParameter(request, session);
        // listCatalog(PARAM_SERVICE_AVISO_NRT, session, response);
        // } else if (isCls()) {
        // setLanguageParameter(request, session);
        // listCatalog(PARAM_SERVICE_CLS, session, response);
        // }
        // Organizer organizer = getOrganizer(session);
        // listCatalog(organizer.getDefaultServiceName(), session, response);

        // listServices(session, response);

        if (LOG.isDebugEnabled()) {
            LOG.debug("execDefaultRequest() - exiting");
        }
    }

    /**
     * Scans request's parameters and executes the request. Each request must have the {@value #PARAM_ACTION}
     * parameter. If request's parameters doesn't match, a default request is execute.
     * 
     * Valid matches of parameters are:
     * <ul>
     * <li>PARAM_ACTION = ACTION_LIST_CATALOG & PARAM_SERVICE = Service name</li>
     * <li>PARAM_ACTION = ACTION_LIST_PRODUCT_METADATA & PARAM_SERVICE = Service name & PARAM_PRODCUT =
     * Product Id</li>
     * <li>PARAM_ACTION = ACTION_PRODUCT_DOWNLOADHOME & PARAM_SERVICE = Service name & PARAM_PRODCUT = Product
     * Id</li>
     * <li>PARAM_ACTION = ACTION_PRODUCT_DOWNLOAD & PARAM_SERVICE = Service name & PARAM_PRODCUT = Product Id</li>
     * </ul>
     * 
     * @param response object that contains the response the servlet sends to the client
     * @param request object that contains the request the client has made of the servlet.
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     */
    protected void execRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("execRequest() - entering");
        }

        String action = getAction(request);
        // -----------------------------------
        // actions before check authorization and/or without Http session
        // -----------------------------------
        if (isActionPing(action, request, response)) {
            // Nothing to do
        } else if (isActionDebug(action, request, response)) {
            // Nothing to do
        } else if (isActionGetRequestStatus(action, request, response)) {
            // Nothing to do
        } else if (isActionGetTimeCoverage(action, request, response)) {
            // Nothing to do
        } else if (isActionGetSize(action, request, response)) {
            // Nothing to do
        } else if (isActionDelete(action, request, response)) {
            // Nothing to do
        } else {

            // -----------------------------------
            // actions with check authorization if needed, and/or with Http session
            // -----------------------------------
            HttpSession session = getSession(request);

            if (!checkAuthorized(request, session, response)) {
                LOG.debug("execRequest() - exiting");
                return;
            }

            response.setContentType(CONTENT_TYPE_HTML);

            if (isActionListCatalog(action, request, session, response)) {
                // Nothing to do
            } else if (isActionListProductMetaData(action, request, session, response)) {
                // Nothing to do
            } else if (isActionListProductDownloadHome(action, request, session, response)) {
                // Nothing to do
            } else if (isActionProductDownload(action, request, session, response)) {
                // Nothing to do
            } else if (isActionListServices(action, request, session, response)) {
                // Nothing to do
            } else if (isActionRefresh(action, request, session, response)) {
                // Nothing to do
            } else {
                // No parameter or parameters doesn't match
                execDefaultRequest(request, session, response);
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("execRequest() - exiting");
        }
    }

    /**
     * Returns the current session associated with this request, or if the request does not have a session,
     * creates one.
     * 
     * @param request object that contains the request the client has made of the servlet.
     * 
     * @return the HttpSession associated with this request
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     */
    protected HttpSession getSession(HttpServletRequest request) throws ServletException, IOException {

        if (!isCreateOrGetSession(request)) {
            return null;
        }

        HttpSession session = request.getSession(true);
        isValid(session);
        initSession(session);

        return session;

    }

    /**
     * Checks if is action delete.
     * 
     * @param response the response
     * @param request the request
     * @param action the action
     * 
     * @return true, if is action delete
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     */
    protected boolean isActionDelete(String action, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!action.equalsIgnoreCase(ACTION_DELETE)) {
            return false;
        }

        deleteFile(getDataParams(request), response);

        return true;

    }

    /**
     * Checks if is action get request status.
     * 
     * @param response the response
     * @param request the request
     * @param action the action
     * 
     * @return true, if is action get request status
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     */
    protected boolean isActionGetRequestStatus(String action, HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {

        if (!action.equalsIgnoreCase(ACTION_GET_REQUEST_STATUS)) {
            return false;
        }

        String requestIdAsString = request.getParameter(PARAM_REQUEST_ID);
        long requestId = -1;

        StatusModeResponse statusModeResponse = null;
        try {
            if (!MotuServlet.isNullOrEmpty(requestIdAsString)) {
                requestId = Long.parseLong(requestIdAsString);
                statusModeResponse = requestManagement.getResquestStatusMap(requestId);
            }

            response.setContentType(null);

            if (statusModeResponse == null) {
                Organizer.marshallStatusModeResponse(new MotuInvalidRequestIdException(requestId), response.getWriter());
            } else {
                Organizer.marshallStatusModeResponse(statusModeResponse, response.getWriter());
            }

        } catch (MotuMarshallException e) {
            response.sendError(500, String.format("ERROR: %s", e.getMessage()));
        }
        return true;
    }

    /**
     * Checks if is action list get size.
     * 
     * @param response the response
     * @param request the request
     * @param action the action
     * 
     * @return true, if is action list get size
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     */
    protected boolean isActionGetSize(String action, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionListGetSize(String, HttpServletRequest, HttpSession, HttpServletResponse) - entering");
        }

        if (!action.equalsIgnoreCase(ACTION_GET_SIZE)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isActionListGetSize(String, HttpServletRequest, HttpSession, HttpServletResponse) - exiting");
            }
            return false;
        }

            
        ExtractionParameters extractionParameters = new ExtractionParameters(
                request.getParameter(MotuRequestParametersConstant.PARAM_SERVICE),
                request.getParameter(MotuRequestParametersConstant.PARAM_DATA),
                getVariables(request),
                getTemporalCoverage(request),
                getGeoCoverage(request),
                getDepthCoverage(request),
                getProductId(request, response),
                Organizer.Format.NETCDF,
                response.getWriter(),
                null,
                null,
                true);

        extractionParameters.setBatchQueue(isBatch(request));

        // Set assertion to manage CAS.
        extractionParameters.setAssertion(AssertionHolder.getAssertion());

        response.setContentType(null);

        getAmountDataSize(extractionParameters, response);

        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionListGetSize(String, HttpServletRequest, HttpSession, HttpServletResponse) - exiting");
        }
        return true;
    }

    /**
     * Checks if is action get time coverage.
     * 
     * @param response the response
     * @param request the request
     * @param action the action
     * 
     * @return true, if is action get time coverage
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     */
    protected boolean isActionGetTimeCoverage(String action, HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionGetTimeCoverage() - entering");
        }
        if (!action.equalsIgnoreCase(ACTION_GET_TIME_COVERAGE)) {
            return false;
        }

        String serviceName = request.getParameter(PARAM_SERVICE);
        if (MotuServlet.isNullOrEmpty(serviceName)) {
            serviceName = "";
        }

        String locationData = request.getParameter(PARAM_DATA);

        String productId = getProductId(request, response);

        if (MotuServlet.isNullOrEmpty(locationData) && MotuServlet.isNullOrEmpty(productId)) {
            if (LOG.isDebugEnabled()) {
                LOG.info(" empty locationData and empty productId");
                LOG.debug("isActionGetTimeCoverage() - exiting");
            }
            response.sendError(400, String.format("ERROR: neither '%s' nor '%s' parameters are filled - Choose one of them",
                                                  PARAM_DATA,
                                                  PARAM_PRODUCT));
            return true;
        }

        if (!MotuServlet.isNullOrEmpty(locationData) && !MotuServlet.isNullOrEmpty(productId)) {
            if (LOG.isDebugEnabled()) {
                LOG.info(" non empty locationData and non empty productId");
                LOG.debug("isActionGetTimeCoverage() - exiting");
            }
            response.sendError(400, String.format("ERROR: '%s' and '%s' parameters are not compatible - Choose only one of them",
                                                  PARAM_DATA,
                                                  PARAM_PRODUCT));
            return true;
        }

        if (MotuServlet.isNullOrEmpty(serviceName) && !MotuServlet.isNullOrEmpty(productId)) {
            if (LOG.isDebugEnabled()) {
                LOG.info("empty serviceName  and non empty productId");
                LOG.debug("isActionGetTimeCoverage() - exiting");
            }
            response.sendError(400, String.format("ERROR: '%s' parameter is filled but '%s' is empty. You have to fill it.",
                                                  PARAM_PRODUCT,
                                                  PARAM_SERVICE));
            return true;
        }

        // -------------------------------------------------
        // get Time coverage
        // -------------------------------------------------
        if (!MotuServlet.isNullOrEmpty(locationData)) {
            productGetTimeCoverage(locationData, response);
        } else if (!MotuServlet.isNullOrEmpty(serviceName) && !MotuServlet.isNullOrEmpty(productId)) {
            productGetTimeCoverage(serviceName, productId, response);
        }

        response.setContentType(null);

        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionGetTimeCoverage() - exiting");
        }

        return true;
    }

    /**
     * Executes the ACTION_LIST_CATALOG if request's parameters match.
     * 
     * @param response object that contains the response the servlet sends to the client
     * @param session request sesssion
     * @param request object that contains the request the client has made of the servlet.
     * @param action action to be executed.
     * 
     * @return true is request is ACTION_LIST_CATALOG and have been executed, false otherwise.
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     */
    protected boolean isActionListCatalog(String action, HttpServletRequest request, HttpSession session, HttpServletResponse response)
            throws ServletException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionListCatalog() - entering");
        }

        if (!action.equalsIgnoreCase(ACTION_LIST_CATALOG)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isActionListCatalog() - exiting");
            }
            return false;
        }

        String serviceName = request.getParameter(PARAM_SERVICE);
        if (MotuServlet.isNullOrEmpty(serviceName)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isActionListCatalog() - exiting");
            }
            return false;
        }

        setLanguageParameter(request, session, response);
        listCatalog(serviceName, session, response);

        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionListCatalog() - exiting");
        }
        return true;
    }

    /**
     * Executes the ACTION_PRODUCT_DOWNLOADHOME if request's parameters match.
     * 
     * @param response object that contains the response the servlet sends to the client
     * @param session request sesssion
     * @param request object that contains the request the client has made of the servlet.
     * @param action action to be executed.
     * 
     * @return true is request is ACTION_PRODUCT_DOWNLOADHOME and have been executed, false otherwise.
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     */
    protected boolean isActionListProductDownloadHome(String action, HttpServletRequest request, HttpSession session, HttpServletResponse response)
            throws ServletException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionListProductDownloadHome() - entering");
        }

        if (!action.equalsIgnoreCase(ACTION_PRODUCT_DOWNLOADHOME)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isActionListProductDownloadHome() - exiting");
            }
            return false;
        }

        String serviceName = request.getParameter(PARAM_SERVICE);
        if (MotuServlet.isNullOrEmpty(serviceName)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isActionListProductDownloadHome() - exiting");
            }
            return false;
        }
        String productId = getProductId(request, response);
        if (MotuServlet.isNullOrEmpty(productId)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isActionListProductDownloadHome() - exiting");
            }
            return false;
        }

        setLanguageParameter(request, session, response);
        productDownloadHome(serviceName, productId, session, response);

        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionListProductDownloadHome() - exiting");
        }
        return true;
    }

    /**
     * Executes the ACTION_LIST_PRODUCT_METADATA if request's parameters match.
     * 
     * @param response object that contains the response the servlet sends to the client
     * @param session request sesssion
     * @param request object that contains the request the client has made of the servlet.
     * @param action action to be executed.
     * 
     * @return true is request is ACTION_LIST_PRODUCT_METADATA and have been executed, false otherwise.
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     */
    protected boolean isActionListProductMetaData(String action, HttpServletRequest request, HttpSession session, HttpServletResponse response)
            throws ServletException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionListProductMetaData() - entering");
        }

        if (!action.equalsIgnoreCase(ACTION_LIST_PRODUCT_METADATA)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isActionListProductMetaData() - exiting");
            }
            return false;
        }

        String serviceName = request.getParameter(PARAM_SERVICE);
        if (MotuServlet.isNullOrEmpty(serviceName)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isActionListProductMetaData() - exiting");
            }
            return false;
        }
        String productId = getProductId(request, response);
        if (MotuServlet.isNullOrEmpty(productId)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isActionListProductMetaData() - exiting");
            }
            return false;
        }

        setLanguageParameter(request, session, response);
        listProductMetaData(serviceName, productId, session, response);

        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionListProductMetaData() - exiting");
        }
        return true;
    }

    /**
     * Executes the ACTION_LIST_SERVICES if request's parameters match.
     * 
     * @param response object that contains the response the servlet sends to the client
     * @param session request sesssion
     * @param request object that contains the request the client has made of the servlet.
     * @param action action to be executed.
     * 
     * @return true is request is ACTION_LIST_SERVICES and have been executed, false otherwise.
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     */
    protected boolean isActionListServices(String action, HttpServletRequest request, HttpSession session, HttpServletResponse response)
            throws ServletException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionListServices() - entering");
        }

        if (!action.equalsIgnoreCase(ACTION_LIST_SERVICES)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isActionListServices() - exiting");
            }
            return false;
        }

        setLanguageParameter(request, session, response);
        listServices(session, response);

        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionListServices() - exiting");
        }
        return true;
    }

    /**
     * Executes the ACTION_PRODUCT_DOWNLOAD if request's parameters match.
     * 
     * @param response object that contains the response the servlet sends to the client
     * @param request object that contains the request the client has made of the servlet.
     * @param action action to be executed.
     * 
     * @return true is request is ACTION_PRODUCT_DOWNLOAD and have been executed, false otherwise.
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     */
    protected boolean isActionPing(String action, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionPing() - entering");
        }

        if (!action.equalsIgnoreCase(ACTION_PING)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isActionPing() - exiting");
            }
            return false;
        }

        response.setContentType(CONTENT_TYPE_PLAIN);
        PrintWriter out = response.getWriter();
        out.write(RESPONSE_ACTION_PING);

        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionPing() - exiting");
        }
        return true;
    }

    /**
     * Checks if is action product download.
     * 
     * @param response the response
     * @param session the session
     * @param request the request
     * @param action the action
     * 
     * @return true, if is action product download
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     */
    protected boolean isActionProductDownload(String action, HttpServletRequest request, HttpSession session, HttpServletResponse response)
            throws IOException, ServletException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionProductDownload(String, HttpServletRequest, HttpSession, HttpServletResponse) - entering");
        }

        if (!RequestManagement.isUseQueueServer()) {
            boolean returnboolean = isActionProductDownloadNoQueueing(action, request, session, response);
            if (LOG.isDebugEnabled()) {
                LOG.debug("isActionProductDownload(String, HttpServletRequest, HttpSession, HttpServletResponse) - exiting");
            }
            return returnboolean;
        }

        if (!action.equalsIgnoreCase(MotuRequestParametersConstant.ACTION_PRODUCT_DOWNLOAD)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isActionProductDownload(String, HttpServletRequest, HttpSession, HttpServletResponse) - exiting");
            }
            return false;
        }

        try {

            setLanguageParameter(request, session, response);
        } catch (ServletException e) {
            LOG.error("isActionProductDownload(String, HttpServletRequest, HttpSession, HttpServletResponse)", e);

            response.sendError(500, String.format("ERROR: %s", e.getMessage()));

            if (LOG.isDebugEnabled()) {
                LOG.debug("isActionProductDownload(String, HttpServletRequest, HttpSession, HttpServletResponse) - exiting");
            }
            return true;
        }

        Writer out = null;
        Organizer.Format responseFormat = null;

        String mode = getMode(request);

        int priority = getRequestPriority(request);

        overrideMaxPoolAnonymous(request);
        overrideMaxPoolAuthenticate(request);

        String userId = getLogin(request, session);
        boolean anonymousUser = isAnonymousUser(request, userId);
        String userHost = getRemoteHost(request);

        if (MotuServlet.isNullOrEmpty(userId)) {
            userId = userHost;
        }

        if (RunnableHttpExtraction.noMode(mode)) {
            out = response.getWriter();
            responseFormat = Organizer.Format.HTML;
        }

        ExtractionParameters extractionParameters = new ExtractionParameters(
                request.getParameter(MotuRequestParametersConstant.PARAM_SERVICE),
                request.getParameter(MotuRequestParametersConstant.PARAM_DATA),
                getVariables(request),
                getTemporalCoverage(request),
                getGeoCoverage(request),
                getDepthCoverage(request),
                getProductId(request, response),
                Organizer.Format.NETCDF,
                out,
                responseFormat,
                userId,
                anonymousUser);

        extractionParameters.setBatchQueue(isBatch(request));
        extractionParameters.setUserHost(userHost);

        // Set assertion to manage CAS.
        extractionParameters.setAssertion(AssertionHolder.getAssertion());

        productDownload(extractionParameters, mode, priority, session, response);

        boolean noMode = RunnableHttpExtraction.noMode(mode);
        if (!noMode) {
            removeOrganizerSession(session);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionProductDownload(String, HttpServletRequest, HttpSession, HttpServletResponse) - exiting");
        }
        return true;

    }

    /**
     * Executes the ACTION_PRODUCT_DOWNLOAD if request's parameters match.
     * 
     * @param response object that contains the response the servlet sends to the client
     * @param session request sesssion
     * @param request object that contains the request the client has made of the servlet.
     * @param action action to be executed.
     * 
     * @return true is request is ACTION_PRODUCT_DOWNLOAD and have been executed, false otherwise.
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     */
    protected boolean isActionProductDownloadNoQueueing(String action, HttpServletRequest request, HttpSession session, HttpServletResponse response)
            throws ServletException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionProductDownloadNoQueueing() - entering");
        }

        if (RequestManagement.isUseQueueServer()) {
            return false;
        }

        if (!action.equalsIgnoreCase(ACTION_PRODUCT_DOWNLOAD)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isActionProductDownloadNoQueueing() - exiting");
            }
            return false;
        }

        Writer out = null;
        Organizer.Format responseFormat = null;

        String mode = getMode(request);

        if (MotuServlet.isNullOrEmpty(mode)) {
            out = response.getWriter();
            responseFormat = Organizer.Format.HTML;
        }

        ExtractionParameters extractionParameters = new ExtractionParameters(
                request.getParameter(MotuRequestParametersConstant.PARAM_SERVICE),
                request.getParameter(MotuRequestParametersConstant.PARAM_DATA),
                getVariables(request),
                getTemporalCoverage(request),
                getGeoCoverage(request),
                getDepthCoverage(request),
                getProductId(request, response),
                Organizer.Format.NETCDF,
                out,
                responseFormat,
                null,
                true);

        // Set assertion to manage CAS.
        extractionParameters.setAssertion(AssertionHolder.getAssertion());

        setLanguageParameter(request, session, response);

        // -------------------------------------------------
        // Data extraction
        // -------------------------------------------------
        productDownloadNoQueueing(extractionParameters, mode, session, response);

        boolean noMode = RunnableHttpExtraction.noMode(mode);
        if (!noMode) {
            removeOrganizerSession(session);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionProductDownloadNoQueueing() - exiting");
        }
        return true;
    }

    /**
     * Executes the ACTION_REFRESH if request's parameters match.
     * 
     * @param response object that contains the response the servlet sends to the client
     * @param session request sesssion
     * @param request object that contains the request the client has made of the servlet.
     * @param action action to be executed.
     * 
     * @return true is request is ACTION_REFRESH and have been executed, false otherwise.
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     */
    protected boolean isActionRefresh(String action, HttpServletRequest request, HttpSession session, HttpServletResponse response)
            throws ServletException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionRefresh() - entering");
        }

        if (!action.equalsIgnoreCase(ACTION_REFRESH)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isActionRefresh() - exiting");
            }
            return false;
        }

        String language = request.getParameter(PARAM_LANGUAGE);
        if (MotuServlet.isNullOrEmpty(language)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isActionRefresh() - exiting");
            }
            return false;
        }

        setLanguageParameter(request, session, response);
        refresh(session, response);

        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionRefresh() - exiting");
        }
        return true;
    }

    /**
     * Checks if a session has to be created.
     * 
     * @param request the request
     * 
     * @return true, if a session has to be created, false otherwise
     */
    protected boolean isCreateOrGetSession(HttpServletRequest request) {
        String action = getAction(request);
        String mode = getMode(request);
        boolean noMode = RunnableHttpExtraction.noMode(mode);

        boolean createOk = false;
        createOk |= action.equalsIgnoreCase(MotuRequestParametersConstant.ACTION_REFRESH);
        createOk |= action.equalsIgnoreCase(MotuRequestParametersConstant.ACTION_LIST_SERVICES);
        createOk |= action.equalsIgnoreCase(MotuRequestParametersConstant.ACTION_LIST_CATALOG);
        createOk |= action.equalsIgnoreCase(MotuRequestParametersConstant.ACTION_LIST_PRODUCT_METADATA);
        createOk |= action.equalsIgnoreCase(MotuRequestParametersConstant.ACTION_PRODUCT_DOWNLOADHOME);
        createOk |= action.equalsIgnoreCase(MotuRequestParametersConstant.ACTION_PRODUCT_DOWNLOAD) && noMode;

        return createOk;
    }

    /**
     * Check authorized.
     * 
     * @param response the response
     * @param session the session
     * @param request the request
     * 
     * @return true, if check authorized
     * 
     * @throws ServletException the servlet exception
     */
    private boolean checkAuthorized(HttpServletRequest request, HttpSession session, HttpServletResponse response) throws ServletException {
        if (authentificationProps == null) {
            return true;
        }

        if (session != null) {
            if (session.getAttribute(SESSION_AUTHORIZED_KEY) != null) {
                return true;
            }
        }
        String login = getLogin(request);
        String password = request.getParameter(PARAM_PWD);

        if (!isAuthorized(login, password)) {
            try {
                response.sendError(401, "Authentification failure");
            } catch (IOException e) {
                LOG.error("response sendError failed", e);
                throw new ServletException("response sendError failed", e);
            }
            return false;
        }
        if (session == null) {
            session = request.getSession(true);
        }
        session.setAttribute(SESSION_AUTHORIZED_KEY, new Boolean(true));
        session.setAttribute(SESSION_AUTHORIZED_USER, login);
        return true;
    }

    /**
     * Creates a new Organizer for a session.
     * 
     * @param session session in which to create Oragnizer
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     */
    private void createOrganizer(HttpSession session) throws ServletException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createOrganizer() - entering");
        }

        try {
            Organizer organizer = new Organizer();
            session.setAttribute(ORGANIZER_SESSION_ATTR, organizer);
        } catch (MotuExceptionBase e) {
            LOG.error("createOrganizer()", e);

            throw new ServletException(e.notifyException(), e);
        } catch (Exception e) {
            LOG.error("createOrganizer()", e);

            throw new ServletException(e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("createOrganizer() - exiting");
        }
    }

    /**
     * Debug pending request.
     * 
     * @param stringBuffer the string buffer
     */
    private void debugPendingRequest(StringBuffer stringBuffer) {

        if (stringBuffer == null) {
            return;
        }

        if (requestManagement == null) {
            return;
        }

        stringBuffer.append("<h1 align=\"center\">\n");
        stringBuffer.append("Pending requests\n");
        stringBuffer.append("</h1>\n");

        QueueServerManagement queueServerManagement = requestManagement.getQueueServerManagement();
        if (queueServerManagement == null) {
            stringBuffer.append("<p> Queue server is not active</p>");
            return;
        }
        stringBuffer.append("<h2>\n");
        stringBuffer.append("Queue server general configuration");
        stringBuffer.append("</h2>\n");
        stringBuffer.append("<p>\n");
        stringBuffer.append(" Default priority: ");
        stringBuffer.append(queueServerManagement.getDefaultPriority());
        stringBuffer.append(" Max. data threshold non-batch: ");
        stringBuffer.append(String.format("%8.2f Mo", queueServerManagement.getMaxDataThreshold(false)));
        stringBuffer.append(" Max. data threshold batch: ");
        stringBuffer.append(String.format("%8.2f Mo", queueServerManagement.getMaxDataThreshold(true)));
        stringBuffer.append("</p>\n");

        stringBuffer.append("<h2>\n");
        stringBuffer.append("Non-Batch Queues");
        stringBuffer.append("</h2>\n");
        debugPendingRequest(stringBuffer, queueServerManagement, false);
        stringBuffer.append("<h2>\n");
        stringBuffer.append("Batch Queues");
        stringBuffer.append("</h2>\n");
        debugPendingRequest(stringBuffer, queueServerManagement, true);
    }

    /**
     * Debug pending request.
     * 
     * @param stringBuffer the string buffer
     * @param batch the batch
     * @param queueServerManagement the queue server management
     */
    private void debugPendingRequest(StringBuffer stringBuffer, QueueServerManagement queueServerManagement, boolean batch) {

        if (stringBuffer == null) {
            return;
        }

        if (queueServerManagement == null) {
            return;
        }

        List<QueueType> queuesConfig = queueServerManagement.getQueuesConfig();
        QueueManagement queueManagement = null;

        boolean hasQueue = false;

        // queues are sorted by data threshold (ascending)
        for (QueueType queueConfig : queuesConfig) {
            queueManagement = queueServerManagement.getQueueManagement(queueConfig);
            if (queueManagement == null) {
                continue;
            }
            if (queueConfig.isBatch() != batch) {
                continue;
            }

            hasQueue = true;

            stringBuffer.append("<h3>\n");
            stringBuffer.append(queueConfig.getId());
            stringBuffer.append(": ");
            stringBuffer.append(queueConfig.getDescription());
            stringBuffer.append("</h3>\n");
            stringBuffer.append("<table border=\"1\">\n");
            stringBuffer.append("<p>\n");
            stringBuffer.append("Max. pool anonymous: ");
            if (queueConfig.isBatch()) {
                stringBuffer.append("unlimited");
            } else {
                stringBuffer.append(queueServerManagement.computeMaxPoolAnonymous(queueManagement));
            }
            stringBuffer.append(" Max. pool authenticate: ");
            if (queueConfig.isBatch()) {
                stringBuffer.append("unlimited");
            } else {
                stringBuffer.append(queueServerManagement.computeMaxPoolAuthenticate(queueManagement));
            }
            stringBuffer.append("</p>\n");
            stringBuffer.append("<p>\n");
            stringBuffer.append("Max. threads: ");
            stringBuffer.append(queueConfig.getMaxThreads());
            stringBuffer.append(" Data threshold: ");
            stringBuffer.append(String.format("%8.2f Mo", queueConfig.getDataThreshold()));
            stringBuffer.append(" Max. pool size: ");
            stringBuffer.append(queueConfig.getMaxPoolSize());
            stringBuffer.append(" Low priority waiting: ");
            stringBuffer.append(queueConfig.getLowPriorityWaiting());
            stringBuffer.append("</p>\n");
            stringBuffer.append("<p>\n");
            stringBuffer.append(" Approximate number of threads that are actively executing tasks: ");
            stringBuffer.append(queueManagement.getThreadPoolExecutor().getActiveCount());
            stringBuffer.append("</p>\n");
            stringBuffer.append("<p>\n");
            stringBuffer.append(" Approximate total number of tasks that have completed execution: ");
            stringBuffer.append(queueManagement.getThreadPoolExecutor().getCompletedTaskCount());
            stringBuffer.append("</p>\n");

            PriorityBlockingQueue<Runnable> priorityBlockingQueue = queueManagement.getPriorityBlockingQueue();

            if (priorityBlockingQueue == null) {
                stringBuffer.append("</table>\n");
                continue;
            }

            stringBuffer.append("<tr>\n");
            stringBuffer.append("<th>\n");
            stringBuffer.append("Request Id");
            stringBuffer.append("</th>\n");
            stringBuffer.append("<th>\n");
            stringBuffer.append("Status");
            stringBuffer.append("</th>\n");
            stringBuffer.append("<th>\n");
            stringBuffer.append("Mode");
            stringBuffer.append("</th>\n");
            stringBuffer.append("<th>\n");
            stringBuffer.append("Priority");
            stringBuffer.append("</th>\n");
            stringBuffer.append("<th>\n");
            stringBuffer.append("Range");
            stringBuffer.append("</th>\n");
            stringBuffer.append("<th>\n");
            stringBuffer.append("Amount data size");
            stringBuffer.append("</th>\n");
            stringBuffer.append("<th>\n");
            stringBuffer.append("User");
            stringBuffer.append("</th>\n");
            stringBuffer.append("<th>\n");
            stringBuffer.append("Anonymous ?");
            stringBuffer.append("</th>\n");
            stringBuffer.append("<th>\n");
            stringBuffer.append("User Host");
            stringBuffer.append("</th>\n");
            stringBuffer.append("<th>\n");
            stringBuffer.append("In queue since");
            stringBuffer.append("</th>\n");
            stringBuffer.append("<th>\n");
            stringBuffer.append("Extraction parameters");
            stringBuffer.append("</th>\n");
            stringBuffer.append("</tr>\n");

            for (Runnable runnable : priorityBlockingQueue) {
                if (!(runnable instanceof RunnableHttpExtraction)) {
                    continue;
                }

                stringBuffer.append("<tr>\n");
                RunnableHttpExtraction runnableHttpExtraction = (RunnableHttpExtraction) runnable;
                StatusModeResponse statusModeResponse = runnableHttpExtraction.getStatusModeResponse();
                if (statusModeResponse == null) {
                    stringBuffer.append("</tr>\n");
                    continue;
                }
                stringBuffer.append("<td>\n");
                stringBuffer.append(statusModeResponse.getRequestId().toString());
                stringBuffer.append("</td>\n");
                stringBuffer.append("<td>\n");
                stringBuffer.append(statusModeResponse.getStatus().toString());
                stringBuffer.append("</td>\n");
                stringBuffer.append("<td>\n");
                stringBuffer.append(runnableHttpExtraction.getMode());
                stringBuffer.append("</td>\n");
                stringBuffer.append("<td>\n");
                stringBuffer.append(Integer.toString(runnableHttpExtraction.getPriority()));
                stringBuffer.append("</td>\n");
                stringBuffer.append("<td>\n");
                stringBuffer.append(Integer.toString(runnableHttpExtraction.getRange()));
                stringBuffer.append("</td>\n");
                stringBuffer.append("<td>\n");
                stringBuffer.append(String.format("%8.2f Mo", runnableHttpExtraction.getQueueLogInfo().getAmountDataSize()));
                stringBuffer.append("</td>\n");
                stringBuffer.append("<td>\n");
                stringBuffer.append(runnableHttpExtraction.getExtractionParameters().getUserId());
                stringBuffer.append("</td>\n");
                stringBuffer.append("<td>\n");
                stringBuffer.append(runnableHttpExtraction.getExtractionParameters().isAnonymousUser());
                stringBuffer.append("</td>\n");
                stringBuffer.append("<td>\n");
                stringBuffer.append(runnableHttpExtraction.getExtractionParameters().getUserHost());
                stringBuffer.append("</td>\n");
                stringBuffer.append("<td>\n");
                if (runnableHttpExtraction.getQueueLogInfo().getInQueueTime() != null) {
                    stringBuffer.append(runnableHttpExtraction.getQueueLogInfo().getInQueueTime().toString());
                } else {
                    stringBuffer.append("Unknown");
                }
                stringBuffer.append("</td>\n");
                stringBuffer.append("<td>\n");
                stringBuffer.append(runnableHttpExtraction.getExtractionParameters().toString());
                stringBuffer.append("</td>\n");
                stringBuffer.append("</tr>\n");
            }
            stringBuffer.append("</table>\n");
        }
        if (!hasQueue) {
            stringBuffer.append("None");
            return;
        }

    }

    /**
     * Debug request all status.
     * 
     * @param stringBuffer the string buffer
     */
    private void debugRequestAllStatus(StringBuffer stringBuffer) {
        if (stringBuffer == null) {
            return;
        }
        stringBuffer.append("<h1 align=\"center\">\n");
        stringBuffer.append("Request status");
        stringBuffer.append("</h1>\n");

        for (StatusModeType statusModeType : StatusModeType.values()) {
            debugRequestStatus(stringBuffer, statusModeType);

        }
    }

    /**
     * Debug request headers.
     * 
     * @param request the request
     */
    private void debugRequestHeaders(HttpServletRequest request) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("debugRequestHeaders(HttpServletRequest) - entering");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("BEGIN REQUEST HEADERS");
        }
        for (Enumeration<?> e = request.getHeaderNames(); e.hasMoreElements();) {
            Object o = e.nextElement();
            if (LOG.isDebugEnabled()) {
                LOG.debug(o);
                LOG.debug(request.getHeader((String) o));
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("END REQUEST HEADERS");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("debugRequestHeaders(HttpServletRequest) - exiting");
        }
    }

    /**
     * Debug request status.
     * 
     * @param stringBuffer the string buffer
     * @param statusModeType the status mode type
     */
    private void debugRequestStatus(StringBuffer stringBuffer, StatusModeType statusModeType) {

        if (stringBuffer == null) {
            return;
        }

        stringBuffer.append("<h2>\n");
        stringBuffer.append("Status: \n");
        stringBuffer.append(statusModeType.toString());
        stringBuffer.append("</h2>\n");
        stringBuffer.append("<table border=\"1\">\n");
        stringBuffer.append("<tr>\n");
        stringBuffer.append("<th>\n");
        stringBuffer.append("Request Id");
        stringBuffer.append("</th>\n");
        stringBuffer.append("<th>\n");
        stringBuffer.append("Time");
        stringBuffer.append("</th>\n");
        stringBuffer.append("<th>\n");
        stringBuffer.append("Status");
        stringBuffer.append("</th>\n");
        stringBuffer.append("<th>\n");
        stringBuffer.append("Code");
        stringBuffer.append("</th>\n");
        stringBuffer.append("<th>\n");
        stringBuffer.append("Message");
        stringBuffer.append("</th>\n");
        stringBuffer.append("<th>\n");
        stringBuffer.append("Remote data");
        stringBuffer.append("</th>\n");
        stringBuffer.append("<th>\n");
        stringBuffer.append("Local data");
        stringBuffer.append("</th>\n");
        stringBuffer.append("</tr>\n");

        Set<Long> requestIds = requestManagement.requestStatusMapKeySet();

        for (Long requestId : requestIds) {
            StatusModeResponse statusModeResponse = requestManagement.getResquestStatusMap(requestId);
            if (statusModeResponse.getStatus() != statusModeType) {
                continue;
            }

            stringBuffer.append("<tr>\n");
            stringBuffer.append("<td>\n");
            stringBuffer.append(requestId.toString());
            stringBuffer.append("</td>\n");
            stringBuffer.append("<td>\n");
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(requestId);
            stringBuffer.append(cal.getTime().toString());
            stringBuffer.append("</td>\n");
            stringBuffer.append("<td>\n");
            stringBuffer.append(statusModeResponse.getStatus().toString());
            stringBuffer.append("</td>\n");
            stringBuffer.append("<td>\n");
            stringBuffer.append(statusModeResponse.getCode().toString());
            stringBuffer.append("</td>\n");
            stringBuffer.append("<td>\n");
            stringBuffer.append(statusModeResponse.getMsg());
            stringBuffer.append(" - file length is : ");
            if (statusModeResponse.getSize() != null) {
                stringBuffer.append(statusModeResponse.getSize());
            } else {
                stringBuffer.append("null");
            }
            stringBuffer.append(" - file lastModified is : ");
            if (statusModeResponse.getDateProc() != null) {
                XMLGregorianCalendar lastModified = statusModeResponse.getDateProc().normalize();
                stringBuffer.append(lastModified.toString());
            } else {
                stringBuffer.append("null");
            }
            stringBuffer.append("</td>\n");
            stringBuffer.append("<td>\n");
            stringBuffer.append(statusModeResponse.getRemoteUri());
            stringBuffer.append("</td>\n");
            stringBuffer.append("<td>\n");
            stringBuffer.append(statusModeResponse.getLocalUri());
            stringBuffer.append("</td>\n");
            stringBuffer.append("</tr>\n");
            stringBuffer.append("\n");
        }
        stringBuffer.append("</table>\n");

    }

    /**
     * Gets the action.
     * 
     * @param request the request
     * 
     * @return the action
     */
    private String getAction(HttpServletRequest request) {
        String action = request.getParameter(PARAM_ACTION);
        if (MotuServlet.isNullOrEmpty(action)) {
            action = ACTION_LIST_CATALOG;
        }

        return action;

    }

    /**
     * Gets the amount data size.
     * 
     * @param response the response
     * @param extractionParameters the extraction parameters
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     */
    private void getAmountDataSize(ExtractionParameters extractionParameters, HttpServletResponse response) throws ServletException, IOException {

        try {
            Organizer organizer = getOrganizer(null, response);
            organizer.getAmountDataSize(extractionParameters);
        } catch (MotuMarshallException e) {
            response.sendError(500, String.format("ERROR: %s", e.getMessage()));
        } catch (MotuExceptionBase e) {
            // Do nothing error is in response error code
            // response.sendError(400, String.format("ERROR: %s", e.notifyException()));
        }
    }

    /**
     * Gets the data params.
     * 
     * @param request the request
     * 
     * @return the data params
     */
    private List<String> getDataParams(HttpServletRequest request) {
        String[] data = request.getParameterValues(PARAM_DATA);
        List<String> listData = null;
        if (data != null) {
            if (data.length > 0) {
                listData = Arrays.asList(data);
            }
        }
        return listData;

    }

    /**
     * Gets the depth coverage from the request.
     * 
     * @param request servlet request
     * 
     * @return a list of deph coverage : first depth min, then depth max
     */
    private List<String> getDepthCoverage(HttpServletRequest request) {
        // -------------------------------------------------
        // Gets Depth coverage
        // -------------------------------------------------
        String lowdepth = request.getParameter(PARAM_LOW_Z);
        String highDepth = request.getParameter(PARAM_HIGH_Z);
        List<String> listDepthCoverage = new ArrayList<String>();

        if (lowdepth != null) {
            listDepthCoverage.add(lowdepth);
        }

        if (highDepth != null) {
            listDepthCoverage.add(highDepth);
        }
        return listDepthCoverage;
    }

    /**
     * Gets the forwarded for.
     * 
     * @param request the request
     * 
     * @return the forwarded for
     */
    private String getForwardedFor(HttpServletRequest request) {

        String forwardedFor = request.getParameter(MotuRequestParametersConstant.PARAM_FORWARDED_FOR);
        return MotuServlet.getHostName(forwardedFor);

    }

    /**
     * Gets the geographical coverage from the request.
     * 
     * @param request servlet request
     * 
     * @return a list of geographical coverage : Lat min, Lon min, Lat max, Lon max
     */
    private List<String> getGeoCoverage(HttpServletRequest request) {
        String lowLat = request.getParameter(PARAM_LOW_LAT);
        String lowLon = request.getParameter(PARAM_LOW_LON);
        String highLat = request.getParameter(PARAM_HIGH_LAT);
        String highLon = request.getParameter(PARAM_HIGH_LON);
        List<String> listLatLonCoverage = new ArrayList<String>();

        if (lowLat != null) {
            listLatLonCoverage.add(lowLat);
        } else {
            listLatLonCoverage.add("-90");
        }
        if (lowLon != null) {
            listLatLonCoverage.add(lowLon);
        } else {
            listLatLonCoverage.add("-180");
        }
        if (highLat != null) {
            listLatLonCoverage.add(highLat);
        } else {
            listLatLonCoverage.add("90");
        }
        if (highLon != null) {
            listLatLonCoverage.add(highLon);
        } else {
            listLatLonCoverage.add("180");
        }
        return listLatLonCoverage;
    }

    /**
     * Gets the login.
     * 
     * @param request the request
     * 
     * @return the login
     */
    private String getLogin(HttpServletRequest request) {

        return request.getParameter(MotuRequestParametersConstant.PARAM_LOGIN);
    }

    // /**
    // * Execute product download.
    // *
    // * @param response object that contains the response the servlet sends to the client
    // * @param listVar variables or expressions to download
    // * @param session request sesssion
    // * @param listLatLonCoverage list contains low latitude, low longitude, high latitude, high longitude
    // (can
    // * be empty string)
    // * @param listDepthCoverage list contains low depth, high depth.
    // * @param listTemporalCoverage list contains start date and end date (can be empty string)
    // * @param productId id of the product
    // * @param serviceName name of the service for the product
    // * @param mode extraction mode. Can be empty.
    // *
    // * @throws IOException the IO exception
    // * @throws ServletException the servlet exception
    // */
    // private void productDownloadNoQueueing(String serviceName,
    // List<String> listVar,
    // List<String> listTemporalCoverage,
    // List<String> listLatLonCoverage,
    // List<String> listDepthCoverage,
    // String productId,
    // String mode,
    // HttpSession session,
    // HttpServletResponse response) throws ServletException, IOException {
    // if (LOG.isDebugEnabled()) {
    // LOG.debug("productDownload() - entering");
    // }
    // boolean modeConsole = mode.equalsIgnoreCase(PARAM_MODE_CONSOLE);
    // boolean modeUrl = mode.equalsIgnoreCase(PARAM_MODE_URL);
    // boolean modeStatus = mode.equalsIgnoreCase(PARAM_MODE_STATUS);
    //
    // try {
    // Organizer organizer = getOrganizer(session);
    // // Product product = organizer.extractData(serviceName,
    // if (modeStatus) {
    // String productDeferedExtractNetcdfStatusUrl = productDeferedExtractNetcdf(organizer,
    // serviceName,
    // listVar,
    // listTemporalCoverage,
    // listLatLonCoverage,
    // listDepthCoverage,
    // null,
    // productId);
    // response.setContentType(CONTENT_TYPE_PLAIN);
    // PrintWriter out = response.getWriter();
    // out.write(productDeferedExtractNetcdfStatusUrl);
    // } else if ((modeConsole) || (modeUrl)) {
    // try {
    // Product product = organizer.extractData(serviceName,
    // listVar,
    // listTemporalCoverage,
    // listLatLonCoverage,
    // listDepthCoverage,
    // productId,
    // null,
    // Organizer.Format.NETCDF);
    //
    // if (modeConsole) {
    // response.sendRedirect(product.getDownloadUrlPath());
    // }
    // if (modeUrl) {
    // response.setContentType(CONTENT_TYPE_PLAIN);
    // PrintWriter out = response.getWriter();
    // out.write(product.getDownloadUrlPath());
    // }
    //
    // } catch (MotuExceptionBase e) {
    // LOG.error("productDownload()", e);
    //
    // // response.getWriter().write(String.format("ERROR: %s", e.notifyException()));
    // response.sendError(400, String.format("ERROR: %s", e.notifyException()));
    // } catch (Exception e) {
    // LOG.error("productDownload()", e);
    //
    // // response.getWriter().write(String.format("ERROR: %s", e.getMessage()));
    // response.sendError(400, String.format("ERROR: %s", e.getMessage()));
    // }
    //
    // // response.getWriter().write(product.getDownloadUrlPath());
    //
    // } else {
    // organizer.extractData(serviceName,
    // listVar,
    // listTemporalCoverage,
    // listLatLonCoverage,
    // listDepthCoverage,
    // productId,
    // null,
    // Organizer.Format.NETCDF,
    // response.getWriter(),
    // Organizer.Format.HTML);
    // }
    // } catch (MotuExceptionBase e) {
    // LOG.error("productDownload()", e);
    // response.sendError(400, String.format("ERROR: %s", e.notifyException()));
    // } catch (Exception e) {
    // LOG.error("productDownload()", e);
    // response.sendError(400, String.format("ERROR: %s", e.getMessage()));
    // }
    //
    // if (LOG.isDebugEnabled()) {
    // LOG.debug("productDownload() - exiting");
    // }
    // }

    /**
     * Gets the login.
     * 
     * @param session the session
     * @param request the request
     * 
     * @return the login
     */
    private String getLogin(HttpServletRequest request, HttpSession session) {

        String userId = getLogin(request);

        if (MotuServlet.isNullOrEmpty(userId) && session != null) {
            userId = (String) session.getAttribute(SESSION_AUTHORIZED_USER);
        }
        return userId;
    }

    /**
     * Gets the mode parameter from the request.
     * 
     * @param request servlet request
     * 
     * @return how to return the result (mode=console : url file, otherwhise HTML pages)
     */
    private String getMode(HttpServletRequest request) {
        // -------------------------------------------------
        // Gets Depth coverage
        // -------------------------------------------------
        String mode = request.getParameter(PARAM_MODE);
        if (mode == null) {
            return "";
        }
        return mode;
    }

    /**
     * Gets Organizer object form the HttpSession.
     * 
     * @param response the response
     * @param session that contains Organizer.
     * 
     * @return Organizer object.
     * 
     * @throws IOException
     */
    private Organizer getOrganizer(HttpSession session, HttpServletResponse response) throws IOException {

        Organizer organizer = null;
        try {
            if (session != null) {
                organizer = (Organizer) session.getAttribute(ORGANIZER_SESSION_ATTR);
                isValid(organizer);
            } else {
                organizer = new Organizer();
            }
        } catch (MotuExceptionBase e) {
            response.sendError(500, String.format("ERROR: - MotuServlet.getOrganizer - Unable to create a new organiser. Native Error: %s", e
                    .notifyException()));
        } catch (ServletException e) {
            response.sendError(500, String.format("ERROR: - MotuServlet.getOrganizer : %s", e.getMessage()));
        }

        // if (organizer.getMotuConfig().isUseProxy()) {
        // System.setProperty("proxyHost",
        // organizer.getMotuConfig().getProxyHost());
        // System.setProperty("proxyPort",
        // organizer.getMotuConfig().getProxyPort());
        // Authenticator.setDefault(new SimpleAuthenticator("dearith",
        // "bienvenue"));
        // }
        return organizer;
    }

    /**
     * Gets the queue server management.
     * 
     * @return the queue server management
     */
    private QueueServerManagement getQueueServerManagement() {
        return requestManagement.getQueueServerManagement();

    };

    /**
     * Gets the remote host.
     * 
     * @param request the request
     * 
     * @return the remote host
     */
    private String getRemoteHost(HttpServletRequest request) {

        String hostName = getForwardedFor(request);

        if (MotuServlet.isNullOrEmpty(hostName)) {
            hostName = request.getRemoteAddr();

            for (String ph : PROXY_HEADERS) {
                String v = request.getHeader(ph);
                if (v != null) {
                    hostName = v;
                    break;
                }
            }
        }
        return MotuServlet.getHostName(hostName);
    }

    // /**
    // * Extraction differé de produit .
    // *
    // * @param listVar the list var
    // * @param selectData the select data
    // * @param organizer the organizer
    // * @param locationData the location data
    // * @param listLatLonCoverage the list lat lon coverage
    // * @param listDepthCoverage the list depth coverage
    // * @param listTemporalCoverage the list temporal coverage
    // *
    // * @return the string
    // *
    // * @throws MotuException the motu exception
    // */
    // private String productDeferedExtractNetcdf(Organizer organizer,
    // String locationData,
    // List<String> listVar,
    // List<String> listTemporalCoverage,
    // List<String> listLatLonCoverage,
    // List<String> listDepthCoverage,
    // SelectData selectData) throws MotuException {
    //
    // // Calcul d'un numéro de produit deférré à partir du temps
    // synchronized (this) {
    // long num = System.currentTimeMillis();
    // if (num == pdsNum) {
    // // Si c'est le même temps que le précédent on incrément pour en avoir un différent
    // pdsNum++;
    // } else {
    // pdsNum = num;
    // }
    // }
    // String productDeferedExtractNetcdfStatusFileName = getStatusFileName();
    //
    // String productDeferedExtractNetcdfStatusFilePathName =
    // Organizer.getMotuConfigInstance().getExtractionPath() + "/"
    // + productDeferedExtractNetcdfStatusFileName;
    // String productDeferedExtractNetcdfStatusUrl = Organizer.getMotuConfigInstance().getDownloadHttpUrl() +
    // "/"
    // + productDeferedExtractNetcdfStatusFileName;
    //
    // MotuServlet.printProductDeferedExtractNetcdfStatus(productDeferedExtractNetcdfStatusFilePathName,
    // StatusModeType.INPROGRESS, MSG_IN_PROGRESS);
    //
    // ProductDeferedExtractNetcdfThread productDeferedExtractNetcdfThread = new
    // ProductDeferedExtractNetcdfThread(
    // productDeferedExtractNetcdfStatusFilePathName,
    // organizer,
    // locationData,
    // listVar,
    // listTemporalCoverage,
    // listLatLonCoverage,
    // listDepthCoverage,
    // selectData);
    //
    // productDeferedExtractNetcdfThread.start();
    //
    // return productDeferedExtractNetcdfStatusUrl;
    //
    // }

    // /**
    // * Extraction differé de produit .
    // *
    // * @param listVar the list var
    // * @param selectData the select data
    // * @param organizer the organizer
    // * @param listLatLonCoverage the list lat lon coverage
    // * @param listDepthCoverage the list depth coverage
    // * @param listTemporalCoverage the list temporal coverage
    // * @param serviceName the service name
    // * @param productId the product id
    // *
    // * @return the string
    // *
    // * @throws MotuException the motu exception
    // */
    // private String productDeferedExtractNetcdf(Organizer organizer,
    // String serviceName,
    // List<String> listVar,
    // List<String> listTemporalCoverage,
    // List<String> listLatLonCoverage,
    // List<String> listDepthCoverage,
    // SelectData selectData,
    // String productId) throws MotuException {
    //
    // // Calcul d'un numéro de produit deférré à partir du temps
    // synchronized (this) {
    // long num = System.currentTimeMillis();
    // if (num == pdsNum) {
    // // Si c'est le même temps que le précédent on incrément pour en avoir un différent
    // pdsNum++;
    // } else {
    // pdsNum = num;
    // }
    // }
    // String productDeferedExtractNetcdfStatusFileName = getStatusFileName();
    //
    // String productDeferedExtractNetcdfStatusFilePathName =
    // Organizer.getMotuConfigInstance().getExtractionPath() + "/"
    // + productDeferedExtractNetcdfStatusFileName;
    // String productDeferedExtractNetcdfStatusUrl = Organizer.getMotuConfigInstance().getDownloadHttpUrl() +
    // "/"
    // + productDeferedExtractNetcdfStatusFileName;
    //
    // MotuServlet.printProductDeferedExtractNetcdfStatus(productDeferedExtractNetcdfStatusFilePathName,
    // StatusModeType.INPROGRESS, MSG_IN_PROGRESS);
    //
    // ProductDeferedExtractNetcdfThread productDeferedExtractNetcdfThread = new
    // ProductDeferedExtractNetcdfThread(
    // productDeferedExtractNetcdfStatusFilePathName,
    // organizer,
    // serviceName,
    // listVar,
    // listTemporalCoverage,
    // listLatLonCoverage,
    // listDepthCoverage,
    // selectData,
    // productId);
    //
    // productDeferedExtractNetcdfThread.start();
    //
    // return productDeferedExtractNetcdfStatusUrl;
    //
    // }

    /**
     * Gets the request priority.
     * 
     * @param request the request
     * 
     * @return the request priority
     */
    private int getRequestPriority(HttpServletRequest request) {

        short priority = getQueueServerManagement().getDefaultPriority();

        String priorityStr = request.getParameter(PARAM_PRIORITY);
        if (MotuServlet.isNullOrEmpty(priorityStr)) {
            return priority;
        }

        try {
            priority = Short.valueOf(priorityStr);
        } catch (NumberFormatException e) {
            priority = getQueueServerManagement().getDefaultPriority();
        }

        return priority;
    }

    /**
     * Gets the status file name.
     * 
     * @return the status file name
     */
    private String getStatusFileName() {
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append("pr_defered_");
        stringBuffer.append(requestManagement.generateRequestId());
        stringBuffer.append("status.xml");

        return stringBuffer.toString();

    }

    // /**
    // * Ecriture du status d'extraction différé de produit dasn un fichier texte.
    // *
    // * @param status status à ecrire
    // * @param productDeferedExtractNetcdfStatusFile fichier texte à écrire
    // *
    // * @throws IOException the IO exception
    // *
    // * @deprecated
    // */
    // static private void printProductDeferedExtractNetcdfStatus(String
    // productDeferedExtractNetcdfStatusFile, String status) throws IOException {
    // PrintWriter pw = new PrintWriter(new File(productDeferedExtractNetcdfStatusFile));
    // pw.print(status);
    // pw.close();
    // if (pw.checkError()) {
    // throw new IOException("Status write error");
    // }
    // }

    //    
    // static private void printProductDeferedExtractNetcdfStatus(StatusModeResponse statusModeResponse,
    // String outputFileName,
    // StatusModeType status,
    // String msg,
    // ErrorType errorType) throws MotuException, IOException {
    // Writer writer = new FileWriter(outputFileName);
    //
    // MotuServlet.printProductDeferedExtractNetcdfStatus(statusModeResponse, writer, status, msg, errorType);
    // }

    /**
     * Gets the temporal coverage from the request.
     * 
     * @param request servlet request
     * 
     * @return a list of temporable coverage, first start date, and then end date (they can be empty string)
     */
    private List<String> getTemporalCoverage(HttpServletRequest request) {
        String startDate = request.getParameter(PARAM_START_DATE);
        String endDate = request.getParameter(PARAM_END_DATE);
        List<String> listTemporalCoverage = new ArrayList<String>();

        if (startDate != null) {
            listTemporalCoverage.add(startDate);
        }
        if (endDate != null) {
            listTemporalCoverage.add(endDate);
        }
        return listTemporalCoverage;
    }

    /**
     * Gets the variables from the request.
     * 
     * @param request servlet request
     * 
     * @return a list of variables
     */
    private List<String> getVariables(HttpServletRequest request) {
        String[] variables = request.getParameterValues(PARAM_VARIABLE);

        List<String> listVar = new ArrayList<String>();
        if (variables != null) {
            for (String var : variables) {
                listVar.add(var);
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
     * Inits the authentification.
     * 
     * @throws ServletException the servlet exception
     */
    private void initAuthentification() throws ServletException {
        try {
            MotuConfig motuConfig = Organizer.getMotuConfigInstance();
            if (motuConfig.isUseAuthentication()) {
                authentificationProps = PropertiesUtilities.loadFromClasspath("motuUser.properties");
            }
        } catch (IOException e) {
            throw new ServletException("Authentification initialisation failure ", e);
        } catch (MotuException e) {
            throw new ServletException(String.format("Authentification initialisation failure - %s", e.notifyException()), e);
        }
    }

    /**
     * JAXB initialisation.
     * 
     * @throws ServletException the servlet exception
     */
    private void initJAXB() throws ServletException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initJAXB() - entering");
        }

        try {
            MotuServlet.jaxbContextMotuMsg = JAXBContext.newInstance(MotuMsgConstant.MOTU_MSG_SCHEMA_PACK_NAME);
        } catch (JAXBException e) {
            LOG.error("initJAXB()", e);
            throw new ServletException("Error in initJAXB ", e);

        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("initJAXB() - exiting");
        }
    }

    /**
     * Sets parameters for proxy connection if a proxy is used.
     * 
     * @throws ServletException the servlet exception
     */
    private void initProxyLogin() throws ServletException {

        try {
            // MotuConfig motuConfig = Organizer.getMotuConfigInstance();
            // if (motuConfig.isUseProxy()) {
            // String user = Organizer.getMotuConfigInstance().getProxyLogin();
            // String pwd = Organizer.getMotuConfigInstance().getProxyPwd();
            // System.setProperty("proxyHost", Organizer.getMotuConfigInstance().getProxyHost());
            // System.setProperty("proxyPort", Organizer.getMotuConfigInstance().getProxyPort());
            // if (user != null && pwd != null) {
            // if (!user.equals("") && !pwd.equals("")) {
            // Authenticator.setDefault(new SimpleAuthenticator(user, pwd));
            // }
            // }
            // }
            Organizer.initProxyLogin();
        } catch (MotuException e) {
            throw new ServletException(String.format("Proxy initialisation failure - %s", e.notifyException()), e);
        }

    }

    /**
     * Inits the request management.
     * 
     * @throws ServletException the servlet exception
     */
    private void initRequestManagement() throws ServletException {
        try {
            requestManagement = RequestManagement.getInstance();
        } catch (MotuException e) {
            if (requestManagement != null) {
                try {
                    requestManagement.shutdown();
                } catch (MotuException e1) {
                    // Do nothing
                }
                throw new ServletException(String.format("ERROR while request management initialization.\n%s", e.notifyException()), e);
            }
            return;
        }

    }

    /**
     * Initializes a new session with a new Organizer object.
     * 
     * @param session session to initialize
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     */
    private void initSession(HttpSession session) throws ServletException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initSession() - entering");
        }

        Organizer organizer;

        LOG.info(String.format("initSession - MaxInactiveInterval " + session.getMaxInactiveInterval()));

        try {
            if (session.isNew()) {
                LOG.info("initSession - session is New ");
                createOrganizer(session);
            }
            organizer = (Organizer) session.getAttribute(ORGANIZER_SESSION_ATTR);
            if (organizer == null) {
                LOG.info("initSession - session is not New but Organizer is null");
                createOrganizer(session);
            }
        } catch (Exception e) {
            LOG.error("initSession()", e);

            throw new ServletException(e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("initSession() - exiting");
        }
    }

    /**
     * Debug product donwload.
     * 
     * @param response the response
     * @param request the request
     * @param action the action
     * 
     * @return true, if is action debug
     * 
     * @throws IOException the IO exception
     */
    private boolean isActionDebug(String action, HttpServletRequest request, HttpServletResponse response) throws IOException {

        if (!action.equalsIgnoreCase("debug")) {
            return false;
        }

        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append("<html>\n");
        stringBuffer.append("<head>\n");
        stringBuffer.append("</head>\n");
        stringBuffer.append("<body>\n");

        debugRequestAllStatus(stringBuffer);
        debugPendingRequest(stringBuffer);

        stringBuffer.append("</body>\n");
        stringBuffer.append("<html>\n");

        response.setContentType(CONTENT_TYPE_HTML);

        PrintWriter out = response.getWriter();
        out.write(stringBuffer.toString());

        return true;

    }

    /**
     * Checks if is anonymous user.
     * 
     * @param userId the user id
     * @param request the request
     * 
     * @return true, if is anonymous user
     */
    private boolean isAnonymousUser(HttpServletRequest request, String userId) {
        if (MotuServlet.isNullOrEmpty(userId)) {
            return true;
        }
        boolean anonymousUser = userId.equalsIgnoreCase(MotuRequestParametersConstant.PARAM_ANONYMOUS_USER_VALUE);
        if (anonymousUser) {
            return true;
        }
        String anonymousUserAsString = request.getParameter(PARAM_ANONYMOUS);
        if (anonymousUserAsString == null) {
            return false;
        }
        anonymousUserAsString = anonymousUserAsString.trim();
        return anonymousUserAsString.equalsIgnoreCase("true") || anonymousUserAsString.equalsIgnoreCase("1");
    }

    /**
     * Checks if is authorized.
     * 
     * @param login the login
     * @param password the password
     * 
     * @return true, if is authorized
     */
    private boolean isAuthorized(String login, String password) {
        if (authentificationProps == null) {
            return true;
        }
        if (login == null || password == null) {
            return false;
        }
        String loginPassword = authentificationProps.getProperty(login);
        if (loginPassword == null) {
            return false;
        }
        if (!password.equals(loginPassword)) {
            return false;
        }
        return true;
    }

    /**
     * Checks if is batch.
     * 
     * @param request the request
     * 
     * @return true, if is batch
     */
    private boolean isBatch(HttpServletRequest request) {
        String batchAsString = request.getParameter(PARAM_BATCH);
        if (MotuServlet.isNullOrEmpty(batchAsString)) {
            return false;
        }
        batchAsString = batchAsString.trim();
        return batchAsString.equalsIgnoreCase("true") || batchAsString.equalsIgnoreCase("1");
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

    /**
     * Lists the catalog for a service name. Informations are witten if HTML format in the writer of the
     * response.
     * 
     * @param response object that contains the response the servlet sends to the client
     * @param session request sesssion
     * @param serviceName name of the service for the catalog
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     */
    private void listCatalog(String serviceName, HttpSession session, HttpServletResponse response) throws ServletException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listCatalog() - entering");
        }

        Organizer organizer = getOrganizer(session, response);

        try {
            organizer.getCatalogInformation(serviceName, response.getWriter(), Organizer.Format.HTML);
        } catch (MotuExceptionBase e) {
            LOG.error("listCatalog()", e);

            throw new ServletException(e.notifyException(), e);
        } catch (Exception e) {
            LOG.error("listCatalog()", e);

            throw new ServletException(e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("listCatalog() - exiting");
        }
    }

    /**
     * Lists metadata for a product. Informations are witten if HTML format in the writer of the response.
     * 
     * @param response object that contains the response the servlet sends to the client
     * @param session request sesssion
     * @param productId id of the product
     * @param serviceName name of the service for the product
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     */
    private void listProductMetaData(String serviceName, String productId, HttpSession session, HttpServletResponse response)
            throws ServletException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listProductMetaData() - entering");
        }

        Organizer organizer = getOrganizer(session, response);

        try {
            organizer.getProductInformation(serviceName, productId, response.getWriter(), Organizer.Format.HTML);
        } catch (MotuExceptionBase e) {
            LOG.error("listProductMetaData()", e);

            throw new ServletException(e.notifyException(), e);
        } catch (Exception e) {
            LOG.error("listProductMetaData()", e);

            throw new ServletException(e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("listProductMetaData() - exiting");
        }
    }

    /**
     * Lists the available data services (Aviso, Mercator, ....) in HTML format.
     * 
     * @param response object that contains the response the servlet sends to the client
     * @param session request sesssion
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     */
    private void listServices(HttpSession session, HttpServletResponse response) throws ServletException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listServices() - entering");
        }

        Organizer organizer = getOrganizer(session, response);

        try {
            organizer.getAvailableServices(response.getWriter(), Organizer.Format.HTML);
        } catch (MotuExceptionBase e) {
            LOG.error("listServices()", e);

            throw new ServletException(e.notifyException(), e);
        } catch (Exception e) {
            LOG.error("listServices()", e);

            throw new ServletException(e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("listServices() - exiting");
        }
    }

    /**
     * Override max pool anonymous.
     * 
     * @param request the request
     */
    private void overrideMaxPoolAnonymous(HttpServletRequest request) {

        short maxPoolAnonymousOverrided = -1;

        String maxPoolAnonymousAsString = request.getParameter(PARAM_MAX_POOL_ANONYMOUS);
        if (MotuServlet.isNullOrEmpty(maxPoolAnonymousAsString)) {
            return;
        }

        try {
            maxPoolAnonymousOverrided = Short.valueOf(maxPoolAnonymousAsString);
            getQueueServerManagement().setMaxPoolAnonymousOverrided(maxPoolAnonymousOverrided);
        } catch (NumberFormatException e) {
            // Do nothing
        }

    }

    /**
     * Override max pool authenticate.
     * 
     * @param request the request
     */
    private void overrideMaxPoolAuthenticate(HttpServletRequest request) {

        short maxPoolAuthOverrided = -1;

        String maxPoolAuthOverridedAsString = request.getParameter(PARAM_MAX_POOL_AUTHENTICATE);
        if (MotuServlet.isNullOrEmpty(maxPoolAuthOverridedAsString)) {
            return;
        }

        try {
            maxPoolAuthOverrided = Short.valueOf(maxPoolAuthOverridedAsString);
            getQueueServerManagement().setMaxPoolAnonymousOverrided(maxPoolAuthOverrided);
        } catch (NumberFormatException e) {
            // Do nothing
        }

    }

    /**
     * Product defered extract netcdf.
     * 
     * @param organizer the organizer
     * @param extractionParameters the extraction parameters
     * @param mode the mode
     * 
     * @return the status mode response
     * 
     * @throws MotuException the motu exception
     */
    private StatusModeResponse productDeferedExtractNetcdf(Organizer organizer, ExtractionParameters extractionParameters, String mode)
            throws MotuException {

        long requestId = requestManagement.generateRequestId();

        StatusModeResponse statusModeResponse = Organizer.createStatusModeResponse();
        statusModeResponse.setCode(ErrorType.OK);
        statusModeResponse.setStatus(StatusModeType.INPROGRESS);
        statusModeResponse.setMsg(MSG_IN_PROGRESS);
        statusModeResponse.setRequestId(requestId);

        requestManagement.putIfAbsentRequestStatusMap(requestId, statusModeResponse);

        ProductDeferedExtractNetcdfThread productDeferedExtractNetcdfThread = new ProductDeferedExtractNetcdfThread(
                statusModeResponse,
                organizer,
                extractionParameters);

        productDeferedExtractNetcdfThread.start();

        return statusModeResponse;

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
     * Product download.
     * 
     * @param response the response
     * @param session the session
     * @param priority the priority
     * @param extractionParameters the extraction parameters
     * @param mode the mode
     * 
     * @throws IOException the IO exception
     */
    private void productDownload(ExtractionParameters extractionParameters,
                                 String mode,
                                 int priority,
                                 HttpSession session,
                                 HttpServletResponse response) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse) - entering");
        }

        // boolean modeConsole = RunnableHttpExtraction.isModeConsole(mode);
        // boolean modeUrl = RunnableHttpExtraction.isModeUrl(mode);
        boolean modeStatus = RunnableHttpExtraction.isModeStatus(mode);
        // boolean noMode = RunnableHttpExtraction.noMode(mode);

        RunnableHttpExtraction runnableHttpExtraction = null;

        StatusModeResponse statusModeResponse = null;

        final ReentrantLock lock = new ReentrantLock();
        final Condition requestEndedCondition = lock.newCondition();

        // HttpServletResponse responseWork = response;
        //
        // if (modeStatus) {
        // responseWork = null;
        // }

        String serviceName = extractionParameters.getServiceName();
        Organizer organizer = getOrganizer(session, response);
        try {

            if (organizer.isGenericService() && !MotuServlet.isNullOrEmpty(serviceName)) {
                organizer.setCurrentService(serviceName);
            }
        } catch (MotuException e) {
            LOG.error("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse)", e);

            response.sendError(400, String.format("ERROR: %s", e.notifyException()));

            if (LOG.isDebugEnabled()) {
                LOG.debug("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse) - exiting");
            }
            return;
        }

        runnableHttpExtraction = new RunnableHttpExtraction(priority, organizer, extractionParameters, response, mode, requestEndedCondition, lock);

        // runnableHttpExtraction.lock = lock;

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
                response.setContentType(null);
                Organizer.marshallStatusModeResponse(statusModeResponse, response.getWriter());
            } else {
                // --------- wait for the end of the request -----------
                requestEndedCondition.await();
                // ------------------------------------------------------
            }
        } catch (MotuMarshallException e) {
            LOG.error("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse)", e);

            response.sendError(500, String.format("ERROR: %s", e.getMessage()));
        } catch (MotuExceptionBase e) {
            LOG.error("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse)", e);

            runnableHttpExtraction.aborted();
            // Do nothing error is in response error code
            // response.sendError(400, String.format("ERROR: %s", e.notifyException()));
        } catch (Exception e) {
            LOG.error("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse)", e);

            runnableHttpExtraction.aborted();
            // response.sendError(500, String.format("ERROR: %s", e.getMessage()));
        } finally {
            // ------------------------------------------------------
            if (lock.isLocked()) {
                lock.unlock();
            }
            // ------------------------------------------------------
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("productDownload(ExtractionParameters, String, int, HttpSession, HttpServletResponse) - exiting");
        }
    }

    /**
     * Gets th product download homepage. Informations are witten if HTML format in the writer of the
     * response.
     * 
     * @param response object that contains the response the servlet sends to the client
     * @param session request sesssion
     * @param productId id of the product
     * @param serviceName name of the service for the product
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     */
    private void productDownloadHome(String serviceName, String productId, HttpSession session, HttpServletResponse response)
            throws ServletException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("productDownloadHome() - entering");
        }

        Organizer organizer = getOrganizer(session, response);
        try {
            organizer.getProductDownloadInfo(serviceName, productId, response.getWriter(), Organizer.Format.HTML);

        } catch (MotuExceptionBase e) {
            LOG.error("productDownloadHome()", e);

            throw new ServletException(e.notifyException(), e);
        } catch (Exception e) {
            LOG.error("productDownloadHome()", e);

            throw new ServletException(e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("productDownloadHome() - exiting");
        }
    }

    // /**
    // * Inits the queue server.
    // *
    // * @throws ServletException the servlet exception
    // */
    // private void initQueueServer() throws ServletException {
    //
    // if (!useQueueServer) {
    // return;
    // }
    //
    // try {
    // queueServerManagement = QueueServerManagement.getInstance();
    // } catch (MotuException e) {
    // if (queueServerManagement != null) {
    // try {
    // queueServerManagement.shutdown();
    // } catch (MotuException e1) {
    // // Do nothing
    // }
    // throw new ServletException(String.format("ERROR while queue server initialization.\n%s",
    // e.notifyException()), e);
    // }
    // return;
    // }
    //
    // }

    /**
     * Product download no queueing.
     * 
     * @param response the response
     * @param session the session
     * @param extractionParameters the extraction parameters
     * @param mode the mode
     * 
     * @throws IOException the IO exception
     */
    private void productDownloadNoQueueing(ExtractionParameters extractionParameters, String mode, HttpSession session, HttpServletResponse response)
            throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("productDownload() - entering");
        }
        boolean modeConsole = mode.equalsIgnoreCase(PARAM_MODE_CONSOLE);
        boolean modeUrl = mode.equalsIgnoreCase(PARAM_MODE_URL);
        boolean modeStatus = mode.equalsIgnoreCase(PARAM_MODE_STATUS);

        // boolean noMode = RunnableHttpExtraction.noMode(mode);

        Organizer organizer = getOrganizer(session, response);

        try {
            if (modeStatus) {
                if (statusAsFile) {
                    String productDeferedExtractNetcdfStatusUrl;
                    productDeferedExtractNetcdfStatusUrl = productDeferedExtractNetcdfWithStatusAsFile(organizer, extractionParameters, mode);
                    response.setContentType(CONTENT_TYPE_PLAIN);
                    PrintWriter out = response.getWriter();
                    out.write(productDeferedExtractNetcdfStatusUrl);
                } else {
                    StatusModeResponse statusModeResponse = productDeferedExtractNetcdf(organizer, extractionParameters, mode);
                    response.setContentType(null);
                    Organizer.marshallStatusModeResponse(statusModeResponse, response.getWriter());
                }
            } else {
                Product product = organizer.extractData(extractionParameters);

                if (modeConsole) {
                    response.sendRedirect(product.getDownloadUrlPath());
                }
                if (modeUrl) {
                    response.setContentType(CONTENT_TYPE_PLAIN);
                    PrintWriter out = response.getWriter();
                    out.write(product.getDownloadUrlPath());
                }
            }
        } catch (MotuExceptionBase e) {
            LOG.error("productDownload()", e);
            response.sendError(400, String.format("ERROR: %s", e.notifyException()));
        } catch (Exception e) {
            LOG.error("productDownload()", e);
            response.sendError(400, String.format("ERROR: %s", e.getMessage()));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("productDownload() - exiting");
        }
    }

    /**
     * Product get time coverage.
     * 
     * @param response the response
     * @param locationData the location data
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     */
    private void productGetTimeCoverage(String locationData, HttpServletResponse response) throws ServletException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("productGetTimeCoverage(String, HttpSession, HttpServletResponse) - entering");
        }

        Organizer organizer = getOrganizer(null, response);
        try {
            organizer.getTimeCoverage(locationData, response.getWriter());
        } catch (MotuException e) {
            LOG.error("productGetTimeCoverage(String, HttpSession, HttpServletResponse)", e);
            // Do nothing error is in response code
            // response.sendError(400, String.format("ERROR: %s", e.notifyException()));
        } catch (MotuMarshallException e) {
            LOG.error("productGetTimeCoverage(String, HttpSession, HttpServletResponse)", e);
            response.sendError(500, String.format("ERROR: %s", e.getMessage()));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("productGetTimeCoverage(String, HttpSession, HttpServletResponse) - exiting");
        }
    }

    /**
     * Product get time coverage.
     * 
     * @param response the response
     * @param serviceName the service name
     * @param productId the product id
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     */
    private void productGetTimeCoverage(String serviceName, String productId, HttpServletResponse response) throws ServletException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("productGetTimeCoverage(String, String, HttpSession, HttpServletResponse) - entering");
        }

        Organizer organizer = getOrganizer(null, response);
        try {
            organizer.getTimeCoverage(serviceName, productId, response.getWriter());
        } catch (MotuException e) {
            LOG.error("productGetTimeCoverage(String, String, HttpSession, HttpServletResponse)", e);
            // Do nothing error is in response code
            // response.sendError(400, String.format("ERROR: %s", e.notifyException()));
        } catch (MotuMarshallException e) {
            LOG.error("productGetTimeCoverage(String, String, HttpSession, HttpServletResponse)", e);
            response.sendError(500, String.format("ERROR: %s", e.getMessage()));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("productGetTimeCoverage(String, String, HttpSession, HttpServletResponse) - exiting");
        }
    }

    /**
     * Refreshes the the output.
     * 
     * @param response object that contains the response the servlet sends to the client
     * @param session request sesssion
     * 
     * @throws IOException the IO exception
     * @throws ServletException the servlet exception
     */
    private void refresh(HttpSession session, HttpServletResponse response) throws ServletException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("refresh() - entering");
        }

        Organizer organizer = getOrganizer(session, response);
        try {
            organizer.refreshHTML(response.getWriter());
        } catch (MotuExceptionBase e) {
            LOG.error("refresh()", e);

            throw new ServletException(e.notifyException(), e);
        } catch (Exception e) {
            LOG.error("refresh()", e);

            throw new ServletException(e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("refresh() - exiting");
        }
    }

    /**
     * Invalidate session.
     * 
     * @param session the session
     */
    private void removeOrganizerSession(HttpSession session) {
        // To avoid to keep Organizer object in session (lot of memory allocated but
        // unused) invalidate the session, except in 'no mode'.
        if (session == null) {
            return;
        }
        session.removeAttribute(ORGANIZER_SESSION_ATTR);

    }

    /**
     * Gets the product id.
     * 
     * @param request the request
     * @param session the session
     * @param response the response
     * 
     * @return the product id
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ServletException 
     */
    protected String getProductId(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String serviceName = request.getParameter(PARAM_SERVICE);
        
        String productId = request.getParameter(MotuRequestParametersConstant.PARAM_PRODUCT);

        if ((MotuServlet.isNullOrEmpty(serviceName)) || (MotuServlet.isNullOrEmpty(productId))){
            return productId;
        }
        
        HttpSession session = getSession(request);
        
        Organizer organizer = getOrganizer(session, response);
        
        return  organizer.getDatasetIdFromURI(productId, serviceName);      
    }
    /**
     * Tests if service called is Mercator or not.
     * 
     * @return true if initialisation parameter service = mercator.
     */
    // private boolean isMercator() {
    // String paramValue = getServletConfig().getInitParameter(PARAM_SERVICE);
    // if (isNullOrEmpty(paramValue)) {
    // return false;
    // }
    // if (paramValue.equalsIgnoreCase(PARAM_SERVICE_MERCATOR)) {
    // return true;
    // }
    // return false;
    // }
    /**
     * Tests if service called is Cls or not.
     * 
     * @return true if initialisation parameter service = cls.
     */
    // private boolean isCls() {
    // String paramValue = getServletConfig().getInitParameter(PARAM_SERVICE);
    // if (isNullOrEmpty(paramValue)) {
    // return false;
    // }
    // if (paramValue.equalsIgnoreCase(PARAM_SERVICE_CLS)) {
    // return true;
    // }
    //        
    // return false;
    // }
    /**
     * Tests if service called is Aviso or not.
     * 
     * @return true if initialisation parameter service = aviso.
     */
    // private boolean isAviso() {
    // String paramValue = getServletConfig().getInitParameter(PARAM_SERVICE);
    // if (isNullOrEmpty(paramValue)) {
    // return false;
    // }
    // if (paramValue.equalsIgnoreCase(PARAM_SERVICE_AVISO_NRT)) {
    // return true;
    // }
    //        
    // return false;
    // }
    // public static void returnFile(String filename, OutputStream out) throws FileNotFoundException,
    // IOException {
    // InputStream in = null;
    // try {
    // in = new BufferedInputStream(new FileInputStream(filename));
    // byte[] buf = new byte[4 * 1024]; // 4K buffer
    // int bytesRead;
    // while ((bytesRead = in.read(buf)) != -1) {
    // out.write(buf, 0, bytesRead);
    // }
    // } finally {
    // if (in != null) {
    // in.close();
    // }
    // }
    // }
}
// CSON: MultipleStringLiterals
