/* 
 * Motu, a high efficient, robust and Standard compliant Web Server for Geographic
 * Data Dissemination.
 *
 * http://cls-motu.sourceforge.net/
 *
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites) - 
 * http://www.cls.fr - and  Contributors
 *
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
package fr.cls.atoll.motu.web.common.utils.xml;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
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
    private final List<String> errors = new ArrayList<String>();

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
    @Override
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
    @Override
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
    @Override
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
