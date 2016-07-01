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
public class TimeUtils {

    /**
     * All minutes have this many milliseconds except the last minute of the day on a day defined with a leap
     * second.
     */
    public static final long MILLISECS_PER_MINUTE = 60 * 1000;

    /** Number of milliseconds per hour, except when a leap second is inserted. */
    public static final long MILLISECS_PER_HOUR = 60 * MILLISECS_PER_MINUTE;

    /**
     * Number of leap seconds per day expect on <BR/>
     * 1. days when a leap second has been inserted, e.g. 1999 JAN 1. <BR/>
     * 2. Daylight-savings "spring forward" or "fall back" days.
     */
    public static final long MILLISECS_PER_DAY = 24 * MILLISECS_PER_HOUR;
}
