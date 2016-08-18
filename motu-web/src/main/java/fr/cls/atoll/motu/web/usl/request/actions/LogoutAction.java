package fr.cls.atoll.motu.web.usl.request.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String ACTION_NAME = "logout";

    /**
     * Constructor of the LogoutAction class.
     * 
     * @param request The logout request to manage
     * @param response The response object used to return the response of the request
     * @param session The session object of the request
     */
    public LogoutAction(String actionCode_, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super(ACTION_NAME, actionCode_, request, response, session);
    }

    /** {@inheritDoc} */
    @Override
    protected void checkHTTPParameters() throws InvalidHTTPParameterException {
        // Nothing to do. No parameter for the logout action
    }

    /** {@inheritDoc} */
    @Override
    protected void process() throws MotuException {
        getSession().invalidate();
    }
}
