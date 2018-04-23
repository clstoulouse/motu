package fr.cls.atoll.motu.web.dal.request.extractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.exception.MotuExceedingCapacityException;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDateRangeException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidLatLonRangeException;
import fr.cls.atoll.motu.web.bll.exception.MotuNoVarException;
import fr.cls.atoll.motu.web.bll.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfAttributeNotFoundException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfVariableException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfVariableNotFoundException;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaLatLon;
import fr.cls.atoll.motu.web.bll.request.model.RequestDownloadStatus;
import fr.cls.atoll.motu.web.dal.request.netcdf.NetCdfReader;
import fr.cls.atoll.motu.web.dal.request.netcdf.NetCdfWriter;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.VarData;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants._Coordinate;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridDataset;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class DatasetGridXYLatLonManager extends DatasetGridManager {

    /**
     * Map of each output Lat value and position (index).
     */
    protected SortedMap<Double, Integer> mapLat;
    /**
     * Map of each output Lon value and position (index).
     */
    protected SortedMap<Double, Integer> mapLon;

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
    private List<double[]> rangesLatValue;
    /**
     * List of each adjacent Lon dimension range values.
     */
    private List<double[]> rangesLonValue;
    /**
     * List of each adjacent Lat dimension range.
     */
    // private List<List<Range>> listYXRanges = null;

    /**
     * Map of output dimensions.
     */
    private Map<String, Dimension> outputDims;
    /**
     * Map of output variables.
     */
    private Map<String, Variable> outputVars;

    private Array latArray = null;
    private Array lonArray = null;

    /**
     * Constructeur.
     * 
     * @param requestProduct
     */
    public DatasetGridXYLatLonManager(RequestDownloadStatus rds_) {
        super(rds_);
        mapLat = new TreeMap<>();
        mapLon = new TreeMap<>();
        outputDims = new HashMap<>();
        outputVars = new HashMap<>();
        rangesLonValue = new ArrayList<>();
        rangesLatValue = new ArrayList<>();
    }

    @Override
    public void extractDataIntoNetCdf() throws MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException,
            MotuNotImplementedException, MotuInvalidDepthRangeException, MotuInvalidLatLonRangeException, NetCdfVariableException, MotuNoVarException,
            NetCdfVariableNotFoundException, IOException {
        initNetCdfExtraction();

        netCdfWriter = new NetCdfWriter(
                getRequestDownloadStatus().getRequestProduct().getRequestProductParameters().getExtractLocationDataTemp(),
                getRequestDownloadStatus().getRequestProduct().getExtractionParameters().getDataOutputFormat());
        NetcdfFile ncFile = netCdfWriter.getNcfileWriter().getNetcdfFile();

        ExtractCriteriaLatLon extractCriteriaLatLon = getRequestDownloadStatus().getRequestProduct().getRequestProductParameters()
                .findCriteriaLatLon();
        if (extractCriteriaLatLon == null) {
            getRequestDownloadStatus().getRequestProduct().getRequestProductParameters().getListCriteria().add(new ExtractCriteriaLatLon());
        }

        // gets global ranges to be extracted
        Range tRange = initTimeRange();
        Range[] yxRange = getYXRange();
        Range zRange = initZRange();

        // get each adjacent Lat/Lon ranges
        List<List<Range>> listYXRanges = initAdjacentYXRange();

        if (hasOutputTimeDimension) {
            inputVarTime = getRequestDownloadStatus().getRequestProduct().getProduct().getProductMetaData().getTimeAxis();
        }
        if (hasOutputZDimension) {
            inputVarZ = getRequestDownloadStatus().getRequestProduct().getProduct().getProductMetaData().getZAxis();
        }
        if (hasOutputLatDimension) {
            inputVarLat = getRequestDownloadStatus().getRequestProduct().getProduct().getProductMetaData().getLatAxis();
        }
        if (hasOutputLonDimension) {
            inputVarLon = getRequestDownloadStatus().getRequestProduct().getProduct().getProductMetaData().getLonAxis();
        }

        // -----------------------------
        // Reads and fills Lat/Lon values
        // -----------------------------
        storeLatitudeMap(yxRange, listYXRanges);
        storeLongitudeMap(yxRange, listYXRanges);

        // -----------------------------
        // Create output axis dimension and variable
        // -----------------------------
        createTimeVariable(tRange);
        createZVariable(zRange);
        createLatVariable();
        createLonVariable();

        // process global and axis variables attributes
        List<Attribute> globalFixedAttributes = initializeNetCdfFixedGlobalAttributes();
        List<Attribute> globalDynAttributes = initializeNetCdfDynGlobalAttributes();

        GridDataset gds = new GridDataset(getRequestDownloadStatus().getRequestProduct().getProduct().getNetCdfReaderDataset());
        for (VarData varData : getRequestDownloadStatus().getRequestProduct().getRequestProductParameters().getVariables().values()) {
            GeoGrid geoGrid = gds.findGridByName(varData.getVarName());
            if (geoGrid == null) {
                throw new MotuNotImplementedException(
                        String.format("Variable %s in not geo-referenced - Non-georeferenced data is not implemented (method DatasetGridYXLatLon.extractDataNetcdf)",
                                      varData.getVarName()));
            }
            Variable inputVar = geoGrid.getVariable();
            Variable outputVar = new Variable(ncFile, null, null, inputVar.getShortName());
            List<Dimension> dims = new ArrayList<>();
            List<CoordinateAxis> inputDims = geoGrid.getCoordinateSystem().getCoordinateAxes();
            for (CoordinateAxis inputDim : inputDims) {
                Dimension dimToAdd = outputDims.get(inputDim.getFullName());
                if (dimToAdd != null) {
                    dims.add(dimToAdd);
                    netCdfWriter.putDimension(dimToAdd);
                    netCdfWriter.putVariable(getCoordinateVariable(dimToAdd));
                }
            }
            outputVar.setDimensions(dims);
            outputVar.setDataType(inputVar.getDataType());
            NetCdfWriter.copyAttributes(inputVar, outputVar);
            outputVars.put(outputVar.getFullName(), outputVar);

            netCdfWriter.putVariables(outputVar.getFullName(), outputVar);
            netCdfWriter.initDependentVariablesInVariableList(outputVar, gds);
        }

        netCdfWriter.writeGlobalAttributes(globalFixedAttributes);
        netCdfWriter.writeGlobalAttributes(globalDynAttributes);

        processLatitudeAttributes();
        processLongitudeAttributes();
        processTimeAttributes();
        processZAttributes();

        netCdfWriter.writeVariableInNetCdfFileAndSetNetcdfFileInCreateMode(VAR_ATTR_TO_REMOVE);
        for (Variable outputVar : outputVars.values()) {
            writeVariable(outputVar, gds, zRange, tRange, yxRange, listYXRanges);
        }

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
            netCdfWriter.getNcfileWriter().close();
        } catch (IOException e) {
            throw new MotuException(ErrorType.SYSTEM, e);
        }
        moveTempExtractFileToFinalExtractFile();
    }

    /**
     * Gets a list of Index Ranges for the given lat, lon bounding box. For projection, only an approximation
     * based on lat/lon corners. Must have 2D/LatLon for x and y axis.
     * 
     * @throws MotuNotImplementedException
     * @throws MotuException
     */
    @Override
    public List<List<Range>> initAdjacentYXRange() throws MotuException, MotuInvalidLatLonRangeException, MotuNotImplementedException {
        List<List<Range>> listYXRanges = null;

        if (getRequestDownloadStatus().getRequestProduct().getProduct().getProductMetaData().hasLatLonAxis()) {
            ExtractCriteriaLatLon extractCriteriaLatLon = getRequestDownloadStatus().getRequestProduct().getRequestProductParameters()
                    .findCriteriaLatLon();
            if (extractCriteriaLatLon != null) {
                CoordinateSystem cs = new CoordinateSystem(
                        getRequestDownloadStatus().getRequestProduct().getProduct().getNetCdfReaderDataset(),
                        getRequestDownloadStatus().getRequestProduct().getProduct().getProductMetaData().getLatLonAxis(),
                        null);
                try {
                    listYXRanges = extractCriteriaLatLon.toListRanges(cs, rangesLatValue, rangesLonValue);
                } catch (InvalidRangeException e) {
                    throw new MotuException(ErrorType.SYSTEM, "Error while creating Range");
                }
            }
        } else if (getRequestDownloadStatus().getRequestProduct().getProduct().getProductMetaData().hasGeoXYAxis()) {
            throw new MotuNotImplementedException("X/Y axis is not implemented (method DatasetGridXYLatLon.getAdjacentYXRange");
        }
        return listYXRanges;
    }

    /**
     * Creates Time Dimension and Variable.
     */
    public void createTimeVariable(Range tRange) {
        if (inputVarTime == null) {
            outputVarTime = null;
            return;
        }
        timeDim = new Dimension(inputVarTime.getShortName(), tRange.length(), true);
        outputDims.put(timeDim.getFullName(), timeDim);

        outputVarTime = new Variable(netCdfWriter.getNcfileWriter().getNetcdfFile(), null, null, timeDim.getShortName());
        NetCdfWriter.setDim(outputVarTime, timeDim);
        outputVarTime.setDataType(inputVarTime.getDataType());
        NetCdfWriter.copyAttributes(inputVarTime, outputVarTime);
    }

    /**
     * Creates Z (Depth) Dimension and Variable.
     */
    public void createZVariable(Range zRange) {
        if (inputVarZ == null) {
            outputVarZ = null;
            return;
        }
        zDim = new Dimension(inputVarZ.getShortName(), zRange.length(), true);
        outputDims.put(zDim.getFullName(), zDim);

        outputVarZ = new Variable(netCdfWriter.getNcfileWriter().getNetcdfFile(), null, null, zDim.getShortName());
        NetCdfWriter.setDim(outputVarZ, zDim);
        outputVarZ.setDataType(inputVarZ.getDataType());
        NetCdfWriter.copyAttributes(inputVarZ, outputVarZ);
        // Netcdf 4.0 method Removed
        // outputVarZ.setIsCoordinateAxis(zDim);
        // outputVarZ.setIOVar(inputVarZ);

        // netCdfWriter.writeDimension(zDim);
        // netCdfWriter.writeVariable(outputVarZ, null);
    }

    /**
     * Creates Latitude Dimension and Variable.
     */
    public void createLatVariable() {
        if (inputVarLat == null) {
            outputVarLat = null;
            return;
        }
        latDim = new Dimension(inputVarLat.getShortName(), mapLat.size(), true);
        outputDims.put(latDim.getFullName(), latDim);
        outputVarLat = new Variable(netCdfWriter.getNcfileWriter().getNetcdfFile(), null, null, latDim.getShortName());
        NetCdfWriter.setDim(outputVarLat, latDim);
        outputVarLat.setDataType(inputVarLat.getDataType());
        NetCdfWriter.copyAttributes(inputVarLat, outputVarLat);
        // Netcdf 4.0 method Removed
        // outputVarLat.setIsCoordinateAxis(latDim);
        // outputVarLat.setIOVar(inputVarLat);

        // netCdfWriter.writeDimension(latDim);
        // netCdfWriter.writeVariable(outputVarLat, null);
    }

    /**
     * Creates Longitude Dimension and Variable.
     */
    public void createLonVariable() {
        if (inputVarLon == null) {
            outputVarLon = null;
            return;
        }
        lonDim = new Dimension(inputVarLon.getShortName(), mapLon.size(), true);
        outputDims.put(lonDim.getFullName(), lonDim);

        outputVarLon = new Variable(netCdfWriter.getNcfileWriter().getNetcdfFile(), null, null, lonDim.getShortName());
        NetCdfWriter.setDim(outputVarLon, lonDim);
        outputVarLon.setDataType(inputVarLon.getDataType());
        NetCdfWriter.copyAttributes(inputVarLon, outputVarLon);
        // Netcdf 4.0 method Removed
        // outputVarLon.setIsCoordinateAxis(lonDim);
        // outputVarLon.setIOVar(inputVarLon);

        // netCdfWriter.writeDimension(lonDim);
        // netCdfWriter.writeVariable(outputVarLon, null);
    }

    /**
     * Fills Latitude values in its map and compute its position (index) in the output data array.
     * 
     * @throws MotuException
     * 
     */
    protected void storeLatitudeMap(Range[] yxRange, List<List<Range>> listYXRanges) throws MotuException {
        if (inputVarLat == null) {
            return;
        }

        Array data = null;
        try {
            // Reads data corresponding to the global range.
            List<Range> listRangeGlobal = new ArrayList<>();
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
        } catch (Exception e) {
            throw new MotuException(ErrorType.BAD_PARAMETERS, "ERROR encountered in DatasetGridXYLatLon - fillLatitudeMap", e);

        }

        // -----------------------------
        // Sets the right Lat range values
        // -----------------------------
        yRangeValue[0] = mapLat.firstKey();
        yRangeValue[1] = mapLat.lastKey();
    }

    /**
     * Fills Longitude values in its map and compute its position (index) in the output data array.
     * 
     * @throws MotuException
     * 
     */
    protected void storeLongitudeMap(Range[] yxRange, List<List<Range>> listYXRanges) throws MotuException {
        if (inputVarLon != null) {
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
            } catch (Exception e) {
                throw new MotuException(ErrorType.BAD_PARAMETERS, "ERROR encountered in DatasetGridXYLatLon - fillLongitudeMap", e);

            }
            // -----------------------------
            // Sets the right Lon range values
            // -----------------------------
            xRangeValue[0] = mapLon.firstKey();
            xRangeValue[1] = mapLon.lastKey();
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
    public void writeVariable(Variable outputVar, GridDataset gds, Range zRange, Range tRange, Range[] yxRange, List<List<Range>> listYXRanges)
            throws MotuException, MotuNotImplementedException {
        Variable inputVar = outputVar;
        GeoGrid geoGrid = gds.findGridByName(inputVar.getFullName());
        if (geoGrid == null) {
            throw new MotuNotImplementedException(
                    String.format("Variable %s in not geo-referenced - Non-georeferenced data is not implemented (method DatasetGrid.extractData)",
                                  inputVar.getFullName()));
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
                throw new MotuException(ErrorType.BAD_PARAMETERS, "Error in subsetting geo grid", e);
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
        }

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
        for (Iterator<int[]> it = keySet.iterator(); it.hasNext();) {
            Runtime.getRuntime().gc();

            int[] origin = null;
            int[] shape = null;
            origin = it.next();

            if (origin == null) {
                throw new MotuException(ErrorType.SYSTEM, "Error in NetCfdWriter finish - unable to find origin - (origin is null)");
            }

            shape = originAndShape.get(origin);

            if (shape == null) {
                throw new MotuException(ErrorType.SYSTEM, "Error in NetCfdWriter finish - unable to find shape - (shape is null)");
            }

            Array dataToFill = null;
            dataToFill = Array.factory(outputVar.getDataType(), shape);

            double fillValue = NetCdfReader.initializeMissingData(outputVar, dataToFill);

            for (List<Range> listYXRange : listYXRanges) {
                try {
                    geoGridSubset = geoGrid.subset(tRange, zRange, listYXRange.get(0), listYXRange.get(1));
                } catch (InvalidRangeException e) {
                    throw new MotuException(ErrorType.BAD_PARAMETERS, "Error in subsetting geo grid", e);
                }
                inputVarSubset = geoGridSubset.getVariable();
                if (NetCdfWriter.isReadByBlock(inputVarSubset)) {
                    fillDataByBlock(inputVarSubset, dataToFill);
                } else {
                    fillDataInOneGulp(inputVarSubset, dataToFill, origin, fillValue, yxRange);
                }
            }
            netCdfWriter.writeVariableData(outputVar, origin, dataToFill);
            dataToFill = null;

        }
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
        int[] origin = null;
        int[] shape = null;
        Array data = null;
        try {
            Map<int[], int[]> originAndShape = NetCdfWriter.parseOriginAndShape(var);

            Set<int[]> keySet = originAndShape.keySet();

            for (Iterator<int[]> it = keySet.iterator(); it.hasNext();) {
                origin = it.next();

                if (origin == null) {
                    throw new MotuException(
                            ErrorType.SYSTEM,
                            "Error in DatasetGridXYLatLon fillDataByBlock - unable to find origin - (origin is null)");
                }

                shape = originAndShape.get(origin);

                if (shape == null) {
                    throw new MotuException(
                            ErrorType.SYSTEM,
                            "Error in DatasetGridXYLatLon fillDataByBlock - unable to find shape - (shape is null)");
                }
                // CSOON: StrictDuplicateCode
                data = var.read(origin, shape);
                // writeVariableData(var, data);
            }
        } catch (IOException e) {
            throw new MotuException(ErrorType.BAD_PARAMETERS, "Error IOException in DatasetGridXYLatLon fillDataByBlock", e);
        } catch (InvalidRangeException e) {
            throw new MotuException(ErrorType.BAD_PARAMETERS, "Error InvalidRangeException in DatasetGridXYLatLon fillDataByBlock", e);
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
    protected void fillDataInOneGulp(Variable varToRead, Array dataToFill, int[] originToFill, double fillValue, Range[] yxRange)
            throws MotuException {
        Array dataRead = null;
        try {
            dataRead = varToRead.read();
        } catch (IOException e) {
            throw new MotuException(ErrorType.BAD_PARAMETERS, "Error in DatasetGridXYLatLon fillDataInOneGulp", e);
        }

        switch (dataRead.getRank()) {
        case 1:

            break;
        case 2:
            fillData2D(varToRead, dataRead, dataToFill, originToFill, fillValue, yxRange);
            break;
        case 3:

            break;
        case 4:

            break;

        default:
            break;
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
    public void fillData2D(Variable varSrc, Array dataSrc, Array dataDest, int[] originDest, double fillValue, Range[] yxRange) throws MotuException {
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
                            ErrorType.INVALID_LAT_LON_RANGE,
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
                dataDest.setDouble(imaOut.set(jDest, iDest), dataValue);
            }
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
