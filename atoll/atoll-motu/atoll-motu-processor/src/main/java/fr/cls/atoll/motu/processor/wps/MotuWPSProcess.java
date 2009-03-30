package fr.cls.atoll.motu.processor.wps;

import java.net.Authenticator;

import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.LiteralInput;
import org.deegree.services.wps.output.LiteralOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cls.atoll.motu.library.configuration.MotuConfig;
import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.atoll.motu.library.intfce.SimpleAuthenticator;
import fr.cls.atoll.motu.msg.xml.ErrorType;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.2 $ - $Date: 2009-03-30 15:05:22 $
 */
public abstract class MotuWPSProcess implements Processlet {

    private static final Logger LOG = LoggerFactory.getLogger(MotuWPSProcess.class);

    /**
     * Constructeur.
     */
    public MotuWPSProcess() {
    }

    /** {@inheritDoc} */
    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    @Override
    public void init() {
        initProxyLogin();
    }

    /** {@inheritDoc} */
    @Override
    public void process(ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info) throws ProcessletException {
        // TODO Auto-generated method stub

    }

    /**
     * Sets parameters for proxy connection if a proxy is used.
     * 
     * @throws ServletException the servlet exception
     */
    protected void initProxyLogin() {
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
            // throw new ServletException(String.format("Proxy initialisation failure - %s",
            // e.notifyException()), e);
            if (LOG.isDebugEnabled()) {
                LOG.debug("END MotuWPSProcess.initProxyLogin() -- MotuException");
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

    protected static void setReturnCode(ErrorType code, String msg, ProcessletOutputs out) {

        LiteralOutput codeParam = (LiteralOutput) out.getParameter(MotuWPSProcess.PARAM_CODE);
        LiteralOutput msgParam = (LiteralOutput) out.getParameter(MotuWPSProcess.PARAM_MESSAGE);

        if ((codeParam != null) && (code != null)) {
            codeParam.setValue(code.toString());
        }
        if ((msgParam != null) && (msg != null)) {
            msgParam.setValue(msg);
        }

    }

    /**
     * Gets Organizer object form the HttpSession.
     * 
     * @param response the response
     * @param session that contains Organizer.
     * 
     * @return Organizer object.
     */
    protected Organizer getOrganizer(ProcessletOutputs response) {

        Organizer organizer = null;
        try {
            organizer = new Organizer();
        } catch (MotuExceptionBase e) {
            String msg = String.format("ERROR: - MotuWPSProcess.getOrganizer - Unable to create a new organiser. Native Error: %s", e
                    .notifyException());
            MotuWPSProcess.setReturnCode(ErrorType.SYSTEM, msg, response);
        }

        return organizer;
    }

    /** Url parameter name. */
    public static final String PARAM_URL = "Url";

    /** Service servlet parameter name. */
    public static final String PARAM_SERVICE = "Service";

    /** Product servlet parameter name. */
    public static final String PARAM_PRODUCT = "Product";

    public static final String PARAM_START = "Start";

    public static final String PARAM_END = "End";

    /** Process output return code parameter name. */
    public static final String PARAM_CODE = "Code";

    /** Process output message parameter name. */
    public static final String PARAM_MESSAGE = "Message";

}
