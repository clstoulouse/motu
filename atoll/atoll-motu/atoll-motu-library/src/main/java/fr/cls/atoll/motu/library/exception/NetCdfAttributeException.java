/**
 * 
 */
package fr.cls.atoll.motu.library.exception;

import org.apache.log4j.Logger;

import ucar.nc2.Attribute;
import ucar.nc2.Variable;

/**
 * Exception class for NetCDF attribute exception.
 * 
 * @author $Author: ccamel $
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:21 $
 * 
 */
public class NetCdfAttributeException extends MotuExceptionBase {
    /**
     * Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(NetCdfAttributeException.class);

    private static final long serialVersionUID = -1;

    /**
     * @param message message to post.
     * @param cause native exception.
     */
    public NetCdfAttributeException(String message, Throwable cause) {
        super(message, cause);
        this.netCdfAttribute = null;
        this.netCdfVariable = null;
        notifyLogException();
    }

    /**
     * @param message message to post.
     */
    public NetCdfAttributeException(String message) {
        super(message);
        this.netCdfAttribute = null;
        this.netCdfVariable = null;
        notifyLogException();
    }

    /**
     * @param cause
     * @param cause native exception.
     */
    public NetCdfAttributeException(Throwable cause) {
        super(cause);
        this.netCdfAttribute = null;
        this.netCdfVariable = null;
        notifyLogException();
    }

    /**
     * @param netCdfAttribute NetCDF attribute that causes the exception
     * @param message message to post.
     * @param cause native exception.
     */
    public NetCdfAttributeException(Attribute netCdfAttribute, String message, Throwable cause) {
        super(message, cause);
        this.netCdfAttribute = netCdfAttribute;
        this.netCdfVariable = null;
        notifyLogException();
    }

    /**
     * @param netCdfAttribute NetCDF attribute that causes the exception
     * @param message message to post.
     */
    public NetCdfAttributeException(Attribute netCdfAttribute, String message) {
        super(message);
        this.netCdfAttribute = netCdfAttribute;
        this.netCdfVariable = null;
        notifyLogException();
    }

    /**
     * @param netCdfAttribute NetCDF attribute that causes the exception
     * @param cause native exception.
     */
    public NetCdfAttributeException(Attribute netCdfAttribute, Throwable cause) {
        super(cause);
        this.netCdfAttribute = netCdfAttribute;
        this.netCdfVariable = null;
        notifyLogException();
    }

    /**
     * @param netCdfVariable NetCDF variable that causes the exception
     * @param netCdfAttribute NetCDF attribute that causes the exception
     * @param message message to post.
     * @param cause native exception.
     */
    public NetCdfAttributeException(Variable netCdfVariable, Attribute netCdfAttribute, String message, Throwable cause) {
        super(message, cause);
        this.netCdfVariable = netCdfVariable;
        this.netCdfAttribute = netCdfAttribute;
        notifyLogException();
    }

    /**
     * @param netCdfVariable NetCDF variable that causes the exception
     * @param netCdfAttribute NetCDF attribute that causes the exception
     * @param message message to post.
     */
    public NetCdfAttributeException(Variable netCdfVariable, Attribute netCdfAttribute, String message) {
        super(message);
        this.netCdfVariable = netCdfVariable;
        this.netCdfAttribute = netCdfAttribute;
        notifyLogException();
    }

    /**
     * @param netCdfVariable NetCDF variable that causes the exception
     * @param netCdfAttribute NetCDF attribute that causes the exception
     * @param cause native exception.
     */
    public NetCdfAttributeException(Variable netCdfVariable, Attribute netCdfAttribute, Throwable cause) {
        super(cause);
        this.netCdfVariable = netCdfVariable;
        this.netCdfAttribute = netCdfAttribute;
        notifyLogException();
    }

    /**
     * @param netCdfVariable NetCDF variable that causes the exception
     * @param message message to post.
     * @param cause native exception.
     */
    public NetCdfAttributeException(Variable netCdfVariable, String message, Throwable cause) {
        super(message, cause);
        this.netCdfAttribute = null;
        this.netCdfVariable = netCdfVariable;
        notifyLogException();
    }

    /**
     * @param netCdfVariable NetCDF variable that causes the exception
     * @param message message to post.
     */
    public NetCdfAttributeException(Variable netCdfVariable, String message) {
        super(message);
        this.netCdfAttribute = null;
        this.netCdfVariable = netCdfVariable;
        notifyLogException();
    }

    /**
     * @param netCdfVariable NetCDF variable that causes the exception
     * @param cause native exception.
     */
    public NetCdfAttributeException(Variable netCdfVariable, Throwable cause) {
        super(cause);
        this.netCdfAttribute = null;
        // CSOFF: StrictDuplicateCode : normal duplication code.
        this.netCdfVariable = netCdfVariable;
        notifyLogException();
    }

    /**
     * writes exception information into the log.
     */
    public void notifyLogException() {

        super.notifyLogException();
        LOG.warn(notifyException());
    }

    // CSON: StrictDuplicateCode

    /**
     * @return exception information.
     */
    public String notifyException() {
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(super.notifyException());

        if (netCdfVariable != null) {
            stringBuffer.append(String.format("\nNetCdf variable name: %s\n", netCdfVariable.getName()));
        }
        if (netCdfAttribute != null) {
            stringBuffer.append(String.format("\nNetCdf attribute name: %s\n", netCdfAttribute.getName()));
        }
        return stringBuffer.toString();
    }

    /**
     * NetCDF attribute which causes the exception.
     */
    final private Attribute netCdfAttribute;

    /**
     * @return the netCdfAttribute
     */
    public Attribute getNetCdfAttribute() {
        return this.netCdfAttribute;
    }

    /**
     * NetCDF variable whose attribute causes the exception.
     */
    final private Variable netCdfVariable;

    /**
     * @return the neCdfVariable
     */
    public Variable getNetCdfVariable() {
        return this.netCdfVariable;
    }

}
