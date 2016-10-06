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

import java.util.Date;
import java.util.List;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDateException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDateRangeException;
import fr.cls.atoll.motu.web.dal.request.netcdf.NetCdfReader;
import ucar.ma2.Array;
import ucar.ma2.Range;

/**
 * This class introduces temporal coverage criterias to be apply on data (for extraction/selection and
 * research).
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class ExtractCriteriaDatetime extends ExtractCriteria {

    /**
     * Default constructor.
     */
    public ExtractCriteriaDatetime() {

    }

    /**
     * Constructor.
     * 
     * @param from start date
     * @param to end date
     */
    public ExtractCriteriaDatetime(Date from, Date to) {

        setTo(to);
        setFrom(from);
    }

    /**
     * Constructor.
     * 
     * @param from start date
     * @param to end date
     */
    public ExtractCriteriaDatetime(String from, String to) throws MotuInvalidDateException {
        setValues(from, to);
    }

    /**
     * Constructor from a list that contains start date and end date values.
     * 
     * @param list to be converted
     * @throws MotuInvalidDateException
     */
    public ExtractCriteriaDatetime(List<String> list) throws MotuInvalidDateException {
        setValues(list);
    }

    /**
     * start date.
     * 
     * @uml.property name="from"
     */
    private Date from = null;

    /**
     * Getter of the property <tt>from</tt>.
     * 
     * @return Returns the from.
     * @uml.property name="from"
     */
    public Date getFrom() {
        return this.from;
    }

    /**
     * Setter of the property <tt>from</tt>.
     * 
     * @param value The from to set.
     * @uml.property name="from"
     */
    public void setFrom(Date value) {
        this.from = value;
        adjust();
    }

    /**
     * Setter of the property <tt>from</tt>.
     * 
     * @param value The date string representation to set.
     * @uml.property name="from"
     */
    public void setFrom(String value) throws MotuInvalidDateException {
        setFrom(NetCdfReader.parseDate(value));
    }

    /**
     * Setter of the property <tt>from</tt>.
     * 
     * @param value The date string representation to set.
     * @param format The format representaiton to set.
     * @uml.property name="from"
     */
    public void setFrom(String value, String format) throws MotuInvalidDateException {
        setFrom(NetCdfReader.parseDate(value, format));
    }

    /**
     * End date.
     * 
     * @uml.property name="to"
     */
    private Date to = null;

    /**
     * Getter of the property <tt>to</tt>.
     * 
     * @return Returns the to.
     * @uml.property name="to"
     */
    public Date getTo() {
        return this.to;
    }

    /**
     * Setter of the property <tt>to</tt>.
     * 
     * @param to The to to set.
     * @uml.property name="to"
     */
    public void setTo(Date to) {
        this.to = to;
        adjust();
    }

    /**
     * Setter of the property <tt>from</tt>.
     * 
     * @param value The date string representation to set.
     * @uml.property name="from"
     */
    public void setTo(String value) throws MotuInvalidDateException {
        setTo(NetCdfReader.parseDate(value));
    }

    /**
     * Setter of the property <tt>from</tt>.
     * 
     * @param value The date string representation to set.
     * @param format The format representation to set.
     * @uml.property name="to"
     */
    public void setTo(String value, String format) throws MotuInvalidDateException {
        setTo(NetCdfReader.parseDate(value, format));
    }

    /**
     * Set criteria from a list that contains start date and end date values.
     * 
     * @param list to be converted
     * @throws MotuInvalidDateException
     */
    public void setValues(List<String> list) throws MotuInvalidDateException {

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
     * Set criteria from start date and end date values.
     * 
     * @param start start date
     * @param end end date
     */
    public void setValues(String start, String end) throws MotuInvalidDateException {
        MotuInvalidDateException invalidDate = null;
        try {
            setTo(end);
        } catch (MotuInvalidDateException e) {
            invalidDate = e;
        }
        try {
            setFrom(start);
        } catch (MotuInvalidDateException e) {
            invalidDate = e;
        }

        if (invalidDate != null) {
            throw invalidDate;
        }
    }

    /**
     * Adjust start date and end date accordding to their value. (start date always <= end date)
     * 
     */
    private void adjust() {
        if ((from != null) && (to != null)) {
            if (from.compareTo(to) > 0) {
                Date tmp = from;
                from = to;
                to = tmp;
            }
        } else if ((from == null) && (to != null)) {
            from = (Date) to.clone();
        } else if ((from != null) && (to == null)) {
            to = (Date) from.clone();
        }
    }

    /**
     * Gets range corresponding to criteria.
     * 
     * @param array values from which range is computed
     * @param udUnits units of the values
     * @return range corresponding to criteria in the array, or null if no range found
     * @throws MotuException
     * @throws MotuInvalidDateRangeException
     */
    public Range toRange(Array array, String udUnits) throws MotuException, MotuInvalidDateRangeException {
        return toRange((double[]) array.get1DJavaArray(Double.class), udUnits);

    }

    /**
     * Gets range corresponding to criteria, and range values (optional).
     * 
     * @param array values from which range is computed
     * @param udUnits units of the values
     * @param rangeValue values coresponding to the range
     * @return range corresponding to criteria in the array, or null if no range found
     * @throws MotuException
     * @throws MotuInvalidDateRangeException
     */
    public Range toRange(Array array, String udUnits, double[] rangeValue) throws MotuException, MotuInvalidDateRangeException {
        return toRange((double[]) array.get1DJavaArray(Double.class), udUnits, rangeValue);

    }

    /**
     * Return range corresponding to criteria, and range values (optional).
     * 
     * @param array values from which range is computed
     * @param udUnits units of the values
     * @param rangeValue values corresponding to the range, or Double.MAX_VALUE if undefined values
     * @return range corresponding to criteria in the array, or null if no range found
     * @throws MotuException
     * @throws MotuInvalidDateRangeException
     */
    public Range toRange(double[] array, String udUnits, double[] rangeValue) throws MotuException, MotuInvalidDateRangeException {
        Range range = toRange(array, udUnits);
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
     * @param udUnits units of the values
     * @return range corresponding to criteria in the array, or null if no range found
     * @throws MotuException
     * @throws MotuInvalidDateRangeException
     */
    public Range toRange(double[] array, String udUnits) throws MotuException, MotuInvalidDateRangeException {
        if ((from == null) && (to == null)) {
            return null;
        }

        int first = -1;
        int last = -1;
        double startDate = NetCdfReader.getDate(this.from, udUnits);
        double endDate = NetCdfReader.getDate(this.to, udUnits);

        double[] minmax = ExtractCriteria.getMinMax(array);

        // criteria value are out of range
        if (((startDate > minmax[1]) && (endDate > minmax[1])) || ((startDate < minmax[0]) && (endDate < minmax[0]))) {
            throw new MotuInvalidDateRangeException(from, to, NetCdfReader.getDate(minmax[0], udUnits), NetCdfReader.getDate(minmax[1], udUnits));
        }

        // if (startDate == endDate) {
        // first = ExtractCriteria.findMaxIndex(array, startDate);
        // last = first;
        // } else {
        // first = ExtractCriteria.findMinIndex(array, startDate);
        // last = ExtractCriteria.findMaxIndex(array, endDate);
        // }

        first = ExtractCriteria.findMinIndex(array, startDate);
        last = ExtractCriteria.findMaxIndex(array, endDate);

        // no index found
        if ((first == -1) || (last == -1)) {
            throw new MotuInvalidDateRangeException(from, to, NetCdfReader.getDate(minmax[0], udUnits), NetCdfReader.getDate(minmax[1], udUnits));
        }

        // criteria is not a valid range.
        if (first > last) {
            MotuInvalidDateRangeException motuInvalidDateRangeException = new MotuInvalidDateRangeException(
                    from,
                    to,
                    NetCdfReader.getDate(minmax[0], udUnits),
                    NetCdfReader.getDate(minmax[1], udUnits));
            motuInvalidDateRangeException.setNearestValidValues(NetCdfReader.getDate(array[last], udUnits),
                                                                NetCdfReader.getDate(array[first], udUnits));
            throw motuInvalidDateRangeException;
        }

        Range range = null;
        try {
            range = new Range(first, last);
        } catch (Exception e) {
            throw new MotuException(ErrorType.INVALID_DATE_RANGE, "Error in ExtractCriteriaDatatime toRange", e);
        }
        return range;
    }

    /**
     * @return start date as string
     * @throws MotuException
     */
    public String getFromAsString() {
        return NetCdfReader.getDateAsGMTNoZeroTimeString(from);
    }

    /**
     * @return end date as string
     * @throws MotuException
     */
    public String getToAsString() {
        return NetCdfReader.getDateAsGMTNoZeroTimeString(to);
    }
}
