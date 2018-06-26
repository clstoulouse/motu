package fr.cls.atoll.motu.web.common.utils;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.exception.MotuException;

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

    public static final long ONE_SECOND_IN_MILLI = 1000L;
    public static final long ONE_MINUTE_IN_MILLI = 60 * ONE_SECOND_IN_MILLI;
    public static final long ONE_HOUR_IN_MILLI = 60 * ONE_MINUTE_IN_MILLI;
    public static final long ONE_DAY_IN_MILLI = 24 * ONE_HOUR_IN_MILLI;
    public static final long ONE_MONTH_IN_MILLI = 30 * ONE_DAY_IN_MILLI;
    public static final long ONE_YEAR_IN_MILLI = 365 * ONE_DAY_IN_MILLI;

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
        return sb.toString();
    }

}
