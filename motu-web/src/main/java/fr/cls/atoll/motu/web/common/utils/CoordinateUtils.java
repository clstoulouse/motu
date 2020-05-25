package fr.cls.atoll.motu.web.common.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.web.dal.request.netcdf.NetCdfReader;
import fr.cls.atoll.motu.web.dal.request.netcdf.NetCdfWriter;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;
import ucar.ma2.Range;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis2D;
import ucar.nc2.dt.grid.GridCoordSys;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.geoloc.Projection;
import ucar.unidata.geoloc.ProjectionPoint;
import ucar.unidata.geoloc.ProjectionPointImpl;

public class CoordinateUtils {

    /**
     * Return a longitude in the bounds [-180;180] using modulo 360
     * 
     * @param longitude
     * @return
     */
    public static double getLongitudeM180P180(double longitude) {
        double newLong = longitude % 360;
        if (newLong > 180) {
            newLong -= 360;
        }
        if (newLong < -180) {
            newLong += 360;
        }
        return newLong;
    }
    
    public static String getMinValForAxisAsString(CoordinateAxis axis) {
        String minValStr = null;
        if (axis != null) {
            MAMath.MinMax minMax = NetCdfWriter.getMinMaxSkipMissingData(axis, null);
            minValStr = NetCdfReader.getStandardLatAsString(minMax.min);
        }
        return minValStr;
    }

    public static List<Range> createRange(int minj, int maxj, int mini, int maxi) throws InvalidRangeException {
        List<Range> rangeList = new ArrayList<>();
        rangeList.add(new Range(minj, maxj));
        rangeList.add(new Range(mini, maxi));
        return rangeList;
    }

    /**
     * Given a coordinate position, find what grid element contains it, but always return valid index. This
     * means that
     * 
     * This methode is copied from CoordinateAxis1D.findCoordElementBounded because it doesn' worry about
     * 'lastIndex' parameter for Lon axis type.
     * 
     * <pre>
     *                              if values are ascending:
     *                              pos &lt; edge[0] return 0
     *                              edge[n] &lt; pos return n-1
     *                              edge[i] &lt;= pos &lt; edge[i+1] return i
     *                         
     *                              if values are descending:
     *                              pos &gt; edge[0] return 0
     *                              edge[n] &gt; pos return n-1
     *                              edge[i] &gt; pos &gt;= edge[i+1] return i
     * </pre>
     * 
     * @param axis axis from which to get coorinate element
     * @param pos position in this coordinate system
     * @param lastIndex last position we looked for, or -1 if none
     * @return index of grid point containing it
     * @throws MotuNotImplementedException
     */
    public static int findCoordElementBounded(CoordinateAxis1D axis, double pos, int lastIndex) throws MotuNotImplementedException {
        if (!axis.isNumeric()) {
            throw new MotuNotImplementedException("ERROR in ExtractCriteriaLatLon - findCoordElementBounded on non-numeric not implemented");
        }

        boolean isAscending = false;
        int n = (int) axis.getSize();
        // WARNING : if the axis has just one value,
        // we can't define if axis is ascending or descending
        // To be true to the the Netcdf-java APIs, define axis as ascending
        // In this case, the edge (bound) of the axis is always [value, 0] :
        // if the value is < 0 the egde is correct,
        // but if the values is > 0, the edge is wrong
        if (n < 2) {
            isAscending = true;
        } else {
            isAscending = axis.getCoordValue(0) < axis.getCoordValue(1);
        }

        if (axis.getAxisType() == AxisType.Lon) {
            if (lastIndex < 0) {
                lastIndex = 0;
            }
            for (int x = lastIndex; x < axis.getSize(); x++) {
                if (isAscending) {
                    if (LatLonPointImpl.betweenLon(pos, axis.getCoordEdge(x), axis.getCoordEdge(x + 1))) {
                        return x;
                    } else if (n < 2) { // if only one value, check with the reverted the edge values
                        if (LatLonPointImpl.betweenLon(pos, axis.getCoordEdge(x + 1), axis.getCoordEdge(x))) {
                            return x;
                        }
                    }
                } else {
                    if (LatLonPointImpl.betweenLon(pos, axis.getCoordEdge(x + 1), axis.getCoordEdge(x))) {
                        return x;
                    }
                }

            }
            return -1;
        }

        if (lastIndex < 0) {
            lastIndex = (int) axis.getSize() / 2;
        }

        // Special case if there just one value.
        if (n < 2) {
            if ((pos >= axis.getCoordEdge(0)) && (pos <= axis.getCoordEdge(1))) {
                return 0;
            } else if ((pos >= axis.getCoordEdge(1)) && (pos <= axis.getCoordEdge(0))) {
                return 0;
            } else {
                return -1;
            }
        }

        if (isAscending) {

            if (pos < axis.getCoordEdge(0)) {
                // return 0;
                return -1;
            }

            if (pos > axis.getCoordEdge(n)) {
                // return n - 1;
                return -1;
            }

            while (pos < axis.getCoordEdge(lastIndex)) {
                lastIndex--;
            }

            while (pos > axis.getCoordEdge(lastIndex + 1)) {
                lastIndex++;
            }

            return lastIndex;

        } else {

            if (pos > axis.getCoordEdge(0)) {
                // return 0;
                return -1;
            }

            if (pos < axis.getCoordEdge(n)) {
                // return n - 1;
                return -1;
            }

            while (pos > axis.getCoordEdge(lastIndex)) {
                lastIndex--;
            }

            while (pos < axis.getCoordEdge(lastIndex + 1)) {
                lastIndex++;
            }

            return lastIndex;
        }
    }

    /**
     * Get Index Ranges for the given lat, lon bounding box. For projection, only an approximation based on
     * latlon corners. Must have CoordinateAxis1D or 2D for x and y axis. This method is copied from
     * GridCoordSys because of not CoordinateAxis1D.findCoordElementBounded
     * 
     * @param gcs grid coordinate system
     * @param rect lat, lon bounding box.
     * @return list of 2 Range objects, first y then x.
     * @throws MotuException
     * @throws MotuNotImplementedException
     */
    public static List<Range> getRangesFromLatLonRect(GridCoordSys gcs, LatLonRect rect) throws MotuException, MotuNotImplementedException {

        double minx;
        double maxx;
        double miny;
        double maxy;

        CoordinateAxis xaxis = gcs.getXHorizAxis();
        CoordinateAxis yaxis = gcs.getYHorizAxis();

        MAMath.MinMax xMinMax = NetCdfWriter.getMinMaxSkipMissingData(xaxis, null);
        MAMath.MinMax yMinMax = NetCdfWriter.getMinMaxSkipMissingData(yaxis, null);

        LatLonRect gcsRect = new LatLonRect(new LatLonPointImpl(yMinMax.min, xMinMax.min), new LatLonPointImpl(yMinMax.max, xMinMax.max));
        if (gcsRect.containedIn(rect)) {
            rect = new LatLonRect(gcsRect);
        }

        LatLonPointImpl llpt = rect.getLowerLeftPoint();
        LatLonPointImpl urpt = rect.getUpperRightPoint();
        LatLonPointImpl lrpt = rect.getLowerRightPoint();
        LatLonPointImpl ulpt = rect.getUpperLeftPoint();

        if (gcs.isLatLon()) {
            minx = CoordinateUtils.getMinOrMaxLon(llpt.getLongitude(), ulpt.getLongitude(), true);
            miny = Math.min(llpt.getLatitude(), lrpt.getLatitude());
            maxx = CoordinateUtils.getMinOrMaxLon(urpt.getLongitude(), lrpt.getLongitude(), false);
            maxy = Math.min(ulpt.getLatitude(), urpt.getLatitude());

        } else {
            Projection dataProjection = gcs.getProjection();
            ProjectionPoint ll = dataProjection.latLonToProj(llpt, new ProjectionPointImpl());
            ProjectionPoint ur = dataProjection.latLonToProj(urpt, new ProjectionPointImpl());
            ProjectionPoint lr = dataProjection.latLonToProj(lrpt, new ProjectionPointImpl());
            ProjectionPoint ul = dataProjection.latLonToProj(ulpt, new ProjectionPointImpl());

            minx = Math.min(ll.getX(), ul.getX());
            miny = Math.min(ll.getY(), lr.getY());
            maxx = Math.max(ur.getX(), lr.getX());
            maxy = Math.max(ul.getY(), ur.getY());
        }

        if ((xaxis instanceof CoordinateAxis1D) && (yaxis instanceof CoordinateAxis1D)) {
            CoordinateAxis1D xaxis1 = (CoordinateAxis1D) xaxis;
            CoordinateAxis1D yaxis1 = (CoordinateAxis1D) yaxis;
            int minxIndex = xaxis1.findCoordElementBounded(minx);
            int minyIndex = yaxis1.findCoordElementBounded(miny);
            int maxxIndex = findCoordElementBounded(xaxis1, maxx, minxIndex + 1);
            int maxyIndex = yaxis1.findCoordElementBounded(maxy);
            List<Range> list = new ArrayList<Range>();
            try {
                list.add(new Range(Math.min(minyIndex, maxyIndex), Math.max(minyIndex, maxyIndex)));
                list.add(new Range(Math.min(minxIndex, maxxIndex), Math.max(minxIndex, maxxIndex)));
            } catch (InvalidRangeException e) {
                throw new MotuException(
                        ErrorType.INVALID_LAT_LON_RANGE,
                        "ERROR in ExtractCriteriaLatLon - getRangesFromLatLonRect - while creating list of ranges",
                        e);
            }
            return list;
        } else if ((xaxis instanceof CoordinateAxis2D) && (yaxis instanceof CoordinateAxis2D) && gcs.isLatLon()) {
            CoordinateAxis2D lonAxis = (CoordinateAxis2D) xaxis;
            CoordinateAxis2D latAxis = (CoordinateAxis2D) yaxis;
            int[] shape = lonAxis.getShape();
            int nj = shape[0];
            int ni = shape[1];
            int mini = Integer.MAX_VALUE;
            int minj = Integer.MAX_VALUE;
            int maxi = -1;
            int maxj = -1;
            boolean test = true;
            for (int j = 0; j < nj; j++) {
                for (int i = 0; i < ni; i++) {
                    double lat = latAxis.getCoordValue(j, i);
                    double lon = lonAxis.getCoordValue(j, i);
                    if (latAxis.isMissing(lat)) {
                        continue;
                    }
                    if (lonAxis.isMissing(lon)) {
                        continue;
                    }

                    if ((lat >= miny) && (lat <= maxy) && (lon >= minx) && (lon <= maxx)) {
                        if (i > maxi) {
                            maxi = i;
                        }
                        if (i < mini) {
                            mini = i;
                        }
                        if (j > maxj) {
                            maxj = j;
                        }
                        if (j < minj) {
                            minj = j;
                        }
                        test = true;
                    } else {
                        if (test) {
                            test = false;
                        }
                    }
                }
            }
            if ((mini > maxi) || (minj > maxj)) {
                mini = 0;
                minj = 0;
                maxi = -1;
                maxj = -1;
            }
            List<Range> list = new ArrayList<Range>();
            try {
                list.add(new Range(minj, maxj));
                list.add(new Range(mini, maxi));
            } catch (InvalidRangeException e) {
                throw new MotuException(
                        ErrorType.BAD_PARAMETERS,
                        "ERROR in ExtractCriteriaLatLon - getRangesFromLatLonRect - while creating list of ranges",
                        e);
            }
            return list;
        } else {
            throw new MotuNotImplementedException("ERROR in ExtractCriteriaLatLon - getRangesFromLatLonRect - Only implemented for 1D or 2D/LatLon");
        }

    }

    /**
     * Gets the min. or max. of two longitudes.
     * 
     * @param lon1 first longitude
     * @param lon2 second longitude
     * @param wantMin true: returns min., false: returns max.
     * @return min. lon or max. lon, depends on wantMin.
     */
    public static double getMinOrMaxLon(double lon1, double lon2, boolean wantMin) {
        double midpoint = (lon1 + lon2) / 2;
        lon1 = LatLonPointImpl.lonNormal(lon1, midpoint);
        lon2 = LatLonPointImpl.lonNormal(lon2, midpoint);

        return wantMin ? Math.min(lon1, lon2) : Math.max(lon1, lon2);
    }

    public static MinMax computeMinMax(MinMax ref, MinMax work) {
        if (ref == null) {
            ref = work;
        } else {
            if (ref.min > work.min) {
                ref.min = work.min;
            }
            if (ref.max < work.max) {
                ref.max = work.max;
            }
        }

        return ref;
    }

    /**
     * Compute lat min max.
     * 
     * @param latAxis the lat axis
     * @param minj the minj
     * @param mini the mini
     * @param maxj the maxj
     * @param maxi the maxi
     * @throws MotuException the motu exception
     */
    public static MAMath.MinMax computeLatMinMax(MAMath.MinMax minMaxYValue2D, CoordinateAxis2D latAxis, int minj, int mini, int maxj, int maxi)
            throws MotuException {
        if (latAxis == null) {
            throw new MotuException(ErrorType.INVALID_LATITUDE, "ERROR in ExtractCriteriaLatLon#computeLatMinMax for CoordinateAxis2D: axis is null");

        }
        if (latAxis.getAxisType() != AxisType.Lat) {
            String msg = String
                    .format("ERROR in ExtractCriteriaLatLon#computeLatMinMax for CoordinateAxis2D: axis name '%s' - type is '%s' and expected type is '%s'",
                            latAxis.getFullName(),
                            latAxis.getAxisType().name(),
                            AxisType.Lat.name());
            throw new MotuException(ErrorType.INVALID_LATITUDE, msg);
        }
        double latMin = Double.MAX_VALUE;
        double latMax = -(Double.MAX_VALUE);
        for (int j = minj; j <= maxj; j++) {
            for (int i = mini; i <= maxi; i++) {
                double value = latAxis.getCoordValue(j, i);
                if (latMin > value) {
                    latMin = value;
                }
                if (latMax < value) {
                    latMax = value;
                }
            }
        }

        return computeMinMax(minMaxYValue2D, new MinMax(latMin, latMax));
    }

    /**
     * Compute lon min max.
     * 
     * @param lonAxis the lon axis
     * @param minj the minj
     * @param mini the mini
     * @param maxj the maxj
     * @param maxi the maxi
     * @param minx the minx
     * @throws MotuException the motu exception
     */
    public static MAMath.MinMax computeLonMinMax(MAMath.MinMax minMaxXValue2D,
                                                 CoordinateAxis2D lonAxis,
                                                 int minj,
                                                 int mini,
                                                 int maxj,
                                                 int maxi,
                                                 double minx,
                                                 double maxx)
            throws MotuException {
        if (lonAxis == null) {
            throw new MotuException(
                    ErrorType.INVALID_LONGITUDE,
                    "ERROR in ExtractCriteriaLatLon#computeLonMinMax for CoordinateAxis2D: axis is null");

        }
        if (lonAxis.getAxisType() != AxisType.Lon) {
            String msg = String
                    .format("ERROR in ExtractCriteriaLatLon#computeLonMinMax for CoordinateAxis2D: axis name '%s' - type is '%s' and expected type is '%s'",
                            lonAxis.getFullName(),
                            lonAxis.getAxisType().name(),
                            AxisType.Lon.name());
            throw new MotuException(ErrorType.INVALID_LONGITUDE, msg);
        }

        double longitudeCenter = (minx + maxx) / 2;

        double lonMin = Double.MAX_VALUE;
        double lonMax = -(Double.MAX_VALUE);
        for (int j = minj; j <= maxj; j++) {
            for (int i = mini; i <= maxi; i++) {
                double value = lonAxis.getCoordValue(j, i);

                if (lonMin > value) {
                    lonMin = value;
                }
                if (lonMax < value) {
                    lonMax = value;
                }

                if (lonMin < minx) {
                    lonMin = LatLonPointImpl.lonNormal(lonMin, longitudeCenter);
                }
                if (lonMax < minx) {
                    lonMax = LatLonPointImpl.lonNormal(lonMax, longitudeCenter);
                }

                if (lonMin > lonMax) {
                    double temp = lonMin;
                    lonMin = lonMax;
                    lonMax = temp;
                }
            }
        }
        return computeMinMax(minMaxXValue2D, new MinMax(lonMin, lonMax));
    }

    public static MinMax computeMinMaxX(Projection dataProjection, LatLonRect rect) {
        LatLonPointImpl llpt = rect.getLowerLeftPoint();
        LatLonPointImpl urpt = rect.getUpperRightPoint();
        LatLonPointImpl lrpt = rect.getLowerRightPoint();
        LatLonPointImpl ulpt = rect.getUpperLeftPoint();

        MinMax mm = new MinMax(Double.MIN_VALUE, Double.MAX_VALUE);
        mm.min = getMinOrMaxLon(llpt.getLongitude(), ulpt.getLongitude(), true);
        mm.max = getMinOrMaxLon(urpt.getLongitude(), lrpt.getLongitude(), false);

        if (mm.min > mm.max) {
            double longitudeCenter = mm.min + 180;
            mm.max = LatLonPointImpl.lonNormal(mm.max, longitudeCenter);
        }
        return mm;
    }

    public static MinMax computeMinMaxY(Projection dataProjection, LatLonRect rect) {
        LatLonPointImpl llpt = rect.getLowerLeftPoint();
        LatLonPointImpl urpt = rect.getUpperRightPoint();
        LatLonPointImpl lrpt = rect.getLowerRightPoint();
        LatLonPointImpl ulpt = rect.getUpperLeftPoint();

        MinMax mm = new MinMax(Double.MIN_VALUE, Double.MAX_VALUE);
        mm.min = Math.min(llpt.getLatitude(), lrpt.getLatitude());
        mm.max = Math.min(ulpt.getLatitude(), urpt.getLatitude());

        return mm;
    }

    public static MinMax[] computeMinMaxXY(Projection dataProjection, LatLonRect rect) {
        return new MinMax[] { computeMinMaxX(dataProjection, rect), computeMinMaxY(dataProjection, rect) };
    }

    public static void checkXYAxis(CoordinateAxis xaxis, CoordinateAxis yaxis, GridCoordSys gcs) throws MotuNotImplementedException {
        if (!((xaxis instanceof CoordinateAxis2D) && (yaxis instanceof CoordinateAxis2D) && gcs.isLatLon())) {
            throw new MotuNotImplementedException("ERROR in ExtractCriteriaLatLon - getListRangesFromLatLonRect2D - Only implemented for 2D/LatLon");
        }
    }

    public static double checkLon(double lon, double refXMin) {
        double lonRes = lon;
        if (Double.compare(lon, refXMin) < 0) {
            double longitudeCenter = refXMin + 180;
            lonRes = LatLonPointImpl.lonNormal(lon, longitudeCenter);
        }
        lonRes = getLongitudeM180P180(lonRes);
        return lonRes;
    }

    public static boolean isInside(double lat, double lon, MinMax minMaxX, MinMax minMaxY) {
        return (Double.compare(lat, minMaxY.min) >= 0) && (Double.compare(lat, minMaxY.max) <= 0) && (Double.compare(lon, minMaxX.min) >= 0)
                && (Double.compare(lon, minMaxX.max) <= 0);
    }

    /**
     * Removes Y/X empty ranges of a list Y/X ranges .
     * 
     * @param listRanges list of Y/X list ranges
     * @throws MotuException
     */
    public static void removeEmptyYXRanges(List<List<Range>> listRanges) throws MotuException {
        for (List<Range> ranges : listRanges) {
            if (!(CoordinateUtils.hasRange(ranges))) {
                listRanges.remove(ranges);
            }
        }
    }

    /**
     * Returns a true if all of Y/X empty ranges of a list are empty ranges or if list is empty.
     * 
     * @param listRanges list of Y/X list ranges to be tested
     * @return true if all of Y/X empty ranges of a list are empty ranges or if list is empty.
     * @throws MotuException
     */
    public static boolean hasEmptyYXRanges(List<List<Range>> listRanges) throws MotuException {

        if (listRanges.size() <= 0) {
            return true;
        }

        int countEmptyRanges = 0;
        for (List<Range> ranges : listRanges) {
            if (!(CoordinateUtils.hasRange(ranges))) {
                countEmptyRanges++;
            }
        }

        return (countEmptyRanges == listRanges.size());
    }

    /**
     * Returns a list of Y/X empty ranges. Ranges are empty, if last = first - 1.
     * 
     * @return a list of Y/X empty ranges.
     * @throws InvalidRangeException
     */
    public static List<Range> createEmptyYXRanges() throws InvalidRangeException {
        List<Range> ranges = new ArrayList<Range>();
        // Ranges are empty, if last = first - 1.
        ranges.add(new Range(0, -1));
        ranges.add(new Range(0, -1));
        return ranges;
    }

    /**
     * /** Returns true if ranges corresponds to a valid range. range is not valid if 'first' is 0 and 'last
     * is '-1'
     * 
     * @param ranges ranges to be tested
     * @return true or false
     * @throws MotuException
     */
    public static boolean hasRange(List<Range> ranges) throws MotuException {
        if (ranges.size() != 2) {
            throw new MotuException(
                    ErrorType.INCONSISTENCY,
                    String.format("Inconsistency in list range (size %d) - (ExtractCriteriaLatLon.hasRanges)", ranges.size()));
        }
        Range rangeLat = ranges.get(0);
        Range rangeLon = ranges.get(1);

        return (CoordinateUtils.hasRange(rangeLat) && CoordinateUtils.hasRange(rangeLon));
    }

    /**
     * Returns true if range corresponds to a valid range. range is not valid if 'first' is 0 and 'last is
     * '-1'
     * 
     * @param range range to be tested
     * @return true or false
     */
    public static boolean hasRange(Range range) {
        if (range == null) {
            return false;
        }
        return !((range.first() == 0) && (range.last() == -1));
    }

    /**
     * Gets range values corresponding to range index.
     * @param gcs grid coordinate system from which range is computed
     * @param rangeLat latitude range
     * @param rangeLon longitude range
     * @param rangeValueLat latitude values corresponding to the range
     * @param rangeValueLon longitude values corresponding to the range
     */
    public static void getRangeValues(GridCoordSys gcs, Range rangeLat, Range rangeLon, double[] rangeValueLat, double[] rangeValueLon)
            throws MotuNotImplementedException {
        // this is the case where no point are included
        boolean hasLatRange = hasRange(rangeLat);
        boolean hasLonRange = hasRange(rangeLon);

        CoordinateAxis xaxis = gcs.getXHorizAxis();
        CoordinateAxis yaxis = gcs.getYHorizAxis();
        if ((xaxis instanceof CoordinateAxis1D) && (yaxis instanceof CoordinateAxis1D)) {
            CoordinateAxis1D xaxis1 = (CoordinateAxis1D) xaxis;
            CoordinateAxis1D yaxis1 = (CoordinateAxis1D) yaxis;
            if ((rangeValueLat != null) && hasLatRange) {
                rangeValueLat[0] = yaxis1.getCoordValue(rangeLat.first());
                rangeValueLat[1] = yaxis1.getCoordValue(rangeLat.last());
            }
            if ((rangeValueLon != null) && hasLonRange) {
                rangeValueLon[0] = getLongitudeM180P180(xaxis1.getCoordValue(rangeLon.first()));
                rangeValueLon[1] = getLongitudeM180P180(xaxis1.getCoordValue(rangeLon.last()));
            }
        } else if ((xaxis instanceof CoordinateAxis2D) && (yaxis instanceof CoordinateAxis2D) && gcs.isLatLon()) {
            CoordinateAxis2D lonAxis = (CoordinateAxis2D) xaxis;
            CoordinateAxis2D latAxis = (CoordinateAxis2D) yaxis;
            if ((rangeValueLat != null) && hasLatRange) {
                rangeValueLat[0] = latAxis.getCoordValue(rangeLat.first(), rangeLon.first());
                rangeValueLat[1] = latAxis.getCoordValue(rangeLat.last(), rangeLon.last());
            }
            if ((rangeValueLon != null) && hasLonRange) {
                rangeValueLon[0] = getLongitudeM180P180(lonAxis.getCoordValue(rangeLat.first(), rangeLon.first()));
                rangeValueLon[1] = getLongitudeM180P180(lonAxis.getCoordValue(rangeLat.last(), rangeLon.last()));
            }
        } else {
            throw new MotuNotImplementedException(
                    "Coordinate axes that are not 1D or 2D/LatLon are not implemented in ExtractCriteriaLatLon.toRange");
        }

    }

    public static String getMaxValForAxisAsString(CoordinateAxis axis) {
        String maxValStr = null;
        if (axis != null) {
            MAMath.MinMax minMax = NetCdfWriter.getMinMaxSkipMissingData(axis, null);
            maxValStr = NetCdfReader.getStandardLatAsString(minMax.max);
        }
        return maxValStr;
    }

    public static MAMath.MinMax getMinMaxValueForAxis(CoordinateAxis axis) {
        MAMath.MinMax minMax = new MAMath.MinMax(Double.MIN_VALUE, Double.MAX_VALUE);
        if (axis != null) {
            minMax = NetCdfWriter.getMinMaxSkipMissingData(axis, null);
        }
        return minMax;
    }

    public static double getMinValueForAxis(CoordinateAxis axis) {
        double minVal;
        if (axis == null) {
            minVal = Double.MIN_VALUE;
        } else {
            MAMath.MinMax minMax = NetCdfWriter.getMinMaxSkipMissingData(axis, null);
            return minMax.min;
        }
        return minVal;
    }

    public static double getMaxValueForAxis(CoordinateAxis axis) {
        double zMaxValue;
        if (axis == null) {
            zMaxValue = Double.MAX_VALUE;
        } else {
            MAMath.MinMax minMax = NetCdfWriter.getMinMaxSkipMissingData(axis, null);
            zMaxValue = minMax.max;
        }
        return zMaxValue;
    }


    /**
     * Compute the maximum index to include at a 1cm tolerance, the 'from' depth', in the input sorted
     * 'depths' array in meter in parameter .
     * 
     * @param depths The sorted array in meter.
     * @param from The depth from which to start.
     * @return The minimum index or -1 if not found
     */
    public static int findMinDepthIndex(double[] depths, double from) {
        int first = Arrays.binarySearch(depths, from);
        if (first < 0) {
            // Extract insertion point
            first = -1 - first;
            // Check at 1 cm for rounding tolerance
            if (first != 0 && (Math.floor(from * 100) - Math.floor(100 * depths[first - 1])) <= 1) {
                first--;
            }
        }
        if (first == depths.length) {
            first = -1;
        }
        return first;
    }

    /**
     * Compute the maximum index to include at a 1cm tolerance, the 'to' depth', in the input sorted 'depths'
     * array in meter in parameter .
     * 
     * @param depths The sorted array in meter.
     * @param to The depth to dig to.
     * @return The maximum index or -1 if not found
     */
    public static int findMaxDepthIndex(double[] depths, double to) {
        int last = Arrays.binarySearch(depths, to);
        if (last < 0) {
            // Extract insertion point
            last = -1 - last;
            // Check at 1 cm for rounding tolerance
            if (last == depths.length || Math.floor(100 * depths[last]) - Math.floor(100 * to) > 1) {
                last--;
            }
        } else if (last == 0 && Math.floor(100 * depths[0]) - Math.floor(100 * to) > 1) {
            last = -1;
        } else if (last == depths.length) {
            last--;
        }
        return last;
    }
}
