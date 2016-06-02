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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.library.misc.exception.MotuExceedingCapacityException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDateRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidLatLonRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuNoVarException;
import fr.cls.atoll.motu.library.misc.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfVariableException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfVariableNotFoundException;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaDatetime;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaDepth;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaLatLon;
import fr.cls.atoll.motu.web.common.format.OutputFormat;
import fr.cls.atoll.motu.web.dal.request.netcdf.NetCdfReader;
import fr.cls.atoll.motu.web.dal.request.netcdf.NetCdfWriter;
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

//CSOFF: MultipleStringLiterals : avoid message in constants declaration and trace log.

/**
 * Gridded dataset class.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class DatasetGrid extends fr.cls.atoll.motu.web.dal.request.netcdf.data.DatasetBase {

    private static final Logger LOG = LogManager.getLogger();

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
            MotuNotImplementedException, MotuInvalidDepthRangeException, MotuInvalidLatLonRangeException, NetCdfVariableException, MotuNoVarException,
            NetCdfVariableNotFoundException {

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
                // throw new MotuNotImplementedException(String
                // .format("Variable %s in not geo-referenced - Non-georeferenced data is not implemented
                // (method: DatasetGrid.extractData)",
                // varData.getVarName()));
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

                    throw new MotuException("Error in subsetting geo grid", e);
                }
            }
            // pass geoGridsubset and geoGrid (the original geoGrid) to be able to get somme information
            // (lost
            // in subsetting - See bug above) about the variable of the GeoGrid
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
    public void extractData(OutputFormat dataOutputFormat) throws MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException,
            MotuNotImplementedException, MotuInvalidDepthRangeException, MotuInvalidLatLonRangeException, NetCdfVariableException, MotuNoVarException,
            NetCdfVariableNotFoundException, IOException {
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

        case NETCDF4:
            throw new MotuNotImplementedException(
                    String.format("extraction into %s is not implemented for OPENDAP (if you wish to enable it enable the NCSS)",
                                  dataOutputFormat.toString()));

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
            MotuNotImplementedException, MotuInvalidDepthRangeException, MotuInvalidLatLonRangeException, NetCdfVariableException, MotuNoVarException,
            NetCdfVariableNotFoundException, IOException {
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

        // Is this dataset a Geo X/Y with Lat/Lon whose dimensions depend on X/Y ?
        boolean isGeoXY = product.hasGeoXYAxisWithLonLatEquivalence();

        /*
         * try { NetCdfReader.toNcML(product.getNetCdfReaderDataset(), "c:\\temp\\test.xml"); } catch
         * (IOException e1) { // TODO Auto-generated catch block e1.printStackTrace(); }
         */
        String locationData = product.getNetCdfReaderDataset().getLocation();
        NetCdfReader netCdfReader = new NetCdfReader(locationData, product.isCasAuthentication());
        this.readingTime += netCdfReader.open(false);

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
            netCdfWriter.writeVariables(listVariableXSubset, mapXRange, product.getNetCdfReader().getOrignalVariables());
            netCdfWriter.writeVariables(listVariableYSubset, mapYRange, product.getNetCdfReader().getOrignalVariables());

            addGeoXYNeededVariables();

        }
        // List<CoordinateAxis> listVariableXSubset = new ArrayList<CoordinateAxis>();
        // List<CoordinateAxis> listVariableYSubset = new ArrayList<CoordinateAxis>();
        // Map<String, Range> mapXRange = new HashMap<String, Range>();
        // Map<String, Range> mapYRange = new HashMap<String, Range>();
        //
        // Map<String, List<Section>> mapVarOrgRanges = new HashMap<String, List<Section>>();

        for (VarData varData : variablesValues()) {

            GeoGrid geoGrid = gds.findGridByName(varData.getVarName());
            if (geoGrid == null) {
                // throw new MotuNotImplementedException(String
                // .format("Variable %s in not geo-referenced - Non-georeferenced data is not implemented
                // (method: DatasetGrid.extractData)",
                // varData.getVarName()));
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
                        if (Organizer.isNullOrEmpty(listVarOrgRanges)) {
                            listVarOrgRanges = new ArrayList<Section>();
                            mapVarOrgRanges.put(varName, listVarOrgRanges);
                        }

                        Section section = DatasetGrid.getOriginalRangeList(geoGridSubset, tRange, zRange, yRange, xRange);
                        listVarOrgRanges.add(section);

                        // if coordinate axes of the geogrid are Lat/Lon
                        // then compute the original range for the subset Lat/lon.
                        prepareLatLonWriting(geoGridSubset, yRange, xRange);

                    }

                } catch (InvalidRangeException e) {
                    LOG.error("extractDataIntoNetCdf()", e);

                    throw new MotuException("Error in subsetting geo grid", e);
                }
            }
            // for (GeoGrid g : listGeoGridSubset) {
            // Variable v = g.getVariable();
            // int[] sh = g.getShape();
            // StringBuffer stringBuffer = new StringBuffer();
            // for (int s : sh) {
            // stringBuffer.append(s);
            // stringBuffer.append(",");
            // }
            //
            // Section section = v.getShapeAsSection();
            //
            // int p = 1;
            // for (Range r : v.getRanges()) {
            // p = p * r.length();
            // }
            // System.out.println(v.getName() + ":" + stringBuffer.toString() + " / " +
            // v.getRanges().toString() + " / " + v.getRanges().size()
            // + " / " + p + " / " + section);
            // // Array array = v.read();
            // // double[] vals = (double[]) array.get1DJavaArray(Double.class);
            // // for (double val : vals) {
            // // System.out.print(val);
            // // System.out.print(" ");
            // // }
            // }
            if (isGeoXY) {
                // //prepareLatLonWriting(listGeoGridSubset);
                // prepareXYWriting();
                //
                // netCdfWriter.writeVariables(listVariableXSubset, mapXRange,
                // product.getNetCdfReader().getOrignalVariables());
                // netCdfWriter.writeVariables(listVariableYSubset, mapYRange,
                // product.getNetCdfReader().getOrignalVariables());

                // pass geoGridsubset and geoGrid (the original geoGrid) to be able to get some information
                // (lost
                // in subsetting - See bug below) about the variable of the GeoGrid
                netCdfWriter.writeVariablesWithGeoXY(listGeoGridSubset, geoGrid, gds, product.getNetCdfReader().getOrignalVariables());

            } else {

                // pass geoGridsubset and geoGrid (the original geoGrid) to be able to get some information
                // (lost
                // in subsetting - See bug below) about the variable of the GeoGrid
                netCdfWriter.writeVariables(listGeoGridSubset, geoGrid, gds, product.getNetCdfReader().getOrignalVariables());
            }

        }
        if (isGeoXY) {
            netCdfWriter.finishGeoXY(VAR_ATTR_TO_REMOVE, listDistinctXRange, listDistinctYRange, mapVarOrgRanges);
        } else {
            netCdfWriter.finish(VAR_ATTR_TO_REMOVE);
        }

        this.readingTime += netCdfWriter.getReadingTime();
        this.writingTime += netCdfWriter.getWritingTime();

        product.moveTempExtractFileToFinalExtractFile();

        if (LOG.isDebugEnabled()) {
            LOG.debug("extractDataIntoNetCdf() - exiting");
        }
    }

    protected void addGeoXYNeededVariables() {
        // Add Longitude variable
        ParameterMetaData parameterMetaData = product.findLongitudeIgnoreCase();
        VarData varData = VarData.createFrom(parameterMetaData);
        if (varData != null) {
            putVariables(varData.getVarName(), varData);

        }
        parameterMetaData = product.findLatitudeIgnoreCase();
        varData = VarData.createFrom(parameterMetaData);
        if (varData != null) {
            putVariables(varData.getVarName(), varData);

        }
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
            xaxis.setAxisType(product.getCoordinateAxisType(xaxis.getName()));
        }
        if (yaxis.getAxisType() == null) {
            yaxis.setAxisType(product.getCoordinateAxisType(yaxis.getName()));
        }

        if ((xaxis.getAxisType() == AxisType.GeoX) || (yaxis.getAxisType() == AxisType.GeoY)) {
            return;
        }

        if (xaxis.getAxisType() == null) {
            throw new MotuException(String.format("ERROR in DatasetGrid#prepareLatLonWriting - axis type for '%s' axis is null", xaxis.getName()));
        }
        if (yaxis.getAxisType() == null) {
            throw new MotuException(String.format("ERROR in DatasetGrid#prepareLatLonWriting - axis type for '%s' axis is null", yaxis.getName()));
        }

        String xName = xaxis.getName();
        List<Section> listVarOrgRanges = mapVarOrgRanges.get(xName);
        if (Organizer.isNullOrEmpty(listVarOrgRanges)) {
            listVarOrgRanges = new ArrayList<Section>();
            mapVarOrgRanges.put(xName, listVarOrgRanges);
        }

        Section section = getOriginalRangeListGeoAxis(xaxis, yRange, xRange);
        listVarOrgRanges.add(section);

        String yName = yaxis.getName();
        listVarOrgRanges = mapVarOrgRanges.get(yName);
        if (Organizer.isNullOrEmpty(listVarOrgRanges)) {
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

            DatasetGrid.addRange(yRange, mapYRange);

            DatasetGrid.addRange(xRange, mapXRange);

        }

        // for (List<Range> yxRanges : listYXRanges) {
        // // GridDatatype geoGridSubset = null;
        // Range yRange = yxRanges.get(0);
        // Range xRange = yxRanges.get(1);
        // System.out.print(yRange.toString());
        // System.out.print(" ");
        // System.out.print(xRange.toString());
        // System.out.println(" ");
        // }

        RangeComparator rangeComparator = new RangeComparator();

        listDistinctXRange = new ArrayList<Range>(mapXRange.values());
        Collections.sort(listDistinctXRange, rangeComparator);

        listDistinctYRange = new ArrayList<Range>(mapYRange.values());
        Collections.sort(listDistinctYRange, rangeComparator);

        for (Range range : listDistinctYRange) {
            CoordinateAxis axis = DatasetGrid.subset(product.getGeoYAxis(), range);
            listVariableYSubset.add(axis);
            String axisName = axis.getName();
            List<Section> listVarOrgRanges = mapVarOrgRanges.get(axisName);
            if (Organizer.isNullOrEmpty(listVarOrgRanges)) {
                listVarOrgRanges = new ArrayList<Section>();
                mapVarOrgRanges.put(axisName, listVarOrgRanges);
            }
            List<Range> lr = new ArrayList<Range>();
            lr.add(range);
            Section section = new Section(lr);
            listVarOrgRanges.add(section);

        }

        for (Range range : listDistinctXRange) {
            CoordinateAxis axis = DatasetGrid.subset(product.getGeoXAxis(), range);
            listVariableXSubset.add(axis);
            String axisName = axis.getName();
            List<Section> listVarOrgRanges = mapVarOrgRanges.get(axisName);
            if (Organizer.isNullOrEmpty(listVarOrgRanges)) {
                listVarOrgRanges = new ArrayList<Section>();
                mapVarOrgRanges.put(axisName, listVarOrgRanges);
            }
            List<Range> lr = new ArrayList<Range>();
            lr.add(range);
            Section section = new Section(lr);
            listVarOrgRanges.add(section);
        }

        // for (Range r : listDistinctXRange) {
        // // GridDatatype geoGridSubset = null;
        // System.out.print(r.toString());
        // System.out.println(" ");
        // }
        // for (Range r : listDistinctYRange) {
        // // GridDatatype geoGridSubset = null;
        // System.out.print(r.toString());
        // System.out.println(" ");
        // }
        //
        // for (Entry<String, List<Section>> entry : mapVarOrgRanges.entrySet()) {
        // String key = entry.getKey();
        // List<Section> sections = entry.getValue();
        // System.out.print(key);
        // System.out.print(" ");
        // for (Section section : sections) {
        // System.out.print(section);
        // System.out.print(" / ");
        // }
        // System.out.println(" ");
        // }

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
            xDimOrgIndex = DatasetGrid.findDimension(geoGrid, gcs.getXHorizAxis().getDimension(0));
            yDimOrgIndex = DatasetGrid.findDimension(geoGrid, gcs.getYHorizAxis().getDimension(0));

        } else { // 2D case
            yDimOrgIndex = DatasetGrid.findDimension(geoGrid, gcs.getXHorizAxis().getDimension(0));
            xDimOrgIndex = DatasetGrid.findDimension(geoGrid, gcs.getXHorizAxis().getDimension(1));
        }

        if (gcs.getVerticalAxis() != null)
            zDimOrgIndex = DatasetGrid.findDimension(geoGrid, gcs.getVerticalAxis().getDimension(0));
        if (gcs.getTimeAxis() != null) {
            if (gcs.getTimeAxis1D() != null)
                tDimOrgIndex = DatasetGrid.findDimension(geoGrid, gcs.getTimeAxis1D().getDimension(0));
            else
                tDimOrgIndex = DatasetGrid.findDimension(geoGrid, gcs.getTimeAxis().getDimension(1));
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
        return getGeoDimVarIndex(var, product.getGeoXAxis());
    }

    /**
     * Gets the geo y dim var index.
     * 
     * @param var the var
     * @return the geo y dim var index
     */
    protected int getGeoYDimVarIndex(Variable var) {
        return getGeoDimVarIndex(var, product.getGeoYAxis());
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
            throw new MotuException("ERROR in DatasetGrid#addRange.", e);
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
            throw new MotuException("ERROR - in DatasetGrid#subset axis parameter is null");
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
            throw new MotuException(String.format("ERROR - in DatasetGrid#subset with axis name '%s'", axis.getName()), e);
        }
        List<Dimension> dims = v_section.getDimensions();
        for (Dimension dim : dims) {
            dim.setShared(true); // make them shared (section will make them unshared)
        }

        if (!(v_section instanceof CoordinateAxis)) {
            throw new MotuException(
                    String.format("ERROR - in DatasetGrid#subset: unexpected result after subsetting axis name '%s': new variable is not a 'CoordinateAxis' instance but a '%s'",
                                  axis.getName(),
                                  axis.getClass().getName()));

        }
        return (CoordinateAxis) v_section;
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
    public Range getZRange() throws MotuException, MotuInvalidDepthRangeException, NetCdfVariableException {
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
                throw new MotuException(
                        String.format("Inconsistency between Longitude ranges list (%d items) and Longitude values list (%d items) - (%s)",
                                      listYXRanges.size(),
                                      rangesLonValue.size(),
                                      this.getClass().getName()));
            }

            if (listYXRanges.size() != rangesLatValue.size()) {
                throw new MotuException(
                        String.format("Inconsistency between Latitude ranges list (%d items) and Latitude values list (%d items) - (%s)",
                                      listYXRanges.size(),
                                      rangesLatValue.size(),
                                      this.getClass().getName()));
            }

            MAMath.MinMax minMaxLat = null;
            MAMath.MinMax minMaxLon = null;

            if (productMetadata.hasLatLonAxis2D()) {
                minMaxLat = extractCriteriaLatLon.getMinMaxYValue2D();
                minMaxLon = extractCriteriaLatLon.getMinMaxXValue2D();
                if (minMaxLat == null) {
                    throw new MotuException(
                            "Error in DatasetGrid#getAdjacentYXRange: Latitude/Longitude axes are 2D and min/max latitude values to extract are null");
                }
                if (minMaxLon == null) {
                    throw new MotuException(
                            "Error in DatasetGrid#getAdjacentYXRange: Latitude/Longitude axes are 2D and min/max longitude values to extract are null");
                }
                // System.out.println("extractCriteriaLatLon.minXValue2D");
                // System.out.println(extractCriteriaLatLon.getMinMaxXValue2D().min);
                // System.out.println("extractCriteriaLatLon.maxXValue2D");
                // System.out.println(extractCriteriaLatLon.getMinMaxXValue2D().max);
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
        } else if (productMetadata.hasGeoXYAxis()) {
            throw new MotuNotImplementedException(
                    "Extraction with X/Y axes and without Lat/Lon data are not implemented (method DatasetGrid#getAdjacentYXRange");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getAdjacentYXRange() - exiting");
        }
    }

}
// CSON: MultipleStringLiterals
