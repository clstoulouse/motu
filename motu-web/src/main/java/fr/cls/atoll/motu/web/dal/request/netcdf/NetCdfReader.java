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
package fr.cls.atoll.motu.web.dal.request.netcdf;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.bind.JAXBElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.datetime.FastDateFormat;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDateException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDepthException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidLatitudeException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidLongitudeException;
import fr.cls.atoll.motu.web.bll.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfAttributeException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfAttributeNotFoundException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfVariableException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfVariableNotFoundException;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.dal.config.stdname.xml.model.StandardName;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants._Coordinate;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dods.DODSNetcdfFile;
import ucar.nc2.ncml.NcMLWriter;
import ucar.nc2.units.DateUnit;
import ucar.nc2.units.SimpleUnit;
import ucar.nc2.util.CancelTask;
import ucar.unidata.geoloc.LatLonPointImpl;

// CSOFF: MultipleStringLiterals : avoid message in constants declaration and trace log.

/**
 * Class to read netCDF files.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class NetCdfReader {

    /** Logger for this class. */
    private static final Logger LOG = LogManager.getLogger();

    /** NetCdf global attribute "Title". */
    public final static String GLOBALATTRIBUTE_TITLE = "title";

    /** NetCdf global attribute "Institution". */
    public final static String GLOBALATTRIBUTE_INSTITUTION = "institution";

    /** NetCdf global attribute "Source". */
    public final static String GLOBALATTRIBUTE_SOURCE = "source";

    /** NetCdf global attribute "History". */
    public final static String GLOBALATTRIBUTE_HISTORY = "history";

    /** NetCdf global attribute "References". */
    public final static String GLOBALATTRIBUTE_REFERENCES = "references";

    /** NetCdf global attribute "Comment". */
    public final static String GLOBALATTRIBUTE_COMMENT = "comment";

    /** NetCdf global attribute "Conventions". */
    public final static String GLOBALATTRIBUTE_CONVENTIONS = "Conventions";

    /** NetCdf global Conventions attribute value. */
    public final static String GLOBALATTRIBUTE_CONVENTIONS_VALUE = "CF-1.0";

    /** NetCdf global attribute "easting". */
    public final static String GLOBALATTRIBUTE_EASTING = "easting";

    /** NetCdf global attribute "northing". */
    public final static String GLOBALATTRIBUTE_NORTHING = "northing";

    /** NetCdf global attribute "julian_day_unit". */
    public final static String GLOBALATTRIBUTE_JULIAN_DAY_UNIT = "julian_day_unit";

    /** NetCdf global attribute "latitude_min". */
    public final static String GLOBALATTRIBUTE_LATITUDE_MIN = "latitude_min";

    /** NetCdf global attribute "latitude_max". */
    public final static String GLOBALATTRIBUTE_LATITUDE_MAX = "latitude_max";

    /** NetCdf global attribute "longitude_min". */
    public final static String GLOBALATTRIBUTE_LONGITUDE_MIN = "longitude_min";

    /** NetCdf global attribute "longitude_max". */
    public final static String GLOBALATTRIBUTE_LONGITUDE_MAX = "longitude_max";

    /** NetCdf global attribute "z_min". */
    public final static String GLOBALATTRIBUTE_Z_MIN = "z_min";

    /** NetCdf global attribute "z_max". */
    public final static String GLOBALATTRIBUTE_Z_MAX = "z_max";

    /** NetCdf global attribute "time_min". */
    public final static String GLOBALATTRIBUTE_TIME_MIN = "time_min";

    /** NetCdf global attribute "time_max". */
    public final static String GLOBALATTRIBUTE_TIME_MAX = "time_max";

    /** NetCdf global attribute "OriginalName". */
    public final static String GLOBALATTRIBUTE_ORIGINALNAME = "OriginalName";

    /** NetCdf global attribute "CreatedBy". */
    public final static String GLOBALATTRIBUTE_CREATEDBY = "CreatedBy";

    /** NetCdf global attribute "FileType". */
    public final static String GLOBALATTRIBUTE_FILETYPE = "FileType";

    /** NetCdf variable attribute "_FillValue". */
    public final static String VARIABLEATTRIBUTE_FILEVALUE = "_FillValue";

    /** NetCdf variable attribute "_FillValue". */
    public final static String VARIABLEATTRIBUTE_MISSINGVALUE = "missing_value";

    /** NetCdf variable attribute "valid_min". */
    public final static String VARIABLEATTRIBUTE_VALID_MIN = "valid_min";

    /** NetCdf variable attribute "valid_max". */
    public final static String VARIABLEATTRIBUTE_VALID_MAX = "valid_max";

    /** NetCdf variable attribute "axis". */
    public final static String VARIABLEATTRIBUTE_AXIS = "axis";

    /** The Constant VARIABLEATTRIBUTE_UNIT_LONG. */
    public final static String VARIABLEATTRIBUTE_UNIT_LONG = "unit_long";

    /** NetCdf variable attribute "long_name". */
    public final static String VARIABLEATTRIBUTE_LONG_NAME = "long_name";

    /** The Constant VARIABLEATTRIBUTE_GRID_MAPPING. */
    public final static String VARIABLEATTRIBUTE_GRID_MAPPING = "grid_mapping";

    /** NetCdf variable attribute "standard_name". */
    public final static String VARIABLEATTRIBUTE_STANDARD_NAME = "standard_name";

    /** NetCdf variable Time axis attribute value". */
    public final static String VARIABLEATTRIBUTE_TIME_AXIS_VALUE = "T";

    /** NetCdf variable X axis attribute value". */
    public final static String VARIABLEATTRIBUTE_X_AXIS_VALUE = "X";

    /** NetCdf variable Y axis attribute value". */
    public final static String VARIABLEATTRIBUTE_Y_AXIS_VALUE = "Y";

    /** NetCdf variable Z axis attribute value". */
    public final static String VARIABLEATTRIBUTE_Z_AXIS_VALUE = "Z";

    /** NetCdf variable time long_name attribute value". */
    public final static String VARIABLEATTRIBUTE_TIME_LONG_NAME_VALUE = "time";

    /** NetCdf variable time standard_name attribute value". */
    public final static String VARIABLEATTRIBUTE_TIME_STANDARD_NAME_VALUE = "time";

    /** NetCdf variable latitude long_name attribute value". */
    public final static String VARIABLEATTRIBUTE_LAT_LONG_NAME_VALUE = "latitude";

    /** NetCdf variable latitude standard_name attribute value". */
    public final static String VARIABLEATTRIBUTE_LAT_STANDARD_NAME_VALUE = "latitude";

    /** NetCdf variable longitude long_name attribute value". */
    public final static String VARIABLEATTRIBUTE_LON_LONG_NAME_VALUE = "longitude";

    /** NetCdf variable latitude standard_name attribute value". */
    public final static String VARIABLEATTRIBUTE_LON_STANDARD_NAME_VALUE = "longitude";

    /** NetCdf variable geoZ long_name attribute value". */
    public final static String VARIABLEATTRIBUTE_GEOZ_LONG_NAME_VALUE = "Z";

    /** NetCdf variable geoZ standard_name attribute value". */
    public final static String VARIABLEATTRIBUTE_GEOZ_STANDARD_NAME_VALUE = "Z";

    /** NetCdf variable geoY long_name attribute value". */
    public final static String VARIABLEATTRIBUTE_GEOY_LONG_NAME_VALUE = "Y";

    /** NetCdf variable geoY standard_name attribute value". */
    public final static String VARIABLEATTRIBUTE_GEOY_STANDARD_NAME_VALUE = "Y";

    /** NetCdf variable geoX long_name attribute value". */
    public final static String VARIABLEATTRIBUTE_GEOX_LONG_NAME_VALUE = "X";

    /** NetCdf variable geoX standard_name attribute value". */
    public final static String VARIABLEATTRIBUTE_GEOX_STANDARD_NAME_VALUE = "X";

    /** NetCdf variable depth long_name attribute value". */
    public final static String VARIABLEATTRIBUTE_DEPTH_LONG_NAME_VALUE = "depth";

    /** NetCdf variable depth standard_name attribute value". */
    public final static String VARIABLEATTRIBUTE_DEPTH_STANDARD_NAME_VALUE = "depth";

    /** NetCdf variable depth standard_name attribute value". */
    public final static String VARIABLEATTRIBUTE_DOWN_NAME_VALUE = "down";

    /** Latitude / longitude decimal format. */
    public final static String LATLON_DECIMALFORMAT = "##0.#####";

    /** GeoX / GeoY decimal format. */
    public final static String GEOXY_DECIMALFORMAT = "##0.#####";

    /** Z decimal format. */
    public final static String Z_DECIMALFORMAT = "##0.#####";

    /** Z string value when Z is zero. */
    public final static String Z_ZEROVALUE = "Surface";

    /** Date/time format. */
    public final static String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /** Date format. */
    public final static String DATE_FORMAT = "yyyy-MM-dd";

    public static final TimeZone GMT_TIMEZONE = TimeZone.getTimeZone("GMT");

    /** Date format with time (DATETIME_FORMAT). */
    public static final FastDateFormat DATETIME_TO_STRING_DEFAULT = FastDateFormat.getInstance(DATETIME_FORMAT, GMT_TIMEZONE);

    /** Date format without time (DATE_FORMAT). */
    public static final FastDateFormat DATE_TO_STRING_DEFAULT = FastDateFormat.getInstance(DATE_FORMAT, GMT_TIMEZONE);

    private static final String[] DATE_FORMATS = new String[] { "yyyy-mm-dd h:m:s", "yyyy-mm-dd'T'h:m:s", "yyyy-mm-dd" };

    /** Names of possible longitude. */
    public static final String[] LONGITUDE_NAMES = { "longitude", "Longitude", "LONGITUDE", "lon", "Lon", "LON", };

    /** Names of possible latitude. */
    public static final String[] LATITUDE_NAMES = { "latitude", "Latitude", "LATITUDE", "lat", "Lat", "LAT", };

    /** Names of possible GeoX. */
    public static final String[] GEOX_NAMES = { "x", "X", };

    /** Names of possible GeoY. */
    public static final String[] GEOY_NAMES = { "y", "Y", };

    /** The Constant SCALE_FACTOR_ATTR_NAME. */
    public final static String SCALE_FACTOR_ATTR_NAME = "scale_factor";

    /** The Constant OFFSET_ATTR_NAME. */
    public final static String ADD_OFFSET_ATTR_NAME = "add_offset";

    /** The location data. */
    private String locationData = "";

    /** NetCdf dataset. */
    private NetcdfDataset netcdfDataset = null;

    /** The is open with enhance var. */
    protected boolean isOpenWithEnhanceVar = true;

    private final Map<String, Variable> orignalVariables;

    /** Does Service needs CAS authentication to access catalog resources and data. */
    protected boolean casAuthentication = false;

    /**
     * 
     * Default constructor.
     */
    public NetCdfReader() {
        init();
        orignalVariables = new HashMap<>();
    }

    /**
     * /** Constructor.
     * 
     * @param locationData NetCDF file name or Opendap location data (URL) to read.
     */
    public NetCdfReader(String locationData) {
        this();
        this.locationData = locationData;
    }

    /**
     * Intialization.
     */
    private void init() {
        NetcdfDataset.setUseNaNs(false);
    }

    /**
     * Checks if is cas authentication.
     * 
     * @return true, if is cas authentication
     */
    public boolean isCasAuthentication() {
        return casAuthentication;
    }

    /**
     * Sets the cas authentication.
     * 
     * @param casAuthentication the new cas authentication
     */
    public void setCasAuthentication(boolean casAuthentication) {
        this.casAuthentication = casAuthentication;
    }

    /**
     * Gets the orignal variables.
     * 
     * @return the orignal variables
     */
    public Map<String, Variable> getOrignalVariables() {
        return orignalVariables;
    }

    /**
     * Getter of the property <tt>locationData</tt>.
     * 
     * @return Returns the locationData.
     * 
     * @uml.property name="locationData"
     */
    public String getLocationData() {
        return locationData;
    }

    /**
     * Setter of the property <tt>locationData</tt>.
     * 
     * @param locationData The urlSite to set.
     * 
     * @uml.property name="locationData"
     */
    public void setLocationData(String locationData) {
        this.locationData = locationData;
    }

    /**
     * Getter of the property <tt>netcdfDataset</tt>.
     * 
     * @return Returns the dataset.
     * 
     * @uml.property name="netcdfDataset"
     */
    public NetcdfDataset getNetcdfDataset() {
        return netcdfDataset;
    }

    /**
     * Setter of the property <tt>netcdfDataset</tt>.
     * 
     * @param netcdfDataset The dataset to set.
     * 
     * @uml.property name="netcdfDataset"
     */
    public void setNetcdfDataset(NetcdfDataset netcdfDataset) {
        this.netcdfDataset = netcdfDataset;
    }

    /**
     * Gets the coordinate systems.
     * 
     * @return coordinate systems.
     */
    public List<CoordinateSystem> getCoordinateSystems() {
        return getNetcdfDataset().getCoordinateSystems();
    }

    /**
     * Controls that all axes have an axis type. If an axis has an axis type to null, it tries set the axis
     * type (GeoX or GeoY).
     */
    private void controlAxes() {
        List<CoordinateAxis> coordinateAxes = getNetcdfDataset().getCoordinateAxes();
        for (CoordinateAxis coord : coordinateAxes) {
            if (coord.getAxisType() != null) {
                continue;
            }

            if (coord.getShortName().equalsIgnoreCase("x")) {
                Dimension dim = getNetcdfDataset().findDimension(coord.getFullName());
                if (dim == null) {
                    continue;
                }
                coord.setAxisType(AxisType.GeoX);
                coord.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.GeoX.toString()));
            }
            if (coord.getShortName().equalsIgnoreCase("y")) {
                Dimension dim = getNetcdfDataset().findDimension(coord.getFullName());
                if (dim == null) {
                    continue;
                }
                coord.setAxisType(AxisType.GeoY);
                coord.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.GeoY.toString()));
            }

        }
    }

    /**
     * Gets the coordinate axes.
     * 
     * @return coordinate systems.
     */
    public List<CoordinateAxis> getCoordinateAxes() {
        return getNetcdfDataset().getCoordinateAxes();
    }

    /**
     * Gets the dimension list.
     * 
     * @return the dimensions contained directly in the root group.
     */
    public List<Dimension> getDimensionList() {
        return getNetcdfDataset().getRootGroup().getDimensions();
    }

    /**
     * Gets the root variable.
     * 
     * @param shortName - short name of variable.
     * 
     * @return the variable with the specified (short) name in the root group.
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     */
    public Variable getRootVariable(String shortName) throws NetCdfVariableNotFoundException {
        Variable var = getNetcdfDataset().getRootGroup().findVariable(shortName);
        if (var == null) {
            throw new NetCdfVariableNotFoundException(shortName);
        }
        return var;
    }

    /**
     * Gets the variable.
     * 
     * @param fullName - full name of variable.
     * 
     * @return the variable with the specified (full) name.
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     */
    public Variable getVariable(String fullName) throws NetCdfVariableNotFoundException {
        return NetCdfReader.getVariable(fullName, getNetcdfDataset());
    }

    /**
     * Gets the variable.
     * 
     * @param fullName the full name
     * @param ds the netcdf dataset
     * 
     * @return the variable
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     */
    public static Variable getVariable(String fullName, NetcdfDataset ds) throws NetCdfVariableNotFoundException {
        Variable var = ds.findVariable(fullName);
        if (var == null) {
            throw new NetCdfVariableNotFoundException(fullName);
        }
        return var;
    }

    /**
     * Gets the root variables.
     * 
     * @return all of the variables of the root group.
     */
    public List<Variable> getRootVariables() {
        return getNetcdfDataset().getRootGroup().getVariables();
    }

    /**
     * Gets the variables.
     * 
     * @param group from which to get variable.
     * 
     * @return all of the variables of a group.
     */
    public List<Variable> getVariables(Group group) {
        return group.getVariables();
    }

    /**
     * Gets the variables.
     * 
     * @return all of the variables of all groups.
     */
    public List<Variable> getVariables() {
        return getNetcdfDataset().getVariables();
    }

    /**
     * Gets the attributes.
     * 
     * @return global attributes (attributes of the root group).
     */
    public List<Attribute> getAttributes() {
        return getNetcdfDataset().getRootGroup().getAttributes();
    }

    /**
     * Gets the attributes.
     * 
     * @param group in which to search the attribute.
     * 
     * @return attributes from a group.
     */
    public List<Attribute> getAttributes(Group group) {
        return group.getAttributes();
    }

    /**
     * Gets an attribute of a variable.
     * 
     * @param attributeName attribute name.
     * @param varName full name of variable.
     * 
     * @return an instance of Attribute.
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws NetCdfAttributeNotFoundException if attribute is not found
     */
    public Attribute getAttribute(String varName, String attributeName) throws NetCdfVariableNotFoundException, NetCdfAttributeNotFoundException {
        Variable variable = getVariable(varName);
        return NetCdfReader.getAttribute(variable, attributeName);
    }

    /**
     * Gets an attribute of a variable.
     * 
     * @param attributeName attribute name.
     * @param variable a NetCDF variable.
     * 
     * @return an instance of Attribute.
     * 
     * @throws NetCdfAttributeNotFoundException if attribute is not found
     */
    public static Attribute getAttribute(Variable variable, String attributeName) throws NetCdfAttributeNotFoundException {
        Attribute attribute = variable.findAttributeIgnoreCase(attributeName);
        if (attribute == null) {
            throw new NetCdfAttributeNotFoundException(variable, attributeName);
        }

        return attribute;
    }

    /**
     * Gets a global attribute (attribute of the root group).
     * 
     * @param attributeName attribute name.
     * 
     * @return an instance of Attribute.
     * 
     * @throws NetCdfAttributeNotFoundException if attribute is not found
     */
    public Attribute getAttribute(String attributeName) throws NetCdfAttributeNotFoundException {
        Attribute attribute = getNetcdfDataset().getRootGroup().findAttributeIgnoreCase(attributeName);

        if (attribute == null) {
            throw new NetCdfAttributeNotFoundException(attributeName);
        }
        return attribute;
    }

    /**
     * Read all the data for this Variable and return a memory resident Array. The Array has the same element
     * type and shape as the Variable.
     * <p>
     * If the Variable is a member of an array of Structures, this returns only the variable's data in the
     * first Structure, so that the Array shape is the same as the Variable.
     * 
     * @param fullName variable, with the specified (full) name. It may possibly be nested in multiple groups
     *            and/or structures. eg "group/subgroup/name1.name2.name".
     * 
     * @return a ucar.ma2.Array with data for the variable.
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws NetCdfVariableException the net cdf variable exception
     */
    public Array getGrid(String fullName) throws NetCdfVariableException, NetCdfVariableNotFoundException {
        return getGrid(getVariable(fullName));
    }

    /**
     * Read all the data for the variable and returns a memory resident Array. The Array has the same element
     * type and shape as the Variable.
     * <p>
     * If the Variable is a member of an array of Structures, this returns only the variable's data in the
     * first Structure, so that the Array shape is the same as the Variable.
     * 
     * @param variable variable to be read.
     * 
     * @return a ucar.ma2.Array with data for the variable.
     * 
     * @throws NetCdfVariableException the net cdf variable exception
     */
    public Array getGrid(Variable variable) throws NetCdfVariableException {
        Array grid = null;
        try {
            grid = variable.read();
        } catch (Exception e) {
            LOG.error("getGrid()", e);

            throw new NetCdfVariableException(variable, "Error in getGrid ", e);
        }

        return grid;
    }

    /**
     * Reads a section of the data for a variable and return a memory resident Array. The Array has the same
     * element type as the Variable, and the requested shape. Note that this does not do rank reduction, so
     * the returned Array has the same rank as the Variable. Use Array.reduce() for rank reduction.
     * <p>
     * <code>assert(origin[ii] + shape[ii]*stride[ii] <= Variable.shape[ii]); </code>
     * <p>
     * 
     * @param origin int array specifying for each dimension of the variable the starting index of the
     *            extraction . If null, assume all zeroes.
     * @param shape int array specifying the extents in each dimension. If null, assume getShape(); This
     *            becomes the shape of the returned Array.
     * @param fullName variable, with the specified (full) name. It may possibly be nested in multiple groups
     *            and/or structures. eg "group/subgroup/name1.name2.name".
     * 
     * @return a ucar.ma2.Array with data for the variable.
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws NetCdfVariableException the net cdf variable exception
     */
    public Array getGrid(String fullName, int[] origin, int[] shape) throws NetCdfVariableException, NetCdfVariableNotFoundException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getGrid() - entering");
        }

        Variable var = null;
        Array grid = null;

        var = getVariable(fullName);

        try {
            grid = var.read(origin, shape);
        } catch (Exception e) {
            LOG.error("getGrid()", e);

            throw new NetCdfVariableException(var, "Error in getGrid", e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getGrid() - exiting");
        }
        return grid;
    }

    /**
     * Reads data section specified by a "section selector", and return a memory resident Array. Uses Fortran
     * 90 array section syntax.
     * 
     * @param sectionSpec specification string, eg "1:2,10,:,1:100:10". May optionally have (). ":, 0:200,
     *            0:100:5 " means : all the first dimension, the 200 first values of the second dimension, and
     *            the 100 first of the third diemnsion, by selecting only one value out of 5.
     * @param fullName variable, with the specified (full) name. It may possibly be nested in multiple groups
     *            and/or structures. eg "group/subgroup/name1.name2.name".
     * 
     * @return a ucar.ma2.Array with data for the variable.
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws NetCdfVariableException the net cdf variable exception
     */
    public Array getGrid(String fullName, String sectionSpec) throws NetCdfVariableException, NetCdfVariableNotFoundException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getGrid() - entering");
        }

        Variable var = null;
        Array grid = null;

        var = getVariable(fullName);

        try {
            grid = var.read(sectionSpec);
        } catch (Exception e) {
            LOG.error("getGrid()", e);

            throw new NetCdfVariableException(var, String.format("Error in getGrid - range %s", sectionSpec), e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getGrid() - exiting");
        }
        return grid;
    }

    /**
     * Retrieve the CoordinateAxis with the specified name.
     * 
     * @param fullName full name of the coordinate axis
     * 
     * @return the CoordinateAxis, or null if not found
     */
    public CoordinateAxis getCoordinateAxis(String fullName) {
        return getNetcdfDataset().findCoordinateAxis(fullName);
    }

    /**
     * Re-opens the reader.
     *
     * @param enhanceVar the enhance var
     * @return the time (in nanoseconds) taken to open the dataset
     * @throws MotuException the motu exception
     */
    public long reOpen(boolean enhanceVar) throws MotuException {
        close();
        return open(enhanceVar);
    }

    /**
     * Opens the reader, if it is closed.
     *
     * @param enhanceVar the enhance var
     * @return the time (in nanoseconds) taken to open the dataset
     * @throws MotuException the motu exception
     */
    public long open(boolean enhanceVar) throws MotuException {
        long d1 = System.nanoTime();

        if (this.isOpenWithEnhanceVar != enhanceVar) {
            this.isOpenWithEnhanceVar = enhanceVar;
            return reOpen(enhanceVar);
        }

        try {
            setNetcdfDataset(acquireDataset(locationData, enhanceVar, new CancelTask() {

                @Override
                public void setProgress(String arg0, int arg1) {
                }

                @Override
                public void setError(String arg0) {
                }

                @Override
                public boolean isCancel() {
                    return false;
                }
            }));
            controlAxes();
        } catch (Exception e) {
            throw new MotuException(
                    ErrorType.NETCDF_LOADING,
                    String.format("Error in NetCdfReader open - Unable to aquire dataset - location data:'%s'", locationData),
                    e);
        }

        if (hasGeoXYAxisWithLonLatEquivalence()) {
            NetCdfCancelTask ct = new NetCdfCancelTask();
            CoordSysBuilderYXLatLon conv = new CoordSysBuilderYXLatLon();

            conv.augmentDataset(getNetcdfDataset(), ct);

            if (conv.isAugmented()) {
                if (ct.hasError()) {
                    throw new MotuException(ErrorType.NETCDF_LOADING, ct.getError());
                }

                conv.buildCoordinateSystems(getNetcdfDataset());
            }
        }
        return System.nanoTime() - d1;
    }

    /**
     * Inits the original variables.
     * 
     * @param ds the ds
     */
    private void initOriginalVariables(NetcdfDataset ds) {
        orignalVariables.clear();
        for (Variable var : ds.getVariables()) {
            orignalVariables.put(var.getFullName(), var);
        }

    }

    /**
     * Factory method for opening a dataset through the netCDF API, and identifying its coordinate variables.
     * 
     * @param location location of file
     * @param enhanceVar if true, process scale/offset/missing
     * @param cancelTask the cancel task
     * 
     * @return NetcdfDataset object
     * 
     * @throws IOException the IO exception
     * @throws MotuException
     * 
     * @see #NetcdfDataset Coordinate Systems are always added
     */
    public NetcdfDataset acquireDataset(String location, boolean enhanceVar, ucar.nc2.util.CancelTask cancelTask) throws IOException, MotuException {
        NetcdfDataset ds;
        // if enhanceVar ==> call NetcdfDataset.acquireDataset method
        // else enhance() is not called but Coordinate Systems are added
        if (enhanceVar) {
            ds = NetcdfDataset.acquireDataset(location, cancelTask);

            try (NetcdfFile ncfile = NetcdfDataset.acquireFile(location, cancelTask)) {
                NetcdfDataset dsTmp;
                List<Variable> vList;
                if (ncfile instanceof NetcdfDataset) {
                    dsTmp = (NetcdfDataset) ncfile;
                    vList = dsTmp.getVariables();
                } else if (ncfile instanceof DODSNetcdfFile) {
                    dsTmp = new NetcdfDataset(ncfile, enhanceVar);
                    vList = ((DODSNetcdfFile) ncfile).getVariables();
                } else {
                    dsTmp = new NetcdfDataset(ncfile, enhanceVar);
                    vList = dsTmp.getVariables();
                }

                // copy missing attributes
                for (Variable v : vList) {
                    Variable vDS = ds.findVariable(v.getFullNameEscaped());
                    if (vDS != null) {
                        for (Attribute a : v.getAttributes()) {
                            if (vDS.findAttribute(a.getFullNameEscaped()) == null) {
                                vDS.addAttribute(a);
                            }
                        }
                    }
                    orignalVariables.put(vDS.getFullName(), vDS);
                }
            }
        } else {
            NetcdfFile ncfile = NetcdfDataset.acquireFile(location, cancelTask);
            if (ncfile instanceof NetcdfDataset) {
                ds = (NetcdfDataset) ncfile;
                ucar.nc2.dataset.CoordSysBuilder.factory(ds, cancelTask);
                initOriginalVariables(ds);
                ds.finish(); // recalc the global lists
            } else {
                ds = new NetcdfDataset(ncfile, false);
                ucar.nc2.dataset.CoordSysBuilder.factory(ds, cancelTask);
                initOriginalVariables(ds);
                ds.finish(); // rebuild global lists
            }
        }
        return ds;
    }

    /**
     * To nc ml.
     * 
     * @param ds the ds
     * @param file the file
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    static public void toNcML(NetcdfDataset ds, String file) throws IOException {
        OutputStream out = new FileOutputStream(file);

        NcMLWriter writer = new NcMLWriter();
        writer.writeXML(ds, out, null);

    }

    /**
     * Checks if is closed.
     * 
     * @return if the reader already closed?
     */
    // public boolean isClosed() {
    // return getNetcdfDataset() == null ? true : getNetcdfDataset().isClosed();
    // }

    /**
     * Closes the reader.
     * 
     * @throws MotuException the motu exception
     */
    public void close() throws MotuException {
        if (getNetcdfDataset() != null) {
            try {
                // if (!getNetcdfDataset().isClosed()) {
                getNetcdfDataset().close();
                setNetcdfDataset(null);
                // }
            } catch (Exception e) {
                throw new MotuException(ErrorType.NETCDF_LOADING, String.format("Enable to close NetCDF reader - location: %s", locationData), e);
            }
        }
    }

    /**
     * Reads the value of an attribute of a variable and returns its value as a double.
     * 
     * @param attributeName attribute name
     * @param varName variable (full) name
     * 
     * @return value of the attribute
     * 
     * @throws NetCdfVariableNotFoundException variable is not found
     * @throws NetCdfAttributeNotFoundException attribute is not found
     * @throws NetCdfAttributeException invalid request (see error message).
     */
    public double getDoubleValue(String varName, String attributeName)
            throws NetCdfAttributeNotFoundException, NetCdfVariableNotFoundException, NetCdfAttributeException {
        Variable variable = getVariable(varName);
        return getDoubleValue(variable, attributeName);
    }

    /**
     * Reads the value of an attribute of a variable and returns its value as a double.
     * 
     * @param attributeName attribute name
     * @param variable a NetCDF variable.
     * 
     * @return value of the attribute
     * 
     * @throws NetCdfAttributeNotFoundException attribute is not found
     * @throws NetCdfAttributeException invalid request (see error message).
     */
    public double getDoubleValue(Variable variable, String attributeName) throws NetCdfAttributeNotFoundException, NetCdfAttributeException {

        Attribute attribute = NetCdfReader.getAttribute(variable, attributeName);
        return getDoubleValue(variable, attribute);
    }

    /**
     * Reads the value of an attribute of a variable and returns its value as a double.
     * 
     * @param attribute a NetCDF attribute.
     * @param variable a NestCDF variable.
     * 
     * @return value of the attribute
     * 
     * @throws NetCdfAttributeException invalid request (see error message).
     */
    public double getDoubleValue(Variable variable, Attribute attribute) throws NetCdfAttributeException {
        Number value = null;
        value = attribute.getNumericValue();
        if (value == null) {
            throw new NetCdfAttributeException(variable, attribute, "Error in getDoubleAttribute - Unable to get numeric value from attribute");
        }
        return value.doubleValue();
    }

    /**
     * Reads the value of an attribute of a variable and returns its value as a string.
     * 
     * @param attributeName attribute name
     * @param varName variable (full) name
     * 
     * @return value of the attribute
     * 
     * @throws NetCdfVariableNotFoundException variable is not found
     * @throws NetCdfAttributeNotFoundException attribute is not found
     * @throws NetCdfAttributeException invalid request (see error message).
     */
    public String getStringValue(String varName, String attributeName)
            throws NetCdfAttributeNotFoundException, NetCdfVariableNotFoundException, NetCdfAttributeException {
        Variable variable = getVariable(varName);
        return getStringValue(variable, attributeName);
    }

    /**
     * Reads the value of an attribute of a variable and returns its value as a string.
     * 
     * @param attributeName attribute name
     * @param variable a NetCDF variable.
     * 
     * @return value of the attribute
     * 
     * @throws NetCdfAttributeNotFoundException attribute is not found
     * @throws NetCdfAttributeException invalid request (see error message).
     */
    public static String getStringValue(Variable variable, String attributeName) throws NetCdfAttributeNotFoundException, NetCdfAttributeException {

        Attribute attribute = NetCdfReader.getAttribute(variable, attributeName);
        return getStringValue(variable, attribute);
    }

    /**
     * Reads the value of an attribute of a variable and returns its value as a string.
     * 
     * @param attribute a NetCDF attribute.
     * @param variable a NetCDF variable.
     * 
     * @return value of the attribute
     * 
     * @throws NetCdfAttributeException invalid request (see error message).
     */
    public static String getStringValue(Variable variable, Attribute attribute) throws NetCdfAttributeException {
        String value = null;
        if (attribute == null) {
            return value;
        }

        if (!attribute.isString()) {
            throw new NetCdfAttributeException(
                    variable,
                    attribute,
                    String.format("Error in getStringValue - Unable to get string value from attribute - Attribute type (%s) is not STRING ",
                                  attribute.getDataType().toString()));
        }

        value = attribute.getStringValue();

        if (value == null) {
            throw new NetCdfAttributeException(variable, attribute, "Error in getStringValue - Unable to get string value from attribute");
        }
        return value;
    }

    /**
     * Reads the value of a global attribute and returns its value as a string.
     * 
     * @param attributeName attribute name
     * 
     * @return value of the attribute
     * 
     * @throws NetCdfAttributeNotFoundException attribute is not found
     * @throws NetCdfAttributeException invalid request (see error message).
     */
    public String getStringValue(String attributeName) throws NetCdfAttributeNotFoundException, NetCdfAttributeException {
        Attribute attribute = getAttribute(attributeName);
        return getStringValue(attribute);
    }

    /**
     * Reads the value of a global attribute and returns its value as a string.
     * 
     * @param attribute a NetCDF attribute.
     * 
     * @return value of the attribute
     * 
     * @throws NetCdfAttributeException invalid request (see error message).
     */
    public static String getStringValue(Attribute attribute) throws NetCdfAttributeException {
        String value = null;

        if (!attribute.isString()) {
            throw new NetCdfAttributeException(
                    attribute,
                    String.format("Error in getStringValue - Unable to get string value from attribute - Attribute type (%s) is not STRING ",
                                  attribute.getDataType().toString()));
        }

        value = attribute.getStringValue();

        if (value == null) {
            throw new NetCdfAttributeException(attribute, "Error in getStringValue - Unable to get string value from global attribute");
        }
        return value;
    }

    /**
     * Returns a java.util.Date object from a date value and an udunits string.
     * 
     * @param unitsString udunits string
     * @param value value of the date
     * 
     * @return a Date
     * 
     * @throws MotuException the motu exception
     */
    public static Date getDate(double value, String unitsString) throws MotuException {
        Date date = null;
        try {
            DateUnit dateUnit = new DateUnit(unitsString);
            date = dateUnit.makeDate(value);
        } catch (Exception e) {
            throw new MotuException(ErrorType.INVALID_DATE, "Error in getDate", e);
        }
        return date;
    }

    /**
     * Returns a double value corresponding to a Date an udunits string.
     * 
     * @param unitsString udunits string
     * @param date date to convert to
     * 
     * @return a string representation of the date
     * 
     * @throws MotuException the motu exception
     */
    public static double getDate(Date date, String unitsString) throws MotuException {
        double value = Double.MAX_VALUE;
        try {
            DateUnit dateUnit = new DateUnit(unitsString);
            value = dateUnit.makeValue(date);
        } catch (Exception e) {
            throw new MotuException(ErrorType.INVALID_DATE, "Error in getDate", e);
        }
        return value;
    }

    /**
     * Returns a standard (ISO) GMT string representation from a date value and an udunits string.
     * 
     * @param unitsString udunits string
     * @param value value of the date
     * 
     * @return a string representation of the date
     * 
     * @throws MotuException the motu exception
     */
    public static String getDateAsIsoString(double value, String unitsString) throws MotuException {
        String date = null;
        try {
            DateUnit dateUnit = new DateUnit(unitsString);
            date = dateUnit.makeStandardDateString(value);
        } catch (Exception e) {
            throw new MotuException(ErrorType.NETCDF_LOADING, "Error in getDateAsString", e);
        }
        return date;
    }

    /**
     * Returns a GMT string representation (yyyy-MM-dd HH:mm:ss) from a date value and an udunits string.
     * 
     * @param unitsString udunits string
     * @param value value of the date
     * 
     * @return a string representation of the date
     * 
     * @throws MotuException the motu exception
     */
    public static String getDateAsGMTString(double value, String unitsString) throws MotuException {
        Date date = NetCdfReader.getDate(value, unitsString);
        return FastDateFormat.getInstance(DATETIME_FORMAT, GMT_TIMEZONE).format(date);
    }

    /**
     * Returns a GMT string representation (yyyy-MM-dd HH:mm:ss) without time if 0 ((yyyy-MM-dd) from a date
     * value and an udunits string.
     * 
     * @param unitsString udunits string
     * @param value value of the date
     * 
     * @return a string representation of the date
     * 
     * @throws MotuException the motu exception
     */
    public static String getDateAsGMTNoZeroTimeString(double value, String unitsString) throws MotuException {

        Date date = NetCdfReader.getDate(value, unitsString);
        return getDateAsGMTNoZeroTimeString(date);
    }

    /**
     * Returns a GMT string representation (yyyy-MM-dd HH:mm:ss) without time if 0 (yyyy-MM-dd) from a date
     * value and an udunits string.
     * 
     * @param date Date object to convert
     * 
     * @return a string representation of the date
     */
    public static String getDateAsGMTNoZeroTimeString(Date date) {
        if (date == null) {
            return "";
        }
        GregorianCalendar calendar = new GregorianCalendar(GMT_TIMEZONE);
        calendar.setTime(date);

        int h = calendar.get(Calendar.HOUR_OF_DAY);
        int m = calendar.get(Calendar.MINUTE);
        int s = calendar.get(Calendar.SECOND);

        String format = DATETIME_FORMAT;
        if ((h == 0) && (m == 0) && (s == 0)) {
            format = DATE_FORMAT;
        }
        return FastDateFormat.getInstance(format, GMT_TIMEZONE).format(date);
    }

    /**
     * Returns a latitude string representation from a value.
     * 
     * @param value value of the latitude
     * 
     * @return a string representation of the latitude
     */
    public static String getLatAsString(double value) {
        DecimalFormat decimalFormat = new DecimalFormat(LATLON_DECIMALFORMAT, new DecimalFormatSymbols(Locale.US));
        return decimalFormat.format(value);
    }

    /**
     * Normalize the latitude to lie between +/-90.
     * 
     * @param value value of the latitude
     * 
     * @return latitude value normalized
     */
    public static double getLatNormal(double value) {
        return LatLonPointImpl.latNormal(value);
    }

    /**
     * Make a nicely formatted representation of a latitude, eg 40.34N or 12.9S.
     * 
     * @param value value of the latitude
     * 
     * @return a string representation of the latitude
     */
    public static String getStandardLatAsString(double value) {
        return LatLonPointImpl.latToString(value, LATLON_DECIMALFORMAT.length());
    }

    /**
     * Returns a normalize latitude string representation (Normalize the latitude to lie between +/-90).
     * 
     * @param value value of the latitude
     * 
     * @return a string representation of the latitude
     */
    public static String getNormalizeLatAsString(double value) {
        DecimalFormat decimalFormat = new DecimalFormat(LATLON_DECIMALFORMAT, new DecimalFormatSymbols(Locale.US));
        return decimalFormat.format(LatLonPointImpl.latNormal(value));
    }

    /**
     * Returns a longitude string representation (no normalization) from a value.
     * 
     * @param value value of the longitude
     * 
     * @return a string representation of the longitude
     */
    public static String getLonAsString(double value) {
        DecimalFormat decimalFormat = new DecimalFormat(LATLON_DECIMALFORMAT, new DecimalFormatSymbols(Locale.US));
        return decimalFormat.format(value);
    }

    /**
     * Normalize the longitude to lie between +/-180.
     * 
     * @param value value of the longitude
     * 
     * @return longitude value normalized
     */
    public static double getLonNormal(double value) {
        return LatLonPointImpl.lonNormal(value);
    }

    /**
     * Make a nicely formatted representation of a longitude, eg 120.3W or 99.99E.
     * 
     * @param value value of the longitude
     * 
     * @return a string representation of the longitude
     */
    public static String getStandardLonAsString(double value) {
        return LatLonPointImpl.lonToString(value, LATLON_DECIMALFORMAT.length());
    }

    /**
     * Returns a normalize longitude string representation (Normalize the longitude to lie between +/-180).
     * 
     * @param value value of the longitude
     * 
     * @return a string representation of the longitude
     */
    public static String getNormalizeLonAsString(double value) {
        DecimalFormat decimalFormat = new DecimalFormat(LATLON_DECIMALFORMAT, new DecimalFormatSymbols(Locale.US));
        return decimalFormat.format(LatLonPointImpl.lonNormal(value));
    }

    /**
     * Returns a normalize longitude string representation (Normalize the longitude longitude into the range
     * [0, 360]).
     * 
     * @param value value of the longitude
     * 
     * @return a string representation of the longitude
     */
    public static String getNormalizeLon360AsString(double value) {
        DecimalFormat decimalFormat = new DecimalFormat(LATLON_DECIMALFORMAT, new DecimalFormatSymbols(Locale.US));
        return decimalFormat.format(LatLonPointImpl.lonNormal360(value));
    }

    /**
     * Returns a GeoX string representation from a value.
     * 
     * @param value value of the GeoX
     * 
     * @return a string representation of the GeoX
     */
    public static String getStandardGeoXYAsString(double value) {
        DecimalFormat decimalFormat = new DecimalFormat(GEOXY_DECIMALFORMAT, new DecimalFormatSymbols(Locale.US));
        return decimalFormat.format(value);
    }

    /**
     * Returns a GeoX or GeoY string representation from a value.
     * 
     * @param unit unit of the value (udUnit)
     * @param value value of the GeoX or GeoY
     * 
     * @return a string representation of the GeoX or GeoY
     */
    public static String getStandardGeoXYAsString(double value, SimpleUnit unit) {

        StringBuffer result = new StringBuffer();
        result.append(getStandardGeoXYAsString(value));
        result.append(" ");
        result.append(unit.getUnitString());

        return result.toString();
    }

    /**
     * Returns a GeoX or GeoY string representation from a value.
     * 
     * @param unit unit of the value
     * @param value value of the GeoX or GeoY
     * 
     * @return a string representation of the GeoX or GeoY
     */
    public static String getStandardGeoXYAsString(double value, String unit) {

        StringBuffer result = new StringBuffer();
        result.append(getStandardGeoXYAsString(value));
        result.append(" ");
        result.append(unit);

        return result.toString();
    }

    /**
     * Returns a Z string representation from a value.
     * 
     * @param value value of the Z
     * 
     * @return a string representation of the Z
     */
    public static String getStandardZAsString(double value) {
        return NetCdfReader.getStandardZAsFmtString(value, Z_DECIMALFORMAT);
    }

    /**
     * Returns a Z string representation from a value.
     * 
     * @param value value of the Z
     * @param format format string of the Z
     * 
     * @return a string representation of the Z
     */
    public static String getStandardZAsFmtString(double value, String format) {
        DecimalFormat decimalFormat = new DecimalFormat(format, new DecimalFormatSymbols(Locale.US));
        if (value == 0.0) {
            return NetCdfReader.Z_ZEROVALUE;
        }
        return decimalFormat.format(value);
    }

    /**
     * Gets the standard z as string.
     * 
     * @param value the value
     * @param roundingMode the rounding mode
     * @param desiredDecimalNumberDigits the desired decimal number of digits
     * 
     * @return the standard z as fmt string
     */
    public static String getStandardZAsString(double value, RoundingMode roundingMode, int desiredDecimalNumberDigits) {

        int in = (int) (value);
        double frac = value - in;

        if (frac == 0d) {
            return NetCdfReader.getStandardZAsString(value);
        }

        DecimalFormat decimalFormat = new DecimalFormat();

        decimalFormat.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        decimalFormat.setGroupingUsed(false);
        decimalFormat.setMinimumFractionDigits(desiredDecimalNumberDigits);
        decimalFormat.setMaximumFractionDigits(desiredDecimalNumberDigits);

        decimalFormat.setRoundingMode(roundingMode);

        return decimalFormat.format(value);
    }

    /**
     * Returns a Z string representation from a value.
     * 
     * @param unit unit of the value (udUnit)
     * @param value value of the Z
     * 
     * @return a string representation of the Z
     */
    public static String getStandardZAsString(double value, SimpleUnit unit) {

        StringBuffer result = new StringBuffer();
        result.append(getStandardZAsString(value));
        if (value != 0) {
            result.append(" ");
            result.append(unit.getUnitString());
        }

        return result.toString();
    }

    /**
     * Returns a Z string representation from a value.
     * 
     * @param unit unit of the value
     * @param value value of the Z
     * 
     * @return a string representation of the Z
     */
    public static String getStandardZAsString(double value, String unit) {

        StringBuffer result = new StringBuffer();
        result.append(getStandardZAsString(value));
        if (value != 0) {
            result.append(" ");
            result.append(unit);
        }

        return result.toString();
    }

    /**
     * Gets the scale factor attribute.
     * 
     * @param variable the variable
     * 
     * @return the scale factor attribute
     */
    public static Attribute getScaleFactorAttribute(Variable variable) {
        Attribute attribute = null;
        try {
            attribute = NetCdfReader.getAttribute(variable, SCALE_FACTOR_ATTR_NAME);
        } catch (NetCdfAttributeNotFoundException e) {
            // Do nothing
        }
        return attribute;
    }

    /**
     * Gets the add offset attribute.
     * 
     * @param variable the variable
     * 
     * @return the add offset attribute
     */
    public static Attribute getAddOffsetAttribute(Variable variable) {
        Attribute attribute = null;
        try {
            attribute = NetCdfReader.getAttribute(variable, ADD_OFFSET_ATTR_NAME);
        } catch (NetCdfAttributeNotFoundException e) {
            // Do nothing
        }
        return attribute;
    }

    /**
     * Gets the scale factor attribute value.
     * 
     * @param variable the variable
     * 
     * @return the scale factor attribute value
     */
    public static Number getScaleFactorAttributeValue(Variable variable) {
        Attribute attribute = NetCdfReader.getScaleFactorAttribute(variable);
        if (attribute == null) {
            return null;
        }
        return attribute.getNumericValue();
    }

    /**
     * Gets the add offset attribute value.
     * 
     * @param variable the variable
     * 
     * @return the add offset attribute value
     */
    public static Number getAddOffsetAttributeValue(Variable variable) {
        Attribute attribute = NetCdfReader.getAddOffsetAttribute(variable);
        if (attribute == null) {
            return null;
        }
        return attribute.getNumericValue();
    }

    /**
     * Parses text from the beginning of the given string to produce a date. The method may not use the entire
     * text of the given string.
     * <p>
     * See the {@link java.text.DateFormat#parse(String, ParsePosition)} method for more information on date
     * parsing.
     * 
     * @param source A <code>String</code> whose beginning should be parsed (it tries to parse with
     *            DATETIME_FORMAT and DATE_FORMAT if previous is not successfull).
     * 
     * @return A <code>Date</code> parsed from the string.
     * 
     * @throws MotuInvalidDateException the motu invalid date exception
     */
    public static Date parseDate(String source, int setTimeTo0ForBeginOfDays1ForEndOfDayNegativeForNow) throws MotuInvalidDateException {
        Date date = null;
        int i = 0;
        while (date == null && i < DATE_FORMATS.length) {
            date = parseDate(source, DATE_FORMATS[i]);
            i++;
        }

        if (date == null) {
            throw new MotuInvalidDateException(source);
        } else {
            // this is a only a DAY format
            if (DATE_FORMAT == DATE_FORMATS[i - 1]) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                if (setTimeTo0ForBeginOfDays1ForEndOfDayNegativeForNow == 0) {
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                } else if (setTimeTo0ForBeginOfDays1ForEndOfDayNegativeForNow == 1) {
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 59);
                    cal.set(Calendar.MILLISECOND, 999);
                }
                date = cal.getTime();
            }
        }

        return date;
    }

    public static Date parseDate(String dateStr_, String dateFormat_) {
        SimpleDateFormat fmt = new SimpleDateFormat(dateFormat_);
        Date date = null;
        try {
            date = fmt.parse(dateStr_);
        } catch (Exception e) {
            // noop
        }
        return date;
    }

    /**
     * Converts and eventually normalize a longitude string representation (eg 60 E, 120.23 W, 60, -120.23)
     * Normalize +/-180.
     * 
     * @param normalize set to true to normalize longitude value
     * @param value longitude string representation
     * 
     * @return converted longitude.
     * 
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     */
    public static double unconvertLon(String value, boolean normalize) throws MotuInvalidLongitudeException {

        double origVal = Double.MAX_VALUE;
        try {
            String valueTrim = value.trim();
            if (!valueTrim.matches("[\\d*\\.*\\s*]*[eEwW]")) {
                origVal = Double.parseDouble(value);
            } else {
                String[] strSplit = valueTrim.split("[eEwW]");
                if (strSplit.length <= 0) {
                    throw new MotuInvalidLongitudeException(value);
                }
                if (strSplit.length > 1) {
                    throw new MotuInvalidLongitudeException(value);
                }
                origVal = Double.parseDouble(strSplit[0]);
                if (valueTrim.matches("[\\d*\\.*\\s*]*[wW]")) {
                    origVal = -origVal;
                }
            }
        } catch (Exception e) {
            throw new MotuInvalidLongitudeException(value, e);
        }

        if (normalize) {
            origVal = LatLonPointImpl.lonNormal(origVal);
        }
        return origVal;
    }

    /**
     * Converts and normalize a longitude string representation (eg 60 E, 120.23 W, 60, -120.23) Normalize
     * +/-180.
     * 
     * @param value longitude string representation
     * 
     * @return converted longitude.
     * 
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     */
    public static double unconvertLon(String value) throws MotuInvalidLongitudeException {

        return NetCdfReader.unconvertLon(value, true);
    }

    /**
     * Converts and normalize a latitude string representation (eg 60 N, 75.56 W, 60, -75.56) Normalize +/-90.
     * 
     * @param value latitude string representation
     * 
     * @return converted latitude.
     * 
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     */
    public static double unconvertLat(String value) throws MotuInvalidLatitudeException {

        double origVal = Double.MAX_VALUE;
        try {
            String valueTrim = value.trim();
            if (!valueTrim.matches("[\\d*\\.*\\s*]*[nNsS]")) {
                return LatLonPointImpl.latNormal(Double.parseDouble(value));
            }
            String[] strSplit = valueTrim.split("[nNsS]");
            if (strSplit.length <= 0) {
                throw new MotuInvalidLatitudeException(value);
            }
            if (strSplit.length > 1) {
                throw new MotuInvalidLatitudeException(value);
            }
            origVal = Double.parseDouble(strSplit[0]);
            if (valueTrim.matches("[\\d*\\.*\\s*]*[sS]")) {
                origVal = -origVal;
            }
        } catch (Exception e) {
            throw new MotuInvalidLatitudeException(value, e);
        }
        return LatLonPointImpl.latNormal(origVal);
    }

    /**
     * Converts a depth string representation ("Surface" is converted to 0.0).
     * 
     * @param value depth string representation
     * 
     * @return converted depth.
     * 
     * @throws MotuInvalidDepthException the motu invalid depth exception
     */
    public static double unconvertDepth(String value) throws MotuInvalidDepthException {

        double origVal = Double.MAX_VALUE;
        try {
            if (value.equalsIgnoreCase(NetCdfReader.Z_ZEROVALUE)) {
                origVal = 0.0;
            } else {
                origVal = Double.parseDouble(value);
            }
        } catch (Exception e) {
            throw new MotuInvalidDepthException(value, e);
        }
        return origVal;
    }

    /**
     * Gets the axis attribute value.
     * 
     * @param axis axis from which to get the axis attribute value
     * 
     * @return the axis attribute value
     */
    public static String getAxisAttributeValue(CoordinateAxis axis) {
        String value = "Unknown";
        AxisType axisType = axis.getAxisType();

        if (axisType == AxisType.Time) {
            value = NetCdfReader.VARIABLEATTRIBUTE_TIME_AXIS_VALUE;
        } else if ((axisType == AxisType.Height) || (axisType == AxisType.GeoZ)) {
            value = NetCdfReader.VARIABLEATTRIBUTE_Z_AXIS_VALUE;
        } else if ((axisType == AxisType.Lat) || (axisType == AxisType.GeoY)) {
            value = NetCdfReader.VARIABLEATTRIBUTE_Y_AXIS_VALUE;
        } else if ((axisType == AxisType.Lon) || (axisType == AxisType.GeoX)) {
            value = NetCdfReader.VARIABLEATTRIBUTE_X_AXIS_VALUE;
        }
        return value;
    }

    /**
     * Gets the long name attribute value.
     * 
     * @param axis axis from which to get the long name attribute value
     * 
     * @return the long name attribute value
     */
    public static String getLongNameAttributeValue(CoordinateAxis axis) {
        String value = "Unknown";
        AxisType axisType = axis.getAxisType();

        if (axisType == AxisType.Time) {
            value = NetCdfReader.VARIABLEATTRIBUTE_TIME_LONG_NAME_VALUE;
        } else if (axisType == AxisType.Height) {
            value = NetCdfReader.VARIABLEATTRIBUTE_DEPTH_LONG_NAME_VALUE;
        } else if (axisType == AxisType.GeoZ) {
            value = NetCdfReader.VARIABLEATTRIBUTE_GEOZ_LONG_NAME_VALUE;
        } else if (axisType == AxisType.GeoY) {
            value = NetCdfReader.VARIABLEATTRIBUTE_GEOY_LONG_NAME_VALUE;
        } else if (axisType == AxisType.GeoX) {
            value = NetCdfReader.VARIABLEATTRIBUTE_GEOX_LONG_NAME_VALUE;
        } else if (axisType == AxisType.Lat) {
            value = NetCdfReader.VARIABLEATTRIBUTE_LAT_LONG_NAME_VALUE;
        } else if (axisType == AxisType.Lon) {
            value = NetCdfReader.VARIABLEATTRIBUTE_LON_LONG_NAME_VALUE;
        }
        return value;
    }

    /**
     * Gets the standard name attribute value.
     * 
     * @param axis axis from which to get the standard name attribute value
     * 
     * @return the standard name attribute value
     */
    public static String getStandardNameAttributeValue(CoordinateAxis axis) {
        String value = "Unknown";
        AxisType axisType = axis.getAxisType();

        if (axisType == AxisType.Time) {
            value = NetCdfReader.VARIABLEATTRIBUTE_TIME_STANDARD_NAME_VALUE;
        } else if (axisType == AxisType.Height) {
            value = NetCdfReader.VARIABLEATTRIBUTE_DEPTH_STANDARD_NAME_VALUE;
        } else if (axisType == AxisType.GeoZ) {
            value = NetCdfReader.VARIABLEATTRIBUTE_GEOZ_STANDARD_NAME_VALUE;
        } else if (axisType == AxisType.GeoY) {
            value = NetCdfReader.VARIABLEATTRIBUTE_GEOY_STANDARD_NAME_VALUE;
        } else if (axisType == AxisType.GeoX) {
            value = NetCdfReader.VARIABLEATTRIBUTE_GEOX_STANDARD_NAME_VALUE;
        } else if (axisType == AxisType.Lat) {
            value = NetCdfReader.VARIABLEATTRIBUTE_LAT_STANDARD_NAME_VALUE;
        } else if (axisType == AxisType.Lon) {
            value = NetCdfReader.VARIABLEATTRIBUTE_LON_STANDARD_NAME_VALUE;
        }
        return value;
    }

    /**
     * Gets coordinates axis.
     * 
     * @param axisType axis type to find.
     * 
     * @return CoordinateAxis instance if found, otherwise null
     */
    public CoordinateAxis getCoordinateAxis(AxisType axisType) {
        List<CoordinateAxis> coordinateAxes = getCoordinateAxes();

        CoordinateAxis axis = null;
        for (CoordinateAxis coord : coordinateAxes) {
            if (coord.getAxisType() == axisType) {
                axis = coord;
                break;
            }
        }
        return axis;
    }

    /**
     * Gets GeoX coordinate axis type.
     * 
     * @return GeoX CoordinateAxis instance if found, otherwise null.
     */
    public CoordinateAxis getGeoXAxis() {
        return getCoordinateAxis(AxisType.GeoX);
    }

    /**
     * Gets GeoY coordinate axis type.
     * 
     * @return GeoY CoordinateAxis instance if found, otherwise null.
     */
    public CoordinateAxis getGeoYAxis() {
        return getCoordinateAxis(AxisType.GeoY);
    }

    /**
     * Gets Latitude coordinate axis type.
     * 
     * @return Lat CoordinateAxis instance if found, otherwise null.
     */
    public CoordinateAxis getGeoLatAxis() {
        return getCoordinateAxis(AxisType.Lat);
    }

    /**
     * Gets Longitude coordinate axis type.
     * 
     * @return Lon CoordinateAxis instance if found, otherwise null.
     */
    public CoordinateAxis getGeoLonAxis() {
        return getCoordinateAxis(AxisType.Lon);
    }

    /**
     * Checks for geo X axis.
     * 
     * @return true if dataset has GeoX CoordinateAxis, otherwise false.
     */
    public boolean hasGeoXAxis() {
        return getGeoXAxis() != null;
    }

    /**
     * Checks for geo Y axis.
     * 
     * @return true if dataset has GeoY CoordinateAxis, otherwise false.
     */
    public boolean hasGeoYAxis() {
        return getGeoYAxis() != null;
    }

    /**
     * Checks for geo lat axis.
     * 
     * @return true if dataset has Latitude CoordinateAxis, otherwise false.
     */
    public boolean hasGeoLatAxis() {
        return getGeoLatAxis() != null;
    }

    /**
     * Checks for geo lon axis.
     * 
     * @return true if dataset has Longitude CoordinateAxis, otherwise false.
     */
    public boolean hasGeoLonAxis() {
        return getGeoLonAxis() != null;
    }

    /**
     * Checks for geo XY axis with lon lat equivalence.
     * 
     * @return true if axes collection contains GeoX with Longitude equivalence and GeoY with Latitude
     *         equivalenceaxes.
     * @throws MotuException
     */
    public boolean hasGeoXYAxisWithLonLatEquivalence() throws MotuException {
        return (hasGeoXAxisWithLonEquivalence() && hasGeoYAxisWithLatEquivalence());
    }

    /**
     * Checks for geo Y axis with lat equivalence.
     * 
     * @return true if GeoX axis exists among coordinate axes and if there is a longitude variable equivalence
     *         (Variable whose name is 'longitude' and with at least two dimensions X/Y).
     * @throws MotuException
     */
    public boolean hasGeoYAxisWithLatEquivalence() throws MotuException {
        CoordinateAxis coord = getGeoYAxis();
        if (coord == null) {
            return false;
        }

        Variable var = findLatitudeIgnoreCase();

        if (var == null) {
            return false;
        }

        List<Dimension> listDims = var.getDimensions();

        return hasGeoXYDimensions(listDims);
    }

    /**
     * Checks for geo X axis with lon equivalence.
     * 
     * @return true if GeoX axis exists among coordinate axes and if there is a longitude variable
     *         equivalence) (Variable whose name isa longitude name' and with at least two dimensions X/Y).
     * @throws MotuException
     */
    public boolean hasGeoXAxisWithLonEquivalence() throws MotuException {
        CoordinateAxis coord = getGeoXAxis();
        if (coord == null) {
            return false;
        }

        Variable var = findLongitudeIgnoreCase();

        if (var == null) {
            return false;
        }

        List<Dimension> listDims = var.getDimensions();

        return hasGeoXYDimensions(listDims);
    }

    /**
     * Gets the coordinate variable.
     * 
     * @param dim the dim
     * 
     * @return the coordinate variable
     * @throws MotuException
     */
    public CoordinateAxis getCoordinateVariable(Dimension dim) throws MotuException {
        return NetCdfReader.getCoordinateVariable(dim, getNetcdfDataset());
    }

    /**
     * Gets the coordinate variable.
     * 
     * @param dim the dim
     * @param ds the ds
     * 
     * @return the coordinate variable
     * @throws MotuException
     * 
     */
    public static CoordinateAxis getCoordinateVariable(Dimension dim, NetcdfDataset ds) throws MotuException {
        Variable variable = null;
        try {
            variable = NetCdfReader.getVariable(dim.getFullName(), ds);
        } catch (NetCdfVariableNotFoundException e) {
            throw new MotuException(
                    ErrorType.NETCDF_LOADING,
                    String.format("Error in getCoordinateVariable - Unable to get variable '%s'", dim.getFullName()),
                    e);
        }

        if (!variable.isCoordinateVariable() || !(variable instanceof CoordinateAxis)) {
            return null;
        }

        return (CoordinateAxis) variable;
    }

    /**
     * Checks for geo XY dimensions.
     * 
     * @param listDims list of Dimensions to search in.
     * 
     * @return true if a list of Dimension corresponds at least to a GeoX and GeoY axis coordinates variables.
     * @throws MotuException
     */
    @SuppressWarnings("unchecked")
    public boolean hasGeoXYDimensions(List<Dimension> listDims) throws MotuException {
        // TODO SMA Hack to fix issue, because this boolean has to be in a cache
        if (getNetcdfDataset() == null) {
            open(true);
        }
        if (listDims.size() < 2) {
            return false;
        }

        CoordinateAxis coordY = null;
        CoordinateAxis coordX = null;

        for (Dimension dim : listDims) {
            if ((coordY != null) && (coordX != null)) {
                break;
            }
            // List<Variable> listVar = (List<Variable>) getCoordinateVariable(dim);
            //
            // for (Variable var : listVar) {
            // if (!(var instanceof CoordinateAxis)) {
            // continue;
            // }
            // CoordinateAxis axis = (CoordinateAxis) var;
            // AxisType axisType = axis.getAxisType();
            //
            // if (AxisType.GeoY == axisType) {
            // coordY = axis;
            // break;
            // }
            // if (AxisType.GeoX == axisType) {
            // coordX = axis;
            // break;
            // }
            // }
            CoordinateAxis axis = getCoordinateVariable(dim);

            if (axis == null) {
                continue;
            }
            AxisType axisType = axis.getAxisType();

            if (AxisType.GeoY == axisType) {
                coordY = axis;
            }
            if (AxisType.GeoX == axisType) {
                coordX = axis;
            }

        }

        if ((coordY == null) || (coordX == null)) {
            return false;
        }
        return true;
    }

    /**
     * Finds Variable corresponding to a longitude name .
     * 
     * @param listVars list of Variable to search in.
     * 
     * @return Variable instance if found, otherwise null
     */

    public static Variable findLongitudeIgnoreCase(List<Variable> listVars) {

        if (listVars == null) {
            return null;
        }

        Variable varFound = null;
        for (String name : NetCdfReader.LONGITUDE_NAMES) {
            for (Variable var : listVars) {
                if (var.getName().equals(name)) {
                    varFound = var;
                    break;
                }
                String stdNameValue = null;
                try {
                    stdNameValue = NetCdfReader.getStringValue(var, VARIABLEATTRIBUTE_STANDARD_NAME);
                } catch (Exception e) {
                    // Do nothing
                }
                if (!StringUtils.isNullOrEmpty(stdNameValue)) {
                    if (stdNameValue.equals(name)) {
                        varFound = var;
                        break;
                    }

                }
            }
            if (varFound != null) {
                break;
            }

        }
        return varFound;
    }

    /**
     * Finds Variable corresponding to a longitude name in the dataset.
     * 
     * @return Variable instance if found, otherwise null
     */

    public Variable findLongitudeIgnoreCase() {
        return findLongitudeIgnoreCase(getRootVariables());
    }

    /**
     * Finds Variable corresponding to a latitude name .
     * 
     * @param listVars list of Variable to search in.
     * 
     * @return Variable instance if found, otherwise null
     */

    public static Variable findLatitudeIgnoreCase(List<Variable> listVars) {
        if (listVars == null) {
            return null;
        }

        Variable varFound = null;
        for (String name : NetCdfReader.LATITUDE_NAMES) {
            for (Variable var : listVars) {
                if (var.getName().equals(name)) {
                    varFound = var;
                    break;
                }
                String stdNameValue = null;
                try {
                    stdNameValue = NetCdfReader.getStringValue(var, VARIABLEATTRIBUTE_STANDARD_NAME);
                } catch (Exception e) {
                    // Do nothing
                }
                if (!StringUtils.isNullOrEmpty(stdNameValue)) {
                    if (stdNameValue.equals(name)) {
                        varFound = var;
                        break;
                    }

                }
            }
            if (varFound != null) {
                break;
            }
        }
        return varFound;
    }

    /**
     * Finds Variable corresponding to a latitude name in the dataset.
     * 
     * @return Variable instance if found, otherwise null
     */

    public Variable findLatitudeIgnoreCase() {
        return findLatitudeIgnoreCase(getRootVariables());
    }

    /**
     * Gets coordinate variables corresponding to each dimension of a variable.
     * 
     * @param var from which we search the coordinate variables for its dimension. If the variable is a
     *            coordinate axis, its return a empty list.
     * 
     * @return the list of the coordinate variables.
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException
     */
    public List<Variable> getCoordinateVariables(Variable var) throws MotuNotImplementedException, MotuException {
        return NetCdfReader.getCoordinateVariables(var, getNetcdfDataset());
    }

    /**
     * Gets the coordinate variables.
     * 
     * @param var the var
     * @param ds the net cdf dataset
     * 
     * @return the coordinate variables
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the net cdf variable not found exception
     */
    public static List<Variable> getCoordinateVariables(Variable var, NetcdfDataset ds) throws MotuNotImplementedException, MotuException {

        List<Variable> listCoordVars = new ArrayList<Variable>();

        if (var instanceof CoordinateAxis) {
            return listCoordVars;
        }

        List<Dimension> listDims = var.getDimensions();
        for (Dimension dim : listDims) {
            Variable dimCoordVars = NetCdfReader.getCoordinateVariable(dim, ds);
            listCoordVars.add(dimCoordVars);
        }

        return listCoordVars;
    }

    /**
     * Do we have the same Variable in two lists.
     * 
     * @param list2 List to compare with list1
     * @param list1 List to compare with list2
     * 
     * @return true if all in list1 are in list2 and all in list2 are in list1.
     */
    public static boolean containsAll(List<CoordinateAxis> list1, List<Variable> list2) {

        if (list1.size() != list2.size()) {
            return false;
        }

        for (Variable v1 : list1) {
            boolean gotIt = false;
            for (Variable v2 : list2) {
                if (v1.getName().equals(v2.getName())) {
                    gotIt = true;
                }
            }
            if (!gotIt) {
                return false;
            }
        }
        return true;
    }

    /**
     * Initializes all array elements vith a the value cooresponding to the FillValue attribute of a variable.
     * If the FillValue attribute doesn't exist for the variable, fillvalue is set to the max value of the
     * data type (ie max value of a double, of a float ....) and attibute FillValue is added to the variable.
     * 
     * @param var variable corresponding to the data (type, FillValue attribute)
     * @param data array to initialize
     * 
     * @return the fill vlue for the array.
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    public static double initializeMissingData(Variable var, Array data) throws MotuNotImplementedException {

        double fillValue = Double.MAX_VALUE;
        DataType dataType = var.getDataType();

        Attribute attribute = null;
        try {
            attribute = NetCdfReader.getAttribute(var, NetCdfReader.VARIABLEATTRIBUTE_FILEVALUE);
        } catch (NetCdfAttributeNotFoundException e) {
            // Do nothing
        }

        if (dataType.equals(DataType.DOUBLE)) {
            double fillValueDouble = Double.MAX_VALUE;
            if (attribute == null) {
                attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_FILEVALUE, fillValueDouble);
                var.addAttribute(attribute);
            } else {
                fillValueDouble = attribute.getNumericValue().doubleValue();
            }
            fillValue = fillValueDouble;
            NetCdfReader.initializeMissingData(data, fillValueDouble);

        } else if (dataType.equals(DataType.FLOAT)) {
            float fillValueFloat = Float.MAX_VALUE;
            if (attribute == null) {
                attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_FILEVALUE, fillValueFloat);
                var.addAttribute(attribute);
            } else {
                fillValueFloat = attribute.getNumericValue().floatValue();
            }
            fillValue = fillValueFloat;
            NetCdfReader.initializeMissingData(data, fillValueFloat);
        } else {
            throw new MotuNotImplementedException(
                    String.format("initialization with data type %s is not implemented (NetCdrfReader - initializeMissingData", dataType.toString()));
        }
        return fillValue;
    }

    /**
     * Initializes all array elements vith a value.
     * 
     * @param data array to initialize
     * @param fillValue value to set
     */
    public static void initializeMissingData(Array data, double fillValue) {

        IndexIterator iter = data.getIndexIterator();
        while (iter.hasNext()) {
            iter.setDoubleNext(fillValue);
        }

    }

    /**
     * Initializes all array elements vith a value.
     * 
     * @param data array to initialize
     * @param fillValue value to set
     */
    public static void initializeMissingData(Array data, float fillValue) {

        IndexIterator iter = data.getIndexIterator();
        while (iter.hasNext()) {
            iter.setFloatNext(fillValue);
        }

    }

    /**
     * Gets the netcdf variable names according to a standard name. The returned list contains at least one
     * element. It may contain several elements (some standard can correspond to one or more netcdf variable
     * names in the dataset. If no netcdf variable name is found, the returned list contains the standardName
     * parameter (considering it's a netcdf varaible name).
     * 
     * @param standardName the standard name
     * 
     * @return the list of the netcdf variable names. (one or more names)
     * 
     * @throws NetCdfAttributeException the neCdf attribute exception
     */
    public List<String> getNetcdfVarNameByStandardName(String standardName) throws NetCdfAttributeException {
        List<String> listVarName = new ArrayList<>();
        List<Variable> listVariable = getVariables();
        String attrValue;
        for (Variable variable : listVariable) {
            try {
                Attribute attribute = NetCdfReader.getAttribute(variable, VARIABLEATTRIBUTE_STANDARD_NAME);
                attrValue = NetCdfReader.getStringValue(attribute);
                if (standardName.equalsIgnoreCase(attrValue)) {
                    listVarName.add(variable.getFullName());
                }
            } catch (NetCdfAttributeNotFoundException e) {
                // Do Nothing
            }
        }

        // standard name not found, search in standard name equivalence XML file.
        if (listVarName.isEmpty()) {
            listVarName = getStandardNameEquivalence(standardName);
        }
        // standard name not found, we consider standardName parameter a netcdf variable name.
        if (listVarName.isEmpty()) {
            listVarName.add(standardName);
        }

        return listVarName;
    }

    /**
     * Gets the standard name equivalence.
     * 
     * @param standardName the standard name
     * 
     * @return the standard name equivalence
     */
    public static List<String> getStandardNameEquivalence(String standardName) {
        List<String> listVarName = new ArrayList<>();
        List<StandardName> listStd = BLLManager.getInstance().getConfigManager().getStandardNameList();
        if (listStd != null) {
            for (StandardName std : listStd) {
                if (standardName.equalsIgnoreCase(std.getName())) {
                    List<JAXBElement<String>> ncVars = std.getNetcdfName();
                    for (JAXBElement<String> ncVar : ncVars) {
                        listVarName.add(ncVar.getValue());
                    }
                }
            }
        }

        return listVarName;
    }

    /**
     * Get the minimum and the maximum data value of the previously read Array, skipping missing values as
     * defined by isMissingData(double val).
     * 
     * @param a Array to get min/max values
     * @return both min and max value.
     */
    // public static MAMath.MinMax getMinMaxSkipMissingData( Array a, VariableDS vs) {
    // if (!vs.hasMissing()) {
    // return MAMath.getMinMax( a);
    // }
    //
    // IndexIterator iter = a.getIndexIterator();
    // double max = -Double.MAX_VALUE;
    // double min = Double.MAX_VALUE;
    // while (iter.hasNext()) {
    // double val = iter.getDoubleNext();
    // if (vs.isMissing(val)) {
    // continue;
    // }
    // if (val > max) {
    // max = val;
    // }
    // if (val < min) {
    // min = val;
    // }
    //
    // }
    // return new MAMath.MinMax(min, max);
    // }
    // public static void test(Map<int[], int[]> map, int iStart, int blockSize, int iDim, int[] origin, int[]
    // shape, int[] varShape) {
    // if (iDim >= varShape.length) {
    // // map.put(origin, shape);
    // int[] originNew = new int[varShape.length];
    // int[] shapeNew = new int[varShape.length];
    // int iStartNew = iStart + blockSize;
    // int iDimNew = 0;
    // test(map, iStartNew, blockSize, iDimNew, originNew, shapeNew, varShape);
    // return;
    // }
    // if (iStart >= varShape[iDim]) {
    // return;
    // }
    //
    // test(map, iStart, blockSize, iDim + 1, origin, shape, varShape);
    //
    // origin[iDim] = iStart;
    // shape[iDim] = Math.min(blockSize, (varShape[iDim] - iStart));
    //
    // }
    // public String getDateAsString(Attribute attribute) {
    //
    // SimpleDateFormat dateFormat = new SimpleDateFormat();
    // String stringDate;
    //
    // Date d = null;
    //
    // if (attribute.isString()) {
    // stringDate = attribute.getStringValue();
    // } else if (attribute.isArray()) {
    // stringDate = attribute.getValues().toString();
    // }
    // try {
    // d = dateFormat.parse(stringDate);
    // } catch (ParseException e) {
    // noop
    // }
    //
    // //dateFormat.applyPattern("yyyyMMdd_HHmmss");
    // return dateFormat.format(d);
    // }

}
// CSON: MultipleStringLiterals
