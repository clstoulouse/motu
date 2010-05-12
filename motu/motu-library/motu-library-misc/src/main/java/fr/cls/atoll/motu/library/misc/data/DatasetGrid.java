package fr.cls.atoll.motu.library.misc.data;

import fr.cls.atoll.motu.library.misc.exception.MotuExceedingCapacityException;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDateRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidLatLonRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuNoVarException;
import fr.cls.atoll.motu.library.misc.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfVariableException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfVariableNotFoundException;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.library.misc.netcdf.NetCdfReader;
import fr.cls.atoll.motu.library.misc.netcdf.NetCdfWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridDataset;

//CSOFF: MultipleStringLiterals : avoid message in constants declaration and trace log.

/**
 * Gridded dataset class.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class DatasetGrid extends fr.cls.atoll.motu.library.misc.data.DatasetBase {
    /**
     * Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(DatasetGrid.class);

    /**
     * Default constructor.
     */
    public DatasetGrid() {
    }

    /**
     * Constructor.
     * 
     * @param product product linked to the dataset
     */
    public DatasetGrid(Product product) {
        super(product);
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
    public void computeAmountDataSize() throws MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException,
            MotuNotImplementedException, MotuInvalidDepthRangeException, MotuInvalidLatLonRangeException, NetCdfVariableException,
            MotuNoVarException, NetCdfVariableNotFoundException {

        initGetAmountData();

        getTimeRange();
        // gets ranges to be extracted
        // Number of ranges for Longitude can be 1 or 2.
        getAdjacentYXRange();
        // getYXRange();
        getZRange();

        GridDataset gds = new GridDataset(product.getNetCdfReaderDataset());

        // List <Variable> listVars = gds.getDataVariables();
        // for (Variable v : listVars) {
        // System.out.print("GDS Var:\t");
        // System.out.print(v.getName());
        // System.out.println(v.isCaching());
        // }
        NetCdfWriter netCdfWriter = new NetCdfWriter();

        netCdfWriter.resetAmountDataSize();

        for (VarData varData : variablesValues()) {

            GeoGrid geoGrid = gds.findGridByName(varData.getVarName());
            if (geoGrid == null) {
                throw new MotuNotImplementedException(String
                        .format("Variable %s in not geo-referenced - Non-georeferenced data is not implemented (method: DatasetGrid.extractData)",
                                varData.getVarName()));
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

                    throw new MotuException("Error in subsetting geo grid", e);
                }
            }
            // pass geoGridsubset and geoGrid (the original geoGrid) to be able to get somme information
            // (lost
            // in subsetting - See bug below) about the variable of the GeoGrid
            netCdfWriter.computeAmountDataSize(listGeoGridSubset);
        }

        amountDataSize = netCdfWriter.getAmountDataSize();
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
    public void extractData(Organizer.Format dataOutputFormat) throws MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException,
            MotuNotImplementedException, MotuInvalidDepthRangeException, MotuInvalidLatLonRangeException, NetCdfVariableException,
            MotuNoVarException, NetCdfVariableNotFoundException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractData() - entering");
        }

        if (product == null) {
            throw new MotuException("Error in DatasetGrid - extractData product have not nbeen set (= null)");
        }

        switch (dataOutputFormat) {
        case NETCDF:
            extractDataIntoNetCdf();
            break;

        default:
            throw new MotuNotImplementedException(String.format("extraction into %s is not implemented", dataOutputFormat.toString()));
            // break;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("extractData() - exiting");
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
            MotuNotImplementedException, MotuInvalidDepthRangeException, MotuInvalidLatLonRangeException, NetCdfVariableException,
            MotuNoVarException, NetCdfVariableNotFoundException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractDataIntoNetCdf() - entering");
        }

        initNetCdfExtraction();

        getTimeRange();
        // gets ranges to be extracted
        // Number of ranges for Longitude can be 1 or 2.
        getAdjacentYXRange();
        // getYXRange();
        getZRange();
        /*
         * try { NetCdfReader.toNcML(product.getNetCdfReaderDataset(), "c:\\temp\\test.xml"); } catch
         * (IOException e1) { // TODO Auto-generated catch block e1.printStackTrace(); }
         */
        String locationData = product.getNetCdfReaderDataset().getLocation();
        NetCdfReader netCdfReader = new NetCdfReader(locationData, product.isCasAuthentification());
        netCdfReader.open(false);

        // GridDataset gds = new GridDataset(product.getNetCdfReaderDataset());
        GridDataset gds = new GridDataset(netCdfReader.getNetcdfDataset());

        List<Attribute> globalFixedAttributes = initializeNetCdfFixedGlobalAttributes();
        List<Attribute> globalDynAttributes = initializeNetCdfDynGlobalAttributes();

        // List <Variable> listVars = gds.getDataVariables();
        // for (Variable v : listVars) {
        // System.out.print("GDS Var:\t");
        // System.out.print(v.getName());
        // System.out.println(v.isCaching());
        // }

        // NetCdfWriter netCdfWriter = new NetCdfWriter(product.getExtractLocationData(), true);
        NetCdfWriter netCdfWriter = new NetCdfWriter(product.getExtractLocationDataTemp(), true);

        netCdfWriter.writeGlobalAttributes(globalFixedAttributes);
        netCdfWriter.writeGlobalAttributes(globalDynAttributes);

        netCdfWriter.resetAmountDataSize();

        for (VarData varData : variablesValues()) {

            GeoGrid geoGrid = gds.findGridByName(varData.getVarName());
            if (geoGrid == null) {
                throw new MotuNotImplementedException(String
                        .format("Variable %s in not geo-referenced - Non-georeferenced data is not implemented (method: DatasetGrid.extractData)",
                                varData.getVarName()));
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
                    LOG.error("extractDataIntoNetCdf()", e);

                    throw new MotuException("Error in subsetting geo grid", e);
                }
            }
            // pass geoGridsubset and geoGrid (the original geoGrid) to be able to get somme information
            // (lost
            // in subsetting - See bug below) about the variable of the GeoGrid

            netCdfWriter.writeVariables(listGeoGridSubset, geoGrid, gds, product.getNetCdfReader().getOrignalVariables());
        }

        netCdfWriter.finish(VAR_ATTR_TO_REMOVE);

        product.moveTempExtractFileToFinalExtractFile();

        if (LOG.isDebugEnabled()) {
            LOG.debug("extractDataIntoNetCdf() - exiting");
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTimeRange() - entering");
        }

        if (tRangeValue != null) {
            assert tRangeValue.length == 2;
            tRangeValue[0] = Double.MAX_VALUE;
            tRangeValue[1] = Double.MAX_VALUE;
        }

        tRange = null;
        if (productMetadata.hasTimeAxis()) {
            Array timeAxisData = product.getTimeAxisData();
            CoordinateAxis timeAxis = productMetadata.getTimeAxis();

            ExtractCriteriaDatetime extractCriteriaDatetime = findCriteriaDatetime();
            if (extractCriteriaDatetime != null) {
                tRange = extractCriteriaDatetime.toRange(timeAxisData, timeAxis.getUnitsString(), tRangeValue);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getTimeRange() - exiting");
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("getYXRange() - entering");
        }

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
        if (productMetadata.hasLatLonAxis()) {
            yxRange = new Range[2];
            ExtractCriteriaLatLon extractCriteriaLatLon = findCriteriaLatLon();
            if (extractCriteriaLatLon != null) {
                CoordinateSystem cs = new CoordinateSystem(product.getNetCdfReaderDataset(), productMetadata.getLatLonAxis(), null);
                List<Range> yxRanges = extractCriteriaLatLon.toRange(cs, yRangeValue, xRangeValue);
                if (yxRanges.size() == 2) {
                    yxRange[0] = yxRanges.get(0);
                    yxRange[1] = yxRanges.get(1);
                }
            }
        } else if (productMetadata.hasGeoXYAxis()) {
            throw new MotuNotImplementedException("X/Y axis is not implemented (method DatasetGrid.getYXRange");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getYXRange() - exiting");
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
    public void getZRange() throws MotuException, MotuInvalidDepthRangeException, NetCdfVariableException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getZRange() - entering");
        }

        if (zRangeValue != null) {
            assert zRangeValue.length == 2;
            zRangeValue[0] = Double.MAX_VALUE;
            zRangeValue[1] = Double.MAX_VALUE;
        }

        zRange = null;
        if (productMetadata.hasZAxis()) {
            Array zAxisData = product.getZAxisData();
            ExtractCriteriaDepth extractCriteriaDepth = findCriteriaDepth();
            if (extractCriteriaDepth != null) {
                zRange = extractCriteriaDepth.toRange(zAxisData, zRangeValue);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getZRange() - exiting");
        }
    }

    /**
     * Gets a list of Index Ranges for the given lat, lon bounding box. For projection, only an approximation
     * based on lat/lon corners. Must have 2D/LatLon for x and y axis.
     * 
     * @throws MotuNotImplementedException
     * @throws MotuException
     */
    public void getAdjacentYXRange() throws MotuException, MotuInvalidLatLonRangeException, MotuNotImplementedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAdjacentYXRange() - entering");
        }

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

        if (productMetadata.hasLatLonAxis()) {
            ExtractCriteriaLatLon extractCriteriaLatLon = findCriteriaLatLon();
            if (extractCriteriaLatLon == null) {
                extractCriteriaLatLon = new ExtractCriteriaLatLon();
                addCriteria(extractCriteriaLatLon);
            }
            CoordinateSystem cs = new CoordinateSystem(product.getNetCdfReaderDataset(), productMetadata.getLatLonAxis(), null);
            listYXRanges = extractCriteriaLatLon.toListRanges(cs, rangesLatValue, rangesLonValue);

            if (listYXRanges.size() != rangesLonValue.size()) {
                throw new MotuException(String
                        .format("Inconsistency between Longitude ranges list (%d items) and Longitude values list (%d items) - (%s)", listYXRanges
                                .size(), rangesLonValue.size(), this.getClass().getName()));
            }

            if (listYXRanges.size() != rangesLatValue.size()) {
                throw new MotuException(String
                        .format("Inconsistency between Latitude ranges list (%d items) and Latitude values list (%d items) - (%s)", listYXRanges
                                .size(), rangesLatValue.size(), this.getClass().getName()));
            }

            MAMath.MinMax minMaxLat = getMinMaxLatNormal();
            yRangeValue[0] = minMaxLat.min;
            yRangeValue[1] = minMaxLat.max;

            MAMath.MinMax minMaxLon = getMinMaxLonNormal();
            xRangeValue[0] = minMaxLon.min;
            xRangeValue[1] = minMaxLon.max;
        } else if (productMetadata.hasGeoXYAxis()) {
            throw new MotuNotImplementedException("X/Y axis is not implemented (method DatasetGridXYLatLon.getAdjacentYXRange");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getAdjacentYXRange() - exiting");
        }
    }

}
// CSON: MultipleStringLiterals
