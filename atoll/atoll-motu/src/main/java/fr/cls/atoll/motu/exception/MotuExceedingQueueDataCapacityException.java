package fr.cls.atoll.motu.exception;

import org.apache.log4j.Logger;

/**
 * <br><br>Copyright : Copyright (c) 2008.
 * <br><br>Société : CLS (Collecte Localisation Satellites)
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-02-20 13:00:25 $
 */
public class MotuExceedingQueueDataCapacityException extends MotuExceptionBase {
    /**
     * Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(MotuExceedingQueueDataCapacityException.class);

    private static final long serialVersionUID = -1L;

    /**
     * @param max max capacity allowed in Megabytes.
     */
    public MotuExceedingQueueDataCapacityException(double max) {
        super("Exceeding capacity.");
        this.actual = Double.MAX_VALUE;
        this.max = max;
        this.batchQueue = false;
        notifyLogException();

    }

    /**
     * The Constructor.
     * 
     * @param batchQueue the batch queue
     * @param max max capacity allowed in Megabytes.
     * @param actual actual capacity in Megabytes.
     */
    public MotuExceedingQueueDataCapacityException(double actual, double max, boolean batchQueue) {
        super("Exceeding queue data capacity.");
        this.actual = actual;
        this.max = max;
        this.batchQueue = batchQueue;
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
        stringBuffer.append("\nBatch Queue property: ");
        stringBuffer.append(Boolean.toString(batchQueue));
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
            stringBuffer.append(String.format("%8.2f",actual));
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
            stringBuffer.append(String.format("%8.2f",max));
        } else {
            stringBuffer.append("???");            
        }
        stringBuffer.append(" Megabyte(s)");
        return stringBuffer.toString();
    }
    /** The batch queue. */
    final private boolean batchQueue;

    /**
     * Checks if is batch queue.
     * 
     * @return true, if is batch queue
     */
    public boolean isBatchQueue() {
        return batchQueue;
    }


}
