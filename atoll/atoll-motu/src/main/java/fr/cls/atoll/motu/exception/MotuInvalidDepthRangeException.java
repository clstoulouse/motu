/**
 * 
 */
package fr.cls.atoll.motu.exception;

import org.apache.log4j.Logger;

// CSOFF: MultipleStringLiterals : avoid message in constants declaration and trace log.

/**
 * Depth range exception class of Motu.
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-02-20 13:00:25 $
 * 
 */
public class MotuInvalidDepthRangeException extends MotuExceptionBase {
    /**
     * Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(MotuInvalidDepthRangeException.class);

    private static final long serialVersionUID = -1L;

    /**
     * @param invalidRangeMin invalid depth range min. representation which causes the exception
     * @param invalidRangeMax invalid depth range max. representation which causes the exception
     * @param validRangeMin valid depth range min. representation
     * @param validRangeMax valid depth range max. representation
     */
    public MotuInvalidDepthRangeException(double invalidRangeMin, double invalidRangeMax, double validRangeMin, double validRangeMax) {
        super("Invalid depth range.");
        // CSOFF: StrictDuplicateCode : normal duplication code.

        this.invalidRange = new double[2];
        this.invalidRange[0] = invalidRangeMin;
        this.invalidRange[1] = invalidRangeMax;

        this.validRange = new double[2];
        this.validRange[0] = validRangeMin;
        this.validRange[1] = validRangeMax;

        notifyLogException();
    }

    // CSOFF: StrictDuplicateCode

    /**
     * @param invalidRangeMin invalid depth range min. representation which causes the exception
     * @param invalidRangeMax invalid depth range max. representation which causes the exception
     * @param validRangeMin valid depth range min. representation
     * @param validRangeMax valid depth range max. representation
     * @param cause native exception.
     */
    public MotuInvalidDepthRangeException(double invalidRangeMin, double invalidRangeMax, double validRangeMin, double validRangeMax, Throwable cause) {
        super("Invalid depth range.", cause);

        this.invalidRange = new double[2];
        this.invalidRange[0] = invalidRangeMin;
        this.invalidRange[1] = invalidRangeMax;

        this.validRange = new double[2];
        this.validRange[0] = validRangeMin;
        this.validRange[1] = validRangeMax;

        notifyLogException();
    }

    /**
     * @param invalidRange invalid depth range representation which causes the exception
     * @param validRange valid depth range representation
     * @param cause native exception.
     */
    public MotuInvalidDepthRangeException(double[] invalidRange, double[] validRange, Throwable cause) {
        super("Invalid depth range.", cause);

        assert invalidRange.length == 2;
        assert validRange.length == 2;

        this.invalidRange = invalidRange;
        this.validRange = validRange;
        notifyLogException();
    }

    /**
     * @param invalidRange invalid depth range representation which causes the exception
     * @param validRange valid depth range representation
     */
    public MotuInvalidDepthRangeException(double[] invalidRange, double[] validRange) {
        super("Invalid depth range.");

        assert invalidRange.length == 2;
        assert validRange.length == 2;

        this.invalidRange = invalidRange;
        this.validRange = validRange;
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
        if (invalidRange != null) {
            stringBuffer.append("\nInvalid range:");
            stringBuffer.append(getInvalidRangeAsString());
        }
        if (validRange != null) {
            stringBuffer.append("Valid range is:[");
            stringBuffer.append(getValidRangeAsString());
        }

        return stringBuffer.toString();
    }

    /**
     * Depth range representation which causes the exception.
     */
    final private double[] invalidRange;

    /**
     * @return the invalidRange
     */
    public double[] getInvalidRange() {
        return this.invalidRange;
    }

    /**
     * @return the invalidRange as a string interval representation
     */
    public String getInvalidRangeAsString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("[");
        stringBuffer.append(Double.toString(invalidRange[0]));
        stringBuffer.append(",");
        stringBuffer.append(Double.toString(invalidRange[1]));
        stringBuffer.append("]");
        return stringBuffer.toString();
    }

    /**
     * Valid Depth range representation.
     */
    final private double[] validRange;

    /**
     * @return the validRange
     */
    public double[] getValidRange() {
        return this.validRange;
    }

    /**
     * @return the validRange as a string interval representation
     */
    public String getValidRangeAsString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("[");
        stringBuffer.append(Double.toString(validRange[0]));
        stringBuffer.append(",");
        stringBuffer.append(Double.toString(validRange[1]));
        stringBuffer.append("]");
        return stringBuffer.toString();
    }

}
// CSON: MultipleStringLiterals
