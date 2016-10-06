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
 * Latitude exception class of Motu.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class MotuInvalidLatitudeException extends MotuExceptionBase {

    private static final long serialVersionUID = -1L;

    /**
     * Latitude representation which causes the exception..
     */
    final private double lat;

    /**
     * String Latitude representation which causes the exception..
     */
    final private String latString;

    public MotuInvalidLatitudeException(String latStr, Double lat, Throwable cause) {
        super(getErrorMessage(latStr, lat), cause);
        this.latString = latStr == null ? "?" : latStr;
        this.lat = lat == null ? Double.MAX_VALUE : lat;
    }

    /**
     * @param lat latitude representation which causes the exception
     * @param cause native exception.
     */
    public MotuInvalidLatitudeException(String lat, Throwable cause) {
        this(lat, null, cause);
    }

    /**
     * @param lat latitude representation which causes the exception
     */
    public MotuInvalidLatitudeException(String lat) {
        this(lat, null, null);
    }

    /**
     * @param lat latitude representation which causes the exception
     * @param cause native exception.
     */
    public MotuInvalidLatitudeException(double lat, Throwable cause) {
        this(null, lat, cause);
    }

    /**
     * @param lat latitdue representation which causes the exception
     */
    public MotuInvalidLatitudeException(double lat) {
        this(null, lat, null);
    }

    public static String getErrorMessage(String latString, Double lat) {
        StringBuffer stringBuffer = new StringBuffer("Invalid latitude. ");

        stringBuffer.append("Latitude:");
        if (lat != Double.MAX_VALUE) {
            stringBuffer.append(Double.toString(lat));
        } else {
            stringBuffer.append(latString);
        }
        return stringBuffer.toString();
    }

    /**
     * @return the latString
     */
    public String getLatString() {
        return this.latString;
    }

    /**
     * @return the lat
     */
    public double getLat() {
        return this.lat;
    }

    /**
     * @return latitude as string
     */
    public String getLatAsString() {

        if (lat != Double.MAX_VALUE) {
            return Double.toString(lat);
        } else {
            return latString;
        }
    }

}
// CSON: MultipleStringLiterals
