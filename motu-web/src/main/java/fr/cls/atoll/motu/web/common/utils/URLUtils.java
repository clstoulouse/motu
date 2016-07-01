package fr.cls.atoll.motu.web.common.utils;

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
public class URLUtils {

    public static String concatUrlPaths(String... strings) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < strings.length; i++) {
            String s = strings[i];
            sb.append(s);
            if (!s.endsWith("/") && i < (strings.length - 1)) {
                sb.append("/");
            }
        }
        return sb.toString();
    }

}
