package fr.cls.atoll.motu.web.usl.request.parameter;

import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.ACTION_LIST_CATALOG;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_ACTION;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_MODE;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_REQUEST_ID;

import javax.servlet.http.HttpServletRequest;

import fr.cls.atoll.motu.web.common.utils.StringUtils;

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
public class CommonHTTPParameters {

    /**
     * Gets the action.
     * 
     * @param request the request
     * 
     * @return the action
     */
    public static String getActionFromRequest(HttpServletRequest request) {
        String action = request.getParameter(PARAM_ACTION);
        if (StringUtils.isNullOrEmpty(action)) {
            action = ACTION_LIST_CATALOG;
        }

        return action;
    }

    /**
     * Gets the mode parameter from the request.
     * 
     * @param request servlet request
     * 
     * @return how to return the result (mode=console : url file, otherwhise HTML pages)
     */
    public static String getModeFromRequest(HttpServletRequest request) {
        return request.getParameter(PARAM_MODE);
    }

    /**
     * .
     * 
     * @param request
     * @return 0 if the request id has not the good format, otherwise the request Id
     */
    public static String getRequestIdFromRequest(HttpServletRequest request) {
        return request.getParameter(PARAM_REQUEST_ID);
    }

}
