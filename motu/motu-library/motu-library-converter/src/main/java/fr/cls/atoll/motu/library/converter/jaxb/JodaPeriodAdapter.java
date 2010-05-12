package fr.cls.atoll.motu.library.converter.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAXB adapter that converts a xs:dateTime or xs:date into a {@link DateTime} and vice-versa.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:ccamel@cls.fr">Christophe Camel</a>
 */
public class JodaPeriodAdapter extends XmlAdapter<String, Period> {

    /**
     * Constructeur.
     */
    public JodaPeriodAdapter() {
    }

    /**
     * Format used to print and parse period.
     */
    // public static DateTimeFormatter DATE_FORMATER = new DateTimeFormatterBuilder().appendYear(4,
    // 4).appendLiteral('-').appendMonthOfYear(2)
    // .appendLiteral('-').appendDayOfMonth(2).appendLiteral('T').appendHourOfDay(2).appendLiteral(':').appendMinuteOfHour(2).appendLiteral(':')
    // .appendSecondOfMinute(2).appendLiteral(' ').appendTimeZoneOffset(null, false, 2, 2).toFormatter();
    public static final PeriodFormatter PERIOD_FORMATER = ISOPeriodFormat.standard();

    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JodaPeriodAdapter.class);

    /**
     * Convert a given date into a string representation.
     * 
     * @param dt the date to print.
     * @return the string representation.
     */
    @Override
    public String marshal(Period dt) {
        return PERIOD_FORMATER.print(dt);
    }

    /**
     * Convert a given string date representation into an instance of Joda time date.
     * 
     * @param s the string to convert into a date.
     * @return a {@link DateTime} instance.
     */
    @Override
    public Period unmarshal(String s) {
        try {
            return JodaPeriodAdapter.PERIOD_FORMATER.parsePeriod(s);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Failed to parse period from " + s, e);
            return new Period();
        }
    }

}
