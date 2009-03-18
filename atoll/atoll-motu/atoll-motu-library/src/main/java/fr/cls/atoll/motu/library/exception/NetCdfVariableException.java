/**
 * 
 */
package fr.cls.atoll.motu.library.exception;

import org.apache.log4j.Logger;

import ucar.nc2.Variable;

/**
 * Exception class for NetCDF variable exception.
 * 
 * @author $Author: ccamel $
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:21 $
 * 
 */
public class NetCdfVariableException extends MotuExceptionBase {
    /**
     * Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(NetCdfVariableException.class);

    private static final long serialVersionUID = -1;

    /**
     * @param message message to post.
     * @param cause native exception.
     */
    public NetCdfVariableException(String message, Throwable cause) {
        super(message, cause);
        this.netCdfVariable = null;
        notifyLogException();
    }

    /**
     * @param message message to post.
     */
    public NetCdfVariableException(String message) {
        super(message);
        this.netCdfVariable = null;
        notifyLogException();
    }

    /**
     * @param cause
     * @param cause native exception.
     */
    public NetCdfVariableException(Throwable cause) {
        super(cause);
        this.netCdfVariable = null;
        notifyLogException();
    }

    /**
     * @param netCdfVariable NetCDF variable that causes the exception
     * @param message message to post.
     * @param cause native exception.
     */
    public NetCdfVariableException(Variable netCdfVariable, String message, Throwable cause) {
        super(message, cause);
        this.netCdfVariable = netCdfVariable;
        notifyLogException();
    }

    /**
     * @param netCdfVariable NetCDF variable that causes the exception
     * @param message message to post.
     */
    public NetCdfVariableException(Variable netCdfVariable, String message) {
        super(message);
        this.netCdfVariable = netCdfVariable;
        notifyLogException();
    }

    /**
     * @param netCdfVariable NetCDF variable that causes the exception
     * @param cause native exception.
     */
    public NetCdfVariableException(Variable netCdfVariable, Throwable cause) {
        super(cause);
        this.netCdfVariable = netCdfVariable;
        // CSOFF: StrictDuplicateCode : normal duplication code.
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
        return stringBuffer.toString();
    }

    /**
     * NetCDF variable which causes the exception.
     */
    final private Variable netCdfVariable;

    /**
     * @return the netCdfVariable
     */
    public Variable getNetCdfVariable() {
        return this.netCdfVariable;
    }
}
