package fr.cls.atoll.motu.web.dal.request.extractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.exception.MotuExceedingCapacityException;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDateRangeException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidLatLonRangeException;
import fr.cls.atoll.motu.web.bll.exception.MotuNoVarException;
import fr.cls.atoll.motu.web.bll.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfVariableException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfVariableNotFoundException;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaDatetime;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaDepth;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaLatLon;
import fr.cls.atoll.motu.web.bll.request.model.RequestProduct;
import fr.cls.atoll.motu.web.bll.request.model.RequestProductParameters;
import fr.cls.atoll.motu.web.bll.request.model.RequestProductParameters.RangeComparator;
import fr.cls.atoll.motu.web.bll.request.model.metadata.DataProvider;
import fr.cls.atoll.motu.web.common.utils.CoordinateUtils;
import fr.cls.atoll.motu.web.common.utils.ListUtils;
import fr.cls.atoll.motu.web.dal.request.netcdf.NetCdfReader;
import fr.cls.atoll.motu.web.dal.request.netcdf.NetCdfWriter;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.VarData;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ParameterMetaData;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ProductMetaData;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.ma2.Range;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridDataset;
import ucar.unidata.geoloc.LatLonPointImpl;

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
public class DatasetGridManager extends DALAbstractDatasetManager {

    /** Contains variable attributes names to remove in output. */
    static final String[] VAR_ATTR_TO_REMOVE = new String[] { "Date_CNES_JD", "date", "_unsigned", "_CoordinateAxes" };

    /** Time dimension range values. */
    protected double[] tRangeValue;

    /** Y or Lat dimension range values. */
    protected double[] yRangeValue;

    /** X or Lon dimension range values. */
    protected double[] xRangeValue;

    /** List of each adjacent Lat dimension range values. */
    List<double[]> rangesLatValue;

    /** List of each adjacent Lon dimension range values. */
    List<double[]> rangesLonValue;

    /** Z dimension range values. */
    protected double[] zRangeValue;

    /** Has output time dimension. */
    protected boolean hasOutputTimeDimension = false;

    /** Has output latitude dimension. */
    protected boolean hasOutputLatDimension = false;

    /** Has output longitude dimension. */
    protected boolean hasOutputLonDimension = false;

    /** Has output Z dimension. */
    protected boolean hasOutputZDimension = false;

    /** The list distinct x range. */
    protected List<Range> listDistinctXRange = null;

    /** The list distinct y range. */
    protected List<Range> listDistinctYRange = null;

    protected List<List<Range>> adjacentYXRanges = null;

    /**
     * Constructeur.
     * 
     * @param requestProduct
     */
    public DatasetGridManager(RequestProduct rp) {
        super(rp);

        yRangeValue = new double[] { Double.MAX_VALUE, Double.MAX_VALUE, };
        xRangeValue = new double[] { Double.MAX_VALUE, Double.MAX_VALUE, };
        tRangeValue = new double[] { Double.MAX_VALUE, Double.MAX_VALUE, };
        zRangeValue = new double[] { Double.MAX_VALUE, Double.MAX_VALUE, };
        rangesLatValue = new ArrayList<>();
        rangesLonValue = new ArrayList<>();
    }

    /**
     * Extract data.
     * 
     * @param dataOutputFormat data output format (NetCdf, HDF, Ascii, ...).
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
    public void extractData() throws MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException, MotuNotImplementedException,
            MotuInvalidDepthRangeException, MotuInvalidLatLonRangeException, NetCdfVariableException, MotuNoVarException,
            NetCdfVariableNotFoundException, IOException {
        switch (getRequestProduct().getExtractionParameters().getDataOutputFormat()) {
        case NETCDF:
            extractDataIntoNetCdf();
            break;

        case NETCDF4:
            throw new MotuNotImplementedException(
                    String.format("extraction into %s is not implemented for OPENDAP (if you wish to enable it enable the NCSS)",
                                  getRequestProduct().getExtractionParameters().getDataOutputFormat().toString()));

        default:
            throw new MotuNotImplementedException(
                    String.format("extraction into %s is not implemented",
                                  getRequestProduct().getExtractionParameters().getDataOutputFormat().toString()));
        }
    }

    /**
     * Compute amount data size.
     * 
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuNoVarException the motu no var exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     * @throws IOException
     */
    @Override
    public double computeAmountDataSize() throws MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException,
            MotuNotImplementedException, MotuInvalidDepthRangeException, MotuInvalidLatLonRangeException, NetCdfVariableException, MotuNoVarException,
            NetCdfVariableNotFoundException, IOException {
        initGetAmountData();

        Range tRange = initTimeRange();
        // gets ranges to be extracted
        // Number of ranges for Longitude can be 1 or 2.
        List<List<Range>> listYXRanges = getAdjacentYXRange();
        Range zRange = initZRange();

        try (GridDataset gds = new GridDataset(getRequestProduct().getProduct().getNetCdfReaderDataset())) {
            NetCdfReader reader = getRequestProduct().getProduct().getNetCdfReader();
            NetCdfWriter netCdfWriter = new NetCdfWriter();

            netCdfWriter.resetAmountDataSize();

            HashSet<Dimension> dimensions = new HashSet<>();
            List<String> countedVar = new ArrayList<>();

            for (VarData varData : getRequestProduct().getRequestProductParameters().getVariables().values()) {

                GeoGrid geoGrid = gds.findGridByName(varData.getVarName());
                if (geoGrid != null) {
                    List<GeoGrid> listGeoGridSubset = new ArrayList<>();

                    for (List<Range> yxRanges : listYXRanges) {
                        Range yRange = yxRanges.get(0);
                        Range xRange = yxRanges.get(1);

                        GeoGrid geoGridSubset = null;
                        try {
                            geoGridSubset = geoGrid.subset(tRange, zRange, yRange, xRange);
                            listGeoGridSubset.add(geoGridSubset);
                            dimensions.addAll(geoGridSubset.getVariable().getDimensions());
                            countedVar.add(geoGridSubset.getVariable().getFullNameEscaped());
                        } catch (InvalidRangeException e) {
                            throw new MotuException(ErrorType.BAD_PARAMETERS, "Error in subsetting geo grid", e);
                        }
                    }
                    netCdfWriter.computeAmountDataSize(listGeoGridSubset);
                } else {
                    computeSizeOfNotGriddedVariables(tRange, zRange, reader, netCdfWriter, dimensions, countedVar, varData);
                }
            }

            addDataSizeOfDimensions(tRange, zRange, netCdfWriter, dimensions, countedVar);

            amountDataSize = netCdfWriter.getAmountDataSize();
        }
        return amountDataSize;
    }

    /**
     * Compute the amount data size of not gridded variables (scalar, bounds ...) .
     * 
     * @param tRange
     * @param zRange
     * @param reader
     * @param netCdfWriter
     * @param dimensions
     * @param countedVar
     * @param varData
     * @throws NetCdfVariableNotFoundException
     * @throws MotuException
     * @throws MotuNotImplementedException
     * @throws MotuInvalidLatLonRangeException
     */
    private void computeSizeOfNotGriddedVariables(Range tRange,
                                                  Range zRange,
                                                  NetCdfReader reader,
                                                  NetCdfWriter netCdfWriter,
                                                  HashSet<Dimension> dimensions,
                                                  List<String> countedVar,
                                                  VarData varData)
            throws NetCdfVariableNotFoundException, MotuException, MotuInvalidLatLonRangeException, MotuNotImplementedException {
        Variable scalarVar = reader.getVariable(varData.getVarName());
        if (scalarVar != null) {
            List<List<Range>> ranges = prepareNotGriddedVariableRanges(scalarVar, zRange, tRange);
            for (List<Range> rangeList : ranges) {
                try {
                    netCdfWriter.addAmountDataSize(scalarVar.section(rangeList));
                } catch (InvalidRangeException e) {
                    throw new MotuException(
                            ErrorType.INVALID_LAT_LON_RANGE,
                            "Error when trying to compute the size of the variable " + scalarVar.getFullName(),
                            e);
                }
            }
            dimensions.addAll(scalarVar.getDimensions());
            countedVar.add(scalarVar.getFullNameEscaped());
        }
    }

    /**
     * Add the sizes of the dimensions if not already counted as variables .
     * 
     * @param tRange
     * @param zRange
     * @param netCdfWriter
     * @param dimensions
     * @param countedVar
     * @throws MotuException
     * @throws MotuNotImplementedException
     * @throws MotuInvalidLatLonRangeException
     */
    private void addDataSizeOfDimensions(Range tRange,
                                         Range zRange,
                                         NetCdfWriter netCdfWriter,
                                         HashSet<Dimension> dimensions,
                                         List<String> countedVar)
            throws MotuException, MotuInvalidLatLonRangeException, MotuNotImplementedException {
        for (Dimension d : dimensions) {
            String dimName = d.getFullNameEscaped();
            if (!countedVar.contains(dimName)) {
                Variable dimVar = getRequestProduct().getProduct().getNetCdfReaderDataset().findVariable(dimName);
                if (dimVar != null) {
                    for (List<Range> section : prepareNotGriddedVariableRanges(dimVar, zRange, tRange)) {
                        try {
                            netCdfWriter.addAmountDataSize(dimVar.section(section));
                        } catch (InvalidRangeException e) {
                            throw new MotuException(
                                    ErrorType.INVALID_LAT_LON_RANGE,
                                    "Error when trying to compute the size of the dimension " + dimVar.getFullName(),
                                    e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Inits the get amount data.
     *
     * @throws MotuException the motu exception
     * @throws MotuNoVarException the motu no var exception
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     */
    public void initGetAmountData() throws MotuException, MotuNoVarException, NetCdfVariableNotFoundException {
        if (getRequestProduct().getRequestProductParameters().getVariables().isEmpty()) {
            throw new MotuNoVarException("Variable list is empty");
        }

        getRequestProduct().getDataBaseExtractionTimeCounter().addReadingTime(getRequestProduct().getProduct().openNetCdfReader(false));

        setHasOutputDimension();
    }

    /**
     * Prepare lat lon writing.
     * 
     * @param geoGridSubset the geo grid subset
     * @param yRange the y range
     * @param xRange the x range
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    protected void prepareLatLonWriting(GeoGrid geoGridSubset, Range yRange, Range xRange, Map<String, List<Section>> mapVarOrgRanges)
            throws MotuException, MotuNotImplementedException {
        CoordinateAxis xaxis = geoGridSubset.getCoordinateSystem().getXHorizAxis();
        CoordinateAxis yaxis = geoGridSubset.getCoordinateSystem().getYHorizAxis();

        // Bug the Netcdf-java Library in subset method of GeoGrid, the axis Type is not always set.
        if (xaxis.getAxisType() == null) {
            xaxis.setAxisType(getRequestProduct().getProduct().getCoordinateAxisType(xaxis.getFullName()));
        }
        if (yaxis.getAxisType() == null) {
            yaxis.setAxisType(getRequestProduct().getProduct().getCoordinateAxisType(yaxis.getFullName()));
        }

        if ((xaxis.getAxisType() == AxisType.GeoX) || (yaxis.getAxisType() == AxisType.GeoY)) {
            return;
        }

        if (xaxis.getAxisType() == null) {
            throw new MotuException(
                    ErrorType.INVALID_LONGITUDE,
                    String.format("ERROR in DatasetGrid#prepareLatLonWriting - axis type for '%s' axis is null", xaxis.getFullName()));
        }
        if (yaxis.getAxisType() == null) {
            throw new MotuException(
                    ErrorType.INVALID_LATITUDE,
                    String.format("ERROR in DatasetGrid#prepareLatLonWriting - axis type for '%s' axis is null", yaxis.getFullName()));
        }

        String xName = xaxis.getFullName();
        List<Section> listVarOrgRanges = mapVarOrgRanges.get(xName);
        if (ListUtils.isNullOrEmpty(listVarOrgRanges)) {
            listVarOrgRanges = new ArrayList<>();
            mapVarOrgRanges.put(xName, listVarOrgRanges);
        }

        Section section = getOriginalRangeListGeoAxis(xaxis, yRange, xRange);
        listVarOrgRanges.add(section);

        String yName = yaxis.getFullName();
        listVarOrgRanges = mapVarOrgRanges.get(yName);
        if (ListUtils.isNullOrEmpty(listVarOrgRanges)) {
            listVarOrgRanges = new ArrayList<>();
            mapVarOrgRanges.put(yName, listVarOrgRanges);
        }

        section = getOriginalRangeListGeoAxis(yaxis, yRange, xRange);
        listVarOrgRanges.add(section);

    }

    private void prepareXYWritingAxisY(Map<String, Range> mapYRange,
                                       List<CoordinateAxis> listVariableYSubset,
                                       Map<String, List<Section>> mapVarOrgRanges)
            throws MotuNotImplementedException, MotuException {
        RangeComparator rangeComparator = new RangeComparator();
        listDistinctYRange = new ArrayList<>(mapYRange.values());
        Collections.sort(listDistinctYRange, rangeComparator);
        for (Range range : listDistinctYRange) {
            CoordinateAxis axis = subset(getRequestProduct().getProduct().getGeoYAxis(), range);
            listVariableYSubset.add(axis);
            String axisName = axis.getFullName();
            List<Section> listVarOrgRanges = mapVarOrgRanges.get(axisName);
            if (ListUtils.isNullOrEmpty(listVarOrgRanges)) {
                listVarOrgRanges = new ArrayList<>();
                mapVarOrgRanges.put(axisName, listVarOrgRanges);
            }
            List<Range> lr = new ArrayList<>();
            lr.add(range);
            Section section = new Section(lr);
            listVarOrgRanges.add(section);
        }
    }

    private void prepareXYWritingAxisX(Map<String, Range> mapXRange,
                                       List<CoordinateAxis> listVariableXSubset,
                                       Map<String, List<Section>> mapVarOrgRanges)
            throws MotuNotImplementedException, MotuException {
        RangeComparator rangeComparator = new RangeComparator();
        listDistinctXRange = new ArrayList<>(mapXRange.values());
        Collections.sort(listDistinctXRange, rangeComparator);
        for (Range range : listDistinctXRange) {
            CoordinateAxis axis = subset(getRequestProduct().getProduct().getGeoXAxis(), range);
            listVariableXSubset.add(axis);
            String axisName = axis.getFullName();
            List<Section> listVarOrgRanges = mapVarOrgRanges.get(axisName);
            if (ListUtils.isNullOrEmpty(listVarOrgRanges)) {
                listVarOrgRanges = new ArrayList<>();
                mapVarOrgRanges.put(axisName, listVarOrgRanges);
            }
            List<Range> lr = new ArrayList<>();
            lr.add(range);
            Section section = new Section(lr);
            listVarOrgRanges.add(section);
        }
    }

    /**
     * Prepare xy writing.
     * 
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuInvalidLatLonRangeException
     */
    protected void prepareXYMapAndList(Map<String, Range> mapXRange,
                                       Map<String, Range> mapYRange,
                                       List<CoordinateAxis> listVariableXSubset,
                                       List<CoordinateAxis> listVariableYSubset,
                                       Map<String, List<Section>> mapVarOrgRanges)
            throws MotuException, MotuNotImplementedException, MotuInvalidLatLonRangeException {
        for (List<Range> yxRanges : getAdjacentYXRange()) {
            Range yRange = yxRanges.get(0);
            addRange(yRange, mapYRange);

            Range xRange = yxRanges.get(1);
            addRange(xRange, mapXRange);
        }

        prepareXYWritingAxisY(mapYRange, listVariableYSubset, mapVarOrgRanges);
        prepareXYWritingAxisX(mapXRange, listVariableXSubset, mapVarOrgRanges);
    }

    /**
     * Checks dimension in output.
     * 
     * @throws MotuException the net cdf variable not found exception
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     */
    public void setHasOutputDimension() throws MotuException, NetCdfVariableNotFoundException {
        hasOutputTimeDimension = false;
        hasOutputLatDimension = false;
        hasOutputLonDimension = false;
        hasOutputZDimension = false;

        for (VarData varData : getRequestProduct().getRequestProductParameters().getVariables().values()) {
            Variable variable = getRequestProduct().getProduct().getNetCdfReader().getRootVariable(varData.getVarName());
            List<Dimension> dimsVar = variable.getDimensions();
            for (Dimension dim : dimsVar) {
                CoordinateAxis coord = getCoordinateVariable(dim);
                if (coord != null) {
                    hasOutputTimeDimension |= coord.getAxisType() == AxisType.Time;
                    hasOutputLatDimension |= coord.getAxisType() == AxisType.Lat;
                    hasOutputLonDimension |= coord.getAxisType() == AxisType.Lon;
                    if (getRequestProduct().getProduct().getNetCdfReader().hasGeoXYAxisWithLonLatEquivalence()) {
                        hasOutputLatDimension |= coord.getAxisType() == AxisType.GeoY;
                        hasOutputLonDimension |= coord.getAxisType() == AxisType.GeoX;
                    }
                    hasOutputZDimension |= (coord.getAxisType() == AxisType.GeoZ) || (coord.getAxisType() == AxisType.Height);
                }
            }
        }
    }

    /**
     * Gets the coordinate variable.
     * 
     * @param dim the dim
     * 
     * @return the coordinate variable
     * 
     * @throws MotuException the netcdf variable not found
     */
    public CoordinateAxis getCoordinateVariable(Dimension dim) throws MotuException {
        return getRequestProduct().getProduct().getNetCdfReader().getCoordinateVariable(dim);
    }

    private NetCdfWriter initNetCdfWriterWithGlobalAttributes() throws IOException, MotuException {
        NetCdfWriter netCdfWriter = new NetCdfWriter(
                getRequestProduct().getRequestProductParameters().getExtractLocationDataTemp(),
                getRequestProduct().getExtractionParameters().getDataOutputFormat());
        netCdfWriter.writeGlobalAttributes(initializeNetCdfFixedGlobalAttributes());
        netCdfWriter.writeGlobalAttributes(initializeNetCdfDynGlobalAttributes());
        netCdfWriter.resetAmountDataSize();
        return netCdfWriter;
    }

    private void initMapAndListForGeoXY(NetCdfWriter netCdfWriter, Map<String, List<Section>> mapVarOrgRanges)
            throws MotuNotImplementedException, MotuException, MotuExceedingCapacityException, MotuInvalidLatLonRangeException {
        Map<String, Range> mapXRange = new HashMap<>();
        Map<String, Range> mapYRange = new HashMap<>();
        List<CoordinateAxis> listVariableXSubset = new ArrayList<>();
        List<CoordinateAxis> listVariableYSubset = new ArrayList<>();
        // If GeoXY then compute the X/Y dim and subset the X/Y variables
        prepareXYMapAndList(mapXRange, mapYRange, listVariableXSubset, listVariableYSubset, mapVarOrgRanges);
        // Write variables X/Y definitions.
        netCdfWriter.prepareVariablesInMap(listVariableXSubset, mapXRange);
        netCdfWriter.prepareVariablesInMap(listVariableYSubset, mapYRange);
        addGeoXYNeededVariables();
    }

    private void initVariablesMapFromGridData(NetCdfWriter netCdfWriter,
                                              GridDataset gds,
                                              GeoGrid geoGrid,
                                              Map<String, List<Section>> mapVarOrgRanges,
                                              boolean isGeoXY,
                                              Range zRange,
                                              Range tRange)
            throws MotuNotImplementedException, MotuException, MotuExceedingCapacityException, MotuInvalidLatLonRangeException {
        List<GeoGrid> listGeoGridSubset = computeGeoGridSubset(mapVarOrgRanges, isGeoXY, geoGrid, zRange, tRange);
        initVariablesMap(netCdfWriter, isGeoXY, listGeoGridSubset, geoGrid, gds);
    }

    private List<GeoGrid> computeGeoGridSubset(Map<String, List<Section>> mapVarOrgRanges,
                                               boolean isGeoXY,
                                               GeoGrid geoGrid,
                                               Range zRange,
                                               Range tRange)
            throws MotuException, MotuNotImplementedException, MotuInvalidLatLonRangeException {
        List<GeoGrid> listGeoGridSubset = new ArrayList<>();
        for (List<Range> yxRanges : getAdjacentYXRange()) {
            Range yRange = yxRanges.get(0);
            Range xRange = yxRanges.get(1);

            GeoGrid geoGridSubset = null;
            try {
                // -----------------------------------------------------------------------
                // WARNING :
                //
                // section method of Variable create a new instance of the class VariableDS from the
                // original variable, but some informations are lost (as Fillvalue).
                // And Subset of GeoGrid uses section method.
                //
                // Example :
                // ...
                // VariableDS v_section = (VariableDS) v.section(rangesList);
                // ...
                // v is an instance of class VariableDS and the attribute fillValue of Attribute
                // smProxy is set and hasFillValue is set to true.
                // After calling v.section, the attribute fillValue of attribute smProxy of v_section
                // is not set and hasFillValue is set to false.
                //
                // So, when you work with v_section variable and you called hasFillValue method, it
                // returns false whereas with the original variable v, hasFillValue method returns
                // true.
                // -----------------------------------------------------------------------
                geoGridSubset = geoGrid.subset(tRange, zRange, yRange, xRange);
                listGeoGridSubset.add(geoGridSubset);

                // if GeoXY then compute the original range for the subset variable.
                // This is already done in the geo-grid but the information (original range is private
                // and there is no getter on it)
                if (isGeoXY) {
                    String varName = geoGridSubset.getVariable().getFullName();
                    List<Section> listVarOrgRanges = mapVarOrgRanges.get(varName);
                    if (ListUtils.isNullOrEmpty(listVarOrgRanges)) {
                        listVarOrgRanges = new ArrayList<>();
                        mapVarOrgRanges.put(varName, listVarOrgRanges);
                    }

                    Section section = getOriginalRangeList(geoGridSubset, tRange, zRange, yRange, xRange);
                    listVarOrgRanges.add(section);

                    // if coordinate axes of the geogrid are Lat/Lon
                    // then compute the original range for the subset Lat/lon.
                    prepareLatLonWriting(geoGridSubset, yRange, xRange, mapVarOrgRanges);
                }
            } catch (InvalidRangeException e) {
                throw new MotuException(ErrorType.BAD_PARAMETERS, "Error in subsetting geo grid", e);
            }
        }
        return listGeoGridSubset;
    }

    private void initVariablesMap(NetCdfWriter netCdfWriter, boolean isGeoXY, List<GeoGrid> listGeoGridSubset, GeoGrid geoGrid, GridDataset gds)
            throws MotuNotImplementedException, MotuExceedingCapacityException, MotuException {
        // pass geoGridsubset and geoGrid (the original geoGrid) to be able to get some
        // information
        // (lost
        // in subsetting - See bug below) about the variable of the GeoGrid
        if (isGeoXY) {
            netCdfWriter.initVariablesMapWithGeoXY(listGeoGridSubset,
                                                   geoGrid,
                                                   gds,
                                                   getRequestProduct().getProduct().getNetCdfReader().getOrignalVariables());

        } else {
            netCdfWriter.initVariablesMap(listGeoGridSubset, geoGrid, gds, getRequestProduct().getProduct().getNetCdfReader().getOrignalVariables());
        }
    }

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
    public void extractDataIntoNetCdf() throws MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException,
            MotuNotImplementedException, MotuInvalidDepthRangeException, MotuInvalidLatLonRangeException, MotuNoVarException, NetCdfVariableException,
            NetCdfVariableNotFoundException, IOException {
        initNetCdfExtraction();

        Range tRange = initTimeRange();
        // gets ranges to be extracted. Number of ranges for Longitude can be 1 or 2.
        Range zRange = initZRange();

        NetCdfWriter netCdfWriter = initNetCdfWriterWithGlobalAttributes();

        // -------------------------------------------------
        // If GeoXY then compute the X/Y dim and subset the X/Y variables
        // Write variables X/Y definitions.
        // Add Lat/Lon variables and variable concerning projection to list of variables to extract
        // WARN: this must be done first to compute and subset the right output (dim and var. X/Y range).
        // -------------------------------------------------
        // Is this dataset a Geo X/Y with Lat/Lon whose dimensions depend on X/Y ?
        boolean isGeoXY = getRequestProduct().getProduct().getProductMetaData().hasGeoXYAxisWithLonLatEquivalence();

        Map<String, List<Section>> mapVarOrgRangesForXY = new HashMap<>();
        if (isGeoXY) {
            initMapAndListForGeoXY(netCdfWriter, mapVarOrgRangesForXY);
        }

        EnumSet<NetcdfDataset.Enhance> x = EnumSet.noneOf(NetcdfDataset.Enhance.class);
        x.add(NetcdfDataset.Enhance.CoordSystems);

        try (GridDataset netcdfGridDS = GridDataset.open(getRequestProduct().getProduct().getLocationData(), x)) {
            for (VarData requestedVarData : getRequestProduct().getRequestProductParameters().getVariables().values()) {
                GeoGrid requestedGeoGrid = netcdfGridDS.findGridByName(requestedVarData.getVarName());
                if (requestedGeoGrid != null) {
                    initVariablesMapFromGridData(netCdfWriter, netcdfGridDS, requestedGeoGrid, mapVarOrgRangesForXY, isGeoXY, zRange, tRange);
                }
            }
            extractNotGriddedVariables(tRange, zRange, netCdfWriter);

            if (isGeoXY) {
                netCdfWriter.finishGeoXY(VAR_ATTR_TO_REMOVE, listDistinctXRange, listDistinctYRange, mapVarOrgRangesForXY);
            } else {
                netCdfWriter.finish(VAR_ATTR_TO_REMOVE);
            }
        }
        getRequestProduct().getDataBaseExtractionTimeCounter().addReadingTime(netCdfWriter.getReadingTime());
        getRequestProduct().getDataBaseExtractionTimeCounter().addWritingTime(netCdfWriter.getWritingTime());

        moveTempExtractFileToFinalExtractFile();
    }

    private void extractNotGriddedVariables(Range tRange, Range zRange, NetCdfWriter netCdfWriter)
            throws MotuException, MotuInvalidLatLonRangeException, MotuNotImplementedException {
        for (String requestedVar : getRequestProduct().getRequestProductParameters().getVariables().keySet()) {
            if (!netCdfWriter.getVariables().containsKey(requestedVar)) {
                Variable originalVar = getRequestProduct().getProduct().getNetCdfReaderDataset().findVariable(requestedVar);
                if (originalVar != null) {
                    checkAndAddDimensions(netCdfWriter, originalVar);
                    List<List<Range>> rangesByRequest = prepareNotGriddedVariableRanges(originalVar, zRange, tRange);
                    putScalarVariables(netCdfWriter, requestedVar, originalVar, rangesByRequest);
                } else {
                    throw new MotuException(ErrorType.BAD_PARAMETERS, "Variable '" + requestedVar + "' does not exist.");
                }
            }
        }
    }

    /**
     * Check that dimension has been added or try to find add the corresponding Variables .
     * 
     * @param netCdfWriter
     * @param originalVar
     */
    private void checkAndAddDimensions(NetCdfWriter netCdfWriter, Variable originalVar) {

        for (Dimension d : originalVar.getDimensions()) {
            if (!netCdfWriter.hasDimension(d)) {
                netCdfWriter.putDimension(d);
                String dimName = d.getFullNameEscaped();
                if (!getRequestProduct().getRequestProductParameters().getVariables().containsKey(dimName)) {
                    Variable dimVar = getRequestProduct().getProduct().getNetCdfReaderDataset().findVariable(dimName);
                    if (dimVar != null) {
                        netCdfWriter.putVariables(dimName, dimVar);
                    }
                }
            }
        }
    }

    /**
     * From existing Ranges extracted from query, build the Range list (ordered by dimension) for the not
     * gridded Variable in parameter. .
     * 
     * @param var The variable on which to analyze the dimension to produce the corresponding List of Range.
     * @param zRange The depth Range
     * @param tRange The time Range
     * @return The list of List of Range for each section of the variable to extract.
     * @throws MotuException
     * @throws MotuNotImplementedException
     * @throws MotuInvalidLatLonRangeException
     */
    private List<List<Range>> prepareNotGriddedVariableRanges(Variable var, Range zRange, Range tRange)
            throws MotuException, MotuInvalidLatLonRangeException, MotuNotImplementedException {
        List<List<Range>> listYXRanges = getAdjacentYXRange();
        List<List<Range>> allRanges = new ArrayList<>();
        int maxSize = 0;
        for (Dimension dim : var.getDimensions()) {
            CoordinateAxis coord = getCoordinateVariable(dim);
            Range range = null;
            List<Range> listRange = null;
            if (coord != null && coord.getAxisType() != null) {
                switch (coord.getAxisType()) {
                case GeoZ:
                case Height:
                    range = zRange;
                    break;
                case GeoY:
                case Lat:
                    listRange = extractLonRange(listYXRanges);
                    break;
                case GeoX:
                case Lon:
                    listRange = extractLatRange(listYXRanges);
                    break;
                case Time:
                    range = tRange;
                    break;
                default:
                    break;
                }
            }
            if (listRange == null) {
                listRange = new ArrayList<>();
                listRange.add(range);
            }
            maxSize = Math.max(listRange.size(), maxSize);
            allRanges.add(listRange);
        }
        return groupRangesByRequest(allRanges, maxSize);
    }

    private List<Range> extractLatRange(List<List<Range>> listYXRanges) {
        return extractRange(listYXRanges, 1);
    }

    private List<Range> extractLonRange(List<List<Range>> listYXRanges) {
        return extractRange(listYXRanges, 0);
    }

    private List<Range> extractRange(List<List<Range>> listYXRanges, int index) {
        List<Range> listRange;
        listRange = new ArrayList<>();
        for (List<Range> rangeLatLon : listYXRanges) {
            listRange.add(rangeLatLon.get(index));
        }
        if (listYXRanges.isEmpty()) {
            listRange.add(null);
        }
        return listRange;
    }

    private List<List<Range>> groupRangesByRequest(List<List<Range>> allRanges, int maxSize) throws MotuException {
        List<List<Range>> rangesByRequest = new ArrayList<>();
        for (int i = 0; i < maxSize; i++) {
            List<Range> rangeRequest = new ArrayList<>();
            for (List<Range> rangeDim : allRanges) {
                if (rangeDim.size() == 1) {
                    rangeRequest.add(rangeDim.get(0));
                } else if (rangeDim.size() == maxSize) {
                    rangeRequest.add(rangeDim.get(i));
                } else {
                    // Inconsistent dimensions sets
                    throw new MotuException(ErrorType.NETCDF_VARIABLE_NOT_FOUND, "");
                }
            }
            rangesByRequest.add(rangeRequest);
        }
        return rangesByRequest;
    }

    private void putScalarVariables(NetCdfWriter netCdfWriter, String varName, Variable var, List<List<Range>> rangesByRequest) throws MotuException {
        try {
            for (List<Range> ranges : rangesByRequest) {
                Variable varRequested = var.section(ranges);
                netCdfWriter.putVariables(varName, varRequested);
            }
        } catch (InvalidRangeException e) {
            throw new MotuException(ErrorType.NETCDF_VARIABLE, e.getMessage(), e);
        }
    }

    // ======================================

    private void checkRequestVariableListIsNotEmpty() throws MotuNoVarException {
        if (getRequestProduct().getRequestProductParameters().getVariables().isEmpty()) {
            throw new MotuNoVarException("Variable list is empty");
        }
    }

    private void openNetCdfReader() throws MotuException {
        getRequestProduct().getDataBaseExtractionTimeCounter().addReadingTime(getRequestProduct().getProduct().openNetCdfReader(false));
    }

    private void initNetCdfOutputFileName() {
        // Create output NetCdf file
        String fileName = NetCdfWriter.getUniqueNetCdfFileName(getRequestProduct().getProduct().getProductId());
        getRequestProduct().getRequestProductParameters().setExtractFilename(fileName);
    }

    /**
     * NetCdf extraction initialization.
     *
     * @throws MotuException the motu exception
     * @throws MotuNoVarException the motu no var exception
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     */
    public void initNetCdfExtraction() throws MotuException, MotuNoVarException, NetCdfVariableNotFoundException {
        if (getRequestProduct().getProduct() != null) {
            checkRequestVariableListIsNotEmpty();
            openNetCdfReader();
            initNetCdfOutputFileName();
            setHasOutputDimension();
        }
    }

    /**
     * Init time range and range values to be extracted.
     * 
     * @throws MotuInvalidDateRangeException
     * @throws MotuException
     */
    public Range initTimeRange() throws MotuException, MotuInvalidDateRangeException {
        if (tRangeValue != null) {
            assert tRangeValue.length == 2;
            tRangeValue[0] = Double.MAX_VALUE;
            tRangeValue[1] = Double.MAX_VALUE;
        }

        Range tRange = null;
        Product curProduct = getRequestProduct().getProduct();
        if (curProduct.getProductMetaData().hasTimeAxis()) {
            ExtractCriteriaDatetime extractCriteriaDatetime = getRequestProduct().getRequestProductParameters().findCriteriaDatetime();
            if (extractCriteriaDatetime != null) {
                Array timeAxisData = curProduct.getTimeAxisData();
                CoordinateAxis timeAxis = curProduct.getProductMetaData().getTimeAxis();
                tRange = extractCriteriaDatetime.toRange(timeAxisData, timeAxis.getUnitsString(), tRangeValue);
            }
        }
        return tRange;
    }

    /**
     * Gets Y/X (Lat/Lon or GeoX/GeoY) range and ranges values to be extracted.
     * 
     * @throws MotuException
     * @throws MotuNotImplementedException
     * @throws MotuInvalidLatLonRangeException
     */
    public Range[] getYXRange() throws MotuException, MotuInvalidLatLonRangeException, MotuNotImplementedException {
        if (yRangeValue != null) {
            assert yRangeValue.length == 2;
            yRangeValue[0] = Double.MAX_VALUE;
            yRangeValue[1] = Double.MAX_VALUE;
        }
        if (xRangeValue != null) {
            assert xRangeValue.length == 2;
            xRangeValue[0] = Double.MAX_VALUE;
            xRangeValue[1] = Double.MAX_VALUE;
        }

        Range[] yxRange = null;
        if (getRequestProduct().getProduct().getProductMetaData().hasLatLonAxis()) {
            yxRange = new Range[2];
            ExtractCriteriaLatLon extractCriteriaLatLon = getRequestProduct().getRequestProductParameters().findCriteriaLatLon();
            if (extractCriteriaLatLon != null) {
                CoordinateSystem cs = new CoordinateSystem(
                        getRequestProduct().getProduct().getNetCdfReaderDataset(),
                        getRequestProduct().getProduct().getProductMetaData().getLatLonAxis(),
                        null);
                List<Range> yxRanges = extractCriteriaLatLon.toRange(cs, yRangeValue, xRangeValue);
                if (yxRanges.size() == 2) {
                    yxRange[0] = yxRanges.get(0);
                    yxRange[1] = yxRanges.get(1);
                }
            }
        } else if (getRequestProduct().getProduct().getProductMetaData().hasGeoXYAxis()) {
            throw new MotuNotImplementedException("X/Y axis is not implemented (method DatasetGrid.getYXRange");
        }
        return yxRange;
    }

    /**
     * Gets Z range and ranges values to be extracted.
     * 
     * @throws MotuInvalidDepthRangeException
     * @throws MotuException
     * @throws NetCdfVariableException
     * 
     */
    public Range initZRange() throws MotuException, MotuInvalidDepthRangeException {
        if (zRangeValue != null) {
            zRangeValue[0] = Double.MAX_VALUE;
            zRangeValue[1] = Double.MAX_VALUE;
        }

        Range zRange = null;
        if (getRequestProduct().getProduct().getProductMetaData().hasZAxis()) {
            ExtractCriteriaDepth extractCriteriaDepth = getRequestProduct().getRequestProductParameters().findCriteriaDepth();
            if (extractCriteriaDepth != null) {
                Array zAxisData = getRequestProduct().getProduct().getZAxisData();
                zRange = extractCriteriaDepth.toRange(zAxisData, zRangeValue);
            }
        }

        return zRange;
    }

    private void resetXYRangeValueArrayToMaxValue() {
        if (yRangeValue != null) {
            assert yRangeValue.length == 2;
            yRangeValue[0] = Double.MAX_VALUE;
            yRangeValue[1] = Double.MAX_VALUE;
        }
        if (xRangeValue != null) {
            assert xRangeValue.length == 2;
            xRangeValue[0] = Double.MAX_VALUE;
            xRangeValue[1] = Double.MAX_VALUE;
        }
    }

    private void checklistYXRangesConsistency(List<List<Range>> listYXRanges) throws MotuException {
        if (listYXRanges.size() != rangesLonValue.size()) {
            throw new MotuException(
                    ErrorType.INCONSISTENCY,
                    String.format("Inconsistency between Longitude ranges list (%d items) and Longitude values list (%d items) - (%s)",
                                  listYXRanges.size(),
                                  rangesLonValue.size(),
                                  this.getClass().getName()));
        }

        if (listYXRanges.size() != rangesLatValue.size()) {
            throw new MotuException(
                    ErrorType.INCONSISTENCY,
                    String.format("Inconsistency between Latitude ranges list (%d items) and Latitude values list (%d items) - (%s)",
                                  listYXRanges.size(),
                                  rangesLatValue.size(),
                                  this.getClass().getName()));
        }
    }

    private ExtractCriteriaLatLon initExtractCriteriaLatLon() {
        ExtractCriteriaLatLon extractCriteriaLatLon = getRequestProduct().getRequestProductParameters().findCriteriaLatLon();
        if (extractCriteriaLatLon == null) {
            extractCriteriaLatLon = new ExtractCriteriaLatLon();
            getRequestProduct().getRequestProductParameters().getListCriteria().add(extractCriteriaLatLon);
        }
        return extractCriteriaLatLon;
    }

    protected void checkMinMaxNotNull(MAMath.MinMax minMaxLat, ErrorType err) throws MotuException {
        if (minMaxLat == null) {
            String latLonMsg = err == ErrorType.INVALID_LATITUDE ? "latitude" : "longitude";
            throw new MotuException(
                    err,
                    "Error in DatasetGrid#getAdjacentYXRange: Latitude/Longitude axes are 2D and min/max " + latLonMsg
                            + " values to extract are null");
        }
    }

    /**
     * Gets a list of Index Ranges for the given lat, lon bounding box. For projection, only an approximation
     * based on lat/lon corners. Must have 2D/LatLon for x and y axis.
     * 
     * @throws MotuNotImplementedException
     * @throws MotuException
     */
    public List<List<Range>> getAdjacentYXRange() throws MotuException, MotuInvalidLatLonRangeException, MotuNotImplementedException {
        if (adjacentYXRanges == null) {
            adjacentYXRanges = initAdjacentYXRange();
        }
        return adjacentYXRanges;
    }

    /**
     * Gets a list of Index Ranges for the given lat, lon bounding box. For projection, only an approximation
     * based on lat/lon corners. Must have 2D/LatLon for x and y axis.
     * 
     * @throws MotuNotImplementedException
     * @throws MotuException
     */
    private List<List<Range>> initAdjacentYXRange() throws MotuException, MotuInvalidLatLonRangeException, MotuNotImplementedException {
        resetXYRangeValueArrayToMaxValue();
        List<List<Range>> listYXRanges = null;

        if (getRequestProduct().getProduct().getProductMetaData().hasLatLonAxis()) {
            ExtractCriteriaLatLon extractCriteriaLatLon = initExtractCriteriaLatLon();
            CoordinateSystem cs = getRequestProduct().getProduct().getProductMetaData().getCoordinateSystem();
            try {
                listYXRanges = extractCriteriaLatLon.toListRanges(cs, rangesLatValue, rangesLonValue);
            } catch (InvalidRangeException e) {
                throw new MotuNotImplementedException("Error during range creation while filling Gaps #initAdjacentYXRange");
            }
            checklistYXRangesConsistency(listYXRanges);

            MAMath.MinMax minMaxLat;
            MAMath.MinMax minMaxLon;
            if (getRequestProduct().getProduct().getProductMetaData().hasLatLonAxis2D()) {
                minMaxLat = extractCriteriaLatLon.getMinMaxYValue2D();
                minMaxLon = extractCriteriaLatLon.getMinMaxXValue2D();
                checkMinMaxNotNull(minMaxLat, ErrorType.INVALID_LATITUDE);
                checkMinMaxNotNull(minMaxLon, ErrorType.INVALID_LONGITUDE);
            } else {
                minMaxLat = getMinMaxLatNormal();
                minMaxLon = getMinMaxLonNormal(listYXRanges);
            }

            yRangeValue[0] = minMaxLat.min;
            yRangeValue[1] = minMaxLat.max;

            xRangeValue[0] = CoordinateUtils.getLongitudeM180P180(minMaxLon.min);
            xRangeValue[1] = CoordinateUtils.getLongitudeM180P180(minMaxLon.max);
        } else if (getRequestProduct().getProduct().getProductMetaData().hasGeoXYAxis()) {
            throw new MotuNotImplementedException(
                    "Extraction with X/Y axes and without Lat/Lon data are not implemented (method DatasetGrid#getAdjacentYXRange");
        }
        return listYXRanges;
    }

    /**
     * Checks for T range value.
     * 
     * @return Time range values have been set ?.
     */
    protected boolean hasTRangeValue() {
        return tRangeValue[0] != Double.MAX_VALUE;
    }

    /**
     * Checks for Z range value.
     * 
     * @return Z range values have been set ?.
     */
    protected boolean hasZRangeValue() {
        return zRangeValue[0] != Double.MAX_VALUE;

    }

    /**
     * Checks for Y range value.
     * 
     * @return Y range values have been set ?.
     */
    protected boolean hasYRangeValue() {
        return yRangeValue[0] != Double.MAX_VALUE;

    }

    /**
     * Checks for X range value.
     * 
     * @return X range values have been set ?.
     */
    protected boolean hasXRangeValue() {
        return xRangeValue[0] != Double.MAX_VALUE;

    }

    /**
     * Get min/max the longitude from the grid.
     *
     * @return Normalized Min/Max of the Longitude ranges values
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    public MAMath.MinMax getMinMaxLonNormal(List<List<Range>> listYXRanges) throws MotuException, MotuNotImplementedException {
        // Assumes size of lists are consistency.
        if (listYXRanges.size() == 2) {
            List<Range> yxRanges1 = listYXRanges.get(0);
            List<Range> yxRanges2 = listYXRanges.get(1);
            return RequestProductParameters.getMinMaxLonNormal(yxRanges1.get(1), yxRanges2.get(1), rangesLonValue.get(0), rangesLonValue.get(1));
        } else if (rangesLonValue.size() == 1) {
            double[] rangeLonVal = rangesLonValue.get(0);
            return new MAMath.MinMax(rangeLonVal[0], rangeLonVal[1]);
        } else if (rangesLonValue.isEmpty()) {
            // no range
            return new MAMath.MinMax(Double.MAX_VALUE, Double.MAX_VALUE);
        } else {
            throw new MotuNotImplementedException(
                    String.format("Longitude ranges list more than 2 elements is not implemented (%s)", this.getClass().getName()));

        }
    }

    /**
     * Get min/max the latitude from the grid.
     * 
     * @return Normalized Min/Max of the latitude ranges values
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    public MAMath.MinMax getMinMaxLatNormal() throws MotuNotImplementedException {
        // Assumes size of lists are consistency.
        if ((rangesLatValue.size() == 1) || (rangesLatValue.size() == 2)) {
            // Assumes that all latitude's Ranges are the same.
            double[] rangeLatVal = rangesLatValue.get(0);
            return new MAMath.MinMax(NetCdfReader.getLatNormal(rangeLatVal[0]), NetCdfReader.getLatNormal(rangeLatVal[1]));
        } else if (rangesLatValue.isEmpty()) {
            // no range
            return new MAMath.MinMax(Double.MAX_VALUE, Double.MAX_VALUE);
        } else {
            double[] minmax = ListUtils.findMinMax(rangesLatValue);
            double min = minmax[0];
            double max = minmax[1];
            return new MAMath.MinMax(NetCdfReader.getLatNormal(min), NetCdfReader.getLatNormal(max));
        }
    }

    protected void addGeoXYNeededVariables() {
        addVariableFromParameterMetaData(getRequestProduct().getProduct().findLongitudeIgnoreCase());
        addVariableFromParameterMetaData(getRequestProduct().getProduct().findLatitudeIgnoreCase());
    }

    private void addVariableFromParameterMetaData(ParameterMetaData parameterMetaData) {
        VarData varData = VarData.createFrom(parameterMetaData);
        if (varData != null) {
            getRequestProduct().getRequestProductParameters().getVariables().put(varData.getVarName(), varData);
        }
    }

    /**
     * Gets the original range list geo axis.
     * 
     * @param axis the axis
     * @param yRange the y_range
     * @param xRange the x_range
     * @return the original range list geo axis
     */
    public Section getOriginalRangeListGeoAxis(CoordinateAxis axis, Range yRange, Range xRange) {

        // get the ranges list
        int rank = axis.getRank();
        Range[] ranges = new Range[rank];
        int indexX = getGeoXDimVarIndex(axis);
        if (indexX >= 0) {
            ranges[indexX] = xRange;
        }
        int indexY = getGeoYDimVarIndex(axis);
        if (indexY >= 0) {
            ranges[indexY] = yRange;
        }
        List<Range> rangesList = Arrays.asList(ranges);

        return new Section(rangesList);
    }

    public static Section getOriginalRangeList(GeoGrid geoGrid, Range tRange, Range zRange, Range yRange, Range xRange) {
        int xDimOrgIndex;
        int yDimOrgIndex;
        int zDimOrgIndex = -1;
        int tDimOrgIndex = -1;

        GridCoordSystem gcs = geoGrid.getCoordinateSystem();
        CoordinateAxis xaxis = gcs.getXHorizAxis();
        if (xaxis instanceof CoordinateAxis1D) {
            xDimOrgIndex = findDimension(geoGrid, gcs.getXHorizAxis().getDimension(0));
            yDimOrgIndex = findDimension(geoGrid, gcs.getYHorizAxis().getDimension(0));

        } else { // 2D case
            yDimOrgIndex = findDimension(geoGrid, gcs.getXHorizAxis().getDimension(0));
            xDimOrgIndex = findDimension(geoGrid, gcs.getXHorizAxis().getDimension(1));
        }

        if (gcs.getVerticalAxis() != null)
            zDimOrgIndex = findDimension(geoGrid, gcs.getVerticalAxis().getDimension(0));
        if (gcs.getTimeAxis() != null) {
            if (gcs.getTimeAxis1D() != null)
                tDimOrgIndex = findDimension(geoGrid, gcs.getTimeAxis1D().getDimension(0));
            else
                tDimOrgIndex = findDimension(geoGrid, gcs.getTimeAxis().getDimension(1));
        }
        // get the ranges list
        int rank = geoGrid.getRank();
        Range[] ranges = new Range[rank];
        if (null != geoGrid.getXDimension())
            ranges[xDimOrgIndex] = xRange;
        if (null != geoGrid.getYDimension())
            ranges[yDimOrgIndex] = yRange;
        if (null != geoGrid.getZDimension())
            ranges[zDimOrgIndex] = zRange;
        if (null != geoGrid.getTimeDimension())
            ranges[tDimOrgIndex] = tRange;
        List<Range> rangesList = Arrays.asList(ranges);

        return new Section(rangesList);
    }

    /**
     * Find dimension.
     * 
     * @param geoGrid the geo grid
     * @param want the want
     * @return the int
     */
    public static int findDimension(GeoGrid geoGrid, Dimension want) {
        List<Dimension> dims = geoGrid.getVariable().getDimensions();
        for (int i = 0; i < dims.size(); i++) {
            Dimension d = dims.get(i);
            if (d.equals(want))
                return i;
        }
        return -1;
    }

    /**
     * Gets the geo x dim var index.
     * 
     * @param var the var
     * @return the geo x dim var index
     */
    protected int getGeoXDimVarIndex(Variable var) {
        return getGeoDimVarIndex(var, getRequestProduct().getProduct().getGeoXAxis());
    }

    /**
     * Gets the geo y dim var index.
     * 
     * @param var the var
     * @return the geo y dim var index
     */
    protected int getGeoYDimVarIndex(Variable var) {
        return getGeoDimVarIndex(var, getRequestProduct().getProduct().getGeoYAxis());
    }

    /**
     * Gets the geo dim var index.
     * 
     * @param var the var
     * @param axis the axis
     * @return the geo dim var index
     */
    protected int getGeoDimVarIndex(Variable var, CoordinateAxis axis) {
        int index = -1;
        if (axis == null) {
            return index;
        }
        for (int i = 0; i < var.getDimensions().size(); i++) {
            if (var.getDimension(i).getFullName().equalsIgnoreCase(axis.getFullName())) {
                index = i;
                break;
            }
        }

        return index;
    }

    /**
     * Adds the range.
     * 
     * @param range the range
     * @param mapRange the map range
     * @return true, if adds the range
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    protected static boolean addRange(Range range, Map<String, Range> mapRange) throws MotuException {
        String key = range.toString();
        Range rangeRef = mapRange.get(key);
        boolean isRangeAlreadyAdded = (rangeRef != null);
        // Range does not exist
        if (!isRangeAlreadyAdded) {
            try {
                Iterator<Entry<String, Range>> it = mapRange.entrySet().iterator();
                while (it.hasNext() && !isRangeAlreadyAdded) {
                    Entry<String, Range> entry = it.next();
                    key = entry.getKey();
                    rangeRef = entry.getValue();

                    if (rangeRef.intersects(range)) {
                        // There is an intersection:
                        addRangeToMapIfExtensionIsRequiered(range, mapRange, key, rangeRef);
                        isRangeAlreadyAdded = true;
                    }
                }
            } catch (InvalidRangeException e) {
                throw new MotuException(ErrorType.BAD_PARAMETERS, "ERROR in DatasetGrid#addRange.", e);
            }
            if (!isRangeAlreadyAdded) {
                mapRange.put(range.toString(), range);
                isRangeAlreadyAdded = true;
            }

        }
        return isRangeAlreadyAdded;
    }

    private static void addRangeToMapIfExtensionIsRequiered(Range range, Map<String, Range> mapRange, String keyInMap, Range rangeRef)
            throws InvalidRangeException {
        if (!((range.first() >= rangeRef.first()) && (range.last() <= rangeRef.last()))) {
            // range exceeds one one side so we create a new Range with union
            mapRange.remove(keyInMap);
            Range newRange = rangeRef.union(range);
            mapRange.put(newRange.toString(), newRange);
        } // else means the range is fully included in existing one
    }

    /**
     * Subset.
     * 
     * @param axis the axis
     * @param range the range
     * @return the variable
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    public static CoordinateAxis subset(CoordinateAxis axis, Range range) throws MotuException, MotuNotImplementedException {
        if (axis == null) {
            throw new MotuException(ErrorType.INVALID_LAT_LON_RANGE, "ERROR - in DatasetGrid#subset axis parameter is null");
        }
        if (!(axis instanceof CoordinateAxis1D)) {
            throw new MotuNotImplementedException(
                    String.format("ERROR in DatasetGrid#subset : Process a coordinate axis with more than one dimensions is not yet implemented (axis name:'%s')",
                                  axis.getFullName()));
        }
        // get the ranges list
        int rank = axis.getRank();
        if (rank != 1) {
            throw new MotuNotImplementedException(
                    String.format("ERROR - The subsetting of the coordinate axis '%s' with '%d' dimensions is not yet implemented",
                                  axis.getFullName(),
                                  rank));
        }

        Range[] ranges = new Range[rank];
        ranges[0] = range;
        List<Range> rangesList = Arrays.asList(ranges);

        // subset the variable
        VariableDS vSection = null;
        try {
            vSection = (VariableDS) axis.section(rangesList);
        } catch (InvalidRangeException e) {
            throw new MotuException(
                    ErrorType.BAD_PARAMETERS,
                    String.format("ERROR - in DatasetGrid#subset with axis name '%s'", axis.getFullName()),
                    e);
        }
        List<Dimension> dims = vSection.getDimensions();
        for (Dimension dim : dims) {
            dim.setShared(true); // make them shared (section will make them unshared)
        }

        if (!(vSection instanceof CoordinateAxis)) {
            throw new MotuException(
                    ErrorType.INVALID_LAT_LON_RANGE,
                    String.format("ERROR - in DatasetGrid#subset: unexpected result after subsetting axis name '%s': new variable is not a 'CoordinateAxis' instance but a '%s'",
                                  axis.getFullName(),
                                  axis.getClass().getName()));

        }
        return (CoordinateAxis) vSection;
    }

    /**
     * adds Time min/max attribute and date Units
     * 
     * @param globalAttributes
     */
    private void addGlobalAttributesForTime(List<Attribute> globalAttributes) {
        if (hasOutputTimeDimension) {
            Attribute attribute = null;
            double min;
            double max;
            if (hasTRangeValue()) {
                min = tRangeValue[0];
                max = tRangeValue[1];
            } else {
                min = getRequestProduct().getProduct().getProductMetaData().getTimeAxisMinValueAsDouble();
                max = getRequestProduct().getProduct().getProductMetaData().getTimeAxisMaxValueAsDouble();
            }
            attribute = new Attribute(NetCdfReader.GLOBALATTRIBUTE_TIME_MIN, min);
            globalAttributes.add(attribute);
            attribute = new Attribute(NetCdfReader.GLOBALATTRIBUTE_TIME_MAX, max);
            globalAttributes.add(attribute);
            attribute = new Attribute(
                    NetCdfReader.GLOBALATTRIBUTE_JULIAN_DAY_UNIT,
                    getRequestProduct().getProduct().getProductMetaData().getTimeAxis().getUnitsString());
            globalAttributes.add(attribute);
        }
    }

    /**
     * adds Z min/max attribute
     * 
     * @param globalAttributes
     */
    private void addGlobalAttributesForDepth(List<Attribute> globalAttributes) {
        if (hasOutputZDimension) {
            double min;
            double max;
            if (hasZRangeValue()) {
                min = zRangeValue[0];
                max = zRangeValue[1];
            } else {
                max = getRequestProduct().getProduct().getProductMetaData().getZAxisMaxValue();
                min = getRequestProduct().getProduct().getProductMetaData().getZAxisMinValue();
            }

            globalAttributes.add(new Attribute(NetCdfReader.GLOBALATTRIBUTE_Z_MIN, min));
            globalAttributes.add(new Attribute(NetCdfReader.GLOBALATTRIBUTE_Z_MAX, max));
        }
    }

    /**
     * adds Lat min/max attribute
     * 
     * @param globalAttributes
     */
    private void addGlobalAttributesForLat(List<Attribute> globalAttributes) {
        if (hasOutputLatDimension) {
            Attribute attribute = null;
            double min;
            double max;
            if (hasYRangeValue()) {
                // min = NetCdfReader.getLatNormal(yRangeValue[0]);
                // max = NetCdfReader.getLatNormal(yRangeValue[1]);
                min = yRangeValue[0];
                max = yRangeValue[1];
            } else {
                min = getRequestProduct().getProduct().getProductMetaData().getLatNormalAxisMinValue();
                max = getRequestProduct().getProduct().getProductMetaData().getLatNormalAxisMaxValue();
            }

            attribute = new Attribute(NetCdfReader.GLOBALATTRIBUTE_LATITUDE_MIN, min);
            globalAttributes.add(attribute);
            attribute = new Attribute(NetCdfReader.GLOBALATTRIBUTE_LATITUDE_MAX, max);
            globalAttributes.add(attribute);
        }
    }

    /**
     * adds Lon min/max attribute
     * 
     * @param globalAttributes
     */
    private void addGlobalAttributesForLon(List<Attribute> globalAttributes) {
        if (hasOutputLonDimension) {
            Attribute attribute = null;
            double min;
            double max;
            if (hasXRangeValue()) {
                min = xRangeValue[0];
                max = xRangeValue[1];
            } else {
                min = getRequestProduct().getProduct().getProductMetaData().getLonNormalAxisMinValue();
                max = getRequestProduct().getProduct().getProductMetaData().getLonNormalAxisMaxValue();
            }
            if (min > max) {
                double longitudeCenter = min + 180.0;
                max = LatLonPointImpl.lonNormal(max, longitudeCenter);
            }

            attribute = new Attribute(NetCdfReader.GLOBALATTRIBUTE_LONGITUDE_MIN, min);
            globalAttributes.add(attribute);
            attribute = new Attribute(NetCdfReader.GLOBALATTRIBUTE_LONGITUDE_MAX, max);
            globalAttributes.add(attribute);
        }
    }

    /**
     * initializes a list of global attributes ('dynamic' attributes that depend on data). Range values can be
     * Double.MAX_VALUE if no value
     *
     * @return a list of global attributes
     */
    public List<Attribute> initializeNetCdfDynGlobalAttributes() {
        List<Attribute> globalAttributes = new ArrayList<>();
        addGlobalAttributesForTime(globalAttributes);
        addGlobalAttributesForDepth(globalAttributes);
        addGlobalAttributesForLat(globalAttributes);
        addGlobalAttributesForLon(globalAttributes);
        return globalAttributes;
    }

    private void checkFromOriginalGlobalAttributeName(Attribute attrToAdd) {
        String[] listToNotApplyPrefix = new String[] {
                "title", "Conventions", "netcdf_version_id", "product_version", "software_version", "lat", "lon", "field_type" };
        String prefixName = "FROM_ORIGINAL_FILE__";
        for (String s : listToNotApplyPrefix) {
            if (attrToAdd.getFullName().contains(s)) {
                attrToAdd.setName(prefixName + attrToAdd.getFullName());
            }
        }
    }

    private List<Attribute> getAllGlobalAttributesList(List<Attribute> globalAttributesRead) {
        ProductMetaData pmd = getRequestProduct().getProduct().getProductMetaData();
        DataProvider dp = pmd.getDataProvider();

        // This map lists all default attributes that have to be added to Global attributes
        Map<String, String> allDefaultGlobalAttrMap = new HashMap<>();
        allDefaultGlobalAttrMap.put(NetCdfReader.GLOBALATTRIBUTE_TITLE, pmd.getTitle());
        allDefaultGlobalAttrMap.put(NetCdfReader.GLOBALATTRIBUTE_INSTITUTION, dp != null ? dp.getName() : " ");
        allDefaultGlobalAttrMap.put(NetCdfReader.GLOBALATTRIBUTE_REFERENCES, dp != null ? dp.getWebSite() : " ");
        allDefaultGlobalAttrMap.put(NetCdfReader.GLOBALATTRIBUTE_SOURCE, " ");
        allDefaultGlobalAttrMap.put(NetCdfReader.GLOBALATTRIBUTE_CONVENTIONS, NetCdfReader.GLOBALATTRIBUTE_CONVENTIONS_VALUE);
        allDefaultGlobalAttrMap.put(NetCdfReader.GLOBALATTRIBUTE_COMMENT, pmd.getDescription());
        allDefaultGlobalAttrMap.put(NetCdfReader.GLOBALATTRIBUTE_HISTORY,
                                    "Data extracted from dataset " + getRequestProduct().getProduct().getLocationData());

        // This map is to know which default attributes have been treated
        List<String> defaultAddedAttributeDone = new ArrayList<>(allDefaultGlobalAttrMap.size());

        List<Attribute> globalAttributes = new ArrayList<>();
        for (Attribute readAttr : globalAttributesRead) {
            String attrFyllName = readAttr.getFullName();
            Attribute attrToAdd = readAttr;
            if (allDefaultGlobalAttrMap.keySet().contains(attrFyllName)) {
                String defaultValue = allDefaultGlobalAttrMap.get(attrFyllName);
                attrToAdd = checkGlobalAttribute(readAttr, attrFyllName, defaultValue);
                defaultAddedAttributeDone.add(attrFyllName);
            } else {
                checkFromOriginalGlobalAttributeName(attrToAdd);
            }
            globalAttributes.add(attrToAdd);
        }

        for (Entry<String, String> attrKV : allDefaultGlobalAttrMap.entrySet()) {
            if (!defaultAddedAttributeDone.contains(attrKV.getKey())) {
                globalAttributes.add(new Attribute(attrKV.getKey(), attrKV.getValue()));
            }
        }

        return globalAttributes;
    }

    private Attribute checkGlobalAttribute(Attribute readAttr, String attrFullEspacedName, String attrDefaultValue) {
        Attribute attribute = null;
        if (readAttr.getFullNameEscaped() != null) {
            attribute = getRequestProduct().getProduct().getNetCdfReader().getAttribute(attrFullEspacedName);
            if (attribute == null) {
                attribute = new Attribute(attrFullEspacedName, attrDefaultValue);
            }
        }
        return attribute;
    }

    /**
     * initializes a list of global attributes ('fixed' attributes that don't depend on data).
     * 
     * @return a list of global attributes
     */
    public List<Attribute> initializeNetCdfFixedGlobalAttributes() {
        return getAllGlobalAttributesList(getRequestProduct().getProduct().getNetCdfReader().getAttributes());
    }

}
