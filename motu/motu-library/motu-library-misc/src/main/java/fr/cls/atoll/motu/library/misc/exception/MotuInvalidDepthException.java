/**
 * 
 */
package fr.cls.atoll.motu.library.misc.exception;

import org.apache.log4j.Logger;

// CSOFF: MultipleStringLiterals : avoid message in constants declaration and trace log.
/**
 * Depth invalid value exception class.
 * 
 * @author $Author: ccamel $
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:21 $
 * 
 */
public class MotuInvalidDepthException extends MotuExceptionBase {
    /**
     * Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(MotuInvalidDepthException.class);

    private static final long serialVersionUID = -1L;

    /**
     * @param depth depth representation which causes the exception
     * @param cause native exception.
     */
    public MotuInvalidDepthException(String depth, Throwable cause) {
        super("Invalid depth.", cause);
        this.depthString = depth;
        this.depth = Double.MAX_VALUE;
        notifyLogException();

    }

    /**
     * @param depth depth representation which causes the exception
     */
    public MotuInvalidDepthException(String depth) {
        super("Invalid depth.");
        this.depthString = depth;
        this.depth = Double.MAX_VALUE;
        notifyLogException();
    }

    /**
     * @param depth depth representation which causes the exception
     * @param cause native exception.
     */
    public MotuInvalidDepthException(double depth, Throwable cause) {
        super("Invalid depth.", cause);
        this.depth = depth;
        this.depthString = "?";
        notifyLogException();
    }

    /**
     * @param depth depth representation which causes the exception
     */
    public MotuInvalidDepthException(double depth) {
        super("Invalid depth.");
        this.depth = depth;
        this.depthString = "?";
        notifyLogException();
    }

    /**
     * writes exception information into the log.
     */
    public void notifyLogException() {

        super.notifyLogException();
        LOG.warn(notifyException());
    }

    /**
     * @return exception information.
     */
    public String notifyException() {
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(super.notifyException());
        stringBuffer.append("\nDepth:");
        if (depth != Double.MAX_VALUE) {
            stringBuffer.append(Double.toString(depth));
        } else {
            stringBuffer.append(depthString);
        }
        return stringBuffer.toString();
    }

    /**
     * String depth representation which causes the exception..
     */
    final private String depthString;

    /**
     * @return the depthString
     */
    public String getDepthString() {
        return this.depthString;
    }

    /**
     * Dpth representation which causes the exception.
     */
    final private double depth;

    /**
     * @return the depth
     */
    public double getDepth() {
        return this.depth;
    }

    /**
     * @return depth as string
     */
    public String getDepthAsString() {

        if (depth != Double.MAX_VALUE) {
            return Double.toString(depth);
        } else {
            return depthString;
        }
    }

}
// CSON: MultipleStringLiterals
