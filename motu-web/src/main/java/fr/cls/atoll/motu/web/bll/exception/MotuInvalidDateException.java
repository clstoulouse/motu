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
package fr.cls.atoll.motu.web.bll.exception;

import java.text.SimpleDateFormat;
import java.util.Date;

// CSOFF: MultipleStringLiterals : avoid message in constants declaration and trace log.

/**
 * Date exception class of Motu.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class MotuInvalidDateException extends MotuExceptionBase {

    private static final long serialVersionUID = -1L;

    /**
     * String date representation which causes the exception..
     */
    final private String dateString;

    public MotuInvalidDateException(String dateStr, Date date, Throwable cause) {
        super(getErrorMessage(dateStr, date), cause);
        this.dateString = dateStr == null ? "?" : dateStr;
        this.date = date;
    }

    /**
     * @param date string date representation which causes the exception
     * @param cause native exception.
     */
    public MotuInvalidDateException(String date, Throwable cause) {
        this(date, null, cause);
    }

    /**
     * @param date string date representation which causes the exception
     */
    public MotuInvalidDateException(String date) {
        this(date, null, null);
    }

    /**
     * @param date Date representation which causes the exception
     * @param cause native exception.
     */
    public MotuInvalidDateException(Date date, Throwable cause) {
        this(null, date, cause);
    }

    /**
     * @param date Date representation which causes the exception
     */
    public MotuInvalidDateException(Date date) {
        this(null, date, null);
    }

    public static String getErrorMessage(String dateString, Date date) {
        StringBuffer stringBuffer = new StringBuffer("Invalid date. ");

        stringBuffer.append("Date:");
        if (date != null) {
            stringBuffer.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
        } else {
            stringBuffer.append(dateString);
        }
        return stringBuffer.toString();
    }

    /**
     * @return the dateString
     */
    public String getDateString() {
        return this.dateString;
    }

    /**
     * Date representation which causes the exception..
     */
    final private Date date;

    /**
     * @return the date
     */
    public Date getDate() {
        return this.date;
    }
}
