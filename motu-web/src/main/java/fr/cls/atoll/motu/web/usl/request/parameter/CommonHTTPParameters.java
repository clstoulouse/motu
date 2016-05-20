package fr.cls.atoll.motu.web.usl.request.parameter;

import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.ACTION_LIST_CATALOG;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_ACTION;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_HIGH_LAT;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_HIGH_LON;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_HIGH_Z;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_LOW_LAT;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_LOW_LON;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_LOW_Z;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_MODE;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_PRIORITY;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_REQUEST_ID;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_SERVICE;
import static fr.cls.atoll.motu.api.message.MotuRequestParametersConstant.PARAM_VARIABLE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
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

    public static String getLatitudeLowFromRequest(HttpServletRequest request) {
        return request.getParameter(PARAM_LOW_LAT);
    }

    public static String getLatitudeHighFromRequest(HttpServletRequest request) {
        return request.getParameter(PARAM_HIGH_LAT);
    }

    public static String getLongitudeLowFromRequest(HttpServletRequest request) {
        return request.getParameter(PARAM_LOW_LON);
    }

    public static String getLongitudeHighFromRequest(HttpServletRequest request) {
        return request.getParameter(PARAM_HIGH_LON);
    }

    /**
     * .
     * 
     * @param request
     * @return
     */
    public static String getDepthLowFromRequest(HttpServletRequest request) {
        return request.getParameter(PARAM_LOW_Z);
    }

    /**
     * .
     * 
     * @param request
     * @return
     */
    public static String getDepthHighFromRequest(HttpServletRequest request) {
        return request.getParameter(PARAM_HIGH_Z);
    }

    /**
     * .
     * 
     * @param request
     * @return
     */
    public static String getStartDateFromRequest(HttpServletRequest request) {
        return request.getParameter(MotuRequestParametersConstant.PARAM_START_DATE);
    }

    /**
     * .
     * 
     * @param request
     * @return
     */
    public static String getEndDateFromRequest(HttpServletRequest request) {
        return request.getParameter(MotuRequestParametersConstant.PARAM_END_DATE);
    }

    public static String getServiceFromRequest(HttpServletRequest request) {
        return request.getParameter(PARAM_SERVICE);
    }

    public static String getDataFromParameter(HttpServletRequest request) {
        return request.getParameter(MotuRequestParametersConstant.PARAM_DATA);
    }

    public static String[] getVariablesFromParameter(HttpServletRequest request) {
        return request.getParameterValues(PARAM_VARIABLE);
    }

    public static List<String> getVariablesAsListFromParameter(HttpServletRequest request) {
        String[] variables = CommonHTTPParameters.getVariablesFromParameter(request);

        List<String> listVar = new ArrayList<String>();
        if (variables != null) {
            listVar = Arrays.asList(variables);
        }
        return listVar;
    }

    /**
     * .
     * 
     * @param request
     * @return
     */
    public static String getPriorityFromRequest(HttpServletRequest request) {
        return request.getParameter(PARAM_PRIORITY);
    }

}
