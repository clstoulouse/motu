package fr.cls.atoll.motu.web.usl.user;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.client.util.AssertionHolder;

import fr.cls.atoll.motu.api.message.MotuRequestParametersConstant;
import fr.cls.atoll.motu.library.cas.util.AssertionUtils;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.usl.USLManager;
import fr.cls.atoll.motu.web.usl.common.utils.HTTPUtils;

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
public class USLUserManager implements IUSLUserManager {

    /** The Constant proxyHeaders. */
    private static final List<String> PROXY_HEADERS = new ArrayList<String>();

    static {
        PROXY_HEADERS.add("x-forwarded-for");
        PROXY_HEADERS.add("HTTP_X_FORWARDED_FOR");
        PROXY_HEADERS.add("HTTP_FORWARDED");
        PROXY_HEADERS.add("HTTP_CLIENT_IP");
    }

    public USLUserManager() {
    }

    /** {@inheritDoc} */
    @Override
    public boolean isUserAnonymous() {
        return StringUtils.isNullOrEmpty(getUserName());
    }

    /** {@inheritDoc} */
    @Override
    public String getUserName() {
        return AssertionUtils.getAttributePrincipalName(AssertionHolder.getAssertion());
    }

    private String getHostFromRequestHeader(HttpServletRequest request_) {
        String hostName = request_.getRemoteAddr();
        for (String ph : PROXY_HEADERS) {
            String v = request_.getHeader(ph);
            if (v != null) {
                hostName = v;
                break;
            }
        }
        return hostName;
    }

    /**
     * Gets the forwarded for.
     * 
     * @param request the request
     * 
     * @return the forwarded for
     */
    private String getForwardedForHostnameFromRequest(HttpServletRequest request_) {
        String forwardedFor = request_.getParameter(MotuRequestParametersConstant.PARAM_FORWARDED_FOR);
        return HTTPUtils.getHostName(forwardedFor);
    }

    /**
     * Gets the user remote host.
     * 
     * @param request the request
     * 
     * @return the remote host
     */
    @Override
    public String getUserHostName(HttpServletRequest request_) {
        String hostName = getForwardedForHostnameFromRequest(request_);
        if (hostName == null || hostName.trim().length() <= 0) {
            hostName = getHostFromRequestHeader(request_);
        }
        return HTTPUtils.getHostName(hostName);
    }

    /** {@inheritDoc} */
    @Override
    public String getLoginOrUserHostname(HttpServletRequest request_) {
        String userLoginOrHostName = USLManager.getInstance().getUserManager().getUserName();
        if (userLoginOrHostName == null || userLoginOrHostName.trim().length() <= 0) {
            String userHost = getUserHostName(request_);
            userLoginOrHostName = userHost;
        }
        return userLoginOrHostName;
    }

}
