package fr.cls.atoll.motu.library.misc.cas.util;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2010. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2010-03-04 16:05:15 $
 */
public class CasAuthentificationHolder {
    /**
     * ThreadLocal to hold the Assertion for Threads to access.
     */
    private static final ThreadLocal<Boolean> threadLocal = new ThreadLocal<Boolean>();

    /**
     * Retrieve the assertion from the ThreadLocal.
     */
    public static Boolean isCasAuthentification() {
        return threadLocal.get();
    }

    /**
     * Add the Assertion to the ThreadLocal.
     */
    public static void setCasAuthentification(final Boolean casAuthentification) {
        threadLocal.set(casAuthentification);
    }

    /**
     * Clear the ThreadLocal.
     */
    public static void clear() {
        threadLocal.set(null);
    }
}
