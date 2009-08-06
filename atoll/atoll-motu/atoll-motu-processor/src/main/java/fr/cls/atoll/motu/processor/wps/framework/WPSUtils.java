package fr.cls.atoll.motu.processor.wps.framework;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.deegree.commons.utils.HttpUtils;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.atoll.motu.processor.wps.MotuWPSProcess;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-08-06 14:28:57 $
 */
public class WPSUtils {

    public WPSUtils() {
    }

    public static InputStream post(String url, String xmlFile) throws MotuException {

        if (MotuWPSProcess.isNullOrEmpty(url)) {
            throw new MotuException("WPSUtils#post - Unable to process : url is null or empty.");
        }

        InputStream in = null;
        Map<String, String> headers = new HashMap<String, String>();
        try {
            in = Organizer.getUriAsInputStream(xmlFile);
            in = HttpUtils.post(HttpUtils.STREAM, url, in, headers);

        } catch (Exception e) {
            throw new MotuException("WPSInfo - Unable to process.", e);
        }

        if (in == null) {
            throw new MotuException("WPSInfo - Unable to process : post return a null input stream.");
        }

        return in;
    }

}
