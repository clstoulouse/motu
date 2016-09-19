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
    private double[] invalidRange;

    /**
     * Valid Depth range representation.
     */
    private double[] validRange;

    /** The nearest valid values. */
    private double[] nearestValidValues;

    /**
     * The Constructor.
     *
     * @param invalidRangeMin invalid depth range min. representation which causes the exception
     * @param invalidRangeMax invalid depth range max. representation which causes the exception
     * @param validRangeMin valid depth range min. representation
     * @param validRangeMax valid depth range max. representation
     */
    public MotuInvalidDepthRangeException(double invalidRangeMin, double invalidRangeMax, double validRangeMin, double validRangeMax) {
        this(invalidRangeMin, invalidRangeMax, validRangeMin, validRangeMax, null);
    }

    /**
     * The Constructor.
     *
     * @param invalidRangeMin invalid depth range min. representation which causes the exception
     * @param invalidRangeMax invalid depth range max. representation which causes the exception
     * @param validRangeMin valid depth range min. representation
     * @param validRangeMax valid depth range max. representation
     * @param cause native exception.
     */
    public MotuInvalidDepthRangeException(
        double invalidRangeMin,
        double invalidRangeMax,
        double validRangeMin,
        double validRangeMax,
        Throwable cause) {
        super(getErrorMessage(invalidRangeMin, invalidRangeMax, validRangeMin, validRangeMax), cause);

        this.invalidRange = new double[2];
        this.invalidRange[0] = invalidRangeMin;
        this.invalidRange[1] = invalidRangeMax;

        this.validRange = new double[2];
        this.validRange[0] = validRangeMin;
        this.validRange[1] = validRangeMax;
    }

    private static String getErrorMessage(double invalidRangeMin, double invalidRangeMax, double validRangeMin, double validRangeMax) {
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append("\nInvalid depth range:");
        stringBuffer.append(getInvalidRangeAsString(invalidRangeMin, invalidRangeMax));
        stringBuffer.append(" Valid range is:[");
        stringBuffer.append(getValidRangeAsString(validRangeMin, validRangeMax));
        stringBuffer.append(". ");

        // if (nearestValidValues != null) {
        // stringBuffer.append(getNearestValidValuesMessage(nearestValidValues[0], nearestValidValues[1],
        // invalidRangeMin, invalidRangeMax));
        // }

        return stringBuffer.toString();
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
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("[");
        stringBuffer.append(Double.toString(invalidRangeMin));
        stringBuffer.append(",");
        stringBuffer.append(Double.toString(invalidRangeMax));
        stringBuffer.append("]");
        return stringBuffer.toString();
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
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("[");
        stringBuffer.append(Double.toString(validRangeMin));
        stringBuffer.append(",");
        stringBuffer.append(Double.toString(validRangeMax));
        stringBuffer.append("]");
        return stringBuffer.toString();
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
     * Sets the nearest valid values.
     *
     * @param min the min
     * @param max the max
     */
    public void setNearestValidValues(double min, double max) {
        this.nearestValidValues = new double[2];
        this.nearestValidValues[0] = min;
        this.nearestValidValues[1] = max;
    }

    /**
     * Sets the nearest valid values.
     *
     * @param nearestValidRange the nearest valid values
     */
    public void setNearestValidValues(double[] nearestValidRange) {
        this.nearestValidValues = nearestValidRange;
    }

    /**
     * Gets the nearest valid values as string.
     *
     * @return the nearest valid values as string
     */
    public static String getNearestValidValuesAsString(double nearestValidValuesMin, double nearestValidValuesMax) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("values <= ");
        stringBuffer.append(Double.toString(nearestValidValuesMin));
        stringBuffer.append(", values >= ");
        stringBuffer.append(Double.toString(nearestValidValuesMax));
        return stringBuffer.toString();
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
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("The nearest valid range against ");
        stringBuffer.append(getInvalidRangeAsString(invalidRangeMin, invalidRangeMax));
        stringBuffer.append(" is: ");
        stringBuffer.append(getNearestValidValuesAsString(nearestValidValuesMin, nearestValidValuesMax));
        stringBuffer.append(". ");

        return stringBuffer.toString();
    }

}
