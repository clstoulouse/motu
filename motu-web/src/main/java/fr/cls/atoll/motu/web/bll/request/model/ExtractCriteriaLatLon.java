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

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import javax.measure.DecimalMeasure;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.library.inventory.GeospatialCoverage;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidLatLonRangeException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidLatitudeException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidLongitudeException;
import fr.cls.atoll.motu.web.bll.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.web.common.utils.CoordinateUtils;
import fr.cls.atoll.motu.web.dal.request.netcdf.NetCdfReader;
import fr.cls.atoll.motu.web.dal.request.netcdf.NetCdfWriter;
import fr.cls.atoll.motu.web.dal.tds.ncss.model.SpatialRange;
import ucar.ma2.ArrayDouble.D2;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;
import ucar.ma2.Range;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis2D;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dt.grid.GridCoordSys;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.geoloc.Projection;
import ucar.unidata.geoloc.ProjectionPoint;
import ucar.unidata.geoloc.ProjectionPointImpl;

//CSOFF: MultipleStringLiterals : avoid message in constants declaration and trace log.

/**
 * This class introduces geographical coverage criterias as latitude an longitude coordinates to be apply on
 * data (for extraction/selection and research).
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class ExtractCriteriaLatLon extends ExtractCriteriaGeo {

    /**
     * Latitude min.
     */
    public final static String LATITUDE_MIN = "-90";
    /**
     * Latitude max.
     */
    public final static String LATITUDE_MAX = "90";
    /**
     * Longitude min.
     */
    public final static String LONGITUDE_MIN = "-180";
    /**
     * Longitude max.
     */
    public final static String LONGITUDE_MAX = "180";

    /**
     * Bounding box for latitude/longitude points. This is a rectangle in lat/lon coordinates. Note that
     * LatLonPoint always has lon in the range +/-180. *
     * 
     * @uml.property name="latLonRect"
     */
    private LatLonRect latLonRect = null;

    private MAMath.MinMax minMaxXValue2D = null;
    private MAMath.MinMax minMaxYValue2D = null;

    /**
     * Default constructor.
     */
    public ExtractCriteriaLatLon() {
        // set a LatLonRect that covers the whole world.
        setLatLonRect(new LatLonRect());
    }

    public MAMath.MinMax getMinMaxXValue2D() {
        return minMaxXValue2D;
    }

    public MAMath.MinMax getMinMaxYValue2D() {
        return minMaxYValue2D;
    }

    /**
     * Constructor.
     * 
     * @param latLonRect lat/lon bounding box
     */
    public ExtractCriteriaLatLon(LatLonRect latLonRect) {
        setLatLonRect(latLonRect);
    }

    /**
     * Constructor.
     * 
     * @param latLonLow lat/lon low point
     * @param latLonHigh lat/lon high point
     */
    public ExtractCriteriaLatLon(LatLonPoint latLonLow, LatLonPoint latLonHigh) {
        setLatLonRect(latLonLow, latLonHigh);
    }

    /**
     * Constructor.
     * 
     * @param latLow latitude low
     * @param lonLow longitude low
     * @param latHigh latitude high
     * @param lonHigh longitude high
     */
    public ExtractCriteriaLatLon(double latLow, double lonLow, double latHigh, double lonHigh) {
        setLatLonRect(latLow, lonLow, latHigh, lonHigh);
    }

    /**
     * Instantiates a new extract criteria lat lon.
     * 
     * @param geospatialCoverage the geospatial coverage
     */
    public ExtractCriteriaLatLon(GeospatialCoverage geospatialCoverage) {
        setLatLonRect(geospatialCoverage);
    }

    /**
     * Instantiates a new extract criteria lat lon.
     * 
     * @param geospatialCoverage the geospatial coverage
     */
    public ExtractCriteriaLatLon(fr.cls.atoll.motu.web.dal.tds.ncss.model.GeospatialCoverage geospatialCoverage) {
        setLatLonRect(geospatialCoverage);
    }

    /**
     * Constructor.
     * 
     * @param latLow latitude low
     * @param lonLow longitude low
     * @param latHigh latitude high
     * @param lonHigh longitude high
     */
    public ExtractCriteriaLatLon(String latLow, String lonLow, String latHigh, String lonHigh)
            throws MotuInvalidLatitudeException, MotuInvalidLongitudeException {
        setLatLonRect(latLow, lonLow, latHigh, lonHigh);
    }

    /**
     * Constructor from a list that contains low latitude value, low longitude value, high latitude value,
     * high longitude value.
     * 
     * @param list to be converted
     * @throws MotuInvalidLatitudeException
     * @throws MotuInvalidLongitudeException
     */
    public ExtractCriteriaLatLon(List<String> list) throws MotuInvalidLatitudeException, MotuInvalidLongitudeException {

        switch (list.size()) {
        case 4:
            setLatLonRect(list.get(0), list.get(1), list.get(2), list.get(3));
            break;
        case 3:
            setLatLonRect(list.get(0), list.get(1), list.get(2), LONGITUDE_MAX);
            break;
        case 2:
            setLatLonRect(list.get(0), list.get(1), LATITUDE_MAX, LONGITUDE_MAX);
            break;
        case 1:
            setLatLonRect(list.get(0), LONGITUDE_MIN, LATITUDE_MAX, LONGITUDE_MAX);
            break;
        default:
            // set a LatLonRect that covers the whole world.
            setLatLonRect(new LatLonRect());
            break;
        }
    }

    /**
     * Getter of the property <tt>latLonRect</tt>.
     * 
     * @return Returns the latLonRect.
     * @uml.property name="latLonRect"
     */
    public LatLonRect getLatLonRect() {
        return this.latLonRect;
    }

    /**
     * Setter of the property <tt>latLonRect</tt>.
     * 
     * @param latLonRect The latLonRect to set.
     * @uml.property name="latLonRect"
     */
    public void setLatLonRect(LatLonRect latLonRect) {
        this.latLonRect = latLonRect;
    }

    /**
     * Setter of the property <tt>latLonRect</tt>.
     * 
     * @param latLonLow lat/lon low point
     * @param latLonHigh lat/lon high point
     * @uml.property name="latLonRect"
     */
    public void setLatLonRect(LatLonPoint latLonLow, LatLonPoint latLonHigh) {
        setLatLonRect(new LatLonRect(latLonLow, latLonHigh));
    }

    /**
     * Setter of the property <tt>latLonRect</tt>.
     * 
     * @param latLow latitude low
     * @param lonLow longitude low
     * @param latHigh latitude high
     * @param lonHigh longitude high
     * @uml.property name="latLonRect"
     */
    public void setLatLonRect(double latLow, double lonLow, double latHigh, double lonHigh) {

        // to fix problem with LatLonrect width computation
        // if ((Math.abs(lonHigh - lonLow) >= 360.) && ((Math.abs(lonHigh) >= 360.) || (Math.abs(lonLow) >=
        // 360.))) {
        // if ((Math.abs(lonHigh - lonLow) > 360.)) {
        // // lonHigh -= 0.000000001;
        // setLatLonRect(new LatLonPointImpl(latLow, -180.), new LatLonPointImpl(latHigh, 180.));
        // } else {
        setLatLonRect(new LatLonPointImpl(latLow, lonLow), new LatLonPointImpl(latHigh, lonHigh));
        // }

    }

    /**
     * Setter of the property <tt>latLonRect</tt>.
     * 
     * @param latLow latitude low
     * @param lonLow longitude low
     * @param latHigh latitude high
     * @param lonHigh longitude high
     * @throws MotuInvalidLatitudeException
     * @throws MotuInvalidLongitudeException
     * @uml.property name="latLonRect"
     */
    public void setLatLonRect(String latLow, String lonLow, String latHigh, String lonHigh)
            throws MotuInvalidLatitudeException, MotuInvalidLongitudeException {

        // to fix problem with LatLonrect width computation : unconvert longitude with no normalization here.
        setLatLonRect(NetCdfReader.unconvertLat(latLow),
                      NetCdfReader.unconvertLon(lonLow, false),
                      NetCdfReader.unconvertLat(latHigh),
                      NetCdfReader.unconvertLon(lonHigh, false));
    }

    /**
     * Sets the lat lon rect.
     * 
     * @param latLow the lat low
     * @param lonLow the lon low
     * @param latHigh the lat high
     * @param lonHigh the lon high
     */
    public void setLatLonRect(DecimalMeasure<?> latLow, DecimalMeasure<?> lonLow, DecimalMeasure<?> latHigh, DecimalMeasure<?> lonHigh) {
        double latlowTemp = (latLow != null) ? latLow.getValue().doubleValue() : Double.parseDouble(LATITUDE_MIN);
        double lonLowTemp = (lonLow != null) ? lonLow.getValue().doubleValue() : Double.parseDouble(LONGITUDE_MIN);
        double latHighTemp = (latHigh != null) ? latHigh.getValue().doubleValue() : Double.parseDouble(LATITUDE_MAX);
        double lonHighTemp = (lonHigh != null) ? lonHigh.getValue().doubleValue() : Double.parseDouble(LONGITUDE_MAX);

        setLatLonRect(latlowTemp, lonLowTemp, latHighTemp, lonHighTemp);
    }

    /**
     * Sets the lat lon rect.
     * 
     * @param geospatialCoverage the new lat lon rect
     */
    public void setLatLonRect(GeospatialCoverage geospatialCoverage) {
        if (geospatialCoverage == null) {
            return;
        }
        setLatLonRect(geospatialCoverage.getSouth(), geospatialCoverage.getWest(), geospatialCoverage.getNorth(), geospatialCoverage.getEast());
    }

    /**
     * Sets the lat lon rect.
     * 
     * @param geospatialCoverage the new lat lon rect
     */
    public void setLatLonRect(fr.cls.atoll.motu.web.dal.tds.ncss.model.GeospatialCoverage geospatialCoverage) {
        if (geospatialCoverage == null) {
            return;
        }

        SpatialRange spatialRangeNorthSouth = geospatialCoverage.getNorthsouth();
        SpatialRange spatialRangeEastWest = geospatialCoverage.getEastwest();

        if ((spatialRangeNorthSouth == null) || (spatialRangeEastWest == null)) {
            return;
        }

        double latLow = spatialRangeNorthSouth.getStart();
        double latHigh = latLow + spatialRangeNorthSouth.getSize();

        double lonLow = spatialRangeEastWest.getStart();
        double lonHigh = lonLow + spatialRangeEastWest.getSize();

        setLatLonRect(latLow, lonLow, latHigh, lonHigh);
    }

    /**
     * Return range corresponding to latitude/longitude criteria.
     * 
     * @param cs coordinate system from which range is computed (must have Lat/Lon axes)
     * @param rangeValueLat latitude values corresponding to the range
     * @param rangeValueLon longitude values corresponding to the range
     * @return list of two Range objects, first lat then lon.
     * @throws MotuException
     * @throws MotuNotImplementedException
     * @throws MotuInvalidLatLonRangeException
     */
    public List<Range> toRange(CoordinateSystem cs, double[] rangeValueLat, double[] rangeValueLon)
            throws MotuException, MotuInvalidLatLonRangeException, MotuNotImplementedException {

        Formatter errMessages = new Formatter();
        GridCoordSys gcs = new GridCoordSys(cs, errMessages);
        // NetCDF 2.2.16
        // List<Range> listRange = (List<Range>) gcs.getLatLonBoundingBox(latLonRect);
        // NetCDF 2.2.18
        // List<Range> listRange = (List<Range>) gcs.getRangesFromLatLonRect(latLonRect);
        List<Range> listRange = getRangesFromLatLonRect(gcs, latLonRect);

        if (listRange.size() != 2) {
            throw new MotuInvalidLatLonRangeException(latLonRect, gcs.getLatLonBoundingBox());
        }

        Range rangeLat = listRange.get(0);
        Range rangeLon = listRange.get(1);

        if (!(ExtractCriteriaLatLon.hasRange(rangeLat) && ExtractCriteriaLatLon.hasRange(rangeLon))) {
            throw new MotuInvalidLatLonRangeException(latLonRect, gcs.getLatLonBoundingBox());
        }

        if (rangeValueLat != null) {
            assert rangeValueLat.length == 2;
            rangeValueLat[0] = Double.MAX_VALUE;
            rangeValueLat[1] = Double.MAX_VALUE;
        }
        if (rangeValueLon != null) {
            assert rangeValueLon.length == 2;
            rangeValueLon[0] = Double.MAX_VALUE;
            rangeValueLon[1] = Double.MAX_VALUE;
        }

        if ((rangeLat == null) || (rangeLon == null)) {
            return listRange;
        }

        getRangeValues(gcs, rangeLat, rangeLon, rangeValueLat, rangeValueLon);

        return listRange;
    }

    /**
     * Gets range values corresponding to range index.
     * 
     * @param gcs grid coordinate system from which range is computed
     * @param rangeLat latitude range
     * @param rangeLon longitude range
     * @param rangeValueLat latitude values corresponding to the range
     * @param rangeValueLon longitude values corresponding to the range
     */
    private void getRangeValues(GridCoordSys gcs, Range rangeLat, Range rangeLon, double[] rangeValueLat, double[] rangeValueLon)
            throws MotuNotImplementedException {
        // this is the case where no point are included
        boolean hasLatRange = ExtractCriteriaLatLon.hasRange(rangeLat);
        boolean hasLonRange = ExtractCriteriaLatLon.hasRange(rangeLon);

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
                rangeValueLon[0] = CoordinateUtils.getLongitudeM180P180(xaxis1.getCoordValue(rangeLon.first()));
                rangeValueLon[1] = CoordinateUtils.getLongitudeM180P180(xaxis1.getCoordValue(rangeLon.last()));
            }
        } else if ((xaxis instanceof CoordinateAxis2D) && (yaxis instanceof CoordinateAxis2D) && gcs.isLatLon()) {
            CoordinateAxis2D lonAxis = (CoordinateAxis2D) xaxis;
            CoordinateAxis2D latAxis = (CoordinateAxis2D) yaxis;
            if ((rangeValueLat != null) && hasLatRange) {
                rangeValueLat[0] = latAxis.getCoordValue(rangeLat.first(), rangeLon.first());
                rangeValueLat[1] = latAxis.getCoordValue(rangeLat.last(), rangeLon.last());
            }
            if ((rangeValueLon != null) && hasLonRange) {
                rangeValueLon[0] = CoordinateUtils.getLongitudeM180P180(lonAxis.getCoordValue(rangeLat.first(), rangeLon.first()));
                rangeValueLon[1] = CoordinateUtils.getLongitudeM180P180(lonAxis.getCoordValue(rangeLat.last(), rangeLon.last()));
            }
        } else {
            throw new MotuNotImplementedException(
                    "Coordinate axes that are not 1D or 2D/LatLon are not implemented in ExtractCriteriaLatLon.toRange");
        }

    }

    /**
     * Gets a list of Index Ranges for the given lat, lon bounding box. For projection, only an approximation
     * based on lat/lon corners. Must have 2D/LatLon for x and y axis.
     * 
     * @param cs coordinate system from which range is computed (must have Lat/Lon axes)
     * @param listRangeValueLat latitude values (min/max) corresponding to the each range. Can be null.
     * @param listRangeValueLon longitude values (min/max) corresponding to the each range. Can be null.
     * @return a list of list of 2 Range objects, first y (lat) then x (lon). Can be empty if rect is out of
     *         range.
     * @throws MotuNotImplementedException
     * @throws MotuException
     * @throws InvalidRangeException
     */
    public List<List<Range>> toListRanges(CoordinateSystem cs, List<double[]> listRangeValueLat, List<double[]> listRangeValueLon)
            throws MotuException, MotuInvalidLatLonRangeException, MotuNotImplementedException, InvalidRangeException {
        Formatter errMessages = new Formatter();
        GridCoordSys gcs = new GridCoordSys(cs, errMessages);
        List<List<Range>> listRanges = getListRangesFromLatLonRect(gcs, latLonRect);
        if (ExtractCriteriaLatLon.hasEmptyYXRanges(listRanges)) {
            throw new MotuInvalidLatLonRangeException(latLonRect, gcs.getLatLonBoundingBox());
        }

        removeEmptyYXRanges(listRanges);

        int latMin = Integer.MAX_VALUE;
        int latMax = Integer.MIN_VALUE;
        int lonMin = Integer.MAX_VALUE;
        int lonMax = Integer.MIN_VALUE;
        double[] rangeValueLat = new double[2];
        rangeValueLat[0] = Double.MAX_VALUE;
        rangeValueLat[1] = Double.MIN_VALUE;
        double[] rangeValueLon = new double[2];
        rangeValueLon[0] = Double.MAX_VALUE;
        rangeValueLon[1] = Double.MIN_VALUE;
        boolean isFirstTime = true;
        if (listRanges.size() > 2) {
            if (!((listRangeValueLat == null) && (listRangeValueLon == null))) {
                for (List<Range> ranges : listRanges) {
                    Range rangeLat = ranges.get(0);
                    Range rangeLon = ranges.get(1);

                    if (ExtractCriteriaLatLon.hasRange(ranges)) {
                        double[] curRangeValueLat = new double[2];
                        curRangeValueLat[0] = Double.MAX_VALUE;
                        curRangeValueLat[1] = Double.MIN_VALUE;
                        double[] curRangeValueLon = new double[2];
                        curRangeValueLon[0] = Double.MAX_VALUE;
                        curRangeValueLon[1] = Double.MIN_VALUE;

                        getRangeValues(gcs, rangeLat, rangeLon, curRangeValueLat, curRangeValueLon);

                        // listRangeValueLat.add(curRangeValueLat);
                        // listRangeValueLon.add(curRangeValueLon);

                        if (curRangeValueLon[0] < rangeValueLon[0] || isFirstTime) {
                            // lonMin = rangeLon.first();
                            rangeValueLon[0] = curRangeValueLon[0];
                        }

                        if (curRangeValueLon[1] > rangeValueLon[1] || isFirstTime) {
                            // lonMax = rangeLon.last();
                            rangeValueLon[1] = curRangeValueLon[1];
                        }

                        if (curRangeValueLat[0] < rangeValueLat[0] || isFirstTime) {
                            // latMin = rangeLat.first();
                            rangeValueLat[0] = curRangeValueLat[0];
                        }

                        if (curRangeValueLat[1] > rangeValueLat[1] || isFirstTime) {
                            // latMax = rangeLat.last();
                            rangeValueLat[1] = curRangeValueLat[1];
                        }

                        if (rangeLon.first() < lonMin) {
                            lonMin = rangeLon.first();
                        }

                        if (rangeLon.last() > lonMax) {
                            lonMax = rangeLon.last();
                        }

                        if (rangeLat.first() < latMin) {
                            latMin = rangeLat.first();
                        }

                        if (rangeLat.last() > latMax) {
                            latMax = rangeLat.last();
                        }

                        isFirstTime = false;
                    }
                }
            }

            listRanges.clear();
            List<Range> rL = new ArrayList<>();
            rL.add(new Range(latMin, latMax));
            rL.add(new Range(lonMin, lonMax));
            listRanges.add(rL);

            listRangeValueLat.add(rangeValueLat);
            listRangeValueLon.add(rangeValueLon);
        } else

        {
            if ((listRangeValueLat == null) && (listRangeValueLon == null)) {
                return listRanges;
            }
            for (List<Range> ranges : listRanges) {
                rangeValueLat = new double[2];
                rangeValueLat[0] = Double.MAX_VALUE;
                rangeValueLat[1] = Double.MIN_VALUE;
                rangeValueLon = new double[2];
                rangeValueLon[0] = Double.MAX_VALUE;
                rangeValueLon[1] = Double.MIN_VALUE;

                Range rangeLat = ranges.get(0);
                Range rangeLon = ranges.get(1);

                if (!(ExtractCriteriaLatLon.hasRange(ranges))) {
                    continue;
                }
                getRangeValues(gcs, rangeLat, rangeLon, rangeValueLat, rangeValueLon);

                listRangeValueLat.add(rangeValueLat);
                listRangeValueLon.add(rangeValueLon);
            }
        }

        return listRanges;
    }

    /**
     * @return lower left latitude of the lat/lon box, Double.MAX_VALUE if not set.
     */
    public double getLowerLeftLat() {
        if (latLonRect == null) {
            return Double.MAX_VALUE;
        }
        return latLonRect.getLowerLeftPoint().getLatitude();
    }

    /**
     * @return upper left latitude of the lat/lon box, Double.MAX_VALUE if not set.
     */
    public double getUpperLeftLat() {
        if (latLonRect == null) {
            return Double.MAX_VALUE;
        }
        return latLonRect.getUpperLeftPoint().getLatitude();
    }

    /**
     * @return lower right latitude of the lat/lon box, Double.MAX_VALUE if not set.
     */
    public double getLowerRightLat() {
        if (latLonRect == null) {
            return Double.MAX_VALUE;
        }
        return latLonRect.getLowerRightPoint().getLatitude();
    }

    /**
     * @return upper right latitude of the lat/lon box, Double.MAX_VALUE if not set.
     */
    public double getUpperRightLat() {
        if (latLonRect == null) {
            return Double.MAX_VALUE;
        }
        return latLonRect.getUpperRightPoint().getLatitude();
    }

    /**
     * @return lower left longitude of the lat/lon box, Double.MAX_VALUE if not set.
     */
    public double getLowerLeftLon() {
        if (latLonRect == null) {
            return Double.MAX_VALUE;
        }
        return latLonRect.getLowerLeftPoint().getLongitude();
    }

    /**
     * @return upper left longitude of the lat/lon box, Double.MAX_VALUE if not set.
     */
    public double getUpperLeftLon() {
        if (latLonRect == null) {
            return Double.MAX_VALUE;
        }
        return latLonRect.getUpperLeftPoint().getLongitude();
    }

    /**
     * @return lower right longitude of the lat/lon box, Double.MAX_VALUE if not set.
     */
    public double getLowerRightLon() {
        if (latLonRect == null) {
            return Double.MAX_VALUE;
        }
        return latLonRect.getLowerRightPoint().getLongitude();
    }

    /**
     * @return upper right longitude of the lat/lon box, Double.MAX_VALUE if not set.
     */
    public double getUpperRightLon() {
        if (latLonRect == null) {
            return Double.MAX_VALUE;
        }
        return latLonRect.getUpperRightPoint().getLongitude();
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

        return (ExtractCriteriaLatLon.hasRange(rangeLat) && ExtractCriteriaLatLon.hasRange(rangeLon));
    }

    /**
     * Gets a list of Index Ranges for the given lat, lon bounding box. For projection, only an approximation
     * based on lat/lon corners. Must have 2D/LatLon for x and y axis.
     * 
     * @param gcs Grid CoordinateSystem
     * @param rect dounfing lat/lon rectangle
     * @return a list of list of 2 Range objects, first y (lat) then x (lon). Can be empty if rect is out of
     *         range.
     * @throws MotuNotImplementedException
     * @throws MotuException
     */
    private List<List<Range>> getListRangesFromLatLonRect(GridCoordSys gcs, LatLonRect rect) throws MotuNotImplementedException, MotuException {
        CoordinateAxis xaxis = gcs.getXHorizAxis();
        CoordinateAxis yaxis = gcs.getYHorizAxis();

        // Warning : CoordinateAxis min/max values doesn't have regard for missing value/fill value.
        MAMath.MinMax xMinMax = NetCdfWriter.getMinMaxSkipMissingData(xaxis, null);
        MAMath.MinMax yMinMax = NetCdfWriter.getMinMaxSkipMissingData(yaxis, null);

        LatLonRect gcsRect = new LatLonRect(new LatLonPointImpl(yMinMax.min, xMinMax.min), new LatLonPointImpl(yMinMax.max, xMinMax.max));
        // If geo criteria include Lat/Lon coord. system : set criteria to Lat/Lon Coord. System
        if (gcsRect.containedIn(rect)) {
            rect = new LatLonRect(gcsRect);
        } else {
            // If geo criteria include Lon coord. system : set criteria to /Lon Coord. System
            LatLonRect gcsRectLon = new LatLonRect(new LatLonPointImpl(0, xMinMax.min), new LatLonPointImpl(0, xMinMax.max));
            LatLonRect rectLon = new LatLonRect(new LatLonPointImpl(0, rect.getLonMin()), new LatLonPointImpl(0, rect.getLonMax()));
            if (gcsRectLon.containedIn(rectLon)) {
                rect = new LatLonRect(
                        new LatLonRect(new LatLonPointImpl(rect.getLatMin(), xMinMax.min), new LatLonPointImpl(rect.getLatMax(), xMinMax.max)));
            }

            // If geo criteria include Lat coord. system : set criteria to /Lat Coord. System
            LatLonRect gcsRectLat = new LatLonRect(new LatLonPointImpl(yMinMax.min, 0), new LatLonPointImpl(yMinMax.max, 0));
            LatLonRect rectLat = new LatLonRect(new LatLonPointImpl(rect.getLatMin(), 0), new LatLonPointImpl(rect.getLatMax(), 0));
            if (gcsRectLat.containedIn(rectLat)) {
                rect = new LatLonRect(
                        new LatLonRect(new LatLonPointImpl(yMinMax.min, rect.getLonMin()), new LatLonPointImpl(yMinMax.max, rect.getLonMax())));
            }

        }

        if ((xaxis instanceof CoordinateAxis2D) && (yaxis instanceof CoordinateAxis2D) && gcs.isLatLon()) {
            return getListRangesFromLatLonRect2D(gcs, rect);
        } else if ((xaxis instanceof CoordinateAxis1D) && (yaxis instanceof CoordinateAxis1D)) {
            return getListRangesFromLatLonRect1D(gcs, rect);
        } else {
            throw new MotuNotImplementedException(
                    "ERROR in ExtractCriteriaLatLon - getListRangesFromLatLonRect2D - Only implemented for 2D/LatLon or 1D axes");
        }

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
            if (!(ExtractCriteriaLatLon.hasRange(ranges))) {
                countEmptyRanges++;
            }
        }

        return (countEmptyRanges == listRanges.size());
    }

    /**
     * Removes Y/X empty ranges of a list Y/X ranges .
     * 
     * @param listRanges list of Y/X list ranges
     * @throws MotuException
     */
    public static void removeEmptyYXRanges(List<List<Range>> listRanges) throws MotuException {
        for (List<Range> ranges : listRanges) {
            if (!(ExtractCriteriaLatLon.hasRange(ranges))) {
                listRanges.remove(ranges);
            }
        }
    }

    /**
     * Gets a list of Index Ranges for the given lat, lon bounding box. For projection, only an approximation
     * based on lat/lon corners. Must have 1D/LatLon for x and y axis.
     * 
     * @param gcs Grid CoordinateSystem
     * @param rect dounfing lat/lon rectangle
     * @return a list of list of 2 Range objects, first y (lat) then x (lon). Can be empty if rect is out of
     *         range.
     * @throws MotuNotImplementedException
     * @throws MotuException
     */
    private List<List<Range>> getListRangesFromLatLonRect1D(GridCoordSys gcs, LatLonRect rect) throws MotuNotImplementedException, MotuException {
        List<List<Range>> listRanges = new ArrayList<List<Range>>();
        double minx;
        double maxx;
        double miny;
        double maxy;

        LatLonPointImpl llpt = rect.getLowerLeftPoint();
        LatLonPointImpl urpt = rect.getUpperRightPoint();
        LatLonPointImpl lrpt = rect.getLowerRightPoint();
        LatLonPointImpl ulpt = rect.getUpperLeftPoint();

        if (gcs.isLatLon()) {
            minx = getMinOrMaxLon(llpt.getLongitude(), ulpt.getLongitude(), true);
            miny = Math.min(llpt.getLatitude(), lrpt.getLatitude());
            maxx = getMinOrMaxLon(urpt.getLongitude(), lrpt.getLongitude(), false);
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

        CoordinateAxis xaxis = gcs.getXHorizAxis();
        CoordinateAxis yaxis = gcs.getYHorizAxis();
        if (!((xaxis instanceof CoordinateAxis1D) && (yaxis instanceof CoordinateAxis1D))) {
            throw new MotuNotImplementedException("ERROR in ExtractCriteriaLatLon - getListRangesFromLatLonRect1D - Only implemented for 1D axes");
        }

        CoordinateAxis1D xaxis1 = (CoordinateAxis1D) xaxis;
        CoordinateAxis1D yaxis1 = (CoordinateAxis1D) yaxis;
        // int minxIndex = xaxis1.findCoordElementBounded(minx);
        int minxIndex = findCoordElementBounded(xaxis1, minx, -1);
        // int minyIndex = yaxis1.findCoordElementBounded(miny);
        int minyIndex = findCoordElementBounded(yaxis1, miny, -1);
        // int maxxIndex = xaxis1.findCoordElementBounded(maxx);
        // FIX JIRA MOTU-133: Replace findCoordElementBounded(xaxis1, maxx, minxIndex+1);
        int maxxIndex = findCoordElementBounded(xaxis1, maxx, minxIndex);
        // int maxyIndex = yaxis1.findCoordElementBounded(maxy);
        int maxyIndex = findCoordElementBounded(yaxis1, maxy, -1);

        List<Range> ranges = new ArrayList<>();

        try {
            // has latitude range ?
            // if no return empty YX ranges
            // has longitude range ?
            // if no return empty YX ranges
            if (((minyIndex < 0) && (maxyIndex < 0)) || ((minxIndex < 0) && (maxxIndex < 0))) {
                // In Netcdf-Java 4.xx, no need to create empty ranges
                // return now an empty range list
                // listRanges.add(createEmptyYXRanges());
                return listRanges;
            }

            // min y index is negative (not found), search the nearest index
            if ((minyIndex < 0)) {
                minyIndex = yaxis1.findCoordElementBounded(miny);
            }
            // min y index is negative (not found), search the nearest index
            if (maxyIndex < 0) {
                maxyIndex = yaxis1.findCoordElementBounded(maxy);
            }

            // min x index is negative (not found), set min x index to
            // to zero)
            if ((minxIndex < 0)) {
                // minxIndex = maxxIndex;
                minxIndex = 0;
            }
            // min x and max x have same value ==> min x and max x are same point ==> max index = min index
            if ((maxxIndex < 0) && (minx == maxx)) {
                maxxIndex = minxIndex;
            }

            if (maxxIndex >= 0) {
                ranges.add(new Range(Math.min(minyIndex, maxyIndex), Math.max(minyIndex, maxyIndex)));
                ranges.add(new Range(Math.min(minxIndex, maxxIndex), Math.max(minxIndex, maxxIndex)));

                listRanges.add(ranges);
            } else {
                // maxxIndex is negative, get the last value
                maxxIndex = (int) xaxis1.getSize() - 1;
                ranges.add(new Range(Math.min(minyIndex, maxyIndex), Math.max(minyIndex, maxyIndex)));
                ranges.add(new Range(Math.min(minxIndex, maxxIndex), Math.max(minxIndex, maxxIndex)));

                listRanges.add(ranges);

                minxIndex = 0;
                maxxIndex = findCoordElementBounded(xaxis1, maxx, -1);
                if (maxxIndex >= 0) {
                    ranges = new ArrayList<>();

                    ranges.add(new Range(Math.min(minyIndex, maxyIndex), Math.max(minyIndex, maxyIndex)));
                    ranges.add(new Range(Math.min(minxIndex, maxxIndex), Math.max(minxIndex, maxxIndex)));

                    listRanges.add(ranges);
                }
            }
        } catch (InvalidRangeException e) {
            throw new MotuException(
                    ErrorType.INVALID_LAT_LON_RANGE,
                    "ERROR in ExtractCriteriaLatLon - getRangesFromLatLonRect1D - while creating list of ranges",
                    e);

        }

        return listRanges;
    }

    private MinMax computeMinMaxX(Projection dataProjection, LatLonRect rect) {
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

    private MinMax computeMinMaxY(Projection dataProjection, LatLonRect rect) {
        LatLonPointImpl llpt = rect.getLowerLeftPoint();
        LatLonPointImpl urpt = rect.getUpperRightPoint();
        LatLonPointImpl lrpt = rect.getLowerRightPoint();
        LatLonPointImpl ulpt = rect.getUpperLeftPoint();

        MinMax mm = new MinMax(Double.MIN_VALUE, Double.MAX_VALUE);
        mm.min = Math.min(llpt.getLatitude(), lrpt.getLatitude());
        mm.max = Math.min(ulpt.getLatitude(), urpt.getLatitude());

        return mm;
    }

    private MinMax[] computeMinMaxXY(Projection dataProjection, LatLonRect rect) {
        return new MinMax[] { computeMinMaxX(dataProjection, rect), computeMinMaxY(dataProjection, rect) };
    }

    private void checkXYAxis(CoordinateAxis xaxis, CoordinateAxis yaxis, GridCoordSys gcs) throws MotuNotImplementedException {
        if (!((xaxis instanceof CoordinateAxis2D) && (yaxis instanceof CoordinateAxis2D) && gcs.isLatLon())) {
            throw new MotuNotImplementedException("ERROR in ExtractCriteriaLatLon - getListRangesFromLatLonRect2D - Only implemented for 2D/LatLon");
        }
    }

    private double checkLon(double lon, double refXMin) {
        double lonRes = lon;
        if (Double.compare(lon, refXMin) < 0) {
            double longitudeCenter = refXMin + 180;
            lonRes = LatLonPointImpl.lonNormal(lon, longitudeCenter);
        }
        lonRes = CoordinateUtils.getLongitudeM180P180(lonRes);
        return lonRes;
    }

    private boolean isInside(double lat, double lon, MinMax minMaxX, MinMax minMaxY) {
        return (Double.compare(lat, minMaxY.min) >= 0) && (Double.compare(lat, minMaxY.max) <= 0) && (Double.compare(lon, minMaxX.min) >= 0)
                && (Double.compare(lon, minMaxX.max) <= 0);
    }

    /**
     * Gets a list of Index Ranges for the given lat, lon bounding box. For projection, only an approximation
     * based on lat/lon corners. Must have 2D/LatLon for x and y axis.
     * 
     * @param gcs Grid CoordinateSystem
     * @param rect dounfing lat/lon rectangle
     * @return a list of list of 2 Range objects, first y (lat) then x (lon). Can be empty if rect is out of
     *         range.
     * @throws MotuNotImplementedException
     * @throws MotuException
     */
    private List<List<Range>> getListRangesFromLatLonRect2D(GridCoordSys gcs, LatLonRect rect) throws MotuNotImplementedException, MotuException {
        MinMax[] minMaxXYRectAr = computeMinMaxXY(gcs.getProjection(), rect);
        MinMax minMaxX = minMaxXYRectAr[0];
        MinMax minMaxY = minMaxXYRectAr[1];

        CoordinateAxis xaxis = gcs.getXHorizAxis();
        CoordinateAxis yaxis = gcs.getYHorizAxis();
        checkXYAxis(xaxis, yaxis, gcs);

        CoordinateAxis2D lonAxis = (CoordinateAxis2D) xaxis;
        CoordinateAxis2D latAxis = (CoordinateAxis2D) yaxis;

        // lonAxis & latAxis have same shape, depending of X and Y coordinates
        int[] shape = lonAxis.getShape();
        int mini = Integer.MAX_VALUE;
        int minj = Integer.MAX_VALUE;
        int maxi = -1;
        int maxj = -1;
        boolean newRanges = false;

        D2 latAxisCoord2D = latAxis.getCoordValuesArray();
        D2 lonAxisCoord2D = lonAxis.getCoordValuesArray();
        List<List<Range>> listRanges = new ArrayList<>();
        // get each continuum ranges
        for (int j = 0; j < shape[0]; j++) {
            for (int i = 0; i < shape[1]; i++) {
                double lat = latAxisCoord2D.get(j, i);
                double lon = lonAxisCoord2D.get(j, i);
                if (!(latAxis.isMissing(lat) || lonAxis.isMissing(lon))) {
                    lon = checkLon(lon, minMaxX.min);
                    if (isInside(lat, lon, minMaxX, minMaxY)) {
                        if (i > maxi) {
                            maxi = i;
                        }
                        if (i < mini) {
                            mini = i;
                        }
                        newRanges = true;
                    }
                }

            }
            if (newRanges) {
                if (j > maxj) {
                    maxj = j;
                }
                if (j < minj) {
                    minj = j;
                }
                onNewRange(listRanges, mini, minj, maxi, maxj, latAxis, lonAxis, minMaxX);
                newRanges = false;
                mini = Integer.MAX_VALUE;
                minj = Integer.MAX_VALUE;
                maxi = -1;
                maxj = -1;
            }
        }

        return listRanges;
    }

    private void onNewRange(List<List<Range>> listRanges,
                            int mini,
                            int minj,
                            int maxi,
                            int maxj,
                            CoordinateAxis2D latAxis,
                            CoordinateAxis2D lonAxis,
                            MinMax xMinMax)
            throws MotuException {
        try {
            listRanges.add(createRange(minj, maxj, mini, maxi));
            minMaxYValue2D = computeLatMinMax(minMaxYValue2D, latAxis, minj, mini, maxj, maxi);
            minMaxXValue2D = computeLonMinMax(minMaxXValue2D, lonAxis, minj, mini, maxj, maxi, xMinMax.min, xMinMax.max);
        } catch (InvalidRangeException e) {
            throw new MotuException(
                    ErrorType.BAD_PARAMETERS,
                    "ERROR in ExtractCriteriaLatLon - getListRangesFromLatLonRect2D - while creating list of ranges",
                    e);
        }
    }

    private List<Range> createRange(int minj, int maxj, int mini, int maxi) throws InvalidRangeException {
        List<Range> rangeList = new ArrayList<>();
        rangeList.add(new Range(minj, maxj));
        rangeList.add(new Range(mini, maxi));
        return rangeList;
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
    public MAMath.MinMax computeLonMinMax(MAMath.MinMax minMaxXValue2D,
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
    public MAMath.MinMax computeLatMinMax(MAMath.MinMax minMaxYValue2D, CoordinateAxis2D latAxis, int minj, int mini, int maxj, int maxi)
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

    public MinMax computeMinMax(MinMax ref, MinMax work) {
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
    public List<Range> getRangesFromLatLonRect(GridCoordSys gcs, LatLonRect rect) throws MotuException, MotuNotImplementedException {

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
            minx = getMinOrMaxLon(llpt.getLongitude(), ulpt.getLongitude(), true);
            miny = Math.min(llpt.getLatitude(), lrpt.getLatitude());
            maxx = getMinOrMaxLon(urpt.getLongitude(), lrpt.getLongitude(), false);
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
    public int findCoordElementBounded(CoordinateAxis1D axis, double pos, int lastIndex) throws MotuNotImplementedException {
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

}
// CSON: MultipleStringLiterals
