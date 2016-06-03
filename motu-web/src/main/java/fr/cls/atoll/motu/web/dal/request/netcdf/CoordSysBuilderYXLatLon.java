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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfAttributeNotFoundException;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants._Coordinate;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dataset.CoordinateTransform;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.ProjectionCT;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dataset.conv.CF1Convention;
import ucar.nc2.util.CancelTask;
import ucar.unidata.geoloc.ProjectionImpl;
import ucar.unidata.geoloc.projection.LatLonProjection;

// CSOFF: MultipleStringLiterals : avoid message in '@SuppressWarnings("unchecked")'.

/**
 * Class to create a Lat/Lon coordinate system from X/Y coordinate.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class CoordSysBuilderYXLatLon extends CF1Convention {

    public static final String LAT_LON_COORDINATE_SYSTEM_PREFIX = "latLonCoordinateSystem";

    /**
     * Default constructor.
     */
    public CoordSysBuilderYXLatLon() {
        debug = true;
    }

    public boolean isAugmented() {
        return isAugmented;
    }

    public void setAugmented(boolean isAugmented) {
        this.isAugmented = isAugmented;
    }

    boolean isAugmented = true;

    /**
     * Makes changes to the dataset, like adding new variables, attribuites, etc.
     * 
     * @param ds modify this dataset
     * @param cancelTask give user a chance to bail out
     * @throws MotuNotImplementedException
     * @see ucar.nc2.dataset.conv.CF1Convention#augmentDataset(ucar.nc2.dataset.NetcdfDataset,
     *      ucar.nc2.util.CancelTask)
     */
    @Override
    public void augmentDataset(NetcdfDataset ds, CancelTask cancelTask) {

        clearCoordinateTransform();

        List<Variable> listVars = ds.getVariables();
        Variable varLat = NetCdfReader.findLatitudeIgnoreCase(listVars);
        Variable varLon = NetCdfReader.findLongitudeIgnoreCase(listVars);

        if ((varLat == null) || (varLon == null)) {
            setAugmented(false);
            return;
        }
        if (!(varLat instanceof VariableDS) || !(varLon instanceof VariableDS)) {
            setAugmented(false);
            return;
        }
        if ((varLat instanceof CoordinateAxis) && (varLon instanceof CoordinateAxis)) {
            addLatAxisTypeAttr(ds, varLat);
            addLonAxisTypeAttr(ds, varLon);
            ds.finish();
            return;
        }

        // add Coordinate Axis Type Attribute
        addLatAxisTypeAttr(ds, varLat);
        addLonAxisTypeAttr(ds, varLon);

        try {
            addCoordinateSystems(ds, (VariableDS) varLat, (VariableDS) varLon);
        } catch (MotuNotImplementedException e) {
            if (cancelTask != null) {
                cancelTask.setError(e.getMessage());
            }
        } catch (MotuException e) {
            if (cancelTask != null) {
                cancelTask.setError(e.getMessage());
            }
        }

        //
        // List<CoordinateAxis> listAxes = (List<CoordinateAxis>)ds.getCoordinateAxes();
        // Variable varGeoX = null;
        // Variable varGeoY = null;
        //
        // for (CoordinateAxis axis : listAxes) {
        // if (axis.getAxisType() == AxisType.GeoX) {
        // varGeoX = axis;
        // }
        // if (axis.getAxisType() == AxisType.GeoY) {
        // varGeoY = axis;
        // }
        // }
        //
        // if ((varGeoX == null) || (varGeoY == null)) {
        // return;
        // }
        // if (!(varGeoX instanceof VariableDS) || !(varGeoY instanceof VariableDS)) {
        // return;
        // }
        //
        //
        // try {
        // addCoordinateSystems(ds, (VariableDS) varLat, (VariableDS) varLon);
        // } catch (MotuNotImplementedException e) {
        // if (cancelTask != null) {
        // cancelTask.setError(e.notifyException());
        // }
        // }

        // ProjectionCT projCT = null;
        //
        // ProjectionImpl proj = new LatLonProjection("LatLonProj");
        //
        // projCT = new ProjectionCT("LatLonCoordinateSystem", "", proj);
        //
        // VariableDS v = makeCoordinateTransformVariable(ds, projCT);
        //
        // ds.addVariable(null, v);
        // String xname = findCoordinateName(ds, AxisType.Lon);
        // String yname = findCoordinateName(ds, AxisType.Lat);
        // if (xname != null && yname != null) {
        // v.addAttribute(new Attribute(_Coordinate.Axes, yname + " " + xname));
        // } else {
        // listVars = (List<Variable>) ds.getVariables();
        // varLat = NetCdfReader.findLatitudeIgnoreCase(listVars);
        // varLon = NetCdfReader.findLongitudeIgnoreCase(listVars);
        // if (varLon != null && varLat != null) {
        // v.addAttribute(new Attribute(_Coordinate.Axes, varLat.getName() + " " + varLon.getName()));
        // } else {
        // v.addAttribute(new Attribute(_Coordinate.Axes, "latitude longitude"));
        // }
        // }
        //
        // // proj = new LatLonProjection("testProj2");
        // // projCT = new ProjectionCT("ProjectionCoordinateSystem", "", proj);
        // // v = makeCoordinateTransformVariable(ds, projCT);
        // // ds.addVariable(null, v);
        // // v.addAttribute(new Attribute(_Coordinate.Axes, "y x"));
        //
        // // String xname = findCoordinateName( ds, AxisType.GeoX);
        // // String yname = findCoordinateName( ds, AxisType.GeoY);
        // // if (xname != null && yname != null) {
        // // v.addAttribute( new Attribute(_Coordinate.Axes, xname+" "+yname));
        // // }
        //
        ds.finish();
    }

    /**
     * Adds Lat/Lon coordinate system to the dataset.
     * 
     * @param ds modify this dataset
     * @param varLat latitude variable of the dataset
     * @param varLon longitude variable of the dataset
     * @throws MotuNotImplementedException
     * @throws MotuException
     */
    public void addCoordinateSystems(NetcdfDataset ds, VariableDS varLat, VariableDS varLon) throws MotuNotImplementedException, MotuException {

        if ((varLat == null) || (varLon == null)) {
            return;
        }
        CoordinateAxis coordLat = CoordinateAxis.factory(null, varLat);
        coordLat.setAxisType(AxisType.Lat);

        CoordinateAxis coordLon = CoordinateAxis.factory(null, varLon);
        coordLon.setAxisType(AxisType.Lon);

        List<CoordinateSystem> listCoordinateSystems = ds.getCoordinateSystems();

        List<CoordinateAxis> listNewAxes = new ArrayList<CoordinateAxis>();

        int i = 0;
        for (CoordinateSystem cs : listCoordinateSystems) {

            listNewAxes.clear();
            listNewAxes.add(coordLat);
            listNewAxes.add(coordLon);

            boolean containsXYAxes = cs.containsAxisType(AxisType.GeoX) && cs.containsAxisType(AxisType.GeoY);
            if (!containsXYAxes) {
                continue;
            }

            List<CoordinateAxis> listOriginAxes = cs.getCoordinateAxes();
            for (CoordinateAxis axis : listOriginAxes) {
                if ((axis.getAxisType() == AxisType.GeoX) || (axis.getAxisType() == AxisType.GeoY)) {
                    continue;
                }
                if ((axis.getAxisType() == AxisType.Lat) || (axis.getAxisType() == AxisType.Lon)) {
                    continue;
                }
                listNewAxes.add(axis);
            }
            i++;
            String coordTransName = String.format("latLonCoordinateSystem%d", i);
            addCoordinateTransformVariable(ds, coordTransName, listNewAxes, listOriginAxes);
            //
            // coordTransName = String.format("ProjectionCoordinateSystem%d", i);
            // addCoordinateTransformVariable(ds, coordTransName, listOriginAxes, listOriginAxes);

        }

        // for (VariableDS v : coordinateTransformValues()) {
        // System.out.print("Transform Variable name and dims: ");
        // System.out.println(v.getNameAndDimensions());
        // System.out.print("\tisCaching:\t");
        // System.out.print(v.isCaching());
        // try {
        // System.out.print("\t" + _Coordinate.Axes + ":\t");
        // System.out.println(NetCdfReader.getAttribute(v, _Coordinate.Axes).getStringValue());
        // } catch (NetCdfAttributeNotFoundException e) {
        // e.printStackTrace();
        // }
        // }
    }

    /**
     * Adds coordinate system to the variable of the dataset.
     * 
     * @param ds modify this dataset
     * @param coordTransName coordinate systèem variable name
     * @param listAxes Axes (Variables) of the new coordinate system.
     * @throws MotuNotImplementedException
     * @throws MotuException
     */
    private void addCoordinateSystemsAttr(NetcdfDataset ds, String coordTransName, List<CoordinateAxis> listAxes)
            throws MotuNotImplementedException, MotuException {

        List<Variable> listVars = ds.getVariables();
        List<Variable> listCoordVars = null;

        for (Variable var : listVars) {
            if (var.isCaching()) {
                continue;
            }
            if (var instanceof CoordinateAxis) {
                continue;
            }
            Attribute axisTypeAttr = null;
            try {
                axisTypeAttr = NetCdfReader.getAttribute(var, _Coordinate.AxisType);
            } catch (NetCdfAttributeNotFoundException e) {
                // Do nothing;
            }

            if (axisTypeAttr != null) {
                continue;
            }

            listCoordVars = NetCdfReader.getCoordinateVariables(var, ds);
            // int count = listCoordVars.size();
            // if (count != 1) {
            // throw new MotuNotImplementedException(
            // String
            // .format("ERROR - in CoordSysBuilderYXLatLon - addCoordinateSystemsAttr - dimension with %d
            // coordinate variable(s) is not implemented (variable name:%s)",
            // count,
            // var.getName()));
            // }

            Attribute attribute = null;
            StringBuffer stringBuffer = new StringBuffer();
            boolean containsAll = NetCdfReader.containsAll(listAxes, listCoordVars);
            if (containsAll) {
                try {
                    attribute = NetCdfReader.getAttribute(var, _Coordinate.Systems);
                    stringBuffer.append(attribute.getStringValue());
                    stringBuffer.append(" ");
                    stringBuffer.append(coordTransName);
                    var.addAttribute(new Attribute(_Coordinate.Systems, stringBuffer.toString()));
                } catch (NetCdfAttributeNotFoundException e) {
                    var.addAttribute(new Attribute(_Coordinate.Systems, coordTransName));
                }
            }
        }

    }

    /**
     * Adds the coordinate transform variable to the dataset.
     * 
     * @param ds modify this dataset
     * @param coordTransName coordinate systèem variable name
     * @param listNewAxes Axes (Variables) of the new coordinate system.
     * @param listOriginAxes Axes (Variables) of the original coordinate system.
     * @throws MotuNotImplementedException
     * @throws MotuException
     */
    private void addCoordinateTransformVariable(NetcdfDataset ds,
                                                String coordTransName,
                                                List<CoordinateAxis> listNewAxes,
                                                List<CoordinateAxis> listOriginAxes) throws MotuNotImplementedException, MotuException {

        if (coordinateTransformContainsKey(coordTransName)) {
            return;
        }

        ProjectionCT projCT = null;
        ProjectionImpl proj = new LatLonProjection();
        projCT = new ProjectionCT(coordTransName, "", proj);

        VariableDS v = makeCoordinateTransformVariable(ds, projCT);

        putCoordinateTransform(coordTransName, v);

        String coordinateAxesName = CoordinateSystem.makeName(listNewAxes);

        ds.addVariable(null, v);
        v.addAttribute(new Attribute(_Coordinate.Axes, coordinateAxesName));

        addCoordinateSystemsAttr(ds, coordTransName, listOriginAxes);

    }

    // /**
    // * Adds a {@link _Coordinate.AxisType} attribute equals to GeoX to a variable. Adds only if the variable
    // * does'nt have a a {@link _Coordinate.AxisType} attribute.
    // *
    // * @param ds modify this dataset
    // * @param var modify this variable
    // */
    // private void addGeoXAxisTypeAttr(NetcdfDataset ds, Variable var) {
    //
    // Attribute attribute = null;
    // try {
    // attribute = NetCdfReader.getAttribute(var, _Coordinate.AxisType);
    // } catch (NetCdfAttributeNotFoundException e) {
    // attribute = new Attribute(_Coordinate.AxisType, AxisType.GeoX.toString());
    // var.addAttribute(attribute);
    // }
    //
    // }
    //
    // /**
    // * Adds a {@link _Coordinate.AxisType} attribute equals to GeoY to a variable. Adds only if the variable
    // * does'nt have a a {@link _Coordinate.AxisType} attribute.
    // *
    // * @param ds modify this dataset
    // * @param var modify this variable
    // */
    // private void addGeoYAxisTypeAttr(NetcdfDataset ds, Variable var) {
    //
    // Attribute attribute = null;
    // try {
    // attribute = NetCdfReader.getAttribute(var, _Coordinate.AxisType);
    // } catch (NetCdfAttributeNotFoundException e) {
    // attribute = new Attribute(_Coordinate.AxisType, AxisType.GeoY.toString());
    // var.addAttribute(attribute);
    // }
    //
    // }
    /**
     * Adds a {@link _Coordinate.AxisType} attribute equals to Lat to a variable. Adds only if the variable
     * does'nt have a a {@link _Coordinate.AxisType} attribute.
     * 
     * @param ds modify this dataset
     * @param var modify this variable
     */
    private void addLatAxisTypeAttr(NetcdfDataset ds, Variable var) {

        Attribute attribute = null;
        try {
            NetCdfReader.getAttribute(var, _Coordinate.AxisType);
        } catch (NetCdfAttributeNotFoundException e) {
            attribute = new Attribute(_Coordinate.AxisType, AxisType.Lat.toString());
            var.addAttribute(attribute);
        }

    }

    /**
     * Adds a {@link _Coordinate.AxisType} attribute equals to Lon to a variable. Adds only if the varaible
     * does'nt have a a {@link _Coordinate.AxisType} attribute.
     * 
     * @param ds modify this dataset
     * @param var modify this variable
     */
    private void addLonAxisTypeAttr(NetcdfDataset ds, Variable var) {

        Attribute attribute = null;
        try {
            NetCdfReader.getAttribute(var, _Coordinate.AxisType);
        } catch (NetCdfAttributeNotFoundException e) {
            attribute = new Attribute(_Coordinate.AxisType, AxisType.Lon.toString());
            var.addAttribute(attribute);
        }
    }

    // /** look for aliases.
    // * @param ds dataset
    // * @param axisType axis type
    // * @return coordinate variable name.
    // */
    // private String findCoordinateName(NetcdfDataset ds, AxisType axisType) {
    //
    // List vlist = ds.getVariables();
    // for (int i = 0; i < vlist.size(); i++) {
    // VariableEnhanced ve = (VariableEnhanced) vlist.get(i);
    // if (axisType == getAxisType(ds, ve)) {
    // return ve.getName();
    // }
    // }
    // return null;
    // }

    /**
     * @see ucar.nc2.dataset.CoordSysBuilder#makeCoordinateTransformVariable(ucar.nc2.dataset.NetcdfDataset,
     *      ucar.nc2.dataset.CoordinateTransform)
     * @param ds dataset
     * @param ct coordiante transform
     * @return coordinate transfrom variable.
     */

    @Override
    protected VariableDS makeCoordinateTransformVariable(NetcdfDataset ds, CoordinateTransform ct) {
        VariableDS v = new VariableDS(ds, null, null, ct.getName(), DataType.CHAR, "", null, null);

        // fake data
        Array data = Array.factory(DataType.CHAR.getPrimitiveClassType(), new int[] {}, new char[] { ' ' });
        v.setCachedData(data, true);

        return v;
    }

    /**
     * @uml.property name="coordinateTransform"
     * @uml.associationEnd inverse="coordSysBuilderYXLatLon:ucar.nc2.Variable" qualifier="key:java.lang.Object
     *                     ucar.nc2.Variable"
     */
    private Map<String, VariableDS> coordinateTransformMap = new HashMap<String, VariableDS>();

    /**
     * Getter of the property <tt>coordinateTransform</tt>.
     * 
     * @return Returns the coordinateTransformMap.
     * @uml.property name="coordinateTransform"
     */
    public Map<String, VariableDS> getCoordinateTransform() {
        return this.coordinateTransformMap;
    }

    /**
     * Returns a set view of the keys contained in this map.
     * 
     * @return a set view of the keys contained in this map.
     * @see java.util.Map#keySet()
     * @uml.property name="coordinateTransform"
     */
    public Set<String> coordinateTransformKeySet() {
        return this.coordinateTransformMap.keySet();
    }

    /**
     * Returns a collection view of the values contained in this map.
     * 
     * @return a collection view of the values contained in this map.
     * @see java.util.Map#values()
     * @uml.property name="coordinateTransform"
     */
    public Collection<VariableDS> coordinateTransformValues() {
        return this.coordinateTransformMap.values();
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified key.
     * 
     * @param key key whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map contains a mapping for the specified key.
     * @see java.util.Map#containsKey(Object)
     * @uml.property name="coordinateTransform"
     */
    public boolean coordinateTransformContainsKey(String key) {
        return this.coordinateTransformMap.containsKey(key);
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the specified value.
     * 
     * @param coordinateTransform value whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map maps one or more keys to the specified value.
     * @see java.util.Map#containsValue(Object)
     * @uml.property name="coordinateTransform"
     */
    public boolean coordinateTransformContainsValue(Variable coordinateTransform) {
        return this.coordinateTransformMap.containsValue(coordinateTransform);
    }

    /**
     * Returns the value to which this map maps the specified key.
     * 
     * @param key key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or <tt>null</tt> if the map contains no
     *         mapping for this key.
     * @see java.util.Map#get(Object)
     * @uml.property name="coordinateTransform"
     */
    public VariableDS getCoordinateTransform(String key) {
        return this.coordinateTransformMap.get(key);
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     * 
     * @return <tt>true</tt> if this map contains no key-value mappings.
     * @see java.util.Map#isEmpty()
     * @uml.property name="coordinateTransform"
     */
    public boolean isCoordinateTransformEmpty() {
        return this.coordinateTransformMap.isEmpty();
    }

    /**
     * Returns the number of key-value mappings in this map.
     * 
     * @return the number of key-value mappings in this map.
     * @see java.util.Map#size()
     * @uml.property name="coordinateTransform"
     */
    public int coordinateTransformSize() {
        return this.coordinateTransformMap.size();
    }

    /**
     * Setter of the property <tt>coordinateTransform</tt>.
     * 
     * @param coordinateTransform the coordinateTransformMap to set.
     * @uml.property name="coordinateTransform"
     */
    public void setCoordinateTransform(Map<String, VariableDS> coordinateTransform) {
        this.coordinateTransformMap = coordinateTransform;
    }

    /**
     * Associates the specified value with the specified key in this map (optional operation).
     * 
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or <tt>null</tt>
     * @see java.util.Map#put(Object,Object)
     * @uml.property name="coordinateTransform"
     */
    public Variable putCoordinateTransform(String key, VariableDS value) {
        return this.coordinateTransformMap.put(key, value);
    }

    /**
     * Removes the mapping for this key from this map if it is present (optional operation).
     * 
     * @param key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or <tt>null</tt> if there was no mapping for key.
     * @see java.util.Map#remove(Object)
     * @uml.property name="coordinateTransform"
     */
    public Variable removeCoordinateTransform(String key) {
        return this.coordinateTransformMap.remove(key);
    }

    /**
     * Removes all mappings from this map (optional operation).
     * 
     * @see java.util.Map#clear()
     * @uml.property name="coordinateTransform"
     */
    public void clearCoordinateTransform() {
        this.coordinateTransformMap.clear();
    }

}
// CSON: MultipleStringLiterals
