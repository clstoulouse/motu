package fr.cls.atoll.motu.web.usl.wcs.request.parameter;

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

    public static final String SERVICE = "SERVICE";
    public static final String ACCEPT_VERSIONS = "ACCEPTVERSIONS";
    public static final String REQUEST = "REQUEST";
    public static final String COVERAGE_ID = "COVERAGEID";

    public static String getServiceFromRequest(HttpServletRequest request) {
        return CommonHTTPParameters.getRequestParameterIgnoreCase(request, SERVICE);
    }

    public static String getAcceptVersionsFromRequest(HttpServletRequest request) {
        return CommonHTTPParameters.getRequestParameterIgnoreCase(request, ACCEPT_VERSIONS);
    }

    public static String getRequestFromRequest(HttpServletRequest request) {
        return CommonHTTPParameters.getRequestParameterIgnoreCase(request, "request");
    }

    public static String getCoverageIdFromRequest(HttpServletRequest request) {
        return CommonHTTPParameters.getRequestParameterIgnoreCase(request, COVERAGE_ID);
    }

}
