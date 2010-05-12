package fr.cls.atoll.motu.library.misc.exception;

/**
 * No variable Exception class of Motu.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class MotuNoVarException extends MotuExceptionBase {

    private static final long serialVersionUID = -1L;

    /**
     * @param message message to post.
     * @param cause native exception.
     */
    public MotuNoVarException(String message, Throwable cause) {
        super(message, cause);
        notifyLogException();

    }

    /**
     * @param message message to post.
     */
    public MotuNoVarException(String message) {
        super(message);
        notifyLogException();
    }

    /**
     * @param cause native exception.
     */
    public MotuNoVarException(Throwable cause) {
        super(cause);
        notifyLogException();
    }

}
