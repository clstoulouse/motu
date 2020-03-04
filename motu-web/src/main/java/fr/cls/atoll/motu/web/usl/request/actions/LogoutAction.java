package fr.cls.atoll.motu.web.usl.request.actions;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Pierre LACOSTE
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class LogoutAction extends AbstractAction {

    public static final String ACTION_NAME = "logout";
    public static final String ACTION_CODE = "008";

    /**
     * Constructor of the LogoutAction class.
     * 
     * @param request The logout request to manage
     * @param response The response object used to return the response of the request
     * @param session The session object of the request
     */
    public LogoutAction(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super(ACTION_NAME, ACTION_CODE, request, response, session);
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        // Nothing to do. No parameter for the logout action
    }

    /** {@inheritDoc} */
    @Override
    protected void process() throws MotuException {
        if (getSession() != null) {
            getSession().invalidate();
        }
        logoutFromCasSSO();
    }

    private void logoutFromCasSSO() throws MotuException {
        if (BLLManager.getInstance().getConfigManager().isCasActivated()) {
            // Redirect to logout fron CAS SSO server
            try {
                getResponse().sendRedirect(BLLManager.getInstance().getConfigManager().getCasServerUrl() + "/logout");
            } catch (IOException e) {
                throw new MotuException(ErrorType.SYSTEM, "Error while sending download redirection PARAM_MODE_CONSOLE", e);
            }
        }
    }
}
