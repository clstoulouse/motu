package fr.cls.atoll.motu.web.usl.request.actions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.usl.request.parameter.CommonHTTPParameters;
import fr.cls.atoll.motu.web.usl.request.session.SessionManager;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public abstract class AbstractAuthorizedAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger();

    public AbstractAuthorizedAction(String actionName_, HttpServletRequest request_, HttpServletResponse response_) {
        super(actionName_, request_, response_, null);
    }

    public AbstractAuthorizedAction(String actionName_, HttpServletRequest request_, HttpServletResponse response_, HttpSession session_) {
        super(actionName_, request_, response_, session_);
    }

    /**
     * Checks if is authorized.
     * 
     * @return true, if is authorized
     */
    @Override
    protected boolean isAuthorized() {
        // FIXME SMA Security issue isn't it ???
        if (BLLManager.getInstance().getUserManager().getAuthenticationProps() == null) {
            return true;
        }
        String login = getLoginFromRequest();
        String password = CommonHTTPParameters.getPasswordFromRequest(getRequest());
        if (login == null || password == null) {
            return false;
        }
        String loginPassword = BLLManager.getInstance().getUserManager().getAuthenticationProps().getProperty(login);
        if (loginPassword == null) {
            return false;
        }
        if (!password.equals(loginPassword)) {
            return false;
        }
        return true;
    }

    /**
     * Check authorized.
     *
     * @param request the request
     * @param session the session
     * @param response the response
     * @return true, if check authorized
     * @throws ServletException the servlet exception
     */
    private boolean checkAuthorized() throws ServletException {
        if (BLLManager.getInstance().getUserManager().getAuthenticationProps() == null) {
            return true;
        }

        if (getSession() != null) {
            // FIXME This key should be compared to the CAS server token with a CAS validate HTTP call to CAS
            // server ???
            if (SessionManager.getInstance().getAuthorizedKey(getSession()) != null) {
                return true;
            }
        }

        if (!isAuthorized()) {
            try {
                getResponse().sendError(401, "Authentication failure");
            } catch (IOException e) {
                LOGGER.error("response sendError failed", e);
                throw new ServletException("response sendError failed", e);
            }
            return false;
        }
        SessionManager.getInstance().setAuthorizedKey(getSession(), true);
        SessionManager.getInstance().setAuthorizedUser(getSession(), getLoginFromRequest());

        return true;
    }

}
