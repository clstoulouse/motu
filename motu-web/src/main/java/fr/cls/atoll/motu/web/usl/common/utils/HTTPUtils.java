package fr.cls.atoll.motu.web.usl.common.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

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

}
