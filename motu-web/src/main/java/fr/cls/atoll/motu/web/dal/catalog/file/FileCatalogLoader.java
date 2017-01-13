package fr.cls.atoll.motu.web.dal.catalog.file;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.vfs2.FileObject;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.library.converter.DateUtils;
import fr.cls.atoll.motu.library.inventory.Access;
import fr.cls.atoll.motu.library.inventory.CatalogOLA;
import fr.cls.atoll.motu.library.inventory.DepthCoverage;
import fr.cls.atoll.motu.library.inventory.GeospatialCoverage;
import fr.cls.atoll.motu.library.inventory.Inventory;
import fr.cls.atoll.motu.library.inventory.Resource;
import fr.cls.atoll.motu.library.inventory.ResourceOLA;
import fr.cls.atoll.motu.library.inventory.ResourcesOLA;
import fr.cls.atoll.motu.library.inventory.TimePeriod;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaLatLon;
import fr.cls.atoll.motu.web.common.utils.StringUtils;
import fr.cls.atoll.motu.web.common.utils.xml.XMLErrorHandler;
import fr.cls.atoll.motu.web.common.utils.xml.XMLUtils;
import fr.cls.atoll.motu.web.dal.catalog.AbstractCatalogLoader;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.DataFile;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.vfs.ConfigLoader;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.vfs.VFSManager;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ParameterMetaData;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ProductMetaData;
import ucar.ma2.MAMath.MinMax;
import ucar.unidata.geoloc.LatLonRect;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Pierre LACOSTE
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class FileCatalogLoader extends AbstractCatalogLoader {

    private static final String INVENTORY_CONFIG_SCHEMA = "fr/cls/atoll/motu/library/inventory/Inventory.xsd";

    private static final String CATALOG_CONFIG_SCHEMA = "fr/cls/atoll/motu/library/inventory/CatalogOLA.xsd";

    private static final String INVENTORY_OLA_SCHEMA_PACK_NAME = "fr.cls.atoll.motu.library.inventory";

    private static final String CATALOG_OLA_SCHEMA_PACK_NAME = INVENTORY_OLA_SCHEMA_PACK_NAME;

    /**
     * Load ftp catalog.
     * 
     * @param path the path
     * 
     * @throws MotuException the motu exception
     */
    public CatalogData loadFtpCatalog(String path) throws MotuException {
        CatalogData catalogData = new CatalogData();
        loadFtpCatalogInit(catalogData);

        CatalogOLA catalogOLA = getCatalogOLA(path);
        // --------------------------
        // -------- Loads dataset
        // --------------------------
        ResourcesOLA resourcesOLA = catalogOLA.getResourcesOLA();

        catalogData.setTitle(catalogOLA.getName());

        for (ResourceOLA resourceOLA : resourcesOLA.getResourceOLA()) {
            catalogData.setCurrentProductType(resourceOLA.getUrn().toString());
            loadFtpInventory(resourceOLA.getInventoryUrl().toString(), catalogData);
            catalogData.getListProductTypeDataset().add(catalogData.getSameProductTypeDataset());
        }

        // Remove products that are not anymore in the catalog
        catalogData.getProducts().keySet().retainAll(catalogData.getProductsLoaded());

        return catalogData;
    }

    /**
     * Gets the catalog ola.
     * 
     * @param xmlUri the xml uri
     * 
     * @return the catalog ola
     * 
     * @throws MotuException the motu exception
     */
    public static CatalogOLA getCatalogOLA(String xmlUri) throws MotuException {

        CatalogOLA catalogOLA = new CatalogOLA();

        List<String> errors = validateCatalogOLA(xmlUri);
        if (errors.size() > 0) {
            StringBuffer stringBuffer = new StringBuffer();
            for (String str : errors) {
                stringBuffer.append(str);
                stringBuffer.append("\n");
            }
            throw new MotuException(
                    ErrorType.LOADING_CATALOG,
                    String.format("ERROR - CatalogOLA file '%s' is not valid - See errors below:\n%s", xmlUri, stringBuffer.toString()));
        }

        InputStream in = getUriAsInputStream(xmlUri);

        try {
            JAXBContext jc = JAXBContext.newInstance(CATALOG_OLA_SCHEMA_PACK_NAME);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            catalogOLA = (CatalogOLA) unmarshaller.unmarshal(in);
        } catch (Exception e) {
            throw new MotuException(ErrorType.LOADING_CATALOG, "Error in getCatalogOLA", e);
        }

        if (catalogOLA == null) {
            throw new MotuException(ErrorType.LOADING_CATALOG, "Unable to load Catalog OLA (CatalogOLA is null)");
        }

        try {
            in.close();
        } catch (IOException io) {
            // Do nothing
        }

        return catalogOLA;
    }

    public void loadFtpCatalogInit(CatalogData cd) {
        Set<String> productsLoaded = cd.getProductsLoaded();

        // Set to store product id that are in the catalog.
        if (productsLoaded == null) {
            productsLoaded = new HashSet<String>();
            cd.setProductsLoaded(productsLoaded);
        }
        productsLoaded.clear();

        List<List<Product>> listProductTypeDataset = new ArrayList<List<Product>>();
        cd.setListProductTypeDataset(listProductTypeDataset);

        List<String> listCatalogRefSubPaths = new ArrayList<String>();
        cd.setListCatalogRefSubPaths(listCatalogRefSubPaths);

        cd.setCurrentProductType("");
        getCurrentProductSubTypes(cd);

        cd.setSameProductTypeDataset(new ArrayList<Product>());
    }

    /**
     * Gets the current product sub types.
     * 
     * @return the currentProductSubTypes
     */
    protected List<String> getCurrentProductSubTypes(CatalogData cd) {
        if (cd.getCurrentProductSubTypes() == null) {
            cd.setCurrentProductSubTypes(new ArrayList<String>());
        }
        return cd.getCurrentProductSubTypes();
    }

    /**
     * Load ftp inventory.
     * 
     * @param xmlUri the xml uri
     * 
     * @throws MotuException the motu exception
     */
    public Product loadFtpInventory(String xmlUri, CatalogData cd) throws MotuException {

        Inventory inventoryOLA = getInventoryOLA(xmlUri);

        String productId = inventoryOLA.getResource().getUrn().toString();

        boolean newProduct = true;

        Product product = cd.getProducts(productId);

        if (product == null) {
            product = new Product();
        } else {
            newProduct = false;
        }

        loadInventoryMetaData(product, inventoryOLA);
        product.setLocationMetaData(xmlUri);

        ProductMetaData productMetaData = product.getProductMetaData();
        productMetaData.setProductType(cd.getCurrentProductType());
        productMetaData.setLastUpdate(DateUtils.getDateTimeAsUTCString(inventoryOLA.getLastModificationDate(), DateUtils.DATETIME_PATTERN2));
        List<Product> sameProductTypeDataset = new ArrayList<Product>();
        sameProductTypeDataset.add(product);
        cd.setSameProductTypeDataset(sameProductTypeDataset);

        cd.getProductsLoaded().add(productId);

        if (newProduct) {
            putProducts(productMetaData.getProductId(), product, cd);
        }

        return product;
    }

    /**
     * Associates the specified value with the specified key in this map (optional operation).
     * 
     * @param value value to be associated with the specified key.
     * @param key key with which the specified value is to be associated.
     * 
     * @return previous value associated with specified key, or <tt>null</tt>
     * 
     * @see java.util.Map#put(Object,Object)
     * @uml.property name="products"
     */
    @Override
    public Product putProducts(String key, Product value, CatalogData cd) {
        if (key == null) {
            return null;
        }

        if (value == null) {
            return null;
        }

        cd.getProductsByTdsUrl().put(value.getTdsUrlPath(), value);

        return cd.getProducts().put(key.trim(), value);
    }

    /**
     * Gets the inventory ola.
     * 
     * @param xmlUri the xml uri to load
     * 
     * @return the inventory ola
     * 
     * @throws MotuException the motu exception
     */
    public static Inventory getInventoryOLA(String xmlUri) throws MotuException {

        if (StringUtils.isNullOrEmpty(xmlUri)) {
            throw new MotuException(ErrorType.LOADING_CATALOG, "ERROR - Organizer#getInventoryOLA - Inventory  url '%s' is null or empty");
        }

        Inventory inventoryOLA = null;

        List<String> errors = validateInventoryOLA(xmlUri);
        if (errors.size() > 0) {
            StringBuffer stringBuffer = new StringBuffer();
            for (String str : errors) {
                stringBuffer.append(str);
                stringBuffer.append("\n");
            }
            throw new MotuException(
                    ErrorType.LOADING_CATALOG,
                    String.format("ERROR - Inventory file '%s' is not valid - See errors below:\n%s", xmlUri, stringBuffer.toString()));
        }

        InputStream in = getUriAsInputStream(xmlUri);

        try {
            JAXBContext jc = JAXBContext.newInstance(INVENTORY_OLA_SCHEMA_PACK_NAME);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            inventoryOLA = (Inventory) unmarshaller.unmarshal(in);
        } catch (Exception e) {
            throw new MotuException(ErrorType.LOADING_CATALOG, "Error in getInventoryOLA", e);
        }

        if (inventoryOLA == null) {
            throw new MotuException(ErrorType.LOADING_CATALOG, "Unable to load Inventory (inventoryOLA is null)");
        }

        try {
            in.close();
        } catch (IOException io) {
            // Do nothing
        }
        return inventoryOLA;
    }

    /**
     * Validate inventory ola.
     * 
     * @param xmlUri the xml uri to validate
     * 
     * @return the list of XML validation errors (empty is no error)
     * 
     * @throws MotuException the motu exception
     */
    public static List<String> validateInventoryOLA(String xmlUri) throws MotuException {

        InputStream inSchema = getUriAsInputStream(INVENTORY_CONFIG_SCHEMA);
        if (inSchema == null) {
            throw new MotuException(
                    ErrorType.LOADING_CATALOG,
                    String.format("ERROR in Organiser.validateInventoryOLA - InventoryOLA  schema ('%s') not found:", INVENTORY_CONFIG_SCHEMA));
        }

        InputStream inXml = getUriAsInputStream(xmlUri);

        if (inXml == null) {
            throw new MotuException(
                    ErrorType.LOADING_CATALOG,
                    String.format("ERROR in Organiser.validateInventoryOLA - InventoryOLA  xml ('%s') not found:", xmlUri));
        }

        XMLErrorHandler errorHandler = XMLUtils.validateXML(inSchema, inXml);

        try {
            inXml.close();
        } catch (IOException e) {
            // Do nothing
        }

        if (errorHandler == null) {
            throw new MotuException(
                    ErrorType.LOADING_CATALOG,
                    "ERROR in Organiser.validateInventoryOLA - InventoryOLA schema : XMLErrorHandler is null");
        }
        return errorHandler.getErrors();

    }

    /**
     * Gets the uri as input stream.
     * 
     * @param uri the uri
     * 
     * @return the uri as input stream
     * 
     * @throws MotuException the motu exception
     */
    public static InputStream getUriAsInputStream(String uri) throws MotuException {
        InputStream in = null;
        try {

            in = ConfigLoader.getInstance().getAsStream(uri);
            if (in == null) {
                FileObject fileObject = resolveFile(uri);
                if (fileObject != null) {
                    in = fileObject.getContent().getInputStream();
                }
            }
        } catch (IOException e) {
            throw new MotuException(ErrorType.LOADING_CATALOG, String.format("'%s' uri file has not be found", uri), e);
        }
        return in;
    }

    /**
     * Resolve file.
     * 
     * @param uri the uri
     * 
     * @return the file object
     * 
     * @throws MotuException the motu exception
     */
    public static FileObject resolveFile(String uri) throws MotuException {
        return new VFSManager().resolveFile(uri);
    }

    /**
     * Load inventory meta data.
     * 
     * @param inventoryOLA the inventory ola
     * @throws MotuException the motu exception
     */
    public void loadInventoryMetaData(Product product, Inventory inventoryOLA) throws MotuException {

        if (inventoryOLA == null) {
            return;
        }

        Resource resource = inventoryOLA.getResource();
        Access access = resource.getAccess();
        ProductMetaData productMetaData = product.getProductMetaData();

        if (productMetaData == null) {
            productMetaData = new ProductMetaData();
            product.setProductMetaData(productMetaData);
        }

        loadInventoryGlobalMetaData(product, inventoryOLA);

        URI accessUri = null;
        URI accessUriTemp = null;
        String login = access.getLogin();
        String password = access.getPassword();
        StringBuffer userInfo = null;

        if (password == null) {
            password = "";
        }

        if (!StringUtils.isNullOrEmpty(login)) {
            userInfo = new StringBuffer();
            userInfo.append(login);
            userInfo.append(":");
            userInfo.append(password);
        }

        try {
            accessUriTemp = access.getUrlPath();

            if (userInfo != null) {
                accessUri = new URI(
                        accessUriTemp.getScheme(),
                        userInfo.toString(),
                        accessUriTemp.getHost(),
                        accessUriTemp.getPort(),
                        accessUriTemp.getPath(),
                        accessUriTemp.getQuery(),
                        accessUriTemp.getFragment());
            } else {
                accessUri = accessUriTemp;
            }

        } catch (URISyntaxException e) {
            throw new MotuException(
                    ErrorType.LOADING_CATALOG,
                    String.format("Invalid URI '%s' in inventory product '%s' at '%s.urlPath' tag.attribute",
                                  accessUri,
                                  productMetaData.getProductId(),
                                  access.getClass().toString()),
                    e);
        }

        product.setLocationData(accessUri.toString());

        List<DataFile> dataFiles = CatalogData.loadFtpDataFiles(inventoryOLA);

        product.setDataFiles(dataFiles);
    }

    /**
     * Reads product global variable metadata from a NetCDF file.
     * 
     * @param inventoryOLA the inventory ola
     * 
     * @throws MotuException the motu exception
     */

    public void loadInventoryGlobalMetaData(Product product, Inventory inventoryOLA) throws MotuException {
        ProductMetaData productMetaData = product.getProductMetaData();

        if (product.getProductMetaData() == null) {
            throw new MotuException(ErrorType.LOADING_CATALOG, "Error in loadInventoryGlobalMetaData - Unable to load - productMetaData is null");
        }

        productMetaData.setProductId(inventoryOLA.getResource().getUrn().toString());
        productMetaData.setTitle(inventoryOLA.getResource().getUrn().toString());
        productMetaData.setLastUpdate(DateUtils.getDateTimeAsUTCString(inventoryOLA.getLastModificationDate(), DateUtils.DATETIME_PATTERN2));

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
            productMetaData
                    .setDepthCoverage(new MinMax(depthCoverage.getMin().getValue().doubleValue(), depthCoverage.getMax().getValue().doubleValue()));
            productMetaData.setDepthUnits(depthCoverage.getUnits());
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
    }

    /**
     * Validate catalog ola.
     * 
     * @param xmlUri the xml uri
     * 
     * @return the list< string>
     * 
     * @throws MotuException the motu exception
     */
    public static List<String> validateCatalogOLA(String xmlUri) throws MotuException {

        InputStream inSchema = getUriAsInputStream(CATALOG_CONFIG_SCHEMA);
        if (inSchema == null) {
            throw new MotuException(
                    ErrorType.LOADING_CATALOG,
                    String.format("ERROR in Organiser.validateInventoryOLA - CatalogOLA  schema ('%s') not found:", CATALOG_CONFIG_SCHEMA));
        }

        InputStream inXml = getUriAsInputStream(xmlUri);

        if (inXml == null) {
            throw new MotuException(
                    ErrorType.LOADING_CATALOG,
                    String.format("ERROR in Organiser.validateInventoryOLA - CatalogOLA  xml ('%s') not found:", xmlUri));
        }

        XMLErrorHandler errorHandler = XMLUtils.validateXML(inSchema, inXml);

        if (errorHandler == null) {
            throw new MotuException(
                    ErrorType.LOADING_CATALOG,
                    "ERROR in Organiser.validateInventoryOLA - CatalogOLA schema : XMLErrorHandler is null");
        }
        return errorHandler.getErrors();

    }

}
