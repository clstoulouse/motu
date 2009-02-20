/**
 * 
 */
package fr.cls.atoll.motu.exception;

import org.apache.log4j.Logger;

/**
 * Exception class for NetCDF variable 'not found' exception.
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-02-20 13:00:25 $
 * 
 */
public class NetCdfVariableNotFoundException extends MotuExceptionBase {
    /**
     * Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(NetCdfVariableNotFoundException.class);

    private static final long serialVersionUID = -1;

    /**
     * @param varName name of the 'not found' variable.
     */
    public NetCdfVariableNotFoundException(String varName) {
        super("NetCdf variable not found.");
        this.varName = varName;
        notifyLogException();
    }

    /**
     * writes exception information into the log.
     */
    public void notifyLogException() {

        super.notifyLogException();
        LOG.warn("Variable name: " + varName);
    }

    /**
     * @return exception information.
     */
    public String notifyException() {
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(super.notifyException());
        stringBuffer.append(String.format("\nVariable name: %s\n", varName));
        return stringBuffer.toString();
    }

    /**
     * NetCDF attribute which causes the exception.
     */
    final private String varName;

}
