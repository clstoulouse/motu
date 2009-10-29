package fr.cls.atoll.motu.library.exception;

import javax.xml.bind.JAXBException;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2008. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.2 $ - $Date: 2009-10-29 10:51:20 $
 */
public class MotuMarshallException extends MotuExceptionBase {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * The Constructor.
     * 
     * @param message the message
     * @param cause the cause
     */
    public MotuMarshallException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * The Constructor.
     * 
     * @param message the message
     * @param cause the cause
     */
    public MotuMarshallException(String message, JAXBException cause) {
        super(message, ((cause.getLinkedException() != null) ? cause.getLinkedException() : cause));

    }

    /**
     * The Constructor.
     * 
     * @param message the message
     */
    public MotuMarshallException(String message) {
        super(message);
    }

    /**
     * The Constructor.
     * 
     * @param cause the cause
     */
    public MotuMarshallException(Throwable cause) {
        super(cause);
    }
    /**
     * The Constructor.
     * 
     * @param cause the cause
     */
    public MotuMarshallException(JAXBException cause) {
        super(((cause.getLinkedException() != null) ? cause.getLinkedException() : cause));

    }

}
