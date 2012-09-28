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
package fr.cls.atoll.motu.web.services;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.ApplicationContextFacade;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.log4j.Logger;
import org.deegree.commons.utils.HttpUtils;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.wps.ProcessletException;

import fr.cls.atoll.motu.library.cas.HttpClientCAS;
import fr.cls.atoll.motu.library.cas.util.HttpUtil;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.library.misc.queueserver.QueueServerManagement;
import fr.cls.atoll.motu.library.misc.queueserver.RequestManagement;
import fr.cls.atoll.motu.library.misc.queueserver.RunnableExtraction;
import fr.cls.atoll.motu.library.misc.utils.ConfigLoader;
import fr.cls.atoll.motu.processor.wps.WPSRequestManagement;
import fr.cls.atoll.motu.processor.wps.framework.WPSUtils;
import fr.cls.atoll.motu.web.servlet.ServletConfigAdapter;
import fr.cls.atoll.motu.web.servlet.ServletContextAdapter;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class MotuOGCFrontController extends OGCFrontController {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(MotuOGCFrontController.class);
    /**
     * .
     */
    private static final long serialVersionUID = 792206014995098390L;
  
    /** The Constant PARAM_POLLING_TIME. */
    private static final String PARAM_POLLING_TIME = "pollingTime";

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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doGet(request, response);
        try {
            Organizer.closeVFSSystemManager();
        } catch (MotuException e) {
            // Do nothing
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.GenericServlet#getServletConfig()
     */
    @Override
    public ServletConfig getServletConfig() {
        // TODO Auto-generated method stub
        return super.getServletConfig();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                        
    	super.doPost(request, response);
        try {
            Organizer.removeVFSSystemManager();
        } catch (MotuException e) {
            // Do nothing
        }
    }


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (isShuttingDown()) {
            resp.sendError(400, RunnableExtraction.SHUTDOWN_MSG);
            return;
        }
        
        enteringServiceMethod();
        
        try {
            super.service(req, resp);
        } catch (Throwable e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("service() - excetion raised", e);
            }        	
        	throw new ServletException(e);
        } finally {
            leavingServiceMethod();
        }
    }

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
                String msg = String.format("Motu OGC Servlet is shutting down - There is (are) still %d request(s) being processed", serviceCounter);
                LOG.info(msg);
            }

            if (requestManagement != null) {
                requestManagement.shutdown();
            }
          // Wait for the service methods to stop.
          while (getServiceCounter() > 0) {
              try {
                  if (LOG.isInfoEnabled()) {
                      String msg = String.format("Motu OGC Servlet is shutting down - There is (are) still %d request(s) being processed", serviceCounter);
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
     * Constructeur.
     */
    public MotuOGCFrontController() {
    }

    /** {@inheritDoc} */
    @Override
    public void init(ServletConfig config) throws ServletException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("init(ServletConfig) - entering");
        }

        // config is wrapped to allow custom resolution of deegree configuration files. This is a hack since
        // some deegree methods are private and thus can't be overriden.
        super.init(wrapServletConfig(config));

        try {
            String paramValue = getServletConfig().getInitParameter(PARAM_POLLING_TIME);
            if (!WPSUtils.isNullOrEmpty(paramValue)) {
                pollingTime = Integer.parseInt(paramValue);
            }

            initProxyLogin();
            
            MultiThreadedHttpConnectionManager connectionManager = HttpUtil.createConnectionManager();
            HttpClientCAS httpClientCAS = new HttpClientCAS(connectionManager);

            HttpUtils.setHttpClient(httpClientCAS);

            // Initialisation Queue Server
            initRequestManagement();

            initWPSRequestManagement();

        } catch (MotuExceptionBase e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("END MotuOGCFrontController.initProxyLogin() -- ProcessletException");
            }
            throw new ServletException(String.format("Error in MotuOGCFrontController#init : %s", e.notifyException()), e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("init(ServletConfig) - exiting");
        }
    }

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

    protected WPSRequestManagement wpsRequestManagement = null;

    public WPSRequestManagement getWPSRequestManagement() {
        return wpsRequestManagement;
    }

    /**
     * Sets parameters for proxy connection if a proxy is used.
     * 
     * @throws ProcessletException the processlet exception
     */
    protected void initProxyLogin() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("BEGIN MotuOGCFrontController.initProxyLogin()");
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

        if (LOG.isDebugEnabled()) {
            LOG.debug("END MotuOGCFrontController.initProxyLogin()");
        }
    }

    /**
     * Inits the request management.
     */
    protected void initRequestManagement() throws MotuException {
        try {
            requestManagement = RequestManagement.getInstance();
        } catch (MotuException e) {
            if (requestManagement != null) {
                try {
                    requestManagement.shutdown();
                } catch (MotuException e1) {
                    // Do nothing
                }
                throw e;
            }
            return;
        }

    }

    protected void initWPSRequestManagement() throws MotuException {
        wpsRequestManagement = WPSRequestManagement.getInstance();

    }

    /**
     * Method that returns an adapted version of the servlet config returned by the super method. Thus, the
     * {@link ServletContext#getRealPath(String)} is overriden to allow a nice resolution of a file among
     * external directories.
     * 
     * @return the servlet context instance of this servlet.
     */
    private ServletConfig wrapServletConfig(final ServletConfig sc) {
        return new ServletConfigAdapter(sc) {
            private ServletContextAdapter ctx = null;

            @Override
            public ServletContext getServletContext() {
                if (ctx == null) {
                    ctx = new ServletContextAdapter(super.getServletContext()) {

                        /**
                         * First try to resolve the given location as a resource (using classpath extensions
                         * if necessary). If this try fails, then let the process go on.
                         */
                        @Override
                        public String getRealPath(String name) {
                            try {
                                // try the classpath
                                URL url = ConfigLoader.getInstance().get(name);

                                if (url != null) {
                                    return url.toString();
                                }

                                // try the current context naming
                                // TODO: try to see if we can keep independence with the container
                                ApplicationContext appCtx = null;
                                if (ctx.getRootContext() instanceof ApplicationContextFacade) {

                                    Field privateStringField = ApplicationContextFacade.class.getDeclaredField("context");
                                    privateStringField.setAccessible(true);
                                    Object context = privateStringField.get(ctx.getRootContext());

                                    if ((context != null) && context instanceof ApplicationContext) {
                                        DirContext dc = ((ApplicationContext) context).getResources();

                                        Attributes atts = dc.getAttributes(name);
                                        for (NamingEnumeration e = atts.getAll(); e.hasMore();) {
                                            final Attribute a = (Attribute) e.next();

                                            if ("canonicalPath".equals(a.getID())) {
                                                String s = a.get().toString();
                                                File f = new File(s);
                                                if (f.exists()) {
                                                    return f.getAbsolutePath();
                                                }
                                            }
                                        }
                                    }
                                }

                                throw new IllegalStateException("name " + name + " not resolved on classpath. Try default (servlet) resolution.");

                            } catch (Exception e) {
                                return super.getRealPath(name);
                            }
                        }
                    };
                }
                return ctx;
            }
        };
    }
}
