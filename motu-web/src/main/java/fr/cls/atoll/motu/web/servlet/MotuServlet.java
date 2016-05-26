/* 
 * Motu, a high efficient, robust and Standard compliant Web Server for Geographic
 * Data Dissemination.
 *
 * http://cls-motu.sourceforge.net/
 *
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites) - 
 * http://www.cls.fr - and  Contributors
 *
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
package fr.cls.atoll.motu.web.servlet;

import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.ACTION_DELETE;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.ACTION_DESCRIBE_COVERAGE;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.ACTION_GET_TIME_COVERAGE;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.ACTION_LIST_CATALOG;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.ACTION_LIST_PRODUCT_METADATA;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.ACTION_LIST_SERVICES;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.ACTION_LOGOUT;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.ACTION_PRODUCT_DOWNLOADHOME;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.ACTION_REFRESH;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_DATA;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_EXTRA_METADATA;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_LANGUAGE;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_PRODUCT;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_SERVICE;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import fr.cls.atoll.motu.api.message.MotuMsgConstant;
import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.api.message.xml.ObjectFactory;
import fr.cls.atoll.motu.api.message.xml.StatusModeResponse;
import fr.cls.atoll.motu.api.message.xml.StatusModeType;
import fr.cls.atoll.motu.library.misc.configuration.ConfigService;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.misc.exception.MotuMarshallException;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.library.misc.queueserver.QueueServerManagement;
import fr.cls.atoll.motu.library.misc.queueserver.RequestManagement;
import fr.cls.atoll.motu.library.misc.utils.ManifestManagedBean;
import fr.cls.atoll.motu.web.bll.request.model.ExtractionParameters;
import fr.cls.atoll.motu.web.bll.request.queueserver.RunnableExtraction;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;
import fr.cls.atoll.motu.web.usl.USLManager;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;

/**
 * The Class MotuServlet.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class MotuServlet extends HttpServlet {

    /*
     * Thread d'extraction différé de produit
     */

    /** The polling time. */
    protected int pollingTime = 1000;

    /**
     * Gets the polling time.
     *
     * @return the polling time
     */
    public int getPollingTime() {
        return pollingTime;
    }

    /**
     * Sets the polling time.
     *
     * @param pollingTime the new polling time
     */
    public void setPollingTime(int pollingTime) {
        this.pollingTime = pollingTime;
    }

    /** The service counter. */
    protected volatile int serviceCounter = 0;

    /**
     * Entering service method.
     */
    protected synchronized void enteringServiceMethod() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("enteringServiceMethod() - start");
        }

        serviceCounter++;

        if (LOG.isDebugEnabled()) {
            LOG.debug("enteringServiceMethod() - end");
        }
    }

    /**
     * Leaving service method.
     */
    protected synchronized void leavingServiceMethod() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("leavingServiceMethod() - start");
        }

        serviceCounter--;

        if (LOG.isDebugEnabled()) {
            LOG.debug("leavingServiceMethod() - end");
        }
    }

    /**
     * Gets the service counter.
     * 
     * @return the service counter
     */
    protected synchronized int getServiceCounter() {
        return serviceCounter;
    }

    /** The shutting down. */
    private boolean shuttingDown = false;

    /**
     * Sets the shutting down.
     *
     * @param flag the shutting down
     */
    protected void setShuttingDown(boolean flag) {
        shuttingDown = flag;
    }

    /**
     * Checks if is shutting down.
     * 
     * @return true, if checks if is shutting down
     */
    protected boolean isShuttingDown() {
        return shuttingDown;
    }

    /** The jcontext. */
    private static JAXBContext jaxbContextMotuMsg = null;

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(MotuServlet.class);

    /** The object factory. */
    private static ObjectFactory objectFactory = new ObjectFactory();

    /** The Constant PARAM_USE_QUEUE_SERVER. */
    private static final String PARAM_USE_QUEUE_SERVER = "useQueueServer";

    /** The Constant PARAM_POLLING_TIME. */
    private static final String PARAM_POLLING_TIME = "pollingTime";

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -1L;

    // /** The queue server management. */
    // private QueueServerManagement queueServerManagement = null;

    // /** The resquest status map. */
    // private ConcurrentMap<Long, StatusModeResponse> resquestStatusMap = new ConcurrentHashMap<Long,
    // StatusModeResponse>();

    /** The request management. */
    private RequestManagement requestManagement = null;

    /** The status as file. */
    private boolean statusAsFile = false;

    /**
     * Default constructor.
     */
    public MotuServlet() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (isShuttingDown()) {
            resp.sendError(400, RunnableExtraction.SHUTDOWN_MSG);
            return;
        }

        enteringServiceMethod();

        try {
            super.service(req, resp);
        } finally {
            leavingServiceMethod();
        }
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
     * @param statusModeResponse the status mode response
     * @param writer the writer
     * @param status status à ecrire
     * @param msg message à ecrire
     * @param errorType the error type
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("deduceServiceNameFromPath(HttpServletRequest) - start");
        }

        String[] servletPathElts = request.getServletPath().split("/");
        String groupName = "";
        String serviceName = "";
        for (int i = 0; i < servletPathElts.length; i++) {
            if (!servletPathElts[i].equals("")) {
                groupName = servletPathElts[i];

                if (LOG.isInfoEnabled()) {
                    LOG.info("deduceServiceNameFromPath(HttpServletRequest) - String groupName=" + groupName);
                }

                break;
            }
        }

        if (groupName.equals("")) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("deduceServiceNameFromPath(HttpServletRequest) - end");
            }
            return "";
        }

        List<ConfigService> listConfServ = Organizer.getMotuConfigInstance().getConfigService();
        for (ConfigService confServ : listConfServ) {
            if (confServ.getGroup().equalsIgnoreCase(groupName) && confServ.getDefaultGroupService()) {
                serviceName = confServ.getName();

                if (LOG.isInfoEnabled()) {
                    LOG.info("deduceServiceNameFromPath(HttpServletRequest) - A -  String serviceName=" + serviceName);
                }

                break;
            }
        }
        if (serviceName.equals("")) {
            for (ConfigService confServ : listConfServ) {
                if (confServ.getName().equalsIgnoreCase(groupName)) {
                    serviceName = confServ.getName();
                    if (LOG.isInfoEnabled()) {
                        LOG.info("deduceServiceNameFromPath(HttpServletRequest) - B - String serviceName=" + serviceName);
                    }
                    break;
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("deduceServiceNameFromPath(HttpServletRequest) - end - String serviceName=" + serviceName);
        }
        return serviceName;
    }

    /**
     * Destroy.
     */
    @Override
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy() - start");
        }

        try {
            // Check to see whether there are still service methods running,
            // and if there are, tell them to stop. */
            if (getServiceCounter() > 0) {
                setShuttingDown(true);
            }

            if (LOG.isInfoEnabled()) {
                String msg = String.format("Motu REST Servlet is shutting down - There is (are) still %d request(s) being processed", serviceCounter);
                LOG.info(msg);
            }

            if (requestManagement != null) {
                requestManagement.shutdown();
            }

            // Wait for the service methods to stop.
            while (getServiceCounter() > 0) {
                try {
                    if (LOG.isInfoEnabled()) {
                        String msg = String.format("Servlet is shutting down - There is (are) still %d request(s) being processed", serviceCounter);
                        LOG.info(msg);
                    }
                    Thread.sleep(pollingTime);
                } catch (InterruptedException e) {
                    LOG.error("destroy()", e);
                    // Do nothing
                }
            }
        } catch (MotuException e) {
            LOG.error("destroy()", e);
            // Do nothing
        } finally {
            super.destroy();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy() - end");
        }
    }

    /**
     * Inits the.
     *
     * @param servletConfig the servlet config
     * @throws ServletException the servlet exception {@inheritDoc}.
     */
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        ManifestManagedBean.register();

        // Log initialization is done by a listener configured in web.xml.
        // LogManager.getInstance().loadConfiguration("log4j.xml");

        // Initialisation JAXB
        initJAXB();

        // Initialisation du proxy pour connection opendap
        initProxyLogin();

        // Initialisation de la liste des utilisateurs autorisés
        initAuthentication();

        String paramValue = getServletConfig().getInitParameter(PARAM_USE_QUEUE_SERVER);
        if (!MotuServlet.isNullOrEmpty(paramValue)) {
            RequestManagement.setUseQueueServer(Boolean.parseBoolean(paramValue));
        }

        paramValue = getServletConfig().getInitParameter(PARAM_POLLING_TIME);
        if (!MotuServlet.isNullOrEmpty(paramValue)) {
            pollingTime = Integer.parseInt(paramValue);
        }

        // Initialisation Queue Server
        initRequestManagement();
    }

    /**
     * Delete file.
     *
     * @param urls the urls to delete
     * @param response the response
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
     * @param request object that contains the request the client has made of the servlet.
     * @param response object that contains the response the servlet sends to the client
     * @throws ServletException the servlet exception
     * @throws IOException the IO exception
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logDebugRequestHeaders(request);

        try {
            USLManager.getInstance().getRequestManager().onNewRequest(request, response);
        } catch (InvalidHTTPParameterException e) {
            response.sendError(500, String.format("Oops, an HTTP parameter is not valid: %s", e.getMessage()));
        }

        // try {
        // execRequest(request, response);
        // } catch (Exception e) {
        // throw new ServletException(e);
        // }

    }

    /**
     * Handles a POST request.
     *
     * @param request object that contains the request the client has made of the servlet.
     * @param response object that contains the response the servlet sends to the client
     * @throws ServletException the servlet exception
     * @throws IOException the IO exception
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
     * @param request object that contains the request the client has made of the servlet.
     * @param session request sesssion
     * @param response object that contains the response the servlet sends to the client
     * @throws ServletException the servlet exception
     * @throws IOException the IO exception
     */
    protected void execDefaultRequest(HttpServletRequest request, HttpSession session, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType(CONTENT_TYPE_HTML);

        if (LOG.isDebugEnabled()) {
            LOG.debug("execDefaultRequest() - entering");
        }

        setLanguageParameter(request, session, response);

        try {
            if (Organizer.getMotuConfigInstance().getDefaultActionIsListServices()) {
                listServices(request, session, response);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("execDefaultRequest() - exiting");
                }
                return;
            }
        } catch (MotuExceptionBase e) {
            throw new ServletException(e.notifyException(), e);
        }

        // System.out.println("deduceServiceNameFromPath(request) :");
        // System.out.println(deduceServiceNameFromPath(request));

        // String serviceName = getServletConfig().getInitParameter(PARAM_SERVICE);
        String serviceName;
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("execDefaultRequest(HttpServletRequest, HttpSession, HttpServletResponse) - Going to deduce Service Name");
            }

            serviceName = deduceServiceNameFromPath(request);
        } catch (MotuExceptionBase e) {
            throw new ServletException(e.notifyException(), e);
        }

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
     * <li>PARAM_ACTION = ACTION_PRODUCT_DOWNLOAD & PARAM_SERVICE = Service name & PARAM_PRODCUT = Product Id
     * </li>
     * </ul>
     *
     * @param request object that contains the request the client has made of the servlet.
     * @param response object that contains the response the servlet sends to the client
     * @throws ServletException the servlet exception
     * @throws IOException the IO exception
     * @throws MotuException the motu exception
     */
    protected void execRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, MotuException {
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
        } else if (isActionDescribeProduct(action, request, response)) {
            // Nothing to do
        } else if (isActionGetTimeCoverage(action, request, response)) {
            // Nothing to do
        } else if (isActionGetSize(action, request, response)) {
            // Nothing to do
        } else if (isActionDelete(action, request, response)) {
            // Nothing to do
        } else if (isActionLogout(action, request, response)) {
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

            // content returned is the responsibility of each action
            // response.setContentType(CONTENT_TYPE_HTML);

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
            } else if (isActionDescribeCoverage(action, request, session, response)) {
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
     * Checks if is action describe product.
     *
     * @param action the action
     * @param request the request
     * @param response the response
     * @return true, if is action describe product
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ServletException the servlet exception
     * @throws MotuException the motu exception
     */
    private boolean isActionDescribeProduct(String action, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException, MotuException {

        if (!action.equalsIgnoreCase(MotuRequestParametersConstant.ACTION_DESCRIBE_PRODUCT)) {
            return false;
        }

        String serviceName = request.getParameter(MotuRequestParametersConstant.PARAM_SERVICE);
        if (MotuServlet.isNullOrEmpty(serviceName)) {
            serviceName = "";
        }

        String locationData = request.getParameter(MotuRequestParametersConstant.PARAM_DATA);

        String productId = "";
        try {
            productId = getProductIdFromParamId(request.getParameter(MotuRequestParametersConstant.PARAM_PRODUCT), request, response);
        } catch (MotuException e) {
            response.sendError(400, String.format("ERROR: '%s' ", e.notifyException()));
            return true;
        } catch (Exception e) {
            response.sendError(400, String.format("ERROR: '%s' ", e.getMessage()));
            return true;
        }

        if (MotuServlet.isNullOrEmpty(locationData) && MotuServlet.isNullOrEmpty(productId)) {
            if (LOG.isDebugEnabled()) {
                LOG.info(" empty locationData and empty productId");
                LOG.debug("isActionDescribeProduct() - exiting");
            }
            response.sendError(400,
                               String.format("ERROR: neither '%s' nor '%s' parameters are filled - Choose one of them", PARAM_DATA, PARAM_PRODUCT));
            return true;
        }

        if (!MotuServlet.isNullOrEmpty(locationData) && !MotuServlet.isNullOrEmpty(productId)) {
            if (LOG.isDebugEnabled()) {
                LOG.info(" non empty locationData and non empty productId");
                LOG.debug("isActionDescribeProduct() - exiting");
            }
            response.sendError(400,
                               String.format("ERROR: '%s' and '%s' parameters are not compatible - Choose only one of them",
                                             PARAM_DATA,
                                             PARAM_PRODUCT));
            return true;
        }

        if (MotuServlet.isNullOrEmpty(serviceName) && !MotuServlet.isNullOrEmpty(productId)) {
            if (LOG.isDebugEnabled()) {
                LOG.info("empty serviceName  and non empty productId");
                LOG.debug("isActionDescribeProduct() - exiting");
            }
            response.sendError(400,
                               String.format("ERROR: '%s' parameter is filled but '%s' is empty. You have to fill it.",
                                             PARAM_PRODUCT,
                                             PARAM_SERVICE));
            return true;
        }

        String tdsCatalogFileName = request.getParameter(MotuRequestParametersConstant.PARAM_XML_FILE);

        boolean loadExtraMetadata = isExtraMetadata(request);
        // -------------------------------------------------
        // get Time coverage
        // -------------------------------------------------
        if (!MotuServlet.isNullOrEmpty(locationData)) {
            productDescribeProduct(locationData, tdsCatalogFileName, loadExtraMetadata, response);
        } else if (!MotuServlet.isNullOrEmpty(serviceName) && !MotuServlet.isNullOrEmpty(productId)) {
            productDescribeProduct(loadExtraMetadata, serviceName, productId, response);
        }

        response.setContentType(null);

        return true;

    }

    /**
     * Checks if is action delete.
     *
     * @param action the action
     * @param request the request
     * @param response the response
     * @return true, if is action delete
     * @throws ServletException the servlet exception
     * @throws IOException the IO exception
     */
    protected boolean isActionDelete(String action, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!action.equalsIgnoreCase(ACTION_DELETE)) {
            return false;
        }

        deleteFile(getDataParams(request), response);

        return true;

    }

    /**
     * Checks if is action list get size.
     *
     * @param action the action
     * @param request the request
     * @param response the response
     * @return true, if is action list get size
     * @throws ServletException the servlet exception
     * @throws IOException the IO exception
     * @throws MotuException the motu exception
     */
    protected boolean isActionGetSize(String action, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, MotuException {
        // if (LOG.isDebugEnabled()) {
        // LOG.debug("isActionListGetSize(String, HttpServletRequest, HttpSession, HttpServletResponse) -
        // entering");
        // }
        //
        // if (!action.equalsIgnoreCase(ACTION_GET_SIZE)) {
        // if (LOG.isDebugEnabled()) {
        // LOG.debug("isActionListGetSize(String, HttpServletRequest, HttpSession, HttpServletResponse) -
        // exiting");
        // }
        // return false;
        // }

        // OutputFormat dataFormat = null;
        // try {
        // dataFormat = getDataFormat(request);
        // } catch (MotuExceptionBase e) {
        // response.sendError(400, String.format("ERROR: %s", e.notifyException()));
        // return true;
        // } catch (Exception e) {
        // response.sendError(400, String.format("ERROR: %s", e.getMessage()));
        // return true;
        // }
        // String productId = "";
        // try {
        // productId =
        // getProductIdFromParamId(request.getParameter(MotuRequestParametersConstant.PARAM_PRODUCT), request,
        // response);
        // } catch (MotuException e) {
        // response.sendError(400, String.format("ERROR: '%s' ", e.notifyException()));
        // return true;
        // } catch (Exception e) {
        // response.sendError(400, String.format("ERROR: '%s' ", e.getMessage()));
        // return true;
        // }
        //
        // ExtractionParameters extractionParameters = new ExtractionParameters(
        // request.getParameter(MotuRequestParametersConstant.PARAM_SERVICE),
        // request.getParameter(MotuRequestParametersConstant.PARAM_DATA),
        // getVariables(request),
        // getTemporalCoverage(request),
        // getGeoCoverage(request),
        // getDepthCoverage(request),
        // productId,
        // dataFormat,
        // response.getWriter(),
        // null,
        // null,
        // true);
        //
        // extractionParameters.setBatchQueue(isBatch(request));
        //
        // // Set assertion to manage CAS.
        // extractionParameters.setAssertion(AssertionHolder.getAssertion());

        // response.setContentType(null);

        getAmountDataSize(extractionParameters, response);

        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionListGetSize(String, HttpServletRequest, HttpSession, HttpServletResponse) - exiting");
        }
        return true;
    }

    /**
     * Checks if is action get time coverage.
     *
     * @param action the action
     * @param request the request
     * @param response the response
     * @return true, if is action get time coverage
     * @throws ServletException the servlet exception
     * @throws IOException the IO exception
     * @throws MotuException the motu exception
     */
    protected boolean isActionGetTimeCoverage(String action, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, MotuException {

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

        String productId = "";
        try {
            productId = getProductIdFromParamId(request.getParameter(MotuRequestParametersConstant.PARAM_PRODUCT), request, response);
        } catch (MotuException e) {
            response.sendError(400, String.format("ERROR: '%s' ", e.notifyException()));
            return true;
        } catch (Exception e) {
            response.sendError(400, String.format("ERROR: '%s' ", e.getMessage()));
            return true;
        }

        if (MotuServlet.isNullOrEmpty(locationData) && MotuServlet.isNullOrEmpty(productId)) {
            if (LOG.isDebugEnabled()) {
                LOG.info(" empty locationData and empty productId");
                LOG.debug("isActionGetTimeCoverage() - exiting");
            }
            response.sendError(400,
                               String.format("ERROR: neither '%s' nor '%s' parameters are filled - Choose one of them", PARAM_DATA, PARAM_PRODUCT));
            return true;
        }

        if (!MotuServlet.isNullOrEmpty(locationData) && !MotuServlet.isNullOrEmpty(productId)) {
            if (LOG.isDebugEnabled()) {
                LOG.info(" non empty locationData and non empty productId");
                LOG.debug("isActionGetTimeCoverage() - exiting");
            }
            response.sendError(400,
                               String.format("ERROR: '%s' and '%s' parameters are not compatible - Choose only one of them",
                                             PARAM_DATA,
                                             PARAM_PRODUCT));
            return true;
        }

        if (MotuServlet.isNullOrEmpty(serviceName) && !MotuServlet.isNullOrEmpty(productId)) {
            if (LOG.isDebugEnabled()) {
                LOG.info("empty serviceName  and non empty productId");
                LOG.debug("isActionGetTimeCoverage() - exiting");
            }
            response.sendError(400,
                               String.format("ERROR: '%s' parameter is filled but '%s' is empty. You have to fill it.",
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
     * @param action action to be executed.
     * @param request object that contains the request the client has made of the servlet.
     * @param session request sesssion
     * @param response object that contains the response the servlet sends to the client
     * @return true is request is ACTION_LIST_CATALOG and have been executed, false otherwise.
     * @throws ServletException the servlet exception
     * @throws IOException the IO exception
     */
    protected boolean isActionListCatalog(String action, HttpServletRequest request, HttpSession session, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType(CONTENT_TYPE_HTML);

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
     * @param action action to be executed.
     * @param request object that contains the request the client has made of the servlet.
     * @param session request sesssion
     * @param response object that contains the response the servlet sends to the client
     * @return true is request is ACTION_PRODUCT_DOWNLOADHOME and have been executed, false otherwise.
     * @throws ServletException the servlet exception
     * @throws IOException the IO exception
     * @throws MotuException the motu exception
     */
    protected boolean isActionListProductDownloadHome(String action, HttpServletRequest request, HttpSession session, HttpServletResponse response)
            throws ServletException, IOException, MotuException {

        response.setContentType(CONTENT_TYPE_HTML);

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
        String productId = "";
        try {
            productId = getProductIdFromParamId(request.getParameter(MotuRequestParametersConstant.PARAM_PRODUCT), request, response);
        } catch (MotuException e) {
            response.sendError(400, String.format("ERROR: '%s' ", e.notifyException()));
            return true;
        } catch (Exception e) {
            response.sendError(400, String.format("ERROR: '%s' ", e.getMessage()));
            return true;
        }
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
     * Executes the {@link MotuRequestParametersConstant#ACTION_DESCRIBE_COVERAGE} if request's parameters
     * match.
     *
     * @param action action to be executed.
     * @param request object that contains the request the client has made of the servlet.
     * @param session request session
     * @param response object that contains the response the servlet sends to the client
     * @return true is request is A{@link MotuRequestParametersConstant#ACTION_DESCRIBE_COVERAGE} and have
     *         been executed, false otherwise.
     * @throws ServletException the servlet exception
     * @throws IOException the IO exception
     * @throws MotuException the motu exception
     */
    protected boolean isActionDescribeCoverage(String action, HttpServletRequest request, HttpSession session, HttpServletResponse response)
            throws ServletException, IOException, MotuException {

        response.setContentType(CONTENT_TYPE_XML);

        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionDescribeCoverage() - entering");
        }

        if (!action.equalsIgnoreCase(ACTION_DESCRIBE_COVERAGE)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isActionDescribeCoverage() - exiting");
            }
            return false;
        }

        String serviceName = request.getParameter(PARAM_SERVICE);
        if (MotuServlet.isNullOrEmpty(serviceName)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isActionDescribeCoverage() - exiting");
            }
            return false;
        }
        String productId = "";
        try {
            productId = getProductIdFromParamId(request.getParameter(MotuRequestParametersConstant.PARAM_DATASET_ID), request, response);
        } catch (MotuException e) {
            response.sendError(400, String.format("ERROR: '%s' ", e.notifyException()));
            return true;
        } catch (Exception e) {
            response.sendError(400, String.format("ERROR: '%s' ", e.getMessage()));
            return true;
        }
        if (MotuServlet.isNullOrEmpty(productId)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isActionDescribeCoverage() - exiting");
            }
            return false;
        }

        setLanguageParameter(request, session, response);
        describeCoverage(serviceName, productId, session, response);

        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionDescribeCoverage() - exiting");
        }
        return true;
    }

    /**
     * Executes the ACTION_LIST_PRODUCT_METADATA if request's parameters match.
     *
     * @param action action to be executed.
     * @param request object that contains the request the client has made of the servlet.
     * @param session request sesssion
     * @param response object that contains the response the servlet sends to the client
     * @return true is request is ACTION_LIST_PRODUCT_METADATA and have been executed, false otherwise.
     * @throws ServletException the servlet exception
     * @throws IOException the IO exception
     * @throws MotuException the motu exception
     */
    protected boolean isActionListProductMetaData(String action, HttpServletRequest request, HttpSession session, HttpServletResponse response)
            throws ServletException, IOException, MotuException {
        response.setContentType(CONTENT_TYPE_HTML);

        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionListProductMetaData() - entering");
        }

        if (!action.equalsIgnoreCase(ACTION_LIST_PRODUCT_METADATA)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isActionListProductMetaData() - exiting");
            }
            return false;
        }

        OutputFormat responseFormat = getResponseFormat(request);
        setResponseContentType(responseFormat, response);

        String serviceName = request.getParameter(PARAM_SERVICE);
        if (MotuServlet.isNullOrEmpty(serviceName)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isActionListProductMetaData() - exiting");
            }
            return false;
        }

        String productId = "";
        try {
            productId = getProductIdFromParamId(request.getParameter(MotuRequestParametersConstant.PARAM_PRODUCT), request, response);
        } catch (MotuException e) {
            response.sendError(400, String.format("ERROR: '%s' ", e.notifyException()));
            return true;
        } catch (Exception e) {
            response.sendError(400, String.format("ERROR: '%s' ", e.getMessage()));
            return true;
        }

        if (MotuServlet.isNullOrEmpty(productId)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isActionListProductMetaData() - exiting");
            }
            return false;
        }

        setLanguageParameter(request, session, response);
        listProductMetaData(serviceName, productId, responseFormat, session, response);

        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionListProductMetaData() - exiting");
        }
        return true;
    }

    /**
     * Executes the ACTION_LIST_SERVICES if request's parameters match.
     *
     * @param action action to be executed.
     * @param request object that contains the request the client has made of the servlet.
     * @param session request sesssion
     * @param response object that contains the response the servlet sends to the client
     * @return true is request is ACTION_LIST_SERVICES and have been executed, false otherwise.
     * @throws ServletException the servlet exception
     * @throws IOException the IO exception
     */
    protected boolean isActionListServices(String action, HttpServletRequest request, HttpSession session, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType(CONTENT_TYPE_HTML);

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
        listServices(request, session, response);

        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionListServices() - exiting");
        }
        return true;
    }

    /**
     * Executes the ACTION_LOGOUT if request's parameters match.
     *
     * @param action action to be executed.
     * @param request object that contains the request the client has made of the servlet.
     * @param response object that contains the response the servlet sends to the client
     * @return true is request is ACTION_LOGOUT and have been executed, false otherwise.
     * @throws ServletException the servlet exception
     * @throws IOException the IO exception
     */
    protected boolean isActionLogout(String action, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionLogout() - entering");
        }

        if (!action.equalsIgnoreCase(ACTION_LOGOUT)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("isActionLogout() - exiting");
            }
            return false;
        }

        // Invalidate session
        request.getSession().invalidate();

        if (LOG.isDebugEnabled()) {
            LOG.debug("isActionLogout() - exiting");
        }
        return true;
    }

    /**
     * Executes the ACTION_REFRESH if request's parameters match.
     *
     * @param action action to be executed.
     * @param request object that contains the request the client has made of the servlet.
     * @param session request sesssion
     * @param response object that contains the response the servlet sends to the client
     * @return true is request is ACTION_REFRESH and have been executed, false otherwise.
     * @throws ServletException the servlet exception
     * @throws IOException the IO exception
     */
    protected boolean isActionRefresh(String action, HttpServletRequest request, HttpSession session, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType(CONTENT_TYPE_HTML);

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
     * Creates a new Organizer for a session.
     *
     * @param session session in which to create Oragnizer
     * @throws ServletException the servlet exception
     * @throws IOException the IO exception
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
     * Debug request headers.
     * 
     * @param request the request
     */
    private void logDebugRequestHeaders(HttpServletRequest request) {
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
     * Gets the amount data size.
     *
     * @param extractionParameters the extraction parameters
     * @param response the response
     * @return the amount data size
     * @throws ServletException the servlet exception
     * @throws IOException the IO exception
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
     * Gets the catalog type params.
     *
     * @param request the request
     * @return the catalog type params
     * @throws MotuException the motu exception
     */
    private List<CatalogData.CatalogType> getCatalogTypeParams(HttpServletRequest request) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCatalogTypeParams(HttpServletRequest) - start");
        }

        String[] catalogTypes = request.getParameterValues(MotuRequestParametersConstant.PARAM_CATALOG_TYPE);
        List<CatalogData.CatalogType> listCatalogtype = new ArrayList<CatalogData.CatalogType>();
        if (catalogTypes != null) {
            if (catalogTypes.length > 0) {
                for (String catalogType : catalogTypes) {
                    try {
                        listCatalogtype.add(CatalogData.CatalogType.valueOf(catalogType));
                    } catch (IllegalArgumentException e) {
                        throw new MotuException(
                                String.format("Parameter '%s': invalid value '%s' - Valid values are : %s",
                                              MotuRequestParametersConstant.PARAM_CATALOG_TYPE,
                                              catalogType,
                                              CatalogData.CatalogType.valuesToString()),
                                e);
                    }
                }
            }
        }
        // else {
        // //listCatalogtype.add(CatalogData.CatalogType.getDefault());
        // }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getCatalogTypeParams(HttpServletRequest) - end");
        }
        return listCatalogtype;

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
    // OutputFormat.NETCDF);
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
    // OutputFormat.NETCDF,
    // response.getWriter(),
    // OutputFormat.HTML);
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
     * Gets the queue server management.
     * 
     * @return the queue server management
     */
    private QueueServerManagement getQueueServerManagement() {
        return requestManagement.getQueueServerManagement();

    };

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
     * Sets the response content type.
     *
     * @param format the format
     * @param response the response
     * @throws MotuException the motu exception
     */
    private void setResponseContentType(OutputFormat format, HttpServletResponse response) throws MotuException {

        switch (format) {
        case HTML:
            response.setContentType(CONTENT_TYPE_HTML);
            break;
        case XML:
            response.setContentType(CONTENT_TYPE_XML);
            break;
        default:
            response.setContentType(CONTENT_TYPE_PLAIN);
            break;
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
     * Checks if is extra metadata.
     * 
     * @param request the request
     * 
     * @return true, if is extra metadata
     */
    private boolean isExtraMetadata(HttpServletRequest request) {
        String extraMetadataAsString = request.getParameter(PARAM_EXTRA_METADATA);
        if (MotuServlet.isNullOrEmpty(extraMetadataAsString)) {
            return true;
        }
        extraMetadataAsString = extraMetadataAsString.trim();
        return extraMetadataAsString.equalsIgnoreCase("true") || extraMetadataAsString.equalsIgnoreCase("1");
    }

    /**
     * Lists the catalog for a service name. Informations are witten if HTML format in the writer of the
     * response.
     *
     * @param serviceName name of the service for the catalog
     * @param session request sesssion
     * @param response object that contains the response the servlet sends to the client
     * @throws ServletException the servlet exception
     * @throws IOException the IO exception
     */
    private void listCatalog(String serviceName, HttpSession session, HttpServletResponse response) throws ServletException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listCatalog() - entering");
        }

        Organizer organizer = getOrganizer(session, response);

        try {
            organizer.getCatalogInformation(serviceName, response.getWriter(), OutputFormat.HTML);
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
     * @param serviceName name of the service for the product
     * @param productId id of the product
     * @param responseFormat the response format
     * @param session request sesssion
     * @param response object that contains the response the servlet sends to the client
     * @throws ServletException the servlet exception
     * @throws IOException the IO exception
     */
    private void listProductMetaData(String serviceName,
                                     String productId,
                                     OutputFormat responseFormat,
                                     HttpSession session,
                                     HttpServletResponse response) throws ServletException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listProductMetaData() - entering");
        }

        Organizer organizer = getOrganizer(session, response);

        try {
            organizer.getProductInformation(serviceName, productId, response.getWriter(), responseFormat);
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
     * @param request the request
     * @param session request sesssion
     * @param response object that contains the response the servlet sends to the client
     * @throws ServletException the servlet exception
     * @throws IOException the IO exception
     */
    private void listServices(HttpServletRequest request, HttpSession session, HttpServletResponse response) throws ServletException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listServices() - entering");
        }

        Organizer organizer = getOrganizer(session, response);

        try {
            List<CatalogData.CatalogType> listCatalogType = getCatalogTypeParams(request);

            organizer.getAvailableServices(response.getWriter(), OutputFormat.HTML, listCatalogType);
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
     * Gets th product download homepage. Informations are witten if HTML format in the writer of the
     * response.
     *
     * @param serviceName name of the service for the product
     * @param productId id of the product
     * @param session request sesssion
     * @param response object that contains the response the servlet sends to the client
     * @throws ServletException the servlet exception
     * @throws IOException the IO exception
     */
    private void productDownloadHome(String serviceName, String productId, HttpSession session, HttpServletResponse response)
            throws ServletException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("productDownloadHome() - entering");
        }

        Organizer organizer = getOrganizer(session, response);
        try {
            organizer.getProductDownloadInfo(serviceName, productId, response.getWriter(), OutputFormat.HTML);

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

    /**
     * Gets the coverage description of a product. Informations are witten if XML format in the writer of the
     * response.
     *
     * @param serviceName name of the service for the product
     * @param productId id of the product
     * @param session request sesssion
     * @param response object that contains the response the servlet sends to the client
     * @throws ServletException the servlet exception
     * @throws IOException the IO exception
     */
    private void describeCoverage(String serviceName, String productId, HttpSession session, HttpServletResponse response)
            throws ServletException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("describeCoverage() - entering");
        }

        Organizer organizer = getOrganizer(session, response);
        try {
            organizer.getProductDownloadInfo(serviceName, productId, response.getWriter(), OutputFormat.XML);

        } catch (MotuExceptionBase e) {
            LOG.error("describeCoverage()", e);

            throw new ServletException(e.notifyException(), e);
        } catch (Exception e) {
            LOG.error("describeCoverage()", e);

            throw new ServletException(e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("describeCoverage() - exiting");
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
     * Product describe product.
     *
     * @param locationData the location data
     * @param tdsCatalogFileName the tds catalog file name
     * @param loadExtraMetadata the load extra metadata
     * @param response the response
     * @throws ServletException the servlet exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void productDescribeProduct(String locationData, String tdsCatalogFileName, boolean loadExtraMetadata, HttpServletResponse response)
            throws ServletException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("productDescribeProduct(String, String, HttpServletResponse) - entering");
        }

        Organizer organizer = getOrganizer(null, response);
        try {

            organizer.getProductMetadataInfo(locationData, tdsCatalogFileName, loadExtraMetadata, response.getWriter());

        } catch (MotuMarshallException e) {
            LOG.error("productDescribeProduct(String, String, HttpServletResponse)", e);

            response.sendError(500, String.format("ERROR: %s", e.getMessage()));
        } catch (MotuExceptionBase e) {
            LOG.error("productDescribeProduct(String, String, HttpServletResponse)", e);

            // Do nothing error is in response code
            // response.sendError(400, String.format("ERROR: %s", e.notifyException()));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("productDescribeProduct(String, String, HttpServletResponse) - exiting");
        }
    }

    /**
     * Product describe product.
     *
     * @param loadExtraMetadata the load extra metadata
     * @param serviceName the service name
     * @param productId the product id
     * @param response the response
     * @throws ServletException the servlet exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void productDescribeProduct(boolean loadExtraMetadata, String serviceName, String productId, HttpServletResponse response)
            throws ServletException, IOException {

        Organizer organizer = getOrganizer(null, response);
        try {
            organizer.getProductMetadataInfo(response.getWriter(), serviceName, productId, loadExtraMetadata);
        } catch (MotuMarshallException e) {
            response.sendError(500, String.format("ERROR: %s", e.getMessage()));
        } catch (MotuExceptionBase e) {
            // Do nothing error is in response code
            // response.sendError(400, String.format("ERROR: %s", e.notifyException()));
        }

    }

    /**
     * Product get time coverage.
     *
     * @param locationData the location data
     * @param response the response
     * @throws ServletException the servlet exception
     * @throws IOException the IO exception
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
     * @param serviceName the service name
     * @param productId the product id
     * @param response the response
     * @throws ServletException the servlet exception
     * @throws IOException the IO exception
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
     * @param session request sesssion
     * @param response object that contains the response the servlet sends to the client
     * @throws ServletException the servlet exception
     * @throws IOException the IO exception
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
