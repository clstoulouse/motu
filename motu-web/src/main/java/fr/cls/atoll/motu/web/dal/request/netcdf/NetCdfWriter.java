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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuExceedingCapacityException;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfAttributeNotFoundException;
import fr.cls.atoll.motu.web.common.format.OutputFormat;
import fr.cls.atoll.motu.web.common.utils.ListUtils;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.common.utils.UnitUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayLong;
import ucar.ma2.ArrayShort;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.ma2.Range;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.NetcdfFileWriter.Version;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants._Coordinate;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridDataset;
import ucar.unidata.geoloc.LatLonPointImpl;

/**
 * Copy a metadata and data to a Netcdf-3 local file. All metadata and data is copied into the
 * NetcdfFileWritable.
 * 
 * These class is very similar to ucar.nc2.FileWriter. Because of private attributes of ucar.nc2.FileWriter,
 * these class is not an extend of FileWriter, and some method have copied from FileWriter.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class NetCdfWriter {

    /** Logger for this class. */
    private static final Logger LOG = LogManager.getLogger();

    // ////////////////////////////////////////////////////////////////////////////////////
    /** The Constant NETCDF_FILE_EXTENSION_NC. */
    public final static String NETCDF_FILE_EXTENSION_FINAL = ".nc";

    /** The Constant NETCDF_FILE_EXTENSION_EXTRACT. */
    public final static String NETCDF_FILE_EXTENSION_EXTRACT = ".extract";
    /**
     * NetCDF file.
     */
    protected NetcdfFileWriter ncfileWriter;
    /**
     * NetCDF file name.
     */
    protected String ncfilePath;

    /**
     * Dimensions Map <dimensionName, Dimension>
     * 
     */
    protected Map<String, Dimension> dimensionMap;

    /**
     * Min/Max variable's value Map .
     */
    protected Map<String, MAMath.MinMax> minMaxHash;

    /**
     * Map to store output origin from which one's writes data.
     */
    protected Map<String, int[]> originOutOffsetHash;
    /**
     * Map to store longitude center for normalization.
     */
    protected double longitudeCenter = 0.0;

    /** The reading time in nanoseconds (ns). */
    private long readingTime = 0L;

    /** The writing time in nanoseconds (ns). */
    private long writingTime = 0L;

    /**
     * amount of data in Megabytes to be written.
     * 
     */
    private double amountDataSize;

    private Version netcdfFileVersion;

    /**
     * List of Variable Map.
     */
    protected Map<String, List<Variable>> variablesMap;

    /**
     * Constructeur.
     */
    public NetCdfWriter() {
        variablesMap = new HashMap<>();
        originOutOffsetHash = new HashMap<>();
        minMaxHash = new HashMap<>();
        dimensionMap = new HashMap<>();
    }

    public NetcdfFileWriter getNcfileWriter() {
        return this.ncfileWriter;
    }

    /**
     * For writing parts of a NetcdfFile to a new Netcdf-3 local file. To copy all the contents, the static
     * method FileWriter.writeToFile() is preferred. These are mostly convenience methods on top of
     * NetcdfFileWriteable.
     *
     * @param ncFilePath_ file name to write to.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public NetCdfWriter(String ncFilePath_, OutputFormat outputFormat_) throws IOException {
        this();
        netcdfFileVersion = getVersionFromOutputFormat(outputFormat_);
        ncfilePath = ncFilePath_;
        ncfileWriter = NetcdfFileWriter.createNew(netcdfFileVersion, this.ncfilePath);
    }

    public static Version getVersionFromOutputFormat(OutputFormat outputFormat_) {
        return outputFormat_.name() != null && outputFormat_.name().contains("4") ? Version.netcdf4 : Version.netcdf3;
    }

    /**
     * Getter of the property <tt>variables</tt>.
     * 
     * @return Returns the variablesMap.
     */
    public Map<String, List<Variable>> getVariables() {
        return this.variablesMap;
    }

    /**
     * Setter of the property <tt>variables</tt>.
     * 
     * @param value the variablesMap to set.
     */
    public void setVariables(Map<String, List<Variable>> value) {
        this.variablesMap = value;
    }

    /**
     * Associates the specified value with the specified key in this map (optional operation).
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or
     * @see java.util.Map#put(Object,Object)
     */
    public List<Variable> putVariables(String key, Variable value) {
        List<Variable> listVar = getVariables().get(key);
        if (listVar != null) {
            listVar.add(value);
        } else {
            listVar = new ArrayList<>();
            listVar.add(value);
            getVariables().put(key, listVar);
        }
        return listVar;
    }

    /**
     * Removes the mapping for this key from this map if it is present (optional operation).
     *
     * @param key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or if there was no mapping for key.
     * @see java.util.Map#remove(Object)
     */
    public List<Variable> removeVariables(String key) {
        return getVariables().remove(key);
    }

    /**
     * Removes all mappings from this map (optional operation).
     * 
     * @see java.util.Map#clear()
     */
    public void clearVariables() {
        getVariables().clear();
    }

    /**
     * Add a Dimension to the file.
     * 
     * @param dim copy this dimension
     */
    public void putDimension(Dimension dim) {
        if (!dimensionMap.containsKey(dim.getFullName())) {
            int length = -1;
            if (!dim.isUnlimited()) {
                length = dim.getLength();
            }
            Dimension newDim = getNcfileWriter().addDimension(null,
                                                              dim.getFullName(),
                                                              length,
                                                              dim.isShared(),
                                                              dim.isUnlimited(),
                                                              dim.isVariableLength());
            dimensionMap.put(newDim.getFullName(), newDim);
        }
    }

    /**
     * Write a global attribute to the file.
     * 
     * @param att take attribute name, value, from here
     */
    public void writeGlobalAttribute(Attribute att) {
        getNcfileWriter().addGroupAttribute(null, att);
    }

    /**
     * Write a Variable attribute to the file.
     * 
     * @param varName name of variable to attach attribute to
     * @param att take attribute name, value, from here
     */
    public void writeAttribute(Variable var, Attribute att) {
        long d1 = System.nanoTime();
        if (!getNcfileWriter().addVariableAttribute(var, att)) {
            LOG.warn("Unable to write attribute" + att.getFullName() + ", in variable:" + var.getFullNameEscaped() + " [" + ncfilePath + "]");
        }
        long d2 = System.nanoTime();
        this.writingTime += (d2 - d1);
    }

    private List<Dimension> getDimentions(Variable var) throws MotuException {
        List<Dimension> dims = new ArrayList<>();
        List<Dimension> dimvList = var.getDimensions();
        for (Dimension dim : dimvList) {
            Dimension dimToWrite = dimensionMap.get(dim.getFullName());
            if (dimToWrite == null) {
                throw new MotuException(
                        ErrorType.NETCDF_VARIABLE,
                        String.format("Error in NetCdfWriter writeVariable - Variable %s - Dimension %s must be added first",
                                      var.getFullName(),
                                      dim.getFullName()));

            }
            dims.add(dimToWrite);
        }
        return dims;
    }

    private Variable writeNetCdfVariable(Variable var) throws MotuException {
        List<Dimension> dims = getDimentions(var);
        long d1 = System.nanoTime();
        Variable newVar;
        if (!"String".equalsIgnoreCase(var.getDataType().name())) {
            newVar = getNcfileWriter().addVariable(null, var.getShortName(), var.getDataType(), dims);
        } else {
            newVar = getNcfileWriter().addStringVariable(null, var, dims);
        }
        long d2 = System.nanoTime();
        this.writingTime += (d2 - d1);
        return newVar;
    }

    private void writeAttributes(Variable var, String[] varAttrToRemove, Variable newVar) {
        boolean removeAttr = false;
        List<Attribute> attributeList = var.getAttributes();
        for (Attribute attribute : attributeList) {
            removeAttr = false;
            if (varAttrToRemove != null) {
                for (String attrToRemove : varAttrToRemove) {
                    if (attrToRemove.equalsIgnoreCase(attribute.getFullName())) {
                        removeAttr = true;
                        break;
                    }
                }
            }
            if (!removeAttr) {
                writeAttribute(newVar, attribute);
            }
        }
    }

    /**
     * Add a Variable to the file. The data is also copied when finish() is called.
     *
     * @param var copy this Variable (not the data)
     * @param varAttrToRemove variable attribute to remove
     * @throws MotuException the motu exception
     */
    public void writeVariable(Variable var, String[] varAttrToRemove) throws MotuException {
        Variable newVar = writeNetCdfVariable(var);
        writeAttributes(var, varAttrToRemove, newVar);
    }

    /**
     * Adds and/or sets stantdard axis attribute. Attribures are : valid_min, valid_max, axis, long_name,
     * standard_name.
     * 
     * @param axis axis
     * @param originalVariables the original variables
     */
    private void processAxisAttributes(CoordinateAxis axis, Map<String, Variable> originalVariables) {
        if (axis != null) {
            if (originalVariables != null) {
                Variable orginalVar = originalVariables.get(axis.getFullName());
                NetCdfWriter.copyAttributes(orginalVar, axis, true);
            }

            AxisType axisType = axis.getAxisType();
            if (axisType != null) {
                MAMath.MinMax minMax = createAxisMinMax(axis);
                axis.addAttribute(createAxisDoubleAttribute(axis, NetCdfReader.VARIABLEATTRIBUTE_VALID_MIN, minMax.min));
                axis.addAttribute(createAxisDoubleAttribute(axis, NetCdfReader.VARIABLEATTRIBUTE_VALID_MAX, minMax.max));

                checkAxisAttribute(axis, NetCdfReader.VARIABLEATTRIBUTE_AXIS, NetCdfReader.getAxisAttributeValue(axis));
                checkAxisAttribute(axis, NetCdfReader.VARIABLEATTRIBUTE_LONG_NAME, NetCdfReader.getLongNameAttributeValue(axis));
                checkAxisAttribute(axis, NetCdfReader.VARIABLEATTRIBUTE_STANDARD_NAME, NetCdfReader.getStandardNameAttributeValue(axis));
            }
        }
    }

    private MAMath.MinMax createAxisMinMax(CoordinateAxis axis) {
        MAMath.MinMax minMax = minMaxHash.get(axis.getFullName());
        if (minMax == null) {
            minMax = NetCdfWriter.getMinMaxSkipMissingData(axis, null, this);
        }

        // Don't apply Scale factor and offset on variable attributes
        // attributes values have to be in accordance with the stored data.
        // NetCdfWriter.applyScaleFactorAndOffset(minMax, axis);

        // if axis is longitude ==> Normalize longitude if min value > max value
        if (axis.getAxisType() == AxisType.Lon) {
            if (minMax.min > minMax.max) {
                // Apply scale factor and offset before normalization
                NetCdfWriter.applyScaleFactorAndOffset(minMax, axis);
                // Normalize
                double center = minMax.min + 180.0;
                minMax.max = LatLonPointImpl.lonNormal(minMax.max, center);
                // Undo scale factor and offset to get native value
                NetCdfWriter.undoScaleFactorAndOffset(minMax, axis);
            }
        }
        return minMax;
    }

    private Attribute createAxisDoubleAttribute(CoordinateAxis axis, String attributeName, Double attributeValue) {
        return NetCdfWriter.createAttribute(attributeName, axis, new Double(attributeValue));
    }

    private Attribute checkAxisAttribute(CoordinateAxis axis, String attributeName, String defaultAttributeValue) {
        Attribute attribute = null;
        try {
            attribute = NetCdfReader.getAttribute(axis, attributeName);
        } catch (NetCdfAttributeNotFoundException e) {
            attribute = new Attribute(attributeName, defaultAttributeValue);
            axis.addAttribute(attribute);
        }
        return attribute;
    }

    /**
     * Creates the attribute.
     * 
     * @param name the name
     * @param var the var
     * @param value the value
     * 
     * @return the attribute
     */
    public static Attribute createAttribute(String name, Variable var, Number value) {
        return NetCdfWriter.createAttribute(name, var.getDataType(), value);
    }

    /**
     * Creates the attribute.
     * 
     * @param name the name
     * @param dataType the data type
     * @param value the value
     * 
     * @return the attribute
     */
    public static Attribute createAttribute(String name, DataType dataType, Number value) {
        return NetCdfWriter.createAttribute(name, dataType.getPrimitiveClassType(), value);
    }

    /**
     * Creates the attribute.
     * 
     * @param name the name
     * @param classType the class type
     * @param value the value
     * 
     * @return the attribute
     */
    public static Attribute createAttribute(String name, Class<?> classType, Number value) {
        int[] shape = new int[1];
        shape[0] = 1;
        Array vala = Array.factory(classType, shape);
        Index ima = vala.getIndex();

        if ((classType == double.class) || (classType == Double.class)) {
            vala.setDouble(ima, value.doubleValue());
        } else if ((classType == float.class) || (classType == Float.class)) {
            vala.setFloat(ima, value.floatValue());
        } else if ((classType == long.class) || (classType == Long.class)) {
            vala.setLong(ima, value.longValue());
        } else if ((classType == int.class) || (classType == Integer.class)) {
            vala.setInt(ima, value.intValue());
        } else if ((classType == short.class) || (classType == Short.class)) {
            vala.setShort(ima, value.shortValue());
        } else if ((classType == byte.class) || (classType == Byte.class)) {
            vala.setByte(ima, value.byteValue());
        }

        return new Attribute(name, vala);
    }

    /**
     * Cast value.
     * 
     * @param variable the variable
     * @param value the value
     * 
     * @return the number
     */
    public static Number castValue(Variable variable, Number value) {
        return NetCdfWriter.castValue(variable.getDataType(), value);
    }

    /**
     * Cast value.
     * 
     * @param dataType the data type
     * @param value the value
     * 
     * @return the number
     */
    public static Number castValue(DataType dataType, Number value) {
        return NetCdfWriter.castValue(dataType.getClassType(), value);

    }

    /**
     * Cast value.
     * 
     * @param classType the class type
     * @param value the value
     * 
     * @return the number
     */
    public static Number castValue(Class<?> classType, Number value) {
        Number valueReturned = null;
        if ((classType == double.class) || (classType == Double.class)) {
            valueReturned = Double.valueOf(value.doubleValue());
        } else if ((classType == float.class) || (classType == Float.class)) {
            valueReturned = Float.valueOf(value.floatValue());
        } else if ((classType == long.class) || (classType == Long.class)) {
            valueReturned = Long.valueOf(value.longValue());
        } else if ((classType == int.class) || (classType == Integer.class)) {
            valueReturned = Integer.valueOf(value.intValue());
        } else if ((classType == short.class) || (classType == Short.class)) {
            valueReturned = Short.valueOf(value.shortValue());
        } else if ((classType == byte.class) || (classType == Byte.class)) {
            valueReturned = Byte.valueOf(value.byteValue());
        }

        return valueReturned;
    }

    /**
     * Removes valid_min and valid_max attribute.
     * 
     * @param var variable to process
     */
    public void removeValidMinMaxVarAttributes(Variable var) {
        if (var != null) {
            try {
                var.remove(NetCdfReader.getAttribute(var, NetCdfReader.VARIABLEATTRIBUTE_VALID_MIN));
            } catch (NetCdfAttributeNotFoundException e) {
                // Nothing to do
            }
            try {
                var.remove(NetCdfReader.getAttribute(var, NetCdfReader.VARIABLEATTRIBUTE_VALID_MAX));
            } catch (NetCdfAttributeNotFoundException e) {
                // Nothing to do
            }
        }
    }

    /**
     * Compute valid min max var attributes.
     *
     * @param var variable to process
     * @param geoGridSubset GeoGrid object (a subset of geoGridOrigin)
     * @param geoGridOrigin GeoGrid object (the origine of the geoGridSubset)
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    public void computeValidMinMaxVarAttributes(Variable var, GeoGrid geoGridSubset, GeoGrid geoGridOrigin)
            throws MotuException, MotuNotImplementedException {
        MAMath.MinMax minMax = minMaxHash.get(var.getFullName());

        if (geoGridOrigin != null) {
            minMax = NetCdfWriter.getMinMaxSkipMissingData(geoGridOrigin, var, minMax, this);
        } else {
            minMax = NetCdfWriter.getMinMaxSkipMissingData(geoGridSubset, var, minMax, this);
        }
        if (minMax != null) {
            minMaxHash.put(var.getFullName(), minMax);
        }

    }

    /**
     * Compute valid min max var attributes.
     *
     * @param axis axis to process
     */
    public void computeValidMinMaxVarAttributes(CoordinateAxis axis) {
        MAMath.MinMax minMax = minMaxHash.get(axis.getFullName());
        MAMath.MinMax minMaxWork = NetCdfWriter.getMinMaxSkipMissingData(axis, minMax, this);
        if (minMaxWork != null) {
            minMaxHash.put(axis.getFullName(), minMaxWork);
        }
    }

    /**
     * Apply scale factor and offset.
     *
     * @param value the value
     * @param variable the variable
     * @return the number
     */
    public static Number applyScaleFactorAndOffset(Number value, Variable variable) {
        Number resNumber = value;
        if (variable != null) {
            Number addOffset = NetCdfReader.getAddOffsetAttributeValue(variable);
            Number scaleFactor = NetCdfReader.getScaleFactorAttributeValue(variable);
            if ((addOffset != null) && (scaleFactor != null)) {
                double offset = addOffset.doubleValue();
                double scale = scaleFactor.doubleValue();
                if (Double.compare(scale, 0.0) == 0) {
                    scale = 1.0;
                }
                resNumber = (scale * value.doubleValue()) + offset;
            }
        }
        return resNumber;
    }

    /**
     * Apply scale factor and offset.
     * 
     * @param minMax the min max
     * @param variable the variable
     */
    public static void applyScaleFactorAndOffset(MAMath.MinMax minMax, Variable variable) {
        if (variable != null) {
            Number addOffset = NetCdfReader.getAddOffsetAttributeValue(variable);
            Number scaleFactor = NetCdfReader.getScaleFactorAttributeValue(variable);
            if ((addOffset != null) && (scaleFactor != null)) {
                double offset = addOffset.doubleValue();
                double scale = scaleFactor.doubleValue();
                if (Double.compare(scale, 0.0) == 0) {
                    scale = 1.0;
                }
                minMax.min = (scale * minMax.min) + offset;
                minMax.max = (scale * minMax.max) + offset;
            }
        }
    }

    /**
     * Undo scale factor and offset.
     * 
     * @param value the value
     * @param variable the variable
     * 
     * @return the number
     */
    public static Number undoScaleFactorAndOffset(Number value, Variable variable) {
        Number resNumber = value;
        if (variable != null) {
            Number addOffset = NetCdfReader.getAddOffsetAttributeValue(variable);
            Number scaleFactor = NetCdfReader.getScaleFactorAttributeValue(variable);
            if ((addOffset != null) && (scaleFactor != null)) {
                double offset = addOffset.doubleValue();
                double scale = scaleFactor.doubleValue();
                if (Double.compare(scale, 0.0) == 0) {
                    scale = 1.0;
                }
                resNumber = castValue(variable, (value.doubleValue() - offset) / scale);
            }
        }
        return resNumber;
    }

    /**
     * Undo scale factor and offset.
     * 
     * @param minMax the min max
     * @param variable the variable
     */
    public static void undoScaleFactorAndOffset(MAMath.MinMax minMax, Variable variable) {
        if (variable != null) {
            Number addOffset = NetCdfReader.getAddOffsetAttributeValue(variable);
            Number scaleFactor = NetCdfReader.getScaleFactorAttributeValue(variable);
            if ((addOffset != null) && (scaleFactor != null)) {
                double offset = addOffset.doubleValue();
                double scale = scaleFactor.doubleValue();
                if (Double.compare(scale, 0.0) == 0) {
                    scale = 1.0;
                }
                minMax.min = (minMax.min - offset) / scale;
                minMax.max = (minMax.max - offset) / scale;
            }
        }
    }

    /**
     * Adds and/or sets valid_min and valid_max attribute.
     * 
     * @param var variable to process
     */
    public void setValidMinMaxVarAttributes(Variable var) {
        MAMath.MinMax minMax = minMaxHash.get(var.getFullName());
        if (minMax != null) {
            setValidMinMaxVarAttributes(var, minMax);
        }
    }

    /**
     * Adds and/or sets valid_min and valid_max attribute.
     * 
     * @param var variable to process
     * @param minMax min. and max values
     */
    public void setValidMinMaxVarAttributes(Variable var, MAMath.MinMax minMax) {
        setValidMinMaxVarAttributes(var, minMax.min, minMax.max);
    }

    /**
     * Adds and/or sets valid_min and valid_max attribute.
     * 
     * @param var variable to process
     * @param min min. value
     * @param max max. value
     */
    public void setValidMinMaxVarAttributes(Variable var, double min, double max) {
        if (var != null) {
            var.addAttribute(new Attribute(NetCdfReader.VARIABLEATTRIBUTE_VALID_MIN, min));
            var.addAttribute(new Attribute(NetCdfReader.VARIABLEATTRIBUTE_VALID_MAX, max));
        }
    }

    /**
     * Compute amount data size.
     *
     * @param listGeoGridSubset the list geo grid subset
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     */
    public void computeAmountDataSize(List<GeoGrid> listGeoGridSubset)
            throws MotuException, MotuNotImplementedException, MotuExceedingCapacityException {
        if (listGeoGridSubset == null) {
            throw new MotuException(ErrorType.INVALID_LAT_LON_RANGE, "Error in computeAmountDataSize - list of geogrids is null");
        }
        if (listGeoGridSubset.size() <= 0) {
            throw new MotuException(ErrorType.INVALID_LAT_LON_RANGE, "Error in computeAmountDataSize - list of geoGrids is empty");
        }

        for (GeoGrid geoGridSubset : listGeoGridSubset) {
            if (geoGridSubset == null) {
                throw new MotuException(ErrorType.INVALID_LAT_LON_RANGE, "Error in computeAmountDataSize - geoGrid is null");
            }

            Variable v = geoGridSubset.getVariable();
            setAmountDataSize(getAmountDataSize() + NetCdfWriter.countVarSize(v));
        }
    }

    /**
     * Adds a list of variables.
     *
     * @param listVars list of variables
     * @throws MotuException the motu exception
     */
    public void putVariables(Collection<Variable> listVars) throws MotuException {

        for (Variable var : listVars) {
            putVariable(var);
        }
    }

    /**
     * Adds a variable.
     * 
     * @param var the var
     * 
     * @throws MotuException the motu exception
     */
    public void putVariable(Variable var) throws MotuException {
        List<Variable> vPreviouslyAdded = getVariables().get(var.getFullName());
        if (vPreviouslyAdded == null) {
            putVariables(var.getFullName(), var);
        }
    }

    /**
     * Adds a list of variables contained in a GeoGrid object to the file. The data will be copied when
     * finish() is called. ----------------------------- WARNING :
     * 
     * section method of Variable create a new instance of the class VariableDS from the original variable,
     * but some informations are lost (as Fillvalue). And Subset of GeoGrid is used section method.
     * 
     * Example : ... VariableDS v_section = (VariableDS) v.section(rangesList);
     * 
     * v is an instance of class VariableDS and the attribute fillValue of attribute smProxy is set and
     * hasFillValue is set to true. After calling v.section, the attribute fillValue of attribute smProxy of
     * v_section is not set and hasFillValue is set to false.
     * 
     * So, when you work with v_section variable and you called hasFillValue method, it returns false, while
     * with the original variable v, hasFillValue method returns true.
     * 
     * That's the reason this method accept the Original geogrid (geoGridOrigin). Can be null.
     * -----------------------------
     *
     * @param geoGridSubset GeoGrid object (a subset of geoGridOrigin)
     * @param geoGridOrigin GeoGrid object (the origine of the geoGridSubset)
     * @param gds the grid from which geogrid is derived
     * @param originalVariables the original variables
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     */
    public void putVariables(GeoGrid geoGridSubset, GeoGrid geoGridOrigin, GridDataset gds, Map<String, Variable> originalVariables)
            throws MotuException, MotuNotImplementedException, MotuExceedingCapacityException {
        if (geoGridSubset == null) {
            throw new MotuException(ErrorType.INVALID_LAT_LON_RANGE, "Error in writeVariables - geoGrid is null");
        }
        putDimensions(geoGridSubset);
        Variable v = geoGridSubset.getVariable();
        setAmountDataSize(getAmountDataSize() + NetCdfWriter.countVarSize(v));
        checkAmountDataSizeThreshold();

        putVariables(v.getFullName(), v);
        MAMath.MinMax minMax = minMaxHash.get(v.getFullName());

        if (geoGridOrigin != null) {
            minMax = NetCdfWriter.getMinMaxSkipMissingData(geoGridOrigin, v, minMax, this);
        } else {
            minMax = NetCdfWriter.getMinMaxSkipMissingData(geoGridSubset, v, minMax, this);
        }

        if (minMax != null) {
            setValidMinMaxVarAttributes(v, minMax);
            minMaxHash.put(v.getFullName(), minMax);
        } else {
            removeValidMinMaxVarAttributes(v);
        }

        CoordinateAxis axis = null;

        // NetCDF 2.2.16
        // ArrayList axes = geoGrid.getCoordinateSystem().getCoordinateAxes();
        // NetCDF 2.2.18
        List<CoordinateAxis> axes = geoGridSubset.getCoordinateSystem().getCoordinateAxes();

        for (int i = 0; i < axes.size(); i++) {
            axis = axes.get(i);
            List<Variable> vPreviouslyAdded = getVariables().get(axis.getFullName());
            if (vPreviouslyAdded == null) {
                putVariables(axis.getFullName(), axis);
                processAxisAttributes(axis, originalVariables);
            }
        }

        initDependentVariablesInVariableList(geoGridSubset, gds);
    }

    /**
     * Adds a list of variables contained in a GeoGrid object to the file. The data will be copied when
     * finish() is called. ----------------------------- WARNING :
     * 
     * section method of Variable create a new instance of the class VariableDS from the original variable,
     * but some informations are lost (as Fillvalue). And Subset of GeoGrid is used section method.
     * 
     * Example : ... VariableDS v_section = (VariableDS) v.section(rangesList);
     * 
     * v is an instance of class VariableDS and the attribute fillValue of attribute smProxy is set and
     * hasFillValue is set to true. After calling v.section, the attribute fillValue of attribute smProxy of
     * v_section is not set and hasFillValue is set to false.
     * 
     * So, when you work with v_section variable and you called hasFillValue method, it returns false, while
     * with the original variable v, hasFillValue method returns true.
     * 
     * That's the reason this method accept the Original geogrid (geoGridOrigin). Can be null.
     * -----------------------------
     *
     * @param listGeoGridSubset list of GeoGrid objects (subsets of geoGridOrigin)
     * @param geoGridOrigin GeoGrid object (the origin of the geoGridSubsets)
     * @param gds the grid from which geogrid is derived
     * @param originalVariables the original variables
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     */
    public void prepareVariables(List<GeoGrid> listGeoGridSubset, GeoGrid geoGridOrigin, GridDataset gds, Map<String, Variable> originalVariables)
            throws MotuException, MotuNotImplementedException, MotuExceedingCapacityException {
        if (listGeoGridSubset == null) {
            throw new MotuException(ErrorType.INVALID_LAT_LON_RANGE, "Error in writeVariables - list of geogrids is null");
        }
        if (listGeoGridSubset.isEmpty()) {
            throw new MotuException(ErrorType.INVALID_LAT_LON_RANGE, "Error in writeVariables - list of geoGrids is empty");
        }

        putDimensions(listGeoGridSubset);

        Map<AxisType, List<Variable>> mapAxis = new HashMap<>();

        for (GeoGrid geoGridSubset : listGeoGridSubset) {
            if (geoGridSubset == null) {
                throw new MotuException(ErrorType.INVALID_LAT_LON_RANGE, "Error in writeVariables - geoGrid is null");
            }

            Variable v = geoGridSubset.getVariable();

            setAmountDataSize(getAmountDataSize() + NetCdfWriter.countVarSize(v));
            checkAmountDataSizeThreshold();

            putVariables(v.getFullName(), v);

            // Removes valid_min and valid_max attribute from the variables :
            // because their values can't be modified after file is created.
            // the valid_min and valid_max can't be calculated after reading the data
            // of the variable, and it's too late to modify objects (variables, dimensions,
            // attributes) in the NetCdf file (see NetCdfFileWriteable class).
            // To have efficient performance, we don't want to read data variable twice :
            // once before file creation, to compute valid min an valid max value,
            // and once after file creation, to write data in the file.

            // Modif : 21/11/07 : Remove only for variables that are not a dimension.
            // Comment this 'if' block, if you activate computeValidMinMaxVarAttributes, below
            if (!(v instanceof CoordinateAxis)) {
                removeValidMinMaxVarAttributes(v);
            }

            CoordinateAxis axis = null;

            List<CoordinateAxis> axes = geoGridSubset.getCoordinateSystem().getCoordinateAxes();

            // Add axes as variable
            for (int i = 0; i < axes.size(); i++) {
                axis = axes.get(i);

                computeValidMinMaxVarAttributes(axis);

                AxisType axisType = axis.getAxisType();

                List<Variable> listAxis = mapAxis.get(axisType);
                if (listAxis == null) {
                    listAxis = new ArrayList<Variable>();
                    listAxis.add(axis);
                } else if (axisType == AxisType.Lon) {
                    listAxis.add(axis);
                }
                mapAxis.put(axisType, listAxis);
            }

            initDependentVariablesInVariableList(geoGridSubset, gds);

        }

        // process axis for the variable
        for (List<Variable> listAxis : mapAxis.values()) {
            if (listAxis.isEmpty()) {
                List<Variable> vPreviouslyListAdded = getVariables().get(listAxis.get(0).getFullName());
                if (vPreviouslyListAdded == null) {
                    getVariables().put(listAxis.get(0).getFullName(), listAxis);
                    for (Variable var : listAxis) {
                        if (var instanceof CoordinateAxis) {
                            CoordinateAxis axis = (CoordinateAxis) var;
                            processAxisAttributes(axis, originalVariables);
                            // remove valid_min/max for indeso (even // dimensions)
                            removeValidMinMaxVarAttributes(var);
                        }
                    }
                }
            }

        }
    }

    /**
     * Write variables with geo xy.
     *
     * @param listGeoGridSubset the list geo grid subset
     * @param geoGridOrigin the geo grid origin
     * @param gds the gds
     * @param originalVariables the original variables
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     */
    public void prepareVariablesWithGeoXY(List<GeoGrid> listGeoGridSubset,
                                          GeoGrid geoGridOrigin,
                                          GridDataset gds,
                                          Map<String, Variable> originalVariables)
            throws MotuException, MotuNotImplementedException, MotuExceedingCapacityException {
        if (listGeoGridSubset == null) {
            throw new MotuException(ErrorType.INVALID_LAT_LON_RANGE, "Error in writeVariablesGeoXY - list of geogrids is null");
        }
        if (listGeoGridSubset.isEmpty()) {
            throw new MotuException(ErrorType.INVALID_LAT_LON_RANGE, "Error in writeVariablesGeoXY - list of geoGrids is empty");
        }

        putDimensionsGeoXY(listGeoGridSubset);

        Map<AxisType, List<Variable>> mapAxis = new HashMap<>();

        for (GeoGrid geoGridSubset : listGeoGridSubset) {
            if (geoGridSubset == null) {
                throw new MotuException(ErrorType.INVALID_LAT_LON_RANGE, "Error in writeVariablesGeoXY - geoGrid is null");
            }

            Variable v = geoGridSubset.getVariable();

            setAmountDataSize(getAmountDataSize() + NetCdfWriter.countVarSize(v));
            checkAmountDataSizeThreshold();

            putVariables(v.getFullName(), v);
            // Removes valid_min and valid_max attribute from the variables :
            // because their values can't be modified after file is created.
            // the valid_min and valid_max can't be calculated after reading the data
            // of the variable, and it's too late to modify objects (variables, dimensions,
            // attributes) in the NetCdf file (see NetCdfFileWriteable class).
            // To have efficient performance, we don't want to read data variable twice :
            // once before file creation, to compute valid min an valid max value,
            // and once after file creation, to write data in the file.

            // Modif : 21/11/07 : Remove only for variables that are not a dimension.
            // Comment this 'if' block, if you activate computeValidMinMaxVarAttributes, below
            if (!(v instanceof CoordinateAxis)) {
                removeValidMinMaxVarAttributes(v);
            }
            CoordinateAxis axis = null;
            List<CoordinateAxis> axes = geoGridSubset.getCoordinateSystem().getCoordinateAxes();

            // Add axes as variable
            for (int i = 0; i < axes.size(); i++) {
                axis = axes.get(i);
                computeValidMinMaxVarAttributes(axis);

                AxisType axisType = axis.getAxisType();
                List<Variable> listAxis = mapAxis.get(axisType);
                if (listAxis == null) {
                    listAxis = new ArrayList<Variable>();
                    listAxis.add(axis);
                } else if (axisType == AxisType.Lon) {
                    listAxis.add(axis);
                } else if (axisType == AxisType.Lat) {
                    listAxis.add(axis);
                }
                mapAxis.put(axisType, listAxis);
            }
            initDependentVariablesInVariableList(geoGridSubset, gds);
        } // end for (GeoGrid geoGridSubset : listGeoGridSubset)

        // process axis for the variable
        for (List<Variable> listAxis : mapAxis.values()) {
            if (!listAxis.isEmpty() && !getVariables().containsKey(listAxis.get(0).getFullName())) {
                getVariables().put(listAxis.get(0).getFullName(), listAxis);
                for (Variable var : listAxis) {
                    if (var instanceof CoordinateAxis) {
                        CoordinateAxis axis = (CoordinateAxis) var;
                        processAxisAttributes(axis, originalVariables);
                    }
                }
            }
        }
        List<Variable> listVar = getVariables().get(listGeoGridSubset.get(0).getVariable().getFullName());
        for (Variable var : listVar) {
            if (!(var instanceof CoordinateAxis)) {
                setValidMinMaxVarAttributes(var);
            }
        }
    }

    /**
     * Write variables.
     *
     * @param listCoordinateAxis the list coordinate axis
     * @param mapRange the map range
     * @param originalVariables the original variables
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     */
    public void prepareVariablesInMap(List<CoordinateAxis> listCoordinateAxis, Map<String, Range> mapRange)
            throws MotuException, MotuNotImplementedException, MotuExceedingCapacityException {
        if (!ListUtils.isNullOrEmpty(listCoordinateAxis)) {
            putDimensionsAxis(listCoordinateAxis, mapRange);

            for (CoordinateAxis var : listCoordinateAxis) {
                writeVariables(var);
            }
        }
    }

    /**
     * Write variables.
     *
     * @param axis the axis
     * @param originalVariables the original variables
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     */
    public void writeVariables(CoordinateAxis axis) throws MotuException, MotuNotImplementedException, MotuExceedingCapacityException {
        if (axis == null) {
            throw new MotuException(ErrorType.INVALID_LAT_LON_RANGE, "Error in writeVariables - axis is null");
        }

        setAmountDataSize(getAmountDataSize() + NetCdfWriter.countVarSize(axis));
        checkAmountDataSizeThreshold();

        putVariables(axis.getFullName(), axis);
    }

    /**
     * Writes variables that depend of the geogrid variable.
     * 
     * @param geoGrid GeoGrid object
     * @param gds the grid from which geoGrid is derived
     * @throws MotuException
     * @throws MotuNotImplementedException
     */
    public void initDependentVariablesInVariableList(GeoGrid geoGrid, GridDataset gds) throws MotuException, MotuNotImplementedException {
        Variable v = geoGrid.getVariable();
        initDependentVariablesInVariableList(v, gds);
    }

    /**
     * Writes variables that depend of the geogrid variable.
     *
     * @param v variable which can have dependent variables
     * @param gds the grid from which geoGrid is derived
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    public void initDependentVariablesInVariableList(Variable v, GridDataset gds) throws MotuException, MotuNotImplementedException {

        try {
            // Find variable relative to Coordinate Systems attribute
            Attribute attribute = NetCdfReader.getAttribute(v, _Coordinate.Systems);
            String[] listCoordinateSystems = attribute.getStringValue().split(" ");
            for (String coordinateSystem : listCoordinateSystems) {
                String coordinateSystemTrimmed = coordinateSystem.trim();
                if (coordinateSystemTrimmed.length() > 0) {
                    Variable varCoordSystem = (Variable) gds.getDataVariable(coordinateSystemTrimmed);
                    if (varCoordSystem != null) {
                        putVariables(varCoordSystem.getFullName(), varCoordSystem);
                    } else {
                        throw new MotuException(
                                ErrorType.INVALID_LAT_LON_RANGE,
                                String.format("Error in NetCdfWriter - writeDependentVariables - variable %s representing coordinate system for %s not found",
                                              coordinateSystemTrimmed,
                                              v.getFullName()));

                    }
                }
            }
        } catch (NetCdfAttributeNotFoundException e) {
            // Nothing to do
        }

        try {
            // Find variable relative to Coordinate Systems attribute
            Attribute attribute = NetCdfReader.getAttribute(v, NetCdfReader.VARIABLEATTRIBUTE_GRID_MAPPING);
            String varProjectionName = attribute.getStringValue();
            if (!StringUtils.isNullOrEmpty(varProjectionName)) {
                Variable varProjection = (Variable) gds.getDataVariable(varProjectionName);
                if (varProjection != null) {
                    putVariables(varProjection.getFullName(), varProjection);
                }
            }
        } catch (NetCdfAttributeNotFoundException e) {
            // Nothing to do
        }

    }

    /**
     * Adds a list of dimension contained in a GeoGrid object to the file. The data will be copied when
     * finish() is called.
     *
     * @param listDims lsit of Dimension to add
     * @throws MotuException the motu exception
     */
    public void putDimensions(Collection<Dimension> listDims) throws MotuException {

        for (Dimension dim : listDims) {
            putDimension(dim);
        }
    }

    /**
     * Adds a list of dimension contained in a GeoGrid object to the file. The data will be copied when
     * finish() is called.
     *
     * @param geoGrid GeoGrid object
     * @throws MotuException the motu exception
     */
    public void putDimensions(GeoGrid geoGrid) throws MotuException {
        if (geoGrid == null) {
            throw new MotuException(ErrorType.INVALID_LAT_LON_RANGE, "Error in writeDimensions - geoGrid is null");
        }

        List<Dimension> listDims = geoGrid.getDimensions();
        putDimensions(listDims);
    }

    /**
     * Adds a list of dimension contained in a list of GeoGrid objects to the file. The data will be copied
     * when finish() is called. If several geogrids are in the list, it computes output X (longitude)
     * dimension by adding length of each geogrid. It assumes that all geogrids in the list have the same
     * variable, so the same type and number of dimensions
     *
     * @param listGeoGrid list of GeoGrid objects
     * @throws MotuException the motu exception
     */
    public void putDimensions(List<GeoGrid> listGeoGrid) throws MotuException {
        if (listGeoGrid == null) {
            throw new MotuException(ErrorType.INVALID_LAT_LON_RANGE, "Error in putDimensions - list of geoGrids is null");
        }
        if (listGeoGrid.size() <= 0) {
            throw new MotuException(ErrorType.INVALID_LAT_LON_RANGE, "Error in putDimensions - list of geoGrids is empty");
        }

        Dimension computedXDim = null;
        // Compute X Dimension
        for (GeoGrid geoGrid : listGeoGrid) {
            Dimension xDim = geoGrid.getXDimension();
            // geogrid has no X dim ==> break;
            if (xDim == null) {
                break;
            }
            // first X dim found ==> create new Xdim from X geogrid dim.
            if (computedXDim == null) {
                computedXDim = new Dimension(xDim.getFullName(), xDim);
            } else {
                // compute new X dim length
                int length = -1;
                if (!xDim.isUnlimited()) {
                    length = computedXDim.getLength() + xDim.getLength();
                }
                computedXDim.setLength(length);
            }
        }

        // write geogrid dimension
        GeoGrid geoGrid = listGeoGrid.get(0);
        int indexXDim = geoGrid.getXDimensionIndex();

        for (int indexDim = 0; indexDim < geoGrid.getDimensions().size(); indexDim++) {
            if ((indexDim == indexXDim) && (computedXDim != null)) {
                putDimension(computedXDim);
            } else {
                putDimension(geoGrid.getDimension(indexDim));
            }
        }

    }

    /**
     * Put dimensions geo xy.
     *
     * @param listGeoGrid the list geo grid
     * @throws MotuException the motu exception
     */
    public void putDimensionsGeoXY(List<GeoGrid> listGeoGrid) throws MotuException {
        if (listGeoGrid == null) {
            throw new MotuException(ErrorType.INVALID_LAT_LON_RANGE, "Error in putDimensions - list of geoGrids is null");
        }
        if (listGeoGrid.size() <= 0) {
            throw new MotuException(ErrorType.INVALID_LAT_LON_RANGE, "Error in putDimensions - list of geoGrids is empty");
        }

        // Here we dont't computed X/Y dimensions.
        // X/Y dimensions are computed separately

        // write geogrid dimension
        GeoGrid geoGrid = listGeoGrid.get(0);
        int indexXDim = geoGrid.getXDimensionIndex();
        int indexYDim = geoGrid.getYDimensionIndex();

        for (int indexDim = 0; indexDim < geoGrid.getDimensions().size(); indexDim++) {

            // Set dimension except X and Y dimension
            if ((indexDim != indexXDim) && ((indexDim != indexYDim))) {
                putDimension(geoGrid.getDimension(indexDim));
            }
        }

    }

    private void checkAxisType(CoordinateAxis axis, AxisType axisType) throws MotuNotImplementedException {
        if ((axisType != AxisType.GeoX) && (axisType != AxisType.GeoY)) {
            throw new MotuNotImplementedException(
                    String.format("ERROR in putDimensionsAxis : Process a coordinate axis with type '%s' than one dimensions is not yet implemented (axis name:'%s'). Only type '%s' and '%s' are accepted",
                                  axisType.toString(),
                                  axis.getFullName(),
                                  AxisType.GeoX.toString(),
                                  AxisType.GeoY.toString()));

        } else if (axisType != axis.getAxisType()) {
            throw new MotuNotImplementedException(
                    String.format("ERROR in putDimensionsAxis : All of the axis have not the sam type. Expected '%s' but got '%s' (axis name:'%s')",
                                  axisType.toString(),
                                  axis.getAxisType().toString(),
                                  axis.getFullName()));
        }
    }

    private void checkAxisIsD(CoordinateAxis axis) throws MotuNotImplementedException {
        if (!(axis instanceof CoordinateAxis1D)) {
            throw new MotuNotImplementedException(
                    String.format("ERROR in putDimensionsAxis : Process a coordinate axis with more than one dimensions is not yet implemented (axis name:'%s')",
                                  axis.getFullName()));
        }
    }

    private void checkCoordinateAxisList(List<CoordinateAxis> listCoordinateAxis) throws MotuNotImplementedException {
        AxisType axisType = null;
        // Compute output dimension
        for (CoordinateAxis axis : listCoordinateAxis) {
            checkAxisIsD(axis);
            if (axisType == null) {
                axisType = axis.getAxisType();
                checkAxisType(axis, axisType);
            }
        }
    }

    private int computeLength(Dimension dim, Map<String, Range> mapRange) {
        int length = -1;

        if (!dim.isUnlimited()) {
            length = 0;
            for (Range r : mapRange.values()) {
                length = length + r.length();
            }
        }
        return length;
    }

    /**
     * Put dimensions axis.
     *
     * @param listCoordinateAxis the list coordinate axis
     * @param mapRange the map range
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    public void putDimensionsAxis(List<CoordinateAxis> listCoordinateAxis, Map<String, Range> mapRange)
            throws MotuException, MotuNotImplementedException {
        if (listCoordinateAxis == null) {
            throw new MotuException(ErrorType.INVALID_LAT_LON_RANGE, "Error in putDimensionsAxis - list of CoordinateAxis is null");
        }
        if (listCoordinateAxis.isEmpty()) {
            throw new MotuException(ErrorType.INVALID_LAT_LON_RANGE, "Error in putDimensionsAxis - list of CoordinateAxis is empty");
        }

        checkCoordinateAxisList(listCoordinateAxis);

        CoordinateAxis axis = listCoordinateAxis.get(0);
        Dimension dim = axis.getDimension(0);
        if (dim != null) {
            putDimension(new Dimension(dim.getFullName(), computeLength(dim, mapRange), dim.isShared(), dim.isUnlimited(), dim.isVariableLength()));
        }

    }

    /**
     * Add a the list of Variables to the file. The data is also copied when finish() is called.
     *
     * @param varAttrToRemove variable attribute to remove
     * @return amount in Megabytes of the data to be written
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    public void writeVariablesToFile(String[] varAttrToRemove) throws MotuException, MotuNotImplementedException {
        double curAmountDataSize = 0.0;
        for (List<Variable> listVar : getVariables().values()) {
            if (!listVar.isEmpty()) {
                for (Variable v : listVar) {
                    curAmountDataSize += NetCdfWriter.countVarSize(v);
                    if (curAmountDataSize > BLLManager.getInstance().getConfigManager().getMotuConfig().getMaxSizePerFileSub().doubleValue()) {
                        break;
                    }

                }
                writeVariable(listVar.get(0), varAttrToRemove);
            }
        }
        setAmountDataSize(curAmountDataSize);
    }

    /**
     * Add a list of global attribute to the file. The data will be copied when finish() is called.
     * 
     * @param list list of global attributes
     */
    public void writeGlobalAttributes(List<Attribute> list) {
        for (Attribute att : list) {
            writeGlobalAttribute(att);
        }
    }

    /**
     * Call this when all attributes, dimensions, and variables have been added. After calling, The data from
     * all Variables will be NOT written to the file and the ile is NOT closed. You cannot add any other
     * attributes, dimensions, or variables after this call.
     *
     * @param varAttrToRemove variable attribute to remove
     * @throws MotuException the motu exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    public void writeVariableInNetCdfFileAndSetNetcdfFileInCreateMode(String[] varAttrToRemove)
            throws MotuException, MotuExceedingCapacityException, MotuNotImplementedException {
        // Add variables to netCdf file.
        writeVariablesToFile(varAttrToRemove);
        checkAmountDataSizeThreshold();

        try {
            long d1 = System.nanoTime();
            getNcfileWriter().create();
            long d2 = System.nanoTime();
            this.writingTime += (d2 - d1);
        } catch (Exception e) {
            LOG.error("create()", e);
            throw new MotuException(ErrorType.NETCDF_GENERATION, "Error in NetcdfWriter create", e);
        }
    }

    /**
     * Call this when all attributes, dimensions, and variables have been added. The data from all Variables
     * will be written to the file. You cannot add any other attributes, dimensions, or variables after this
     * call.
     *
     * @param varAttrToRemove variable attribute to remove
     * @throws MotuException the motu exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    public void finish(String[] varAttrToRemove) throws MotuException, MotuExceedingCapacityException, MotuNotImplementedException {
        // Add variables to file and create it.
        writeVariableInNetCdfFileAndSetNetcdfFileInCreateMode(varAttrToRemove);

        try {
            for (List<Variable> listVar : getVariables().values()) {
                longitudeCenter = 0.0;
                for (Variable var : listVar) {
                    writeVariableByBlock(var);
                }
            }

        } catch (Exception e) {
            throw new MotuException(ErrorType.NETCDF_GENERATION, "Error in NetcdfWriter finish", e);
        } finally {
            try {
                getNcfileWriter().close();
            } catch (IOException e) {
                throw new MotuException(ErrorType.NETCDF_GENERATION, "Error to close NetcdfWriter", e);
            }
        }
    }

    /**
     * Finish geo xy.
     *
     * @param varAttrToRemove the var attr to remove
     * @param listDistinctXRange the list distinct x range
     * @param listDistinctYRange the list distinct y range
     * @param mapVarOrgRanges the map var org ranges
     * @throws MotuException the motu exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    public void finishGeoXY(String[] varAttrToRemove,
                            List<Range> listDistinctXRange,
                            List<Range> listDistinctYRange,
                            Map<String, List<Section>> mapVarOrgRanges)
            throws MotuException, MotuExceedingCapacityException, MotuNotImplementedException {
        // Add variables to file and create it.
        writeVariableInNetCdfFileAndSetNetcdfFileInCreateMode(varAttrToRemove);

        try {
            for (List<Variable> listVar : getVariables().values()) {
                if (!ListUtils.isNullOrEmpty(listVar)) {
                    List<Section> listVarOrgRanges = mapVarOrgRanges.get(listVar.get(0).getFullName());

                    for (int index = 0; index < listVar.size(); index++) {
                        longitudeCenter = 0.0;
                        Variable var = listVar.get(index);
                        if (!"crs".equalsIgnoreCase(var.getFullNameEscaped())) {
                            Section varOrgRanges = null;
                            if (!ListUtils.isNullOrEmpty(listVarOrgRanges)) {
                                varOrgRanges = listVarOrgRanges.get(index);
                            }
                            writeVariableByBlockGeoXY(var, listDistinctXRange, listDistinctYRange, varOrgRanges);
                        }
                    }
                }
            }

        } catch (Exception e) {
            throw new MotuException(ErrorType.NETCDF_GENERATION, "Error in NetcdfWriter finish", e);
        } finally {
            try {
                getNcfileWriter().close();
            } catch (IOException e) {
                throw new MotuException(ErrorType.NETCDF_GENERATION, "Error to close NetcdfWriter", e);
            }
        }
    }

    /**
     * Gets the lengths of each output dimension of a variable .
     * 
     * @param var varaible to get dimension
     * @return an array that contains the lengths of each dimension of the variable
     */
    public int[] getOutputDimensionValues(Variable var) {

        int rank = var.getRank();
        int[] outDimValues = new int[rank];

        for (int i = 0; i < rank; i++) {
            outDimValues[i] = -1;
            Dimension outDim = dimensionMap.get(var.getDimension(i).getFullName());
            if (outDim != null) {
                outDimValues[i] = outDim.getLength();
            }
        }
        return outDimValues;
    }

    /**
     * Gets the output dimension value.
     * 
     * @param var the var
     * @param dimIndex the dim index
     * @return the output dimension value
     * @throws MotuException the motu exception
     */
    public int getOutputDimensionValue(Variable var, int dimIndex) throws MotuException {

        int[] outDimValues = getOutputDimensionValues(var);
        if ((dimIndex < 0) || (dimIndex >= outDimValues.length)) {
            throw new MotuException(
                    ErrorType.INVALID_LAT_LON_RANGE,
                    String.format("Error in NetcdfWriter getOutputDimensionValue - dimIndex (%d) is out-of-ange. Valid range is [0, %d].",
                                  dimIndex,
                                  outDimValues.length));
        }

        return outDimValues[dimIndex];
    }

    /**
     * Gets the lengths of each dimension of a variable.
     * 
     * @param var varaible to get dimension
     * @return an array that contains the lengths of each dimension of the variable
     */
    public int[] getVarDimensionValues(Variable var) {

        int rank = var.getRank();
        int[] varDimValues = new int[rank];

        for (int i = 0; i < rank; i++) {
            varDimValues[i] = var.getDimension(i).getLength();
        }
        return varDimValues;
    }

    /**
     * Gets offset origin (indexes) of the next data according to output dimension of a variable and the
     * dimension of the input variable.
     * 
     * @param origin origin start
     * @param var variables from wich one's gets dimension
     * @return origin (indexes) of the next data according to data dimension, or null if there is no next
     *         origin (dimension capacity exceeded)
     */
    private int[] getNextOriginOffset(int[] origin, Variable var) {
        int rank = var.getRank();
        int[] outDimValues = getOutputDimensionValues(var);
        int[] offsetDimValues = NetCdfWriter.getNextOrigin(origin, outDimValues);
        if (offsetDimValues == null) {
            return offsetDimValues;
        }
        int[] varDimValues = getVarDimensionValues(var);

        for (int i = 0; i < rank; i++) {
            // if same dim value between output and input variable ==> set offset to zero.
            if (outDimValues[i] <= varDimValues[i]) {
                offsetDimValues[i] = 0;
            }
        }
        return offsetDimValues;
    }

    /**
     * Gets origin (indexes) of the next data according to output dimension of a variable.
     * 
     * @param origin origin start
     * @param var variables from wich one's gets dimension
     * @return origin (indexes) of the next data according to data dimension, or null if there is no next
     *         origin (dimension capacity exceeded)
     */
    public int[] getNextOrigin(int[] origin, Variable var) {
        return NetCdfWriter.getNextOrigin(origin, getOutputDimensionValues(var));
    }

    /**
     * Gets origin (indexes) of the next data according to data dimension.
     * 
     * @param origin origin start
     * @param dim data dimension
     * @return origin (indexes) of the next data according to data dimension, or null if there is no next
     *         origin (dimension capacity exceeded)
     */
    public static int[] getNextOrigin(int[] origin, int[] dim) {
        int[] nextOrigin = origin.clone();
        int rank = origin.length;

        // Start from the highest index of the dimension.
        // next data origin is increased by 1 according to the dimension
        // if value is higher than the dim value
        // value is set to zero and value of the previous index level is increased by 1.....
        // and so on ...
        // If there no next origin (dim capacity exceeded), all value at set to -1.
        for (int i = rank - 1; i >= 0; i--) {
            nextOrigin[i]++;
            if (nextOrigin[i] > dim[i] - 1) {
                int j = i - 1;
                if (j >= 0) {
                    for (int k = i; k < rank; k++) {
                        nextOrigin[k] = 0;
                    }
                } else {
                    nextOrigin = null;
                    break;
                }
            } else {
                break;
            }
        }
        return nextOrigin;
    }

    /**
     * Writes variable data in one gulp. It reads the variable data by block and writes each block data in the
     * netcdf file.
     *
     * @param var variable to be written
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    protected void writeVariableByBlock(Variable var) throws MotuException, MotuNotImplementedException {
        int[] origin = null;
        int[] shape = null;
        Array data = null;
        int rank = var.getRank();
        CoordinateAxis axisLon = null;
        if (var instanceof CoordinateAxis) {
            axisLon = (CoordinateAxis) var;
            if (axisLon.getAxisType() != AxisType.Lon) {
                axisLon = null;
            }
        }
        int[] originOutOffset = originOutOffsetHash.get(var.getFullName());
        if (originOutOffset == null) {
            originOutOffset = new int[rank];
            for (int i = 0; i < rank; i++) {
                originOutOffset[i] = 0;
            }
            originOutOffsetHash.put(var.getFullName(), originOutOffset);
        }

        int[] originMax = new int[rank];
        for (int i = 0; i < rank; i++) {
            originMax[i] = 0;
        }

        try {
            Map<int[], int[]> originAndShape = NetCdfWriter.parseOriginAndShape(var);

            // CSOFF: StrictDuplicateCode : normal duplication code.

            Set<int[]> keySet = originAndShape.keySet();

            for (Iterator<int[]> it = keySet.iterator(); it.hasNext();) {
                // get a Runtime object
                origin = it.next();

                if ((origin == null) && (var.getShape().length != 0)) {
                    throw new MotuException(
                            ErrorType.INVALID_LAT_LON_RANGE,
                            "Error in NetCfdWriter writeVariableByBlock - unable to find origin - (origin is null)");
                }

                shape = originAndShape.get(origin);

                if ((shape == null) && (var.getShape().length != 0)) {
                    throw new MotuException(
                            ErrorType.INVALID_LAT_LON_RANGE,
                            "Error in NetCfdWriter writeVariableByBlock - unable to find shape - (shape is null)");
                }
                // CSOON: StrictDuplicateCode

                data = read(var, origin, shape);
                // Normalize longitude if necessary.
                if (axisLon != null) {
                    MAMath.MinMax minMax = minMaxHash.get(axisLon.getFullName());

                    MAMath.MinMax minMaxTemp = new MAMath.MinMax(minMax.min, minMax.max);
                    NetCdfWriter.applyScaleFactorAndOffset(minMaxTemp, axisLon);
                    longitudeCenter = (minMaxTemp.min + minMaxTemp.max) / 2;
                    normalizeLongitudeData(data, axisLon);
                }

                writeVariableData(var, origin, data);

                // Computes max origin of the data
                for (int i = rank - 1; i >= 0; i--) {
                    int newIndexValue = originOutOffset[i] + origin[i] + shape[i] - 1;
                    originMax[i] = Math.max(originMax[i], newIndexValue);
                }

            }
            // Computes offset origin of the next data for longitude dim
            originOutOffset = originMax.clone();
            originOutOffset = getNextOriginOffset(originOutOffset, var);
            originOutOffsetHash.remove(var.getFullName());
            if (originOutOffset != null) {
                originOutOffsetHash.put(var.getFullName(), originOutOffset);
            }

        } catch (IOException e) {
            LOG.error("writeVariableByBlock()", e);
            throw new MotuException(ErrorType.NETCDF_GENERATION, "Error IOException in NetcdfWriter writeVariableByBlock", e);
        } catch (InvalidRangeException e) {
            LOG.error("writeVariableByBlock()", e);
            throw new MotuException(ErrorType.NETCDF_GENERATION, "Error InvalidRangeException in NetcdfWriter writeVariableByBlock", e);
        }
    }

    /**
     * Write variable by block geo xy.
     *
     * @param var the var
     * @param listDistinctXRange the list distinct x range
     * @param listDistinctYRange the list distinct y range
     * @param varOrgRanges the var org ranges
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    protected void writeVariableByBlockGeoXY(Variable var, List<Range> listDistinctXRange, List<Range> listDistinctYRange, Section varOrgRanges)
            throws MotuException, MotuNotImplementedException {
        int geoXAxisIndex = getGeoXDimVarIndex(var);
        int geoYAxisIndex = getGeoYDimVarIndex(var);

        int rank = var.getRank();

        int[] originSectionOffset = new int[rank];
        for (int i = 0; i < rank; i++) {
            originSectionOffset[i] = 0;
        }

        int outDimXValue = -1;
        int outDimYValue = -1;

        if (geoXAxisIndex != -1) {
            outDimXValue = getOutputDimensionValue(var, geoXAxisIndex);
            originSectionOffset[geoXAxisIndex] = computeSectionOffset(outDimXValue, geoXAxisIndex, varOrgRanges, listDistinctXRange);
        }
        if (geoYAxisIndex != -1) {
            outDimYValue = getOutputDimensionValue(var, geoYAxisIndex);
            originSectionOffset[geoYAxisIndex] = computeSectionOffset(outDimYValue, geoYAxisIndex, varOrgRanges, listDistinctYRange);
        }

        int[] originOutOffset = originOutOffsetHash.get(var.getFullName());
        if (originOutOffset == null) {
            originOutOffset = new int[rank];
            for (int i = 0; i < rank; i++) {
                originOutOffset[i] = originSectionOffset[i];
            }
            originOutOffsetHash.put(var.getFullName(), originOutOffset);
        } else {
            for (int i = 0; i < rank; i++) {
                originOutOffset[i] += originSectionOffset[i];
            }
        }

        try {
            Map<int[], int[]> originAndShape = NetCdfWriter.parseOriginAndShape(var);
            Iterator<int[]> it = originAndShape.keySet().iterator();
            while (it.hasNext()) {
                int[] origin = it.next();
                checkOrigin(origin, var);

                int[] shape = originAndShape.get(origin);
                checkShape(shape, var);

                Array data = read(var, origin, shape);
                writeVariableData(var, origin, data);
            }
            originOutOffsetHash.remove(var.getFullName());

        } catch (IOException e) {
            LOG.error("writeVariableByBlockGeoXY()", e);
            throw new MotuException(ErrorType.NETCDF_GENERATION, "Error IOException in NetcdfWriter writeVariableByBlockGeoXY", e);
        } catch (InvalidRangeException e) {
            LOG.error("writeVariableByBlockGeoXY()", e);
            throw new MotuException(ErrorType.NETCDF_GENERATION, "Error InvalidRangeException in NetcdfWriter writeVariableByBlockGeoXY", e);
        }
    }

    private void checkShape(int[] shape, Variable var) throws MotuException {
        if ((shape == null) && (var.getShape().length != 0)) {
            throw new MotuException(
                    ErrorType.INVALID_LAT_LON_RANGE,
                    "Error in NetCfdWriter writeVariableByBlockGeoXY - unable to find shape - (shape is null)");
        }
    }

    private void checkOrigin(int[] origin, Variable var) throws MotuException {
        if ((origin == null) && (var.getShape().length != 0)) {
            throw new MotuException(
                    ErrorType.INVALID_LAT_LON_RANGE,
                    "Error in NetCfdWriter writeVariableByBlockGeoXY - unable to find origin - (origin is null)");
        }
    }

    /**
     * Compute section offset.
     *
     * @param outDimValue the out dim value
     * @param dimIndex the dim index
     * @param originalSection the original section
     * @param listDistinctRange the list distinct range
     * @return the int
     * @throws MotuException the motu exception
     */
    protected int computeSectionOffset(int outDimValue, int dimIndex, Section originalSection, List<Range> listDistinctRange) throws MotuException {
        int diff = 0;
        if (!(ListUtils.isNullOrEmpty(listDistinctRange) && ((dimIndex < 0) || (outDimValue < 0) || (originalSection == null)))) {
            Range refRange = originalSection.getRange(dimIndex);
            int i = 0;
            while (i < listDistinctRange.size()) {
                Range r = listDistinctRange.get(i);
                if (refRange.intersects(r)) {
                    diff += refRange.first() - r.first();
                    break;
                } else {
                    diff += r.length();
                }
                i++;
            }
        }

        return diff;
    }

    /**
     * Gets the geo x dim var index.
     *
     * @param var the var
     * @return the geo x dim var index
     */
    protected int getGeoXDimVarIndex(Variable var) {
        return getGeoDimVarIndex(var, AxisType.GeoX);
    }

    /**
     * Gets the geo y dim var index.
     *
     * @param var the var
     * @return the geo y dim var index
     */
    protected int getGeoYDimVarIndex(Variable var) {
        return getGeoDimVarIndex(var, AxisType.GeoY);
    }

    /**
     * Gets the geo dim var index.
     *
     * @param var the var
     * @param axisType the axis type
     * @return the geo dim var index
     */
    protected int getGeoDimVarIndex(Variable var, AxisType axisType) {
        int index = -1;
        for (int i = 0; i < var.getDimensions().size(); i++) {
            List<Variable> axes = getVariables().get(var.getDimension(i).getFullName());
            if (ListUtils.isNullOrEmpty(axes)) {
                return index;
            }

            Variable v = axes.get(0);
            if (v instanceof CoordinateAxis) {
                CoordinateAxis axis = (CoordinateAxis) v;
                if (axis.getAxisType() == axisType) {
                    index = i;
                    break;
                }
            }
        }

        return index;
    }

    /**
     * Gets the geo x dim var.
     *
     * @param var the var
     * @return the geo x dim var
     */
    protected CoordinateAxis getGeoXDimVar(Variable var) {
        return getGeoDimVar(var, AxisType.GeoX);
    }

    /**
     * Gets the geo y dim var.
     *
     * @param var the var
     * @return the geo y dim var
     */
    protected CoordinateAxis getGeoYDimVar(Variable var) {
        return getGeoDimVar(var, AxisType.GeoY);
    }

    /**
     * Gets the geo dim var.
     *
     * @param var the var
     * @param axisType the axis type
     * @return the geo dim var
     */
    protected CoordinateAxis getGeoDimVar(Variable var, AxisType axisType) {
        CoordinateAxis axis = null;

        for (Dimension dim : var.getDimensions()) {
            List<Variable> axes = getVariables().get(dim.getFullName());
            if (ListUtils.isNullOrEmpty(axes)) {
                return axis;
            }

            Variable v = axes.get(0);
            if (v instanceof CoordinateAxis) {
                axis = (CoordinateAxis) v;
                if (axis.getAxisType() == axisType) {
                    break;
                }
            }
        }

        return axis;

    }

    /**
     * Puts longitude into the range [center +/- 180] deg. Center correspond to the class attribute
     *
     * @param data data to normalize
     * @param variable the variable {@link #longitudeCenter}
     */
    protected void normalizeLongitudeData(ArrayDouble data, Variable variable) {

        IndexIterator indexIt = data.getIndexIterator();

        while (indexIt.hasNext()) {

            Number lonNativeApply = NetCdfWriter.applyScaleFactorAndOffset(indexIt.getDoubleNext(), variable);

            double lonConverted = LatLonPointImpl.lonNormal(lonNativeApply.doubleValue(), longitudeCenter);
            indexIt.setDoubleCurrent(NetCdfWriter.undoScaleFactorAndOffset(lonConverted, variable).doubleValue());
        }

    }

    /**
     * Puts longitude into the range [center +/- 180] deg. Center correspond to the class attribute
     *
     * @param data data to normalize
     * @param variable the variable {@link #longitudeCenter}
     */
    protected void normalizeLongitudeData(ArrayFloat data, Variable variable) {

        IndexIterator indexIt = data.getIndexIterator();

        while (indexIt.hasNext()) {

            Number lonNativeApply = NetCdfWriter.applyScaleFactorAndOffset(indexIt.getFloatNext(), variable);

            double lonConverted = LatLonPointImpl.lonNormal(lonNativeApply.doubleValue(), longitudeCenter);
            indexIt.setFloatCurrent(NetCdfWriter.undoScaleFactorAndOffset(lonConverted, variable).floatValue());
        }

    }

    /**
     * Normalize longitude data.
     * 
     * @param data the data
     * @param variable the variable
     */
    protected void normalizeLongitudeData(ArrayShort data, Variable variable) {

        IndexIterator indexIt = data.getIndexIterator();

        while (indexIt.hasNext()) {

            Number lonNativeApply = NetCdfWriter.applyScaleFactorAndOffset(indexIt.getShortNext(), variable);

            double lonConverted = LatLonPointImpl.lonNormal(lonNativeApply.doubleValue(), longitudeCenter);
            indexIt.setShortCurrent(NetCdfWriter.undoScaleFactorAndOffset(lonConverted, variable).shortValue());
        }

    }

    /**
     * Normalize longitude data.
     * 
     * @param data the data
     * @param variable the variable
     */
    protected void normalizeLongitudeData(ArrayInt data, Variable variable) {

        IndexIterator indexIt = data.getIndexIterator();

        while (indexIt.hasNext()) {

            Number lonNativeApply = NetCdfWriter.applyScaleFactorAndOffset(indexIt.getIntNext(), variable);

            double lonConverted = LatLonPointImpl.lonNormal(lonNativeApply.doubleValue(), longitudeCenter);
            indexIt.setIntCurrent(NetCdfWriter.undoScaleFactorAndOffset(lonConverted, variable).intValue());
        }

    }

    /**
     * Normalize longitude data.
     * 
     * @param data the data
     * @param variable the variable
     */
    protected void normalizeLongitudeData(ArrayLong data, Variable variable) {

        IndexIterator indexIt = data.getIndexIterator();

        while (indexIt.hasNext()) {

            Number lonNativeApply = NetCdfWriter.applyScaleFactorAndOffset(indexIt.getLongNext(), variable);

            double lonConverted = LatLonPointImpl.lonNormal(lonNativeApply.doubleValue(), longitudeCenter);
            indexIt.setLongCurrent(NetCdfWriter.undoScaleFactorAndOffset(lonConverted, variable).longValue());
        }

    }

    /**
     * Puts longitude into the range [center +/- 180] deg. Center correspond to the class attribute
     *
     * @param data data to normalize
     * @param variable the variable
     * @throws MotuNotImplementedException the motu not implemented exception {@link #longitudeCenter}
     */
    protected void normalizeLongitudeData(Array data, Variable variable) throws MotuNotImplementedException {
        if (data instanceof ArrayDouble) {
            normalizeLongitudeData((ArrayDouble) data, variable);
        } else if (data instanceof ArrayFloat) {
            normalizeLongitudeData((ArrayFloat) data, variable);
        } else if (data instanceof ArrayShort) {
            normalizeLongitudeData((ArrayShort) data, variable);
        } else if (data instanceof ArrayInt) {
            normalizeLongitudeData((ArrayInt) data, variable);
        } else if (data instanceof ArrayLong) {
            normalizeLongitudeData((ArrayLong) data, variable);
        } else {
            throw new MotuNotImplementedException(
                    String.format("Error in NetcdfWriter normalizeLongitudeData - Array type %s is not implemented", data.getClass().getName()));

        }

    }

    /**
     * Writes variable data in one gulp. It writes data in the netcdf file.
     *
     * @param var variable to be written
     * @param data data to be written
     * @throws MotuException the motu exception
     */
    public void writeVariableData(Variable var, Array data) throws MotuException {
        try {
            if (data != null) {
                Variable varToWrite = getNcfileWriter().findVariable(var.getFullName());
                if (varToWrite != null) {
                    getNcfileWriter().write(varToWrite, data);
                    getNcfileWriter().flush();
                } else {
                    throw new MotuException(
                            ErrorType.NETCDF_GENERATION,
                            "Error in NetcdfWriter, unable to find variable named: " + var.getFullName());
                }
            }
        } catch (Exception e) {
            LOG.error("writeVariableData()", e);
            throw new MotuException(ErrorType.NETCDF_GENERATION, "Error in NetcdfWriter writeVariableData", e);
        }
    }

    /**
     * Writes variable data in one gulp. It writes data in the netcdf file.
     *
     * @param var variable to be written
     * @param origin origin of the read data
     * @param data data to be written
     * @throws MotuException the motu exception
     */
    public void writeVariableData(Variable var, int[] origin, Array data) throws MotuException {
        if (data != null) {
            try {
                Variable varToWrite = getNcfileWriter().findVariable(var.getFullName());
                if (varToWrite != null) {
                    long d1;
                    if (origin == null) {
                        d1 = System.nanoTime();
                        getNcfileWriter().write(varToWrite, data);
                    } else {
                        int[] originOut = computeOriginOut(var, origin);
                        d1 = System.nanoTime();
                        getNcfileWriter().write(varToWrite, originOut, data);
                    }
                    getNcfileWriter().flush();
                    long d2 = System.nanoTime();
                    this.writingTime += (d2 - d1);
                } else {
                    throw new MotuException(
                            ErrorType.NETCDF_GENERATION,
                            "Error in NetcdfWriter, unable to find variable named: " + var.getFullName());
                }
            } catch (Exception e) {
                LOG.error("writeVariableData()", e);
                throw new MotuException(ErrorType.NETCDF_GENERATION, "Error in NetcdfWriter writeVariableData", e);
            }
        }
    }

    private int[] computeOriginOut(Variable var, int[] origin) {
        int rank = var.getRank();
        int[] originOutOffset = originOutOffsetHash.get(var.getFullName());
        if (originOutOffset == null) {
            originOutOffset = new int[rank];
            for (int i = 0; i < rank; i++) {
                originOutOffset[i] = 0;
            }
        }
        int[] originOut = originOutOffset.clone();

        if (origin != null) {
            for (int i = 0; i < rank; i++) {
                originOut[i] += origin[i];
            }
        }
        return originOut;
    }

    /**
     * Gets if data have to be read by block.
     *
     * @param var variable to process.
     * @return true is data have to be read by block, otherwise false.
     * @throws MotuException the motu exception
     */
    public static boolean isReadByBlock(Variable var) throws MotuException {

        return NetCdfWriter.countVarElementData(var) > NetCdfWriter.countMaxElementData(var);

    }

    /**
     * Gets if data have to be read by block.
     *
     * @param varShape variable's shape to process.
     * @param datatype variable's data type to process
     * @return true is data have to be read by block, otherwise false.
     * @throws MotuException the motu exception
     */
    public static boolean isReadByBlock(int[] varShape, DataType datatype) throws MotuException {
        return NetCdfWriter.countVarElementData(varShape) > NetCdfWriter.countMaxElementData(varShape, datatype);
    }

    /**
     * Get the minimum and the maximum data value of the variabble, skipping missing values as defined by
     * missing_value attribute of the variable.
     *
     * @param geoGrid GeoGrid object that contains the variable
     * @param var variable to process.
     * @param minMax previous min/max of the variable
     * @param readingTime the reading time
     * @return both min and max value.
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */

    public static MAMath.MinMax getMinMaxSkipMissingData(GeoGrid geoGrid, Variable var, MAMath.MinMax minMax, NetCdfWriter netCdfWriter)
            throws MotuException, MotuNotImplementedException {

        MAMath.MinMax minMaxWork = NetCdfWriter.getMinMaxSkipMissingData(geoGrid, var, netCdfWriter);

        if ((minMaxWork == null) || (minMax == null)) {
            return minMaxWork;
        }

        if (minMax.min < minMaxWork.min) {
            minMaxWork.min = minMax.min;
        }
        if (minMax.max > minMaxWork.max) {
            minMaxWork.max = minMax.max;
        }
        return minMaxWork;
    }

    /**
     * Get the minimum and the maximum data value of the previously read Array, skipping missing values as
     * defined by isMissingData(double val).
     * 
     * @param a Array to get min/max values
     * @param var variable corresponding to the array
     * 
     * @return both min and max value.
     */
    public static MAMath.MinMax getMinMaxSkipMissingData(Array a, Variable var) {
        return getMinMaxSkipMissingData(a, var, true);
    }

    /**
     * Get the minimum and the maximum data value of the previously read Array, skipping missing values as
     * defined by isMissingData(double val).
     * 
     * @param a Array to get min/max values
     * @param var variable corresponding to the array
     * @param convertScaleOffset true to convert value according to scale factor and offset
     * 
     * @return both min and max value.
     */
    public static MAMath.MinMax getMinMaxSkipMissingData(Array a, Variable var, boolean convertScaleOffset) {

        // get or create an enhence varaible to get missing/fill value
        VariableDS vs = null;
        if (var instanceof VariableDS) {
            vs = (VariableDS) var;
            // if (!vs.isEnhanced()) {
            // vs = new VariableDS(null, var, true);
            // }
        } else {
            vs = new VariableDS(null, var, true);
        }

        if (!vs.hasMissingValue()) {
            return MAMath.getMinMax(a);
        }

        IndexIterator iter = a.getIndexIterator();
        double max = -Double.MAX_VALUE;
        double min = Double.MAX_VALUE;
        while (iter.hasNext()) {
            double val = iter.getDoubleNext();
            // Apply scale factor and offset before value comparison.
            boolean isMissing = false;
            if (convertScaleOffset) {
                isMissing = vs.isMissing(vs.convertScaleOffsetMissing(val));
            } else {
                isMissing = vs.isMissing(val);
            }
            if (!isMissing) {
                if (val > max) {
                    max = val;
                }
                if (val < min) {
                    min = val;
                }
            }
        }
        return new MAMath.MinMax(min, max);
    }

    /**
     * Get the minimum and the maximum data value of the variabble, skipping missing values as defined by
     * missing_value attribute of the variable.
     *
     * @param geoGrid GeoGrid object that contains the variable
     * @param var variable to process.
     * @param readingTime the reading time
     * @return both min and max value.
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */

    public static MAMath.MinMax getMinMaxSkipMissingData(GeoGrid geoGrid, Variable var, NetCdfWriter netCdfWriter)
            throws MotuException, MotuNotImplementedException {
        MAMath.MinMax minMax = null;
        try {
            if (!NetCdfWriter.isReadByBlock(var)) {
                // Array data = var.read();
                Array data = NetCdfWriter.read(var, netCdfWriter);
                // Don't use var but geoGrid.getVariable() to get missing/fill value.
                // minMax = geoGrid.getMinMaxSkipMissingData(data);
                minMax = NetCdfWriter.getMinMaxSkipMissingData(data, geoGrid.getVariable());
            } else {
                minMax = NetCdfWriter.getMinMaxSkipMissingDataByBlock(geoGrid, var, netCdfWriter);
            }
        } catch (IOException e) {
            LOG.error("getMinMaxSkipMissingData()", e);
            throw new MotuException(
                    ErrorType.NETCDF_GENERATION,
                    String.format("I/O Error in NetcdfWriter getMinMaxSkipMissingData - Variable %s ", var.getFullName()),
                    e);
        }

        // If all values are missing data, then min is greater to max
        // No MinMax --> return null;
        if (minMax.min > minMax.max) {
            minMax = null;
        }

        return minMax;
    }

    /**
     * Gets the min max skip missing data.
     *
     * @param axis the axis
     * @param minMax the min max
     * @return the min max skip missing data
     */
    public static MAMath.MinMax getMinMaxSkipMissingData(CoordinateAxis axis, MAMath.MinMax minMax) {
        return getMinMaxSkipMissingData(axis, minMax, null);
    }

    /**
     * Get the minimum and the maximum data value of the variabble, skipping missing values as defined by
     * missing_value attribute of the Coordinate axis.
     *
     * @param axis coordinate axis to process.
     * @param minMax previous min/max of the variable
     * @param readingTime the reading time
     * @return both min and max value.
     */
    public static MAMath.MinMax getMinMaxSkipMissingData(CoordinateAxis axis, MAMath.MinMax minMax, NetCdfWriter netCdfWriter) {
        MAMath.MinMax minMaxWork = new MAMath.MinMax(Double.MAX_VALUE, -Double.MAX_VALUE);
        if (!axis.hasMissing()) {
            minMaxWork.min = axis.getMinValue();
            minMaxWork.max = axis.getMaxValue();
        } else {
            try {
                Array data = NetCdfWriter.read(axis, netCdfWriter);
                minMaxWork = NetCdfWriter.getMinMaxSkipMissingData(data, axis, false);
            } catch (IOException ioe) { /* what ?? */
            }
        }

        if (minMax == null) {
            return minMaxWork;
        }

        // Special for Longitude
        // this is not the first part (minMax != null)
        // Normalize longitude with first part min value (as center longitude)
        if (axis.getAxisType() == AxisType.Lon) {
            double center = (minMax.min + minMax.max) / 2;
            minMaxWork.min = LatLonPointImpl.lonNormal(minMaxWork.min, center);
            minMaxWork.max = LatLonPointImpl.lonNormal(minMaxWork.max, center);
        }

        if (minMax.min < minMaxWork.min) {
            minMaxWork.min = minMax.min;
        }
        if (minMax.max > minMaxWork.max) {
            minMaxWork.max = minMax.max;
        }

        return minMaxWork;
    }

    /**
     * Get the minimum and the maximum data value of the variable, skipping missing values as defined by
     * missing_value attribute of the variable. Variable is read by block.
     *
     * @param geoGrid GeoGrid object that contains the variable
     * @param var variable to process.
     * @param readingTime the reading time
     * @return both min and max value.
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    public static MAMath.MinMax getMinMaxSkipMissingDataByBlock(GeoGrid geoGrid, Variable var, NetCdfWriter netCdfWriter)
            throws MotuException, MotuNotImplementedException {
        int[] origin = null;
        int[] shape = null;
        Array data = null;
        MAMath.MinMax minMax = null;
        try {
            Map<int[], int[]> originAndShape = NetCdfWriter.parseOriginAndShape(var);
            Set<int[]> keySet = originAndShape.keySet();

            for (Iterator<int[]> it = keySet.iterator(); it.hasNext();) {
                origin = it.next();

                if (origin == null) {
                    throw new MotuException(
                            ErrorType.INVALID_LAT_LON_RANGE,
                            String.format("Error in NetCfdWriter getMinMaxSkipMissingDataByBlock - unable to find origin - (origin is null) Variable %s ",
                                          var.getFullName()));
                }

                shape = originAndShape.get(origin);

                if (shape == null) {
                    throw new MotuException(
                            ErrorType.INVALID_LAT_LON_RANGE,
                            String.format("Error in NetCfdWriter getMinMaxSkipMissingDataByBlock - unable to find shape - (shape is null)- Variable %s ",
                                          var.getFullName()));
                }
                data = NetCdfWriter.read(var, origin, shape, netCdfWriter);

                // Don't use var but geoGrid.getVariable() to get missing/fill value.
                MAMath.MinMax minMaxTemp = NetCdfWriter.getMinMaxSkipMissingData(data, geoGrid.getVariable());
                if (minMax == null) {
                    minMax = minMaxTemp;
                } else {
                    if (minMaxTemp.max > minMax.max) {
                        minMax.max = minMaxTemp.max;
                    }
                    if (minMaxTemp.min < minMax.min) {
                        minMax.min = minMaxTemp.min;
                    }
                }
            }

        } catch (IOException e) {
            LOG.error("getMinMaxSkipMissingDataByBlock()", e);
            throw new MotuException(ErrorType.NETCDF_GENERATION, "I/O Error in NetcdfWriter getMinMaxSkipMissingDataByBlock", e);
        } catch (InvalidRangeException e) {
            LOG.error("getMinMaxSkipMissingDataByBlock()", e);
            throw new MotuException(ErrorType.NETCDF_GENERATION, "Error in NetcdfWriter getMinMaxSkipMissingDataByBlock", e);
        }

        return minMax;
    }

    /**
     * Computes the data size in Mega-bytes of a variable.
     * 
     * For variable whose datatype size is not known, byte size of each data element is set tot 1.
     *
     * @param var variable to process.
     * @return data size in Mega-bytes of the variable
     * @throws MotuException the motu exception
     */
    private static double countVarSize(Variable var) throws MotuException {

        DataType dataType = var.getDataType();

        // Warning : if variable has scale factor and/or offset attribute
        // variable datatype is "double"
        // so, get the original datatype
        if (var instanceof VariableDS) {
            VariableDS varDS = (VariableDS) var;
            dataType = varDS.getOriginalDataType();
        }

        return countVarSize(var.getShape(), dataType);
    }

    /**
     * Computes the data size in Mega-bytes of a variable.
     * 
     * For variable whose datatype size is not known, byte size of each data element is set tot 1.
     *
     * @param datatype variable's data type to process
     * @param listShapes list of variables's shapes to process.
     * @return data size in Mega-bytes according to data type and shapes
     * @throws MotuException the motu exception
     */
    public static long countVarSize(DataType datatype, List<int[]> listShapes) throws MotuException {
        long count = 0;
        for (int[] shape : listShapes) {
            count += countVarSize(shape, datatype);
        }
        return count;
    }

    /**
     * Computes the data size in Mega-bytes of a variable.
     * 
     * For variable whose datatype size is not known, byte size of each data element is set tot 1.
     *
     * @param varShape variable's shape to process.
     * @param datatype variable's data type to process
     * @return number of max. element
     * @throws MotuException the motu exception
     */
    private static double countVarSize(int[] varShape, DataType datatype) throws MotuException {
        if (varShape.length <= 0) {
            // throw new MotuException(String.format("Error in NetCdfWriter.countVarSize - incorrect dimension
            // %d for parameter varShape",
            // varShape.length));
            return 0.0;
        }
        int byteSize = datatype.getSize();
        if (byteSize <= 0) {
            byteSize = 1;
        }
        return UnitUtils.byteToMegaByte((NetCdfWriter.countVarElementData(varShape) * (double) byteSize));
    }

    /**
     * Computes the maximum number of elements that can be process according to block size for data
     * processing.
     * 
     * Returns (max. size of a block in bytes to be process * 1024) / (byte size of the variable datatype).
     * 
     * The max. size of a block in Kilo-bytes to be process is in the Motu configuration file (dataBlocksize
     * attribute of MotuConfig) for variable whose datatype size is not known, byte size is set tot 1.
     *
     * @param var variable to process.
     * @return data size in Mega-bytes according to data type and shape
     * @throws MotuException the motu exception
     */
    public static long countMaxElementData(Variable var) throws MotuException {
        return countMaxElementData(var.getShape(), var.getDataType());
    }

    /**
     * Count max element data.
     *
     * @param datatype variable's data type to process
     * @param listShapes list of variables's shapes to process.
     * @return returns the number of element in the variable, according to the shapes.
     * @throws MotuException the motu exception
     */
    public static long countMaxElementData(DataType datatype, List<int[]> listShapes) throws MotuException {
        long count = 0;
        for (int[] shape : listShapes) {
            count += countMaxElementData(shape, datatype);
        }
        return count;
    }

    /**
     * Computes the maximum number of elements that can be process according to block size for data
     * processing.
     * 
     * Returns (max. size of a block in bytes to be process * 1024) / (byte size of the variable datatype).
     * 
     * The max. size of a block in Kilo-bytes to be process is in the Motu configuration file (dataBlocksize
     * attribute of MotuConfig) for variable whose datatype size is not known, byte size is set tot 1.
     *
     * @param varShape variable's shape to process.
     * @param datatype variable's data type to process
     * @return number of max. element
     * @throws MotuException the motu exception
     */
    private static long countMaxElementData(int[] varShape, DataType datatype) {
        if (varShape.length <= 0) {
            return 1;
        }
        int byteSize = datatype.getSize();
        if (byteSize <= 0) {
            byteSize = 1;
        }
        return UnitUtils.bytetoKilobyte(BLLManager.getInstance().getConfigManager().getMotuConfig().getDataBlockSize().intValue()) / (byteSize);
    }

    /**
     * Computes the element block size for data processing.
     * 
     * Returns (max. size of a block in bytes to be process / (byte size of the variable datatype) * number of
     * dimension).
     * 
     * The max. size of a block in Kilo-bytes to be process is in the Motu configuration file (dataBlocksize
     * attribute of MotuConfig) For variable whose datatype size is not known, byte size is set tot 1.
     *
     * @param var variable to process.
     * @return element block size
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    private static int getElementBlockSize(Variable var) throws MotuException, MotuNotImplementedException {
        return getElementBlockSize(var.getShape(), var.getDataType());
    }

    /**
     * Computes the element block size for data processing.
     * 
     * Returns (max. size of a block in bytes to be process / (byte size of the variable datatype) * number of
     * dimension).
     * 
     * The max. size of a block in Kilo-bytes to be process is in the Motu configuration file (dataBlocksize
     * attribute of MotuConfig) For variable whose datatype size is not known, byte size is set tot 1.
     *
     * @param varShape variable's shape to process.
     * @param datatype variable's data type to process
     * @return element block size
     */
    private static int getElementBlockSize(int[] varShape, DataType datatype) {
        double maxDataToUse = countMaxElementData(varShape, datatype);
        double pow = 1.0 / varShape.length;
        return (int) Math.pow(maxDataToUse, pow);
    }

    /**
     * Count var element data.
     *
     * @param var variable to process
     * @return returns the number of element in the variable.
     */
    private static long countVarElementData(Variable var) {
        return NetCdfWriter.countVarElementData(var.getShape());
    }

    /**
     * Count var element data.
     *
     * @param var variable to process.
     * @param listShapes list of variables's shapes to process.
     * @return returns the number of element in the variable, according to the shapes.
     * @throws MotuException the motu exception
     */
    public static long countVarElementData(Variable var, List<int[]> listShapes) throws MotuException {
        long count = 0;
        for (int[] shape : listShapes) {
            count += countVarElementData(shape);
        }
        return count;
    }

    /**
     * Count var element data.
     *
     * @param varShape variable shape to process
     * @return returns the number of element in the variable.
     */
    private static long countVarElementData(int[] varShape) {
        if (varShape.length <= 0) {
            return 0;
        }
        long countVarElementData = 1;
        for (int i = 0; i < varShape.length; i++) {
            countVarElementData *= varShape[i];
        }
        return countVarElementData;
    }

    /**
     * Copy attributes.
     *
     * @param src the src
     * @param dest the dest
     */
    public static void copyAttributes(Variable src, Variable dest) {
        NetCdfWriter.copyAttributes(src, dest, true);
    }

    /**
     * Copies attributes of a variable to another variable.
     *
     * @param src variable to copy from
     * @param dest variable to copy to
     * @param overwrite the overwrite
     */
    public static void copyAttributes(Variable src, Variable dest, boolean overwrite) {
        if ((src != null) && (dest != null)) {
            List<Attribute> attributes = src.getAttributes();
            for (Attribute att : attributes) {
                if (overwrite || (!overwrite && dest.findAttributeIgnoreCase(att.getFullName()) != null)) {
                    dest.addAttribute(new Attribute(att.getFullName(), att));
                }
            }
        }
    }

    /**
     * Gets the origins and shapes for block data reading. The max. size of a block in Ko to be process is in
     * the Motu configuration file (dataBlocksize attribute of MotuConfig)
     *
     * @param var variable to process
     * @return a map tha contains the shape (blocksize) to extract for each origin
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    public static Map<int[], int[]> parseOriginAndShape(Variable var) throws MotuException, MotuNotImplementedException {
        return NetCdfWriter.parseOriginAndShape(var.getShape(), var.getDataType());
    }

    /**
     * Gets the origins and shapes for block data reading. The max. size of a block in Ko to be process is in
     * the Motu configuration file (dataBlocksize attribute of MotuConfig)
     *
     * @param varShape variable's shape to process
     * @param datatype variable's data type to process
     * @return a map tha contains the shape (blocksize) to extract for each origin
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    public static Map<int[], int[]> parseOriginAndShape(int[] varShape, DataType datatype) throws MotuException {
        Map<int[], int[]> map;

        for (int i = 0; i < varShape.length; i++) {
            checkShapeValue(varShape[i], i);
        }
        int nDims = varShape.length;
        int blockElementSize = NetCdfWriter.getElementBlockSize(varShape, datatype);

        if (blockElementSize <= 0) {
            blockElementSize = 1;
        }

        switch (nDims) {
        case 0:
            map = NetCdfWriter.parseOriginAndShape0Dim(varShape);
            break;
        case 1:
            map = NetCdfWriter.parseOriginAndShape1Dim(varShape, blockElementSize);
            break;
        case 2:
            map = NetCdfWriter.parseOriginAndShape2Dim(varShape, blockElementSize);
            break;
        case 3:
            map = NetCdfWriter.parseOriginAndShape3Dim(varShape, blockElementSize);
            break;
        case 4:
            map = NetCdfWriter.parseOriginAndShape4Dim(varShape, blockElementSize);
            break;
        default:
            throw new MotuException(
                    ErrorType.NOT_IMPLEMENTED,
                    String.format("Error in  NetCdfWriter.parseOriginAndShape1Dim - Processing for %d-dimension is not implemented", nDims));
        }

        return map;
    }

    private static void checkShapeValue(int shapeValue, int index) throws MotuException {
        if (shapeValue <= 0) {
            throw new MotuException(
                    ErrorType.INVALID_LAT_LON_RANGE,
                    String.format("Error in NetCdfWriter.parseOriginAndShape - incorrect value %d for varShape[%d]", shapeValue, index));
        }
    }

    /**
     * Gets the origins and shapes for block data writing. The max. size of a block in Ko to be process is in
     * the Motu configuration file (dataBlocksize attribute of MotuConfig)
     *
     * @param var variable to process
     * @return a map tha contains the shape (blocksize) to extract for each origin
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    public static Map<int[], int[]> parseOriginAndShapeForWriting(Variable var) throws MotuException, MotuNotImplementedException {

        Map<int[], int[]> map = null;

        int[] varShape = var.getShape();

        for (int i = 0; i < varShape.length; i++) {
            if (varShape[i] <= 0) {
                throw new MotuException(
                        ErrorType.INVALID_LAT_LON_RANGE,
                        String.format("Error in NetCdfWriter.parseOriginAndShape - incorrect value %d for varShape[%d]", varShape[i], i));
            }
        }
        int nDims = varShape.length;
        int blockElementSize = NetCdfWriter.getElementBlockSize(var);

        if (blockElementSize <= 0) {
            blockElementSize = 1;
        }

        switch (nDims) {
        case 1:
            map = NetCdfWriter.parseOriginAndShape1DimForWriting(varShape);
            break;
        case 2:
            map = NetCdfWriter.parseOriginAndShape2DimForWriting(varShape);
            break;
        case 3:
            map = NetCdfWriter.parseOriginAndShape3DimForWriting(varShape);
            break;
        case 4:
            // map = NetCdfWriter.parseOriginAndShape4DimForWriting(varShape);
            break;
        default:
            throw new MotuNotImplementedException(
                    String.format("Error in  NetCdfWriter.parseOriginAndShape1Dim - Processing for %d-dimension is not implemented", nDims));
            // break;
        }

        return map;
    }

    /**
     * Parses the origin and shape0 dim.
     *
     * @param varShape the var shape
     * @return the map
     * @throws MotuException the motu exception
     */
    public static Map<int[], int[]> parseOriginAndShape0Dim(int[] varShape) throws MotuException {
        Map<int[], int[]> map = new HashMap<int[], int[]>();
        if (varShape.length != 0) {
            throw new MotuException(
                    ErrorType.INVALID_LAT_LON_RANGE,
                    String.format("Error in NetCdfWriter.parseOriginAndShape0Dim - incorrect dimension %d for parameter varShape - expected value is 0",
                                  varShape.length));
        }

        map.put(null, null);
        return map;
    }

    /**
     * Gets the origins and shapes for block 1-dimension data processing.
     *
     * @param varShape shape of the variable to be extracted
     * @param blockSize the number of elements to be process for each dimension
     * @return a map tha contains the shape (blocksize) to extract for each origin
     * @throws MotuException the motu exception
     */
    public static Map<int[], int[]> parseOriginAndShape1Dim(int[] varShape, int blockSize) throws MotuException {

        Map<int[], int[]> map = new HashMap<int[], int[]>();
        if (varShape.length != 1) {
            throw new MotuException(
                    ErrorType.INVALID_LAT_LON_RANGE,
                    String.format("Error in NetCdfWriter.parseOriginAndShape1Dim - incorrect dimension %d for parameter varShape - expected value is 1",
                                  varShape.length));
        }

        for (int i = 0; i < varShape[0]; i = i + blockSize) {
            int[] origin = new int[varShape.length];
            int[] shape = new int[varShape.length];

            origin[0] = i;
            shape[0] = Math.min(blockSize, (varShape[0] - i));
            map.put(origin, shape);
        }
        return map;
    }

    /**
     * Gets the origins and shapes for block 2-dimensions data processing.
     *
     * @param varShape shape of the variable to be extracted
     * @param blockSize the number of elements to be process for each dimension
     * @return a map tha contains the shape (blocksize) to extract for each origin
     * @throws MotuException the motu exception
     */
    public static Map<int[], int[]> parseOriginAndShape2Dim(int[] varShape, int blockSize) throws MotuException {
        Map<int[], int[]> map = new HashMap<int[], int[]>();
        if (varShape.length != 2) {
            throw new MotuException(
                    ErrorType.INVALID_LAT_LON_RANGE,
                    String.format("Error in NetCdfWriter.parseOriginAndShape2Dim - incorrect dimension %d for parameter varShape - expected value is 2",
                                  varShape.length));
        }
        for (int i = 0; i < varShape[0]; i = i + blockSize) {
            for (int j = 0; j < varShape[1]; j = j + blockSize) {
                int[] origin = new int[varShape.length];
                int[] shape = new int[varShape.length];

                origin[0] = i;
                origin[1] = j;
                shape[0] = Math.min(blockSize, (varShape[0] - i));
                shape[1] = Math.min(blockSize, (varShape[1] - j));
                map.put(origin, shape);
            }
        }
        return map;
    }

    /**
     * Gets the origins and shapes for block 3-dimensions data processing.
     *
     * @param varShape shape of the variable to be extracted
     * @param blockSize the number of elements to be process for each dimension
     * @return a map tha contains the shape (blocksize) to extract for each origin
     * @throws MotuException the motu exception
     */
    public static Map<int[], int[]> parseOriginAndShape3Dim(int[] varShape, int blockSize) throws MotuException {

        Map<int[], int[]> map = new HashMap<int[], int[]>();
        if (varShape.length != 3) {
            throw new MotuException(
                    ErrorType.INVALID_LAT_LON_RANGE,
                    String.format("Error in NetCdfWriter.parseOriginAndShape3Dim - incorrect dimension %d for parameter varShape - expected value is 3",
                                  varShape.length));
        }
        for (int i = 0; i < varShape[0]; i = i + blockSize) {
            for (int j = 0; j < varShape[1]; j = j + blockSize) {
                for (int k = 0; k < varShape[2]; k = k + blockSize) {
                    int[] origin = new int[varShape.length];
                    int[] shape = new int[varShape.length];

                    origin[0] = i;
                    origin[1] = j;
                    origin[2] = k;
                    shape[0] = Math.min(blockSize, (varShape[0] - i));
                    shape[1] = Math.min(blockSize, (varShape[1] - j));
                    shape[2] = Math.min(blockSize, (varShape[2] - k));
                    map.put(origin, shape);
                }
            }
        }
        return map;
    }

    /**
     * Gets the origins and shapes for block 4-dimensions data processing.
     *
     * @param varShape shape of the variable to be extracted
     * @param blockSize the number of elements to be process for each dimension
     * @return a map tha contains the shape (blocksize) to extract for each origin
     * @throws MotuException the motu exception
     */
    public static Map<int[], int[]> parseOriginAndShape4Dim(int[] varShape, int blockSize) throws MotuException {
        Map<int[], int[]> map = new HashMap<int[], int[]>();
        if (varShape.length != 4) {
            throw new MotuException(
                    ErrorType.INVALID_LAT_LON_RANGE,
                    String.format("Error in NetCdfWriter.parseOriginAndShape4Dim - incorrect dimension %d for parameter varShape - expected value is 4",
                                  varShape.length));
        }
        for (int i = 0; i < varShape[0]; i = i + blockSize) {
            for (int j = 0; j < varShape[1]; j = j + blockSize) {
                for (int k = 0; k < varShape[2]; k = k + blockSize) {
                    for (int l = 0; l < varShape[3]; l = l + blockSize) {
                        int[] origin = new int[varShape.length];
                        int[] shape = new int[varShape.length];

                        origin[0] = i;
                        origin[1] = j;
                        origin[2] = k;
                        origin[3] = l;
                        shape[0] = Math.min(blockSize, (varShape[0] - i));
                        shape[1] = Math.min(blockSize, (varShape[1] - j));
                        shape[2] = Math.min(blockSize, (varShape[2] - k));
                        shape[3] = Math.min(blockSize, (varShape[3] - l));
                        map.put(origin, shape);
                    }
                }
            }
        }
        return map;
    }

    /**
     * Gets the origins and shapes for block 1-dimension data writing.
     *
     * @param varShape shape of the variable to be extracted
     * @return a map tha contains the shape (blocksize) for each origin
     * @throws MotuException the motu exception
     */
    public static Map<int[], int[]> parseOriginAndShape1DimForWriting(int[] varShape) throws MotuException {

        Map<int[], int[]> map = new HashMap<int[], int[]>();
        if (varShape.length != 1) {
            throw new MotuException(
                    ErrorType.INVALID_LAT_LON_RANGE,
                    String.format("Error in NetCdfWriter.parseOriginAndShape1DimForWriting - incorrect dimension %d for parameter varShape",
                                  varShape.length));
        }
        int[] origin = new int[varShape.length];
        int[] shape = new int[varShape.length];
        origin[0] = 0;
        shape[0] = varShape[0];
        map.put(origin, varShape);
        return map;
    }

    /**
     * Gets the origins and shapes for block 2-dimensions data processing.
     *
     * @param varShape shape of the variable to be extracted
     * @return a map tha contains the shape (blocksize) to extract for each origin
     * @throws MotuException the motu exception
     */
    public static Map<int[], int[]> parseOriginAndShape2DimForWriting(int[] varShape) throws MotuException {

        Map<int[], int[]> map = new HashMap<int[], int[]>();
        if (varShape.length != 2) {
            throw new MotuException(
                    ErrorType.INVALID_LAT_LON_RANGE,
                    String.format("Error in NetCdfWriter.parseOriginAndShape2DimForWriting - incorrect dimension %d for parameter varShape",
                                  varShape.length));
        }
        int[] origin = new int[varShape.length];
        int[] shape = new int[varShape.length];
        origin[0] = 0;
        origin[1] = 0;
        shape[0] = varShape[0];
        shape[1] = varShape[1];
        origin[0] = 0;
        shape[0] = varShape[0];
        map.put(origin, shape);
        return map;
    }

    /**
     * Gets the origins and shapes for block 3-dimensions data processing.
     *
     * @param varShape shape of the variable to be extracted
     * @return a map tha contains the shape (blocksize) to extract for each origin
     * @throws MotuException the motu exception
     */
    public static Map<int[], int[]> parseOriginAndShape3DimForWriting(int[] varShape) throws MotuException {

        Map<int[], int[]> map = new HashMap<int[], int[]>();
        if (varShape.length != 3) {
            throw new MotuException(
                    ErrorType.INVALID_LAT_LON_RANGE,
                    String.format("Error in NetCdfWriter.parseOriginAndShape - incorrect dimension %d for parameter varShape", varShape.length));
        }
        for (int i = 0; i < varShape[0]; i++) {
            int[] origin = new int[varShape.length];
            int[] shape = new int[varShape.length];

            origin[0] = i;
            origin[1] = 0;
            origin[2] = 0;
            shape[0] = i;
            shape[1] = varShape[1];
            shape[2] = varShape[2];
            map.put(origin, shape);
        }
        return map;
    }

    /**
     * Gets a unique NetCdf file name (without path).
     * 
     * @param prefix prefix of the file name
     * @return a unique NetCdf file name based on system time.
     */
    public static String getUniqueNetCdfFileName(String prefix) {
        return StringUtils.getUniqueFileName(prefix, NetCdfWriter.NETCDF_FILE_EXTENSION_FINAL);
    }

    /**
     * Getter of the property <tt>amountDataSize</tt>.
     * 
     * @return Returns the amountDataSize.
     * @uml.property name="amountDataSize"
     */
    public double getAmountDataSize() {
        return this.amountDataSize;
    }

    /**
     * Setter of the property <tt>amountDataSize</tt>.
     * 
     * @param amountDataSize The amountDataSize to set.
     * @uml.property name="amountDataSize"
     */
    public void setAmountDataSize(double amountDataSize) {
        this.amountDataSize = amountDataSize;
    }

    public void checkAmountDataSizeThreshold() throws MotuExceedingCapacityException {
        if (getAmountDataSize() > BLLManager.getInstance().getConfigManager().getMotuConfig().getMaxSizePerFileSub().doubleValue()) {
            throw new MotuExceedingCapacityException(
                    BLLManager.getInstance().getConfigManager().getMotuConfig().getMaxSizePerFileSub().doubleValue());
        }
    }

    /**
     * Resets the amountDataSize.
     * 
     */
    public void resetAmountDataSize() {
        setAmountDataSize(0d);
    }

    /**
     * Gets the reading time.
     *
     * @return the reading time
     */
    public long getReadingTime() {
        return readingTime;
    }

    /**
     * Sets the reading time.
     *
     * @param readingTime the new reading time
     */
    public void setReadingTime(long readingTime) {
        this.readingTime = readingTime;
    }

    /**
     * Reset reading time.
     */
    public void resetReadingTime() {
        this.readingTime = 0L;
    }

    /**
     * Gets the writing time.
     *
     * @return the writing time
     */
    public long getWritingTime() {
        return writingTime;
    }

    /**
     * Sets the writing time.
     *
     * @param writingTime the new writing time
     */
    public void setWritingTime(long writingTime) {
        this.writingTime = writingTime;
    }

    /**
     * Reset writing time.
     */
    public void resetWritingTime() {
        this.writingTime = 0L;
    }

    /**
     * sets dimension to a variable.
     * 
     * @param var variable to set the dimension.
     * @param dim dimension to set.
     */
    public static void setDim(Variable var, Dimension dim) {
        List<Dimension> dims = new ArrayList<Dimension>();
        dims.add(dim);
        var.setDimensions(dims);
    }

    /**
     * Read.
     *
     * @param var the var
     * @param origin the origin
     * @param shape the shape
     * @param readingTime the reading time
     * @return the array
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InvalidRangeException the invalid range exception
     */
    public Array read(Variable var, int[] origin, int[] shape) throws IOException, InvalidRangeException {
        long d1 = System.nanoTime();
        Array data = var.read(origin, shape);
        long d2 = System.nanoTime();
        this.readingTime += (d2 - d1);
        return data;
    }

    /**
     * Read.
     *
     * @param var the var
     * @param readingTime the reading time
     * @return the array
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public Array read(Variable var) throws IOException {
        long d1 = System.nanoTime();
        Array data = var.read();
        long d2 = System.nanoTime();
        this.readingTime += (d2 - d1);
        return data;
    }

    public static Array read(Variable var, int[] origin, int[] shape, NetCdfWriter netCdfWriter) throws IOException, InvalidRangeException {
        long d1 = System.nanoTime();
        Array data = var.read(origin, shape);
        long d2 = System.nanoTime();
        if (netCdfWriter != null) {
            netCdfWriter.readingTime += (d2 - d1);
        }
        return data;
    }

    public static Array read(Variable var, NetCdfWriter netCdfWriter) throws IOException {
        long d1 = System.nanoTime();
        Array data = var.read();
        long d2 = System.nanoTime();
        if (netCdfWriter != null) {
            netCdfWriter.readingTime += (d2 - d1);
        }
        return data;
    }

}
// CSON: MultipleStringLiterals
