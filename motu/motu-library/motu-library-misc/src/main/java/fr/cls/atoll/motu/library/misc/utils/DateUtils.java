package fr.cls.atoll.motu.library.misc.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.DateTimeUtils;
import org.joda.time.Interval;
import org.joda.time.ReadableInterval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;
/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class DateUtils {
    
    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(DateUtils.class);

    /** The Constant DATETIME_PATTERN1. */
    public static final String DATETIME_PATTERN1 = "yyyy-MM-dd";

    /** The Constant DATETIME_PATTERN2. */
    public static final String DATETIME_PATTERN2 = "yyyy-MM-dd'T'HH:mm:ss";

    /** The Constant DATETIME_PATTERN3. */
    public static final String DATETIME_PATTERN3 = "yyyy-MM-dd' 'HH:mm:ss";

    /** The Constant DATETIME_PATTERN4. */
    public static final String DATETIME_PATTERN4 = "yyyy-MM-dd' 'HH:mm:ss.SSSZZ";

    /** The Constant DATETIME_PATTERN5. */
    public static final String DATETIME_PATTERN5 = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";

    /** The Constant DATETIME_FORMATTERS. */
    public static final Map<String, DateTimeFormatter> DATETIME_FORMATTERS = new HashMap<String, DateTimeFormatter>();

    public static final String PERIOD_PATTERN_ISO_STANDARD = "PyYmMwWdDThHmMsS";
    public static final String PERIOD_PATTERN_ISO_ALTERNATE = "PyyyymmddThhmmss";
    public static final String PERIOD_PATTERN_ISO_ALTERNATE_WITH_WEEKS = "PyyyyWwwddThhmmss";
    public static final String PERIOD_PATTERN_ISO_ALTERNATE_EXTENDED = "Pyyyy-mm-ddThh:mm:ss";
    public static final String PERIOD_PATTERN_ISO_ALTERNATE_EXTENDED_WITH_WEEKS = "Pyyyy-Www-ddThh:mm:ss";

    /** The Constant PERIOD_FORMATTERS. */
    public static final Map<String, PeriodFormatter> PERIOD_FORMATTERS = new HashMap<String, PeriodFormatter>();

    static {
        DATETIME_FORMATTERS.put(DATETIME_PATTERN1, DateTimeFormat.forPattern(DATETIME_PATTERN1));
        DATETIME_FORMATTERS.put(DATETIME_PATTERN2, DateTimeFormat.forPattern(DATETIME_PATTERN2));
        DATETIME_FORMATTERS.put(DATETIME_PATTERN3, DateTimeFormat.forPattern(DATETIME_PATTERN3));
        DATETIME_FORMATTERS.put(DATETIME_PATTERN4, DateTimeFormat.forPattern(DATETIME_PATTERN4));
        DATETIME_FORMATTERS.put(DATETIME_PATTERN5, DateTimeFormat.forPattern(DATETIME_PATTERN5));

        PERIOD_FORMATTERS.put(PERIOD_PATTERN_ISO_STANDARD, ISOPeriodFormat.standard());
        PERIOD_FORMATTERS.put(PERIOD_PATTERN_ISO_ALTERNATE, ISOPeriodFormat.alternate());
        PERIOD_FORMATTERS.put(PERIOD_PATTERN_ISO_ALTERNATE_WITH_WEEKS, ISOPeriodFormat.alternateWithWeeks());
        PERIOD_FORMATTERS.put(PERIOD_PATTERN_ISO_ALTERNATE_EXTENDED, ISOPeriodFormat.alternateExtended());
        PERIOD_FORMATTERS.put(PERIOD_PATTERN_ISO_ALTERNATE_EXTENDED_WITH_WEEKS, ISOPeriodFormat.alternateExtendedWithWeeks());

    }    
    /**
     * Does a time interval contain the specified millisecond instant.
     *
     * @param interval the interval
     * @param millisInstant the millis instant
     * @return true, if successful
     */
    public static boolean contains(ReadableInterval interval, long millisInstant) {
        long thisStart = interval.getStartMillis();
        long thisEnd = interval.getEndMillis();
        return (millisInstant >= thisStart && millisInstant <= thisEnd);
    }

    /**
     * Does a time interval contain a specified time interval.
     *
     * @param interval the interval to check
     * @param intervalCompareTo the interval to compare to
     * @return true if this time ' intervalCompareTo' contains  'intervalCompareTo'
     */
    public static boolean contains(ReadableInterval interval, ReadableInterval intervalCompareTo) {
        if (intervalCompareTo == null) {
            return DateUtils.containsNow(intervalCompareTo);
        }
        long otherStart = intervalCompareTo.getStartMillis();
        long otherEnd = intervalCompareTo.getEndMillis();
        long thisStart = interval.getStartMillis();
        long thisEnd = interval.getEndMillis();
        return (thisStart <= otherStart && otherStart <= thisEnd && otherEnd <= thisEnd);
    }
    
    /**
     * Does this time interval contain the current instant.
     *
     * @param interval the interval
     * @return true, if successful
     */
    public static boolean containsNow(ReadableInterval interval) {
        return DateUtils.contains(interval, DateTimeUtils.currentTimeMillis());
    }

    /**
     * Does a time interval intersect another time interval.
     * <p>
     *
     * @param interval1 the interval1
     * @param interval2 the interval2
     * @return true if the time intervals intersect
     */
    public static boolean intersects(ReadableInterval interval1, ReadableInterval interval2) {

        if (interval1 == null) {
            return false;
        }
        if (interval2 == null) {
            return false;
        }
        long thisStart = interval1.getStartMillis();
        long thisEnd = interval1.getEndMillis();
        long otherStart = interval2.getStartMillis();
        long otherEnd = interval2.getEndMillis();
        return (thisStart <= otherEnd && otherStart <= thisEnd);
    }
    
    public static Interval intersect(ReadableInterval interval1, ReadableInterval interval2) {
        if (DateUtils.intersects(interval1, interval2) == false) {
            return null;
        }
        long start = Math.max(interval1.getStartMillis(), interval2.getStartMillis());
        long end = Math.min(interval1.getEndMillis(), interval2.getEndMillis());
        return new Interval(start, end);
    }


}
