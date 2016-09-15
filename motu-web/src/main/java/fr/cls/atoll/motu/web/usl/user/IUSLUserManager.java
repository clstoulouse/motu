package fr.cls.atoll.motu.web.usl.user;

import javax.servlet.http.HttpServletRequest;

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
public interface IUSLUserManager {

    boolean isUserAnonymous();

    String getUserName();

    /**
     * .
     * 
     * @param request_
     * @return
     */
    String getLoginOrUserHostname(HttpServletRequest request_);

    /**
     * .
     * 
     * @param request
     * @return
     */
    String getUserHostName(HttpServletRequest request);
}
