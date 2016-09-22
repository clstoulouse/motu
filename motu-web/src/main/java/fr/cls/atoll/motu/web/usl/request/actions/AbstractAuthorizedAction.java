package fr.cls.atoll.motu.web.usl.request.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.usl.request.parameter.CommonHTTPParameters;

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

    public AbstractAuthorizedAction(String actionName_, String actionCode_, HttpServletRequest request_, HttpServletResponse response_) {
        super(actionName_, actionCode_, request_, response_, null);
    }

    public AbstractAuthorizedAction(
        String actionName_,
        String actionCode_,
        HttpServletRequest request_,
        HttpServletResponse response_,
        HttpSession session_) {
        super(actionName_, actionCode_, request_, response_, session_);
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

}
