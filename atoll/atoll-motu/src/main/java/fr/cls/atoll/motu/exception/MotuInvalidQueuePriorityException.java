package fr.cls.atoll.motu.exception;

import org.apache.log4j.Logger;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2008. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-02-20 13:00:25 $
 */
public class MotuInvalidQueuePriorityException extends MotuExceptionBase {

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(MotuInvalidQueuePriorityException.class);

    /** . */
    private static final long serialVersionUID = 1L;

    /**
     * The Constructor.
     * 
     * @param min the min
     * @param max the max
     * @param value the value
     */
    public MotuInvalidQueuePriorityException(int value, int min, int max) {
        super("Invalid queue priority.");
        this.max = max;
        this.min = min;
        this.value = value;
        notifyLogException();
    }

    /** The max. */
    final private int max;

    /** The min. */
    final private int min;

    /** The value. */
    final private int value;

    /**
     * Gets the max.
     * 
     * @return the max
     */
    public int getMax() {
        return this.max;
    }

    /**
     * Gets the min.
     * 
     * @return the min
     */
    public int getMin() {
        return this.min;
    }

    /**
     * Gets the value.
     * 
     * @return the value
     */
    public int getValue() {
        return value;
    }

    /**
     * Gets the max as string.
     * 
     * @return the max as a string representation
     */
    public String getMinAsString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(Integer.toString(min));
        return stringBuffer.toString();
    }

    /**
     * Gets the max as string.
     * 
     * @return the max as string
     */
    public String getMaxAsString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(Integer.toString(max));
        return stringBuffer.toString();
    }

    /**
     * Gets the value as string.
     * 
     * @return the value as string
     */
    public String getValueAsString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(Double.toString(value));
        return stringBuffer.toString();
    }

    /**
     * writes exception information into the log.
     */
    public void notifyLogException() {

        super.notifyLogException();
        LOG.warn(notifyException());
    }

    /**
     * Notify exception.
     * 
     * @return exception information.
     */
    public String notifyException() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(super.notifyException());

        stringBuffer.append("The priotity value ");
        stringBuffer.append(getValueAsString());
        stringBuffer.append(" is not a valid value ");
        stringBuffer.append("(Minimum is ");
        stringBuffer.append(getMinAsString());
        stringBuffer.append("(Maximum is ");
        stringBuffer.append(getMaxAsString());
        return stringBuffer.toString();
    }
}
