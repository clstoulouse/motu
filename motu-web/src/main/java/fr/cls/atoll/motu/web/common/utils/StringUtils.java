package fr.cls.atoll.motu.web.common.utils;

import fr.cls.atoll.motu.api.message.xml.ErrorType;

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
public class StringUtils {

    /** The last unique_ id. */
    private static long LAST_UNIQUE_ID = System.currentTimeMillis();

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().length() <= 0;
    }

    /**
     * Generate unique id.
     * 
     * @return the long
     */
    public static synchronized long generateUniqueId() {

        // Compute a unique id from datetime
        long num = System.currentTimeMillis();

        while (num <= LAST_UNIQUE_ID) {
            num++;
        }

        LAST_UNIQUE_ID = num;

        return LAST_UNIQUE_ID;
    }

    /**
     * Gets a unique file name (without path).
     * 
     * @param prefix prefix of the file name
     * @param suffix the suffix of the file name
     * 
     * @return a unique NetCdf file name based on system time.
     */
    public static String getUniqueFileName(String prefix, String suffix) {
        // Gets a temporary fle name for the file to create.
        StringBuffer stringBuffer = new StringBuffer();
        if (prefix != null) {
            stringBuffer.append(prefix);
        }

        stringBuffer.append("_");

        long numId = generateUniqueId();
        stringBuffer.append(Long.toString(numId));

        if (suffix != null) {
            stringBuffer.append(suffix);
        }
        String temp = Zip.unAccent(stringBuffer.toString());
        // replace all non-words character except '.' by "-"
        return temp.replaceAll("[\\W&&[^\\.]]", "-");
    }

    /**
     * Hack to determine the name of the catalog from the catalog file name. .
     * 
     * @param catalogName The catalog file name
     * @return a name which matches the name attribute of the dataset tag in the TDS xml file.
     */
    public static final String getDataSetName(String catalogName) {
        return catalogName.toUpperCase().replace(".XML", "");
    }

    /**
     * Return the string to log from error type and error message.
     * 
     * @param actionCode The action code
     * @param errortype The error type
     * @param message The message
     * @return the error message to log
     */
    public static String getLogMessage(String actioncode, ErrorType errortype, String message) {
        return getErrorCode(actioncode, errortype) + " : " + message;
    }

    /**
     * Return the string to log from error type and error message.
     * 
     * @param actionCode The action code
     * @param errortype The error type
     * @param message The message
     * @return the error message to log
     */
    public static String getLogMessage(ErrorType errortype, String message) {
        return errortype.value() + " : " + message;
    }

    /**
     * Return the error code string.
     * 
     * @param actioncode the action code
     * @param errortype The error type
     * @param message The message
     * @return the error message to log
     */
    public static String getErrorCode(String actionCode, ErrorType errorType) {
        return actionCode + "-" + errorType.value();
    }
}
