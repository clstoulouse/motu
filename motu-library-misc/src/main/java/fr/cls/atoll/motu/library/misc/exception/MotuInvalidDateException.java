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
package fr.cls.atoll.motu.library.misc.exception;

import fr.cls.atoll.motu.library.misc.netcdf.NetCdfReader;

import java.util.Date;

import org.apache.log4j.Logger;

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
    /**
     * Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(MotuInvalidDateException.class);

    private static final long serialVersionUID = -1L;

    /**
     * @param date string date representation which causes the exception
     * @param cause native exception.
     */
    public MotuInvalidDateException(String date, Throwable cause) {
        super("Invalid date.", cause);
        this.dateString = date;
        this.date = null;
        //notifyLogException();

    }

    /**
     * @param date string date representation which causes the exception
     */
    public MotuInvalidDateException(String date) {
        super("Invalid date.");
        this.dateString = date;
        this.date = null;
        //notifyLogException();
    }

    /**
     * @param date Date representation which causes the exception
     * @param cause native exception.
     */
    public MotuInvalidDateException(Date date, Throwable cause) {
        super("Invalid date.", cause);
        this.dateString = "?";
        this.date = date;
        //notifyLogException();
    }

    /**
     * @param date Date representation which causes the exception
     */
    public MotuInvalidDateException(Date date) {
        super("Invalid date.");
        this.dateString = "?";
        this.date = date;
        //notifyLogException();
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
        stringBuffer.append("\nDate:");
        if (date != null) {
            stringBuffer.append(NetCdfReader.DATETIME_TO_STRING_DEFAULT.format(date));
        } else {
            stringBuffer.append(dateString);
        }
        return stringBuffer.toString();
    }

    /**
     * String date representation which causes the exception..
     */
    final private String dateString;

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

    /**
     * @return date as string
     */
    public String getDateAsString() {

        if (date != null) {
            return NetCdfReader.DATETIME_TO_STRING_DEFAULT.format(date);
        } else {
            return dateString;
        }
    }

}
// CSON: MultipleStringLiterals
