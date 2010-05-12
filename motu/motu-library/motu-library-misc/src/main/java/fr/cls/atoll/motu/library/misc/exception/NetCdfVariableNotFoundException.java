package fr.cls.atoll.motu.library.misc.exception;

import org.apache.log4j.Logger;

/**
 * Exception class for NetCDF variable 'not found' exception.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
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
    @Override
    public void notifyLogException() {

        super.notifyLogException();
        LOG.warn("Variable name: " + varName);
    }

    /**
     * @return exception information.
     */
    @Override
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
