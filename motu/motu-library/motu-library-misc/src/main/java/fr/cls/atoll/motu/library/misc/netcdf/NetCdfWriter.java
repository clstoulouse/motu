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
package fr.cls.atoll.motu.library.misc.netcdf;

import fr.cls.atoll.motu.library.misc.exception.MotuExceedingCapacityException;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfAttributeNotFoundException;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

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
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants._Coordinate;
import ucar.nc2.dataset.CoordinateAxis;
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
// CSOFF: MultipleStringLiterals : avoid constants and trace duplicate string
public class NetCdfWriter {
    /**
     * Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(NetCdfWriter.class);

    // ////////////////////////////////////////////////////////////////////////////////////
    /**
     * Number of bytes in a kilo-byte.
     */
    public final static int BYTES_IN_MEGABYTES = 1048576;
    /**
     * Number of bytes in a mega-byte.
     */
    public final static int BYTES_IN_KILOBYTES = 1024;

    /** The Constant NETCDF_FILE_EXTENSION_NC. */
    public final static String NETCDF_FILE_EXTENSION_FINAL = ".nc";

    /** The Constant NETCDF_FILE_EXTENSION_EXTRACT. */
    public final static String NETCDF_FILE_EXTENSION_EXTRACT = ".extract";
    /**
     * NetCDF file.
     * 
     * @uml.property name="ncfile"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    protected NetcdfFileWriteable ncfile;

    /**
     * @return the ncfile
     */
    public NetcdfFileWriteable getNcfile() {
        return this.ncfile;
    }

    /**
     * NetCDF file name.
     * 
     * @uml.property name="ncfileName"
     */
    protected String ncfileName;

    /**
     * Dimensions Map .
     * 
     * @uml.property name="dimHash"
     * @uml.associationEnd qualifier="getName:java.lang.String ucar.nc2.Dimension"
     */
    protected Map<String, Dimension> dimHash = new HashMap<String, Dimension>();

    /**
     * Min/Max variable's value Map .
     */
    protected Map<String, MAMath.MinMax> minMaxHash = new HashMap<String, MAMath.MinMax>();

    /**
     * Variable shapes Map .
     */
    // protected Map<String, List<int[]>> shapeHash = new HashMap<String, List<int[]>>();
    /**
     * Map to store output origin from which one's writes data.
     */
    protected Map<String, int[]> originOutOffsetHash = new HashMap<String, int[]>();
    /**
     * Map to store longitude center for normalization.
     */
    protected double longitudeCenter = 0.0;

    /**
     * Constructeur.
     */
    public NetCdfWriter() {
        init();
    }

    /**
     * For writing parts of a NetcdfFile to a new Netcdf-3 local file. To copy all the contents, the static
     * method FileWriter.writeToFile() is preferred. These are mostly convenience methods on top of
     * NetcdfFileWriteable.
     * 
     * @param fileOutName file name to write to.
     * @param fill use fill mode or not
     * @throws IOException
     */
    public NetCdfWriter(String fileOutName, boolean fill) throws IOException {
        init();
        ncfileName = fileOutName;
        ncfile = NetcdfFileWriteable.createNew(fileOutName, fill);
    }

    /**
     * Initialization.
     * 
     */
    private void init() {
        variablesMap = new HashMap<String, List<Variable>>();
    }

    /**
     * List of Variable Map.
     */
    protected Map<String, List<Variable>> variablesMap;

    /**
     * Getter of the property <tt>variables</tt>.
     * 
     * @return Returns the variablesMap.
     */
    public Map<String, List<Variable>> getVariables() {
        return this.variablesMap;
    }

    /**
     * Returns a set view of the keys contained in this map.
     * 
     * @return a set view of the keys contained in this map.
     * @see java.util.Map#keySet()
     */
    public Set<String> variablesKeySet() {
        return this.variablesMap.keySet();
    }

    /**
     * Returns a collection view of the values contained in this map.
     * 
     * @return a collection view of the values contained in this map.
     * @see java.util.Map#values()
     */
    public Collection<List<Variable>> variablesValues() {
        return this.variablesMap.values();
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified key.
     * 
     * @param key key whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map contains a mapping for the specified key.
     * @see java.util.Map#containsKey(Object)
     * @uml.property name="variables"
     */
    public boolean variablesContainsKey(String key) {
        return this.variablesMap.containsKey(key);
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the specified value.
     * 
     * @param value value whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map maps one or more keys to the specified value.
     * @see java.util.Map#containsValue(Object)
     */
    public boolean variablesContainsValue(Variable value) {
        return this.variablesMap.containsValue(value);
    }

    /**
     * Returns the value to which this map maps the specified key.
     * 
     * @param key key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or <tt>null</tt> if the map contains no
     *         mapping for this key.
     * @see java.util.Map#get(Object)
     */
    public List<Variable> getVariables(String key) {
        return this.variablesMap.get(key);
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     * 
     * @return <tt>true</tt> if this map contains no key-value mappings.
     * @see java.util.Map#isEmpty()
     */
    public boolean isVariablesEmpty() {
        return this.variablesMap.isEmpty();
    }

    /**
     * Returns the number of key-value mappings in this map.
     * 
     * @return the number of key-value mappings in this map.
     * @see java.util.Map#size()
     */
    public int variablesSize() {
        return this.variablesMap.size();
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
     * @return previous value associated with specified key, or <tt>null</tt>
     * @see java.util.Map#put(Object,Object)
     */
    public List<Variable> putVariables(String key, List<Variable> value) {
        return this.variablesMap.put(key, value);
    }

    /**
     * Associates the specified value with the specified key in this map (optional operation).
     * 
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or <tt>null</tt>
     * @see java.util.Map#put(Object,Object)
     */
    public List<Variable> putVariables(String key, Variable value) {
        List<Variable> listVar = this.variablesMap.get(key);
        if (listVar == null) {
            listVar = new ArrayList<Variable>();
        }
        listVar.add(value);
        return this.variablesMap.put(key, listVar);
    }

    /**
     * Removes the mapping for this key from this map if it is present (optional operation).
     * 
     * @param key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or <tt>null</tt> if there was no mapping for key.
     * @see java.util.Map#remove(Object)
     */
    public List<Variable> removeVariables(String key) {
        return this.variablesMap.remove(key);
    }

    /**
     * Removes all mappings from this map (optional operation).
     * 
     * @see java.util.Map#clear()
     */
    public void clearVariables() {
        this.variablesMap.clear();
    }

    /**
     * Add a Dimension to the file.
     * 
     * @param dim copy this dimension
     */
    public void putDimension(Dimension dim) {
        if (dimHash.get(dim.getName()) != null) {
            return;
        }
        int length;
        if (dim.isUnlimited()) {
            length = -1;
        } else {
            length = dim.getLength();
        }
        Dimension newDim = ncfile.addDimension(dim.getName(), length, dim.isShared(), dim.isUnlimited(), dim.isVariableLength());
        dimHash.put(newDim.getName(), newDim);
    }

    /**
     * Write a global attribute to the file.
     * 
     * @param att take attribute name, value, from here
     */
    public void writeGlobalAttribute(Attribute att) {
        if (att.isArray()) {
            ncfile.addGlobalAttribute(att.getName(), att.getValues());
        } else if (att.isString()) {
            ncfile.addGlobalAttribute(att.getName(), att.getStringValue());
        } else {
            ncfile.addGlobalAttribute(att.getName(), att.getNumericValue());
        }
    }

    /**
     * Write a Variable attribute to the file.
     * 
     * @param varName name of variable to attach attribute to
     * @param att take attribute name, value, from here
     */
    public void writeAttribute(String varName, Attribute att) {
        if (att.isArray()) {
            ncfile.addVariableAttribute(varName, att.getName(), att.getValues());
        } else if (att.isString()) {
            ncfile.addVariableAttribute(varName, att.getName(), att.getStringValue());
        } else {
            ncfile.addVariableAttribute(varName, att.getName(), att.getNumericValue());
        }
    }

    /**
     * Add a Variable to the file. The data is also copied when finish() is called.
     * 
     * @param var copy this Variable (not the data)
     * @param varAttrToRemove variable attribute to remove
     * @throws MotuException
     */
    public void writeVariable(Variable var, String[] varAttrToRemove) throws MotuException {

        // Dimension[] dims = new Dimension[oldVar.getRank()];
        List<Dimension> dims = new ArrayList<Dimension>();
        List<Dimension> dimvList = var.getDimensions();
        for (Dimension dim : dimvList) {
            Dimension dimToWrite = dimHash.get(dim.getName());
            if (dimToWrite == null) {
                throw new MotuException(String.format("Error in NetCdfWriter writeVariable - Variable %s - Dimension %s must be added first", var
                        .getName(), dim.getName()));

            }
            dims.add(dimToWrite);
        }

        ncfile.addVariable(var.getName(), var.getDataType(), dims);

        boolean removeAttr = false;
        List<Attribute> attributeList = var.getAttributes();
        for (Attribute attribute : attributeList) {
            removeAttr = false;
            if (varAttrToRemove != null) {
                for (String attrToRemove : varAttrToRemove) {
                    if (attrToRemove.equalsIgnoreCase(attribute.getName())) {
                        removeAttr = true;
                        break;
                    }
                }
            }
            if (!removeAttr) {
                writeAttribute(var.getName(), attribute);
            }
        }
    }

    /**
     * Adds and/or sets stantdard axis attribute. Attribures are : valid_min, valid_max, axis, long_name,
     * standard_name.
     * 
     * @param axis axis
     * @param originalVariables the original variables
     */
    private void processAxisAttributes(CoordinateAxis axis, Map<String, Variable> originalVariables) {
        if (axis == null) {
            return;
        }
        if (originalVariables != null) {
            Variable orginalVar = originalVariables.get(axis.getName());
            NetCdfWriter.copyAttributes(orginalVar, axis, true);
        }

        AxisType axisType = axis.getAxisType();
        if (axisType == null) {
            return;
        }

        MAMath.MinMax minMax = minMaxHash.get(axis.getName());

        if (minMax == null) {
            minMax = NetCdfWriter.getMinMaxSkipMissingData(axis, null);
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

        Attribute attribute = null;
        // Array array = null;

        // -----------------------------
        // adds or sets valid_min attribute
        // -----------------------------
        // attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_VALID_MIN, minMax.min);
        Number v = new Double(minMax.min);
        attribute = NetCdfWriter.createAttribute(NetCdfReader.VARIABLEATTRIBUTE_VALID_MIN, axis, v);
        axis.addAttribute(attribute);

        // try {
        // attribute = NetCdfReader.getAttribute(axis, NetCdfReader.VARIABLEATTRIBUTE_VALID_MIN);
        // array = Array.factory( DataType.DOUBLE.getPrimitiveClassType(), new int[] {1}, new double[]
        // {axis.getMinValue()});
        // attribute.setValues(array);
        // } catch (NetCdfAttributeNotFoundException e) {
        // attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_VALID_MIN, axis.getMinValue());
        // axis.addAttribute(attribute);
        // }
        // -----------------------------
        // adds or sets valid_max attribute
        // -----------------------------
        // attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_VALID_MAX, minMax.max);
        v = new Double(minMax.max);
        attribute = NetCdfWriter.createAttribute(NetCdfReader.VARIABLEATTRIBUTE_VALID_MAX, axis, v);
        axis.addAttribute(attribute);
        // try {
        // attribute = NetCdfReader.getAttribute(axis, NetCdfReader.VARIABLEATTRIBUTE_VALID_MAX);
        // array = Array.factory( DataType.DOUBLE.getPrimitiveClassType(), new int[] {1}, new double[]
        // {axis.getMaxValue()});
        // attribute.setValues(array);
        // } catch (NetCdfAttributeNotFoundException e) {
        // attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_VALID_MAX, axis.getMaxValue());
        // axis.addAttribute(attribute);
        // }

        // -----------------------------
        // adds axis attribute
        // -----------------------------
        try {
            attribute = NetCdfReader.getAttribute(axis, NetCdfReader.VARIABLEATTRIBUTE_AXIS);
        } catch (NetCdfAttributeNotFoundException e) {
            attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_AXIS, NetCdfReader.getAxisAttributeValue(axis));
            axis.addAttribute(attribute);
        }
        // -----------------------------
        // adds long name attribute
        // -----------------------------
        try {
            attribute = NetCdfReader.getAttribute(axis, NetCdfReader.VARIABLEATTRIBUTE_LONG_NAME);
        } catch (NetCdfAttributeNotFoundException e) {
            attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_LONG_NAME, NetCdfReader.getLongNameAttributeValue(axis));
            axis.addAttribute(attribute);
        }
        // -----------------------------
        // adds standard name attribute
        // -----------------------------
        try {
            attribute = NetCdfReader.getAttribute(axis, NetCdfReader.VARIABLEATTRIBUTE_STANDARD_NAME);
        } catch (NetCdfAttributeNotFoundException e) {
            attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_STANDARD_NAME, NetCdfReader.getStandardNameAttributeValue(axis));
            axis.addAttribute(attribute);
        }

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
        if (var == null) {
            return;
        }

        Attribute attribute = null;

        // -----------------------------
        // removes valid_min attribute
        // -----------------------------
        try {
            attribute = NetCdfReader.getAttribute(var, NetCdfReader.VARIABLEATTRIBUTE_VALID_MIN);
            var.remove(attribute);
        } catch (NetCdfAttributeNotFoundException e) {
            // Nothing to do
        }
        // -----------------------------
        // removes valid_max attribute
        // -----------------------------
        try {
            attribute = NetCdfReader.getAttribute(var, NetCdfReader.VARIABLEATTRIBUTE_VALID_MAX);
            var.remove(attribute);
        } catch (NetCdfAttributeNotFoundException e) {
            // Nothing to do
        }

    }

    /**
     * 
     * @param var variable to process
     * @param geoGridSubset GeoGrid object (a subset of geoGridOrigin)
     * @param geoGridOrigin GeoGrid object (the origine of the geoGridSubset)
     * @throws MotuNotImplementedException
     * @throws MotuException
     */
    public void computeValidMinMaxVarAttributes(Variable var, GeoGrid geoGridSubset, GeoGrid geoGridOrigin) throws MotuException,
            MotuNotImplementedException {
        MAMath.MinMax minMax = minMaxHash.get(var.getName());

        if (geoGridOrigin != null) {
            minMax = NetCdfWriter.getMinMaxSkipMissingData(geoGridOrigin, var, minMax);
        } else {
            minMax = NetCdfWriter.getMinMaxSkipMissingData(geoGridSubset, var, minMax);
        }
        if (minMax != null) {
            minMaxHash.put(var.getName(), minMax);
        }

    }

    /**
     * 
     * @param axis axis to process
     */
    public void computeValidMinMaxVarAttributes(CoordinateAxis axis) {
        MAMath.MinMax minMax = minMaxHash.get(axis.getName());
        MAMath.MinMax minMaxWork = getMinMaxSkipMissingData(axis, minMax);
        if (minMaxWork != null) {
            minMaxHash.put(axis.getName(), minMaxWork);
        }
    }

    /**
     * Apply scale factor and offset.
     * 
     * @param variable the variable
     * @param value the value
     * 
     * @return the number
     */
    public static Number applyScaleFactorAndOffset(Number value, Variable variable) {

        if (variable == null) {
            return value;
        }

        Number addOffset = NetCdfReader.getAddOffsetAttributeValue(variable);
        Number scaleFactor = NetCdfReader.getScaleFactorAttributeValue(variable);

        if ((addOffset == null) && (scaleFactor == null)) {
            return value;
        }

        double offset = 0.0;
        double scale = 1.0;

        if (addOffset != null) {
            offset = addOffset.doubleValue();
        }
        if (scaleFactor != null) {
            scale = scaleFactor.doubleValue();
        }

        if (scale == 0.0) {
            scale = 1.0;
        }

        return new Double((scale * value.doubleValue()) + offset);

    }

    /**
     * Apply scale factor and offset.
     * 
     * @param minMax the min max
     * @param variable the variable
     */
    public static void applyScaleFactorAndOffset(MAMath.MinMax minMax, Variable variable) {
        if (variable == null) {
            return;
        }

        Number addOffset = NetCdfReader.getAddOffsetAttributeValue(variable);
        Number scaleFactor = NetCdfReader.getScaleFactorAttributeValue(variable);

        if ((addOffset == null) && (scaleFactor == null)) {
            return;
        }

        double offset = 0.0;
        double scale = 1.0;

        if (addOffset != null) {
            offset = addOffset.doubleValue();
        }
        if (scaleFactor != null) {
            scale = scaleFactor.doubleValue();
        }

        if (scale == 0.0) {
            scale = 1.0;
        }

        minMax.min = (scale * minMax.min) + offset;
        minMax.max = (scale * minMax.max) + offset;

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

        if (variable == null) {
            return value;
        }

        Number addOffset = NetCdfReader.getAddOffsetAttributeValue(variable);
        Number scaleFactor = NetCdfReader.getScaleFactorAttributeValue(variable);

        if ((addOffset == null) && (scaleFactor == null)) {
            return value;
        }

        double offset = 0.0;
        double scale = 1.0;

        if (addOffset != null) {
            offset = addOffset.doubleValue();
        }
        if (scaleFactor != null) {
            scale = scaleFactor.doubleValue();
        }

        if (scale == 0.0) {
            scale = 1.0;
        }

        return castValue(variable, (value.doubleValue() - offset) / scale);

    }

    /**
     * Undo scale factor and offset.
     * 
     * @param minMax the min max
     * @param variable the variable
     */
    public static void undoScaleFactorAndOffset(MAMath.MinMax minMax, Variable variable) {
        if (variable == null) {
            return;
        }

        Number addOffset = NetCdfReader.getAddOffsetAttributeValue(variable);
        Number scaleFactor = NetCdfReader.getScaleFactorAttributeValue(variable);

        if ((addOffset == null) && (scaleFactor == null)) {
            return;
        }

        double offset = 0.0;
        double scale = 1.0;

        if (addOffset != null) {
            offset = addOffset.doubleValue();
        }
        if (scaleFactor != null) {
            scale = scaleFactor.doubleValue();
        }

        if (scale == 0.0) {
            scale = 1.0;
        }
        minMax.min = (minMax.min - offset) / scale;
        minMax.max = (minMax.max - offset) / scale;

    }

    /**
     * Adds and/or sets valid_min and valid_max attribute.
     * 
     * @param var variable to process
     */
    public void setValidMinMaxVarAttributes(Variable var) {
        MAMath.MinMax minMax = minMaxHash.get(var.getName());
        if (minMax == null) {
            return;
        }
        setValidMinMaxVarAttributes(var, minMax);
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
        if (var == null) {
            return;
        }

        Attribute attribute = null;
        // Array array = null;
        // -----------------------------
        // adds or sets valid_min attribute
        // -----------------------------
        attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_VALID_MIN, min);
        var.addAttribute(attribute);
        // -----------------------------
        // adds or sets valid_max attribute
        // -----------------------------
        attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_VALID_MAX, max);
        var.addAttribute(attribute);

    }

    /**
     * Compute amount data size.
     * 
     * @param listGeoGridSubset the list geo grid subset
     * 
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     */
    public void computeAmountDataSize(List<GeoGrid> listGeoGridSubset) throws MotuException, MotuNotImplementedException,
            MotuExceedingCapacityException {
        if (listGeoGridSubset == null) {
            throw new MotuException("Error in computeAmountDataSize - list of geogrids is null");
        }
        if (listGeoGridSubset.size() <= 0) {
            throw new MotuException("Error in computeAmountDataSize - list of geoGrids is empty");
        }

        for (GeoGrid geoGridSubset : listGeoGridSubset) {
            if (geoGridSubset == null) {
                throw new MotuException("Error in computeAmountDataSize - geoGrid is null");
            }

            Variable v = geoGridSubset.getVariable();

            amountDataSize += NetCdfWriter.countVarSize(v);
        }

        // if (amountDataSize > Organizer.getMotuConfigInstance().getMaxSizePerFile().doubleValue()) {
        // throw new
        // MotuExceedingCapacityException(Organizer.getMotuConfigInstance().getMaxSizePerFile().doubleValue());
        // }
    }

    /**
     * Adds a list of variables.
     * 
     * @param listVars list of variables
     * @throws MotuException
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

        List<Variable> vPreviouslyAdded = getVariables(var.getName());
        if (vPreviouslyAdded == null) {
            putVariables(var.getName(), var);
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
     * @param gds the grid from which geogrid is derived
     * @param geoGridOrigin GeoGrid object (the origine of the geoGridSubset)
     * @param geoGridSubset GeoGrid object (a subset of geoGridOrigin)
     * @param originalVariables the original variables
     * 
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     */
    public void putVariables(GeoGrid geoGridSubset, GeoGrid geoGridOrigin, GridDataset gds, Map<String, Variable> originalVariables)
            throws MotuException, MotuNotImplementedException, MotuExceedingCapacityException {
        if (geoGridSubset == null) {
            throw new MotuException("Error in writeVariables - geoGrid is null");
        }

        putDimensions(geoGridSubset);

        Variable v = geoGridSubset.getVariable();

        amountDataSize += NetCdfWriter.countVarSize(v);
        if (amountDataSize > Organizer.getMotuConfigInstance().getMaxSizePerFile().doubleValue()) {
            throw new MotuExceedingCapacityException(Organizer.getMotuConfigInstance().getMaxSizePerFile().doubleValue());
        }

        putVariables(v.getName(), v);
        // addShapes(v);

        // Removes valid_min and valid_max attribute from the variables :
        // because their values can't be modified after file is created.
        // the valid_min and valid_max can't be calculated after reading the data
        // of the variable, and it's too late to modify objects (variables, dimensions,
        // attributes) in the NetCdf file (see NetCdfFileWriteable class).
        // To have efficient performance, we don't want to read data variable twice :
        // once before file creation, to compute valid min an valid max value,
        // and once after file creation, to write data in the file.

        // removeValidMinMaxVarAttributes(v);

        // Valid_min and valid_max attribute from the variables :
        // because their values can't be modified after file is created.
        // the valid_min and valid_max can't be calculated after reading the data
        // of the variable, and it's too late to modify objects (variables, dimensions,
        // attributes) in the NetCdf file (see NetCdfFileWriteable class).
        // We have to read data variable twice :
        // once before file creation, to compute valid min an valid max value,
        // and once after file creation, to write data in the file.

        // VariableDS vtemp = (VariableDS)geoGridOrigin.getVariable();
        // DataType dd = vtemp.getOriginalDataType();

        MAMath.MinMax minMax = minMaxHash.get(v.getName());

        if (geoGridOrigin != null) {
            minMax = NetCdfWriter.getMinMaxSkipMissingData(geoGridOrigin, v, minMax);
        } else {
            minMax = NetCdfWriter.getMinMaxSkipMissingData(geoGridSubset, v, minMax);
        }

        if (minMax != null) {
            setValidMinMaxVarAttributes(v, minMax);
            minMaxHash.put(v.getName(), minMax);
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
            List<Variable> vPreviouslyAdded = getVariables(axis.getName());
            if (vPreviouslyAdded != null) {
                continue;
            }
            putVariables(axis.getName(), axis);
            // addShapes(axis);

            processAxisAttributes(axis, originalVariables);
        }

        writeDependentVariables(geoGridSubset, gds);
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
     * @param gds the grid from which geogrid is derived
     * @param geoGridOrigin GeoGrid object (the origin of the geoGridSubsets)
     * @param listGeoGridSubset list of GeoGrid objects (subsets of geoGridOrigin)
     * @param originalVariables the original variables
     * 
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     */
    public void writeVariables(List<GeoGrid> listGeoGridSubset, GeoGrid geoGridOrigin, GridDataset gds, Map<String, Variable> originalVariables)
            throws MotuException, MotuNotImplementedException, MotuExceedingCapacityException {
        if (listGeoGridSubset == null) {
            throw new MotuException("Error in writeVariables - list of geogrids is null");
        }
        if (listGeoGridSubset.size() <= 0) {
            throw new MotuException("Error in writeVariables - list of geoGrids is empty");
        }

        putDimensions(listGeoGridSubset);

        Map<AxisType, List<Variable>> mapAxis = new HashMap<AxisType, List<Variable>>();

        for (GeoGrid geoGridSubset : listGeoGridSubset) {
            if (geoGridSubset == null) {
                throw new MotuException("Error in writeVariables - geoGrid is null");
            }

            Variable v = geoGridSubset.getVariable();

            amountDataSize += NetCdfWriter.countVarSize(v);
            if (amountDataSize > Organizer.getMotuConfigInstance().getMaxSizePerFile().doubleValue()) {
                throw new MotuExceedingCapacityException(Organizer.getMotuConfigInstance().getMaxSizePerFile().doubleValue());
            }

            putVariables(v.getName(), v);
            // addShapes(v);

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

            // Valid_min and valid_max attribute from the variables :
            // because their values can't be modified after file is created.
            // the valid_min and valid_max can't be calculated after reading the data
            // of the variable, and it's too late to modify objects (variables, dimensions,
            // attributes) in the NetCdf file (see NetCdfFileWriteable class).
            // We have to read data variable twice :
            // once before file creation, to compute valid min an valid max value,
            // and once after file creation, to write data in the file.

            // VariableDS vtemp = (VariableDS)geoGridOrigin.getVariable();
            // DataType dd = vtemp.getOriginalDataType();

            // Begin Modif : 21/11/07 : Don't compute MinMax for variable that are not a dimension
            // Uncomment the line below to comptute MinMax
            // computeValidMinMaxVarAttributes(v, geoGridSubset, geoGridOrigin);
            // End Modif : 21/11/07

            //          
            // MAMath.MinMax minMax = minMaxHash.get(v.getName());
            //
            // if (geoGridOrigin != null) {
            // minMax = NetCdfWriter.getMinMaxSkipMissingData(geoGridOrigin, v, minMax);
            // } else {
            // minMax = NetCdfWriter.getMinMaxSkipMissingData(geoGridSubset, v, minMax);
            // }
            //
            // if (minMax != null) {
            // setValidMinMaxVarAttributes(v, minMax);
            // minMaxHash.put(v.getName(), minMax);
            // } else {
            // removeValidMinMaxVarAttributes(v);
            // }

            CoordinateAxis axis = null;

            // NetCDF 2.2.16
            // ArrayList axes = geoGrid.getCoordinateSystem().getCoordinateAxes();
            // NetCDF 2.2.18
            List<CoordinateAxis> axes = geoGridSubset.getCoordinateSystem().getCoordinateAxes();

            // Add axes as variable
            for (int i = 0; i < axes.size(); i++) {
                axis = axes.get(i);

                computeValidMinMaxVarAttributes(axis);

                AxisType axisType = axis.getAxisType();

                // // To bypass a bug in Netcdf-java 4.0.31 : axis.getAxisType() returns null.
                // // This bug will normally fixed in the next Netcdf-java version 4.0.32
                // if (axisType == null) {
                // List<CoordinateAxis> axesOrigin = geoGridOrigin.getCoordinateSystem().getCoordinateAxes();
                // for (CoordinateAxis axisOrigin : axesOrigin) {
                // if (axisOrigin.getName() != axis.getName()) {
                // continue;
                // }
                // axisType = axisOrigin.getAxisType();
                // break;
                // }
                // }
                // // END To bypass a bug in Netcdf 4.0.31
                // if (axisType == null) {
                // throw new
                // MotuException(String.format("ERROR - axisType is null (coordinate axis '%s') - (NetCdfWriter.writeVariables)",
                // axis.getName()));
                // }

                List<Variable> listAxis = mapAxis.get(axisType);
                if (listAxis == null) {
                    listAxis = new ArrayList<Variable>();
                    listAxis.add(axis);
                } else if (axisType == AxisType.Lon) {
                    listAxis.add(axis);
                }
                mapAxis.put(axisType, listAxis);
                // addShapes(axis);

                // processAxisAttributes(axis);
            }

            writeDependentVariables(geoGridSubset, gds);

        } // end for (GeoGrid geoGridSubset : listGeoGridSubset)

        // process axis for the variable
        for (List<Variable> listAxis : mapAxis.values()) {
            if (listAxis.size() <= 0) {
                continue;
            }

            List<Variable> vPreviouslyListAdded = getVariables(listAxis.get(0).getName());
            if (vPreviouslyListAdded != null) {
                continue;
            }
            putVariables(listAxis.get(0).getName(), listAxis);

            for (Variable var : listAxis) {
                if (var instanceof CoordinateAxis) {
                    CoordinateAxis axis = (CoordinateAxis) var;
                    processAxisAttributes(axis, originalVariables);
                }
            }
        }
        List<Variable> listVar = getVariables(listGeoGridSubset.get(0).getVariable().getName());
        for (Variable var : listVar) {
            if (var instanceof CoordinateAxis) {
                continue;
            }
            setValidMinMaxVarAttributes(var);
        }
    }

    /**
     * Adds variable's shape to map of shapes.
     * 
     * @param var variable whose shape have to be added
     */
    // public void addShapes(Variable var) {
    // List<int[]> shapes = shapeHash.get(var.getName());
    // if (shapes == null) {
    // shapes = new ArrayList<int[]>();
    // shapeHash.put(var.getName(), shapes);
    // }
    // shapes.add(var.getShape());
    //
    // }
    // public void writeVariablesContainer(Map<String, Variable> listVars, GeoGrid geoGridSubset, GeoGrid
    // geoGridOrigin, GridDataset gds)
    // throws MotuException, MotuNotImplementedException {
    // if (geoGridSubset == null) {
    // throw new MotuException("Error in writeVariables - geoGrid is null");
    // }
    //
    // writeDimensions(geoGridSubset);
    //
    // Variable v = (Variable) geoGridSubset.getVariable();
    // Variable varContainer = listVars.get(v.getName());
    // putVariables(varContainer.getName(), varContainer);
    // // Removes valid_min and valid_max attribute from the variables :
    // // because their value can't be modify after file is created.
    // // the valid_min and valid_max cant be calculate after reading the data
    // // of the variable, and it's too late to modify objects (variables, dimensions,
    // // attributes) in the NetCdf file (see NetCdfFileWriteable cllas).
    // // To have efficient performance, we don't xwant to read data varaible twice :
    // // once before file creation, to compute valid min an valid max value,
    // // and once after file creation, to write data in the file.
    // // removeValidMinMaxVarAttributes(v);
    //
    // MAMath.MinMax minMax = null;
    // if (geoGridOrigin != null) {
    // minMax = getMinMaxSkipMissingData(geoGridOrigin, v);
    // } else {
    // minMax = getMinMaxSkipMissingData(geoGridSubset, v);
    // }
    //
    // if (minMax != null) {
    // setValidMinMaxVarAttributes(v, minMax);
    // } else {
    // removeValidMinMaxVarAttributes(v);
    // }
    //
    // CoordinateAxis axis = null;
    //
    // // NetCDF 2.2.16
    // // ArrayList axes = geoGrid.getCoordinateSystem().getCoordinateAxes();
    // // NetCDF 2.2.18
    // List axes = geoGridSubset.getCoordinateSystem().getCoordinateAxes();
    //
    // for (int i = 0; i < axes.size(); i++) {
    // axis = (CoordinateAxis) axes.get(i);
    // Variable vPreviouslyAdded = getVariables(axis.getName());
    // if (vPreviouslyAdded != null) {
    // continue;
    // }
    // putVariables(axis.getName(), axis);
    // processAxisAttributes(axis);
    // }
    //
    // writeDependentVariables(geoGridSubset, gds);
    // }
    // /**
    // * Adds Coordinate Variables to the file from Dimension object. The data is also copied when finish() is
    // * called.
    // *
    // * @param dim dimension that referenced coordinate variables
    // * @throws MotuException
    // */
    // @SuppressWarnings("unchecked")
    // public void writeVariables(Dimension dim) {
    //
    // List<Variable> listDimCoordVars = (List<Variable>) dim.getCoordinateVariables();
    //
    // Variable v = null;
    //
    // for (int i = 0; i < listDimCoordVars.size(); i++) {
    // v = listDimCoordVars.get(i);
    // Variable vPreviouslyAdded = getVariables(v.getName());
    // if (vPreviouslyAdded != null) {
    // continue;
    // }
    // putVariables(v.getName(), v);
    // if (v instanceof CoordinateAxis) {
    // CoordinateAxis axis = (CoordinateAxis) v;
    // processAxisAttributes(axis);
    // }
    // }
    // }
    /**
     * Writes variables that depend of the geogrid variable.
     * 
     * @param geoGrid GeoGrid object
     * @param gds the grid from which geoGrid is derived
     * @throws MotuException
     * @throws MotuNotImplementedException
     */
    public void writeDependentVariables(GeoGrid geoGrid, GridDataset gds) throws MotuException, MotuNotImplementedException {
        Variable v = geoGrid.getVariable();
        writeDependentVariables(v, gds);
    }

    /**
     * Writes variables that depend of the geogrid variable.
     * 
     * @param v variable which can have dependent variables
     * @param gds the grid from which geoGrid is derived
     * @throws MotuException
     * @throws MotuNotImplementedException
     */
    public void writeDependentVariables(Variable v, GridDataset gds) throws MotuException, MotuNotImplementedException {

        try {
            Attribute attribute = NetCdfReader.getAttribute(v, _Coordinate.Systems);
            String[] listCoordinateSystems = attribute.getStringValue().split(" ");
            for (String coordinateSystem : listCoordinateSystems) {
                String coordinateSystemTrimmed = coordinateSystem.trim();
                if (coordinateSystemTrimmed.length() <= 0) {
                    continue;
                }
                Variable varCoordSystem = (Variable) gds.getDataVariable(coordinateSystemTrimmed);
                if (varCoordSystem != null) {
                    putVariables(varCoordSystem.getName(), varCoordSystem);
                    // addShapes(varCoordSystem);
                } else {
                    throw new MotuException(String
                            .format("Error in NetCdfWriter - writeDependentVariables - variable %s representing coordinate system for %s not found",
                                    coordinateSystemTrimmed,
                                    v.getName()));

                }
            }
        } catch (NetCdfAttributeNotFoundException e) {
            // Nothing to do
        }

    }

    // public void writeVariables(NetcdfDataset netCdfDataset) throws MotuException,
    // MotuNotImplementedException {
    // if (netCdfDataset == null) {
    // throw new MotuException("Error in writeVariables - netCdfDataset is null");
    // }
    //
    // writeDimensions(netCdfDataset);
    //
    // // List<Variable> listVars = (List<Variable>) netCdfDataset.getVariables();
    // List<Variable> listVars = new ArrayList<Variable>();
    // Variable varFound = netCdfDataset.findTopVariable("latitude");
    // listVars.add(varFound);
    // varFound = netCdfDataset.findTopVariable("longitude");
    // listVars.add(varFound);
    // varFound = netCdfDataset.findTopVariable("ssh");
    // listVars.add(varFound);
    // varFound = netCdfDataset.findTopVariable("LatLonCoordinateSystem");
    // listVars.add(varFound);
    // varFound = netCdfDataset.findTopVariable("ProjectionCoordinateSystem");
    // listVars.add(varFound);
    // for (Variable v : listVars) {
    // putVariables(v.getName(), v);
    // // Removes valid_min and valid_max attribute from the variables :
    // // because their value can't be modify after file is created.
    // // the valid_min and valid_max cant be calculate after reading the data
    // // of the variable, and it's too late to modify objects (variables, dimensions,
    // // attributes) in the NetCdf file (see NetCdfFileWriteable cllas).
    // // To have efficient performance, we don't xwant to read data varaible twice :
    // // once before file creation, to compute valid min an valid max value,
    // // and once after file creation, to write data in the file.
    // // removeValidMinMaxVarAttributes(v);
    //
    // // MAMath.MinMax minMax = getMinMaxSkipMissingData(geoGrid, v);
    // // if (minMax != null) {
    // // setValidMinMaxVarAttributes(v, minMax);
    // // } else {
    // // removeValidMinMaxVarAttributes(v);
    // // }
    //
    // CoordinateAxis axis = null;
    //
    // // NetCDF 2.2.16
    // // ArrayList axes = geoGrid.getCoordinateSystem().getCoordinateAxes();
    // // NetCDF 2.2.18
    // List<CoordinateSystem> listCoordinateSystems = (List<CoordinateSystem>)
    // netCdfDataset.getCoordinateSystems();
    //
    // for (CoordinateSystem cs : listCoordinateSystems) {
    // List axes = cs.getCoordinateAxes();
    //
    // for (int i = 0; i < axes.size(); i++) {
    // axis = (CoordinateAxis) axes.get(i);
    // Variable vPreviouslyAdded = getVariables(axis.getName());
    // if (vPreviouslyAdded != null) {
    // continue;
    // }
    // putVariables(axis.getName(), axis);
    // processAxisAttributes(axis);
    // }
    // }
    // }
    // }

    /**
     * Adds a list of dimension contained in a GeoGrid object to the file. The data will be copied when
     * finish() is called.
     * 
     * @param listDims lsit of Dimension to add
     * @throws MotuException
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
     * @throws MotuException
     */
    public void putDimensions(GeoGrid geoGrid) throws MotuException {
        if (geoGrid == null) {
            throw new MotuException("Error in writeDimensions - geoGrid is null");
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
     * @throws MotuException
     */
    public void putDimensions(List<GeoGrid> listGeoGrid) throws MotuException {
        if (listGeoGrid == null) {
            throw new MotuException("Error in writeDimensions - list of geoGrids is null");
        }
        if (listGeoGrid.size() <= 0) {
            throw new MotuException("Error in writeDimensions - list of geoGrids is empty");
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
                computedXDim = new Dimension(xDim.getName(), xDim);
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

    // @SuppressWarnings("unchecked")
    // public void writeDimensions(NetcdfDataset netCdfDataset) throws MotuException {
    // if (netCdfDataset == null) {
    // throw new MotuException("Error in writeDimensions - netCdfDataset is null");
    // }
    //
    // List<Dimension> listDims = (List<Dimension>) netCdfDataset.getDimensions();
    // writeDimensions(listDims);
    // }

    /**
     * Add a the list of Variables to the file. The data is also copied when finish() is called.
     * 
     * @param varAttrToRemove variable attribute to remove
     * @return amount in Megabytes of the data to be written
     * @throws MotuException
     */
    public double writeVariablesToFile(String[] varAttrToRemove) throws MotuException, MotuNotImplementedException {

        // double countDatasize = 0.0;
        amountDataSize = 0.0;
        for (List<Variable> listVar : variablesMap.values()) {
            if (listVar.size() <= 0) {
                continue;
            }
            for (Variable v : listVar) {
                amountDataSize += NetCdfWriter.countVarSize(v);
                if (amountDataSize > Organizer.getMotuConfigInstance().getMaxSizePerFile().doubleValue()) {
                    break;
                }

            }
            writeVariable(listVar.get(0), varAttrToRemove);
        }
        return amountDataSize;
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
     * @throws MotuException
     * @throws MotuExceedingCapacityException
     * @throws MotuNotImplementedException
     */
    public void create(String[] varAttrToRemove) throws MotuException, MotuExceedingCapacityException, MotuNotImplementedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("create() - entering");
        }

        // Add variables to file.
        // amountDataSize = writeVariables(varAttrToRemove);
        writeVariablesToFile(varAttrToRemove);
        if (amountDataSize > Organizer.getMotuConfigInstance().getMaxSizePerFile().doubleValue()) {
            throw new MotuExceedingCapacityException(Organizer.getMotuConfigInstance().getMaxSizePerFile().doubleValue());
        }

        try {
            ncfile.create();
        } catch (Exception e) {
            LOG.error("create()", e);

            throw new MotuException("Error in NetcdfWriter create", e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("create() - exiting");
        }
    }

    /**
     * Call this when all attributes, dimensions, and variables have been added. The data from all Variables
     * will be written to the file. You cannot add any other attributes, dimensions, or variables after this
     * call.
     * 
     * @param varAttrToRemove variable attribute to remove
     * @throws MotuException
     * @throws MotuExceedingCapacityException
     * @throws MotuNotImplementedException
     */
    public void finish(String[] varAttrToRemove) throws MotuException, MotuExceedingCapacityException, MotuNotImplementedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("finish() - entering");
        }

        // Add variables to file and create it.
        create(varAttrToRemove);

        try {
            for (List<Variable> listVar : variablesMap.values()) {
                longitudeCenter = 0.0;
                for (Variable var : listVar) {
                    // List<int[]> listShapes = shapeHash.get(var.getName());
                    // if (listShapes == null) {
                    // throw new MotuException(String.format("ERROR in NetCdfWriter writeVariablesToFile -
                    // Unable to find shapes for variable %s",
                    // var.getName()));
                    // }
                    writeVariableByBlock(var);
                    // if (NetCdfWriter.isReadByBlock(var)) {
                    // writeVariableByBlock(var);
                    // } else {
                    // writeVariableInOneGulp(var);
                    // }

                    // for (int[] shape : listShapes) {
                    // writeVariableByBlock(var, shape);
                    // }

                }
            }

            ncfile.close();
        } catch (Exception e) {
            LOG.error("finish()", e);

            throw new MotuException("Error in NetcdfWriter finish", e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("finish() - exiting");
        }
    }

    /**
     * Writes variable data in one gulp. It reads the variable data by block and writes each block data in the
     * netcdf file.
     * 
     * @param var variable to be written
     * @throws MotuException
     * @throws MotuNotImplementedException
     */
    // protected void writeVariableByBlock(Variable var) throws MotuException, MotuNotImplementedException {
    // writeVariableByBlock(var, var.getShape());
    // }
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
            Dimension outDim = dimHash.get(var.getDimension(i).getName());
            if (outDim == null) {
                continue;
            }
            outDimValues[i] = outDim.getLength();
        }
        return outDimValues;
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
     * @throws MotuException
     * @throws MotuNotImplementedException
     */
    // protected void writeVariableByBlock(Variable var, int[] varShape) throws MotuException,
    // MotuNotImplementedException {
    protected void writeVariableByBlock(Variable var) throws MotuException, MotuNotImplementedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("writeVariableByBlock() - entering");
        }

        int[] origin = null;
        int[] shape = null;
        Array data = null;
        int rank = var.getRank();
        // int[] outDimValues = getOutputDimension(var);
        CoordinateAxis axisLon = null;
        if (var instanceof CoordinateAxis) {
            axisLon = (CoordinateAxis) var;
            if (axisLon.getAxisType() != AxisType.Lon) {
                axisLon = null;
            }
        }
        int[] originOutOffset = originOutOffsetHash.get(var.getName());
        if (originOutOffset == null) {
            originOutOffset = new int[rank];
            for (int i = 0; i < rank; i++) {
                originOutOffset[i] = 0;
            }
            originOutOffsetHash.put(var.getName(), originOutOffset);
        }

        int[] originMax = new int[rank];
        for (int i = 0; i < rank; i++) {
            originMax[i] = 0;
        }

        try {
            // Map<int[], int[]> originAndShape = NetCdfWriter.parseOriginAndShape(varShape,
            // var.getDataType());
            Map<int[], int[]> originAndShape = NetCdfWriter.parseOriginAndShape(var);

            // CSOFF: StrictDuplicateCode : normal duplication code.

            Set<int[]> keySet = originAndShape.keySet();

            for (Iterator<int[]> it = keySet.iterator(); it.hasNext();) {
                // get a Runtime object
                // Runtime r = Runtime.getRuntime();
                // run the garbage collector, to avoid Socket Exception too many files opened ?
                // r.gc();

                origin = it.next();

                if (origin == null) {
                    throw new MotuException("Error in NetCfdWriter finish - unable to find origin - (origin is null)");
                }

                shape = originAndShape.get(origin);

                if (shape == null) {
                    throw new MotuException("Error in NetCfdWriter finish - unable to find shape - (shape is null)");
                }
                // CSOON: StrictDuplicateCode

                data = var.read(origin, shape);
                // Normalize longitude if necessary.
                if (axisLon != null) {
                    MAMath.MinMax minMax = minMaxHash.get(axisLon.getName());

                    MAMath.MinMax minMaxTemp = new MAMath.MinMax(minMax.min, minMax.max);
                    NetCdfWriter.applyScaleFactorAndOffset(minMaxTemp, axisLon);

                    if ((minMaxTemp.min > minMaxTemp.max) || (minMaxTemp.max > 180.)) {
                        longitudeCenter = minMaxTemp.min + 180.0;
                        normalizeLongitudeData(data, axisLon);
                    }
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
            // int indexDimLongitude =
            // for (int i = rank - 1; i >= 0; i--) {
            // originOut[i] += shape[i] - 1;
            // }
            originOutOffset = getNextOriginOffset(originOutOffset, var);
            originOutOffsetHash.remove(var.getName());
            if (originOutOffset != null) {
                originOutOffsetHash.put(var.getName(), originOutOffset);
            }

        } catch (IOException e) {
            LOG.error("writeVariableByBlock()", e);

            throw new MotuException("Error IOException in NetcdfWriter writeVariableByBlock", e);
        } catch (InvalidRangeException e) {
            LOG.error("writeVariableByBlock()", e);

            throw new MotuException("Error InvalidRangeException in NetcdfWriter writeVariableByBlock", e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("writeVariableByBlock() - exiting");
        }
    }

    /**
     * Puts longitude into the range [center +/- 180] deg. Center correspond to the class attribute
     * {@link #longitudeCenter}
     * 
     * @param data data to normalize
     * @param variable the variable
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
     * {@link #longitudeCenter}
     * 
     * @param data data to normalize
     * @param variable the variable
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
     * {@link #longitudeCenter}
     * 
     * @param data data to normalize
     * @param variable the variable
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
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
            throw new MotuNotImplementedException(String.format("Error in NetcdfWriter normalizeLongitudeData - Array type %s is not implemented",
                                                                data.getClass().getName()));

        }

    }

    /**
     * Writes variable data in one gulp. It reads all the variable data in memory and writes them in the
     * netcdf file.
     * 
     * @param var variable to be written
     * @throws MotuException
     */
    protected void writeVariableInOneGulp(Variable var) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("writeVariableInOneGulp() - entering");
        }

        Array data = null;
        try {
            data = var.read();
        } catch (IOException e) {
            LOG.error("writeVariableInOneGulp()", e);

            throw new MotuException("Error in NetcdfWriter writeVariableInOneGulp", e);
        }

        writeVariableData(var, data);

        if (LOG.isDebugEnabled()) {
            LOG.debug("writeVariableInOneGulp() - exiting");
        }
    }

    /**
     * Writes variable data in one gulp. It writes data in the netcdf file.
     * 
     * @param var variable to be written
     * @param data data to be written
     * @throws MotuException
     */
    public void writeVariableData(Variable var, Array data) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("writeVariableData() - entering");
        }

        try {
            if (data != null) {
                ncfile.write(var.getName(), data);
                ncfile.flush();
            }
        } catch (Exception e) {
            LOG.error("writeVariableData()", e);

            throw new MotuException("Error in NetcdfWriter writeVariableData", e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("writeVariableData() - exiting");
        }
    }

    /**
     * Writes variable data in one gulp. It writes data in the netcdf file.
     * 
     * @param var variable to be written
     * @param origin origin of the read data
     * @param data data to be written
     * @throws MotuException
     */
    public void writeVariableData(Variable var, int[] origin, Array data) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("writeVariableData() - entering");
        }

        int rank = var.getRank();

        int[] originOutOffset = originOutOffsetHash.get(var.getName());
        if (originOutOffset == null) {
            originOutOffset = new int[rank];
            for (int i = 0; i < rank; i++) {
                originOutOffset[i] = 0;
            }
        }
        int[] originOut = originOutOffset.clone();

        for (int i = 0; i < rank; i++) {
            originOut[i] += origin[i];
        }
        try {
            if (data != null) {
                ncfile.write(var.getName(), originOut, data);
                ncfile.flush();
            }
        } catch (Exception e) {
            LOG.error("writeVariableData()", e);

            throw new MotuException("Error in NetcdfWriter writeVariableData", e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("writeVariableData() - exiting");
        }
    }

    /**
     * Gets if data have to be read by block.
     * 
     * @param var variable to process.
     * @return true is data have to be read by block, otherwise false.
     * @throws MotuException
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
     * @throws MotuException
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
     * @return both min and max value.
     * @throws MotuException
     * @throws MotuNotImplementedException
     */

    public static MAMath.MinMax getMinMaxSkipMissingData(GeoGrid geoGrid, Variable var, MAMath.MinMax minMax) throws MotuException,
            MotuNotImplementedException {

        MAMath.MinMax minMaxWork = NetCdfWriter.getMinMaxSkipMissingData(geoGrid, var);

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

        if (!vs.hasMissing()) {
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
            if (isMissing) {
                continue;
            }
            if (val > max) {
                max = val;
            }
            if (val < min) {
                min = val;
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
     * @return both min and max value.
     * @throws MotuException
     * @throws MotuNotImplementedException
     */

    public static MAMath.MinMax getMinMaxSkipMissingData(GeoGrid geoGrid, Variable var) throws MotuException, MotuNotImplementedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMinMaxSkipMissingData() - entering");
        }

        MAMath.MinMax minMax = null;
        try {
            if (!NetCdfWriter.isReadByBlock(var)) {
                Array data = var.read();
                // Don't use var but geoGrid.getVariable() to get missing/fill value.
                // minMax = geoGrid.getMinMaxSkipMissingData(data);
                minMax = NetCdfWriter.getMinMaxSkipMissingData(data, geoGrid.getVariable());
            } else {
                minMax = NetCdfWriter.getMinMaxSkipMissingDataByBlock(geoGrid, var);
            }
        } catch (IOException e) {
            LOG.error("getMinMaxSkipMissingData()", e);
            throw new MotuException(String.format("I/O Error in NetcdfWriter getMinMaxSkipMissingData - Variable %s ", var.getName()), e);
        }

        // If all values are missing data, then min is greater to max
        // No MinMax --> return null;
        if (minMax.min > minMax.max) {
            minMax = null;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getMinMaxSkipMissingData() - exiting");
        }
        return minMax;
    }

    /**
     * Get the minimum and the maximum data value of the variabble, skipping missing values as defined by
     * missing_value attribute of the Coordinate axis.
     * 
     * @param axis coordinate axis to process.
     * @param minMax previous min/max of the variable
     * @return both min and max value.
     */
    public static MAMath.MinMax getMinMaxSkipMissingData(CoordinateAxis axis, MAMath.MinMax minMax) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMinMaxSkipMissingData() - entering");
        }

        MAMath.MinMax minMaxWork = new MAMath.MinMax(Double.MAX_VALUE, -Double.MAX_VALUE);

        if (!axis.hasMissing()) {
            minMaxWork.min = axis.getMinValue();
            minMaxWork.max = axis.getMaxValue();
        } else {
            try {
                Array data = axis.read();
                minMaxWork = NetCdfWriter.getMinMaxSkipMissingData(data, axis, false);
            } catch (IOException ioe) { /* what ?? */
            }
        }

        if (minMax == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getMinMaxSkipMissingData() - exiting");
            }
            return minMaxWork;
        }

        // Special for Longitude
        // this is not the first part (minMax != null)
        // Normalize longitude with first part min value (as center longitude)
        if (axis.getAxisType() == AxisType.Lon) {
            minMaxWork.min = minMax.min;
            double center = ((minMax.min != 0.) ? minMax.min : minMax.max);
            minMaxWork.max = LatLonPointImpl.lonNormal(minMaxWork.max, center);
        } else {

            if (minMax.min < minMaxWork.min) {
                minMaxWork.min = minMax.min;
            }
            if (minMax.max > minMaxWork.max) {
                minMaxWork.max = minMax.max;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getMinMaxSkipMissingData() - exiting");
        }
        return minMaxWork;
    }

    /**
     * Get the minimum and the maximum data value of the variable, skipping missing values as defined by
     * missing_value attribute of the variable. Variable is read by block.
     * 
     * @param geoGrid GeoGrid object that contains the variable
     * @param var variable to process.
     * @return both min and max value.
     * @throws MotuException
     * @throws MotuNotImplementedException
     */
    public static MAMath.MinMax getMinMaxSkipMissingDataByBlock(GeoGrid geoGrid, Variable var) throws MotuException, MotuNotImplementedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMinMaxSkipMissingDataByBlock() - entering");
        }

        int[] origin = null;
        int[] shape = null;
        Array data = null;
        MAMath.MinMax minMax = null;
        try {
            Map<int[], int[]> originAndShape = NetCdfWriter.parseOriginAndShape(var);
            Set<int[]> keySet = originAndShape.keySet();

            for (Iterator<int[]> it = keySet.iterator(); it.hasNext();) {
                // get a Runtime object
                // Runtime r = Runtime.getRuntime();
                // run the garbage collector, to avoid Socket Exception too many files opened ?
                // r.gc();

                origin = it.next();

                if (origin == null) {
                    throw new MotuException(String
                            .format("Error in NetCfdWriter getMinMaxSkipMissingDataByBlock - unable to find origin - (origin is null) Variable %s ",
                                    var.getName()));
                }

                shape = originAndShape.get(origin);

                if (shape == null) {
                    throw new MotuException(String
                            .format("Error in NetCfdWriter getMinMaxSkipMissingDataByBlock - unable to find shape - (shape is null)- Variable %s ",
                                    var.getName()));
                }

                data = var.read(origin, shape);
                // Don't use var but geoGrid.getVariable() to get missing/fill value.
                // MAMath.MinMax minMaxTemp = geoGrid.getMinMaxSkipMissingData(data);
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

            throw new MotuException("I/O Error in NetcdfWriter getMinMaxSkipMissingDataByBlock", e);
        } catch (InvalidRangeException e) {
            LOG.error("getMinMaxSkipMissingDataByBlock()", e);

            throw new MotuException("Error in NetcdfWriter getMinMaxSkipMissingDataByBlock", e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getMinMaxSkipMissingDataByBlock() - exiting");
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
     * @throws MotuException
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
     * @throws MotuException
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
     * @throws MotuException
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
        return (NetCdfWriter.countVarElementData(varShape) * (double) byteSize) / BYTES_IN_MEGABYTES;
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
     * @throws MotuException
     */
    public static long countMaxElementData(Variable var) throws MotuException {
        return countMaxElementData(var.getShape(), var.getDataType());
    }

    /**
     * @param datatype variable's data type to process
     * @param listShapes list of variables's shapes to process.
     * @return returns the number of element in the variable, according to the shapes.
     * @throws MotuException
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
     * @throws MotuException
     */
    private static long countMaxElementData(int[] varShape, DataType datatype) throws MotuException {
        if (varShape.length <= 0) {
            // throw new MotuException(String.format("Error in NetCdfWriter.countMaxElementData - incorrect
            // dimension %d for parameter varShape",
            // varShape.length));
            return 0;
        }
        int byteSize = datatype.getSize();
        if (byteSize <= 0) {
            byteSize = 1;
        }

        return (Organizer.getMotuConfigInstance().getDataBlockSize().intValue() * BYTES_IN_KILOBYTES) / (byteSize);
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
     * @throws MotuException
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
     * @throws MotuException
     */
    private static int getElementBlockSize(int[] varShape, DataType datatype) throws MotuException, MotuNotImplementedException {

        double maxDataToUse = countMaxElementData(varShape, datatype);
        double elementBlockSize = 1;

        if ((varShape.length <= 0) || (varShape.length > 4)) {
            throw new MotuNotImplementedException(String
                    .format("Error in  NetCdfWriter.getElementBlockSize - Processing for %d-dimension is not implemented", varShape.length));
        }
        double pow = 1.0 / varShape.length;
        elementBlockSize = Math.pow(maxDataToUse, pow);
        return (int) elementBlockSize;
    }

    /**
     * @param var variable to process
     * @return returns the number of element in the variable.
     */
    private static long countVarElementData(Variable var) {
        return NetCdfWriter.countVarElementData(var.getShape());
    }

    /**
     * @param var variable to process.
     * @param listShapes list of variables's shapes to process.
     * @return returns the number of element in the variable, according to the shapes.
     * @throws MotuException
     */
    public static long countVarElementData(Variable var, List<int[]> listShapes) throws MotuException {
        long count = 0;
        for (int[] shape : listShapes) {
            count += countVarElementData(shape);
        }
        return count;
    }

    /**
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
     * @param dest the dest
     * @param src the src
     */
    public static void copyAttributes(Variable src, Variable dest) {
        NetCdfWriter.copyAttributes(src, dest, true);
    }

    /**
     * Copies attributes of a variable to another variable.
     * 
     * @param dest variable to copy to
     * @param overwrite the overwrite
     * @param src variable to copy from
     */
    public static void copyAttributes(Variable src, Variable dest, boolean overwrite) {

        if ((src == null) || (dest == null)) {
            return;
        }

        List<Attribute> attributes = src.getAttributes();
        for (Attribute att : attributes) {
            if (!overwrite) {
                if (dest.findAttributeIgnoreCase(att.getName()) == null) {
                    continue;
                }
            }
            dest.addAttribute(new Attribute(att.getName(), att));
        }
    }

    /**
     * Gets the origins and shapes for block data reading. The max. size of a block in Ko to be process is in
     * the Motu configuration file (dataBlocksize attribute of MotuConfig)
     * 
     * @param var variable to process
     * @return a map tha contains the shape (blocksize) to extract for each origin
     * @throws MotuException
     * @throws MotuNotImplementedException
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
     * @throws MotuException
     * @throws MotuNotImplementedException
     */
    public static Map<int[], int[]> parseOriginAndShape(int[] varShape, DataType datatype) throws MotuException, MotuNotImplementedException {

        Map<int[], int[]> map = null;

        for (int i = 0; i < varShape.length; i++) {
            if (varShape[i] <= 0) {
                throw new MotuException(String.format("Error in NetCdfWriter.parseOriginAndShape - incorrect value %d for varShape[%d]",
                                                      varShape[i],
                                                      i));
            }
        }
        int nDims = varShape.length;
        int blockElementSize = NetCdfWriter.getElementBlockSize(varShape, datatype);

        if (blockElementSize <= 0) {
            blockElementSize = 1;
        }

        switch (nDims) {
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
            throw new MotuNotImplementedException(String
                    .format("Error in  NetCdfWriter.parseOriginAndShape1Dim - Processing for %d-dimension is not implemented", nDims));
            // break;
        }

        return map;
    }

    /**
     * Gets the origins and shapes for block data writing. The max. size of a block in Ko to be process is in
     * the Motu configuration file (dataBlocksize attribute of MotuConfig)
     * 
     * @param var variable to process
     * @return a map tha contains the shape (blocksize) to extract for each origin
     * @throws MotuException
     * @throws MotuNotImplementedException
     */
    public static Map<int[], int[]> parseOriginAndShapeForWriting(Variable var) throws MotuException, MotuNotImplementedException {

        Map<int[], int[]> map = null;

        int[] varShape = var.getShape();

        for (int i = 0; i < varShape.length; i++) {
            if (varShape[i] <= 0) {
                throw new MotuException(String.format("Error in NetCdfWriter.parseOriginAndShape - incorrect value %d for varShape[%d]",
                                                      varShape[i],
                                                      i));
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
            throw new MotuNotImplementedException(String
                    .format("Error in  NetCdfWriter.parseOriginAndShape1Dim - Processing for %d-dimension is not implemented", nDims));
            // break;
        }

        return map;
    }

    /**
     * Gets the origins and shapes for block 1-dimension data processing.
     * 
     * @param varShape shape of the variable to be extracted
     * @param blockSize the number of elements to be process for each dimension
     * @return a map tha contains the shape (blocksize) to extract for each origin
     * @throws MotuException
     */
    public static Map<int[], int[]> parseOriginAndShape1Dim(int[] varShape, int blockSize) throws MotuException {

        Map<int[], int[]> map = new HashMap<int[], int[]>();
        if (varShape.length != 1) {
            throw new MotuException(String.format("Error in NetCdfWriter.parseOriginAndShape - incorrect dimension %d for parameter varShape",
                                                  varShape.length));
        }

        for (int i = 0; i < varShape[0]; i = i + blockSize) {
            int[] origin = new int[varShape.length];
            int[] shape = new int[varShape.length];

            origin[0] = i;
            shape[0] = Math.min(blockSize, (varShape[0] - i));
            // System.out.println(String.format("i:%d, origin[%d], shape[%d]", i, origin[0], shape[0]));
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
     * @throws MotuException
     */
    public static Map<int[], int[]> parseOriginAndShape2Dim(int[] varShape, int blockSize) throws MotuException {

        Map<int[], int[]> map = new HashMap<int[], int[]>();
        if (varShape.length != 2) {
            throw new MotuException(String.format("Error in NetCdfWriter.parseOriginAndShape - incorrect dimension %d for parameter varShape",
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
                // System.out.println(String.format("i:%d, j:%d, origin[%d, %d], shape[%d,%d]", i, j,
                // origin[0], origin[1], shape[0], shape[1]));
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
     * @throws MotuException
     */
    public static Map<int[], int[]> parseOriginAndShape3Dim(int[] varShape, int blockSize) throws MotuException {

        Map<int[], int[]> map = new HashMap<int[], int[]>();
        if (varShape.length != 3) {
            throw new MotuException(String.format("Error in NetCdfWriter.parseOriginAndShape - incorrect dimension %d for parameter varShape",
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
                    // System.out.println(String.format("i:%d, j:%d, k:%d, origin[%d, %d, %d], shape[%d,%d,
                    // %d]",
                    // i,
                    // j,
                    // k,
                    // origin[0],
                    // origin[1],
                    // origin[2],
                    // shape[0],
                    // shape[1],
                    // shape[2]));
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
     * @throws MotuException
     */
    public static Map<int[], int[]> parseOriginAndShape4Dim(int[] varShape, int blockSize) throws MotuException {

        Map<int[], int[]> map = new HashMap<int[], int[]>();
        if (varShape.length != 4) {
            throw new MotuException(String.format("Error in NetCdfWriter.parseOriginAndShape - incorrect dimension %d for parameter varShape",
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
                        // System.out.println(String.format("i:%d, j:%d, k:%d, l:%d, origin[%d, %d, %d, %d],
                        // shape[%d,%d, %d, %d]",
                        // i,
                        // j,
                        // k,
                        // l,
                        // origin[0],
                        // origin[1],
                        // origin[2],
                        // origin[3],
                        // shape[0],
                        // shape[1],
                        // shape[2],
                        // shape[3]));
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
     * @throws MotuException
     */
    public static Map<int[], int[]> parseOriginAndShape1DimForWriting(int[] varShape) throws MotuException {

        Map<int[], int[]> map = new HashMap<int[], int[]>();
        if (varShape.length != 1) {
            throw new MotuException(String
                    .format("Error in NetCdfWriter.parseOriginAndShape1DimForWriting - incorrect dimension %d for parameter varShape",
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
     * @throws MotuException
     */
    public static Map<int[], int[]> parseOriginAndShape2DimForWriting(int[] varShape) throws MotuException {

        Map<int[], int[]> map = new HashMap<int[], int[]>();
        if (varShape.length != 2) {
            throw new MotuException(String
                    .format("Error in NetCdfWriter.parseOriginAndShape2DimForWriting - incorrect dimension %d for parameter varShape",
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
     * @throws MotuException
     */
    public static Map<int[], int[]> parseOriginAndShape3DimForWriting(int[] varShape) throws MotuException {

        Map<int[], int[]> map = new HashMap<int[], int[]>();
        if (varShape.length != 3) {
            throw new MotuException(String.format("Error in NetCdfWriter.parseOriginAndShape - incorrect dimension %d for parameter varShape",
                                                  varShape.length));
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
            System.out.println(String.format("i:%d, origin[%d, %d, %d], shape[%d,%d,%d]",
                                             i,
                                             origin[0],
                                             origin[1],
                                             origin[2],
                                             shape[0],
                                             shape[1],
                                             shape[2]));
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
        return Organizer.getUniqueFileName(prefix, NetCdfWriter.NETCDF_FILE_EXTENSION_FINAL);
    }

    /**
     * amount of data in Megabytes to be written.
     * 
     * @uml.property name="amountDataSize"
     */
    private double amountDataSize;

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

    /**
     * Resets the amountDataSize.
     * 
     */
    public void resetAmountDataSize() {
        this.amountDataSize = 0d;
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

}
// CSON: MultipleStringLiterals
