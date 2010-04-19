package fr.cls.atoll.motu.processor.wps.framework;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;

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

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2009. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.7 $ - $Date: 2009-10-29 10:52:04 $
 */
public class WPSUtils {

    /** The Constant PROCESSLET_EXCEPTION_FORMAT_CODE. */
    public static final String PROCESSLET_EXCEPTION_FORMAT_CODE = "ERROR - Code: ";

    /** The Constant PROCESSLET_EXCEPTION_FORMAT_MSG. */
    public static final String PROCESSLET_EXCEPTION_FORMAT_MSG = ", Message: ";

    /** The Constant PROCESSLET_EXCEPTION_INDEX_MAX. */
    public final static int PROCESSLET_EXCEPTION_INDEX_MAX = 4;

    /** The Constant PROCESSLET_EXCEPTION_FORMAT_CODE_INDEX. */
    public final static int PROCESSLET_EXCEPTION_FORMAT_CODE_INDEX = 1;

    /** The Constant PROCESSLET_EXCEPTION_MSG_VALUE_INDEX. */
    public final static int PROCESSLET_EXCEPTION_FORMAT_MSG_INDEX = 3;

    /** The Constant PROCESSLET_EXCEPTION_CODE_VALUE_INDEX. */
    public final static int PROCESSLET_EXCEPTION_CODE_VALUE_INDEX = 2;

    /** The Constant PROCESSLET_EXCEPTION_MSG_VALUE_INDEX. */
    public final static int PROCESSLET_EXCEPTION_MSG_VALUE_INDEX = WPSUtils.PROCESSLET_EXCEPTION_INDEX_MAX;

    /**
     * Instantiates a new wPS utils.
     */
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

    /**
     * Encode processlet exception error message.
     * 
     * @param code the code
     * @param msg the msg
     * 
     * @return the string
     */
    public static String encodeProcessletExceptionErrorMessage(ErrorType code, String msg) {
        return encodeProcessletExceptionErrorMessage(code.toString(), msg);
    }

    /**
     * Encode processlet exception error message.
     * 
     * @param code the code
     * @param msg the msg
     * 
     * @return the string
     */
    public static String encodeProcessletExceptionErrorMessage(String code, String msg) {
        return String.format(WPSUtils.PROCESSLET_EXCEPTION_FORMAT_CODE + "%s" + WPSUtils.PROCESSLET_EXCEPTION_FORMAT_MSG + "%s", code, msg);
    }

    /**
     * Decode processlet exception error message.
     * 
     * @param msg the msg
     * 
     * @return the list< string>
     */
    public static List<String> decodeProcessletExceptionErrorMessage(String msg) {

        List<String> result = new ArrayList<String>();

        String regExpr = "(" + WPSUtils.PROCESSLET_EXCEPTION_FORMAT_CODE + ")(.*)(" + WPSUtils.PROCESSLET_EXCEPTION_FORMAT_MSG + ")(.*)";

        Pattern p = Pattern.compile(regExpr);

        Matcher matcher = p.matcher(msg);
        boolean matchFound = matcher.find();

        // Find all matches
        if (matchFound) {
            // Get all groups for this match
            for (int i = 0; i <= matcher.groupCount(); i++) {
                String groupStr = matcher.group(i);
                result.add(groupStr);
            }
        }

        return result;
    }

    /**
     * Gets the code from processlet exception error message.
     * 
     * @param msg the msg
     * 
     * @return the code from processlet exception error message
     */
    public static String getCodeFromProcessletExceptionErrorMessage(String msg) {

        return getCodeFromProcessletExceptionErrorMessage(WPSUtils.decodeProcessletExceptionErrorMessage(msg));

    }

    /**
     * Gets the code from processlet exception error message.
     * 
     * @param decodedMsg the decoded msg
     * 
     * @return the code from processlet exception error message
     */
    public static String getCodeFromProcessletExceptionErrorMessage(List<String> decodedMsg) {

        if (decodedMsg.size() != (WPSUtils.PROCESSLET_EXCEPTION_INDEX_MAX + 1)) {
            return "";
        }

        return decodedMsg.get(WPSUtils.PROCESSLET_EXCEPTION_CODE_VALUE_INDEX);
    }

    /**
     * Gets the msg from processlet exception error message.
     * 
     * @param msg the msg
     * 
     * @return the msg from processlet exception error message
     */
    public static String getMsgFromProcessletExceptionErrorMessage(String msg) {

        return getMsgFromProcessletExceptionErrorMessage(WPSUtils.decodeProcessletExceptionErrorMessage(msg));

    }

    /**
     * Gets the msg from processlet exception error message.
     * 
     * @param decodedMsg the decoded msg
     * 
     * @return the msg from processlet exception error message
     */
    public static String getMsgFromProcessletExceptionErrorMessage(List<String> decodedMsg) {

        if (decodedMsg.size() != (WPSUtils.PROCESSLET_EXCEPTION_INDEX_MAX + 1)) {
            return "";
        }

        return decodedMsg.get(WPSUtils.PROCESSLET_EXCEPTION_MSG_VALUE_INDEX);
    }

    /**
     * Checks if is processlet exception error message encode.
     * 
     * @param msg the msg
     * 
     * @return true, if is processlet exception error message encode
     */
    public static boolean isProcessletExceptionErrorMessageEncode(String msg) {

        List<String> result = decodeProcessletExceptionErrorMessage(msg);

        if (result.size() != (WPSUtils.PROCESSLET_EXCEPTION_INDEX_MAX + 1)) {
            return false;
        }

        return (result.get(WPSUtils.PROCESSLET_EXCEPTION_FORMAT_CODE_INDEX).equals(WPSUtils.PROCESSLET_EXCEPTION_FORMAT_CODE) && result
                .get(WPSUtils.PROCESSLET_EXCEPTION_FORMAT_MSG_INDEX).equals(WPSUtils.PROCESSLET_EXCEPTION_FORMAT_MSG));

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
