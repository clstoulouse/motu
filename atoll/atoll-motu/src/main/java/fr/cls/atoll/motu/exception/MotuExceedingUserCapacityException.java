package fr.cls.atoll.motu.exception;

import org.apache.log4j.Logger;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2008. <br>
 * <br>
 * Soci�t� : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-02-20 13:00:25 $
 */
public class MotuExceedingUserCapacityException extends MotuExceptionBase {

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(MotuExceedingUserCapacityException.class);

    /** . */
    private static final long serialVersionUID = 1L;

    /**
     * The Constructor.
     * 
     * @param max the max
     * @param userId the user id
     * @param isAnonymous the is anonymous
     */
    public MotuExceedingUserCapacityException(String userId, boolean isAnonymous, int max) {
        super("Exceeding user capacity.");
        this.userId = userId;
        this.isAnmonymous = isAnonymous;
        this.max = max;
        notifyLogException();
    }

    /** The user id. */
    final private String userId;

    /** The is anmonymous. */
    final private boolean isAnmonymous;

    /** The max. */
    final private int max;

    /**
     * Checks if is anmonymous.
     * 
     * @return true, if is anmonymous
     */
    public boolean isAnmonymous() {
        return isAnmonymous;
    }

    /**
     * Gets the user id.
     * 
     * @return the user id
     */
    public String getUserId() {
        return userId;
    }

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
     * @return the max as string
     */
    public String getMaxAsString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(Integer.toString(max));
        stringBuffer.append(" request(s).");
        return stringBuffer.toString();
    }

    /**
     * Gets the user category.
     * 
     * @return the user category
     */
    public String getUserCategory() {
        if (isAnmonymous) {
            return "anonymous";
        } else {
            return "authenticate";

        }
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

        stringBuffer.append("The maximum number of requests is reached for the user '");
        stringBuffer.append(userId);
        stringBuffer.append("' (");
        stringBuffer.append(getUserCategory());
        stringBuffer.append(").\n");
        stringBuffer.append("Please, submit the request later.");
        stringBuffer.append("(Maximum is ");
        stringBuffer.append(getMaxAsString());
        stringBuffer.append(", a negative value means 'unlimited').");
        return stringBuffer.toString();
    }

}
