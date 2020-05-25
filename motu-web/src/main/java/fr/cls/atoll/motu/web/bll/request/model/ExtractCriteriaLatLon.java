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

    public static final double LATITUDE_MIN = -90;
    public static final double LATITUDE_TOTAL = 180;
    public static final double LONGITUDE_TOTAL = 360;

    /**
     * Bounding box for latitude/longitude points. This is a rectangle in lat/lon coordinates. Note that
     * LatLonPoint always has lon in the range +/-180. *
     * 
     * @uml.property name="latLonRect"
     */
    private LatLonRect latLonRect = null;

    private double lonMin = Double.NaN;
    private double width = LONGITUDE_TOTAL;
    private double latMin = Double.NaN;
    private double height = LATITUDE_TOTAL;

    private MAMath.MinMax minMaxXValue2D = null;
    private MAMath.MinMax minMaxYValue2D = null;

    /**
     * Default constructor.
     */
    public ExtractCriteriaLatLon() {
        // set a LatLonRect that covers the whole world.
        latLonRect = new LatLonRect();
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
     * @param height latitude height
     * @param width longitude width
     */
    public ExtractCriteriaLatLon(double latLow, double lonLow, double height, double width) {
        setLatLonRect(latLow, lonLow, height, width);
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
     * Constructor from a list that contains low latitude value, low longitude value, high latitude value,
     * high longitude value.
     * 
     * @param list to be converted
     * @throws MotuInvalidLatitudeException
     * @throws MotuInvalidLongitudeException
     */
    public ExtractCriteriaLatLon(List<String> list) throws MotuInvalidLatitudeException, MotuInvalidLongitudeException {
        if (!list.isEmpty()) {
            latMin = NetCdfReader.unconvertLat(list.get(0));
            if (list.size() > 1) {
                lonMin = NetCdfReader.unconvertLon(list.get(1), false);
                if (list.size() > 2) {
                    height = Math.abs(NetCdfReader.unconvertLat(list.get(2)) - latMin);
                    if (list.size() > 3) {
                        double lonMax = NetCdfReader.unconvertLon(list.get(3), false);
                        while (lonMin > lonMax) {
                            lonMax += LONGITUDE_TOTAL;
                        }
                        width = lonMax - lonMin;
                        if (width > LONGITUDE_TOTAL) {
                            width %= LONGITUDE_TOTAL;
                        }
                    }
                }
                setLatLonRect(latMin, lonMin, height, width);
            }
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
        latMin = latLonRect.getLatMin();
        lonMin = latLonRect.getLonMin();
        height = Math.min(Math.abs(latLonRect.getLatMax() - latMin), LATITUDE_TOTAL);
        width = Math.min(Math.abs(latLonRect.getLonMax() - lonMin), LONGITUDE_TOTAL);
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
     * @param height latitude height
     * @param width longitude width
     * @uml.property name="latLonRect"
     */
    public void setLatLonRect(double latLow, double lonLow, double height, double width) {
        setLatLonRect(new LatLonPointImpl(latLow, lonLow), new LatLonPointImpl(latLow + height, lonLow + width));
        lonMin = lonLow;
        this.width = width;
    }

    /**
     * Sets the lat lon rect.
     * 
     * @param geospatialCoverage the new lat lon rect
     */
    public void setLatLonRect(GeospatialCoverage geospatialCoverage) {
        if (geospatialCoverage != null) {
            if (geospatialCoverage.getSouth() != null) {
                latMin = geospatialCoverage.getSouth().getValue().doubleValue();
            }
            if (geospatialCoverage.getWest() != null) {
                lonMin = geospatialCoverage.getWest().getValue().doubleValue();
            }
            if (geospatialCoverage.getNorth() != null) {
                double latMax = geospatialCoverage.getNorth().getValue().doubleValue();
                if (Double.isNaN(latMin)) {
                    latMin = LATITUDE_MIN;
                }
                height = Math.min(Math.abs(latMax - latMin), LATITUDE_TOTAL);
            }
            if (geospatialCoverage.getEast() != null) {
                double lonMax = geospatialCoverage.getEast().getValue().doubleValue();
                if (Double.isNaN(lonMin)) {
                    lonMin = lonMax - LONGITUDE_TOTAL;
                }
                width = Math.min(Math.abs(lonMax - lonMin), LONGITUDE_TOTAL);
            }
            if (!Double.isNaN(latMin) && !Double.isNaN(lonMin)) {
                setLatLonRect(latMin, lonMin, height, width);
            }
        }
    }

    /**
     * Sets the lat lon rect.
     * 
     * @param geospatialCoverage the new lat lon rect
     */
    public void setLatLonRect(fr.cls.atoll.motu.web.dal.tds.ncss.model.GeospatialCoverage geospatialCoverage) {
        if (geospatialCoverage != null) {
            SpatialRange spatialRangeNorthSouth = geospatialCoverage.getNorthsouth();
            if ((spatialRangeNorthSouth != null)) {
                latMin = spatialRangeNorthSouth.getStart();
                height = spatialRangeNorthSouth.getSize();
            }

            SpatialRange spatialRangeEastWest = geospatialCoverage.getEastwest();
            if ((spatialRangeEastWest != null)) {
                lonMin = spatialRangeEastWest.getStart();
                width = spatialRangeEastWest.getSize();
            }
            if (!Double.isNaN(latMin) && !Double.isNaN(lonMin)) {
                setLatLonRect(latMin, lonMin, height, width);
            }
        }
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
        List<Range> listRange = CoordinateUtils.getRangesFromLatLonRect(gcs, latLonRect);

        if (listRange.size() != 2) {
            throw new MotuInvalidLatLonRangeException(latLonRect, gcs.getLatLonBoundingBox());
        }

        Range rangeLat = listRange.get(0);
        Range rangeLon = listRange.get(1);

        if (!(CoordinateUtils.hasRange(rangeLat) && CoordinateUtils.hasRange(rangeLon))) {
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

        CoordinateUtils.getRangeValues(gcs, rangeLat, rangeLon, rangeValueLat, rangeValueLon);

        return listRange;
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
        GridCoordSys gcs = null;
        if (cs instanceof GridCoordSys) {
            gcs = (GridCoordSys) cs;
        } else {
            gcs = new GridCoordSys(cs, errMessages);
        }
        List<List<Range>> listRanges = getListRangesFromLatLonRect(gcs, latLonRect);
        if (CoordinateUtils.hasEmptyYXRanges(listRanges)) {
            throw new MotuInvalidLatLonRangeException(latLonRect, gcs.getLatLonBoundingBox());
        }

        CoordinateUtils.removeEmptyYXRanges(listRanges);

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
        if (listRanges.size() > 2) { // This section is about the request of curvilign method.
            if (!((listRangeValueLat == null) && (listRangeValueLon == null))) {
                for (List<Range> ranges : listRanges) {
                    Range rangeLat = ranges.get(0);
                    Range rangeLon = ranges.get(1);

                    if (CoordinateUtils.hasRange(ranges)) {
                        double[] curRangeValueLat = new double[2];
                        curRangeValueLat[0] = Double.MAX_VALUE;
                        curRangeValueLat[1] = Double.MIN_VALUE;
                        double[] curRangeValueLon = new double[2];
                        curRangeValueLon[0] = Double.MAX_VALUE;
                        curRangeValueLon[1] = Double.MIN_VALUE;

                        CoordinateUtils.getRangeValues(gcs, rangeLat, rangeLon, curRangeValueLat, curRangeValueLon);

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
        } else {
            // This section is for normal request or antemeridian request.
            // If it's a normal method, only one range is defined.
            // If it's the antemeridian request, they are 2 ranges.
            // One range on the left of antemeridian and one range on the right of antemeridian
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

                if (!(CoordinateUtils.hasRange(ranges))) {
                    continue;
                }
                CoordinateUtils.getRangeValues(gcs, rangeLat, rangeLon, rangeValueLat, rangeValueLon);

                listRangeValueLat.add(rangeValueLat);
                listRangeValueLon.add(rangeValueLon);
            }
        }

        return listRanges;
    }

    /**
     * Gets the value of lonMin.
     *
     * @return the value of lonMin
     */
    public double getLonMin() {
        return lonMin;
    }

    /**
     * Gets the value of latMin.
     *
     * @return the value of latMin
     */
    public double getLatMin() {
        return latMin;
    }

    /**
     * Gets the value of lonMax.
     *
     * @return the value of lonMax
     */
    public double getLonMax() {
        if (!Double.isNaN(lonMin)) {
            return lonMin + width;
        } else {
            return lonMin;
        }
    }

    /**
     * Gets the value of latMax.
     *
     * @return the value of latMax
     */
    public double getLatMax() {
        if (!Double.isNaN(latMin)) {
            return latMin + height;
        } else {
            return latMin;
        }
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
     * Gets the value of width.
     *
     * @return the value of width
     */
    public double getWidth() {
        return width;
    }

    /**
     * Gets the value of height.
     *
     * @return the value of height
     */
    public double getHeight() {
        return height;
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

        CoordinateAxis xaxis = gcs.getXHorizAxis();
        CoordinateAxis yaxis = gcs.getYHorizAxis();
        if (!((xaxis instanceof CoordinateAxis1D) && (yaxis instanceof CoordinateAxis1D))) {
            throw new MotuNotImplementedException("ERROR in ExtractCriteriaLatLon - getListRangesFromLatLonRect1D - Only implemented for 1D axes");
        }

        CoordinateAxis1D xaxis1 = (CoordinateAxis1D) xaxis;
        CoordinateAxis1D yaxis1 = (CoordinateAxis1D) yaxis;
        // int minxIndex = xaxis1.findCoordElementBounded(minx);
        int minxIndex = CoordinateUtils.findCoordElementBounded(xaxis1, minx, -1);
        // int minyIndex = yaxis1.findCoordElementBounded(miny);
        int minyIndex = CoordinateUtils.findCoordElementBounded(yaxis1, miny, -1);
        // int maxxIndex = xaxis1.findCoordElementBounded(maxx);
        // FIX JIRA MOTU-133: Replace findCoordElementBounded(xaxis1, maxx, minxIndex+1);
        int maxxIndex = CoordinateUtils.findCoordElementBounded(xaxis1, maxx, minxIndex);
        // int maxyIndex = yaxis1.findCoordElementBounded(maxy);
        int maxyIndex = CoordinateUtils.findCoordElementBounded(yaxis1, maxy, -1);

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
                maxxIndex = CoordinateUtils.findCoordElementBounded(xaxis1, maxx, -1);
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
        MinMax[] minMaxXYRectAr = CoordinateUtils.computeMinMaxXY(gcs.getProjection(), rect);
        MinMax minMaxX = minMaxXYRectAr[0];
        MinMax minMaxY = minMaxXYRectAr[1];

        CoordinateAxis xaxis = gcs.getXHorizAxis();
        CoordinateAxis yaxis = gcs.getYHorizAxis();
        CoordinateUtils.checkXYAxis(xaxis, yaxis, gcs);

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
                    lon = CoordinateUtils.checkLon(lon, minMaxX.min);
                    if (CoordinateUtils.isInside(lat, lon, minMaxX, minMaxY)) {
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
            listRanges.add(CoordinateUtils.createRange(minj, maxj, mini, maxi));
            minMaxYValue2D = CoordinateUtils.computeLatMinMax(minMaxYValue2D, latAxis, minj, mini, maxj, maxi);
            minMaxXValue2D = CoordinateUtils.computeLonMinMax(minMaxXValue2D, lonAxis, minj, mini, maxj, maxi, xMinMax.min, xMinMax.max);
        } catch (InvalidRangeException e) {
            throw new MotuException(
                    ErrorType.BAD_PARAMETERS,
                    "ERROR in ExtractCriteriaLatLon - getListRangesFromLatLonRect2D - while creating list of ranges",
                    e);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Lat min=").append(latMin).append(" height=").append(height).append(" max=").append(latMin + height);
        sb.append("\nLon min=").append(lonMin).append(" width=").append(width).append(" max=").append(lonMin + width);
        sb.append("\nX min max=").append(minMaxXValue2D);
        sb.append("\nY min max=").append(minMaxYValue2D);
        return sb.toString();
    }
}
// CSON: MultipleStringLiterals
