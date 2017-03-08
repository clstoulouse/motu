package fr.cls.atoll.motu.web.usl.wcs.request.parameter;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
public class WCSHTTPParameters {

    public static final String SERVICE = "service";
    public static final String ACCEPT_VERSIONS = "version";
    public static final String REQUEST = "request";
    public static final String COVERAGE_ID = "COVERAGEID";
    public static final String SUBSET = "SUBSET";
    public static final String RANGE_SUBSET = "RANGESUBSET";

    public static String getServiceFromRequest(HttpServletRequest request) {
        return CommonHTTPParameters.getRequestParameterIgnoreCase(request, SERVICE);
    }

    public static String getAcceptVersionsFromRequest(HttpServletRequest request) {
        return CommonHTTPParameters.getRequestParameterIgnoreCase(request, ACCEPT_VERSIONS);
    }

    public static String getRequestFromRequest(HttpServletRequest request) {
        return CommonHTTPParameters.getRequestParameterIgnoreCase(request, REQUEST);
    }

    public static String getCoverageIdFromRequest(HttpServletRequest request) {
        return CommonHTTPParameters.getRequestParameterIgnoreCase(request, COVERAGE_ID);
    }

    public static String[] getSubsetFromRequest(HttpServletRequest request) {
        return getRequestParametersIgnoreCase(request, SUBSET);
    }

    public static String getRangeSubsetFromRequest(HttpServletRequest request) {
        return CommonHTTPParameters.getRequestParameterIgnoreCase(request, RANGE_SUBSET);
    }

    public static String[] getRequestParametersIgnoreCase(HttpServletRequest request, String parameter) {
        String[] paramValue = null;
        if (request.getParameterMap() != null) {
            for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
                if (entry.getKey().equalsIgnoreCase(parameter)) {
                    paramValue = entry.getValue();
                    break;
                }
            }
        }
        return paramValue;
    }
}
