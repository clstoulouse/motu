package fr.cls.atoll.motu.web.usl.wcs;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.usl.request.USLRequestManager;
import fr.cls.atoll.motu.web.usl.request.actions.AbstractAction;
import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;
import fr.cls.atoll.motu.web.usl.wcs.request.actions.WCSGetCapabilitiesAction;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.WCSHTTPParameters;
import fr.cls.atoll.motu.web.usl.wcs.request.parameter.validator.RequestHTTPParameterValidator;

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
public class WCSRequestManager implements IWCSRequestManager {

    /**
     * {@inheritDoc}
     * 
     * @throws InvalidHTTPParameterException
     * @throws MotuException
     */
    @Override
    public void onNewRequest(HttpServletRequest request, HttpServletResponse response) throws MotuException, InvalidHTTPParameterException {
        String requestValue = WCSHTTPParameters.getRequestFromRequest(request);
        AbstractAction actionInst = null;
        try {
            if (requestValue != null) {
                switch (requestValue) {
                case WCSGetCapabilitiesAction.ACTION_NAME:
                    actionInst = new WCSGetCapabilitiesAction("101", request, response);
                }
                if (actionInst != null) {
                    actionInst.doAction();
                }
            } else {
                throw new InvalidHTTPParameterException(
                        WCSHTTPParameters.REQUEST,
                        requestValue,
                        RequestHTTPParameterValidator.getParameterBoundariesAsString());
            }
        } catch (Exception e) {
            // TODO Manage exception with WCS standard exception response
            USLRequestManager.onException(-1L, actionInst, e, response);
        }
    }

}
