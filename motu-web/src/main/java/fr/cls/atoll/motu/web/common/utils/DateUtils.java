package fr.cls.atoll.motu.web.common.utils;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.datetime.FastDateFormat;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.ReadableInterval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.library.converter.exception.MotuConverterException;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDateException;
import ucar.nc2.units.DateUnit;

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
public class DateUtils {

    /** Logger for this class. */
    private static final Logger LOG = LogManager.getLogger();

    /** The Constant DATETIME_FORMATTERS. */
    public static final Map<String, FastDateFormat> DATETIME_FORMATTERS = new HashMap<>();
    public static final Map<String, DateTimeFormatter> JODA_DATETIME_FORMATTERS = new HashMap<>();

    public static final String PERIOD_PATTERN_ISO_STANDARD = "PyYmMwWdDThHmMsS";
    public static final String PERIOD_PATTERN_ISO_ALTERNATE = "PyyyymmddThhmmss";
    public static final String PERIOD_PATTERN_ISO_ALTERNATE_WITH_WEEKS = "PyyyyWwwddThhmmss";
    public static final String PERIOD_PATTERN_ISO_ALTERNATE_EXTENDED = "Pyyyy-mm-ddThh:mm:ss";
    public static final String PERIOD_PATTERN_ISO_ALTERNATE_EXTENDED_WITH_WEEKS = "Pyyyy-Www-ddThh:mm:ss";

    /** The Constant PERIOD_FORMATTERS. */
    public static final Map<String, PeriodFormatter> PERIOD_FORMATTERS = new HashMap<>();

    public static final long ONE_SECOND_IN_MILLI = 1000L;
    public static final long ONE_MINUTE_IN_MILLI = 60 * ONE_SECOND_IN_MILLI;
    public static final long ONE_HOUR_IN_MILLI = 60 * ONE_MINUTE_IN_MILLI;
    public static final long ONE_DAY_IN_MILLI = 24 * ONE_HOUR_IN_MILLI;
    public static final long ONE_MONTH_IN_MILLI = 30 * ONE_DAY_IN_MILLI;
    public static final long ONE_YEAR_IN_MILLI = 365 * ONE_DAY_IN_MILLI;

    /** Date format. */
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    /** Date/time format. */
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DATETIME_MILLIS_EU_PATTERN = "yyyy-MM-dd HH:mm:ss,SSS";
    public static final String DATETIME_MILLIS_PATTERN = "yyyy-MM-dd' 'HH:mm:ss.SSSZZ";
    public static final String DATETIME_T_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DATETIME_T_MILLIS_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";
    public static final String DATETIME_T_MILLIS_EU_PATTERN = "yyyy-MM-dd'T'HH:mm:ss,SSS";
    public static final TimeZone GMT_TIMEZONE = TimeZone.getTimeZone("GMT");
    public static final TimeZone UTC_TIMEZONE = TimeZone.getTimeZone(ZoneOffset.UTC);

    /** Date format with time (DATETIME_FORMAT). */
    public static final FastDateFormat DATETIME_TO_STRING_DEFAULT = FastDateFormat.getInstance(DATETIME_PATTERN, GMT_TIMEZONE);
    /** Date format without time (DATE_FORMAT). */
    public static final FastDateFormat DATE_TO_STRING_DEFAULT = FastDateFormat.getInstance(DATE_PATTERN, GMT_TIMEZONE);

    public static final String[] DATE_FORMATS = new String[] { DATETIME_PATTERN, DATETIME_T_PATTERN, DATE_PATTERN, DATETIME_T_MILLIS_EU_PATTERN };

    private DateUtils() {
        // Nothing to do
    }

    static {
        JODA_DATETIME_FORMATTERS.put(DATE_PATTERN, DateTimeFormat.forPattern(DATE_PATTERN).withZone(DateTimeZone.UTC));
        JODA_DATETIME_FORMATTERS.put(DATETIME_T_PATTERN, DateTimeFormat.forPattern(DATETIME_T_PATTERN).withZone(DateTimeZone.UTC));
        JODA_DATETIME_FORMATTERS.put(DATETIME_PATTERN, DateTimeFormat.forPattern(DATETIME_PATTERN).withZone(DateTimeZone.UTC));
        JODA_DATETIME_FORMATTERS.put(DATETIME_MILLIS_PATTERN, DateTimeFormat.forPattern(DATETIME_MILLIS_PATTERN).withZone(DateTimeZone.UTC));
        JODA_DATETIME_FORMATTERS.put(DATETIME_T_MILLIS_PATTERN, DateTimeFormat.forPattern(DATETIME_T_MILLIS_PATTERN).withZone(DateTimeZone.UTC));

        DATETIME_FORMATTERS.put(DATE_PATTERN, FastDateFormat.getInstance(DATE_PATTERN, UTC_TIMEZONE));
        DATETIME_FORMATTERS.put(DATETIME_T_PATTERN, FastDateFormat.getInstance(DATETIME_T_PATTERN, UTC_TIMEZONE));
        DATETIME_FORMATTERS.put(DATETIME_PATTERN, FastDateFormat.getInstance(DATETIME_PATTERN, UTC_TIMEZONE));
        DATETIME_FORMATTERS.put(DATETIME_MILLIS_PATTERN, FastDateFormat.getInstance(DATETIME_MILLIS_PATTERN, UTC_TIMEZONE));
        DATETIME_FORMATTERS.put(DATETIME_T_MILLIS_PATTERN, FastDateFormat.getInstance(DATETIME_T_MILLIS_PATTERN, UTC_TIMEZONE));

        PERIOD_FORMATTERS.put(PERIOD_PATTERN_ISO_STANDARD, ISOPeriodFormat.standard());
        PERIOD_FORMATTERS.put(PERIOD_PATTERN_ISO_ALTERNATE, ISOPeriodFormat.alternate());
        PERIOD_FORMATTERS.put(PERIOD_PATTERN_ISO_ALTERNATE_WITH_WEEKS, ISOPeriodFormat.alternateWithWeeks());
        PERIOD_FORMATTERS.put(PERIOD_PATTERN_ISO_ALTERNATE_EXTENDED, ISOPeriodFormat.alternateExtended());
        PERIOD_FORMATTERS.put(PERIOD_PATTERN_ISO_ALTERNATE_EXTENDED_WITH_WEEKS, ISOPeriodFormat.alternateExtendedWithWeeks());
    }

    /**
     * Date to XML gregorian calendar.
     * 
     * @param date the date
     * 
     * @return the XML gregorian calendar
     * 
     * @throws MotuException the motu exception
     */
    public static XMLGregorianCalendar dateToXMLGregorianCalendar(Date date) throws MotuException {
        GregorianCalendar gCalendar = new GregorianCalendar();
        gCalendar.setTime(date);
        XMLGregorianCalendar xmlGregorianCalendar;
        try {
            xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCalendar);
        } catch (DatatypeConfigurationException e) {
            throw new MotuException(ErrorType.INVALID_DATE, "ERROR in dateToXMLGregorianCalendar", e);
        }
        return xmlGregorianCalendar;
    }

    public static String getDurationMinSecMsec(long timeInMSec) {
        long min = timeInMSec / 60000;
        long sec = (timeInMSec % 60000) / 1000;
        long milli = (timeInMSec % 60000) % 1000;

        return min + "min " + sec + "sec " + milli + "msec";
    }

    /**
     * @See https://en.wikipedia.org/wiki/ISO_8601#Durations
     * @param timeInMSec time in milliseconds
     * @return A duration example P18Y9M4DT11H9M8S
     */
    public static String getDurationISO8601(long timeInMSec) {
        double totalTime = 0.0;
        int year = (int) Math.floor((timeInMSec - totalTime) / ONE_YEAR_IN_MILLI);
        totalTime += year * ONE_YEAR_IN_MILLI;

        int month = (int) Math.floor((timeInMSec - totalTime) / ONE_MONTH_IN_MILLI);
        totalTime += month * ONE_MONTH_IN_MILLI;

        int day = (int) Math.floor((timeInMSec - totalTime) / ONE_DAY_IN_MILLI);
        totalTime += day * ONE_DAY_IN_MILLI;

        int hour = (int) Math.floor((timeInMSec - totalTime) / ONE_HOUR_IN_MILLI);
        totalTime += hour * ONE_HOUR_IN_MILLI;

        int min = (int) Math.floor((timeInMSec - totalTime) / ONE_MINUTE_IN_MILLI);
        totalTime += min * ONE_MINUTE_IN_MILLI;

        int sec = (int) Math.floor((timeInMSec - totalTime) / ONE_SECOND_IN_MILLI);
        totalTime += sec * ONE_SECOND_IN_MILLI;

        int milli = (int) Math.floor((timeInMSec - totalTime));

        StringBuilder sb = new StringBuilder();
        if (milli > 0) {
            sb.insert(0, "." + String.format("%03d", milli));
        }
        if (sec > 0) {
            sb.insert(0, sec + "S");
        }
        if (min > 0) {
            sb.insert(0, min + "M");
        }
        if (hour > 0) {
            sb.insert(0, hour + "H");
        }
        if (sb.length() > 0) {
            sb.insert(0, "T");
        }

        if (day > 0) {
            sb.insert(0, day + "D");
        }

        if (month > 0) {
            sb.insert(0, month + "M");
        }

        if (year > 0) {
            sb.insert(0, year + "Y");
        }
        sb.insert(0, "P");
        if (sb.length() == 1) {
            sb.append("0D");
        }
        return sb.toString();
    }

    /**
     * Returns a GMT string representation (yyyy-MM-dd HH:mm:ss) without time if 0 (yyyy-MM-dd) from a date
     * value and an udunits string.
     * 
     * @param date Date object to convert
     * 
     * @return a string representation of the date
     */
    public static String getDateAsGMTNoZeroTimeString(Date date) {
        if (date == null) {
            return "";
        }
        GregorianCalendar calendar = new GregorianCalendar(DateUtils.GMT_TIMEZONE);
        calendar.setTime(date);

        int h = calendar.get(Calendar.HOUR_OF_DAY);
        int m = calendar.get(Calendar.MINUTE);
        int s = calendar.get(Calendar.SECOND);

        if ((h == 0) && (m == 0) && (s == 0)) {
            return DATE_TO_STRING_DEFAULT.format(date);
        } else {
            return DATETIME_TO_STRING_DEFAULT.format(date);
        }
    }

    /**
     * Returns a GMT string representation (yyyy-MM-dd HH:mm:ss) from a date value, or an empty String if null
     * 
     * @param date Date object to convert
     * 
     * @return a string representation of the date
     */
    public static String getDateAsGMTString(Date date) {
        if (date == null) {
            return "";
        }
        return DATETIME_TO_STRING_DEFAULT.format(date);
    }

    /**
     * Returns a standard (ISO) GMT string representation from a date value and an udunits string.
     * 
     * @param unitsString udunits string
     * @param value value of the date
     * 
     * @return a string representation of the date
     * 
     * @throws MotuException the motu exception
     */
    public static String getDateAsIsoString(double value, String unitsString) throws MotuException {
        String date = null;
        try {
            DateUnit dateUnit = new DateUnit(unitsString);
            date = dateUnit.makeStandardDateString(value);
        } catch (Exception e) {
            throw new MotuException(ErrorType.NETCDF_LOADING, "Error in getDateAsString", e);
        }
        return date;
    }

    /**
     * Returns a double value corresponding to a Date an udunits string.
     * 
     * @param unitsString udunits string
     * @param date date to convert to
     * 
     * @return a string representation of the date
     * 
     * @throws MotuException the motu exception
     */
    public static double getDate(Date date, String unitsString) throws MotuException {
        double value = Double.MAX_VALUE;
        try {
            DateUnit dateUnit = new DateUnit(unitsString);
            value = dateUnit.makeValue(date);
        } catch (Exception e) {
            throw new MotuException(ErrorType.INVALID_DATE, "Error in getDate", e);
        }
        return value;
    }

    /**
     * Returns a double value corresponding to a Date an udunits string.
     * 
     * @param dateUnit udunits string
     * @param date date to convert to
     * 
     * @return a string representation of the date
     * 
     * @throws MotuException the motu exception
     */
    public static double getDate(Date date, DateUnit dateUnit) throws MotuException {
        double value = Double.MAX_VALUE;
        try {
            value = dateUnit.makeValue(date);
        } catch (Exception e) {
            throw new MotuException(ErrorType.INVALID_DATE, "Error in getDate", e);
        }
        return value;
    }

    /**
     * Returns a java.util.Date object from a date value and an udunits string.
     * 
     * @param unitsString udunits string
     * @param value value of the date
     * 
     * @return a Date
     * 
     * @throws MotuException the motu exception
     */
    public static Date getDate(double value, String unitsString) throws MotuException {
        Date date = null;
        try {
            date = new DateUnit(unitsString).makeDate(value);
        } catch (Exception e) {
            throw new MotuException(ErrorType.INVALID_DATE, "Error in getDate", e);
        }
        return date;
    }

    /**
     * Convert a unitsString into a DateUnit that can be used for converting date .
     * 
     * @param unitsString
     * @return
     * @throws MotuException
     */
    public static DateUnit getDateUnit(String unitsString) throws MotuException {
        DateUnit dateUnit = null;
        try {
            dateUnit = new DateUnit(unitsString);
        } catch (Exception e) {
            throw new MotuException(ErrorType.INVALID_DATE, "Error in getDateUnit", e);
        }
        return dateUnit;
    }

    /**
     * Returns a java.util.Date object from a date value and an udunits string.
     * 
     * @param dateUnit dateUnit from unitstring
     * @param value value of the date
     * 
     * @return a Date
     * 
     * @throws MotuException the motu exception
     */
    public static Date getDate(double value, DateUnit dateUnit) throws MotuException {
        Date date = null;
        try {
            date = dateUnit.makeDate(value);
        } catch (Exception e) {
            throw new MotuException(ErrorType.INVALID_DATE, "Error in getDate", e);
        }
        return date;
    }

    /**
     * Returns a GMT string representation (yyyy-MM-dd HH:mm:ss) from a date value and an udunits string.
     * 
     * @param unitsString udunits string
     * @param value value of the date
     * 
     * @return a string representation of the date
     * 
     * @throws MotuException the motu exception
     */
    public static String getDateAsGMTString(double value, String unitsString) throws MotuException {
        Date date = getDate(value, unitsString);
        return DATETIME_TO_STRING_DEFAULT.format(date);
    }

    /**
     * Returns a GMT string representation (yyyy-MM-dd HH:mm:ss) from a date value and an udunits string.
     * 
     * @param DateUnit dateUnit string
     * @param value value of the date
     * 
     * @return a string representation of the date
     * 
     * @throws MotuException the motu exception
     */
    public static String getDateAsGMTString(double value, DateUnit dateUnit) throws MotuException {
        Date date = getDate(value, dateUnit);
        return DATETIME_TO_STRING_DEFAULT.format(date);
    }

    /**
     * Returns a GMT string representation (yyyy-MM-dd HH:mm:ss) without time if 0 ((yyyy-MM-dd) from a date
     * value and an udunits string.
     * 
     * @param unitsString udunits string
     * @param value value of the date
     * 
     * @return a string representation of the date
     * 
     * @throws MotuException the motu exception
     */
    public static String getDateAsGMTNoZeroTimeString(double value, String unitsString) throws MotuException {

        Date date = getDate(value, unitsString);
        return getDateAsGMTNoZeroTimeString(date);
    }

    public static Date parseDate(String dateStr, String dateFormat) {
        Date date = null;
        if (dateFormat != null) {
            SimpleDateFormat fmt = new SimpleDateFormat(dateFormat);
            try {
                date = fmt.parse(dateStr);
            } catch (Exception e) {
                // noop return null if an issue is raised
                // LOG.error("Error while paring date: " + dateStr_, e);
            }
        } else {
            Iterator<String> itFormat = DATETIME_FORMATTERS.keySet().iterator();
            while (date == null && itFormat.hasNext()) {
                SimpleDateFormat fmt = new SimpleDateFormat(itFormat.next());
                try {
                    date = fmt.parse(dateStr);
                } catch (Exception e) {
                    // noop
                }
            }
        }
        return date;
    }

    /**
     * Parses text from the beginning of the given string to produce a date. The method may not use the entire
     * text of the given string.
     * <p>
     * See the {@link java.text.DateFormat#parse(String, ParsePosition)} method for more information on date
     * parsing.
     * 
     * @param source A <code>String</code> whose beginning should be parsed (it tries to parse with
     *            DATETIME_FORMAT and DATE_FORMAT if previous is not successfull).
     * 
     * @return A <code>Date</code> parsed from the string.
     * 
     * @throws MotuInvalidDateException the motu invalid date exception
     */
    public static Date parseDate(String source, int setTimeTo0ForBeginOfDays1ForEndOfDayNegativeForNow) throws MotuInvalidDateException {
        Date date = null;
        int i = 0;
        while (date == null && i < DateUtils.DATE_FORMATS.length) {
            date = parseDate(source, DateUtils.DATE_FORMATS[i]);
            i++;
        }

        if (date == null) {
            throw new MotuInvalidDateException(source);
        } else {
            // this is a only a DAY format
            if (DATE_PATTERN == DateUtils.DATE_FORMATS[i - 1]) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                if (setTimeTo0ForBeginOfDays1ForEndOfDayNegativeForNow == 0) {
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                } else if (setTimeTo0ForBeginOfDays1ForEndOfDayNegativeForNow == 1) {
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 59);
                    cal.set(Calendar.MILLISECOND, 999);
                }
                date = cal.getTime();
            }
        }

        return date;
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
     * @return true if this time ' intervalCompareTo' contains 'intervalCompareTo'
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
        if (!DateUtils.intersects(interval1, interval2)) {
            return null;
        }
        long start = Math.max(interval1.getStartMillis(), interval2.getStartMillis());
        long end = Math.min(interval1.getEndMillis(), interval2.getEndMillis());
        return new Interval(start, end);
    }

    /**
     * Date time to utc.
     * 
     * @param dateTime the date time
     * @return the date time
     */
    public static DateTime dateTimeToUTC(DateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return new DateTime(dateTime, DateTimeZone.UTC);
    }

    /**
     * Date time to utc.
     * 
     * @param dateTime the date time
     * @return the date time
     */
    public static DateTime dateTimeToUTC(Date date) {
        if (date == null) {
            return null;
        }
        return new DateTime(date.getTime(), DateTimeZone.UTC);
    }

    /**
     * Gets the date time as utc string.
     * 
     * @param dateTime the date time
     * @return the date time as utc string
     */
    public static String getDateTimeAsUTCString(DateTime dateTime) {
        return DateUtils.getDateTimeAsUTCString(dateTime, DateUtils.DATETIME_PATTERN);
    }

    /**
     * Gets the date time as utc string.
     * 
     * @param date the date
     * @return the date time as utc string
     */
    public static String getDateTimeAsUTCString(Date date) {
        return DateUtils.getDateTimeAsUTCString(date, DateUtils.DATETIME_PATTERN);
    }

    /**
     * Gets the date time as utc string.
     * 
     * @param dateTime the date time
     * @param pattern the pattern
     * @return the date time as utc string
     */
    public static String getDateTimeAsUTCString(DateTime dateTime, String pattern) {
        String value = "";
        if (dateTime == null) {
            return value;
        }
        DateTime dateTimeTmp = DateUtils.dateTimeToUTC(dateTime);
        return DateUtils.JODA_DATETIME_FORMATTERS.get(pattern).print(dateTimeTmp);
    }

    /**
     * Gets the date time as utc string.
     * 
     * @param date the date
     * @param pattern the pattern
     * @return the date time as utc string
     */
    public static String getDateTimeAsUTCString(Date date, String pattern) {
        String value = "";
        if (date == null) {
            return value;
        }
        return DateUtils.DATETIME_FORMATTERS.get(pattern).format(date);
    }

    /**
     * Convert a given date into a string representation.
     * 
     * @param dt the date to print.
     * 
     * @return the string representation.
     */
    public static String dateToString(DateTime dt) {
        return dateTimeToString(dt, DateUtils.DATE_PATTERN);
    }

    /**
     * Convert a given date with time into a string representation according to pattern key in parameter.
     * 
     * @param dt the date time to print.
     * @param pattern The pattern from this class constants
     * 
     * @return the string representation.
     */
    public static String dateTimeToString(DateTime dt, final String pattern) {
        return DateUtils.JODA_DATETIME_FORMATTERS.get(pattern).print(dt);
    }

    /**
     * Convert a given date with time into a string representation.
     * 
     * @param dt the date time to print.
     * 
     * @return the string representation.
     */
    public static String dateTimeToString(DateTime dt) {
        return DateUtils.JODA_DATETIME_FORMATTERS.get(DateUtils.DATETIME_PATTERN).print(dt);
    }

    /**
     * Convert a given string date representation into an instance of Joda time date.
     * 
     * @param s the string to convert into a date.
     * @return a {@link DateTime} instance.
     * @throws MotuConverterException the motu converter exception
     */
    public static Date stringToDate(String s) throws MotuConverterException {
        Date date = null;

        StringBuilder stringBuilder = new StringBuilder();
        for (FastDateFormat dateTimeFormatter : DateUtils.DATETIME_FORMATTERS.values()) {
            try {
                date = dateTimeFormatter.parse(s);
            } catch (ParseException e) {
                stringBuilder.append(e.getMessage());
                stringBuilder.append("\n");
            }

            if (date != null) {
                break;
            }
        }

        if (date == null) {
            throw new MotuConverterException(
                    String.format("Cannot convert '%s' to DateTime. Format '%s' is not valid.%nAcceptable format are '%s'",
                                  s,
                                  stringBuilder.toString(),
                                  DateUtils.DATETIME_FORMATTERS.keySet().toString()));
        }
        return date;
    }

    /**
     * String to period.
     * 
     * @param s the s
     * @return the period
     * @throws MotuConverterException the motu converter exception
     */
    public static Period stringToPeriod(String s) throws MotuConverterException {
        Period period = null;

        StringBuilder stringBuilder = new StringBuilder();
        for (PeriodFormatter periodFormatter : DateUtils.PERIOD_FORMATTERS.values()) {
            try {
                period = periodFormatter.parsePeriod(s);
            } catch (IllegalArgumentException e) {
                stringBuilder.append(e.getMessage());
                stringBuilder.append("\n");
            }

            if (period != null) {
                break;
            }
        }

        if (period == null) {
            throw new MotuConverterException(
                    String.format("Cannot convert '%s' to Period. Format '%s' is not valid.%nAcceptable format are '%s'",
                                  s,
                                  stringBuilder.toString(),
                                  DateUtils.PERIOD_FORMATTERS.keySet().toString()));
        }
        return period;
    }

    /**
     * Convert a given date into a string representation (only date).
     * 
     * @param st the full date to print.
     * 
     * @return only the date (extract)
     */
    public static String getDate(String st) {
        if (st.contains("T")) {
            String[] tmp = st.split("T");
            return tmp[0];
        }
        return st;
    }

    public static Date parseDate(String lastupdate) {
        Date result = null;
        for (FastDateFormat format : DATETIME_FORMATTERS.values()) {
            try {
                result = format.parse(lastupdate);
            } catch (ParseException dte) {
                // Nothing to do
            }
            if (result != null) {
                break;
            }
        }
        return result;
    }

    public static DateTime parseDateTime(String lastupdate) {
        DateTime result = null;
        for (DateTimeFormatter format : JODA_DATETIME_FORMATTERS.values()) {
            try {
                result = format.parseDateTime(lastupdate);
            } catch (IllegalArgumentException dte) {
                // Nothing to do
            }
            if (result != null) {
                break;
            }
        }
        return result;
    }

}
