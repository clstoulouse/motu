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
package fr.cls.atoll.motu.web.bll.request.model;

import java.util.Arrays;
import java.util.List;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDepthException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidLatitudeException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidLongitudeException;
import fr.cls.atoll.motu.web.dal.request.netcdf.NetCdfReader;
import ucar.ma2.Array;
import ucar.ma2.Range;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
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

    public static int findMinDepthIndex(double[] depths, double from) {
        int first = Arrays.binarySearch(depths, from);
        if (first < 0) {
            // Subtract 1 cm for rounding tolerance
            first = ExtractCriteria.findMinIndex(depths, Math.floor(from * 100 - 1) / 100.0);
        }
        return first;
    }

    public static int findMaxDepthIndex(double[] depths, double to) {
        int last = Arrays.binarySearch(depths, to);
        if (last < 0) {
            // Add 2 cm for rounding tolerance
            last = ExtractCriteria.findMaxIndex(depths, Math.floor(to * 100 + 2) / 100.0);
        }
        return last;
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

        double[] minmax = ExtractCriteria.getMinMax(array);

        // criteria value are out of range
        if (((from > minmax[1]) && (to > minmax[1])) || ((from < minmax[0]) && (to < minmax[0]))) {
            throw new MotuInvalidDepthRangeException(from, to, minmax[0], minmax[1]);
        }

        int first = findMinDepthIndex(array, from);
        int last = findMaxDepthIndex(array, to);

        // no index found
        if ((first == -1) || (last == -1)) {
            throw new MotuInvalidDepthRangeException(from, to, minmax[0], minmax[1]);
        }

        // criteria is not a valid range.
        if (first > last) {
            throw new MotuInvalidDepthRangeException(from, to, minmax[0], minmax[1], array[last], array[first]);
        }

        Range range = null;
        try {
            range = new Range(first, last);
        } catch (Exception e) {
            throw new MotuException(ErrorType.INVALID_DEPTH_RANGE, "Error in ExtractCriteriaDepth toRange", e);
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
