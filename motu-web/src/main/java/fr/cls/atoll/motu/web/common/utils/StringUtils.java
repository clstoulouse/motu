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

}
