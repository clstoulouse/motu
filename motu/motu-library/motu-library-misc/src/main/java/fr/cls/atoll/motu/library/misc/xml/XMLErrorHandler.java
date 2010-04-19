package fr.cls.atoll.motu.library.misc.xml;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2008. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: ccamel $
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:21 $
 */
public class XMLErrorHandler extends DefaultHandler {

    /**
     * Emumeration for available formats.
     */
    public enum XMLErrorType {

        /** The WARNING. */
        WARNING,

        /** The ERROR. */
        ERROR,

        /** The FATAL. */
        FATAL,

    };

    /**
     * The Constructor.
     */
    public XMLErrorHandler() {
    }

    /** The error count. */
    private int errorCount = 0;

    /**
     * Gets the error count.
     * 
     * @return the error count
     */
    public int getErrorCount() {
        return errorCount;
    }

    /** The errors. */
    private List<String> errors = new ArrayList<String>();

    /**
     * Gets the errors.
     * 
     * @return the errors
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * Warning.
     * 
     * @param ex the exception
     * 
     * @throws SAXException the SAX exception
     */
    public void warning(SAXParseException ex) throws SAXException {
        processException(ex, XMLErrorType.WARNING);
    }

    /**
     * Error.
     * 
     * @param ex the exception
     * 
     * @throws SAXException the SAX exception
     */
    public void error(SAXParseException ex) throws SAXException {
        processException(ex, XMLErrorType.ERROR);
    }

    /**
     * Fatal error.
     * 
     * @param ex the exception
     * 
     * @throws SAXException the SAX exception
     */
    public void fatalError(SAXParseException ex) throws SAXException {
        processException(ex, XMLErrorType.FATAL);
    }

    /**
     * Process exception.
     * 
     * @param ex the exception
     * @param type the type
     */
    public void processException(SAXParseException ex, XMLErrorType type) {
        // System.err.println(exception);
        StringBuffer stringBuffer = new StringBuffer();
        int lineNumber = ex.getLineNumber();
        int colNumber = ex.getColumnNumber();
        if (lineNumber > 0) {
            stringBuffer.append("Line: ");
            stringBuffer.append(lineNumber);
            stringBuffer.append(", ");
        }
        if (lineNumber > 0) {
            stringBuffer.append("Column: ");
            stringBuffer.append(colNumber);
            stringBuffer.append(", ");
        }
        stringBuffer.append("Severity: ");
        stringBuffer.append(type.toString());
        stringBuffer.append(", Description: ");
        stringBuffer.append(ex.getMessage());
        errors.add(stringBuffer.toString());
        errorCount++;
    }

}
