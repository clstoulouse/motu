package fr.cls.atoll.motu.library.misc.utils;

import org.joda.time.DateTimeUtils;
import org.joda.time.Interval;
import org.joda.time.ReadableInterval;
/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class DateUtils {
    
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
