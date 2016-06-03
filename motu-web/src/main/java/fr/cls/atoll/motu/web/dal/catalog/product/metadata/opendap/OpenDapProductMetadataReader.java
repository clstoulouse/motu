package fr.cls.atoll.motu.web.dal.catalog.product.metadata.opendap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuExceptionBase;
import fr.cls.atoll.motu.web.bll.exception.NetCdfAttributeException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfAttributeNotFoundException;
import fr.cls.atoll.motu.web.dal.request.netcdf.NetCdfReader;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ParameterMetaData;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ProductMetaData;
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

    /** NetCdfReader object. */
    private NetCdfReader netCdfReader = null;
    private String productId;
    private ProductMetaData productMetaData;

    public OpenDapProductMetadataReader(String productId_, String locationData, boolean useSSO) {
        netCdfReader = new NetCdfReader(locationData, useSSO);
        productId = productId_;
        productMetaData = new ProductMetaData();
    }

    /**
     * Valeur de productId.
     * 
     * @return la valeur.
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Reads product global metadata from an (NetCDF file).
     * 
     * @throws MotuException the motu exception
     */
    public ProductMetaData loadMetaData() throws MotuException {
        productMetaData.setProductId(productId);

        netCdfReader.open(true);

        productMetaData.setTitle(getProductId());

        try {
            // Gets global attribute 'title' if not set.
            if (productMetaData.getTitle().equals("")) {
                String title = netCdfReader.getStringValue("title");
                productMetaData.setTitle(title);
            }

        } catch (Exception e) {
            throw new MotuException("Error in loadOpendapGlobalMetaData", e);
        }

        // Gets global attribute 'FileType'.
        try {
            // Gets global attribute 'FileType'.
            String fileType = netCdfReader.getStringValue("filetype");
            productMetaData.setProductCategory(fileType);
        } catch (NetCdfAttributeException e) {
            throw new MotuException("Error in loadOpendapGlobalMetaData", e);
        } catch (NetCdfAttributeNotFoundException e) {
            // Do nothing
        }

        // Gets coordinate axes metadata.
        List<CoordinateAxis> coordinateAxes = netCdfReader.getCoordinateAxes();

        if (productMetaData.getCoordinateAxes() == null) {
            productMetaData.setCoordinateAxes(new HashMap<AxisType, CoordinateAxis>());
        }

        for (Iterator<CoordinateAxis> it = coordinateAxes.iterator(); it.hasNext();) {
            CoordinateAxis coordinateAxis = it.next();
            AxisType axisType = coordinateAxis.getAxisType();
            if (axisType != null) {
                productMetaData.putCoordinateAxes(axisType, coordinateAxis);
            }
        }

        if (productMetaData.hasTimeAxis()) {
            productMetaData.setTimeCoverage(productMetaData.getTimeAxisMinValue(), productMetaData.getTimeAxisMaxValue());
        }

        getOpendapVariableMetadata();

        return productMetaData;
    }

    /**
     * Gets the opendap variable metadata.
     *
     * @return the opendap variable metadata
     * @throws MotuException the motu exception
     */
    private void getOpendapVariableMetadata() throws MotuException {
        // Gets variables metadata.
        String unitLong;
        String standardName;
        String longName;

        // openNetCdfReader();

        List<Variable> variables = netCdfReader.getVariables();
        for (Iterator<Variable> it = variables.iterator(); it.hasNext();) {
            Variable variable = it.next();

            // Don't get coordinate variables which are in coordinate axes collection
            // (which have a known AxisType).
            if (variable instanceof CoordinateAxis) {
                CoordinateAxis coordinateAxis = (CoordinateAxis) variable;
                if (coordinateAxis.getAxisType() != null) {
                    if (!(coordinateAxis instanceof CoordinateAxis2D)) {
                        continue;
                    }
                }
            }
            // Don't get cached variables
            // if (variable.isCaching()) {
            // continue;
            // }

            boolean isUnusedVar = false;
            String[] unusedVariables = null;
            // if (this.isProductAlongTrack()) {
            // unusedVariables = UNUSED_VARIABLES_ATP;
            // } else {
            unusedVariables = UNUSED_VARIABLES_GRIDS;
            // }
            for (String unused : unusedVariables) {
                if (variable.getName().equalsIgnoreCase(unused)) {
                    isUnusedVar = true;
                    // try {
                    // Array grid = readVariable(variable);
                    // int rank = grid.getRank();
                    // int[] shape = grid.getShape();
                    // long size = grid.getSize();
                    // System.out.println(rank);
                    // System.out.println(shape);
                    // System.out.println(size);
                    // } catch (MotuExceptionBase e) {
                    // System.out.println(e.notifyException());
                    // }
                    break;
                }
            }

            if (isUnusedVar) {
                continue;
            }

            ParameterMetaData parameterMetaData = new ParameterMetaData();

            parameterMetaData.setName(variable.getName());
            parameterMetaData.setLabel(variable.getDescription());
            parameterMetaData.setUnit(variable.getUnitsString());
            parameterMetaData.setDimensions(variable.getDimensions());

            unitLong = "";
            try {
                unitLong = NetCdfReader.getStringValue(variable, NetCdfReader.VARIABLEATTRIBUTE_UNIT_LONG);
                parameterMetaData.setUnitLong(unitLong);
            } catch (MotuExceptionBase e) {
                parameterMetaData.setUnitLong(unitLong);
            }
            standardName = "";
            try {
                standardName = NetCdfReader.getStringValue(variable, NetCdfReader.VARIABLEATTRIBUTE_STANDARD_NAME);
                parameterMetaData.setStandardName(standardName);
            } catch (MotuExceptionBase e) {
                parameterMetaData.setStandardName(standardName);
            }
            longName = "";
            try {
                longName = NetCdfReader.getStringValue(variable, NetCdfReader.VARIABLEATTRIBUTE_LONG_NAME);
                parameterMetaData.setLongName(longName);
            } catch (MotuExceptionBase e) {
                parameterMetaData.setLongName(longName);
            }

            if (productMetaData.getParameterMetaDatas() == null) {
                productMetaData.setParameterMetaDatas(new HashMap<String, ParameterMetaData>());
            }
            productMetaData.putParameterMetaDatas(variable.getName(), parameterMetaData);
        }
    }

    /**
     * Valeur de productMetaData.
     * 
     * @return la valeur.
     */
    public ProductMetaData getProductMetaData() {
        return productMetaData;
    }

}
