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
package fr.cls.atoll.motu.web.dal.request.netcdf.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.library.misc.data.ExtractCriteriaLatLon;
import fr.cls.atoll.motu.library.misc.exception.MotuExceedingCapacityException;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDateRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidLatLonRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuNoVarException;
import fr.cls.atoll.motu.library.misc.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfAttributeNotFoundException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfVariableException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfVariableNotFoundException;
import fr.cls.atoll.motu.library.misc.netcdf.NetCdfReader;
import fr.cls.atoll.motu.library.misc.netcdf.NetCdfWriter;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants._Coordinate;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridDataset;

// CSOFF: MultipleStringLiterals : avoid message in '@SuppressWarnings("unchecked")'.

/**
 * Class to process gridded dataset in X/Y coordinate with Lat/Lon transformation coordinate system. .
 * 
 * DON'T USE IT : developpment of this functionality not finished and it is deffered.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class DatasetGridXYLatLon extends DatasetGrid {
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LogManager.getLogger();

    /**
     * 
     */
    public DatasetGridXYLatLon() {
        super();
    }

    /**
     * @param product product linked to the dataset
     */
    public DatasetGridXYLatLon(Product product) {
        super(product);
    }

    /**
     * Map of each output Lat value and position (index).
     */
    protected SortedMap<Double, Integer> mapLat = new TreeMap<Double, Integer>();
    /**
     * Map of each output Lon value and position (index).
     */
    protected SortedMap<Double, Integer> mapLon = new TreeMap<Double, Integer>();

    /**
     * Input Lat variable.
     */
    protected Variable inputVarLat = null;
    /**
     * Input Lon variable.
     */
    protected Variable inputVarLon = null;
    /**
     * Input Time variable.
     */
    protected Variable inputVarTime = null;
    /**
     * Input Z variable.
     */
    protected Variable inputVarZ = null;

    /**
     * Output Lat variable.
     */
    protected Variable outputVarLat = null;
    /**
     * Output Lon variable.
     */
    protected Variable outputVarLon = null;
    /**
     * Output Time variable.
     */
    protected Variable outputVarTime = null;
    /**
     * Output Z variable.
     */
    protected Variable outputVarZ = null;

    /**
     * Lat dimension.
     */
    protected Dimension latDim = null;
    /**
     * Lon dimension.
     */
    protected Dimension lonDim = null;
    /**
     * Time dimension.
     */
    protected Dimension timeDim = null;
    /**
     * Z dimension.
     */
    protected Dimension zDim = null;

    /**
     * File writer.
     */
    protected NetCdfWriter netCdfWriter = null;

    // get each adjacent Lat/Lon ranges
    /**
     * List of each adjacent Lat dimension range values.
     */
    List<double[]> rangesLatValue = new ArrayList<double[]>();
    /**
     * List of each adjacent Lon dimension range values.
     */
    List<double[]> rangesLonValue = new ArrayList<double[]>();
    /**
     * List of each adjacent Lat dimension range.
     */
    List<List<Range>> listYXRanges = null;

    /**
     * Map of output dimensions.
     */
    Map<String, Dimension> outputDims = new HashMap<String, Dimension>();
    /**
     * Map of output variables.
     */
    Map<String, Variable> outputVars = new HashMap<String, Variable>();

    Array latArray = null;
    Array lonArray = null;

    /**
     * Extract data into a NetCdf format.
     * 
     * @throws MotuException
     * @throws MotuExceedingCapacityException
     * @throws MotuNotImplementedException
     * @throws MotuInvalidDateRangeException
     * @throws MotuInvalidDepthRangeException
     * @throws NetCdfVariableException
     * @throws MotuInvalidLatLonRangeException
     * @throws MotuNoVarException
     * @throws NetCdfVariableNotFoundException
     * @throws IOException
     */
    @Override
    public void extractDataIntoNetCdf() throws MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException,
            MotuNotImplementedException, MotuInvalidDepthRangeException, MotuInvalidLatLonRangeException, NetCdfVariableException, MotuNoVarException,
            NetCdfVariableNotFoundException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractDataIntoNetCdf() - entering");
        }

        initNetCdfExtraction();

        netCdfWriter = new NetCdfWriter(product.getExtractLocationDataTemp(), true);
        NetcdfFileWriteable ncFile = netCdfWriter.getNcfile();

        ExtractCriteriaLatLon extractCriteriaLatLon = findCriteriaLatLon();
        if (extractCriteriaLatLon == null) {
            addCriteria(new ExtractCriteriaLatLon());
        }

        // gets global ranges to be extracted
        getTimeRange();
        getYXRange();
        getZRange();

        // get each adjacent Lat/Lon ranges
        getAdjacentYXRange();

        /*
         * int countPair = 0; int countPairLat = 0; int countPairLon = 0;
         */
        /*
         * for (List<Range> ranges : listYXRanges) { Range rangeLat = ranges.get(0); Range rangeLon =
         * ranges.get(1); System.out.print("Lat range: "); System.out.print(rangeLat.first());
         * System.out.print("\t"); System.out.print(rangeLat.last()); System.out.print("\tLength\t");
         * System.out.print(rangeLat.length()); System.out.print("\tLon range: ");
         * System.out.print(rangeLon.first()); System.out.print("\t"); System.out.print(rangeLon.last());
         * System.out.print("\tLength\t"); System.out.println(rangeLon.length()); countPair +=
         * rangeLat.length() * rangeLon.length(); countPairLat += rangeLat.length(); countPairLon +=
         * rangeLon.length(); } System.out.print("countPair\t"); System.out.println(countPair);
         * System.out.print("countPairLat\t"); System.out.println(countPairLat);
         * System.out.print("countPairLon\t"); System.out.println(countPairLon);
         */

        //
        // if (hasYRangeValue) {
        // System.out.print("dimLat: ");
        // System.out.println(yxRange[0].length());
        // }
        // if (hasXRangeValue) {
        // System.out.print("dimLon: ");
        // System.out.println(yxRange[1].length());
        // }
        // if (hasTRangeValue) {
        // System.out.print("dimTime: ");
        // System.out.println(tRange.length());
        // }
        // if (hasZRangeValue) {
        // System.out.print("dimZ: ");
        // System.out.println(zRange.length());
        // }
        //
        // for (double[] rangeVal : rangesLatValue) {
        // System.out.print("Lat range value: ");
        // System.out.print(rangeVal[0]);
        // System.out.print("\t");
        // System.out.println(rangeVal[1]);
        // }
        // for (double[] rangeVal : rangesLonValue) {
        // System.out.print("Lon range value: ");
        // System.out.print(rangeVal[0]);
        // System.out.print("\t");
        // System.out.println(rangeVal[1]);
        // }
        // define dimensions, including unlimited
        // Dimension latDim = ncfile.addDimension("lat", 3);
        // Dimension lonDim = ncfile.addDimension("lon", 4);
        // Dimension timeDim = ncfile.addDimension("time", -1, true, true, false);
        if (hasOutputTimeDimension) {
            inputVarTime = productMetadata.getTimeAxis();
        }
        if (hasOutputZDimension) {
            inputVarZ = productMetadata.getZAxis();
        }
        // if (productMetadata.hasGeoYAxis() && hasYRangeValue) {
        // inputVarY = productMetadata.getGeoYAxis();
        // }
        // if (productMetadata.hasGeoXAxis() && hasXRangeValue) {
        // inputVarX = productMetadata.getGeoXAxis();
        // }
        if (hasOutputLatDimension) {
            inputVarLat = productMetadata.getLatAxis();
        }
        if (hasOutputLonDimension) {
            inputVarLon = productMetadata.getLonAxis();
        }

        // -----------------------------
        // Reads and fills Lat/Lon values
        // -----------------------------
        storeLatitudeMap();
        storeLongitudeMap();

        // -----------------------------
        // Create output axis dimension and variable
        // -----------------------------
        createTimeVariable();
        createZVariable();
        createLatVariable();
        createLonVariable();

        // process global and axis variables attributes
        List<Attribute> globalFixedAttributes = initializeNetCdfFixedGlobalAttributes();
        List<Attribute> globalDynAttributes = initializeNetCdfDynGlobalAttributes();

        // netCdfWriter.writeDimensions(outputDims.values());

        GridDataset gds = new GridDataset(product.getNetCdfReaderDataset());

        for (VarData varData : variablesValues()) {

            GeoGrid geoGrid = gds.findGridByName(varData.getVarName());
            if (geoGrid == null) {
                throw new MotuNotImplementedException(
                        String.format("Variable %s in not geo-referenced - Non-georeferenced data is not implemented (method DatasetGridYXLatLon.extractDataNetcdf)",
                                      varData.getVarName()));
            }
            Variable inputVar = geoGrid.getVariable();
            Variable outputVar = new Variable(ncFile, null, null, inputVar.getName());
            List<Dimension> dims = new ArrayList<Dimension>();
            List<CoordinateAxis> inputDims = geoGrid.getCoordinateSystem().getCoordinateAxes();
            for (CoordinateAxis inputDim : inputDims) {
                Dimension dimToAdd = outputDims.get(inputDim.getName());
                if (dimToAdd == null) {
                    // throw new MotuException(String.format("ERROR - Input dimension %s not found (method
                    // DatasetGridYXLatLon.extractDataNetcdf)",
                    // inputDim.getName()));
                    continue;
                }
                dims.add(dimToAdd);
                netCdfWriter.putDimension(dimToAdd);
                netCdfWriter.putVariable(getCoordinateVariable(dimToAdd));
            }
            outputVar.setDimensions(dims);
            outputVar.setDataType(inputVar.getDataType());
            NetCdfWriter.copyAttributes(inputVar, outputVar);
            // outputVar.setIOVar(inputVar);
            outputVars.put(outputVar.getName(), outputVar);

            netCdfWriter.putVariables(outputVar.getName(), outputVar);
            netCdfWriter.writeDependentVariables(outputVar, gds);
            // netCdfWriter.writeVariable(outputVar, null);
        }

        netCdfWriter.writeGlobalAttributes(globalFixedAttributes);
        netCdfWriter.writeGlobalAttributes(globalDynAttributes);

        processLatitudeAttributes();
        processLongitudeAttributes();
        processTimeAttributes();
        processZAttributes();

        netCdfWriter.create(VAR_ATTR_TO_REMOVE);
        // netCdfWriter.finish(VAR_ATTR_TO_REMOVE);
        for (Variable outputVar : outputVars.values()) {

            // Variable inputVar = outputVar.getIOVar();

            writeVariable(outputVar, gds);
        }

        System.out.print("WRITE :");
        // System.out.println(cpt);
        try {
            if (outputVarLat != null) {
                Array data = Array.factory(outputVarLat.getDataType(), outputVarLat.getShape());
                Set<Double> set = mapLat.keySet();
                int i = 0;
                Index ima = data.getIndex();
                for (Double value : set) {
                    data.setDouble(ima.set(i), value);
                    i++;
                }
                netCdfWriter.writeVariableData(outputVarLat, data);
            }
            if (outputVarLon != null) {
                Array data = Array.factory(outputVarLon.getDataType(), outputVarLon.getShape());
                Set<Double> set = mapLon.keySet();
                int i = 0;
                Index ima = data.getIndex();
                for (Double value : set) {
                    data.setDouble(ima.set(i), value);
                    i++;
                }
                netCdfWriter.writeVariableData(outputVarLon, data);
            }
            netCdfWriter.getNcfile().close();
        } catch (IOException e) {
            LOG.error("extractDataIntoNetCdf()", e);

            e.printStackTrace();
        }
        // netCdfWriter.finish(VAR_ATTR_TO_REMOVE);

        product.moveTempExtractFileToFinalExtractFile();

        if (LOG.isDebugEnabled()) {
            LOG.debug("extractDataIntoNetCdf() - exiting");
        }
    }

    /**
     * Gets a list of Index Ranges for the given lat, lon bounding box. For projection, only an approximation
     * based on lat/lon corners. Must have 2D/LatLon for x and y axis.
     * 
     * @throws MotuNotImplementedException
     * @throws MotuException
     */
    @Override
    public void getAdjacentYXRange() throws MotuException, MotuInvalidLatLonRangeException, MotuNotImplementedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAdjacentYXRange() - entering");
        }

        listYXRanges = null;

        if (productMetadata.hasLatLonAxis()) {
            ExtractCriteriaLatLon extractCriteriaLatLon = findCriteriaLatLon();
            if (extractCriteriaLatLon != null) {
                CoordinateSystem cs = new CoordinateSystem(product.getNetCdfReaderDataset(), productMetadata.getLatLonAxis(), null);
                listYXRanges = extractCriteriaLatLon.toListRanges(cs, rangesLatValue, rangesLonValue);
            }
        } else if (productMetadata.hasGeoXYAxis()) {
            throw new MotuNotImplementedException("X/Y axis is not implemented (method DatasetGridXYLatLon.getAdjacentYXRange");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getAdjacentYXRange() - exiting");
        }
    }

    /**
     * Creates Time Dimension and Variable.
     */
    public void createTimeVariable() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createTimeVariable() - entering");
        }

        if (inputVarTime == null) {
            outputVarTime = null;

            if (LOG.isDebugEnabled()) {
                LOG.debug("createTimeVariable() - exiting");
            }
            return;
        }
        timeDim = new Dimension(inputVarTime.getShortName(), tRange.length(), true);
        outputDims.put(timeDim.getName(), timeDim);

        outputVarTime = new Variable(netCdfWriter.getNcfile(), null, null, timeDim.getName());
        NetCdfWriter.setDim(outputVarTime, timeDim);
        outputVarTime.setDataType(inputVarTime.getDataType());
        NetCdfWriter.copyAttributes(inputVarTime, outputVarTime);
        // Netcdf 4.0 method Removed
        // outputVarTime.setIsCoordinateAxis(timeDim);
        // outputVarTime.setIOVar(inputVarTime);

        // netCdfWriter.writeDimension(timeDim);
        // netCdfWriter.writeVariable(outputVarTime, null);

        if (LOG.isDebugEnabled()) {
            LOG.debug("createTimeVariable() - exiting");
        }
    }

    /**
     * Creates Z (Depth) Dimension and Variable.
     */
    public void createZVariable() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createZVariable() - entering");
        }

        if (inputVarZ == null) {
            outputVarZ = null;

            if (LOG.isDebugEnabled()) {
                LOG.debug("createZVariable() - exiting");
            }
            return;
        }
        zDim = new Dimension(inputVarZ.getShortName(), zRange.length(), true);
        outputDims.put(zDim.getName(), zDim);

        outputVarZ = new Variable(netCdfWriter.getNcfile(), null, null, zDim.getName());
        NetCdfWriter.setDim(outputVarZ, zDim);
        outputVarZ.setDataType(inputVarZ.getDataType());
        NetCdfWriter.copyAttributes(inputVarZ, outputVarZ);
        // Netcdf 4.0 method Removed
        // outputVarZ.setIsCoordinateAxis(zDim);
        // outputVarZ.setIOVar(inputVarZ);

        // netCdfWriter.writeDimension(zDim);
        // netCdfWriter.writeVariable(outputVarZ, null);

        if (LOG.isDebugEnabled()) {
            LOG.debug("createZVariable() - exiting");
        }
    }

    /**
     * Creates Latitude Dimension and Variable.
     */
    public void createLatVariable() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createLatVariable() - entering");
        }

        if (inputVarLat == null) {
            outputVarLat = null;

            if (LOG.isDebugEnabled()) {
                LOG.debug("createLatVariable() - exiting");
            }
            return;
        }
        latDim = new Dimension(inputVarLat.getShortName(), mapLat.size(), true);
        outputDims.put(latDim.getName(), latDim);
        outputVarLat = new Variable(netCdfWriter.getNcfile(), null, null, latDim.getName());
        NetCdfWriter.setDim(outputVarLat, latDim);
        outputVarLat.setDataType(inputVarLat.getDataType());
        NetCdfWriter.copyAttributes(inputVarLat, outputVarLat);
        // Netcdf 4.0 method Removed
        // outputVarLat.setIsCoordinateAxis(latDim);
        // outputVarLat.setIOVar(inputVarLat);

        // netCdfWriter.writeDimension(latDim);
        // netCdfWriter.writeVariable(outputVarLat, null);

        if (LOG.isDebugEnabled()) {
            LOG.debug("createLatVariable() - exiting");
        }
    }

    /**
     * Creates Longitude Dimension and Variable.
     */
    public void createLonVariable() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createLonVariable() - entering");
        }

        if (inputVarLon == null) {
            outputVarLon = null;

            if (LOG.isDebugEnabled()) {
                LOG.debug("createLonVariable() - exiting");
            }
            return;
        }
        lonDim = new Dimension(inputVarLon.getShortName(), mapLon.size(), true);
        outputDims.put(lonDim.getName(), lonDim);

        outputVarLon = new Variable(netCdfWriter.getNcfile(), null, null, lonDim.getName());
        NetCdfWriter.setDim(outputVarLon, lonDim);
        outputVarLon.setDataType(inputVarLon.getDataType());
        NetCdfWriter.copyAttributes(inputVarLon, outputVarLon);
        // Netcdf 4.0 method Removed
        // outputVarLon.setIsCoordinateAxis(lonDim);
        // outputVarLon.setIOVar(inputVarLon);

        // netCdfWriter.writeDimension(lonDim);
        // netCdfWriter.writeVariable(outputVarLon, null);

        if (LOG.isDebugEnabled()) {
            LOG.debug("createLonVariable() - exiting");
        }
    }

    /**
     * Fills Latitude values in its map and compute its position (index) in the output data array.
     * 
     * @throws MotuException
     * 
     */
    protected void storeLatitudeMap() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("storeLatitudeMap() - entering");
        }

        if (inputVarLat == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("storeLatitudeMap() - exiting");
            }
            return;
        }

        Array data = null;
        try {
            // Reads data corresponding to the global range.
            List<Range> listRangeGlobal = new ArrayList<Range>();
            listRangeGlobal.add(yxRange[0]);
            listRangeGlobal.add(yxRange[1]);
            latArray = inputVarLat.read(listRangeGlobal);
            // Reads and fills data or each adjacent ranges corresponding to
            // the users' criteria.
            for (List<Range> listRange : listYXRanges) {
                data = inputVarLat.read(listRange);
                Index ima = data.getIndex();
                int countPartialY = listRange.get(0).length();
                int countPartialX = listRange.get(1).length();
                // fills the sorted map with key = latitude and postition = null
                for (int j = 0; j < countPartialY; j++) {
                    for (int i = 0; i < countPartialX; i++) {
                        Double value = data.getDouble(ima.set(j, i));
                        mapLat.put(roundLatLon(value), null);

                    }
                }
            }
            // Run through the longitude map (sorted run) and compute the position (index)
            Integer index = 0;
            Set<Double> set = mapLat.keySet();
            for (Double value : set) {
                mapLat.put(value, index);
                index++;
            }
            // System.out.print("map size: ");
            // System.out.println(mapLat.size());
            // for (Integer i : mapLat.values()) {
            // System.out.print(i);
            // System.out.print(" ");
            // }
            // System.out.println("Map ");
            // for (Double value : set) {
            // // if ((value > 80d) && (value < 81d)) {
            // // System.out.print(value);
            // // }
            // System.out.print(value);
            // System.out.print(" ");
            // }
            // System.out.println(" ");
        } catch (Exception e) {
            LOG.error("storeLatitudeMap()", e);

            throw new MotuException("ERROR encountered in DatasetGridXYLatLon - fillLatitudeMap", e);

        }

        // -----------------------------
        // Sets the right Lat range values
        // -----------------------------
        yRangeValue[0] = mapLat.firstKey();
        yRangeValue[1] = mapLat.lastKey();

        if (LOG.isDebugEnabled()) {
            LOG.debug("storeLatitudeMap() - exiting");
        }
    }

    /**
     * Fills Longitude values in its map and compute its position (index) in the output data array.
     * 
     * @throws MotuException
     * 
     */
    protected void storeLongitudeMap() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("storeLongitudeMap() - entering");
        }

        if (inputVarLon == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("storeLongitudeMap() - exiting");
            }
            return;
        }

        Array data = null;

        try {
            // Reads data corresponding to the global range.
            List<Range> listRangeGlobal = new ArrayList<Range>();
            listRangeGlobal.add(yxRange[0]);
            listRangeGlobal.add(yxRange[1]);
            lonArray = inputVarLon.read(listRangeGlobal);
            // Reads and fills data or each adjacent ranges.
            for (List<Range> listRange : listYXRanges) {
                data = inputVarLon.read(listRange);
                Index ima = data.getIndex();
                int countPartialY = listRange.get(0).length();
                int countPartialX = listRange.get(1).length();
                // fills the sorted map with key = longitude and postition = null
                for (int j = 0; j < countPartialY; j++) {
                    for (int i = 0; i < countPartialX; i++) {
                        Double value = data.getDouble(ima.set(j, i));
                        mapLon.put(roundLatLon(value), null);
                    }
                }
            }
            // Run through the longitude map (sorted run) and compute the position (index)
            Integer index = 0;
            Set<Double> set = mapLon.keySet();
            for (Double value : set) {
                mapLon.put(value, index);
                index++;
            }
            // System.out.print("map size: ");
            // System.out.println(mapLon.size());
            // for (Integer i : mapLon.values()) {
            // System.out.print(i);
            // System.out.print(" ");
            // }
            // System.out.println(" ");
            // for (Double value : set) {
            // System.out.print(value);
            // System.out.print(" ");
            // }
            // System.out.println(" ");
        } catch (Exception e) {
            LOG.error("storeLongitudeMap()", e);

            throw new MotuException("ERROR encountered in DatasetGridXYLatLon - fillLongitudeMap", e);

        }
        // -----------------------------
        // Sets the right Lon range values
        // -----------------------------
        xRangeValue[0] = mapLon.firstKey();
        xRangeValue[1] = mapLon.lastKey();

        if (LOG.isDebugEnabled()) {
            LOG.debug("storeLongitudeMap() - exiting");
        }
    }

    /**
     * Sets and remove specific latitude attributes.
     */
    public void processLatitudeAttributes() {
        if (outputVarLat == null) {
            return;
        }
        Attribute attribute = null;
        // -----------------------------
        // adds or sets valid_min attribute
        // -----------------------------
        attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_VALID_MIN, mapLat.firstKey());
        outputVarLat.addAttribute(attribute);
        // -----------------------------
        // adds or sets valid_max attribute
        // -----------------------------
        attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_VALID_MAX, mapLat.lastKey());
        outputVarLat.addAttribute(attribute);

        // -----------------------------
        // adds or sets axis attribute
        // -----------------------------
        attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_AXIS, NetCdfReader.VARIABLEATTRIBUTE_Y_AXIS_VALUE);
        outputVarLat.addAttribute(attribute);
        // -----------------------------
        // adds or sets axis type attribute
        // -----------------------------
        attribute = new Attribute(_Coordinate.AxisType, AxisType.Lat.toString());
        outputVarLat.addAttribute(attribute);
        // -----------------------------
        // adds long name attribute
        // -----------------------------
        try {
            attribute = NetCdfReader.getAttribute(outputVarLat, NetCdfReader.VARIABLEATTRIBUTE_LONG_NAME);
        } catch (NetCdfAttributeNotFoundException e) {
            attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_LONG_NAME, NetCdfReader.VARIABLEATTRIBUTE_LAT_LONG_NAME_VALUE);
            outputVarLat.addAttribute(attribute);
        }
        // -----------------------------
        // adds standard name attribute
        // -----------------------------
        try {
            attribute = NetCdfReader.getAttribute(outputVarLat, NetCdfReader.VARIABLEATTRIBUTE_STANDARD_NAME);
        } catch (NetCdfAttributeNotFoundException e) {
            attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_STANDARD_NAME, NetCdfReader.VARIABLEATTRIBUTE_LAT_STANDARD_NAME_VALUE);
            outputVarLat.addAttribute(attribute);
        }

        // -----------------------------
        // remove _Fillvalue attribute
        // -----------------------------
        try {
            attribute = NetCdfReader.getAttribute(outputVarLat, NetCdfReader.VARIABLEATTRIBUTE_FILEVALUE);
            outputVarLat.remove(attribute);
        } catch (NetCdfAttributeNotFoundException e) {
            // Nothing to do
        }
        // -----------------------------
        // remove missing_value attribute
        // -----------------------------
        try {
            attribute = NetCdfReader.getAttribute(outputVarLat, NetCdfReader.VARIABLEATTRIBUTE_MISSINGVALUE);
            outputVarLat.remove(attribute);
        } catch (NetCdfAttributeNotFoundException e) {
            // Nothing to do
        }

    }

    /**
     * Sets and remove specific longitude attributes.
     */
    public void processLongitudeAttributes() {
        if (outputVarLon == null) {
            return;
        }
        Attribute attribute = null;
        // -----------------------------
        // adds or sets valid_min attribute
        // -----------------------------
        attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_VALID_MIN, mapLon.firstKey());
        outputVarLon.addAttribute(attribute);
        // -----------------------------
        // adds or sets valid_max attribute
        // -----------------------------
        attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_VALID_MAX, mapLon.lastKey());
        outputVarLon.addAttribute(attribute);

        // -----------------------------
        // adds or sets axis attribute
        // -----------------------------
        attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_AXIS, NetCdfReader.VARIABLEATTRIBUTE_X_AXIS_VALUE);
        outputVarLon.addAttribute(attribute);
        // -----------------------------
        // adds or sets axis type attribute
        // -----------------------------
        attribute = new Attribute(_Coordinate.AxisType, AxisType.Lon.toString());
        outputVarLon.addAttribute(attribute);
        // -----------------------------
        // adds long name attribute
        // -----------------------------
        try {
            attribute = NetCdfReader.getAttribute(outputVarLon, NetCdfReader.VARIABLEATTRIBUTE_LONG_NAME);
        } catch (NetCdfAttributeNotFoundException e) {
            attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_LONG_NAME, NetCdfReader.VARIABLEATTRIBUTE_LON_LONG_NAME_VALUE);
            outputVarLon.addAttribute(attribute);
        }
        // -----------------------------
        // adds standard name attribute
        // -----------------------------
        try {
            attribute = NetCdfReader.getAttribute(outputVarLon, NetCdfReader.VARIABLEATTRIBUTE_STANDARD_NAME);
        } catch (NetCdfAttributeNotFoundException e) {
            attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_STANDARD_NAME, NetCdfReader.VARIABLEATTRIBUTE_LON_STANDARD_NAME_VALUE);
            outputVarLon.addAttribute(attribute);
        }

        // -----------------------------
        // remove _Fillvalue attribute
        // -----------------------------
        try {
            attribute = NetCdfReader.getAttribute(outputVarLon, NetCdfReader.VARIABLEATTRIBUTE_FILEVALUE);
            outputVarLon.remove(attribute);
        } catch (NetCdfAttributeNotFoundException e) {
            // Nothing to do
        }
        // -----------------------------
        // remove missing_value attribute
        // -----------------------------
        try {
            attribute = NetCdfReader.getAttribute(outputVarLon, NetCdfReader.VARIABLEATTRIBUTE_MISSINGVALUE);
            outputVarLon.remove(attribute);
        } catch (NetCdfAttributeNotFoundException e) {
            // Nothing to do
        }

    }

    /**
     * Sets and remove specific time attributes.
     */
    public void processTimeAttributes() {
        if (outputVarTime == null) {
            return;
        }
        Attribute attribute = null;
        // -----------------------------
        // adds or sets valid_min attribute
        // -----------------------------
        attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_VALID_MIN, tRangeValue[0]);
        outputVarTime.addAttribute(attribute);
        // -----------------------------
        // adds or sets valid_max attribute
        // -----------------------------
        attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_VALID_MAX, tRangeValue[1]);
        outputVarTime.addAttribute(attribute);

        // -----------------------------
        // adds or sets axis attribute
        // -----------------------------
        attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_AXIS, NetCdfReader.VARIABLEATTRIBUTE_TIME_AXIS_VALUE);
        outputVarTime.addAttribute(attribute);
        // -----------------------------
        // adds or sets axis type attribute
        // -----------------------------
        attribute = new Attribute(_Coordinate.AxisType, AxisType.Time.toString());
        outputVarTime.addAttribute(attribute);
        // -----------------------------
        // adds long name attribute
        // -----------------------------
        try {
            attribute = NetCdfReader.getAttribute(outputVarTime, NetCdfReader.VARIABLEATTRIBUTE_LONG_NAME);
        } catch (NetCdfAttributeNotFoundException e) {
            attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_LONG_NAME, NetCdfReader.VARIABLEATTRIBUTE_TIME_LONG_NAME_VALUE);
            outputVarTime.addAttribute(attribute);
        }
        // -----------------------------
        // adds standard name attribute
        // -----------------------------
        try {
            attribute = NetCdfReader.getAttribute(outputVarTime, NetCdfReader.VARIABLEATTRIBUTE_STANDARD_NAME);
        } catch (NetCdfAttributeNotFoundException e) {
            attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_STANDARD_NAME, NetCdfReader.VARIABLEATTRIBUTE_TIME_STANDARD_NAME_VALUE);
            outputVarTime.addAttribute(attribute);
        }

        // -----------------------------
        // remove _Fillvalue attribute
        // -----------------------------
        try {
            attribute = NetCdfReader.getAttribute(outputVarTime, NetCdfReader.VARIABLEATTRIBUTE_FILEVALUE);
            outputVarTime.remove(attribute);
        } catch (NetCdfAttributeNotFoundException e) {
            // Nothing to do
        }
        // -----------------------------
        // remove missing_value attribute
        // -----------------------------
        try {
            attribute = NetCdfReader.getAttribute(outputVarTime, NetCdfReader.VARIABLEATTRIBUTE_MISSINGVALUE);
            outputVarTime.remove(attribute);
        } catch (NetCdfAttributeNotFoundException e) {
            // Nothing to do
        }

    }

    /**
     * Sets and remove specific time attributes.
     */
    public void processZAttributes() {
        if (outputVarZ == null) {
            return;
        }
        Attribute attribute = null;
        // -----------------------------
        // adds or sets valid_min attribute
        // -----------------------------
        attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_VALID_MIN, zRangeValue[0]);
        outputVarZ.addAttribute(attribute);
        // -----------------------------
        // adds or sets valid_max attribute
        // -----------------------------
        attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_VALID_MAX, zRangeValue[1]);
        outputVarZ.addAttribute(attribute);

        // -----------------------------
        // adds or sets axis attribute
        // -----------------------------
        attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_AXIS, NetCdfReader.VARIABLEATTRIBUTE_Z_AXIS_VALUE);
        outputVarZ.addAttribute(attribute);
        // -----------------------------
        // adds or sets axis type attribute
        // -----------------------------
        attribute = new Attribute(_Coordinate.AxisType, AxisType.Height.toString());
        outputVarZ.addAttribute(attribute);
        // -----------------------------
        // adds or sets ZisPositive attribute
        // -----------------------------
        attribute = new Attribute(_Coordinate.ZisPositive, NetCdfReader.VARIABLEATTRIBUTE_DOWN_NAME_VALUE);
        outputVarZ.addAttribute(attribute);
        // -----------------------------
        // adds long name attribute
        // -----------------------------
        try {
            attribute = NetCdfReader.getAttribute(outputVarZ, NetCdfReader.VARIABLEATTRIBUTE_LONG_NAME);
        } catch (NetCdfAttributeNotFoundException e) {
            attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_LONG_NAME, NetCdfReader.VARIABLEATTRIBUTE_DEPTH_LONG_NAME_VALUE);
            outputVarZ.addAttribute(attribute);
        }
        // -----------------------------
        // adds standard name attribute
        // -----------------------------
        try {
            attribute = NetCdfReader.getAttribute(outputVarZ, NetCdfReader.VARIABLEATTRIBUTE_STANDARD_NAME);
        } catch (NetCdfAttributeNotFoundException e) {
            attribute = new Attribute(NetCdfReader.VARIABLEATTRIBUTE_STANDARD_NAME, NetCdfReader.VARIABLEATTRIBUTE_DEPTH_STANDARD_NAME_VALUE);
            outputVarZ.addAttribute(attribute);
        }

        // -----------------------------
        // remove _Fillvalue attribute
        // -----------------------------
        try {
            attribute = NetCdfReader.getAttribute(outputVarZ, NetCdfReader.VARIABLEATTRIBUTE_FILEVALUE);
            outputVarZ.remove(attribute);
        } catch (NetCdfAttributeNotFoundException e) {
            // Nothing to do
        }
        // -----------------------------
        // remove missing_value attribute
        // -----------------------------
        try {
            attribute = NetCdfReader.getAttribute(outputVarZ, NetCdfReader.VARIABLEATTRIBUTE_MISSINGVALUE);
            outputVarZ.remove(attribute);
        } catch (NetCdfAttributeNotFoundException e) {
            // Nothing to do
        }

    }

    /**
     * @param outputVar outputVar
     * @param gds gds
     * @throws MotuException
     * @throws MotuNotImplementedException
     */
    public void writeVariable(Variable outputVar, GridDataset gds) throws MotuException, MotuNotImplementedException {

        // Variable inputVar = outputVar.getIOVar();
        Variable inputVar = outputVar;
        GeoGrid geoGrid = gds.findGridByName(inputVar.getName());
        if (geoGrid == null) {
            throw new MotuNotImplementedException(
                    String.format("Variable %s in not geo-referenced - Non-georeferenced data is not implemented (method DatasetGrid.extractData)",
                                  inputVar.getName()));
        }

        GeoGrid geoGridSubset = null;
        MAMath.MinMax minMax = null;
        Variable inputVarSubset = null;
        // Valid_min and valid_max attribute from the variables :
        // because their values can't be modified after file is created.
        // the valid_min and valid_max can't be calculated after reading the data
        // of the variable, and it's too late to modify objects (variables, dimensions,
        // attributes) in the NetCdf file (see NetCdfFileWriteable class).
        // We have to read data variable twice :
        // once before file creation, to compute valid min an valid max value,
        // and once after file creation, to write data in the file.

        // Computes valid_min and valid_max data
        // int tempCountY = 0;
        // int tempCountX = 0;
        // int tempCountElement = 0;
        for (List<Range> listYXRange : listYXRanges) {
            try {
                geoGridSubset = geoGrid.subset(tRange, zRange, listYXRange.get(0), listYXRange.get(1));
            } catch (InvalidRangeException e) {
                throw new MotuException("Error in subsetting geo grid", e);
            }
            inputVarSubset = geoGridSubset.getVariable();
            MAMath.MinMax minMaxSubset = NetCdfWriter.getMinMaxSkipMissingData(geoGrid, inputVarSubset, null);
            if (minMax == null) {
                minMax = minMaxSubset;
            }
            if (minMaxSubset.max > minMax.max) {
                minMax.max = minMaxSubset.max;
            }
            if (minMaxSubset.min < minMax.min) {
                minMax.min = minMaxSubset.min;
            }

            // try {
            // Array data = inputVarSubset.read();
            // tempCountElement += data.getSize();
            // } catch (IOException e) {
            // e.printStackTrace();
            // }
            //
            // tempCountY += listYXRange.get(0).length();
            // tempCountX += listYXRange.get(1).length();

        }

        // System.out.print("tempCountY:");
        // System.out.println(tempCountY);
        // System.out.print("tempCountX:");
        // System.out.println(tempCountX);
        // System.out.print("tempCountElement:");
        // System.out.println(tempCountElement);

        if (minMax != null) {
            netCdfWriter.setValidMinMaxVarAttributes(outputVar, minMax);
        } else {
            netCdfWriter.removeValidMinMaxVarAttributes(outputVar);
        }

        // Writes data
        // Array dataToFill = Array.factory(outputVar.getDataType(), outputVar.getShape());

        // try {
        Map<int[], int[]> originAndShape = NetCdfWriter.parseOriginAndShape(outputVar);

        Set<int[]> keySet = originAndShape.keySet();
        // TODO: replace the following loop (Inefficient use of keySet iterator instead of entrySet
        // iterator) by the following:
        // for( Map.Entry<int[], int[]> entry : originAndShape.entrySet() )

        for (Iterator<int[]> it = keySet.iterator(); it.hasNext();) {
            Runtime runtime = Runtime.getRuntime();
            // TODO:
            // See if the garbage collector invocation is necessary or not (FindBugs: Explicit garbage
            // collection; extremely dubious except in benchmarking code)
            runtime.gc();

            int[] origin = null;
            int[] shape = null;
            origin = it.next();

            if (origin == null) {
                throw new MotuException("Error in NetCfdWriter finish - unable to find origin - (origin is null)");
            }

            shape = originAndShape.get(origin);

            if (shape == null) {
                throw new MotuException("Error in NetCfdWriter finish - unable to find shape - (shape is null)");
            }

            Array dataToFill = null;
            dataToFill = Array.factory(outputVar.getDataType(), shape);

            double fillValue = NetCdfReader.initializeMissingData(outputVar, dataToFill);

            for (List<Range> listYXRange : listYXRanges) {
                try {
                    geoGridSubset = geoGrid.subset(tRange, zRange, listYXRange.get(0), listYXRange.get(1));
                } catch (InvalidRangeException e) {
                    throw new MotuException("Error in subsetting geo grid", e);
                }
                inputVarSubset = geoGridSubset.getVariable();
                if (NetCdfWriter.isReadByBlock(inputVarSubset)) {
                    fillDataByBlock(inputVarSubset, dataToFill);
                } else {
                    fillDataInOneGulp(inputVarSubset, dataToFill, origin, fillValue);
                }
            }
            // data = var.read(origin, shape);
            netCdfWriter.writeVariableData(outputVar, origin, dataToFill);
            dataToFill = null;

        }
        // } catch (IOException e) {
        // throw new MotuException("Error IOException in NetcdfWriter writeVariableByBlock", (Throwable) e);
        // } catch (InvalidRangeException e) {
        // throw new MotuException("Error InvalidRangeException in NetcdfWriter writeVariableByBlock",
        // (Throwable) e);
        // }

        // for (List<Range> listYXRange : listYXRanges) {
        // try {
        // geoGridSubset = geoGrid.subset(tRange, zRange, listYXRange.get(0), listYXRange.get(1));
        // } catch (InvalidRangeException e) {
        // throw new MotuException("Error in subsetting geo grid", (Throwable) e);
        // }
        // inputVarSubset = (Variable) geoGridSubset.getVariable();
        // if (netCdfWriter.isReadByBlock(inputVarSubset)) {
        // fillDataByBlock(inputVarSubset, dataToFill);
        // } else {
        // fillDataInOneGulp(inputVarSubset, dataToFill);
        // }
        // }
    }

    /**
     * Writes variable data in one gulp. It reads the variable data by block and writes each block data in the
     * netcdf file.
     * 
     * @param var variable to be written
     * @param dataToFill array to fill
     * @throws MotuException
     * @throws MotuNotImplementedException
     */
    protected void fillDataByBlock(Variable var, Array dataToFill) throws MotuException, MotuNotImplementedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("fillDataByBlock() - entering");
        }

        int[] origin = null;
        int[] shape = null;
        Array data = null;
        try {
            Map<int[], int[]> originAndShape = NetCdfWriter.parseOriginAndShape(var);

            // CSOFF: StrictDuplicateCode : normal duplication code.

            Set<int[]> keySet = originAndShape.keySet();

            // TODO: replace the following loop (Inefficient use of keySet iterator instead of entrySet
            // iterator) by the following:
            // for( Map.Entry<int[], int[]> entry : originAndShape.entrySet() )

            for (Iterator<int[]> it = keySet.iterator(); it.hasNext();) {
                origin = it.next();

                if (origin == null) {
                    throw new MotuException("Error in DatasetGridXYLatLon fillDataByBlock - unable to find origin - (origin is null)");
                }

                shape = originAndShape.get(origin);

                if (shape == null) {
                    throw new MotuException("Error in DatasetGridXYLatLon fillDataByBlock - unable to find shape - (shape is null)");
                }
                // CSOON: StrictDuplicateCode

                data = var.read(origin, shape);

                // writeVariableData(var, data);
            }
        } catch (IOException e) {
            LOG.error("fillDataByBlock()", e);

            throw new MotuException("Error IOException in DatasetGridXYLatLon fillDataByBlock", e);
        } catch (InvalidRangeException e) {
            LOG.error("fillDataByBlock()", e);

            throw new MotuException("Error InvalidRangeException in DatasetGridXYLatLon fillDataByBlock", e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("fillDataByBlock() - exiting");
        }
    }

    /**
     * Fills variable data in one gulp. It reads all the variable data in memory and fills in a Array object.
     * 
     * @param varToRead variable to be read.
     * @param dataToFill array fto fill in.
     * @param originToFill originToFill
     * @param fillValue fillValue
     * @throws MotuException
     */
    protected void fillDataInOneGulp(Variable varToRead, Array dataToFill, int[] originToFill, double fillValue) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("fillDataInOneGulp() - entering");
        }

        Array dataRead = null;
        try {
            dataRead = varToRead.read();
        } catch (IOException e) {
            LOG.error("fillDataInOneGulp()", e);

            throw new MotuException("Error in DatasetGridXYLatLon fillDataInOneGulp", e);
        }

        switch (dataRead.getRank()) {
        case 1:

            break;
        case 2:
            fillData2D(varToRead, dataRead, dataToFill, originToFill, fillValue);
            break;
        case 3:

            break;
        case 4:

            break;

        default:
            break;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("fillDataInOneGulp() - exiting");
        }
    }

    /**
     * 
     */
    // public static int cpt = 0;
    /**
     * 
     * @param varSrc varSrc
     * @param dataSrc varSrc
     * @param dataDest dataDest
     * @param originDest originDest
     * @param fillValue fillValue
     * @throws MotuException
     */
    public void fillData2D(Variable varSrc, Array dataSrc, Array dataDest, int[] originDest, double fillValue) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("fillData2D() - entering");
        }

        // Assumes that arrays are 2D first first dimension = y and second dimension = x
        // Assumes that data type is double.
        // List<Range> listRange = varSrc.getSectionRanges();
        List<Range> listRange = varSrc.getRanges();
        Range yRange = listRange.get(0);
        Range xRange = listRange.get(1);

        int fromY = yRange.first() - yxRange[0].first();
        // int toY = yRange.last();
        int fromX = xRange.first() - yxRange[1].first();
        // int toX = xRange.last();

        int[] shapeSrc = dataSrc.getShape();
        int countY = shapeSrc[0];
        int countX = shapeSrc[1];

        int[] shapeDest = dataDest.getShape();
        int fromLat = originDest[0];
        int toLat = fromLat + shapeDest[0] - 1;
        int fromLon = originDest[1];
        int toLon = fromLon + shapeDest[1] - 1;

        System.out.print("fillData2D ");
        System.out.print(String.format(" fromY %d - fromX %d ", fromY, fromX));
        System.out.println(String.format(" fromLat %d toLat %d - fromLon %d toLon %d", fromLat, toLat, fromLon, toLon));

        Index imaIn = dataSrc.getIndex();
        Index imaLat = latArray.getIndex();
        Index imaLon = lonArray.getIndex();
        Index imaOut = dataDest.getIndex();

        for (int j = 0; j < countY; j++) {
            for (int i = 0; i < countX; i++) {
                Double latitudeValue = latArray.getDouble(imaLat.set(fromY + j, fromX + i));
                Double longitudeValue = lonArray.getDouble(imaLon.set(fromY + j, fromX + i));
                Integer indexLatOut = mapLat.get(roundLatLon(latitudeValue));
                Integer indexLonOut = mapLon.get(roundLatLon(longitudeValue));
                double dataValue = dataSrc.getDouble(imaIn.set(j, i));

                if ((indexLatOut == null) || (indexLonOut == null)) {
                    String idxLat = (indexLatOut == null) ? "null" : indexLatOut.toString();
                    String idxLon = (indexLonOut == null) ? "null" : indexLonOut.toString();
                    throw new MotuException(
                            String.format("ERROR in DatasetGridXYLatLon - fillData2D - Unable to find latitude value: %f or longitude value: %f (Latitude map index %s, Longitude map index %s",
                                          latitudeValue,
                                          longitudeValue,
                                          idxLat,
                                          idxLon));
                }

                if ((indexLatOut < fromLat) || (indexLatOut > toLat) || (indexLonOut < fromLon) || (indexLonOut > toLon)) {
                    continue;
                }
                int jDest = indexLatOut - fromLat;
                int iDest = indexLonOut - fromLon;
                System.out.println(String.format("Insert fromLat %d fromLon %d indexLatOut %d indexLonOut %d j %d i %d value %f",
                                                 fromLat,
                                                 fromLon,
                                                 indexLatOut,
                                                 indexLonOut,
                                                 jDest,
                                                 iDest,
                                                 dataValue));
                if (dataDest.getDouble(imaOut.set(jDest, iDest)) != fillValue) {
                    System.out.println(String.format("Not fill value fromLat %d fromLon %d indexLatOut %d indexLonOut %d j %d i %d value %f",
                                                     fromLat,
                                                     fromLon,
                                                     indexLatOut,
                                                     indexLonOut,
                                                     jDest,
                                                     iDest,
                                                     dataValue));

                }
                dataDest.setDouble(imaOut.set(jDest, iDest), dataValue);
                // cpt++;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("fillData2D() - exiting");
        }
    }

    /**
     * @param y y
     * @param x x
     * @return lat value
     */
    public double getLatValueFromYX(int y, int x) {
        int[] shape = latArray.getShape();

        int countY = shape[0];
        int countX = shape[1];

        if ((y >= countY) || (x >= countX)) {
            return Double.MAX_VALUE;
        }
        Index ima = latArray.getIndex();
        return latArray.getDouble(ima.set(y, x));
    }

    /**
     * @param y y
     * @param x x
     * @return lon value
     */
    public double getLonValueFromYX(int y, int x) {
        int[] shape = latArray.getShape();

        int countY = shape[0];
        int countX = shape[1];

        if ((y >= countY) || (x >= countX)) {
            return Double.MAX_VALUE;
        }
        Index ima = latArray.getIndex();
        return latArray.getDouble(ima.set(y, x));
    }

    /**
     * @param value value to round
     * @return rounded value
     */
    private double roundLatLon(double value) {
        final double precision = 100.0;
        double temp = Math.round(value * precision);
        return temp / precision;
    }
}

// CSON: MultipleStringLiterals
