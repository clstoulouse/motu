/**
 * 
 */
package fr.cls.atoll.motu.exception;

/**
 * General exception class of Motu.
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-02-20 13:00:25 $
 * 
 */
public class MotuException extends MotuExceptionBase {

    private static final long serialVersionUID = -1L;

    /**
     * @param message message to post.
     * @param cause native exception.
     */
    public MotuException(String message, Throwable cause) {
        super(message, cause);
        notifyLogException();

    }

    /**
     * @param message message to post.
     */
    public MotuException(String message) {
        super(message);
        notifyLogException();
    }

    /**
     * @param cause native exception.
     */
    public MotuException(Throwable cause) {
        super(cause);
        notifyLogException();
    }

}
