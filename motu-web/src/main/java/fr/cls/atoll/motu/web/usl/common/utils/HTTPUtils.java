package fr.cls.atoll.motu.web.usl.common.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletResponse;

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
public class HTTPUtils {

    /** The Constant CONTENT_TYPE_PLAIN. */
    public static final String CONTENT_TYPE_PLAIN_UTF8 = "text/plain; charset=UTF-8";

    /** The Constant CONTENT_TYPE_XML. */
    public static final String CONTENT_TYPE_XML_UTF8 = "text/xml; charset=UTF-8";

    /** The Constant CONTENT_TYPE_HTML. */
    public static final String CONTENT_TYPE_HTML_UTF8 = "text/html; charset=UTF-8";

    /**
     * Gets the ip by name.
     * 
     * @param ip the ip
     * 
     * @return the host name
     */
    public static String getHostName(String ip) {
        if (ip == null || ip.trim().length() <= 0) {
            return ip;
        }

        StringBuffer stringBuffer = new StringBuffer();
        try {
            // if there are several ip, they can be seperate by ','.
            String[] ipSplit = ip.split(",");
            for (String ipString : ipSplit) {
                stringBuffer.append(InetAddress.getByName(ipString.trim()).getHostName());
                stringBuffer.append(", ");
            }
        } catch (UnknownHostException e) {
            // Do Nothing
        }
        if (stringBuffer.length() >= 2) {
            stringBuffer.delete(stringBuffer.length() - 2, stringBuffer.length());
        }
        return stringBuffer.toString();
    }

    public static void writeHttpResponse(HttpServletResponse httpServletResponse_,
                                         String responseStr_,
                                         String responseContentType_,
                                         String[] headerMap)
            throws UnsupportedEncodingException, IOException {
        if (responseContentType_ != null) {
            httpServletResponse_.setContentType(responseContentType_);
        }
        if (headerMap != null && headerMap.length > 0) {
            for (int i = 0; i < headerMap.length; i = i + 2) {
                httpServletResponse_.setHeader(headerMap[i], headerMap[i + 1]);
            }
        }
        httpServletResponse_.getOutputStream().write(responseStr_.getBytes("UTF-8"));
    }

}
