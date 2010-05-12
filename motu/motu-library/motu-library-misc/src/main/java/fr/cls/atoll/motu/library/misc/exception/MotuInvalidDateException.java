package fr.cls.atoll.motu.library.misc.exception;

import fr.cls.atoll.motu.library.misc.netcdf.NetCdfReader;

import java.util.Date;

import org.apache.log4j.Logger;

// CSOFF: MultipleStringLiterals : avoid message in constants declaration and trace log.

/**
 * Date exception class of Motu.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class MotuInvalidDateException extends MotuExceptionBase {
    /**
     * Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(MotuInvalidDateException.class);

    private static final long serialVersionUID = -1L;

    /**
     * @param date string date representation which causes the exception
     * @param cause native exception.
     */
    public MotuInvalidDateException(String date, Throwable cause) {
        super("Invalid date.", cause);
        this.dateString = date;
        this.date = null;
        notifyLogException();

    }

    /**
     * @param date string date representation which causes the exception
     */
    public MotuInvalidDateException(String date) {
        super("Invalid date.");
        this.dateString = date;
        this.date = null;
        notifyLogException();
    }

    /**
     * @param date Date representation which causes the exception
     * @param cause native exception.
     */
    public MotuInvalidDateException(Date date, Throwable cause) {
        super("Invalid date.", cause);
        this.dateString = "?";
        this.date = date;
        notifyLogException();
    }

    /**
     * @param date Date representation which causes the exception
     */
    public MotuInvalidDateException(Date date) {
        super("Invalid date.");
        this.dateString = "?";
        this.date = date;
        notifyLogException();
    }

    /**
     * writes exception information into the log.
     */
    @Override
    public void notifyLogException() {

        super.notifyLogException();
        LOG.warn(notifyException());
    }

    /**
     * @return exception information.
     */
    @Override
    public String notifyException() {
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(super.notifyException());
        stringBuffer.append("\nDate:");
        if (date != null) {
            stringBuffer.append(NetCdfReader.DATETIME_TO_STRING_DEFAULT.format(date));
        } else {
            stringBuffer.append(dateString);
        }
        return stringBuffer.toString();
    }

    /**
     * String date representation which causes the exception..
     */
    final private String dateString;

    /**
     * @return the dateString
     */
    public String getDateString() {
        return this.dateString;
    }

    /**
     * Date representation which causes the exception..
     */
    final private Date date;

    /**
     * @return the date
     */
    public Date getDate() {
        return this.date;
    }

    /**
     * @return date as string
     */
    public String getDateAsString() {

        if (date != null) {
            return NetCdfReader.DATETIME_TO_STRING_DEFAULT.format(date);
        } else {
            return dateString;
        }
    }

}
// CSON: MultipleStringLiterals
