/**
 * 
 */
package fr.cls.atoll.motu.library.exception;

import org.apache.log4j.Logger;

// CSOFF: MultipleStringLiterals : avoid message in constants declaration and trace log.

/**
 * Longitude exception class of Motu.
 * 
 * @author $Author: ccamel $
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:21 $
 * 
 */
public class MotuInvalidLongitudeException extends MotuExceptionBase {
    /**
     * Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(MotuInvalidLongitudeException.class);

    private static final long serialVersionUID = -1L;

    /**
     * @param lon longitude representation which causes the exception
     * @param cause native exception.
     */
    public MotuInvalidLongitudeException(String lon, Throwable cause) {
        super("Invalid longitude.", cause);
        this.lonString = lon;
        this.lon = Double.MAX_VALUE;
        notifyLogException();

    }

    /**
     * @param lon longitude representation which causes the exception
     */
    public MotuInvalidLongitudeException(String lon) {
        super("Invalid longitude.");
        this.lonString = lon;
        this.lon = Double.MAX_VALUE;
        notifyLogException();
    }

    /**
     * @param lon longitude representation which causes the exception
     * @param cause native exception.
     */
    public MotuInvalidLongitudeException(double lon, Throwable cause) {
        super("Invalid longitude.", cause);
        this.lonString = "?";
        this.lon = lon;
        notifyLogException();
    }

    /**
     * @param lon longitude representation which causes the exception
     */
    public MotuInvalidLongitudeException(double lon) {
        super("Invalid longitude.");
        this.lonString = "?";
        this.lon = lon;
        notifyLogException();
    }

    /**
     * writes exception information into the log.
     */
    public void notifyLogException() {

        super.notifyLogException();
        LOG.warn(notifyException());
    }

    /**
     * @return exception information.
     */
    public String notifyException() {
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(super.notifyException());
        stringBuffer.append("\nLongitude:");
        if (lon != Double.MAX_VALUE) {
            stringBuffer.append(Double.toString(lon));
        } else {
            stringBuffer.append(lonString);
        }
        return stringBuffer.toString();
    }

    /**
     * String Longitude representation which causes the exception..
     */
    final private String lonString;

    /**
     * @return the lonString
     */
    public String getLonString() {
        return this.lonString;
    }

    /**
     * Longitude representation which causes the exception..
     */
    final private double lon;

    /**
     * @return the lon
     */
    public double getLon() {
        return this.lon;
    }

    /**
     * @return longitude as string
     */
    public String getLonAsString() {

        if (lon != Double.MAX_VALUE) {
            return Double.toString(lon);
        } else {
            return lonString;
        }
    }

}
// CSON: MultipleStringLiterals
