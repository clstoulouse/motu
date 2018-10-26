package fr.cls.atoll.motu.web.dal.catalog.product.metadata.opendap;

import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuExceptionBase;
import fr.cls.atoll.motu.web.dal.request.netcdf.NetCdfReader;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ParameterMetaData;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ProductMetaData;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis2D;

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
public class OpenDapProductMetadataReader {

    /** Contains variables names of 'gridded' product that are hidden to the user. */
    private static final String[] UNUSED_VARIABLES_GRIDS = new String[] { "LatLonMin", "LatLonStep", "LatLon", };

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger();

    /** NetCdfReader object. */
    private NetCdfReader netCdfReader = null;
    private String productId;

    public OpenDapProductMetadataReader(String productId_, String locationData) {
        netCdfReader = new NetCdfReader(locationData);
        productId = productId_;

    }

    /**
     * Valeur de productId.
     * 
     * @return la valeur.
     */
    public String getProductId() {
        return productId;
    }

    private void initProductMetaDataTitle(ProductMetaData productMetaData) {
        if (productMetaData.getTitle() != null && productMetaData.getTitle().length() <= 0) {
            String title = netCdfReader.getStringValue("title");
            if (title != null) {
                productMetaData.setTitle(title);
            } else {
                productMetaData.setTitle(getProductId());
                LOGGER.warn("Unable to get dataset title of product=" + getProductId() + ", set title with productId");
            }
        }
    }

    private void initProductMetaDataFileType(ProductMetaData productMetaData) {
        String fileType = netCdfReader.getStringValue("filetype");
        if (fileType != null) {
            productMetaData.setProductCategory(fileType);
        }
    }

    private void initProductMetaDataCoordinateAxes(ProductMetaData productMetaData) throws MotuException {
        // Gets coordinate axes metadata.
        List<CoordinateAxis> coordinateAxes = netCdfReader.getCoordinateAxes();
        for (Iterator<CoordinateAxis> it = coordinateAxes.iterator(); it.hasNext();) {
            CoordinateAxis coordinateAxis = it.next();
            AxisType axisType = coordinateAxis.getAxisType();
            if (axisType != null) {
                productMetaData.getCoordinateAxisMap().put(axisType, coordinateAxis);
            }
        }

        if (productMetaData.hasTimeAxis()) {
            productMetaData.setTimeCoverage(productMetaData.getTimeAxisMinValue(), productMetaData.getTimeAxisMaxValue());
        }
    }

    /**
     * Reads product global metadata from an (NetCDF file).
     * 
     * @param productMetaDataOnlyForUpdate if null initialize a new ProductMetaData object
     * @return
     * @throws MotuException
     */
    public ProductMetaData loadMetaData(ProductMetaData productMetaDataOnlyForUpdate) throws MotuException {
        ProductMetaData productMetaData = productMetaDataOnlyForUpdate != null ? productMetaDataOnlyForUpdate : new ProductMetaData();
        productMetaData.setProductId(getProductId());
        productMetaData.setTitle(getProductId());

        netCdfReader.open(true);
        initProductMetaDataTitle(productMetaData);
        initProductMetaDataFileType(productMetaData);
        initProductMetaDataCoordinateAxes(productMetaData);
        initProductVariablesParameterMetaDatas(productMetaData);
        initGeoYAxisWithLatEquivalence(productMetaData);
        initGeoXAxisWithLatEquivalence(productMetaData);

        // netCdfReader.close();
        // TODO If netCdfReader is closed cannot compute MinMax for StereoGraphicProjection
        // @See fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaLatLon#toListRanges(CoordinateSystem
        // cs, List<double[]> listRangeValueLat, List<double[]> listRangeValueLon)
        // GridCoordSys gcs = new GridCoordSys(cs, errMessages);

        return productMetaData;
    }

    /**
     * Checks for geo Y axis with lat equivalence.
     * 
     * @param netCdfReader the net cdf reader
     * 
     * @return true if GeoX axis exists among coordinate axes and if there is a longitude variable equivalence
     *         (Variable whose name is 'longitude' and with at least two dimensions X/Y).
     * 
     * @throws MotuException the motu exception
     */
    public void initGeoYAxisWithLatEquivalence(ProductMetaData productMetaData) throws MotuException {
        CoordinateAxis coord = productMetaData.getGeoYAxis();
        if (coord == null) {
            productMetaData.setGeoYAxisWithLatEquivalence(false);
        }

        ParameterMetaData parameterMetaData = productMetaData.findLatitudeIgnoreCase();

        if (parameterMetaData == null) {
            productMetaData.setGeoYAxisWithLatEquivalence(false);
        } else {
            List<Dimension> listDims = parameterMetaData.getDimensions();
            productMetaData.setGeoYAxisWithLatEquivalence(netCdfReader.hasGeoXYDimensions(listDims));
        }

    }

    /**
     * Checks for geo X axis with lon equivalence.
     * 
     * @param netCdfReader the net cdf reader
     * 
     * @return true if GeoX axis exists among coordinate axes and if there is a longitude variable equivalence
     *         (Variable whose name is 'longitude' and with at least two dimensions X/Y).
     * 
     * @throws MotuException the motu exception
     */
    public void initGeoXAxisWithLatEquivalence(ProductMetaData productMetaData) throws MotuException {
        CoordinateAxis coord = productMetaData.getGeoXAxis();
        if (coord == null) {
            productMetaData.setGeoXAxisWithLatEquivalence(false);
        }

        ParameterMetaData parameterMetaData = productMetaData.findLongitudeIgnoreCase();

        if (parameterMetaData == null) {
            productMetaData.setGeoXAxisWithLatEquivalence(false);
        } else {
            List<Dimension> listDims = parameterMetaData.getDimensions();
            productMetaData.setGeoXAxisWithLatEquivalence(netCdfReader.hasGeoXYDimensions(listDims));
        }
    }

    private boolean isVariableACoordinateAxisAndNotACoordinateAxis2D(Variable variable) {
        return variable != null && (variable instanceof CoordinateAxis) && ((CoordinateAxis) variable).getAxisType() != null
                && !(((CoordinateAxis) variable) instanceof CoordinateAxis2D);
    }

    private boolean isAUnusedVariable(Variable variable) {
        int i = 0;
        while (i < UNUSED_VARIABLES_GRIDS.length && !variable.getFullName().equalsIgnoreCase(UNUSED_VARIABLES_GRIDS[i])) {
            i++;
        }
        return i < UNUSED_VARIABLES_GRIDS.length;
    }

    private ParameterMetaData createParameterMetaData(Variable variable) {
        ParameterMetaData parameterMetaData = new ParameterMetaData();
        parameterMetaData.setName(variable.getFullName());
        parameterMetaData.setLabel(variable.getDescription());
        parameterMetaData.setUnit(variable.getUnitsString());
        parameterMetaData.setDimensions(variable.getDimensions());

        try {
            parameterMetaData.setUnitLong(NetCdfReader.getStringValue(variable, NetCdfReader.VARIABLEATTRIBUTE_UNIT_LONG));
        } catch (MotuExceptionBase e) {
            parameterMetaData.setUnitLong("");
        }
        try {
            parameterMetaData.setStandardName(NetCdfReader.getStringValue(variable, NetCdfReader.VARIABLEATTRIBUTE_STANDARD_NAME));
        } catch (MotuExceptionBase e) {
            parameterMetaData.setStandardName("");
        }
        try {
            parameterMetaData.setLongName(NetCdfReader.getStringValue(variable, NetCdfReader.VARIABLEATTRIBUTE_LONG_NAME));
        } catch (MotuExceptionBase e) {
            parameterMetaData.setLongName("");
        }

        return parameterMetaData;
    }

    private void initProductVariablesParameterMetaDatas(ProductMetaData productMetaData) {
        List<Variable> variables = netCdfReader.getVariables();
        for (Iterator<Variable> it = variables.iterator(); it.hasNext();) {
            Variable variable = it.next();
            // Don't get coordinate variables which are in coordinate axes collection
            // (which have a known AxisType).
            if (variable != null && !isVariableACoordinateAxisAndNotACoordinateAxis2D(variable) && !isAUnusedVariable(variable)) {
                productMetaData.getParameterMetaDataMap().put(variable.getFullName(), createParameterMetaData(variable));
            }
        }
    }

}
