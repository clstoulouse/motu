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
        long day = timeInMSec / 86400000;
        long hour = timeInMSec % 360000;
        long min = timeInMSec % 60000;
        long sec = (timeInMSec % 60000) / 1000;
        long milli = (timeInMSec % 60000) % 1000;

        StringBuilder sb = new StringBuilder();
        if (milli > 0) {
            sb.insert(0, "." + String.format("%03d", milli));
        }
        if (sec > 0 || sb.length() > 0) {
            sb.insert(0, ":" + String.format("%02d", sec));
        }
        if (min > 0 || sb.length() > 0) {
            sb.insert(0, ":" + String.format("%02d", min));
        }
        if (hour > 0 || sb.length() > 0) {
            sb.insert(0, String.format("%02d", hour));
        }
        if (sb.length() > 0) {
            sb.insert(0, "T");
        }

        if (day > 0 || sb.length() <= 1) {
            sb.insert(0, day);
        }
        sb.insert(0, "P");
        return sb.toString();
    }

}
