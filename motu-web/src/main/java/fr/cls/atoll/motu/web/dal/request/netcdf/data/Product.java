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

import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuExceptionBase;
import fr.cls.atoll.motu.web.bll.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfAttributeException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfAttributeNotFoundException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfVariableException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfVariableNotFoundException;
import fr.cls.atoll.motu.web.bll.request.model.metadata.DocMetaData;
import fr.cls.atoll.motu.web.common.format.OutputFormat;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.dal.request.netcdf.NetCdfReader;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ParameterMetaData;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ProductMetaData;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.ma2.MAMath;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis2D;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * This class represents a product.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class Product implements Comparator<Product> {

    /** Logger for this class. */
    private static final Logger LOG = LogManager.getLogger();

    /** Contains variables names of 'gridded' product that are hidden to the user. */
    private static final String[] UNUSED_VARIABLES_GRIDS = new String[] { "LatLonMin", "LatLonStep", "LatLon", };

    /** Contains variables names of 'along track product' product that are hidden to the user. */
    private static final String[] UNUSED_VARIABLES_ATP = new String[] {
            "DeltaT", "Tracks", "NbPoints", "Cycles", "Longitudes", "Latitudes", "BeginDates", "DataIndexes", "GlobalCyclesList", };

    /** The data files. */
    private List<DataFile> dataFiles = null;

    /** URL to find the product (URL NetcdfSubsetService NCSS , ...). */
    private String locationDataNCSS = "";

    /** URL to find the product (URL Opendap , ...). */
    private String locationData = "";

    /**
     * URL of a XML file that describes product's metadata. If there is no XML file, product's metadata will
     * be loaded from netCDF file (dataset).
     */
    private String locationMetaData = "";

    /** The tds service type. */
    private String tdsServiceType;

    /** NetCdfReader object. */
    private NetCdfReader netCdfReader = null;

    /** The product meta data. */
    private ProductMetaData productMetaData;

    /**
     * Default constructor.
     *
     * @param casAuthentication the cas authentication
     */

    public Product() {
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
            throw new MotuException(ErrorType.SYSTEM, "Error in Product.finalize", e);
        }
    }

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

    /**
     * Checks if is product along track.
     * 
     * @return Returns true if product type is an 'along track' product.
     * 
     * @throws MotuException the motu exception
     */
    public boolean isProductAlongTrack() throws MotuException {
        if (productMetaData == null) {
            throw new MotuException(ErrorType.SYSTEM, "Error in isProductAlongTrack - productMetaData is null");
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
    public boolean isFtpMedia() {
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
            throw new MotuException(ErrorType.SYSTEM, "Error in isProductDownloadable - productMetaData is null");
        }

        // return !(productMetaData.isProductAlongTrack() || hasGeoXYAxisWithLonLatEquivalence());
        return !(productMetaData.isProductAlongTrack());
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
            throw new MotuException(
                    ErrorType.NETCDF_LOADING,
                    "Error in loadOpendapMetaData - Unable to open NetCdf dataset - url path is not set (is empty)");
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
    // MotuException("Error in loadInventoryMetaData - Unable to open XML file - url path is not set (is
    // empty)");
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
            throw new MotuException(ErrorType.SYSTEM, "Error in loadOpendapGlobalMetaData - Unable to load - productMetaData is null");
        }

        openNetCdfReader();

        productMetaData.setTitle(getProductId());

        try {
            // Gets global attribute 'title' if not set.
            if (productMetaData.getTitle().equals("")) {
                String title = getNetCdfReader().getStringValue("title");
                productMetaData.setTitle(title);
            }

        } catch (NetCdfAttributeException e) {
            LOG.error("loadOpendapGlobalMetaData()", e);
            throw new MotuException(ErrorType.LOADING_CATALOG, "Error in loadOpendapGlobalMetaData", e);
        } catch (NetCdfAttributeNotFoundException e) {
            // LOG.error("loadOpendapGlobalMetaData()", e);

            // Do nothing
        }

        // Gets global attribute 'FileType'.
        try {
            // Gets global attribute 'FileType'.
            String fileType = getNetCdfReader().getStringValue("filetype");
            productMetaData.setProductCategory(fileType);
        } catch (NetCdfAttributeException e) {
            LOG.error("loadOpendapGlobalMetaData()", e);
            throw new MotuException(ErrorType.LOADING_CATALOG, "Error in loadOpendapGlobalMetaData", e);

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

    // /**
    // * Load inventory meta data.
    // *
    // * @throws MotuException the motu exception
    // */
    // public void loadInventoryMetaData() throws MotuException {
    // loadInventoryMetaData(this.getLocationMetaData());
    // }
    //
    // /**
    // * Load inventory meta data.
    // *
    // * @param xmlUri the xml uri
    // * @throws MotuException the motu exception
    // */
    // public void loadInventoryMetaData(String xmlUri) throws MotuException {
    // Inventory inventoryOLA = Organizer.getInventoryOLA(xmlUri);
    // loadInventoryMetaData(inventoryOLA);
    // }

    // /**
    // * Load inventory meta data.
    // *
    // * @param inventoryOLA the inventory ola
    // * @throws MotuException the motu exception
    // */
    // public void loadInventoryMetaData(Inventory inventoryOLA) throws MotuException {
    //
    // if (inventoryOLA == null) {
    // return;
    // }
    //
    // Resource resource = inventoryOLA.getResource();
    // Access access = resource.getAccess();
    //
    // if (productMetaData == null) {
    // productMetaData = new ProductMetaData();
    // }
    //
    // loadInventoryGlobalMetaData(inventoryOLA);
    //
    // URI accessUri = null;
    // URI accessUriTemp = null;
    // String login = access.getLogin();
    // String password = access.getPassword();
    // StringBuffer userInfo = null;
    //
    // if (password == null) {
    // password = "";
    // }
    //
    // if (!Organizer.isNullOrEmpty(login)) {
    // userInfo = new StringBuffer();
    // userInfo.append(login);
    // userInfo.append(":");
    // userInfo.append(password);
    // }
    //
    // try {
    // accessUriTemp = access.getUrlPath();
    //
    // if (userInfo != null) {
    // accessUri = new URI(
    // accessUriTemp.getScheme(),
    // userInfo.toString(),
    // accessUriTemp.getHost(),
    // accessUriTemp.getPort(),
    // accessUriTemp.getPath(),
    // accessUriTemp.getQuery(),
    // accessUriTemp.getFragment());
    // } else {
    // accessUri = accessUriTemp;
    // }
    //
    // } catch (URISyntaxException e) {
    // throw new MotuException(
    // String.format("Invalid URI '%s' in inventory product '%s' at '%s.urlPath' tag.attribute",
    // accessUri,
    // productMetaData.getProductId(),
    // access.getClass().toString()),
    // e);
    // }
    //
    // setLocationData(accessUri.toString());
    //
    // List<DataFile> dataFiles = CatalogData.loadFtpDataFiles(inventoryOLA);
    //
    // setDataFiles(dataFiles);
    //
    // }
    //
    // /**
    // * Reads product global variable metadata from a NetCDF file.
    // *
    // * @param inventoryOLA the inventory ola
    // *
    // * @throws MotuException the motu exception
    // */
    //
    // public void loadInventoryGlobalMetaData(Inventory inventoryOLA) throws MotuException {
    // if (LOG.isDebugEnabled()) {
    // LOG.debug("loadInventoryGlobalMetaData() - entering");
    // }
    //
    // if (productMetaData == null) {
    // throw new MotuException("Error in loadInventoryGlobalMetaData - Unable to load - productMetaData is
    // null");
    // }
    //
    // productMetaData.setProductId(inventoryOLA.getResource().getUrn().toString());
    // productMetaData.setTitle(Organizer.getDatasetIdFromURI(inventoryOLA.getResource().getUrn().toString()));
    // productMetaData.setLastUpdate(DateUtils.getDateTimeAsUTCString(inventoryOLA.getLastModificationDate(),
    // DateUtils.DATETIME_PATTERN2));
    //
    // Resource resource = inventoryOLA.getResource();
    //
    // TimePeriod timePeriod = resource.getTimePeriod();
    // if (timePeriod != null) {
    // productMetaData.setTimeCoverage(timePeriod.getStart(), timePeriod.getEnd());
    // }
    //
    // GeospatialCoverage geospatialCoverage = resource.getGeospatialCoverage();
    // if (geospatialCoverage != null) {
    // ExtractCriteriaLatLon criteriaLatLon = new ExtractCriteriaLatLon(geospatialCoverage);
    // productMetaData.setGeoBBox(new LatLonRect(criteriaLatLon.getLatLonRect()));
    // }
    //
    // DepthCoverage depthCoverage = resource.getDepthCoverage();
    // if (depthCoverage != null) {
    // productMetaData
    // .setDepthCoverage(new MinMax(depthCoverage.getMin().getValue().doubleValue(),
    // depthCoverage.getMax().getValue().doubleValue()));
    // }
    //
    // // Gets variables metadata.
    // fr.cls.atoll.motu.library.inventory.Variables variables = resource.getVariables();
    // if (variables != null) {
    //
    // for (fr.cls.atoll.motu.library.inventory.Variable variable : variables.getVariable()) {
    //
    // ParameterMetaData parameterMetaData = new ParameterMetaData();
    //
    // parameterMetaData.setName(variable.getName());
    // parameterMetaData.setLabel(variable.getName());
    // parameterMetaData.setUnit(variable.getUnits());
    // parameterMetaData.setUnitLong(variable.getUnits());
    // parameterMetaData.setStandardName(variable.getVocabularyName());
    //
    // if (productMetaData.getParameterMetaDatas() == null) {
    // productMetaData.setParameterMetaDatas(new HashMap<String, ParameterMetaData>());
    // }
    // productMetaData.putParameterMetaDatas(variable.getName(), parameterMetaData);
    //
    // }
    // }
    //
    // // if (productMetaData.getDocumentations() == null) {
    // // productMetaData.setDocumentations(new ArrayList<DocMetaData>());
    // // }
    // // productMetaData.clearDocumentations();
    // //
    // // DocMetaData docMetaData = new DocMetaData();
    // // docMetaData.setTitle(ProductMetaData.MEDIA_KEY);
    // // docMetaData.setResource(CatalogData.CatalogType.FTP.name());
    // // productMetaData.addDocumentations(docMetaData);
    //
    // if (LOG.isDebugEnabled()) {
    // LOG.debug("loadInventoryGlobalMetaData() - exiting");
    // }
    // }

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
     * @return the opendap variable metadata
     * @throws MotuException the motu exception
     */
    private void getOpendapVariableMetadata() throws MotuException {
        // Gets variables metadata.
        String unitLong;
        String standardName;
        String longName;

        openNetCdfReader();

        List<Variable> variables = getNetCdfReader().getVariables();
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
            if (this.isProductAlongTrack()) {
                unusedVariables = UNUSED_VARIABLES_ATP;
            } else {
                unusedVariables = UNUSED_VARIABLES_GRIDS;
            }
            for (String unused : unusedVariables) {
                if (variable.getName().equalsIgnoreCase(unused)) {
                    isUnusedVar = true;
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
     * Update variables.
     *
     * @return the variables
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    public List<String> getVariables() throws MotuException {
        List<String> listVar = new ArrayList<String>();
        if (productMetaData != null) {
            Map<String, ParameterMetaData> parameterMetaDatas = productMetaData.getParameterMetaDatas();
            if (parameterMetaDatas != null) {
                Collection<ParameterMetaData> listParameterMetaData = parameterMetaDatas.values();
                for (ParameterMetaData parameterMetaData : listParameterMetaData) {
                    if (StringUtils.isNullOrEmpty(parameterMetaData.getName())) {
                        continue;
                    }
                    listVar.add(parameterMetaData.getName());
                }
            }
        }

        return listVar;
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
            throw new MotuException(ErrorType.SYSTEM, "Error in getLatAxisData - productMetaData is null");
        }

        Variable variable = productMetaData.getLatAxis();
        if (variable == null) {
            throw new MotuException(
                    ErrorType.INVALID_LATITUDE,
                    String.format("Error in getLatAxisData - No latitude axis found in this product '%s'", this.getProductId()));
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
     * @throws MotuException the motu exception
     * @throws NetCdfVariableException the net cdf variable exception
     */
    public Array getLonAxisData() throws MotuException, NetCdfVariableException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLonAxisData() - entering");
        }

        if (productMetaData == null) {
            throw new MotuException(ErrorType.SYSTEM, "Error in getLonAxisData - productMetaData is null");
        }

        Variable variable = productMetaData.getLonAxis();
        if (variable == null) {
            throw new MotuException(
                    ErrorType.INVALID_LONGITUDE,
                    String.format("Error in getLonAxisData - No longitude axis found in this product '%s'", this.getProductId()));
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
            throw new MotuException(ErrorType.SYSTEM, "Error in getGeoXAxisData - productMetaData is null");
        }

        Variable variable = productMetaData.getGeoXAxis();
        if (variable == null) {
            throw new MotuException(
                    ErrorType.INVALID_LAT_LON_RANGE,
                    String.format("Error in getGeoXAxisData - No geoX axis found in this product '%s'", this.getProductId()));
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
     * @throws MotuException the motu exception
     * @throws NetCdfVariableException the net cdf variable exception
     */
    public Array getGeoYAxisData() throws MotuException, NetCdfVariableException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getGeoYAxisData() - entering");
        }

        if (productMetaData == null) {
            throw new MotuException(ErrorType.SYSTEM, "Error in getGeoYAxisData - productMetaData is null");
        }

        Variable variable = productMetaData.getGeoYAxis();
        if (variable == null) {
            throw new MotuException(
                    ErrorType.INVALID_LAT_LON_RANGE,
                    String.format("Error in getGeoYAxisData - No geoY axis found in this product '%s'", this.getProductId()));
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
     * @throws MotuException the motu exception
     * @throws NetCdfVariableException the net cdf variable exception
     */
    public Array getTimeAxisData() throws MotuException {
        if (productMetaData == null) {
            throw new MotuException(ErrorType.SYSTEM, "Error in getTimeAxisData - productMetaData is null");
        }

        Variable variable = productMetaData.getTimeAxis();
        if (variable == null) {
            // throw new
            // MotuException(String.format("Error in getTimeAxisData - No time axis found in this product
            // '%s'",
            // this.getProductId()));
            return null;
        }

        Array returnArray;
        try {
            returnArray = readVariable(variable);
        } catch (NetCdfVariableException e) {
            throw new MotuException(ErrorType.NETCDF_VARIABLE, "Error while reading variable " + variable, e);
        }
        return returnArray;
    }

    /**
     * Gets time axis data values.
     *
     * @return a list constains time axis date values
     * @throws MotuException the motu exception if string to date conversion fails
     * @throws NetCdfVariableException the net cdf variable exception
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
                throw new MotuException(
                        ErrorType.INVALID_DATE,
                        "Failed to parse date '" + dateString + "'. Expected format '" + dateFormatter.toString() + "'");
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
     * @throws MotuException the motu exception
     * @throws NetCdfVariableException the net cdf variable exception
     */
    public List<String> getTimeAxisDataAsString() throws MotuException {
        List<String> list = new ArrayList<String>();

        Array array = getTimeAxisData();
        if (array == null) {
            return list;
        }

        double datetime = 0.0;

        for (IndexIterator it = array.getIndexIterator(); it.hasNext();) {
            datetime = it.getDoubleNext();
            list.add(0, NetCdfReader.getDateAsGMTNoZeroTimeString(datetime, productMetaData.getTimeAxis().getUnitsString()));
        }

        return list;
    }

    public Map<String, List<String>> getListTimeByDate() throws MotuException {
        Map<String, List<String>> timeByDate = new HashMap<>();
        List<String> dateList = getTimeAxisDataAsString();

        for (String currentDateTime : dateList) {
            String[] dateTimeSplit = currentDateTime.split(" ");
            if (dateTimeSplit.length > 1) {
                String currentDate = dateTimeSplit[0];
                String currentTime = dateTimeSplit[1];
                if (!timeByDate.containsKey(currentDate)) {
                    timeByDate.put(currentDate, new ArrayList<String>());
                }
                List<String> currentListOfTime = timeByDate.get(currentDate);
                currentListOfTime.add(currentTime);
            }
        }
        return timeByDate;
    }

    public String getMinTimeAxisDataAsString() throws MotuException {
        String date = "";
        List<String> dateList = getTimeAxisDataAsString();
        if (!dateList.isEmpty()) {
            date = dateList.get(0);
        }

        return date;
    }

    public String getMaxTimeAxisDataAsString() throws MotuException {
        String date = "";
        List<String> dateList = getTimeAxisDataAsString();
        if (!dateList.isEmpty()) {
            date = dateList.get(dateList.size() - 1);
        }

        return date;
    }

    /**
     * Gets Z (depth) axis data values.
     *
     * @return a {@link Array} constains depth axis data values
     * @throws MotuException the motu exception
     * @throws NetCdfVariableException the net cdf variable exception
     */
    public Array getZAxisData() throws MotuException {
        if (productMetaData == null) {
            throw new MotuException(ErrorType.SYSTEM, "Error in getZAxisData - productMetaData is null");
        }

        Variable variable = productMetaData.getZAxis();
        if (variable == null) {
            throw new MotuException(
                    ErrorType.INVALID_DEPTH,
                    String.format("Error in getZAxisData - No Z (depth) axis found in this product '%s'", this.getProductId()));
        }

        Array returnArray;
        try {
            returnArray = readVariable(variable);
        } catch (NetCdfVariableException e) {
            throw new MotuException(ErrorType.BAD_PARAMETERS, "Error while reading variable " + variable, e);
        }
        return returnArray;

    }

    /**
     * Gets Z (depth) axis data values.
     *
     * @return a list constains depth axis data values
     * @throws MotuException the motu exception
     * @throws NetCdfVariableException the net cdf variable exception
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
    public List<String> getZAxisRoundedUpDataAsString(int desiredDecimalNumberDigits) throws MotuException {
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
    public List<String> getZAxisRoundedDownDataAsString(int desiredDecimalNumberDigits) throws MotuException {
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
    public List<String> getZAxisDataAsString(RoundingMode roundingMode, int desiredDecimalNumberDigits) throws MotuException {
        List<String> list = new ArrayList<String>();
        Array array = getZAxisData();
        double depth = 0.0;
        for (IndexIterator it = array.getIndexIterator(); it.hasNext();) {
            depth = it.getDoubleNext();
            list.add(NetCdfReader.getStandardZAsString(depth, roundingMode, desiredDecimalNumberDigits));
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
     * @return a ucar.nc2.Variable variable or null if not found.
     * @throws MotuException the motu exception
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     */

    public Variable findVariable(String varName) throws MotuException, NetCdfVariableNotFoundException {
        openNetCdfReader();
        Variable variable = getNetCdfReader().getRootVariable(varName);
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
     * @return a ucar.ma2.Array with data for the variable.
     * @throws MotuException the motu exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
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
        openNetCdfReader();

        // Loads global metadata from opendap
        return getNetCdfReader().getGrid(variable);
    }

    /**
     * Extract ftp data.
     * 
     * @param dataOutputFormat the data output format
     */
    public void extractFtpData(OutputFormat dataOutputFormat) {

    }

    /**
     * Gets the min. value of a variable data. First search the min. value in 'valid_min' attribute of the
     * variable, if attribute doesn't exist, calculate the min. value from variable data.
     *
     * @param variable whose min. value has to be calculated
     * @return the min value of the variable data
     * @throws MotuException the motu exception
     * @throws NetCdfVariableException the net cdf variable exception
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
     * @return the min value of the variable data
     * @throws MotuException the motu exception
     * @throws NetCdfVariableException the net cdf variable exception
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
     * @return the min/max value of the variable data
     * @throws MotuException the motu exception
     * @throws NetCdfVariableException the net cdf variable exception
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
     * @param output URL of the output file.
     * @param format output format (NetCDF, HDF5, Ascii).
     */
    public void writeData(String output, String format) {

    }

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

    /**
     * Gets the time coverage from data files.
     *
     * @return the time coverage from data files
     */
    public List<String> getTimeCoverageFromDataFiles() {

        List<String> timeCoverage = new ArrayList<String>();

        if (dataFiles == null) {
            return timeCoverage;
        }

        GregorianCalendar calendar = new GregorianCalendar(NetCdfReader.GMT_TIMEZONE);

        for (DataFile dataFile : dataFiles) {
            // Warning : get Datetime as UTC
            DateTime fileStart = fr.cls.atoll.motu.library.converter.DateUtils.dateTimeToUTC(dataFile.getStartCoverageDate());

            calendar.setTime(fileStart.toDate());

            int h = calendar.get(Calendar.HOUR_OF_DAY);
            int m = calendar.get(Calendar.MINUTE);
            int s = calendar.get(Calendar.SECOND);

            String format = fr.cls.atoll.motu.library.converter.DateUtils.DATETIME_PATTERN3;

            if ((h == 0) && (m == 0) && (s == 0)) {
                format = fr.cls.atoll.motu.library.converter.DateUtils.DATETIME_PATTERN1;
            }

            if (fileStart != null) {
                timeCoverage.add(fr.cls.atoll.motu.library.converter.DateUtils.DATETIME_FORMATTERS.get(format).print(fileStart));
            }

        }

        return timeCoverage;

    }

    /**
     * Getter of the property <tt>location</tt>.
     * 
     * @return Returns the location.
     * 
     * @uml.property name="locationDataNCSS"
     */
    public String getLocationDataNCSS() {
        return this.locationDataNCSS;
    }

    /**
     * Setter of the property <tt>location</tt>.
     * 
     * @param locationDataNCSS The location to set.
     * 
     * @uml.property name="locationDataNCSS"
     */
    public void setLocationDataNCSS(String locationDataNCSS) {
        this.locationDataNCSS = locationDataNCSS;
    }

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

    /**
     * Getter of the property <tt>netCdfReader</tt>.
     * 
     * @return Returns the netCdfReader.
     * 
     * @uml.property name="netCdfReader"
     */
    public NetCdfReader getNetCdfReader() {
        if (netCdfReader == null) {
            setNetCdfReader(new NetCdfReader(locationData));
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
     * @return the time (in nanoseconds) taken to open the dataset
     * @throws MotuException the motu exception
     */
    public long openNetCdfReader() throws MotuException {
        return openNetCdfReader(true);
    }

    /**
     * Open net cdf reader.
     * 
     * @param enhanceVar the enhance var
     * 
     * @return the time (in nanoseconds) taken to open the dataset
     * @throws MotuException the motu exception
     */
    public long openNetCdfReader(boolean enhanceVar) throws MotuException {
        return getNetCdfReader().open(enhanceVar);
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

    /**
     * Gets the extraction path.
     * 
     * @return the extraction path
     * 
     * @throws MotuException the motu exception
     */
    public static String getExtractionPath() {
        StringBuffer stringBuffer = new StringBuffer();

        String dir = BLLManager.getInstance().getConfigManager().getMotuConfig().getExtractionPath();
        stringBuffer.append(dir);

        if (!(dir.endsWith("/") || dir.endsWith("\\"))) {
            stringBuffer.append("/");
        }
        return stringBuffer.toString();
    }

    /** {@inheritDoc} */
    @Override
    public int compare(Product o1, Product o2) {
        int compareValue = 1;
        if (o1 != null && o2 != null && o1.getProductId() != null) {
            compareValue = o1.getProductId().compareToIgnoreCase(o2.getProductId());
        }
        return compareValue;
    }
}
