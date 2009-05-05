package fr.cls.atoll.motu.library.exception;

import org.apache.log4j.Logger;

/**
 * <br><br>Copyright : Copyright (c) 2008.
 * <br><br>Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.2 $ - $Date: 2009-05-05 14:46:57 $
 */
public class MotuInvalidRequestIdException extends MotuExceptionBase {

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(MotuInvalidRequestIdException.class);

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;
    
    /**
     * The Constructor.
     */
    public MotuInvalidRequestIdException() {
        this(-1);
    }

    /**
     * The Constructor.
     * 
     * @param id the id
     */
    public MotuInvalidRequestIdException(long id) {
        super("Invalid request Id.");
        this.id = id;
        notifyLogException();
    }

    /** The id. */
    final private long id;

    /**
     * Gets the id.
     * 
     * @return the id
     */
    public long getId() {
        return this.id;
    }

    /**
     * Gets the id as string.
     * 
     * @return the id as string
     */
    public String getIdAsString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(Long.toString(id));
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

        stringBuffer.append("The request id '.");
        stringBuffer.append(getIdAsString());
        stringBuffer.append(" ' (or null if negative value) is not valid or unknown.");
        return stringBuffer.toString();
    }
}
