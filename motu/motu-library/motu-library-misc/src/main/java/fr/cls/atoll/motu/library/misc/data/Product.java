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
package fr.cls.atoll.motu.library.misc.data;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis2D;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.unidata.geoloc.LatLonRect;
import fr.cls.atoll.motu.library.inventory.Access;
import fr.cls.atoll.motu.library.inventory.DepthCoverage;
import fr.cls.atoll.motu.library.inventory.GeospatialCoverage;
import fr.cls.atoll.motu.library.inventory.Inventory;
import fr.cls.atoll.motu.library.inventory.Resource;
import fr.cls.atoll.motu.library.inventory.TimePeriod;
import fr.cls.atoll.motu.library.misc.exception.MotuExceedingCapacityException;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDateRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidLatLonRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuNoVarException;
import fr.cls.atoll.motu.library.misc.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfAttributeException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfAttributeNotFoundException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfVariableException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfVariableNotFoundException;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.library.misc.metadata.DocMetaData;
import fr.cls.atoll.motu.library.misc.metadata.ParameterMetaData;
import fr.cls.atoll.motu.library.misc.metadata.ProductMetaData;
import fr.cls.atoll.motu.library.misc.netcdf.NetCdfReader;
import fr.cls.atoll.motu.library.misc.netcdf.NetCdfWriter;
import fr.cls.atoll.motu.library.misc.utils.DateUtils;

// CSOFF: MultipleStringLiterals : avoid message in constants declaration and trace log.

/**
 * This class represents a product.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class Product {

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(Product.class);

    /** Time-out in milliseconds for automatic download of the extracted file. */
    private static final int DEFAULT_AUTOMATIC_DOWNLOAD_TIMEOUT = 3000;

    /** Contains variables names of 'gridded' product that are hidden to the user. */
    private static final String[] UNUSED_VARIABLES_GRIDS = new String[] { "LatLonMin", "LatLonStep", "LatLon", };

    /** Contains variables names of 'along track product' product that are hidden to the user. */
    private static final String[] UNUSED_VARIABLES_ATP = new String[] {
            "DeltaT", "Tracks", "NbPoints", "Cycles", "Longitudes", "Latitudes", "BeginDates", "DataIndexes", "GlobalCyclesList", };

    /**
     * Default constructor.
     */
    public Product(boolean casAuthentication) {
        this.casAuthentication = casAuthentication;
    }

    /**
     * Finalize.
     * 
     * @throws MotuException the motu exception
     * 
     * @see java.lang.Object#finalize()
     */

    @Override
    protected void finalize() throws MotuException {
        closeNetCdfReader();
        try {
            super.finalize();
        } catch (Throwable e) {
            throw new MotuException("Error in Product.finalize", e);
        }
    }

    /** The product meta data. */
    private ProductMetaData productMetaData;

    /**
     * Getter of the property <tt>productMetaData</tt>.
     * 
     * @return Returns the productMetaData.
     * 
     * @uml.property name="productMetaData"
     */
    public ProductMetaData getProductMetaData() {
        return productMetaData;
    }

    /**
     * Setter of the property <tt>productMetaData</tt>.
     * 
     * @param productMetaData The productMetaData to set.
     * 
     * @uml.property name="productMetaData"
     */
    public void setProductMetaData(ProductMetaData productMetaData) {
        this.productMetaData = productMetaData;
    }

    /** The dataset. */
    private DatasetBase dataset;

    /**
     * Getter of the property <tt>dataset</tt>.
     * 
     * @return Returns the dataset.
     * 
     * @uml.property name="dataset"
     */
    public DatasetBase getDataset() {
        return this.dataset;
    }

    public void resetDataset() {
        dataset = null;
    }

    /**
     * Setter of the property <tt>dataset</tt>.
     * 
     * @param dataset The dataset to set.
     * 
     * @uml.property name="dataset"
     */
    public void setDataset(DatasetBase dataset) {
        this.dataset = dataset;
    }

    /** Does Service needs CAS authentication to access catalog resources and data. */
    protected boolean casAuthentication = false;

    /**
     * Checks if is cas authentication.
     * 
     * @return true, if is cas authentication
     */
    public boolean isCasAuthentication() {
        return casAuthentication;
    }

    /**
     * Sets the cas authentication.
     * 
     * @param casAuthentication the new cas authentication
     */
    public void setCasAuthentication(boolean casAuthentication) {
        this.casAuthentication = casAuthentication;
    }

    /**
     * Checks for criteria date time.
     * 
     * @return true if datetime criteria have been set, false otherwise.
     */
    public boolean hasCriteriaDateTime() {
        if (dataset == null) {
            return false;
        }
        ExtractCriteriaDatetime extractCriteriaDatetime = dataset.findCriteriaDatetime();
        return extractCriteriaDatetime != null;
    }

    /**
     * Gets the criteria date time.
     * 
     * @return DateTime criteria, null if none.
     */
    public ExtractCriteriaDatetime getCriteriaDateTime() {
        if (dataset == null) {
            return null;
        }
        return dataset.findCriteriaDatetime();
    }

    /**
     * Checks for criteria lat lon.
     * 
     * @return true if Lat/Lon criteria have been set, false otherwise.
     */
    public boolean hasCriteriaLatLon() {
        if (dataset == null) {
            return false;
        }
        ExtractCriteriaLatLon extractCriteriaLatLon = dataset.findCriteriaLatLon();
        return extractCriteriaLatLon != null;
    }

    /**
     * Gets the criteria lat lon.
     * 
     * @return Lat/Lon criteria, null if none.
     */
    public ExtractCriteriaLatLon getCriteriaLatLon() {
        if (dataset == null) {
            return null;
        }
        return dataset.findCriteriaLatLon();
    }

    /**
     * Checks for criteria depth.
     * 
     * @return true if depth criteria have been set, false otherwise.
     */
    public boolean hasCriteriaDepth() {
        if (dataset == null) {
            return false;
        }
        ExtractCriteriaDepth extractCriteriaDepth = dataset.findCriteriaDepth();
        return extractCriteriaDepth != null;
    }

    /**
     * Gets the criteria depth.
     * 
     * @return Depth criteria, null if none.
     */
    public ExtractCriteriaDepth getCriteriaDepth() {
        if (dataset == null) {
            return null;
        }
        return dataset.findCriteriaDepth();
    }

    /**
     * Checks if is product along track.
     * 
     * @return Returns true if product type is an 'along track' product.
     * 
     * @throws MotuException the motu exception
     */
    public boolean isProductAlongTrack() throws MotuException {
        if (productMetaData == null) {
            throw new MotuException("Error in isProductAlongTrack - productMetaData is null");
        }
        return productMetaData.isProductAlongTrack();
    }

    /**
     * Checks if is ftp media.
     * 
     * @return true, if is ftp media
     * 
     * @throws MotuException the motu exception
     */
    public boolean isFtpMedia() throws MotuException {
        if (productMetaData == null) {
            throw new MotuException("Error in isFtpMedia - productMetaData is null");
        }
        return productMetaData.isFtpMedia();
    }

    /**
     * Checks if is product downloadable.
     * 
     * @return Returns true if product type is downloadable. Note that "Along track" product and with
     *         2-dimensional Lat/Lon data are not downloadable in this version.
     * 
     * @throws MotuException the motu exception
     */
    public boolean isProductDownloadable() throws MotuException {
        if (productMetaData == null) {
            throw new MotuException("Error in isProductDownloadable - productMetaData is null");
        }

        // return !(productMetaData.isProductAlongTrack() || hasGeoXYAxisWithLonLatEquivalence());
        return !(productMetaData.isProductAlongTrack());
    }

    /**
     * Checks if is dataset along track.
     * 
     * @return true if dataset instance is a DatasetAlongTrack.
     */
    public boolean isDatasetAlongTrack() {
        return dataset instanceof DatasetAlongTrack;
    }

    /**
     * Checks if is dataset grid.
     * 
     * @return true if dataset instance is a DatasetGrid.
     */
    public boolean isDatasetGrid() {
        return dataset instanceof DatasetGrid;
    }

    /**
     * Reads product metadata from an XML file.
     * 
     * @param url url of the XML file that contains metadata
     */
    public void loadMetaData(String url) {

    }

    /**
     * Constructs product id from location data (last element of the location data).
     */
    public void setProductIdFromLocation() {

        if (productMetaData == null) {
            productMetaData = new ProductMetaData();

            // Get productId
            // first replace all "\" by "/"
            String productId = locationData.replace("\\", "/");
            String[] locationDataSplit = productId.split("/");

            if (locationDataSplit.length > 0) {
                productId = locationDataSplit[locationDataSplit.length - 1];
            }
            String[] pointDataSplit = productId.split("\\.");
            if (pointDataSplit.length > 0) {
                productId = pointDataSplit[0];
            }
            productMetaData.setProductId(productId);
        }

    }

    /**
     * Sets the product id.
     * 
     * @param productId the new product id
     */
    public void setProductId(String productId) {

        if (productMetaData == null) {
            productMetaData = new ProductMetaData();
        }
        productMetaData.setProductId(productId);
    }

    /**
     * Reads product metadata from an URL Opendap dataset.
     * 
     * @param url url of the Opendap dataset
     * 
     * @throws MotuException the motu exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     */
    public void loadOpendapMetaData(String url) throws MotuException, NetCdfAttributeException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("loadOpendapMetaData() - entering");
        }

        setLocationData(url);

        setProductIdFromLocation();

        loadOpendapMetaData();

        if (LOG.isDebugEnabled()) {
            LOG.debug("loadOpendapMetaData() - exiting");
        }
    }

    /**
     * Reads product metadata from a dataset (NetCDF file) from an already loaded Product from the catalog.
     * 
     * @throws MotuException the motu exception
     */
    public void loadOpendapMetaData() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("loadOpendapMetaData() - entering");
        }

        if (locationData.equals("")) {
            throw new MotuException("Error in loadOpendapMetaData - Unable to open NetCdf dataset - url path is not set (is empty)");
        }
        // Loads global metadata from opendap
        loadOpendapGlobalMetaData();

        if (LOG.isDebugEnabled()) {
            LOG.debug("loadOpendapMetaData() - exiting");
        }
    }

    // /**
    // * Reads product metadata from a XML file).
    // *
    // * @throws MotuException the motu exception
    // */
    // public void loadInventoryMetaData() throws MotuException {
    // if (LOG.isDebugEnabled()) {
    // LOG.debug("loadXmlMetaData() - entering");
    // }
    //
    // if (locationMetaData.equals("")) {
    // throw new
    // MotuException("Error in loadInventoryMetaData - Unable to open XML file - url path is not set (is empty)");
    // }
    // // Loads global metadata from opendap
    // loadInventoryGlobalMetaData();
    //
    // if (LOG.isDebugEnabled()) {
    // LOG.debug("loadXmlMetaData() - exiting");
    // }
    // }

    /**
     * Reads product global metadata from an (NetCDF file).
     * 
     * @throws MotuException the motu exception
     */
    public void loadOpendapGlobalMetaData() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("loadOpendapGlobalMetaData() - entering");
        }

        if (productMetaData == null) {
            throw new MotuException("Error in loadOpendapGlobalMetaData - Unable to load - productMetaData is null");
        }

        openNetCdfReader();

        productMetaData.setTitle(getProductId());

        try {
            // Gets global attribute 'title' if not set.
            if (productMetaData.getTitle().equals("")) {
                String title = netCdfReader.getStringValue("title");
                productMetaData.setTitle(title);
            }

        } catch (NetCdfAttributeException e) {
            LOG.error("loadOpendapGlobalMetaData()", e);
            throw new MotuException("Error in loadOpendapGlobalMetaData", e);
        } catch (NetCdfAttributeNotFoundException e) {
            // LOG.error("loadOpendapGlobalMetaData()", e);

            // Do nothing
        }

        // Gets global attribute 'FileType'.
        try {
            // Gets global attribute 'FileType'.
            String fileType = netCdfReader.getStringValue("filetype");
            productMetaData.setProductCategory(fileType);
        } catch (NetCdfAttributeException e) {
            LOG.error("loadOpendapGlobalMetaData()", e);
            throw new MotuException("Error in loadOpendapGlobalMetaData", e);

        } catch (NetCdfAttributeNotFoundException e) {
            // LOG.error("loadOpendapGlobalMetaData()", e);

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

        // Gets variables metadata.
        getOpendapVariableMetadata();

        if (LOG.isDebugEnabled()) {
            LOG.debug("loadOpendapGlobalMetaData() - exiting");
        }
    }
    
    /**
     * Load inventory meta data.
     *
     * @throws MotuException the motu exception
     */
    public void loadInventoryMetaData() throws MotuException {
        loadInventoryMetaData(this.getLocationMetaData());
    }
    
    /**
     * Load inventory meta data.
     *
     * @param xmlUri the xml uri
     * @throws MotuException the motu exception
     */
    public void loadInventoryMetaData(String xmlUri) throws MotuException {
        Inventory inventoryOLA = Organizer.getInventoryOLA(xmlUri);
        loadInventoryMetaData(inventoryOLA);
    }
    
    /**
     * Load inventory meta data.
     *
     * @param inventoryOLA the inventory ola
     * @throws MotuException the motu exception
     */
    public void loadInventoryMetaData(Inventory inventoryOLA) throws MotuException {

        if (inventoryOLA == null) {
            return;            
        }
            
        Resource resource = inventoryOLA.getResource();
        Access access = resource.getAccess();

        if (productMetaData == null) {
            productMetaData = new ProductMetaData();
        }
     
        loadInventoryGlobalMetaData(inventoryOLA);

        URI accessUri = null;
        URI accessUriTemp = null;
        String login = access.getLogin();
        String password = access.getPassword();
        StringBuffer userInfo = null;

        if (password == null) {
            password = "";
        }

        if (!Organizer.isNullOrEmpty(login)) {
            userInfo = new StringBuffer();
            userInfo.append(login);
            userInfo.append(":");
            userInfo.append(password);
        }

        try {
            accessUriTemp = access.getUrlPath();

            if (userInfo != null) {
                accessUri = new URI(accessUriTemp.getScheme(), userInfo.toString(), accessUriTemp.getHost(), accessUriTemp.getPort(), accessUriTemp
                        .getPath(), accessUriTemp.getQuery(), accessUriTemp.getFragment());
            } else {
                accessUri = accessUriTemp;
            }

        } catch (URISyntaxException e) {
            throw new MotuException(String.format("Invalid URI '%s' in inventory product '%s' at '%s.urlPath' tag.attribute", accessUri, productMetaData.getProductId(), access.getClass()
                    .toString()), e);
        }

        setLocationData(accessUri.toString());

        List<DataFile> dataFiles = CatalogData.loadFtpDataFiles(inventoryOLA);

        setDataFiles(dataFiles);

    }
    

    /**
     * Reads product global variable metadata from a NetCDF file.
     * 
     * @param inventoryOLA the inventory ola
     * 
     * @throws MotuException the motu exception
     */

    public void loadInventoryGlobalMetaData(Inventory inventoryOLA) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("loadInventoryGlobalMetaData() - entering");
        }

        if (productMetaData == null) {
            throw new MotuException("Error in loadInventoryGlobalMetaData - Unable to load - productMetaData is null");
        }

        productMetaData.setProductId(inventoryOLA.getResource().getUrn().toString());
        productMetaData.setTitle(Organizer.getDatasetIdFromURI(inventoryOLA.getResource().getUrn().toString()));

        Resource resource = inventoryOLA.getResource();

        TimePeriod timePeriod = resource.getTimePeriod();
        if (timePeriod != null) {
            productMetaData.setTimeCoverage(timePeriod.getStart(), timePeriod.getEnd());
        }

        GeospatialCoverage geospatialCoverage = resource.getGeospatialCoverage();
        if (geospatialCoverage != null) {
            ExtractCriteriaLatLon criteriaLatLon = new ExtractCriteriaLatLon(geospatialCoverage);
            productMetaData.setGeoBBox(new LatLonRect(criteriaLatLon.getLatLonRect()));
        }

        DepthCoverage depthCoverage = resource.getDepthCoverage();
        if (depthCoverage != null) {
            productMetaData.setDepthCoverage(new MinMax(depthCoverage.getMin().getValue().doubleValue(), depthCoverage.getMax().getValue()
                    .doubleValue()));
        }

        // Gets variables metadata.
        fr.cls.atoll.motu.library.inventory.Variables variables = resource.getVariables();
        if (variables != null) {

            for (fr.cls.atoll.motu.library.inventory.Variable variable : variables.getVariable()) {

                ParameterMetaData parameterMetaData = new ParameterMetaData();

                parameterMetaData.setName(variable.getName());
                parameterMetaData.setLabel(variable.getName());
                parameterMetaData.setUnit(variable.getUnits());
                parameterMetaData.setUnitLong(variable.getUnits());
                parameterMetaData.setStandardName(variable.getVocabularyName());

                if (productMetaData.getParameterMetaDatas() == null) {
                    productMetaData.setParameterMetaDatas(new HashMap<String, ParameterMetaData>());
                }
                productMetaData.putParameterMetaDatas(variable.getName(), parameterMetaData);

            }
        }

        // if (productMetaData.getDocumentations() == null) {
        // productMetaData.setDocumentations(new ArrayList<DocMetaData>());
        // }
        // productMetaData.clearDocumentations();
        //        
        // DocMetaData docMetaData = new DocMetaData();
        // docMetaData.setTitle(ProductMetaData.MEDIA_KEY);
        // docMetaData.setResource(CatalogData.CatalogType.FTP.name());
        // productMetaData.addDocumentations(docMetaData);

        if (LOG.isDebugEnabled()) {
            LOG.debug("loadInventoryGlobalMetaData() - exiting");
        }
    }

    /**
     * Sets the media key.
     * 
     * @param value the new media key
     */
    public void setMediaKey(String value) {

        if (productMetaData.getDocumentations() == null) {
            productMetaData.setDocumentations(new ArrayList<DocMetaData>());
        }
        productMetaData.clearDocumentations();

        DocMetaData docMetaData = new DocMetaData();
        docMetaData.setTitle(ProductMetaData.MEDIA_KEY);
        docMetaData.setResource(value);
        productMetaData.addDocumentations(docMetaData);

    }

    /**
     * Gets the opendap variable metadata.
     * 
     * @throws MotuException the motu exception
     */
    @SuppressWarnings("unchecked")
    private void getOpendapVariableMetadata() throws MotuException {
        // Gets variables metadata.
        String unitLong;
        String standardName;
        String longName;

        openNetCdfReader();

        List variables = netCdfReader.getVariables();
        for (Iterator it = variables.iterator(); it.hasNext();) {
            Variable variable = (Variable) it.next();

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
            if (this.isProductAlongTrack()) {
                unusedVariables = UNUSED_VARIABLES_ATP;
            } else {
                unusedVariables = UNUSED_VARIABLES_GRIDS;
            }
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
     * a mapping for the specified variable to extract.
     * 
     * @param varName key whose presence in this map is to be tested.
     * 
     * @return Returns <tt>true</tt> if this product contains a specified variable to be extracted.
     */
    public boolean hasVariableToBeExtracted(String varName) {
        if (dataset == null) {
            return false;
        }
        if (dataset.getVariables() == null) {
            return false;
        }
        return dataset.getVariables().containsKey(varName);
    }

    /**
     * Add variables to the dataset. If dataset doesn't exist, it creates it. If variable already exists in
     * the dataset, it will be replaced.
     * 
     * @param listVar list of variables to be added.
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     */
    public void addVariables(List<String> listVar) throws MotuException, MotuNotImplementedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addVariables() - entering");
        }

        if (dataset == null) {
            createDataset();
        }

        dataset.addVariables(listVar);

        if (LOG.isDebugEnabled()) {
            LOG.debug("addVariables() - exiting");
        }
    }

    /**
     * Updates variables into the dataset. - Adds new variables - Updates the variables which already exist -
     * Remove the variables from the dataset which are not any more in the list If dataset doesn't exist, it
     * creates it.
     * 
     * @param listVar list of variables to be updated.
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     */
    public void updateVariables(List<String> listVar) throws MotuException, MotuNotImplementedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateVariables(List<String>) - start");
        }
        // if list of variables to extract is no set,
        // get all variables form this product
        if (Organizer.isNullOrEmpty(listVar)) {
            listVar = getVariables();
        }

        if (dataset == null) {
            createDataset();
        }

        dataset.updateVariables(listVar);

        if (LOG.isDebugEnabled()) {
            LOG.debug("updateVariables(List<String>) - end");
        }
    }

    /**
     * Update variables.
     * 
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    public List<String> getVariables() throws MotuException, MotuNotImplementedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getVariables() - start");
        }

        List<String> listVar = new ArrayList<String>();

        if (productMetaData == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getVariables() - end - productMetaData is null");
            }
            return listVar;
        }

        Map<String, ParameterMetaData> parameterMetaDatas = productMetaData.getParameterMetaDatas();
        if (parameterMetaDatas == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getVariables() - end - parameterMetaDatas is null");
            }
            return listVar;
        }

        Collection<ParameterMetaData> listParameterMetaData = parameterMetaDatas.values();
        for (ParameterMetaData parameterMetaData : listParameterMetaData) {
            if (Organizer.isNullOrEmpty(parameterMetaData.getName())) {
                continue;
            }
            listVar.add(parameterMetaData.getName());
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getVariables() - end");
        }
        return listVar;
    }

    /**
     * Removes variables from the dataset.
     * 
     * @param listVar list of variables to be removed.
     * 
     * @throws MotuException the motu exception
     */
    public void removeVariables(List<String> listVar) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeVariables() - entering");
        }

        if (dataset == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("removeVariables() - exiting");
            }
            return;
        }

        dataset.removeVariables(listVar);

        if (LOG.isDebugEnabled()) {
            LOG.debug("removeVariables() - exiting");
        }
    }

    /**
     * Removes all variables from the dataset.
     */
    public void clearVariables() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("clearVariables() - entering");
        }

        if (dataset == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("clearVariables() - exiting");
            }
            return;
        }

        dataset.clearVariables();

        if (LOG.isDebugEnabled()) {
            LOG.debug("clearVariables() - exiting");
        }
    }

    /**
     * Updates list of criteria into the dataset. - Adds new criteria - Updates the criteria which already
     * exist - Removes the criteria from the dataset which are not any more in the list If dataset doesn't
     * exist, it creates it.
     * 
     * @param listCriteria list of criteria to be updated.
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     */
    public void updateCriteria(List<ExtractCriteria> listCriteria) throws MotuException, MotuNotImplementedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateCriteria() - entering");
        }

        if (dataset == null) {
            createDataset();
        }

        dataset.updateCriteria(listCriteria);

        if (LOG.isDebugEnabled()) {
            LOG.debug("updateCriteria() - exiting");
        }
    }

    /**
     * Removes all criteria from the dataset.
     */
    public void clearCriteria() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("clearCriteria() - entering");
        }

        if (dataset == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("clearCriteria() - exiting");
            }
            return;
        }

        dataset.clearCriteria();

        if (LOG.isDebugEnabled()) {
            LOG.debug("clearCriteria() - exiting");
        }
    }

    /**
     * Update files.
     * 
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    public void updateFiles() throws MotuException, MotuNotImplementedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateFiles() - entering");
        }

        if (dataset == null) {
            createDataset();
        }

        dataset.updateFiles(dataFiles);

        if (LOG.isDebugEnabled()) {
            LOG.debug("updateFiles() - exiting");
        }
    }

    /**
     * Clear files.
     */
    public void clearFiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("clearFiles() - entering");
        }

        if (dataset == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("clearFiles() - exiting");
            }
            return;
        }

        dataset.clearFiles();

        if (LOG.isDebugEnabled()) {
            LOG.debug("clearFiles() - exiting");
        }
    }

    /**
     * Sets the select data. If dataset doesn't exist, it creates it.
     * 
     * @param selectData to be updated.
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     */
    public void setSelectData(SelectData selectData) throws MotuException, MotuNotImplementedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectData() - entering");
        }

        if (dataset == null) {
            createDataset();
        }

        dataset.setSelectData(selectData);

        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectData() - exiting");
        }
    }

    /**
     * Creates a new dataset.
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     */
    private void createDataset() throws MotuException, MotuNotImplementedException {
        if (productMetaData == null) {
            throw new MotuException("Error in CreateDataset - Unable to create dataset - productMetaData is null");
        }

        if (isFtpMedia()) {
            dataset = new DatasetFtp(this);
        } else if (isProductAlongTrack()) {
            dataset = new DatasetAlongTrack(this);
            throw new MotuException("Extraction of 'Along Track' Product is not yet available.");
        } else if (getNetCdfReader().hasGeoXYAxisWithLonLatEquivalence()) {
            // dataset = new DatasetGridXYLatLon(this);
            dataset = new DatasetGrid(this);
            // throw new
            // MotuNotImplementedException("Dataset grid with 2-dimensional Lat/Lon data is not implemented");
        } else {
            dataset = new DatasetGrid(this);
        }

    }

    /**
     * Gets latitude axis data values.
     * 
     * @return a {@link Array} constains latitude axis data values
     * 
     * @throws MotuException the motu exception
     * @throws NetCdfVariableException the net cdf variable exception
     */
    public Array getLatAxisData() throws MotuException, NetCdfVariableException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLatAxisData() - entering");
        }

        if (productMetaData == null) {
            throw new MotuException("Error in getLatAxisData - productMetaData is null");
        }

        Variable variable = productMetaData.getLatAxis();
        if (variable == null) {
            throw new MotuException(String.format("Error in getLatAxisData - No latitude axis found in this product '%s'", this.getProductId()));
        }

        Array returnArray = readVariable(variable);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLatAxisData() - exiting");
        }
        return returnArray;
    }

    /**
     * Gets longitude axis data values.
     * 
     * @return a {@link Array} constains longitude axis data values
     * 
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuException the motu exception
     */
    public Array getLonAxisData() throws MotuException, NetCdfVariableException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLonAxisData() - entering");
        }

        if (productMetaData == null) {
            throw new MotuException("Error in getLonAxisData - productMetaData is null");
        }

        Variable variable = productMetaData.getLonAxis();
        if (variable == null) {
            throw new MotuException(String.format("Error in getLonAxisData - No longitude axis found in this product '%s'", this.getProductId()));
        }

        Array returnArray = readVariable(variable);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLonAxisData() - exiting");
        }
        return returnArray;
    }

    /**
     * Gets geoX axis data values.
     * 
     * @return a {@link Array} constains geoX axis data values
     * 
     * @throws MotuException the motu exception
     * @throws NetCdfVariableException the net cdf variable exception
     */
    public Array getGeoXAxisData() throws MotuException, NetCdfVariableException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getGeoXAxisData() - entering");
        }

        if (productMetaData == null) {
            throw new MotuException("Error in getGeoXAxisData - productMetaData is null");
        }

        Variable variable = productMetaData.getGeoXAxis();
        if (variable == null) {
            throw new MotuException(String.format("Error in getGeoXAxisData - No geoX axis found in this product '%s'", this.getProductId()));
        }

        Array returnArray = readVariable(variable);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getGeoXAxisData() - exiting");
        }
        return returnArray;
    }

    /**
     * Gets geoY axis data values.
     * 
     * @return a {@link Array} constains geoY axis data values
     * 
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuException the motu exception
     */
    public Array getGeoYAxisData() throws MotuException, NetCdfVariableException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getGeoYAxisData() - entering");
        }

        if (productMetaData == null) {
            throw new MotuException("Error in getGeoYAxisData - productMetaData is null");
        }

        Variable variable = productMetaData.getGeoYAxis();
        if (variable == null) {
            throw new MotuException(String.format("Error in getGeoYAxisData - No geoY axis found in this product '%s'", this.getProductId()));
        }

        Array returnArray = readVariable(variable);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getGeoYAxisData() - exiting");
        }
        return returnArray;
    }

    /**
     * Gets time axis data values.
     * 
     * @return a {@link Array} constains time axis data values
     * 
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuException the motu exception
     */
    public Array getTimeAxisData() throws MotuException, NetCdfVariableException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTimeAxisData() - entering");
        }

        if (productMetaData == null) {
            throw new MotuException("Error in getTimeAxisData - productMetaData is null");
        }

        Variable variable = productMetaData.getTimeAxis();
        if (variable == null) {
            // throw new
            // MotuException(String.format("Error in getTimeAxisData - No time axis found in this product '%s'",
            // this.getProductId()));
            return null;
        }

        Array returnArray = readVariable(variable);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTimeAxisData() - exiting");
        }
        return returnArray;
    }

    /**
     * Gets time axis data values.
     * 
     * @return a list constains time axis date values
     * 
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuException the motu exception if string to date conversion fails
     */
    public List<Date> getTimeAxisDataAsDate() throws MotuException, NetCdfVariableException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTimeAxisDataAsDate() - entering");
        }
        final DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        final Array array = getTimeAxisData();
        final List<Date> list = new LinkedList<Date>();

        double datetime = 0.0;

        for (IndexIterator it = array.getIndexIterator(); it.hasNext();) {
            datetime = it.getDoubleNext();
            String dateString = NetCdfReader.getDateAsGMTString(datetime, productMetaData.getTimeAxis().getUnitsString());
            try {
                list.add(dateFormatter.parse(dateString));
            } catch (ParseException e) {
                throw new MotuException("Failed to parse date '" + dateString + "'. Expected format '" + dateFormatter.toString() + "'");
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getTimeAxisDataAsDate() - exiting");
        }
        return list;
    }

    /**
     * Gets time axis data values.
     * 
     * @return a list constains time axis data values
     * 
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuException the motu exception
     */
    public List<String> getTimeAxisDataAsString() throws MotuException, NetCdfVariableException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTimeAxisDataAsString() - entering");
        }

        List<String> list = new ArrayList<String>();

        Array array = getTimeAxisData();
        if (array == null) {
            return list;
        }

        double datetime = 0.0;

        for (IndexIterator it = array.getIndexIterator(); it.hasNext();) {
            datetime = it.getDoubleNext();
            list.add(NetCdfReader.getDateAsGMTNoZeroTimeString(datetime, productMetaData.getTimeAxis().getUnitsString()));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getTimeAxisDataAsString() - exiting");
        }
        return list;
    }

    /**
     * Gets Z (depth) axis data values.
     * 
     * @return a {@link Array} constains depth axis data values
     * 
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuException the motu exception
     */
    public Array getZAxisData() throws MotuException, NetCdfVariableException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getZAxisData() - entering");
        }

        if (productMetaData == null) {
            throw new MotuException("Error in getZAxisData - productMetaData is null");
        }

        Variable variable = productMetaData.getZAxis();
        if (variable == null) {
            throw new MotuException(String.format("Error in getZAxisData - No Z (depth) axis found in this product '%s'", this.getProductId()));
        }

        Array returnArray = readVariable(variable);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getZAxisData() - exiting");
        }
        return returnArray;

    }

    /**
     * Gets Z (depth) axis data values.
     * 
     * @return a list constains depth axis data values
     * 
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuException the motu exception
     */
    public List<String> getZAxisDataAsString() throws MotuException, NetCdfVariableException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getZAxisDataAsString() - entering");
        }

        List<String> list = new ArrayList<String>();

        Array array = getZAxisData();

        double depth = 0.0;

        for (IndexIterator it = array.getIndexIterator(); it.hasNext();) {
            depth = it.getDoubleNext();
            list.add(NetCdfReader.getStandardZAsString(depth));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getZAxisDataAsString() - exiting");
        }
        return list;

    }

    /**
     * Gets the z axis rounded up data as string.
     * 
     * @param desiredDecimalNumberDigits the desired decimal number digits
     * 
     * @return the z axis rounded up data as string
     * 
     * @throws MotuException the motu exception
     * @throws NetCdfVariableException the net cdf variable exception
     */
    public List<String> getZAxisRoundedUpDataAsString(int desiredDecimalNumberDigits) throws MotuException, NetCdfVariableException {
        return getZAxisDataAsString(RoundingMode.UP, desiredDecimalNumberDigits);
    }

    /**
     * Gets the z axis rounded down data as string.
     * 
     * @param desiredDecimalNumberDigits the desired decimal number digits
     * 
     * @return the z axis rounded down data as string
     * 
     * @throws MotuException the motu exception
     * @throws NetCdfVariableException the net cdf variable exception
     */
    public List<String> getZAxisRoundedDownDataAsString(int desiredDecimalNumberDigits) throws MotuException, NetCdfVariableException {
        return getZAxisDataAsString(RoundingMode.DOWN, desiredDecimalNumberDigits);
    }

    /**
     * Gets the z axis data as string.
     * 
     * @param roundingMode the rounding mode
     * @param desiredDecimalNumberDigits the desired decimal number digits
     * 
     * @return the z axis data as string
     * 
     * @throws MotuException the motu exception
     * @throws NetCdfVariableException the net cdf variable exception
     */
    public List<String> getZAxisDataAsString(RoundingMode roundingMode, int desiredDecimalNumberDigits) throws MotuException, NetCdfVariableException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getZAxisDataAsString() - entering");
        }

        List<String> list = new ArrayList<String>();

        Array array = getZAxisData();

        double depth = 0.0;

        for (IndexIterator it = array.getIndexIterator(); it.hasNext();) {
            depth = it.getDoubleNext();
            list.add(NetCdfReader.getStandardZAsString(depth, roundingMode, desiredDecimalNumberDigits));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getZAxisDataAsString() - exiting");
        }
        return list;

    }

    /**
     * Gets the z axis data as double.
     * 
     * @return the z axis data as string
     * 
     * @throws MotuException the motu exception
     * @throws NetCdfVariableException the net cdf variable exception
     */
    public List<Double> getZAxisDataAsDouble() throws MotuException, NetCdfVariableException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getZAxisDataAsDouble() - entering");
        }

        List<Double> list = new ArrayList<Double>();

        Array array = getZAxisData();

        double depth = 0.0;

        for (IndexIterator it = array.getIndexIterator(); it.hasNext();) {
            depth = it.getDoubleNext();
            list.add(depth);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getZAxisDataAsDouble() - exiting");
        }
        return list;

    }

    /**
     * Checks for geo XY axis with lon lat equivalence.
     * 
     * @return true if axes collection contains GeoX with Longitude equivalence and GeoY with Latitude
     *         equivalenceaxes.
     * 
     * @throws MotuException the motu exception
     */
    public boolean hasGeoXYAxisWithLonLatEquivalence() throws MotuException {
        return (hasGeoXAxisWithLonEquivalence() && hasGeoYAxisWithLatEquivalence());
    }

    /**
     * Checks for geo X axis with lon equivalence.
     * 
     * @return true if GeoX axis exists among coordinate axes and if there is a longitude variable equivalence
     *         (Variable whose name is 'longitude' and with at least two dimensions X/Y).
     * 
     * @throws MotuException the motu exception
     */
    public boolean hasGeoXAxisWithLonEquivalence() throws MotuException {
        return productMetaData.hasGeoXAxisWithLonEquivalence(this.netCdfReader);
    }

    /**
     * Checks for geo Y axis with lat equivalence.
     * 
     * @return true if GeoX axis exists among coordinate axes and if there is a longitude variable equivalence
     *         (Variable whose name is 'longitude' and with at least two dimensions X/Y).
     * 
     * @throws MotuException the motu exception
     */
    public boolean hasGeoYAxisWithLatEquivalence() throws MotuException {
        return productMetaData.hasGeoYAxisWithLatEquivalence(this.netCdfReader);

    }

    /**
     * Gets the geo x axis.
     * 
     * @return the geo x axis
     */
    public CoordinateAxis getGeoXAxis() {
        if (productMetaData == null) {
            return null;
        }

        return productMetaData.getGeoXAxis();
    }

    /**
     * Gets the geo y axis.
     * 
     * @return the geo y axis
     */
    public CoordinateAxis getGeoYAxis() {
        if (productMetaData == null) {
            return null;
        }

        return productMetaData.getGeoYAxis();
    }

    /**
     * Find variable in a NetCdf dataset.
     * <p>
     * If the Variable is a member of an array of Structures, this returns only the variable's data in the
     * first Structure, so that the Array shape is the same as the Variable.
     * 
     * @param varName variable name to search.
     * 
     * @return a ucar.nc2.Variable variable or null if not found.
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuException the motu exception
     */

    public Variable findVariable(String varName) throws MotuException, NetCdfVariableNotFoundException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findVariable() - entering");
        }

        openNetCdfReader();

        Variable variable = getNetCdfReader().getRootVariable(varName);

        if (LOG.isDebugEnabled()) {
            LOG.debug("findVariable() - exiting");
        }
        return variable;
    }

    /**
     * Find longitude ignore case.
     * 
     * @return the parameter meta data
     */
    public ParameterMetaData findLongitudeIgnoreCase() {
        if (productMetaData == null) {
            return null;
        }
        return productMetaData.findLongitudeIgnoreCase();
    }

    /**
     * Find latitude ignore case.
     * 
     * @return the parameter meta data
     */
    public ParameterMetaData findLatitudeIgnoreCase() {
        if (productMetaData == null) {
            return null;
        }
        return productMetaData.findLatitudeIgnoreCase();
    }

    /**
     * Find coordinate axis.
     * 
     * @param axisName the axis name
     * @return the coordinate axis
     */
    public CoordinateAxis findCoordinateAxis(String axisName) {
        if (productMetaData == null) {
            return null;
        }
        return productMetaData.findCoordinateAxis(axisName);
    }

    /**
     * Gets the coordinate axis type.
     * 
     * @param axisName the axis name
     * @return the coordinate axis type
     */
    public AxisType getCoordinateAxisType(String axisName) {
        if (productMetaData == null) {
            return null;
        }
        return productMetaData.getCoordinateAxisType(axisName);
    }

    /**
     * Reads all the data for the variable and returns a memory resident Array. The Array has the same element
     * type and shape as the Variable.
     * <p>
     * If the Variable is a member of an array of Structures, this returns only the variable's data in the
     * first Structure, so that the Array shape is the same as the Variable.
     * 
     * @param varName variable name.
     * 
     * @return a ucar.ma2.Array with data for the variable.
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuException the motu exception
     */

    public Array readVariable(String varName) throws MotuException, NetCdfVariableException, NetCdfVariableNotFoundException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("readVariable() - entering");
        }

        openNetCdfReader();

        Variable variable = findVariable(varName);

        Array returnArray = readVariable(variable);
        if (LOG.isDebugEnabled()) {
            LOG.debug("readVariable() - exiting");
        }
        return returnArray;
    }

    /**
     * Reads all the data for the variable and returns a memory resident Array. The Array has the same element
     * type and shape as the Variable.
     * <p>
     * If the Variable is a member of an array of Structures, this returns only the variable's data in the
     * first Structure, so that the Array shape is the same as the Variable.
     * 
     * @param variable variable to be read.
     * 
     * @return a ucar.ma2.Array with data for the variable.
     * 
     * @throws MotuException the motu exception
     * @throws NetCdfVariableException the net cdf variable exception
     */

    public Array readVariable(Variable variable) throws MotuException, NetCdfVariableException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("readVariable() - entering");
        }

        openNetCdfReader();

        // Loads global metadata from opendap
        Array array = netCdfReader.getGrid(variable);
        // Array array = null;
        // try {
        // openNetCdfReader();
        // // Loads global metadata from opendap
        // array = netCdfReader.getGrid(variable);
        // } finally {
        // netCdfReader.close();
        // }

        if (LOG.isDebugEnabled()) {
            LOG.debug("readVariable() - exiting");
        }
        return array;

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
    public void computeAmountDataSize() throws MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException,
            MotuNotImplementedException, MotuInvalidDepthRangeException, MotuInvalidLatLonRangeException, NetCdfVariableException,
            MotuNoVarException, NetCdfVariableNotFoundException {

        if (dataset == null) {
            throw new MotuException("Error in getAmountDataSize - Nothing to get - dataset is null");
        }

        dataset.computeAmountDataSize();

    }

    /**
     * Gets the amount data size.
     * 
     * @return the amount data size
     */
    public double getAmountDataSize() {
        if (dataset == null) {
            return -1d;
        }
        return dataset.getAmountDataSize();
    }

    /**
     * Gets the amount data size as bytes.
     * 
     * @return the amount data size as bytes
     */
    public double getAmountDataSizeAsBytes() {
        return getAmountDataSizeAsKBytes() * 1024d;
    }

    /**
     * Gets the amount data size as Kilo-bytes.
     * 
     * @return the amount data size as Kilo-bytes
     */
    public double getAmountDataSizeAsKBytes() {
        return getAmountDataSize() * 1024d;
    }

    /**
     * Gets the amount data size as Mega-bytes.
     * 
     * @return the amount data size as Mega-bytes
     */
    public double getAmountDataSizeAsMBytes() {
        return getAmountDataSize();
    }

    /**
     * Extract data.
     * 
     * @param dataOutputFormat data output format (NetCdf, HDF, Ascii, ...).
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
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void extractData(Organizer.Format dataOutputFormat) throws MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException,
            MotuNotImplementedException, MotuInvalidDepthRangeException, MotuInvalidLatLonRangeException, NetCdfVariableException,
            MotuNoVarException, NetCdfVariableNotFoundException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractData() - entering");
        }

        if (dataset == null) {
            throw new MotuException("Error in extractData - Nothing to extract - dataset is null");
        }

        dataset.extractData(dataOutputFormat);

        if (LOG.isDebugEnabled()) {
            LOG.debug("extractData() - exiting");
        }
    }

    /**
     * Extract ftp data.
     * 
     * @param dataOutputFormat the data output format
     */
    public void extractFtpData(Organizer.Format dataOutputFormat) {

    }

    /**
     * Gets the min. value of a variable data. First search the min. value in 'valid_min' attribute of the
     * variable, if attribute doesn't exist, calculate the min. value from variable data.
     * 
     * @param variable whose min. value has to be calculated
     * 
     * @return the min value of the variable data
     * 
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuException the motu exception
     */
    public double getMinValue(Variable variable) throws MotuException, NetCdfVariableException {
        MAMath.MinMax minMax = getMinMaxValue(variable);
        return minMax.min;
    }

    /**
     * Gets the min. value of a variable data. First search the min. value in 'valid_min' attribute of the
     * variable, if attribute doesn't exist, calculate the min. value from variable data.
     * 
     * @param variable whose min. value has to be calculated
     * 
     * @return the min value of the variable data
     * 
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuException the motu exception
     */
    public double getMaxValue(Variable variable) throws MotuException, NetCdfVariableException {
        MAMath.MinMax minMax = getMinMaxValue(variable);
        return minMax.max;

    }

    /**
     * Gets the min. and max. values of a variable data. First search the min. value in 'valid_min' attribute
     * and the min. value in 'valid_max' attribute of the variable, if attribute doesn't exist, calculate the
     * min. value and the max. value from variable data.
     * 
     * @param variable whose min. and max. values have to be calculated
     * 
     * @return the min/max value of the variable data
     * 
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuException the motu exception
     */
    public MAMath.MinMax getMinMaxValue(Variable variable) throws MotuException, NetCdfVariableException {
        boolean attrFound = true;
        MAMath.MinMax minMax = new MAMath.MinMax(Double.MIN_VALUE, Double.MAX_VALUE);
        if (variable == null) {
            return minMax;
        }

        try {
            Attribute minAttr = NetCdfReader.getAttribute(variable, "valid_min");
            Attribute maxAttr = NetCdfReader.getAttribute(variable, "valid_max");

            minMax.min = minAttr.getNumericValue().doubleValue();
            minMax.max = maxAttr.getNumericValue().doubleValue();
        } catch (NetCdfAttributeNotFoundException e) {
            // valid_min not found Do Nothing and continue to search min value in array data
            attrFound = false;
        }

        if (attrFound) {
            return minMax;
        }
        Array data = readVariable(variable);
        minMax = MAMath.getMinMax(data);
        return minMax;

    }

    /**
     * Reads a variable. Creates a variable, read it via the dataset. The dataset stores it in its the
     * variable collection.
     * 
     * @param varName name of the variable (parameter) to be read.
     */

    public void readData(String varName) {

    }

    /**
     * Writes data contained in the variable dataset collection into an output file.
     * 
     * @param format output format (NetCDF, HDF5, Ascii).
     * @param output URL of the output file.
     */
    public void writeData(String output, String format) {

    }

    /** The tds service type. */
    private String tdsServiceType = CatalogData.TDS_OPENDAP_SERVICE;

    /**
     * Gets the tds service type.
     * 
     * @return the tds service type
     */
    public String getTdsServiceType() {
        return this.tdsServiceType;
    }

    /**
     * Sets the tds service type.
     * 
     * @param tdsServiceType the tds service type
     */
    public void setTdsServiceType(String tdsServiceType) {
        this.tdsServiceType = tdsServiceType;
    }

    /** The data files. */
    List<DataFile> dataFiles = null;

    /**
     * Gets the data files.
     * 
     * @return the data files
     */
    public List<DataFile> getDataFiles() {
        return dataFiles;
    }

    /**
     * Sets the data files.
     * 
     * @param dataFiles the new data files
     */
    public void setDataFiles(List<DataFile> dataFiles) {
        this.dataFiles = dataFiles;
    }

    public List<String> getTimeCoverageFromDataFiles() {

        List<String> timeCoverage = new ArrayList<String>();

        if (dataFiles == null) {
            return timeCoverage;
        }

        for (DataFile dataFile : dataFiles) {            
            // Warning : get Datetime as UTC 
            DateTime fileStart = DateUtils.dateTimeToUTC(dataFile.getStartCoverageDate());
            
            if (fileStart != null) {
                timeCoverage.add(DateUtils.DATETIME_FORMATTERS.get(DateUtils.DATETIME_PATTERN3).print(fileStart));
            }

        }
        
        return timeCoverage;
        
    }

    /** URL to find the product (URL Opendap , ...). */
    private String locationData = "";

    /**
     * Getter of the property <tt>location</tt>.
     * 
     * @return Returns the location.
     * 
     * @uml.property name="locationData"
     */
    public String getLocationData() {
        return this.locationData;
    }

    /**
     * Setter of the property <tt>location</tt>.
     * 
     * @param locationData The location to set.
     * 
     * @uml.property name="locationData"
     */
    public void setLocationData(String locationData) {
        this.locationData = locationData;
    }

    /**
     * URL of a XML file that describes product's metatada. If there is no XML file, product's metadata will
     * be loaded from netCDF file (dataset).
     */
    private String locationMetaData = "";

    /**
     * Getter of the property <tt>locationMetaData</tt>.
     * 
     * @return Returns the locationMetaData.
     * 
     * @uml.property name="locationMetaData"
     */
    public String getLocationMetaData() {
        return this.locationMetaData;
    }

    /**
     * Setter of the property <tt>locationMetaData</tt>.
     * 
     * @param locationMetaData The locationMetaData to set.
     * 
     * @uml.property name="locationMetaData"
     */
    public void setLocationMetaData(String locationMetaData) {
        this.locationMetaData = locationMetaData;
    }

    /** NetCdfReader object. */
    private NetCdfReader netCdfReader = null;

    /**
     * Getter of the property <tt>netCdfReader</tt>.
     * 
     * @return Returns the netCdfReader.
     * 
     * @uml.property name="netCdfReader"
     */
    public NetCdfReader getNetCdfReader() {
        if (netCdfReader == null) {
            netCdfReader = new NetCdfReader(locationData, this.casAuthentication);
        }
        return this.netCdfReader;
    }

    /**
     * Setter of the property <tt>netCdfReader</tt>.
     * 
     * @param netCdfReader The netCdfReader to set.
     * 
     * @uml.property name="netCdfReader"
     */
    public void setNetCdfReader(NetCdfReader netCdfReader) {
        this.netCdfReader = netCdfReader;
    }

    /**
     * Closes the netCdfReader objet.
     * 
     * @throws MotuException the motu exception
     */
    public void closeNetCdfReader() throws MotuException {
        if (netCdfReader == null) {
            return;
        }
        netCdfReader.close();
    }

    /**
     * Opens the netCdfReader objet.
     * 
     * @throws MotuException the motu exception
     */
    public void openNetCdfReader() throws MotuException {
        openNetCdfReader(true);
    }

    /**
     * Open net cdf reader.
     * 
     * @param enhanceVar the enhance var
     * 
     * @throws MotuException the motu exception
     */
    public void openNetCdfReader(boolean enhanceVar) throws MotuException {
        getNetCdfReader().open(enhanceVar);
    }

    /**
     * Gets the net cdf reader dataset.
     * 
     * @return the NetCdfDataset object of the NetCdfReader object.
     */
    public NetcdfDataset getNetCdfReaderDataset() {
        if (getNetCdfReader() == null) {
            return null;
        }
        return getNetCdfReader().getNetcdfDataset();
    }

    /**
     * Returns product id.
     * 
     * @return Returns the product id.
     */
    public String getProductId() {
        if (productMetaData != null) {
            return productMetaData.getProductId();
        } else {
            return "Unknown_product_Id";
        }
    }

    
    /**
     * Gets the product id encoded.
     *
     * @return the product id encoded
     */
    public String getProductIdEncoded() {
        return getProductIdEncoded("UTF-8");
    }

    /**
     * Gets the product id encoded.
     *
     * @param enc the enc
     * @return the product id encoded
     */
    public String getProductIdEncoded(String enc) {
        if (productMetaData != null) {
            return productMetaData.getProductIdEncoded();
        } else {
            return "Unknown_product_Id";
        }
    }

    /**
     * Gets the tds url path.
     * 
     * @return the tds url path
     */
    public String getTdsUrlPath() {
        if (productMetaData != null) {
            return productMetaData.getTdsUrlPath();
        } else {
            return "Unknown_tds_url_path";
        }
    }

    /** The output location path and file name. */
    private String extractFilename = "";

    /**
     * Getter of the property <tt>extractFilename</tt>.
     * 
     * @return Returns the extractFilename.
     * 
     * @uml.property name="extractFilename"
     */
    public String getExtractFilename() {
        return this.extractFilename;
    }

    /**
     * Setter of the property <tt>extractFilename</tt>.
     * 
     * @param extractFilename The extractFilename to set.
     * 
     * @uml.property name="extractFilename"
     */
    public void setExtractFilename(String extractFilename) {
        this.extractFilename = extractFilename;
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.extractFilename);
        stringBuffer.append(NetCdfWriter.NETCDF_FILE_EXTENSION_EXTRACT);
        this.extractFilenameTemp = stringBuffer.toString();
    }

    /** The temporary output location path and file name. */
    private String extractFilenameTemp = "";

    /**
     * Getter of the property <tt>extractFilenameTemp</tt>.
     * 
     * @return Returns the extractFilenameTemp.
     * 
     * @uml.property name="extractFilenameTemp"
     */
    public String getExtractFilenameTemp() {
        return this.extractFilenameTemp;
    }

    /**
     * Setter of the property <tt>extractFilenameTemp</tt>.
     * 
     * @param extractFilenameTemp The extractFilenameTemp to set.
     * 
     * @uml.property name="extractFilenameTemp"
     */
    public void setExtractFilenameTemp(String extractFilenameTemp) {
        this.extractFilenameTemp = extractFilenameTemp;
    }

    /**
     * Clears <tt>extractFilename</tt>.
     * 
     * @uml.property name="extractFilename"
     */
    public void clearExtractFilename() {
        this.extractFilename = "";
        this.extractFilenameTemp = "";
    }

    /** Last error encountered. */
    private String lastError = "";

    /**
     * Getter of the property <tt>lastError</tt>.
     * 
     * @return Returns the lastError.
     * 
     * @uml.property name="lastError"
     */
    public String getLastError() {
        return this.lastError;
    }

    /**
     * Setter of the property <tt>lastError</tt>.
     * 
     * @param lastError The lastError to set.
     * 
     * @uml.property name="lastError"
     */
    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    /** Time-out in milliseconds for automatic download of the extracted file. */
    private int autoDownloadTimeOut = DEFAULT_AUTOMATIC_DOWNLOAD_TIMEOUT;

    /**
     * Getter of the property <tt>autoDownloadTimeOut</tt>.
     * 
     * @return Returns the autoDownloadTimeOut.
     * 
     * @uml.property name="autoDownloadTimeOut"
     */
    public int getAutoDownloadTimeOut() {
        return this.autoDownloadTimeOut;
    }

    /**
     * Setter of the property <tt>autoDownloadTimeOut</tt>. Sets to zero, to disable automatic download.
     * 
     * @param autoDownloadTimeOut The autoDownloadTimeOut to set.
     * 
     * @uml.property name="autoDownloadTimeOut"
     */
    public void setAutoDownloadTimeOut(int autoDownloadTimeOut) {
        this.autoDownloadTimeOut = autoDownloadTimeOut;
    }

    /**
     * Sets the automatic download time-out of the default value (DEFAULT_AUTOMATIC_DOWNLOAD_TIMEOUT
     * constant).
     */
    public void setDefaultAutoDownloadTimeOut() {
        this.autoDownloadTimeOut = DEFAULT_AUTOMATIC_DOWNLOAD_TIMEOUT;
    }

    /**
     * Checks if is auto download time out enable.
     * 
     * @return true if the automatic download time-out is enable (> 0).
     */
    public boolean isAutoDownloadTimeOutEnable() {
        return this.autoDownloadTimeOut > 0;
    }

    /**
     * Clears <tt>lastError</tt>.
     * 
     * @uml.property name="lastError"
     */
    public void clearLastError() {
        this.lastError = "";
    }

    /**
     * Checks for last error.
     * 
     * @return true last error message string is not empty, false otherwise.
     */
    public boolean hasLastError() {

        return getLastError().length() > 0;
    }

    /**
     * Gets the output full file name (with path).
     * 
     * @return the output full file name (with path).
     * 
     * @throws MotuException the motu exception
     */
    public String getExtractLocationData() throws MotuException {
        return Product.getExtractLocationData(extractFilename);
    }

    /**
     * Gets the extract location data.
     * 
     * @param fileName the file name
     * 
     * @return the extract location data
     * 
     * @throws MotuException the motu exception
     */
    public static String getExtractLocationData(String fileName) throws MotuException {

        if (fileName.length() <= 0) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(Product.getExtractionPath());
        stringBuffer.append(fileName);

        return stringBuffer.toString();
    }

    /**
     * Gets the output temporary full file name (with path).
     * 
     * @return the output temporary full file name (with path).
     * 
     * @throws MotuException the motu exception
     */
    public String getExtractLocationDataTemp() throws MotuException {

        if (extractFilenameTemp.length() <= 0) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(Product.getExtractionPath());
        stringBuffer.append(extractFilenameTemp);

        return stringBuffer.toString();
    }

    /**
     * Move temp extract file to final extract.
     * 
     * @throws MotuException the motu exception
     */
    public void moveTempExtractFileToFinalExtractFile() throws MotuException {
        // Temporary File
        String locationTmp = getExtractLocationDataTemp();
        File fileTemp = new File(locationTmp);

        // Final File
        String locationFinal = getExtractLocationData();
        File fileFinal = new File(locationFinal);

        // Rename file
        boolean success = fileTemp.renameTo(fileFinal);
        if (!success) {
            throw new MotuException(String.format("Unable to rename file '%s' to file '%s'.", locationTmp, locationFinal));
        }
    }

    /**
     * Gets the extraction path.
     * 
     * @return the extraction path
     * 
     * @throws MotuException the motu exception
     */
    public static String getExtractionPath() throws MotuException {

        StringBuffer stringBuffer = new StringBuffer();

        String dir = Organizer.getMotuConfigInstance().getExtractionPath();
        stringBuffer.append(dir);

        if (!(dir.endsWith("/") || dir.endsWith("\\"))) {
            stringBuffer.append("/");
        }
        return stringBuffer.toString();
    }

    /**
     * Gets the document root oh the Http server.
     * 
     * @return the document root oh the Http server.
     * 
     * @throws MotuException the motu exception
     */
    public String getHpptServerDocumentRoot() throws MotuException {
        return Organizer.getMotuConfigInstance().getHttpDocumentRoot();
    }

    /**
     * Gets the url to download the output file.
     * 
     * @return the url to downolad the output file
     * 
     * @throws MotuException the motu exception
     */
    public String getDownloadUrlPath() throws MotuException {
        return getDownloadUrlPath(extractFilename);
    }

    /**
     * Gets the download url path.
     * 
     * @param fileName the file name
     * 
     * @return the download url path
     * 
     * @throws MotuException the motu exception
     */
    public static String getDownloadUrlPath(String fileName) throws MotuException {

        if (fileName.length() <= 0) {
            return "";
        }

        StringBuffer stringBuffer = new StringBuffer();

        String dir = Organizer.getMotuConfigInstance().getDownloadHttpUrl();
        stringBuffer.append(dir);

        if (!(dir.endsWith("/") || dir.endsWith("\\"))) {
            stringBuffer.append("/");
        }
        stringBuffer.append(fileName);

        return stringBuffer.toString();
    }

    /**
     * Checks for download url path.
     * 
     * @return true the url of the output file to download is not empty, false otherwise.
     * 
     * @throws MotuException the motu exception
     */
    public boolean hasDownloadUrlPath() throws MotuException {

        return getDownloadUrlPath().length() > 0;
    }

}
// CSON: MultipleStringLiterals
