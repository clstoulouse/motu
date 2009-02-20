/**
 * 
 */
package fr.cls.atoll.motu.data;

import java.util.List;

import ucar.ma2.Array;
import ucar.ma2.Range;
import fr.cls.atoll.motu.exception.MotuException;
import fr.cls.atoll.motu.exception.MotuInvalidDepthException;
import fr.cls.atoll.motu.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.exception.MotuInvalidLatitudeException;
import fr.cls.atoll.motu.exception.MotuInvalidLongitudeException;
import fr.cls.atoll.motu.netcdf.NetCdfReader;

/**
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-02-20 13:00:25 $
 * 
 */
public class ExtractCriteriaDepth extends ExtractCriteriaGeo {

    /**
     * Default constructor.
     */
    public ExtractCriteriaDepth() {
    }

    /**
     * Constructor.
     * 
     * @param from low depth to be set.
     * @param to high depth to be set.
     */
    public ExtractCriteriaDepth(double from, double to) {
        setTo(to);
        setFrom(from);
    }

    /**
     * Constructor.
     * 
     * @param from low depth to be set.
     * @param to high depth to be set.
     */
    public ExtractCriteriaDepth(String from, String to) throws MotuInvalidDepthException {
        MotuInvalidDepthException invalidDepth = null;
        try {
            setFrom(from);
        } catch (MotuInvalidDepthException e) {
            invalidDepth = e;
        }
        try {
            setTo(to);
        } catch (MotuInvalidDepthException e) {
            invalidDepth = e;
        }

        if (invalidDepth != null) {
            throw invalidDepth;
        }
    }

    /**
     * Contructor from a list that contains low depth and high depth values.
     * 
     * @param list to be converted
     * @throws MotuInvalidLatitudeException
     * @throws MotuInvalidLongitudeException
     */
    public ExtractCriteriaDepth(List<String> list) throws MotuInvalidDepthException {
        // CSOFF: StrictDuplicateCode : normal duplication code.

        switch (list.size()) {
        case 2:
            setTo(list.get(1));
            setFrom(list.get(0));
            break;
        case 1:
            setTo(list.get(0));
            setFrom(list.get(0));
            break;
        default:
            break;
        }
    }

    // CSON: StrictDuplicateCode.

    /**
     * low depth.
     * 
     * @uml.property name="from"
     */
    private double from = 0.0;

    /**
     * Getter of the property <tt>from</tt>.
     * 
     * @return Returns the from.
     * @uml.property name="from"
     */
    public double getFrom() {
        return this.from;
    }

    /**
     * Setter of the property <tt>from</tt>.
     * 
     * @param value The from to set.
     * @uml.property name="from"
     */
    public void setFrom(double value) {
        this.from = value;
        adjust();
    }

    /**
     * Setter of the property <tt>from</tt>.
     * 
     * @param value The low depth representation to set.
     * @uml.property name="from"
     */
    public void setFrom(String value) throws MotuInvalidDepthException {
        setFrom(NetCdfReader.unconvertDepth(value));
    }

    /**
     * High depth.
     * 
     * @uml.property name="to"
     */
    private double to = 0.0;

    /**
     * Getter of the property <tt>to</tt>.
     * 
     * @return Returns the to.
     * @uml.property name="to"
     */
    public double getTo() {
        return this.to;
    }

    /**
     * Setter of the property <tt>to</tt>.
     * 
     * @param to The to to set.
     * @uml.property name="to"
     */
    public void setTo(double to) {
        this.to = to;
        adjust();
    }

    /**
     * Setter of the property <tt>from</tt>.
     * 
     * @param value The high depth string representation to set.
     * @uml.property name="to"
     */
    public void setTo(String value) throws MotuInvalidDepthException {
        setTo(NetCdfReader.unconvertDepth(value));
    }

    /**
     * Set criteria from low depth and end depth values.
     * 
     * @param low low depth to be set.
     * @param high high depth to be set.
     */
    public void setValues(String low, String high) throws MotuInvalidDepthException {
        MotuInvalidDepthException invalidDepth = null;
        try {
            setFrom(high);
        } catch (MotuInvalidDepthException e) {
            invalidDepth = e;
        }
        try {
            setTo(low);
        } catch (MotuInvalidDepthException e) {
            invalidDepth = e;
        }

        if (invalidDepth != null) {
            throw invalidDepth;
        }
    }

    /**
     * Contructor from a list that contains low depth and high depth values.
     * 
     * @param list to be converted
     * @throws MotuInvalidLatitudeException
     * @throws MotuInvalidLongitudeException
     */
    public void setValues(List<String> list) throws MotuInvalidDepthException {
        // CSOFF: StrictDuplicateCode : normal duplication code.

        switch (list.size()) {
        case 2:
            setValues(list.get(0), list.get(1));
            break;
        case 1:
            setValues(list.get(0), list.get(0));
            break;
        default:
            break;
        }
    }

    /**
     * Adjust low depth and high depth accordding to their value. (low depth always <= high depth)
     * 
     */
    private void adjust() {
        if (from > to) {
            double tmp = from;
            from = to;
            to = tmp;
        }
    }

    /**
     * Return range corresponding to criteria.
     * 
     * @param array values from which range is computed
     * @return range corresponding to criteria in the array, or null if no range found
     * @param rangeValue values coresponding to the range
     * @throws MotuException
     * @throws MotuInvalidDepthRangeException
     */
    public Range toRange(Array array, double[] rangeValue) throws MotuException, MotuInvalidDepthRangeException {
        return toRange((double[]) array.get1DJavaArray(Double.class), rangeValue);

    }

    /**
     * Return range corresponding to criteria.
     * 
     * @param array values from which range is computed
     * @return range corresponding to criteria in the array, or null if no range found
     * @throws MotuException
     * @throws MotuInvalidDepthRangeException
     */
    public Range toRange(Array array) throws MotuException, MotuInvalidDepthRangeException {
        return toRange((double[]) array.get1DJavaArray(Double.class));

    }

    /**
     * Return range corresponding to criteria.
     * 
     * @param array values from which range is computed
     * @return range corresponding to criteria in the array, or null if no range found
     * @param rangeValue values coresponding to the range, or Double.MAX_VALUE if undefined values
     * @throws MotuException
     * @throws MotuInvalidDepthRangeException
     */
    public Range toRange(double[] array, double[] rangeValue) throws MotuException, MotuInvalidDepthRangeException {

        Range range = toRange(array);
        if (rangeValue != null) {
            assert rangeValue.length == 2;
            rangeValue[0] = Double.MAX_VALUE;
            rangeValue[1] = Double.MAX_VALUE;
            if (range != null) {
                rangeValue[0] = array[range.first()];
                rangeValue[1] = array[range.last()];
            }
        }

        return range;
    }

    /**
     * Return range corresponding to criteria.
     * 
     * @param array values from which range is computed
     * @return range corresponding to criteria in the array, or null if no range found
     * @throws MotuException
     * @throws MotuInvalidDepthRangeException
     */
    public Range toRange(double[] array) throws MotuException, MotuInvalidDepthRangeException {

        int first = -1;
        int last = -1;

        double[] minmax = ExtractCriteria.getMinMax(array);

        // criteria value are out of range
        if (((from > minmax[1]) && (to > minmax[1])) || ((from < minmax[0]) && (to < minmax[0]))) {
            throw new MotuInvalidDepthRangeException(from, to, minmax[0], minmax[1]);
        }

        if (from == to) {
            first = ExtractCriteria.findMaxIndex(array, from);
            last = first;
        } else {
            first = ExtractCriteria.findMinIndex(array, from);
            last = ExtractCriteria.findMaxIndex(array, to);
        }

        // no index found
        if ((first == -1) || (last == -1)) {
            throw new MotuInvalidDepthRangeException(from, to, minmax[0], minmax[1]);
        }
        Range range = null;
        try {
            range = new Range(first, last);
        } catch (Exception e) {
            throw new MotuException("Error in ExtractCriteriaDepth toRange", (Throwable) e);
        }
        return range;
    }

    /**
     * @return low depth as string
     */
    public String getFromAsString() {
        return NetCdfReader.getStandardZAsString(from);
    }

    /**
     * @param format format output string
     * @return low depth as string
     */
    public String getFromAsString(String format) {
        return NetCdfReader.getStandardZAsFmtString(from, format);
    }

    /**
     * @return high depth as string
     */
    public String getToAsString() {
        return NetCdfReader.getStandardZAsString(to);
    }

    /**
     * @param format format output string
     * @return high depth as string
     */
    public String getToAsString(String format) {
        return NetCdfReader.getStandardZAsFmtString(to, format);
    }

}
