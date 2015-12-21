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

import org.apache.log4j.Logger;

// CSOFF: MultipleStringLiterals : avoid message in constants declaration and trace log.

/**
 * Longitude exception class of Motu.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class MotuInvalidLongitudeException extends MotuExceptionBase {
    /**
     * Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(MotuInvalidLongitudeException.class);

    private static final long serialVersionUID = -1L;

    /**
     * @param lon longitude representation which causes the exception
     * @param cause native exception.
     */
    public MotuInvalidLongitudeException(String lon, Throwable cause) {
        super("Invalid longitude.", cause);
        this.lonString = lon;
        this.lon = Double.MAX_VALUE;
        notifyLogException();

    }

    /**
     * @param lon longitude representation which causes the exception
     */
    public MotuInvalidLongitudeException(String lon) {
        super("Invalid longitude.");
        this.lonString = lon;
        this.lon = Double.MAX_VALUE;
        notifyLogException();
    }

    /**
     * @param lon longitude representation which causes the exception
     * @param cause native exception.
     */
    public MotuInvalidLongitudeException(double lon, Throwable cause) {
        super("Invalid longitude.", cause);
        this.lonString = "?";
        this.lon = lon;
        notifyLogException();
    }

    /**
     * @param lon longitude representation which causes the exception
     */
    public MotuInvalidLongitudeException(double lon) {
        super("Invalid longitude.");
        this.lonString = "?";
        this.lon = lon;
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
        stringBuffer.append("\nLongitude:");
        if (lon != Double.MAX_VALUE) {
            stringBuffer.append(Double.toString(lon));
        } else {
            stringBuffer.append(lonString);
        }
        return stringBuffer.toString();
    }

    /**
     * String Longitude representation which causes the exception..
     */
    final private String lonString;

    /**
     * @return the lonString
     */
    public String getLonString() {
        return this.lonString;
    }

    /**
     * Longitude representation which causes the exception..
     */
    final private double lon;

    /**
     * @return the lon
     */
    public double getLon() {
        return this.lon;
    }

    /**
     * @return longitude as string
     */
    public String getLonAsString() {

        if (lon != Double.MAX_VALUE) {
            return Double.toString(lon);
        } else {
            return lonString;
        }
    }

}
// CSON: MultipleStringLiterals
