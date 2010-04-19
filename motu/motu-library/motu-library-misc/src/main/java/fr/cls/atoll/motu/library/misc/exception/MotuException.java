/**
 * 
 */
package fr.cls.atoll.motu.library.misc.exception;

/**
 * General exception class of Motu.
 * 
 * @author $Author: ccamel $
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:21 $
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
