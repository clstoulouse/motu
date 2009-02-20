/**
 * 
 */
package fr.cls.atoll.motu.exception;

import org.apache.log4j.Logger;

// CSOFF: MultipleStringLiterals : avoid message in constants declaration and trace log.

/**
 * Longitude exception class of Motu.
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-02-20 13:00:25 $
 * 
 */
public class MotuExceedingCapacityException extends MotuExceptionBase {
    /**
     * Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(MotuExceedingCapacityException.class);

    private static final long serialVersionUID = -1L;

    /**
     * @param max max capacity allowed in Megabytes.
     */
    public MotuExceedingCapacityException(double max) {
        super("Exceeding capacity.");
        this.actual = Double.MAX_VALUE;
        this.max = max;
        notifyLogException();

    }

    /**
     * @param actual actual capacity in Megabytes.
     * @param max max capacity allowed in Megabytes.
     */
    public MotuExceedingCapacityException(double actual, double max) {
        super("Exceeding capacity.");
        this.actual = actual;
        this.max = max;
        notifyLogException();

    }

    // CSOFF: StrictDuplicateCode : normal duplication code.

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

        if (actual != Double.MAX_VALUE) {
            stringBuffer.append("\nActual is ");
            stringBuffer.append(getActualAsString());
            stringBuffer.append(".");
        }
        stringBuffer.append("\nMaximum is ");
        stringBuffer.append(getMaxAsString());
        return stringBuffer.toString();
    }

    // CSON: StrictDuplicateCode : normal duplication code.

    /**
     * Actual capacity value in Megabytes.
     * 
     * @uml.property name="actual"
     */
    final private double actual;

    /**
     * Getter of the property <tt>actual</tt>.
     * 
     * @return Returns the actual.
     * @uml.property name="actual"
     */
    public double getActual() {
        return this.actual;
    }

    /**
     * @return the actual as a string representation
     */
    public String getActualAsString() {
        StringBuffer stringBuffer = new StringBuffer();
        if (actual != Double.MAX_VALUE) {
            stringBuffer.append(String.format("%8.2f", actual));
        } else {
            stringBuffer.append("???");
        }
        stringBuffer.append(" Megabyte(s)");
        return stringBuffer.toString();
    }

    /**
     * Max capacity allowed value in Megabytes.
     * 
     * @uml.property name="max"
     */
    final private double max;

    /**
     * Getter of the property <tt>max</tt>.
     * 
     * @return Returns the max.
     * @uml.property name="max"
     */
    public double getMax() {
        return this.max;
    }

    /**
     * @return the max as a string representation
     */
    public String getMaxAsString() {
        StringBuffer stringBuffer = new StringBuffer();
        if (max != Double.MAX_VALUE) {
            stringBuffer.append(String.format("%8.2f", max));
        } else {
            stringBuffer.append("???");
        }
        stringBuffer.append(" Megabyte(s)");
        return stringBuffer.toString();
    }

}
// CSON: MultipleStringLiterals
