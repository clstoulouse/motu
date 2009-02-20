/**
 * 
 */
package fr.cls.atoll.motu.exception;

import org.apache.log4j.Logger;

// CSOFF: MultipleStringLiterals : avoid message in constants declaration and trace log.

/**
 * Latitude exception class of Motu.
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-02-20 13:00:25 $
 * 
 */
public class MotuInvalidLatitudeException extends MotuExceptionBase {
    /**
     * Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(MotuInvalidLatitudeException.class);

    private static final long serialVersionUID = -1L;

    /**
     * @param lat latitude representation which causes the exception
     * @param cause native exception.
     */
    public MotuInvalidLatitudeException(String lat, Throwable cause) {
        super("Invalid latitude.", cause);
        this.latString = lat;
        this.lat = Double.MAX_VALUE;
        notifyLogException();

    }

    /**
     * @param lat latitude representation which causes the exception
     */
    public MotuInvalidLatitudeException(String lat) {
        super("Invalid latitude.");
        this.latString = lat;
        this.lat = Double.MAX_VALUE;
        notifyLogException();
    }

    /**
     * @param lat latitude representation which causes the exception
     * @param cause native exception.
     */
    public MotuInvalidLatitudeException(double lat, Throwable cause) {
        super("Invalid latitude.", cause);
        this.latString = "?";
        this.lat = lat;
        notifyLogException();
    }

    /**
     * @param lat latitdue representation which causes the exception
     */
    public MotuInvalidLatitudeException(double lat) {
        super("Invalid latitude.");
        this.latString = "?";
        this.lat = lat;
        notifyLogException();
    }

    /**
     * writes exception information into the log.
     */
    public void notifyLogException() {

        super.notifyLogException();
        if (lat != Double.MAX_VALUE) {
            LOG.warn("Latitude: " + lat);
        } else {
            LOG.warn("Latitude string representation: " + latString);
        }
    }

    /**
     * @return exception information.
     */
    public String notifyException() {
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(super.notifyException());
        stringBuffer.append("\nLatitude:");
        if (lat != Double.MAX_VALUE) {
            stringBuffer.append(Double.toString(lat));
        } else {
            stringBuffer.append(latString);
        }
        return stringBuffer.toString();
    }

    /**
     * String Latitude representation which causes the exception..
     */
    final private String latString;

    /**
     * @return the latString
     */
    public String getLatString() {
        return this.latString;
    }

    /**
     * Latitude representation which causes the exception..
     */
    final private double lat;

    /**
     * @return the lat
     */
    public double getLat() {
        return this.lat;
    }

    /**
     * @return latitude as string
     */
    public String getLatAsString() {

        if (lat != Double.MAX_VALUE) {
            return Double.toString(lat);
        } else {
            return latString;
        }
    }

}
// CSON: MultipleStringLiterals
