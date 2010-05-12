/**
 * 
 */
package fr.cls.atoll.motu.library.misc.exception;

/**
 * A class to indicate that a block of code has not been implemented.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class MotuNotImplementedException extends MotuExceptionBase {

    private static final long serialVersionUID = 1L;

    /**
     * @param message message to post.
     * @param cause native exception.
     */
    public MotuNotImplementedException(String message, Throwable cause) {
        super(message, cause);
        notifyLogException();
    }

    /**
     * @param message message to post.
     */
    public MotuNotImplementedException(String message) {
        super(message);
        notifyLogException();
    }

    /**
     * @param cause native exception.
     */
    public MotuNotImplementedException(Throwable cause) {
        super(cause);
        notifyLogException();
    }

}
