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
package fr.cls.atoll.motu.library.misc.queueserver;

import java.util.Calendar;
import java.util.Date;

import fr.cls.atoll.motu.api.message.xml.ErrorType;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class QueueLogError {

    /**
     * Constructor.
     * 
     * @param message the message
     * @param errorType the error type
     */
    public QueueLogError(ErrorType errorType, String message) {
        setErrorCode(errorType);
        setMessage(message);
        setDateError();
    }

    // private Exception exception = null;
    /** The error code. */
    private String errorCode = null;

    /** The message. */
    private String message = null;

    /** The date error. */
    @SuppressWarnings("unused")
    private Date dateError = null;

    /**
     * Gets the error code.
     * 
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Gets the error date.
     * 
     * @return the error date
     */
    public Date getDateError() {
        return dateError;
    }

    /**
     * Sets the error code.
     * 
     * @param errorCode the error code
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Sets the error code.
     * 
     * @param errorType the error type
     */
    public void setErrorCode(ErrorType errorType) {
        this.errorCode = errorType.toString();
    }

    /**
     * Gets the message.
     * 
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message.
     * 
     * @param message the message
     */
    public void setMessage(String message) {
        // To avoid XML processing error, remove control char. and non-printable char. from message.
        this.message = message.replaceAll("\\p{Cntrl}|[^\\p{Print}]", "");
        ;
    }

    /**
     * Sets the date error.
     */
    public void setDateError() {
        this.dateError = Calendar.getInstance().getTime();
    }

    /**
     * To string.
     * 
     * @return the string
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("ERROR is: ");
        buffer.append("code:");
        buffer.append((errorCode == null ? "null" : errorCode));
        buffer.append(" - cause:");
        buffer.append((message == null ? "null" : message));

        return buffer.toString();

    }
    // public String toXML() {
    // XStreamer streamer = new XStreamer();
    // StringBuffer stringBuffer = new StringBuffer();
    // try {
    // stringBuffer.append(streamer.toXML(xStream, this));
    // // stringBuffer.append(streamer.toXML(queueLogInfo.getXStream(), queueLogInfo));
    // } catch (ObjectStreamException e) {
    // e.printStackTrace();
    // }
    // return stringBuffer.toString();
    //
    // }

}
