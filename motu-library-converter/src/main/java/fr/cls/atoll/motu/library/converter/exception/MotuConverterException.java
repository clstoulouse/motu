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
package fr.cls.atoll.motu.library.converter.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// CSOFF: MultipleStringLiterals : avoid message in constants declaration and trace log.

/**
 * Base exception class of Motu.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class MotuConverterException extends Exception {
    /** Logger for this class. */
    private static final Logger LOG = LogManager.getLogger();

    private static final long serialVersionUID = -1L;

    /**
     * @param message message to post.
     * @param cause native exception.
     */
    public MotuConverterException(String message, Throwable cause) {
        super(message, cause);

    }

    /**
     * @param message message to post.
     */
    public MotuConverterException(String message) {
        super(message);
    }

    /**
     * @param cause native exception.
     */
    public MotuConverterException(Throwable cause) {
        super(cause);
    }

    /**
     * writes exception information into the log.
     */
    public void notifyLogException() {
        LOG.warn("Exception class: " + this.getClass().getName());
        LOG.warn(this.getMessage());
        if (this.getCause() != null) {
            LOG.warn("Native Exception: " + getCause().getClass());
            LOG.warn("Native Exception Message: " + getCause().getMessage());
            LOG.warn(getCause());
        }
    }

    /**
     * @see MotuConverterException#getMessage()
     * @return exception information.
     */
    public String notifyException() {
        String msg;

        if (this.getCause() == null) {
            msg = this.getMessage();
            return msg;
        }

        Throwable thisCause = this;
        Throwable nativeCause = this.getCause();
        StringBuffer stringBuffer = new StringBuffer();

        msg = String.format("%s\n", this.getMessage());
        stringBuffer.append(msg);

        while (nativeCause != null) {
            if (nativeCause == thisCause) {
                break;
            }
            msg = String.format("Native Exception Type: %s\nNative Exception Message: %s\n", nativeCause.getClass(), nativeCause.getMessage());

            stringBuffer.append(msg);

            thisCause = nativeCause;
            nativeCause = nativeCause.getCause();
        }

        return stringBuffer.toString();
    }
}
// CSON: MultipleStringLiterals
