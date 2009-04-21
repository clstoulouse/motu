package fr.cls.atoll.motu.web.services;

import java.net.Authenticator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.wps.ProcessletException;

import fr.cls.atoll.motu.library.configuration.MotuConfig;
import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.atoll.motu.library.intfce.SimpleAuthenticator;
import fr.cls.atoll.motu.library.queueserver.QueueServerManagement;
import fr.cls.atoll.motu.library.queueserver.RequestManagement;

/**
 * <br><br>Copyright : Copyright (c) 2009.
 * <br><br>Société : CLS (Collecte Localisation Satellites)
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-04-21 14:52:00 $
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

    /**
     * Constructeur.
     */
    public MotuOGCFrontController() {
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
    
    /** {@inheritDoc} */
    @Override
    public void destroy() {
        super.destroy();
    }

    /** {@inheritDoc} */
    @Override
    public void init(ServletConfig config) throws ServletException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("init(ServletConfig) - entering");
        }
 
        super.init(config);
        
        try {
            initProxyLogin();

            // Initialisation Queue Server
            initRequestManagement();

        } catch (ProcessletException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("END MotuWPSProcess.initProxyLogin() -- ProcessletException");
            }
        }
        

        if (LOG.isDebugEnabled()) {
            LOG.debug("init(ServletConfig) - exiting");
        }
    }
    /**
     * Sets parameters for proxy connection if a proxy is used.
     * 
     * @throws ProcessletException the processlet exception
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
     * Inits the request management.
     */
    protected void initRequestManagement() throws ProcessletException {
        try {
            requestManagement = RequestManagement.getInstance();
        } catch (MotuException e) {
            if (requestManagement != null) {
                try {
                    requestManagement.shutdown();
                } catch (MotuException e1) {
                    // Do nothing
                }
                throw new ProcessletException(String.format("ERROR while request management initialization.\n%s", e.notifyException()));
            }
            return;
        }

    }


}
