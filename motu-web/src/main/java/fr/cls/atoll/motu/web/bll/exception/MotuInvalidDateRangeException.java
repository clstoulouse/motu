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
 * Date range exception class of Motu.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class MotuInvalidDateRangeException extends MotuExceptionBase {

    /** Date/time format. */
    public final static String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -1L;

    /**
     * The Constructor.
     *
     * @param invalidRangeMin invalid Date range min. representation which causes the exception
     * @param invalidRangeMax invalid Date range max. representation which causes the exception
     * @param validRangeMin valid Date range min. representation
     * @param validRangeMax valid Date range max. representation
     */
    public MotuInvalidDateRangeException(Date invalidRangeMin, Date invalidRangeMax, Date validRangeMin, Date validRangeMax) {
        this(invalidRangeMin, invalidRangeMax, validRangeMin, validRangeMax, null);
    }

    /**
     * The Constructor.
     *
     * @param invalidRangeMin invalid Date range min. representation which causes the exception
     * @param invalidRangeMax invalid Date range max. representation which causes the exception
     * @param validRangeMin valid Date range min. representation
     * @param validRangeMax valid Date range max. representation
     * @param cause native exception.
     */
    public MotuInvalidDateRangeException(Date invalidRangeMin, Date invalidRangeMax, Date validRangeMin, Date validRangeMax, Throwable cause) {
        super(getErrorMessage(invalidRangeMin, invalidRangeMax, validRangeMin, validRangeMax), cause);

        this.invalidRange = new Date[2];
        this.invalidRange[0] = invalidRangeMin;
        this.invalidRange[1] = invalidRangeMax;

        this.validRange = new Date[2];
        this.validRange[0] = validRangeMin;
        this.validRange[1] = validRangeMax;
    }

    public static String getErrorMessage(Date invalidRangeMin, Date invalidRangeMax, Date validRangeMin, Date validRangeMax) {
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append("Invalid date range: ");
        stringBuffer.append(getInvalidRangeAsString(invalidRangeMin, invalidRangeMax));
        stringBuffer.append(". ");
        stringBuffer.append("Valid range is: ");
        stringBuffer.append(getValidRangeAsString(validRangeMin, validRangeMax));
        stringBuffer.append(". ");

        // stringBuffer.append(getNearestValidValuesMessage());

        return stringBuffer.toString();
    }

    /**
     * The Constructor.
     *
     * @param invalidRange invalid Date range representation which causes the exception
     * @param validRange valid Date range representation
     * @param cause native exception.
     */
    public MotuInvalidDateRangeException(Date[] invalidRange, Date[] validRange, Throwable cause) {
        super("Invalid date range.", cause);

        assert invalidRange.length == 2;
        assert validRange.length == 2;

        this.invalidRange = invalidRange;
        this.validRange = validRange;
    }

    /**
     * The Constructor.
     *
     * @param invalidRange invalid Date range representation which causes the exception
     * @param validRange valid Date range representation
     */
    public MotuInvalidDateRangeException(Date[] invalidRange, Date[] validRange) {
        super("Invalid date range.");

        assert invalidRange.length == 2;
        assert validRange.length == 2;

        this.invalidRange = invalidRange;
        this.validRange = validRange;
    }

    /**
     * Date range representation which causes the exception.
     */
    final private Date[] invalidRange;

    /**
     * Gets the invalid range.
     *
     * @return the invalidRange
     */
    public Date[] getInvalidRange() {
        return this.invalidRange;
    }

    /**
     * Valid Date range representation.
     */
    final private Date[] validRange;

    /**
     * Gets the valid range.
     *
     * @return the validRange
     */
    public Date[] getValidRange() {
        return this.validRange;
    }

    /** The nearest valid values. */
    private Date[] nearestValidValues = null;

    /**
     * Gets the nearest valid values.
     *
     * @return the nearest valid values
     */
    public Date[] getNearestValidValues() {
        return nearestValidValues;
    }

    /**
     * Sets the nearest valid values.
     *
     * @param min the min
     * @param max the max
     */
    public void setNearestValidValues(Date min, Date max) {
        this.nearestValidValues = new Date[2];
        this.nearestValidValues[0] = min;
        this.nearestValidValues[1] = max;
    }

    /**
     * Sets the nearest valid values.
     *
     * @param nearestValidRange the nearest valid values
     */
    public void setNearestValidValues(Date[] nearestValidRange) {
        this.nearestValidValues = nearestValidRange;
    }

    /**
     * Gets the valid range as string.
     *
     * @return the validRange as a string interval representation
     */
    private static String getValidRangeAsString(Date validRangeMin, Date validRangeMax) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("[");
        stringBuffer.append(new SimpleDateFormat(DATETIME_FORMAT).format(validRangeMin));
        stringBuffer.append(",");
        stringBuffer.append(new SimpleDateFormat(DATETIME_FORMAT).format(validRangeMax));
        stringBuffer.append("]");
        return stringBuffer.toString();
    }

    /**
     * Gets the invalid range as string.
     *
     * @return the invalidRange as a string interval representation
     */
    private static String getInvalidRangeAsString(Date inValidRangeMin, Date inValidRangeMax) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("[");
        stringBuffer.append(new SimpleDateFormat(DATETIME_FORMAT).format(inValidRangeMin));
        stringBuffer.append(",");
        stringBuffer.append(new SimpleDateFormat(DATETIME_FORMAT).format(inValidRangeMax));
        stringBuffer.append("]");
        return stringBuffer.toString();
    }

    /**
     * Gets the nearest valid values as string.
     *
     * @return the nearest valid values as string
     */
    public static String getNearestValidValuesAsString(Date nearestValidValuesMin, Date nearestValidValuesMax) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("values <= ");
        stringBuffer.append(new SimpleDateFormat(DATETIME_FORMAT).format(nearestValidValuesMin));
        stringBuffer.append(", values >= ");
        stringBuffer.append(new SimpleDateFormat(DATETIME_FORMAT).format(nearestValidValuesMax));
        return stringBuffer.toString();
    }

}
