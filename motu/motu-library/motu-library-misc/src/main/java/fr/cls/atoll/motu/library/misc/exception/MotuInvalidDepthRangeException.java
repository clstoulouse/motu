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

// TODO: Auto-generated Javadoc
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
    /**
     * Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(MotuInvalidDepthRangeException.class);

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -1L;

    /**
     * The Constructor.
     *
     * @param invalidRangeMin invalid depth range min. representation which causes the exception
     * @param invalidRangeMax invalid depth range max. representation which causes the exception
     * @param validRangeMin valid depth range min. representation
     * @param validRangeMax valid depth range max. representation
     */
    public MotuInvalidDepthRangeException(double invalidRangeMin, double invalidRangeMax, double validRangeMin, double validRangeMax) {
        super("Invalid depth range.");
        // CSOFF: StrictDuplicateCode : normal duplication code.

        this.invalidRange = new double[2];
        this.invalidRange[0] = invalidRangeMin;
        this.invalidRange[1] = invalidRangeMax;

        this.validRange = new double[2];
        this.validRange[0] = validRangeMin;
        this.validRange[1] = validRangeMax;

        notifyLogException();
    }

    // CSOFF: StrictDuplicateCode

    /**
     * The Constructor.
     *
     * @param invalidRangeMin invalid depth range min. representation which causes the exception
     * @param invalidRangeMax invalid depth range max. representation which causes the exception
     * @param validRangeMin valid depth range min. representation
     * @param validRangeMax valid depth range max. representation
     * @param cause native exception.
     */
    public MotuInvalidDepthRangeException(double invalidRangeMin, double invalidRangeMax, double validRangeMin, double validRangeMax, Throwable cause) {
        super("Invalid depth range.", cause);

        this.invalidRange = new double[2];
        this.invalidRange[0] = invalidRangeMin;
        this.invalidRange[1] = invalidRangeMax;

        this.validRange = new double[2];
        this.validRange[0] = validRangeMin;
        this.validRange[1] = validRangeMax;

        notifyLogException();
    }

    /**
     * The Constructor.
     *
     * @param invalidRange invalid depth range representation which causes the exception
     * @param validRange valid depth range representation
     * @param cause native exception.
     */
    public MotuInvalidDepthRangeException(double[] invalidRange, double[] validRange, Throwable cause) {
        super("Invalid depth range.", cause);

        assert invalidRange.length == 2;
        assert validRange.length == 2;

        this.invalidRange = invalidRange;
        this.validRange = validRange;
        notifyLogException();
    }

    /**
     * The Constructor.
     *
     * @param invalidRange invalid depth range representation which causes the exception
     * @param validRange valid depth range representation
     */
    public MotuInvalidDepthRangeException(double[] invalidRange, double[] validRange) {
        super("Invalid depth range.");

        assert invalidRange.length == 2;
        assert validRange.length == 2;

        this.invalidRange = invalidRange;
        this.validRange = validRange;
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
     * Notify exception.
     *
     * @return exception information.
     */
    @Override
    public String notifyException() {
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(super.notifyException());
        if (invalidRange != null) {
            stringBuffer.append("\nInvalid range:");
            stringBuffer.append(getInvalidRangeAsString());
        }
        if (validRange != null) {
            stringBuffer.append("Valid range is:[");
            stringBuffer.append(getValidRangeAsString());
        }
        
       stringBuffer.append(getNearestValidValuesMessage());

        return stringBuffer.toString();
    }

    /**
     * Depth range representation which causes the exception.
     */
    final private double[] invalidRange;

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
    public String getInvalidRangeAsString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("[");
        stringBuffer.append(Double.toString(invalidRange[0]));
        stringBuffer.append(",");
        stringBuffer.append(Double.toString(invalidRange[1]));
        stringBuffer.append("]");
        return stringBuffer.toString();
    }

    /**
     * Valid Depth range representation.
     */
    final private double[] validRange;

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
    public String getValidRangeAsString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("[");
        stringBuffer.append(Double.toString(validRange[0]));
        stringBuffer.append(",");
        stringBuffer.append(Double.toString(validRange[1]));
        stringBuffer.append("]");
        return stringBuffer.toString();
    }
    
    /** The nearest valid values. */
    private  double[] nearestValidValues = null;
    
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
    public String getNearestValidValuesAsString() {
        if (nearestValidValues == null) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("values <= ");
        stringBuffer.append(Double.toString(nearestValidValues[0]));
        stringBuffer.append(", values >= ");
        stringBuffer.append(Double.toString(nearestValidValues[1]));
        return stringBuffer.toString();
    }
    
    /**
     * Gets the nearest valid values message.
     *
     * @return the nearest valid values message
     */
    public String getNearestValidValuesMessage() {
        StringBuffer stringBuffer = new StringBuffer();
        
        if (nearestValidValues != null) {
            stringBuffer.append("The nearest valid range against ");
            stringBuffer.append(getInvalidRangeAsString());
            stringBuffer.append(" is: ");
            stringBuffer.append(getNearestValidValuesAsString());
        }

        return stringBuffer.toString();
    }
    

}
// CSON: MultipleStringLiterals
