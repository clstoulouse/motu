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

    private static final long serialVersionUID = -1L;

    /**
     * Longitude representation which causes the exception..
     */
    final private double lon;

    /**
     * String Longitude representation which causes the exception..
     */
    final private String lonString;

    public MotuInvalidLongitudeException(String lonStr, Double lon, Throwable cause) {
        super(getErrorMessage(lonStr, lon), cause);
        this.lonString = lonStr == null ? "?" : lonStr;
        this.lon = lon == null ? Double.MAX_VALUE : lon;

    }

    public static String getErrorMessage(String lonString, Double lon) {
        StringBuffer stringBuffer = new StringBuffer("Invalid longitude. ");

        stringBuffer.append("Longitude:");
        if (lon != Double.MAX_VALUE) {
            stringBuffer.append(Double.toString(lon));
        } else {
            stringBuffer.append(lonString);
        }
        return stringBuffer.toString();
    }

    /**
     * @param lon longitude representation which causes the exception
     * @param cause native exception.
     */
    public MotuInvalidLongitudeException(String lon, Throwable cause) {
        this(lon, null, cause);
    }

    /**
     * @param lon longitude representation which causes the exception
     */
    public MotuInvalidLongitudeException(String lon) {
        this(lon, null, null);
    }

    /**
     * @param lon longitude representation which causes the exception
     * @param cause native exception.
     */
    public MotuInvalidLongitudeException(double lon, Throwable cause) {
        this(null, lon, cause);
    }

    /**
     * @param lon longitude representation which causes the exception
     */
    public MotuInvalidLongitudeException(double lon) {
        this(null, lon, null);
    }

    /**
     * @return the lonString
     */
    public String getLonString() {
        return this.lonString;
    }

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
