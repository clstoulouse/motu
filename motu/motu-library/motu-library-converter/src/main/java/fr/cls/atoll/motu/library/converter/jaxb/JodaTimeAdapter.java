package fr.cls.atoll.motu.library.converter.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
public class JodaTimeAdapter extends XmlAdapter<String, DateTime> {

    /**
     * Constructeur.
     */
    public JodaTimeAdapter() {
    }

    /** The Constant DATE_PATTERN. */
    public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZZ";

    /**
     * Format used to print and parse dates.
     */
    // public static DateTimeFormatter DATE_FORMATER = new DateTimeFormatterBuilder().appendYear(4,
    // 4).appendLiteral('-').appendMonthOfYear(2)
    // .appendLiteral('-').appendDayOfMonth(2).appendLiteral('T').appendHourOfDay(2).appendLiteral(':').appendMinuteOfHour(2).appendLiteral(':')
    // .appendSecondOfMinute(2).appendLiteral(' ').appendTimeZoneOffset(null, false, 2, 2).toFormatter();
    public static final DateTimeFormatter DATE_FORMATER = DateTimeFormat.forPattern(JodaTimeAdapter.DATE_PATTERN);

    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JodaTimeAdapter.class);

    /**
     * Convert a given date into a string representation.
     * 
     * @param dt the date to print.
     * @return the string representation.
     */
    @Override
    public String marshal(DateTime dt) {
        return DATE_FORMATER.print(dt);
    }

    /**
     * Convert a given string date representation into an instance of Joda time date.
     * 
     * @param s the string to convert into a date.
     * @return a {@link DateTime} instance.
     */
    @Override
    public DateTime unmarshal(String s) {
        try {
            return DATE_FORMATER.parseDateTime(s);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Failed to parse dateTime from " + s, e);
            return new DateTime();
        }
    }

}
