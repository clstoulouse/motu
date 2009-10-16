package fr.cls.atoll.motu.processor.wps.framework;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.deegree.commons.utils.HttpUtils;
import org.jgrapht.DirectedGraph;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.atoll.motu.processor.iso19139.OperationMetadata;
import fr.cls.atoll.motu.processor.jgraht.OperationRelationshipEdge;
import fr.cls.atoll.motu.processor.wps.MotuWPSProcess;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.4 $ - $Date: 2009-10-16 13:06:54 $
 */
public class WPSUtils {

    public WPSUtils() {
    }
    
    public static InputStream post(String url, String xmlFile) throws MotuException {

        if (Organizer.isNullOrEmpty(url)) {
            throw new MotuException("WPSUtils#post - Unable to process : url is null or empty.");
        }

        InputStream in = null;
        try {
            in = Organizer.getUriAsInputStream(xmlFile);

        } catch (Exception e) {
            throw new MotuException("WPSUtils#post - Unable to process.", e);
        }

        if (in == null) {
            throw new MotuException("WPSUtils#post - Unable to process : null input stream.");
        }

        return WPSUtils.post(url, in);
    }
    

    public static InputStream post(String url, InputStream in) throws MotuException {

        if (in == null) {
            throw new MotuException("WPSUtils#post - Unable to process : null input stream.");
        }

        InputStream is = null;
        Map<String, String> headers = new HashMap<String, String>();
        try {
            is = HttpUtils.post(HttpUtils.STREAM, url, in, headers);

        } catch (Exception e) {
            throw new MotuException("WPSUtils#post - Unable to process.", e);
        }

        if (is == null) {
            throw new MotuException("WPSUtils#post - Unable to process : post return a null input stream.");
        }

        return is;
    }
    
    public static InputStream get(String url) throws MotuException {

        if (Organizer.isNullOrEmpty(url)) {
            throw new MotuException("WPSUtils#get - Unable to process : url is null or empty.");
        }

        InputStream in = null;
        Map<String, String> headers = new HashMap<String, String>();
        try {
            in = HttpUtils.get(HttpUtils.STREAM, url, headers);

        } catch (Exception e) {
            throw new MotuException("WPSUtils#get - Unable to process.", e);
        }

        if (in == null) {
            throw new MotuException("WPSUtils#get - Unable to process : get return a null input stream.");
        }

        return in;
    }

}
