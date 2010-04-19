package fr.cls.atoll.motu.library.misc.exception;

import org.apache.log4j.Logger;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2008. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: ccamel $
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:21 $
 */
public class MotuExceedingQueueCapacityException extends MotuExceptionBase {

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(MotuExceedingQueueCapacityException.class);

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * The Constructor.
     * 
     * @param max the max
     */
    public MotuExceedingQueueCapacityException(int max) {
        super("Exceeding queue capacity.");
        this.max = max;
        notifyLogException();
    }

    /** The max. */
    final private int max;

    /**
     * Gets the max.
     * 
     * @return the max
     */
    public int getMax() {
        return this.max;
    }

    /**
     * Gets the max as string.
     * 
     * @return the max as a string representation
     */
    public String getMaxAsString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(Integer.toString(max));
        stringBuffer.append(" request(s)");
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

        stringBuffer.append("The maximum number of requests is reached for the time being. Please, submit the request later.");
        if (max != Integer.MAX_VALUE) {
            stringBuffer.append("(Maximum is ");
            stringBuffer.append(getMaxAsString());
            stringBuffer.append(", a negative value means 'unlimited').");
        }
        return stringBuffer.toString();
    }

}
