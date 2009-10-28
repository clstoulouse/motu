package fr.cls.atoll.motu.processor.wps.framework;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deegree.commons.utils.HttpUtils;
import org.deegree.services.wps.input.LiteralInput;
import org.jgrapht.DirectedGraph;

import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.atoll.motu.msg.xml.ErrorType;
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
 * @version $Revision: 1.6 $ - $Date: 2009-10-28 15:48:01 $
 */
public class WPSUtils {
    
    public static final String PROCESSLET_EXCEPTION_FORMAT_CODE = "ERROR - Code: ";
    public static final String PROCESSLET_EXCEPTION_FORMAT_MSG = ", Message: ";

    public WPSUtils() {
    }
    
    /**
     * Post.
     * 
     * @param url the url
     * @param urlFile the url file
     * 
     * @return the input stream
     * 
     * @throws MotuException the motu exception
     */
    public static InputStream post(String url, URL urlFile) throws MotuException {
        return WPSUtils.post(url, urlFile.toString());
    }    
    
    /**
     * Post.
     * 
     * @param url the url
     * @param xmlFile the xml file
     * 
     * @return the input stream
     * 
     * @throws MotuException the motu exception
     */
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
    

    /**
     * Post.
     * 
     * @param url the url
     * @param in the in
     * 
     * @return the input stream
     * 
     * @throws MotuException the motu exception
     */
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
    
    /**
     * Gets the.
     * 
     * @param url the url
     * 
     * @return the input stream
     * 
     * @throws MotuException the motu exception
     */
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
    
    public static String encodeProcessletExceptionErrorMessage(ErrorType code, String msg) {
        return encodeProcessletExceptionErrorMessage(code.toString(), msg);        
    }
    public static String encodeProcessletExceptionErrorMessage(String code, String msg) {
        return String.format(WPSUtils.PROCESSLET_EXCEPTION_FORMAT_CODE + "%s" + WPSUtils.PROCESSLET_EXCEPTION_FORMAT_MSG + "%s", code, msg);        
    }
    
    public static List<String> decodeProcessletExceptionErrorMessage(String msg) {

        List<String> result = new ArrayList<String>();

        String regExpr = "(" + WPSUtils.PROCESSLET_EXCEPTION_FORMAT_CODE + ")(.*)(" + WPSUtils.PROCESSLET_EXCEPTION_FORMAT_MSG + ")(.*)";
        
        Pattern p = Pattern.compile(regExpr);
        
        Matcher matcher = p.matcher(msg);
        boolean matchFound = matcher.find();

     // Find all matches
        if (matchFound) {
            // Get all groups for this match
            for (int i=0; i<=matcher.groupCount(); i++) {
                String groupStr = matcher.group(i);
                result.add(groupStr);
            }
        }
                
        return result;
    }
    
    public static boolean isProcessletExceptionErrorMessageEncode(String msg) {

        List<String> result = decodeProcessletExceptionErrorMessage(msg);
        
        if (result.size() != 5) {
            return false;
        }
        
        return (result.get(1).equals(WPSUtils.PROCESSLET_EXCEPTION_FORMAT_CODE) && result.get(3).equals(WPSUtils.PROCESSLET_EXCEPTION_FORMAT_MSG)); 
        
    }
    
    /**
     * Test if a string is null or empty.
     * 
     * @param value string to be tested.
     * 
     * @return true if string is null or empty, otherwise false.
     */
    public static boolean isNullOrEmpty(String value) {
        if (value == null) {
            return true;
        }
        if (value.equals("")) {
            return true;
        }
        return false;
    }

    /**
     * Checks if is null or empty.
     * 
     * @param value the value
     * 
     * @return true, if is null or empty
     */
    public static boolean isNullOrEmpty(LiteralInput value) {
        if (value == null) {
            return true;
        }

        return WPSUtils.isNullOrEmpty(value.getValue());
    }


}
