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
 * Date range exception class of Motu.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class MotuInvalidDateRangeException extends MotuExceptionBase {
    /**
     * Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(MotuInvalidDateRangeException.class);

    private static final long serialVersionUID = -1L;

    /**
     * @param invalidRangeMin invalid Date range min. representation which causes the exception
     * @param invalidRangeMax invalid Date range max. representation which causes the exception
     * @param validRangeMin valid Date range min. representation
     * @param validRangeMax valid Date range max. representation
     */
    public MotuInvalidDateRangeException(Date invalidRangeMin, Date invalidRangeMax, Date validRangeMin, Date validRangeMax) {
        super("Invalid date range.");
        // CSOFF: StrictDuplicateCode : normal duplication code.

        this.invalidRange = new Date[2];
        this.invalidRange[0] = invalidRangeMin;
        this.invalidRange[1] = invalidRangeMax;

        this.validRange = new Date[2];
        this.validRange[0] = validRangeMin;
        this.validRange[1] = validRangeMax;

        notifyLogException();
    }

    // CSON: StrictDuplicateCode

    /**
     * @param invalidRangeMin invalid Date range min. representation which causes the exception
     * @param invalidRangeMax invalid Date range max. representation which causes the exception
     * @param validRangeMin valid Date range min. representation
     * @param validRangeMax valid Date range max. representation
     * @param cause native exception.
     */
    public MotuInvalidDateRangeException(Date invalidRangeMin, Date invalidRangeMax, Date validRangeMin, Date validRangeMax, Throwable cause) {
        super("Invalid date range.", cause);

        this.invalidRange = new Date[2];
        this.invalidRange[0] = invalidRangeMin;
        this.invalidRange[1] = invalidRangeMax;

        this.validRange = new Date[2];
        this.validRange[0] = validRangeMin;
        this.validRange[1] = validRangeMax;

        notifyLogException();
    }

    /**
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
        notifyLogException();
    }

    /**
     * @param invalidRange invalid Date range representation which causes the exception
     * @param validRange valid Date range representation
     */
    public MotuInvalidDateRangeException(Date[] invalidRange, Date[] validRange) {
        super("Invalid date range.");

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
     * @return exception information.
     */
    @Override
    public String notifyException() {
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(super.notifyException());
        if (invalidRange != null) {
            stringBuffer.append("\nInvalid range: ");
            stringBuffer.append(getInvalidRangeAsString());
        }
        if (validRange != null) {
            stringBuffer.append("Valid range is: ");
            stringBuffer.append(getValidRangeAsString());
        }

        return stringBuffer.toString();
    }

    /**
     * Date range representation which causes the exception.
     */
    final private Date[] invalidRange;

    /**
     * @return the invalidRange
     */
    public Date[] getInvalidRange() {
        return this.invalidRange;
    }

    /**
     * @return the invalidRange as a string interval representation
     */
    public String getInvalidRangeAsString() {
        if (invalidRange == null) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("[");
        stringBuffer.append(NetCdfReader.DATETIME_TO_STRING_DEFAULT.format(invalidRange[0]));
        stringBuffer.append(",");
        stringBuffer.append(NetCdfReader.DATETIME_TO_STRING_DEFAULT.format(invalidRange[1]));
        stringBuffer.append("]");
        return stringBuffer.toString();
    }

    /**
     * Valid Date range representation.
     */
    final private Date[] validRange;

    /**
     * @return the validRange
     */
    public Date[] getValidRange() {
        return this.validRange;
    }

    /**
     * @return the validRange as a string interval representation
     */
    public String getValidRangeAsString() {
        if (validRange == null) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("[");
        stringBuffer.append(NetCdfReader.DATETIME_TO_STRING_DEFAULT.format(validRange[0]));
        stringBuffer.append(",");
        stringBuffer.append(NetCdfReader.DATETIME_TO_STRING_DEFAULT.format(validRange[1]));
        stringBuffer.append("]");
        return stringBuffer.toString();
    }

}
// CSON: MultipleStringLiterals
