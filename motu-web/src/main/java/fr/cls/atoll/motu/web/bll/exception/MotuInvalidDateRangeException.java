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
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

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
    public MotuInvalidDateRangeException(
        Date invalidRangeMin,
        Date invalidRangeMax,
        Date validRangeMin,
        Date validRangeMax,
        Date nearestValidMin,
        Date nearestValidMax,
        Throwable cause) {
        super(getErrorMessage(invalidRangeMin, invalidRangeMax, validRangeMin, validRangeMax, nearestValidMin, nearestValidMax), cause);

        this.invalidRange = new Date[2];
        this.invalidRange[0] = invalidRangeMin;
        this.invalidRange[1] = invalidRangeMax;

        this.validRange = new Date[2];
        this.validRange[0] = validRangeMin;
        this.validRange[1] = validRangeMax;

        if (nearestValidMin != null && nearestValidMax != null) {
            this.nearestValidValues = new Date[2];
            this.nearestValidValues[0] = nearestValidMin;
            this.nearestValidValues[1] = nearestValidMax;
        } else {
            nearestValidValues = null;
        }
    }

    public MotuInvalidDateRangeException(
        Date invalidRangeMin,
        Date invalidRangeMax,
        Date validRangeMin,
        Date validRangeMax,
        Date nearestValidMin,
        Date nearestValidMax) {
        this(invalidRangeMin, invalidRangeMax, validRangeMin, validRangeMax, nearestValidMin, nearestValidMax, null);
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
        this(invalidRangeMin, invalidRangeMax, validRangeMin, validRangeMax, null, null, cause);
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
    public MotuInvalidDateRangeException(Date invalidRangeMin, Date invalidRangeMax, Date validRangeMin, Date validRangeMax) {
        this(invalidRangeMin, invalidRangeMax, validRangeMin, validRangeMax, null, null, null);
    }

    public static String getErrorMessage(Date invalidRangeMin,
                                         Date invalidRangeMax,
                                         Date validRangeMin,
                                         Date validRangeMax,
                                         Date nearestValidMin,
                                         Date nearestValidMax) {
        StringBuilder stringBuilder = new StringBuilder();

        if (invalidRangeMin.after(invalidRangeMax)) {
            stringBuilder.append("Invalid input dates, starDate shall be after endDate: ");
            stringBuilder.append(getInvalidRangeAsString(invalidRangeMin, invalidRangeMax));
            stringBuilder.append(".\nFor information, dataset range is: ");
        } else if (nearestValidMin != null && nearestValidMax != null) {
            stringBuilder.append("No data in date range: ");
            stringBuilder.append(getInvalidRangeAsString(invalidRangeMin, invalidRangeMax));
            stringBuilder.append(".\nSurrounding dates with data are ");
            stringBuilder.append(getNearestValidValuesAsString(nearestValidMin, nearestValidMax));
            stringBuilder.append(".\nFor information, dataset range is: ");
        } else {
            stringBuilder.append("Invalid date range: ");
            stringBuilder.append(getInvalidRangeAsString(invalidRangeMin, invalidRangeMax));
            stringBuilder.append(". Valid range is: ");
        }
        stringBuilder.append(getValidRangeAsString(validRangeMin, validRangeMax));
        stringBuilder.append(".");

        return stringBuilder.toString();
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
        this.nearestValidValues = null;
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
        this.nearestValidValues = null;
    }

    /**
     * Date range representation which causes the exception.
     */
    private final Date[] invalidRange;

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
    private final Date[] validRange;

    /**
     * Gets the valid range.
     *
     * @return the validRange
     */
    public Date[] getValidRange() {
        return this.validRange;
    }

    /** The nearest valid values. */
    private final Date[] nearestValidValues;

    /**
     * Gets the nearest valid values.
     *
     * @return the nearest valid values
     */
    public Date[] getNearestValidValues() {
        return nearestValidValues;
    }

    /**
     * Gets the valid range as string.
     *
     * @return the validRange as a string interval representation
     */
    private static String getValidRangeAsString(Date validRangeMin, Date validRangeMax) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        stringBuilder.append(new SimpleDateFormat(DATETIME_FORMAT).format(validRangeMin));
        stringBuilder.append(",");
        stringBuilder.append(new SimpleDateFormat(DATETIME_FORMAT).format(validRangeMax));
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    /**
     * Gets the invalid range as string.
     *
     * @return the invalidRange as a string interval representation
     */
    private static String getInvalidRangeAsString(Date inValidRangeMin, Date inValidRangeMax) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        stringBuilder.append(new SimpleDateFormat(DATETIME_FORMAT).format(inValidRangeMin));
        stringBuilder.append(",");
        stringBuilder.append(new SimpleDateFormat(DATETIME_FORMAT).format(inValidRangeMax));
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    /**
     * Gets the nearest valid values as string.
     *
     * @return the nearest valid values as string
     */
    public static String getNearestValidValuesAsString(Date nearestValidValuesMin, Date nearestValidValuesMax) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("values <= ");
        stringBuilder.append(new SimpleDateFormat(DATETIME_FORMAT).format(nearestValidValuesMin));
        stringBuilder.append(" and values >= ");
        stringBuilder.append(new SimpleDateFormat(DATETIME_FORMAT).format(nearestValidValuesMax));
        return stringBuilder.toString();
    }

}
