/**
 * 
 */
package fr.cls.atoll.motu.library.exception;

import org.apache.log4j.Logger;

import ucar.unidata.geoloc.LatLonRect;

// CSOFF: MultipleStringLiterals : avoid message in constants declaration and trace log.

/**
 * Depth range exception class of Motu.
 * 
 * @author $Author: ccamel $
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:21 $
 * 
 */
public class MotuInvalidLatLonRangeException extends MotuExceptionBase {
    /**
     * Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(MotuInvalidLatLonRangeException.class);

    private static final long serialVersionUID = -1L;

    /**
     * @param invalidRect invalid lat/lon bounding box representation which causes the exception
     * @param validRect valid lat/lon bounding representation
     */
    public MotuInvalidLatLonRangeException(LatLonRect invalidRect, LatLonRect validRect) {
        super("Invalid latitude/longitude bounding box point.");

        this.invalidRect = new LatLonRect(invalidRect);
        this.validRect = new LatLonRect(validRect);

        notifyLogException();
    }

    /**
     * @param invalidRect invalid lat/lon bounding box representation which causes the exception
     * @param validRect valid lat/lon bounding representation
     * @param cause native exception.
     */
    public MotuInvalidLatLonRangeException(LatLonRect invalidRect, LatLonRect validRect, Throwable cause) {
        super("Invalid latitude/longitude bounding box point.", cause);

        this.invalidRect = new LatLonRect(invalidRect);
        this.validRect = new LatLonRect(validRect);

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
        if (invalidRect != null) {
            stringBuffer.append("\nInvalid bounding box: ");
            stringBuffer.append(getInvalidRectAsString());
        }
        if (validRect != null) {
            stringBuffer.append("\nValid bounding box: lower/left point [");
            stringBuffer.append(getValidRectAsString());
        }

        return stringBuffer.toString();
    }

    /**
     * Depth range representation which causes the exception.
     */
    final private LatLonRect invalidRect;

    /**
     * @return the invalidRect
     */
    public LatLonRect getInvalidRect() {
        return this.invalidRect;
    }

    /**
     * @return the validRect as a string interval representation
     */
    public String getInvalidRectAsString() {
        if (invalidRect == null) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Lower/left point [");
        stringBuffer.append(invalidRect.getLowerLeftPoint().toString());
        stringBuffer.append("] Upper/right point [");
        stringBuffer.append(invalidRect.getUpperRightPoint().toString());
        stringBuffer.append("]");
        return stringBuffer.toString();
    }

    /**
     * Valid Depth range representation.
     */
    final private LatLonRect validRect;

    /**
     * @return the validRect
     */
    public LatLonRect getValidRect() {
        return this.validRect;
    }

    /**
     * @return the validRect as a string interval representation
     */
    public String getValidRectAsString() {
        if (validRect == null) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Lower/left point [");
        stringBuffer.append(validRect.getLowerLeftPoint().toString());
        stringBuffer.append("] Upper/right point [");
        stringBuffer.append(validRect.getUpperRightPoint().toString());
        stringBuffer.append("]");
        return stringBuffer.toString();
    }

}
// CSON: MultipleStringLiterals
