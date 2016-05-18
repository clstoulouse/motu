package fr.cls.atoll.motu.web.usl.request;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fr.cls.atoll.motu.web.usl.request.parameter.exception.InvalidHTTPParameterException;

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
public interface IUSLRequestManager {

    /**
     * .
     * 
     * @param request
     * @param response
     * @throws IOException
     * @throws InvalidHTTPParameterException
     */
    void onNewRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, InvalidHTTPParameterException;

}
