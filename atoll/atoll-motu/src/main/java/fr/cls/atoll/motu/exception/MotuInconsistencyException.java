package fr.cls.atoll.motu.exception;

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
public class MotuInconsistencyException extends MotuExceptionBase {

    /**
     * .
     */
    private static final long serialVersionUID = 1L;

    /**
     * The Constructor.
     * 
     * @param message the message
     * @param cause the cause
     */
    public MotuInconsistencyException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * The Constructor.
     * 
     * @param message the message
     */
    public MotuInconsistencyException(String message) {
        super(message);
    }

    /**
     * The Constructor.
     * 
     * @param cause the cause
     */
    public MotuInconsistencyException(Throwable cause) {
        super(cause);
    }

}
