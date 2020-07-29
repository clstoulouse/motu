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
 * Depth range exception class of Motu.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class MotuInvalidDepthRangeException extends MotuExceptionBase {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -1L;

    /**
     * Depth range representation which causes the exception.
     */
    private final double[] invalidRange;

    /**
     * Valid Depth range representation.
     */
    private final double[] validRange;

    /** The nearest valid values. */
    private final double[] nearestValidValues;

    /**
     * The Constructor.
     *
     * @param invalidRangeMin invalid depth range min. representation which causes the exception
     * @param invalidRangeMax invalid depth range max. representation which causes the exception
     * @param validRangeMin valid depth range min. representation
     * @param validRangeMax valid depth range max. representation
     * @param nearestValidMax
     * @param nearestValidMin
     */
    public MotuInvalidDepthRangeException(
        double invalidRangeMin,
        double invalidRangeMax,
        double validRangeMin,
        double validRangeMax,
        double nearestValidMin,
        double nearestValidMax) {
        this(invalidRangeMin, invalidRangeMax, validRangeMin, validRangeMax, nearestValidMin, nearestValidMax, null);
    }

    /**
     * The Constructor.
     *
     * @param invalidRangeMin invalid depth range min. representation which causes the exception
     * @param invalidRangeMax invalid depth range max. representation which causes the exception
     * @param validRangeMin valid depth range min. representation
     * @param validRangeMax valid depth range max. representation
     * @param nearestValidMax
     * @param nearestValidMin
     */
    public MotuInvalidDepthRangeException(double invalidRangeMin, double invalidRangeMax, double validRangeMin, double validRangeMax) {
        this(invalidRangeMin, invalidRangeMax, validRangeMin, validRangeMax, Double.NaN, Double.NaN, null);
    }

    /**
     * The Constructor.
     *
     * @param invalidRangeMin invalid depth range min. representation which causes the exception
     * @param invalidRangeMax invalid depth range max. representation which causes the exception
     * @param validRangeMin valid depth range min. representation
     * @param validRangeMax valid depth range max. representation
     * @param nearestValidMax
     * @param nearestValidMin
     * @param cause native exception.
     */
    public MotuInvalidDepthRangeException(
        double invalidRangeMin,
        double invalidRangeMax,
        double validRangeMin,
        double validRangeMax,
        double nearestValidMin,
        double nearestValidMax,
        Throwable cause) {
        super(getErrorMessage(invalidRangeMin, invalidRangeMax, validRangeMin, validRangeMax, nearestValidMin, nearestValidMax), cause);

        this.invalidRange = new double[2];
        this.invalidRange[0] = invalidRangeMin;
        this.invalidRange[1] = invalidRangeMax;

        this.validRange = new double[2];
        this.validRange[0] = validRangeMin;
        this.validRange[1] = validRangeMax;

        if ((!Double.isNaN(nearestValidMin)) && (!Double.isNaN(nearestValidMax))) {
            this.nearestValidValues = new double[2];
            this.nearestValidValues[0] = nearestValidMin;
            this.nearestValidValues[1] = nearestValidMax;
        } else {
            this.nearestValidValues = null;
        }
    }

    private static String getErrorMessage(double invalidRangeMin,
                                          double invalidRangeMax,
                                          double validRangeMin,
                                          double validRangeMax,
                                          double nearestValidMin,
                                          double nearestValidMax) {
        StringBuilder stringBuilder = new StringBuilder();

        if (invalidRangeMin > invalidRangeMax) {
            stringBuilder.append("Invalid input depths, depthMin shall be under depthMax: ");
            stringBuilder.append(getInvalidRangeAsString(invalidRangeMin, invalidRangeMax));
            stringBuilder.append(".\nFor information, dataset range is: ");
        } else if ((!Double.isNaN(nearestValidMin)) && (!Double.isNaN(nearestValidMax))) {
            stringBuilder.append("No data in Depth range: ");
            stringBuilder.append(getInvalidRangeAsString(invalidRangeMin, invalidRangeMax));
            stringBuilder.append(".\nSurrounding depths with data are ");
            stringBuilder.append(getNearestValidValuesAsString(nearestValidMin, nearestValidMax));
            stringBuilder.append(".\nFor information, dataset range is: ");
        } else {
            stringBuilder.append("Invalid depth range: ");
            stringBuilder.append(getInvalidRangeAsString(invalidRangeMin, invalidRangeMax));
            stringBuilder.append(". Valid range is: ");
        }
        stringBuilder.append(getValidRangeAsString(validRangeMin, validRangeMax));
        stringBuilder.append(".");

        return stringBuilder.toString();
    }

    /**
     * Gets the invalid range.
     *
     * @return the invalidRange
     */
    public double[] getInvalidRange() {
        return this.invalidRange;
    }

    /**
     * Gets the invalid range as string.
     *
     * @return the invalidRange as a string interval representation
     */
    public static String getInvalidRangeAsString(double invalidRangeMin, double invalidRangeMax) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        stringBuilder.append(Double.toString(invalidRangeMin));
        stringBuilder.append(",");
        stringBuilder.append(Double.toString(invalidRangeMax));
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    /**
     * Gets the valid range.
     *
     * @return the validRange
     */
    public double[] getValidRange() {
        return this.validRange;
    }

    /**
     * Gets the valid range as string.
     *
     * @return the validRange as a string interval representation
     */
    public static String getValidRangeAsString(double validRangeMin, double validRangeMax) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        stringBuilder.append(Double.toString(validRangeMin));
        stringBuilder.append(",");
        stringBuilder.append(Double.toString(validRangeMax));
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    /**
     * Gets the nearest valid values.
     *
     * @return the nearest valid values
     */
    public double[] getNearestValidValues() {
        return nearestValidValues;
    }

    /**
     * Gets the nearest valid values as string.
     *
     * @return the nearest valid values as string
     */
    public static String getNearestValidValuesAsString(double nearestValidValuesMin, double nearestValidValuesMax) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("values <= ");
        stringBuilder.append(Double.toString(nearestValidValuesMin));
        stringBuilder.append(" and values >= ");
        stringBuilder.append(Double.toString(nearestValidValuesMax));
        return stringBuilder.toString();
    }

    /**
     * Gets the nearest valid values message.
     *
     * @return the nearest valid values message
     */
    public static String getNearestValidValuesMessage(double nearestValidValuesMin,
                                                      double nearestValidValuesMax,
                                                      double invalidRangeMin,
                                                      double invalidRangeMax) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("The nearest valid range against ");
        stringBuilder.append(getInvalidRangeAsString(invalidRangeMin, invalidRangeMax));
        stringBuilder.append(" is: ");
        stringBuilder.append(getNearestValidValuesAsString(nearestValidValuesMin, nearestValidValuesMax));
        stringBuilder.append(". ");

        return stringBuilder.toString();
    }

}
