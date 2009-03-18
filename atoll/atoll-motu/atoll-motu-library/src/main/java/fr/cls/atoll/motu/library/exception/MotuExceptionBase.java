/**
 * 
 */
package fr.cls.atoll.motu.library.exception;

import org.apache.log4j.Logger;

// CSOFF: MultipleStringLiterals : avoid message in constants declaration and trace log.

/**
 * Base exception class of Motu.
 * 
 * @author $Author: ccamel $
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:21 $
 * 
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

        if (this.getCause() != null) {
            msg = String.format("%s\nNative Exception Type: %s\nNative Exception Message: %s\n", this.getMessage(), getCause().getClass(), getCause()
                    .getMessage());
        } else {
            msg = this.getMessage();
        }
        return msg;
    }
}
// CSON: MultipleStringLiterals
