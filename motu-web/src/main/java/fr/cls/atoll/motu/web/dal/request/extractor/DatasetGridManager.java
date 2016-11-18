package fr.cls.atoll.motu.web.dal.request.extractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaDatetime;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaDepth;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaLatLon;
import fr.cls.atoll.motu.web.bll.request.model.RequestProduct;
import fr.cls.atoll.motu.web.common.utils.ListUtils;
import fr.cls.atoll.motu.web.dal.request.netcdf.NetCdfReader;
import fr.cls.atoll.motu.web.dal.request.netcdf.NetCdfWriter;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.DatasetBase;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.DatasetBase.RangeComparator;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.VarData;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ParameterMetaData;
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
    static final String[] VAR_ATTR_TO_REMOVE = new String[] { "Date_CNES_JD", "date", "_unsigned", };

    /** Time dimension range. */
    protected Range tRange = null;

    /** Y/X or Lat/Lon dimension range. (Y first, X second) */
    protected Range[] yxRange = null;

    /** Z dimension range. */
    protected Range zRange = null;

    /** Time dimension range values. */
    protected double[] tRangeValue = new double[] { Double.MAX_VALUE, Double.MAX_VALUE, };

    /** Y or Lat dimension range values. */
    protected double[] yRangeValue = new double[] { Double.MAX_VALUE, Double.MAX_VALUE, };

    /** X or Lon dimension range values. */
    protected double[] xRangeValue = new double[] { Double.MAX_VALUE, Double.MAX_VALUE, };

    /** List of each adjacent Lat dimension range values. */
    List<double[]> rangesLatValue = new ArrayList<double[]>();

    /** List of each adjacent Lon dimension range values. */
    List<double[]> rangesLonValue = new ArrayList<double[]>();

    /** List of each adjacent Lat dimension range. */
    List<List<Range>> listYXRanges = null;

    /** Z dimension range values. */
    protected double[] zRangeValue = new double[] { Double.MAX_VALUE, Double.MAX_VALUE, };

    /**
     * Constructeur.
     * 
     * @param requestProduct
     */
    public DatasetGridManager(RequestProduct requestProduct) {
        super(requestProduct);
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
            // break;
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
     */
    @Override
    public double computeAmountDataSize() throws MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException,
            MotuNotImplementedException, MotuInvalidDepthRangeException, MotuInvalidLatLonRangeException, NetCdfVariableException, MotuNoVarException,
            NetCdfVariableNotFoundException {
        initGetAmountData();

        getTimeRange();
        // gets ranges to be extracted
        // Number of ranges for Longitude can be 1 or 2.
        getAdjacentYXRange();
        // getYXRange();
        getZRange();

        GridDataset gds = new GridDataset(getRequestProduct().getProduct().getNetCdfReaderDataset());

        NetCdfWriter netCdfWriter = new NetCdfWriter();

        netCdfWriter.resetAmountDataSize();

        for (VarData varData : getRequestProduct().getDataSetBase().getVariables().values()) {

            GeoGrid geoGrid = gds.findGridByName(varData.getVarName());
            if (geoGrid == null) {
                continue;
            }
            List<GeoGrid> listGeoGridSubset = new ArrayList<GeoGrid>();

            for (List<Range> yxRanges : listYXRanges) {
                // GridDatatype geoGridSubset = null;
                Range yRange = yxRanges.get(0);
                Range xRange = yxRanges.get(1);

                GeoGrid geoGridSubset = null;
                try {
                    // -----------------------------------------------------------------------
                    // WARNING :
                    //
                    // section method of Variable create a new instance of the class VariableDS from the
                    // original
                    // variable,
                    // but some informations are lost (as Fillvalue).
                    // And Subset of GeoGrid is used section method.
                    //
                    // Example :
                    // ...
                    // VariableDS v_section = (VariableDS) v.section(rangesList);
                    //
                    // v is an instance of class VariableDS and the attribute fillValue of attribute smProxy
                    // is
                    // set and hasFillValue is set to true.
                    // After calling v.section, the attribute fillValue of attribute smProxy of v_section is
                    // not
                    // set and hasFillValue is set to false.
                    //
                    // So, when you work with v_section variable and you called hasFillValue method, it
                    // returns
                    // false, while with the original variable v, hasFillValue method returns true.
                    // -----------------------------------------------------------------------
                    geoGridSubset = geoGrid.subset(tRange, zRange, yRange, xRange);
                    listGeoGridSubset.add(geoGridSubset);
                    // geoGridSubset = geoGrid.makeSubset(null, null, tRange, zRange, yxRange[0], yxRange[1]);
                } catch (InvalidRangeException e) {

                    throw new MotuException(ErrorType.BAD_PARAMETERS, "Error in subsetting geo grid", e);
                }
            }
            // pass geoGridsubset and geoGrid (the original geoGrid) to be able to get somme information
            // (lost
            // in subsetting - See bug above) about the variable of the GeoGrid
            netCdfWriter.computeAmountDataSize(listGeoGridSubset);
        }

        amountDataSize = netCdfWriter.getAmountDataSize();
        return amountDataSize;
    }

    /**
     * Inits the get amount data.
     *
     * @throws MotuException the motu exception
     * @throws MotuNoVarException the motu no var exception
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     */
    public void initGetAmountData() throws MotuException, MotuNoVarException, NetCdfVariableNotFoundException {
        if (getRequestProduct().getDataSetBase().getVariables().isEmpty()) {
            throw new MotuNoVarException("Variable list is empty");
        }

        getRequestProduct().getDataSetBase().getDataBaseExtractionTimeCounter().addReadingTime(getRequestProduct().getProduct().openNetCdfReader());

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
    protected void prepareLatLonWriting(GeoGrid geoGridSubset, Range yRange, Range xRange) throws MotuException, MotuNotImplementedException {
        CoordinateAxis xaxis = geoGridSubset.getCoordinateSystem().getXHorizAxis();
        CoordinateAxis yaxis = geoGridSubset.getCoordinateSystem().getYHorizAxis();

        // Bug the Netcdf-java Library in subset method of GeoGrid, the axis Type is not always set.
        if (xaxis.getAxisType() == null) {
            xaxis.setAxisType(getRequestProduct().getProduct().getCoordinateAxisType(xaxis.getName()));
        }
        if (yaxis.getAxisType() == null) {
            yaxis.setAxisType(getRequestProduct().getProduct().getCoordinateAxisType(yaxis.getName()));
        }

        if ((xaxis.getAxisType() == AxisType.GeoX) || (yaxis.getAxisType() == AxisType.GeoY)) {
            return;
        }

        if (xaxis.getAxisType() == null) {
            throw new MotuException(
                    ErrorType.INVALID_LONGITUDE,
                    String.format("ERROR in DatasetGrid#prepareLatLonWriting - axis type for '%s' axis is null", xaxis.getName()));
        }
        if (yaxis.getAxisType() == null) {
            throw new MotuException(
                    ErrorType.INVALID_LATITUDE,
                    String.format("ERROR in DatasetGrid#prepareLatLonWriting - axis type for '%s' axis is null", yaxis.getName()));
        }

        String xName = xaxis.getName();
        List<Section> listVarOrgRanges = mapVarOrgRanges.get(xName);
        if (ListUtils.isNullOrEmpty(listVarOrgRanges)) {
            listVarOrgRanges = new ArrayList<Section>();
            mapVarOrgRanges.put(xName, listVarOrgRanges);
        }

        Section section = getOriginalRangeListGeoAxis(xaxis, yRange, xRange);
        listVarOrgRanges.add(section);

        String yName = yaxis.getName();
        listVarOrgRanges = mapVarOrgRanges.get(yName);
        if (ListUtils.isNullOrEmpty(listVarOrgRanges)) {
            listVarOrgRanges = new ArrayList<Section>();
            mapVarOrgRanges.put(yName, listVarOrgRanges);
        }

        section = getOriginalRangeListGeoAxis(yaxis, yRange, xRange);
        listVarOrgRanges.add(section);

    }

    /**
     * Prepare xy writing.
     * 
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    protected void prepareXYWriting() throws MotuException, MotuNotImplementedException {

        listVariableXSubset = new ArrayList<CoordinateAxis>();
        listVariableYSubset = new ArrayList<CoordinateAxis>();
        mapXRange = new HashMap<String, Range>();
        mapYRange = new HashMap<String, Range>();
        mapVarOrgRanges = new HashMap<String, List<Section>>();

        for (List<Range> yxRanges : listYXRanges) {

            Range yRange = yxRanges.get(0);
            Range xRange = yxRanges.get(1);

            addRange(yRange, mapYRange);

            addRange(xRange, mapXRange);

        }

        RangeComparator rangeComparator = new RangeComparator();

        listDistinctXRange = new ArrayList<Range>(mapXRange.values());
        Collections.sort(listDistinctXRange, rangeComparator);

        listDistinctYRange = new ArrayList<Range>(mapYRange.values());
        Collections.sort(listDistinctYRange, rangeComparator);

        for (Range range : listDistinctYRange) {
            CoordinateAxis axis = subset(getRequestProduct().getProduct().getGeoYAxis(), range);
            listVariableYSubset.add(axis);
            String axisName = axis.getName();
            List<Section> listVarOrgRanges = mapVarOrgRanges.get(axisName);
            if (ListUtils.isNullOrEmpty(listVarOrgRanges)) {
                listVarOrgRanges = new ArrayList<Section>();
                mapVarOrgRanges.put(axisName, listVarOrgRanges);
            }
            List<Range> lr = new ArrayList<Range>();
            lr.add(range);
            Section section = new Section(lr);
            listVarOrgRanges.add(section);

        }

        for (Range range : listDistinctXRange) {
            CoordinateAxis axis = subset(getRequestProduct().getProduct().getGeoXAxis(), range);
            listVariableXSubset.add(axis);
            String axisName = axis.getName();
            List<Section> listVarOrgRanges = mapVarOrgRanges.get(axisName);
            if (ListUtils.isNullOrEmpty(listVarOrgRanges)) {
                listVarOrgRanges = new ArrayList<Section>();
                mapVarOrgRanges.put(axisName, listVarOrgRanges);
            }
            List<Range> lr = new ArrayList<Range>();
            lr.add(range);
            Section section = new Section(lr);
            listVarOrgRanges.add(section);
        }
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

        for (VarData varData : getRequestProduct().getDataSetBase().getVariables().values()) {
            Variable variable = getRequestProduct().getProduct().getNetCdfReader().getRootVariable(varData.getVarName());
            List<Dimension> dimsVar = variable.getDimensions();
            for (Dimension dim : dimsVar) {
                // ATESTER : changement de signature dans getCoordinateVariables entre netcdf-java 2.2.20 et
                // 2.2.22
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
            MotuNotImplementedException, MotuInvalidDepthRangeException, MotuInvalidLatLonRangeException, NetCdfVariableException, MotuNoVarException,
            NetCdfVariableNotFoundException, IOException {
        initNetCdfExtraction();

        getTimeRange();
        // gets ranges to be extracted
        // Number of ranges for Longitude can be 1 or 2.
        getAdjacentYXRange();
        // getYXRange();
        getZRange();

        // Is this dataset a Geo X/Y with Lat/Lon whose dimensions depend on X/Y ?
        boolean isGeoXY = getRequestProduct().getProduct().hasGeoXYAxisWithLonLatEquivalence();

        String locationData = getRequestProduct().getProduct().getNetCdfReaderDataset().getLocation();
        NetCdfReader netCdfReader = new NetCdfReader(locationData, getRequestProduct().getProduct().isCasAuthentication());
        getRequestProduct().getDataSetBase().getDataBaseExtractionTimeCounter().addReadingTime(netCdfReader.open(false));

        // GridDataset gds = new GridDataset(getRequestProduct().getProduct().getNetCdfReaderDataset());
        GridDataset gds = new GridDataset(netCdfReader.getNetcdfDataset());

        List<Attribute> globalFixedAttributes = initializeNetCdfFixedGlobalAttributes();
        List<Attribute> globalDynAttributes = initializeNetCdfDynGlobalAttributes();

        // NetCdfWriter netCdfWriter = new
        // NetCdfWriter(getRequestProduct().getProduct().getExtractLocationData(), true);
        NetCdfWriter netCdfWriter = new NetCdfWriter(getRequestProduct().getDataSetBase().getExtractLocationDataTemp(), true);

        netCdfWriter.writeGlobalAttributes(globalFixedAttributes);
        netCdfWriter.writeGlobalAttributes(globalDynAttributes);

        netCdfWriter.resetAmountDataSize();

        // -------------------------------------------------
        // If GeoXY then compute the X/Y dim and subset the X/Y variables
        // Write variables X/Y definitions.
        // Add Lat/Lon variables and variable concerning projection to list of variables to extract
        // WARNING : this must be done first to compute and subset the right output (dimension and var. X/Y
        // range).
        // -------------------------------------------------
        if (isGeoXY) {

            // If GeoXY then compute the X/Y dim and subset the X/Y variables
            prepareXYWriting();

            // Write variables X/Y definitions.
            netCdfWriter.writeVariables(listVariableXSubset, mapXRange, getRequestProduct().getProduct().getNetCdfReader().getOrignalVariables());
            netCdfWriter.writeVariables(listVariableYSubset, mapYRange, getRequestProduct().getProduct().getNetCdfReader().getOrignalVariables());

            addGeoXYNeededVariables();

        }

        for (VarData varData : getRequestProduct().getDataSetBase().getVariables().values()) {

            GeoGrid geoGrid = gds.findGridByName(varData.getVarName());
            if (geoGrid == null) {
                continue;
            }
            List<GeoGrid> listGeoGridSubset = new ArrayList<GeoGrid>();

            for (List<Range> yxRanges : listYXRanges) {
                // GridDatatype geoGridSubset = null;
                Range yRange = yxRanges.get(0);
                Range xRange = yxRanges.get(1);

                GeoGrid geoGridSubset = null;
                try {
                    // -----------------------------------------------------------------------
                    // WARNING :
                    //
                    // section method of Variable create a new instance of the class VariableDS from the
                    // original
                    // variable,
                    // but some informations are lost (as Fillvalue).
                    // And Subset of GeoGrid is used section method.
                    //
                    // Example :
                    // ...
                    // VariableDS v_section = (VariableDS) v.section(rangesList);
                    //
                    // v is an instance of class VariableDS and the attribute fillValue of attribute smProxy
                    // is
                    // set and hasFillValue is set to true.
                    // After calling v.section, the attribute fillValue of attribute smProxy of v_section is
                    // not
                    // set and hasFillValue is set to false.
                    //
                    // So, when you work with v_section variable and you called hasFillValue method, it
                    // returns
                    // false, while with the original variable v, hasFillValue method returns true.
                    // -----------------------------------------------------------------------
                    geoGridSubset = geoGrid.subset(tRange, zRange, yRange, xRange);
                    listGeoGridSubset.add(geoGridSubset);

                    // if GeoXY then compute the original range for the subset variable.
                    // This is already done in the geo-grid but the information (original range is private and
                    // there is no getter on it)
                    if (isGeoXY) {
                        String varName = geoGridSubset.getVariable().getName();
                        List<Section> listVarOrgRanges = mapVarOrgRanges.get(varName);
                        if (ListUtils.isNullOrEmpty(listVarOrgRanges)) {
                            listVarOrgRanges = new ArrayList<Section>();
                            mapVarOrgRanges.put(varName, listVarOrgRanges);
                        }

                        Section section = getOriginalRangeList(geoGridSubset, tRange, zRange, yRange, xRange);
                        listVarOrgRanges.add(section);

                        // if coordinate axes of the geogrid are Lat/Lon
                        // then compute the original range for the subset Lat/lon.
                        prepareLatLonWriting(geoGridSubset, yRange, xRange);

                    }

                } catch (InvalidRangeException e) {
                    throw new MotuException(ErrorType.BAD_PARAMETERS, "Error in subsetting geo grid", e);
                }
            }
            if (isGeoXY) {
                // pass geoGridsubset and geoGrid (the original geoGrid) to be able to get some information
                // (lost
                // in subsetting - See bug below) about the variable of the GeoGrid
                netCdfWriter.writeVariablesWithGeoXY(listGeoGridSubset,
                                                     geoGrid,
                                                     gds,
                                                     getRequestProduct().getProduct().getNetCdfReader().getOrignalVariables());

            } else {
                // pass geoGridsubset and geoGrid (the original geoGrid) to be able to get some information
                // (lost
                // in subsetting - See bug below) about the variable of the GeoGrid
                netCdfWriter.writeVariables(listGeoGridSubset,
                                            geoGrid,
                                            gds,
                                            getRequestProduct().getProduct().getNetCdfReader().getOrignalVariables());
            }

        }
        if (isGeoXY) {
            netCdfWriter.finishGeoXY(VAR_ATTR_TO_REMOVE, listDistinctXRange, listDistinctYRange, mapVarOrgRanges);
        } else {
            netCdfWriter.finish(VAR_ATTR_TO_REMOVE);
        }

        getRequestProduct().getDataSetBase().getDataBaseExtractionTimeCounter().addReadingTime(netCdfWriter.getReadingTime());
        getRequestProduct().getDataSetBase().getDataBaseExtractionTimeCounter().addWritingTime(netCdfWriter.getWritingTime());

        moveTempExtractFileToFinalExtractFile();
    }

    // ======================================

    /**
     * NetCdf extraction initialization.
     *
     * @throws MotuException the motu exception
     * @throws MotuNoVarException the motu no var exception
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     */
    public void initNetCdfExtraction() throws MotuException, MotuNoVarException, NetCdfVariableNotFoundException {
        if (getRequestProduct().getProduct() != null) {
            if (getRequestProduct().getDataSetBase().getVariables().isEmpty()) {
                throw new MotuNoVarException("Variable list is empty");
            }

            getRequestProduct().getDataSetBase().getDataBaseExtractionTimeCounter()
                    .addReadingTime(getRequestProduct().getProduct().openNetCdfReader());

            // Create output NetCdf file
            getRequestProduct().getDataSetBase()
                    .setExtractFilename(NetCdfWriter.getUniqueNetCdfFileName(getRequestProduct().getProduct().getProductId()));

            setHasOutputDimension();
        }
    }

    /**
     * Gets time range and range values to be extracted.
     * 
     * @throws MotuInvalidDateRangeException
     * @throws MotuException
     * @throws NetCdfVariableException
     */
    public void getTimeRange() throws MotuException, MotuInvalidDateRangeException, NetCdfVariableException {
        if (tRangeValue != null) {
            assert tRangeValue.length == 2;
            tRangeValue[0] = Double.MAX_VALUE;
            tRangeValue[1] = Double.MAX_VALUE;
        }

        tRange = null;
        if (getRequestProduct().getProduct().getProductMetaData().hasTimeAxis()) {
            Array timeAxisData = getRequestProduct().getProduct().getTimeAxisData();
            CoordinateAxis timeAxis = getRequestProduct().getProduct().getProductMetaData().getTimeAxis();

            ExtractCriteriaDatetime extractCriteriaDatetime = getRequestProduct().getDataSetBase().findCriteriaDatetime();
            if (extractCriteriaDatetime != null) {
                tRange = extractCriteriaDatetime.toRange(timeAxisData, timeAxis.getUnitsString(), tRangeValue);
            }
        }
    }

    /**
     * Gets Y/X (Lat/Lon or GeoX/GeoY) range and ranges values to be extracted.
     * 
     * @throws MotuException
     * @throws MotuNotImplementedException
     * @throws MotuInvalidLatLonRangeException
     */
    public void getYXRange() throws MotuException, MotuInvalidLatLonRangeException, MotuNotImplementedException {
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

        yxRange = null;
        if (getRequestProduct().getProduct().getProductMetaData().hasLatLonAxis()) {
            yxRange = new Range[2];
            ExtractCriteriaLatLon extractCriteriaLatLon = getRequestProduct().getDataSetBase().findCriteriaLatLon();
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
    }

    /**
     * Gets Z range and ranges values to be extracted.
     * 
     * @throws MotuInvalidDepthRangeException
     * @throws MotuException
     * @throws NetCdfVariableException
     * 
     */
    public Range getZRange() throws MotuException, MotuInvalidDepthRangeException, NetCdfVariableException {
        if (zRangeValue != null) {
            zRangeValue[0] = Double.MAX_VALUE;
            zRangeValue[1] = Double.MAX_VALUE;
        }

        zRange = null;
        if (getRequestProduct().getProduct().getProductMetaData().hasZAxis()) {
            Array zAxisData = getRequestProduct().getProduct().getZAxisData();
            ExtractCriteriaDepth extractCriteriaDepth = getRequestProduct().getDataSetBase().findCriteriaDepth();
            if (extractCriteriaDepth != null) {
                zRange = extractCriteriaDepth.toRange(zAxisData, zRangeValue);
            }
        }

        return zRange;
    }

    /**
     * Gets a list of Index Ranges for the given lat, lon bounding box. For projection, only an approximation
     * based on lat/lon corners. Must have 2D/LatLon for x and y axis.
     * 
     * @throws MotuNotImplementedException
     * @throws MotuException
     */
    public void getAdjacentYXRange() throws MotuException, MotuInvalidLatLonRangeException, MotuNotImplementedException {
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

        yxRange = null;

        listYXRanges = null;

        if (getRequestProduct().getProduct().getProductMetaData().hasLatLonAxis()) {
            ExtractCriteriaLatLon extractCriteriaLatLon = getRequestProduct().getDataSetBase().findCriteriaLatLon();
            if (extractCriteriaLatLon == null) {
                extractCriteriaLatLon = new ExtractCriteriaLatLon();
                getRequestProduct().getDataSetBase().getListCriteria().add(extractCriteriaLatLon);
            }
            CoordinateSystem cs = new CoordinateSystem(
                    getRequestProduct().getProduct().getNetCdfReaderDataset(),
                    getRequestProduct().getProduct().getProductMetaData().getLatLonAxis(),
                    null);
            listYXRanges = extractCriteriaLatLon.toListRanges(cs, rangesLatValue, rangesLonValue);

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

            MAMath.MinMax minMaxLat = null;
            MAMath.MinMax minMaxLon = null;

            if (getRequestProduct().getProduct().getProductMetaData().hasLatLonAxis2D()) {
                minMaxLat = extractCriteriaLatLon.getMinMaxYValue2D();
                minMaxLon = extractCriteriaLatLon.getMinMaxXValue2D();
                if (minMaxLat == null) {
                    throw new MotuException(
                            ErrorType.INVALID_LATITUDE,
                            "Error in DatasetGrid#getAdjacentYXRange: Latitude/Longitude axes are 2D and min/max latitude values to extract are null");
                }
                if (minMaxLon == null) {
                    throw new MotuException(
                            ErrorType.INVALID_LATITUDE,
                            "Error in DatasetGrid#getAdjacentYXRange: Latitude/Longitude axes are 2D and min/max longitude values to extract are null");
                }
                yRangeValue[0] = minMaxLat.min;
                yRangeValue[1] = minMaxLat.max;

                xRangeValue[0] = minMaxLon.min;
                xRangeValue[1] = minMaxLon.max;

            } else {

                minMaxLat = getMinMaxLatNormal();
                yRangeValue[0] = minMaxLat.min;
                yRangeValue[1] = minMaxLat.max;

                minMaxLon = getMinMaxLonNormal();
                xRangeValue[0] = minMaxLon.min;
                xRangeValue[1] = minMaxLon.max;
            }
        } else if (getRequestProduct().getProduct().getProductMetaData().hasGeoXYAxis()) {
            throw new MotuNotImplementedException(
                    "Extraction with X/Y axes and without Lat/Lon data are not implemented (method DatasetGrid#getAdjacentYXRange");
        }
    }

    /** Has output time dimension. */
    protected boolean hasOutputTimeDimension = false;

    /** Has output latitude dimension. */
    protected boolean hasOutputLatDimension = false;

    /** Has output longitude dimension. */
    protected boolean hasOutputLonDimension = false;

    /** Has output Z dimension. */
    protected boolean hasOutputZDimension = false;

    /** The list variable x subset. */
    protected List<CoordinateAxis> listVariableXSubset = null;

    /** The list variable y subset. */
    protected List<CoordinateAxis> listVariableYSubset = null;

    /** The map x range. */
    protected Map<String, Range> mapXRange = null;

    /** The map y range. */
    protected Map<String, Range> mapYRange = null;

    /** The map var org ranges. */
    protected Map<String, List<Section>> mapVarOrgRanges = null;

    /** The list distinct x range. */
    protected List<Range> listDistinctXRange = null;

    /** The list distinct y range. */
    protected List<Range> listDistinctYRange = null;

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
    public MAMath.MinMax getMinMaxLonNormal() throws MotuException, MotuNotImplementedException {

        // Assumes size of lists are consistency.
        if (listYXRanges.size() == 2) {
            List<Range> yxRanges1 = listYXRanges.get(0);
            List<Range> yxRanges2 = listYXRanges.get(1);
            return DatasetBase.getMinMaxLonNormal(yxRanges1.get(1), yxRanges2.get(1), rangesLonValue.get(0), rangesLonValue.get(1));
        } else if (rangesLonValue.size() == 1) {
            double[] rangeLonVal = rangesLonValue.get(0);
            return new MAMath.MinMax(rangeLonVal[0], rangeLonVal[1]);
        } else if (rangesLonValue.size() == 0) {
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
        } else if (rangesLatValue.size() == 0) {
            // no range
            return new MAMath.MinMax(Double.MAX_VALUE, Double.MAX_VALUE);
        } else {
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            for (double[] latValues : rangesLatValue) {
                for (double value : latValues) {
                    if (value < min) {
                        min = value;
                    } else if (value > max) {
                        max = value;
                    }
                }

            }
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
            getRequestProduct().getDataSetBase().getVariables().put(varData.getVarName(), varData);
        }
    }

    /**
     * Gets the original range list geo axis.
     * 
     * @param axis the axis
     * @param y_range the y_range
     * @param x_range the x_range
     * @return the original range list geo axis
     */
    public Section getOriginalRangeListGeoAxis(CoordinateAxis axis, Range y_range, Range x_range) {

        // get the ranges list
        int rank = axis.getRank();
        Range[] ranges = new Range[rank];
        int indexX = getGeoXDimVarIndex(axis);
        if (indexX >= 0) {
            ranges[indexX] = x_range;
        }
        int indexY = getGeoYDimVarIndex(axis);
        if (indexY >= 0) {
            ranges[indexY] = y_range;
        }
        List<Range> rangesList = Arrays.asList(ranges);

        return new Section(rangesList);
    }

    public static Section getOriginalRangeList(GeoGrid geoGrid, Range t_range, Range z_range, Range y_range, Range x_range)
            throws InvalidRangeException {
        int xDimOrgIndex = -1, yDimOrgIndex = -1, zDimOrgIndex = -1, tDimOrgIndex = -1;

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
            ranges[xDimOrgIndex] = x_range;
        if (null != geoGrid.getYDimension())
            ranges[yDimOrgIndex] = y_range;
        if (null != geoGrid.getZDimension())
            ranges[zDimOrgIndex] = z_range;
        if (null != geoGrid.getTimeDimension())
            ranges[tDimOrgIndex] = t_range;
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
            if (var.getDimension(i).getName().equalsIgnoreCase(axis.getName())) {
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
    protected static boolean addRange(Range range, Map<String, Range> mapRange) throws MotuException, MotuNotImplementedException {
        String key = range.toString();
        Range rangeRef = mapRange.get(key);
        // Range already exists
        if (rangeRef != null) {
            return false;
        }

        Set<Entry<String, Range>> entries = mapRange.entrySet();

        boolean rangeIsAdded = false;
        try {

            for (Entry<String, Range> entry : entries) {
                key = entry.getKey();
                rangeRef = entry.getValue();

                // No intersection : add range into the map
                if (!(rangeRef.intersects(range))) {
                    if (((rangeRef.last() + 1) != range.first()) && ((rangeRef.first() - 1) != range.last())) {
                        continue;
                    }
                }

                // Intersection:

                // range is include in rangeRef : break
                if ((range.first() >= rangeRef.first()) && (range.last() <= rangeRef.last())) {
                    rangeIsAdded = true;
                    break;
                }

                // new Range with union
                Range newRange = rangeRef.union(range);
                mapRange.remove(key);
                mapRange.put(newRange.toString(), newRange);
                rangeIsAdded = true;
                break;

            }

        } catch (InvalidRangeException e) {
            throw new MotuException(ErrorType.BAD_PARAMETERS, "ERROR in DatasetGrid#addRange.", e);
        }

        if (!rangeIsAdded) {
            mapRange.put(range.toString(), range);
        }

        return true;

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
                                  axis.getName()));
        }

        // get the ranges list
        int rank = axis.getRank();

        if (rank != 1) {
            throw new MotuNotImplementedException(
                    String.format("ERROR - The subsetting of the coordinate axis '%s' with '%d' dimensions is not yet implemented",
                                  axis.getName(),
                                  rank));
        }

        Range[] ranges = new Range[rank];
        ranges[0] = range;
        List<Range> rangesList = Arrays.asList(ranges);

        // subset the variable
        VariableDS v_section = null;
        try {
            v_section = (VariableDS) axis.section(rangesList);
        } catch (InvalidRangeException e) {
            throw new MotuException(ErrorType.BAD_PARAMETERS, String.format("ERROR - in DatasetGrid#subset with axis name '%s'", axis.getName()), e);
        }
        List<Dimension> dims = v_section.getDimensions();
        for (Dimension dim : dims) {
            dim.setShared(true); // make them shared (section will make them unshared)
        }

        if (!(v_section instanceof CoordinateAxis)) {
            throw new MotuException(
                    ErrorType.INVALID_LAT_LON_RANGE,
                    String.format("ERROR - in DatasetGrid#subset: unexpected result after subsetting axis name '%s': new variable is not a 'CoordinateAxis' instance but a '%s'",
                                  axis.getName(),
                                  axis.getClass().getName()));

        }
        return (CoordinateAxis) v_section;
    }

    /**
     * initializes a list of global attributes ('dynamic' attributes that depend on data). Range values can be
     * Double.MAX_VALUE if no value
     *
     * @return a list of global attributes
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    public List<Attribute> initializeNetCdfDynGlobalAttributes() throws MotuException, MotuNotImplementedException {
        // intialisation
        List<Attribute> globalAttributes = new ArrayList<Attribute>();
        Attribute attribute = null;

        double min = 0.0;
        double max = 0.0;

        // -----------------------------
        // adds Time min/max attribute and date Units
        // -----------------------------
        if (hasOutputTimeDimension) {
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
        // -----------------------------
        // adds Z min/max attribute
        // -----------------------------
        if (hasOutputZDimension) {
            if (hasZRangeValue()) {
                min = zRangeValue[0];
                max = zRangeValue[1];
            } else {
                min = getRequestProduct().getProduct().getProductMetaData().getZAxisMinValue();
                max = getRequestProduct().getProduct().getProductMetaData().getZAxisMaxValue();
            }

            attribute = new Attribute(NetCdfReader.GLOBALATTRIBUTE_Z_MIN, min);
            globalAttributes.add(attribute);
            attribute = new Attribute(NetCdfReader.GLOBALATTRIBUTE_Z_MAX, max);
            globalAttributes.add(attribute);
        }
        // -----------------------------
        // adds Lat min/max attribute
        // -----------------------------
        if (hasOutputLatDimension) {
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
        // -----------------------------
        // adds Lon min/max attribute
        // -----------------------------
        if (hasOutputLonDimension) {
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

        return globalAttributes;
    }

    /**
     * initializes a list of global attributes ('fixed' attributes that don't depend on data).
     * 
     * @return a list of global attributes
     * 
     * @throws MotuException the motu exception
     */
    public List<Attribute> initializeNetCdfFixedGlobalAttributes() throws MotuException {
        List<Attribute> globalAttributes = new ArrayList<Attribute>();
        Attribute attribute = null;

        // -----------------------------
        // adds title attribute
        // -----------------------------
        try {
            attribute = getRequestProduct().getProduct().getNetCdfReader().getAttribute(NetCdfReader.GLOBALATTRIBUTE_TITLE);
        } catch (NetCdfAttributeNotFoundException e) {
            // LOG.error("initializeNetCdfFixedGlobalAttributes()", e);

            attribute = new Attribute(NetCdfReader.GLOBALATTRIBUTE_TITLE, getRequestProduct().getProduct().getProductMetaData().getTitle());
        }
        globalAttributes.add(attribute);
        // -----------------------------
        // adds institution attribute
        // -----------------------------
        try {
            attribute = getRequestProduct().getProduct().getNetCdfReader().getAttribute(NetCdfReader.GLOBALATTRIBUTE_INSTITUTION);
        } catch (NetCdfAttributeNotFoundException e) {
            // LOG.error("initializeNetCdfFixedGlobalAttributes()", e);

            if (getRequestProduct().getProduct().getProductMetaData().getDataProvider() != null) {
                attribute = new Attribute(
                        NetCdfReader.GLOBALATTRIBUTE_INSTITUTION,
                        getRequestProduct().getProduct().getProductMetaData().getDataProvider().getName());
            } else {
                attribute = new Attribute(NetCdfReader.GLOBALATTRIBUTE_INSTITUTION, " ");
            }
        }
        globalAttributes.add(attribute);
        // -----------------------------
        // adds references attribute
        // -----------------------------
        try {
            attribute = getRequestProduct().getProduct().getNetCdfReader().getAttribute(NetCdfReader.GLOBALATTRIBUTE_REFERENCES);
        } catch (NetCdfAttributeNotFoundException e) {
            // LOG.error("initializeNetCdfFixedGlobalAttributes()", e);

            if (getRequestProduct().getProduct().getProductMetaData().getDataProvider() != null) {
                attribute = new Attribute(
                        NetCdfReader.GLOBALATTRIBUTE_REFERENCES,
                        getRequestProduct().getProduct().getProductMetaData().getDataProvider().getWebSite());
            } else {
                attribute = new Attribute(NetCdfReader.GLOBALATTRIBUTE_REFERENCES, " ");
            }
        }
        globalAttributes.add(attribute);
        // -----------------------------
        // adds source attribute
        // -----------------------------
        try {
            attribute = getRequestProduct().getProduct().getNetCdfReader().getAttribute(NetCdfReader.GLOBALATTRIBUTE_SOURCE);
        } catch (NetCdfAttributeNotFoundException e) {
            // LOG.error("initializeNetCdfFixedGlobalAttributes()", e);

            attribute = new Attribute(NetCdfReader.GLOBALATTRIBUTE_SOURCE, " ");
        }
        globalAttributes.add(attribute);
        // -----------------------------
        // adds conventions attribute
        // -----------------------------
        attribute = new Attribute(NetCdfReader.GLOBALATTRIBUTE_CONVENTIONS, NetCdfReader.GLOBALATTRIBUTE_CONVENTIONS_VALUE);
        globalAttributes.add(attribute);
        // -----------------------------
        // adds comment attribute
        // -----------------------------
        try {
            attribute = getRequestProduct().getProduct().getNetCdfReader().getAttribute(NetCdfReader.GLOBALATTRIBUTE_COMMENT);
        } catch (NetCdfAttributeNotFoundException e) {
            // LOG.error("initializeNetCdfFixedGlobalAttributes()", e);

            attribute = new Attribute(NetCdfReader.GLOBALATTRIBUTE_COMMENT, getRequestProduct().getProduct().getProductMetaData().getDescription());
        }
        // -----------------------------
        // adds history attribute
        // -----------------------------
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Data extracted from dataset ");
        stringBuffer.append(getRequestProduct().getProduct().getLocationData());
        globalAttributes.add(new Attribute(NetCdfReader.GLOBALATTRIBUTE_HISTORY, stringBuffer.toString()));
        // -----------------------------
        // adds easting attribute
        // -----------------------------
        try {
            attribute = getRequestProduct().getProduct().getNetCdfReader().getAttribute(NetCdfReader.GLOBALATTRIBUTE_EASTING);
        } catch (NetCdfAttributeNotFoundException e) {
            // LOG.error("initializeNetCdfFixedGlobalAttributes()", e);

            // Do nothing
        }
        // -----------------------------
        // adds northing attribute
        // -----------------------------
        try {
            attribute = getRequestProduct().getProduct().getNetCdfReader().getAttribute(NetCdfReader.GLOBALATTRIBUTE_NORTHING);
        } catch (NetCdfAttributeNotFoundException e) {
            // LOG.error("initializeNetCdfFixedGlobalAttributes()", e);

            // Do nothing
        }

        return globalAttributes;
    }

}
