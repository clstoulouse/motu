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

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
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

    /**
     * .
     */
    private static final long serialVersionUID = 1L;

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger();

    // /*
    // * Thread d'extraction différé de produit
    // */
    //
    // /** The polling time. */
    // protected int pollingTime = 1000;
    //
    // /**
    // * Gets the polling time.
    // *
    // * @return the polling time
    // */
    // public int getPollingTime() {
    // return pollingTime;
    // }
    //
    // /**
    // * Sets the polling time.
    // *
    // * @param pollingTime the new polling time
    // */
    // public void setPollingTime(int pollingTime) {
    // this.pollingTime = pollingTime;
    // }
    //
    // /** The service counter. */
    // protected volatile int serviceCounter = 0;
    //
    // /**
    // * Entering service method.
    // */
    // protected synchronized void enteringServiceMethod() {
    // if (LOG.isDebugEnabled()) {
    // LOG.debug("enteringServiceMethod() - start");
    // }
    //
    // serviceCounter++;
    //
    // if (LOG.isDebugEnabled()) {
    // LOG.debug("enteringServiceMethod() - end");
    // }
    // }
    //
    // /**
    // * Leaving service method.
    // */
    // protected synchronized void leavingServiceMethod() {
    // if (LOG.isDebugEnabled()) {
    // LOG.debug("leavingServiceMethod() - start");
    // }
    //
    // serviceCounter--;
    //
    // if (LOG.isDebugEnabled()) {
    // LOG.debug("leavingServiceMethod() - end");
    // }
    // }
    //
    // /**
    // * Gets the service counter.
    // *
    // * @return the service counter
    // */
    // protected synchronized int getServiceCounter() {
    // return serviceCounter;
    // }
    //
    // /** The shutting down. */
    // private boolean shuttingDown = false;
    //
    // /**
    // * Sets the shutting down.
    // *
    // * @param flag the shutting down
    // */
    // protected void setShuttingDown(boolean flag) {
    // shuttingDown = flag;
    // }
    //
    // /**
    // * Checks if is shutting down.
    // *
    // * @return true, if checks if is shutting down
    // */
    // protected boolean isShuttingDown() {
    // return shuttingDown;
    // }
    //
    // /** The jcontext. */
    // private static JAXBContext jaxbContextMotuMsg = null;
    //
    // /** Logger for this class. */
    // private static final Logger LOG = Logger.getLogger(MotuServlet.class);
    //
    // /** The object factory. */
    // private static ObjectFactory objectFactory = new ObjectFactory();
    //
    // /** The Constant PARAM_USE_QUEUE_SERVER. */
    // private static final String PARAM_USE_QUEUE_SERVER = "useQueueServer";
    //
    // /** The Constant PARAM_POLLING_TIME. */
    // private static final String PARAM_POLLING_TIME = "pollingTime";
    //
    // /** The Constant serialVersionUID. */
    // private static final long serialVersionUID = -1L;

    // /** The queue server management. */
    // private QueueServerManagement queueServerManagement = null;

    // /** The resquest status map. */
    // private ConcurrentMap<Long, StatusModeResponse> resquestStatusMap = new ConcurrentHashMap<Long,
    // StatusModeResponse>();
    //
    // /** The request management. */
    // private RequestManagement requestManagement = null;
    //
    // /** The status as file. */
    // private boolean statusAsFile = false;

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
    // @Override
    // protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
    // IOException {
    //
    // // FIXME SMA : Mangage shutdown actions !
    // // if (isShuttingDown()) {
    // // resp.sendError(400, RunnableExtraction.SHUTDOWN_MSG);
    // // return;
    // // }
    // //
    // // enteringServiceMethod();
    // //
    // // try {
    // // super.service(req, resp);
    // // } finally {
    // // leavingServiceMethod();
    // // }
    // }

    // /**
    // * Ecriture du status d'extraction différé de produit dans un fichier XML.
    // *
    // * @param statusModeResponse the status mode response
    // * @param writer the writer
    // * @param status status à ecrire
    // * @param msg message à ecrire
    // * @param errorType the error type
    // */
    // static private void printProductDeferedExtractNetcdfStatus(StatusModeResponse statusModeResponse,
    // Writer writer,
    // StatusModeType status,
    // String msg,
    // ErrorType errorType) {
    // if (statusModeResponse == null) {
    // statusModeResponse = Organizer.createStatusModeResponse();
    //
    // }
    //
    // try {
    //
    // statusModeResponse.setCode(errorType);
    // statusModeResponse.setStatus(status);
    // statusModeResponse.setMsg(msg);
    //
    // Organizer.marshallStatusModeResponse(statusModeResponse, writer);
    //
    // } catch (Exception e) {
    // try {
    // Organizer.marshallStatusModeResponse(e, writer);
    // } catch (MotuMarshallException e2) {
    // LOG.error("status writing error - " + e2.notifyException(), e2);
    // }
    // }
    //
    // }
    //
    // /**
    // * Deduce service name from path.
    // *
    // * @param request the request
    // *
    // * @return the service name or empty string if not found
    // *
    // * @throws MotuException the motu exception
    // */
    // public String deduceServiceNameFromPath(HttpServletRequest request) throws MotuException {
    // if (LOG.isDebugEnabled()) {
    // LOG.debug("deduceServiceNameFromPath(HttpServletRequest) - start");
    // }
    //
    // String[] servletPathElts = request.getServletPath().split("/");
    // String groupName = "";
    // String serviceName = "";
    // for (int i = 0; i < servletPathElts.length; i++) {
    // if (!servletPathElts[i].equals("")) {
    // groupName = servletPathElts[i];
    //
    // if (LOG.isInfoEnabled()) {
    // LOG.info("deduceServiceNameFromPath(HttpServletRequest) - String groupName=" + groupName);
    // }
    //
    // break;
    // }
    // }
    //
    // if (groupName.equals("")) {
    // if (LOG.isDebugEnabled()) {
    // LOG.debug("deduceServiceNameFromPath(HttpServletRequest) - end");
    // }
    // return "";
    // }
    //
    // List<ConfigService> listConfServ = Organizer.getMotuConfigInstance().getConfigService();
    // for (ConfigService confServ : listConfServ) {
    // if (confServ.getGroup().equalsIgnoreCase(groupName) && confServ.getDefaultGroupService()) {
    // serviceName = confServ.getName();
    //
    // if (LOG.isInfoEnabled()) {
    // LOG.info("deduceServiceNameFromPath(HttpServletRequest) - A - String serviceName=" + serviceName);
    // }
    //
    // break;
    // }
    // }
    // if (serviceName.equals("")) {
    // for (ConfigService confServ : listConfServ) {
    // if (confServ.getName().equalsIgnoreCase(groupName)) {
    // serviceName = confServ.getName();
    // if (LOG.isInfoEnabled()) {
    // LOG.info("deduceServiceNameFromPath(HttpServletRequest) - B - String serviceName=" + serviceName);
    // }
    // break;
    // }
    // }
    // }
    //
    // if (LOG.isDebugEnabled()) {
    // LOG.debug("deduceServiceNameFromPath(HttpServletRequest) - end - String serviceName=" + serviceName);
    // }
    // return serviceName;
    // }
    //
    // /**
    // * Destroy.
    // */
    // @Override
    // public void destroy() {
    // if (LOG.isDebugEnabled()) {
    // LOG.debug("destroy() - start");
    // }
    //
    // try {
    // // Check to see whether there are still service methods running,
    // // and if there are, tell them to stop. */
    // if (getServiceCounter() > 0) {
    // setShuttingDown(true);
    // }
    //
    // if (LOG.isInfoEnabled()) {
    // String msg = String.format("Motu REST Servlet is shutting down - There is (are) still %d request(s)
    // being processed", serviceCounter);
    // LOG.info(msg);
    // }
    //
    // if (requestManagement != null) {
    // requestManagement.shutdown();
    // }
    //
    // // Wait for the service methods to stop.
    // while (getServiceCounter() > 0) {
    // try {
    // if (LOG.isInfoEnabled()) {
    // String msg = String.format("Servlet is shutting down - There is (are) still %d request(s) being
    // processed", serviceCounter);
    // LOG.info(msg);
    // }
    // Thread.sleep(pollingTime);
    // } catch (InterruptedException e) {
    // LOG.error("destroy()", e);
    // // Do nothing
    // }
    // }
    // } catch (MotuException e) {
    // LOG.error("destroy()", e);
    // // Do nothing
    // } finally {
    // super.destroy();
    // }
    //
    // if (LOG.isDebugEnabled()) {
    // LOG.debug("destroy() - end");
    // }
    // }

    /**
     * Inits the.
     *
     * @param servletConfig the servlet config
     * @throws ServletException the servlet exception {@inheritDoc}.
     */
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        // ManifestManagedBean.register();

        // Log initialization is done by a listener configured in web.xml.
        // LogManager.getInstance().loadConfiguration("log4j.xml");

        // // Initialisation JAXB
        // initJAXB();
        //
        // // Initialisation du proxy pour connection opendap
        // initProxyLogin();
        //
        // // Initialisation de la liste des utilisateurs autorisés
        // initAuthentication();
        //
        // String paramValue = getServletConfig().getInitParameter(PARAM_USE_QUEUE_SERVER);
        // if (!MotuServlet.isNullOrEmpty(paramValue)) {
        // RequestManagement.setUseQueueServer(Boolean.parseBoolean(paramValue));
        // }
        //
        // paramValue = getServletConfig().getInitParameter(PARAM_POLLING_TIME);
        // if (!MotuServlet.isNullOrEmpty(paramValue)) {
        // pollingTime = Integer.parseInt(paramValue);
        // }
        //
        // // Initialisation Queue Server
        // initRequestManagement();
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
        // logDebugRequestHeaders(request);

        try {
            USLManager.getInstance().getRequestManager().onNewRequest(request, response);
        } catch (InvalidHTTPParameterException e) {
            response.sendError(500, String.format("Oops, an HTTP parameter is not valid: %s", e.getMessage()));
        } catch (MotuException e) {
            LOGGER.error("Error while processing HTTP request", e);
            throw new ServletException(e);
        }

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
    // protected void execRequest(HttpServletRequest request, HttpServletResponse response) throws
    // ServletException, IOException, MotuException {
    // if (LOG.isDebugEnabled()) {
    // LOG.debug("execRequest() - entering");
    // }
    //
    // String action = getAction(request);
    // // -----------------------------------
    // // actions before check authorization and/or without Http session
    // // -----------------------------------
    // if (isActionPing(action, request, response)) {
    // // Nothing to do
    // } else if (isActionDebug(action, request, response)) {
    // // Nothing to do
    // } else if (isActionGetRequestStatus(action, request, response)) {
    // // Nothing to do
    // } else if (isActionDescribeProduct(action, request, response)) {
    // // Nothing to do
    // } else if (isActionGetTimeCoverage(action, request, response)) {
    // // Nothing to do
    // } else if (isActionGetSize(action, request, response)) {
    // // Nothing to do
    // } else if (isActionDelete(action, request, response)) {
    // // Nothing to do
    // } else if (isActionLogout(action, request, response)) {
    // // Nothing to do
    // } else {
    //
    // // -----------------------------------
    // // actions with check authorization if needed, and/or with Http session
    // // -----------------------------------
    // HttpSession session = getSession(request);
    //
    // if (!checkAuthorized(request, session, response)) {
    // LOG.debug("execRequest() - exiting");
    // return;
    // }
    //
    // // content returned is the responsibility of each action
    // // response.setContentType(CONTENT_TYPE_HTML);
    //
    // if (isActionListCatalog(action, request, session, response)) {
    // // Nothing to do
    // } else if (isActionListProductMetaData(action, request, session, response)) {
    // // Nothing to do
    // } else if (isActionListProductDownloadHome(action, request, session, response)) {
    // // Nothing to do
    // } else if (isActionProductDownload(action, request, session, response)) {
    // // Nothing to do
    // } else if (isActionListServices(action, request, session, response)) {
    // // Nothing to do
    // } else if (isActionRefresh(action, request, session, response)) {
    // // Nothing to do
    // } else if (isActionDescribeCoverage(action, request, session, response)) {
    // // Nothing to do
    // } else {
    // // No parameter or parameters doesn't match
    // execDefaultRequest(request, session, response);
    // }
    // }
    // if (LOG.isDebugEnabled()) {
    // LOG.debug("execRequest() - exiting");
    // }
    // }

}
// CSON: MultipleStringLiterals
