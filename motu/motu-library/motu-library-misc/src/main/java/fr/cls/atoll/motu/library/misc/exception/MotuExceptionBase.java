package fr.cls.atoll.motu.library.misc.exception;

import org.apache.log4j.Logger;

// CSOFF: MultipleStringLiterals : avoid message in constants declaration and trace log.

/**
 * Base exception class of Motu.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class MotuExceptionBase extends Exception {
    /**
     * Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(MotuExceptionBase.class);

    private static final long serialVersionUID = -1L;

    /**
     * @param message message to post.
     * @param cause native exception.
     */
    public MotuExceptionBase(String message, Throwable cause) {
        super(message, cause);

    }

    /**
     * @param message message to post.
     */
    public MotuExceptionBase(String message) {
        super(message);
    }

    /**
     * @param cause native exception.
     */
    public MotuExceptionBase(Throwable cause) {
        super(cause);
    }

    /**
     * writes exception information into the log.
     */
    public void notifyLogException() {
        LOG.warn("Exception class: " + this.getClass().getName());
        LOG.warn(this.getMessage());
        if (this.getCause() != null) {
            LOG.warn("Native Exception: " + getCause().getClass());
            LOG.warn("Native Exception Message: " + getCause().getMessage());
            LOG.warn(getCause());
        }
    }

    /**
     * @see MotuExceptionBase#getMessage()
     * @return exception information.
     */
    public String notifyException() {
        String msg;

        if (this.getCause() == null) {
            msg = this.getMessage();
            return msg;
        }

        Throwable thisCause = this;
        Throwable nativeCause = this.getCause();
        StringBuffer stringBuffer = new StringBuffer();

        msg = String.format("%s\n", this.getMessage());
        stringBuffer.append(msg);

        while (nativeCause != null) {
            if (nativeCause == thisCause) {
                break;
            }
            msg = String.format("Native Exception Type: %s\nNative Exception Message: %s\n", nativeCause.getClass(), nativeCause.getMessage());

            stringBuffer.append(msg);

            thisCause = nativeCause;
            nativeCause = nativeCause.getCause();
        }

        return stringBuffer.toString();
    }
}
// CSON: MultipleStringLiterals
