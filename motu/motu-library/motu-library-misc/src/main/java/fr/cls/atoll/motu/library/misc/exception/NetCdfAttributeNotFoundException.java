package fr.cls.atoll.motu.library.misc.exception;

import org.apache.log4j.Logger;

import ucar.nc2.Variable;

// CSOFF: MultipleStringLiterals : avoid message in constants declaration and trace log.

/**
 * Exception class for NetCDF attribute 'not found' exception.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class NetCdfAttributeNotFoundException extends MotuExceptionBase {
    /**
     * Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(NetCdfAttributeNotFoundException.class);

    private static final long serialVersionUID = -1;

    /**
     * @param attrName name of the 'not found' attribute.
     */
    public NetCdfAttributeNotFoundException(String attrName) {
        super("NetCdf attribute not found.");
        this.attrName = attrName;
        this.netCdfVariable = null;
        notifyLogException();
    }

    /**
     * @param netCdfVariable NetCDF variable that causes the exception
     * @param attrName name of the 'not found' attribute.
     */
    public NetCdfAttributeNotFoundException(Variable netCdfVariable, String attrName) {
        super("NetCdf attribute not found.");
        this.netCdfVariable = netCdfVariable;
        this.attrName = attrName;
        notifyLogException();
    }

    /**
     * writes exception information into the log.
     */
    @Override
    public void notifyLogException() {

        super.notifyLogException();
        LOG.warn(notifyException());
    }

    /**
     * @return exception information.
     */
    @Override
    public String notifyException() {
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(super.notifyException());
        if (netCdfVariable != null) {
            stringBuffer.append(String.format("\nNetCdf variable name: %s\n", netCdfVariable.getName()));
        }
        stringBuffer.append(String.format("\nAttribute name: %s\n", attrName));
        return stringBuffer.toString();
    }

    /**
     * NetCDF varialbe whose attribute is not found.
     */
    final private Variable netCdfVariable;

    /**
     * NetCDF attribute which causes the exception.
     */
    final private String attrName;

}
// CSON: MultipleStringLiterals
