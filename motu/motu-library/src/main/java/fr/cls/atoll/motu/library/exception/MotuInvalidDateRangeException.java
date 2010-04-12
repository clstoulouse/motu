/**
 * 
 */
package fr.cls.atoll.motu.library.exception;

import java.util.Date;

import org.apache.log4j.Logger;

import fr.cls.atoll.motu.library.netcdf.NetCdfReader;

// CSOFF: MultipleStringLiterals : avoid message in constants declaration and trace log.

/**
 * Date range exception class of Motu.
 * 
 * @author $Author: ccamel $
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:21 $
 * 
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
    public void notifyLogException() {

        super.notifyLogException();
        LOG.warn(notifyException());
    }

    /**
     * @return exception information.
     */
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
