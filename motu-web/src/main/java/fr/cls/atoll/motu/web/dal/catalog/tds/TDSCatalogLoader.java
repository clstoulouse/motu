package fr.cls.atoll.motu.web.dal.catalog.tds;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.library.cas.exception.MotuCasException;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDateException;
import fr.cls.atoll.motu.web.bll.request.model.ExtractCriteriaLatLon;
import fr.cls.atoll.motu.web.bll.request.model.metadata.DocMetaData;
import fr.cls.atoll.motu.web.common.utils.ReflectionUtils;
import fr.cls.atoll.motu.web.dal.catalog.AbstractCatalogLoader;
import fr.cls.atoll.motu.web.dal.config.xml.model.CatalogService;
import fr.cls.atoll.motu.web.dal.request.netcdf.NetCdfReader;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ProductMetaData;
import fr.cls.atoll.motu.web.dal.tds.model.Catalog;
import fr.cls.atoll.motu.web.dal.tds.model.CatalogRef;
import fr.cls.atoll.motu.web.dal.tds.model.DatasetType;
import fr.cls.atoll.motu.web.dal.tds.model.DateTypeFormatted;
import fr.cls.atoll.motu.web.dal.tds.model.DocumentationType;
import fr.cls.atoll.motu.web.dal.tds.model.GeospatialCoverage;
import fr.cls.atoll.motu.web.dal.tds.model.Metadata;
import fr.cls.atoll.motu.web.dal.tds.model.Property;
import fr.cls.atoll.motu.web.dal.tds.model.Service;
import fr.cls.atoll.motu.web.dal.tds.model.SpatialRange;
import fr.cls.atoll.motu.web.dal.tds.model.TimeCoverageType;
import fr.cls.atoll.motu.web.dal.tds.model.Variables;
import ucar.ma2.MAMath.MinMax;
import ucar.unidata.geoloc.LatLonRect;

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
public class TDSCatalogLoader extends AbstractCatalogLoader {

    /** NCSS TDS Service Type. */
    public static final String TDS_NCSS_SERVICE = "NetcdfSubset";

    /** OpenDAP TDS Service Type. */
    public static final String TDS_OPENDAP_SERVICE = "opendap";

    /** DODS TDS Service Type. */
    public static final String TDS_DODS_SERVICE = "dods";

    /** ServiceName XML tag element. */
    private static final String XML_TAG_START = "start";

    /** ServiceName XML tag element. */
    private static final String XML_TAG_END = "end";

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Loads an Tds catalog.
     * 
     * @param catalogRef catalog reference(from Tds catalog) from which information will be loaded Dataset
     *            contains a list of datasets, recursivity is use to load.
     * 
     * @return the number of path element found in the catalog ref name.
     * 
     * @throws MotuException the motu exception
     */
    private int loadTdsCatalogRef(CatalogRef catalogRef, boolean useSSO, CatalogData cd) throws MotuException {
        List<String> listCatalogRefSubPaths = new ArrayList<String>();
        if (catalogRef == null || catalogRef.getHref() == null || catalogRef.getHref().trim().length() <= 0) {
            return 0;
        }

        String catalogHref = catalogRef.getHref().replace("\\", "/");
        String[] catalogHrefSplit = catalogHref.split("/");
        String catalogXmlName = "";

        if (catalogHrefSplit.length > 0) {
            catalogXmlName = catalogHrefSplit[catalogHrefSplit.length - 1];
            for (int i = 0; i < catalogHrefSplit.length - 1; i++) {
                listCatalogRefSubPaths.add(catalogHrefSplit[i]);
            }
        }

        StringBuffer location = new StringBuffer();
        location.append(catalogRef.getUrlPath());
        for (String path : listCatalogRefSubPaths) {
            location.append(path);
            location.append("/");
        }
        location.append(catalogXmlName);

        loadTdsSubCatalog(location.toString(), useSSO, cd);

        return catalogHrefSplit.length - 1;
    }

    /**
     * Loads an Tds top level catalog.
     * 
     * @param path path of the Xml top level catalog file.
     * 
     * @throws MotuException the motu exception
     */
    private void loadTdsSubCatalog(String path, boolean useSSO, CatalogData cd) throws MotuException {
        try {
            Catalog catalogXml = getCatalogFromTDS(path, useSSO);

            List<JAXBElement<? extends DatasetType>> list = catalogXml.getDataset();

            for (Iterator<JAXBElement<? extends DatasetType>> it = list.iterator(); it.hasNext();) {
                JAXBElement<? extends DatasetType> o = it.next();
                DatasetType datasetType = o.getValue();
                if (datasetType != null) {
                    if (datasetType instanceof CatalogRef) {
                        int numberSubPaths = loadTdsCatalogRef((CatalogRef) datasetType, useSSO, cd);
                        removeListCatalogRefSubPaths(cd, numberSubPaths);
                    } else if (datasetType instanceof DatasetType) {
                        loadTdsProducts(datasetType, catalogXml, useSSO, cd);
                    }
                }

            }

            // Remove products that are not anymore in the catalog
            cd.productsKeySet().retainAll(cd.getProductsLoaded());
        } catch (Exception e) {
            throw new MotuException("Error while initializing TDS sub catalog", e);
        }

    }

    /**
     * Loads Tds products from Tds catalog.
     * 
     * @param catalogXml Tds catalog
     * @param datasetType dataset (from Tds catalog) from which information will be loaded Dataset contains a
     *            list of datasets, recursivity is use to load.
     * 
     * @throws MotuException the motu exception
     */
    private void loadTdsProducts(DatasetType datasetType, Catalog catalogXml, boolean useSSO, CatalogData cd) throws MotuException {
        if (datasetType == null) {
            return;
        }

        // When Url path of the dataset is not null or not empty,
        // we are at the last level --> Create Product and add to the list.
        if (datasetType.getUrlPath() != null) {
            if (!datasetType.getUrlPath().equals("")) {

                initializeProductFromTds(datasetType, catalogXml, useSSO, cd);

                return;
            }
        }

        cd.setCurrentGeospatialCoverage(findTdsGeoAndDepthCoverage(datasetType));

        // Saves - the product type at the top level product (on the first call,
        // level 0)
        // - the product sub-type, if not top level product
        // (type or sub-type correspond to the dataser name)
        if (cd.getCurrentProductType() == null || cd.getCurrentProductType().length() <= 0) {
            cd.setCurrentProductType(datasetType.getName());
        } else {
            cd.getCurrentProductSubTypes().add(datasetType.getName());
        }

        List<Service> listServices = datasetType.getService();
        for (Service o : listServices) {
            if (o == null) {
                continue;
            }
            catalogXml.getService().add(o);
            // System.out.print(o.getName());
            // System.out.print(":");
            // System.out.println(o.getServiceType());
        }

        List<JAXBElement<? extends DatasetType>> list = datasetType.getDataset();

        for (Iterator<JAXBElement<? extends DatasetType>> it = list.iterator(); it.hasNext();) {
            JAXBElement<? extends DatasetType> o = it.next();

            DatasetType datasetTypeChild = o.getValue();
            if (datasetTypeChild != null) {
                if (datasetTypeChild instanceof CatalogRef) {
                    // System.out.println("is CatalogRef");
                    int numberSubPaths = loadTdsCatalogRef((CatalogRef) datasetTypeChild, useSSO, cd);
                    removeListCatalogRefSubPaths(cd, numberSubPaths);

                } else if (datasetType instanceof DatasetType) {
                    // System.out.println("is DatasetType");
                    loadTdsProducts(datasetTypeChild, catalogXml, useSSO, cd);
                }
            }
        }

        cd.setCurrentGeospatialCoverage(null);

        if (cd.getSameProductTypeDataset().size() > 0) {
            cd.getListProductTypeDataset().add(cd.getSameProductTypeDataset());
            cd.getSameProductTypeDataset().clear();
        }

        int last = cd.getCurrentProductSubTypes().size() - 1;
        if (last >= 0) {
            cd.getCurrentProductSubTypes().remove(last);
        }

    }

    /**
     * Find tds geo and depth coverage.
     * 
     * @param datasetType the dataset type
     * 
     * @return the geospatial coverage
     * 
     * @throws MotuException the motu exception
     */
    private GeospatialCoverage findTdsGeoAndDepthCoverage(DatasetType datasetType) throws MotuException {
        GeospatialCoverage geospatialCoverage = null;

        List<Object> listMetadataObject = CatalogData.findJaxbElement(datasetType.getThreddsMetadataGroup(), Metadata.class);
        if (listMetadataObject == null) {
            return geospatialCoverage;
        }
        for (Object objectMetadataElt : listMetadataObject) {

            if (!(objectMetadataElt instanceof Metadata)) {
                continue;
            }
            Metadata metadata = (Metadata) objectMetadataElt;
            List<Object> listGeoCoverageObject = CatalogData.findJaxbElement(metadata.getThreddsMetadataGroup(), GeospatialCoverage.class);
            for (Object objectElt : listGeoCoverageObject) {

                if (!(objectElt instanceof GeospatialCoverage)) {
                    continue;
                }

                geospatialCoverage = (GeospatialCoverage) objectElt;
            }
        }

        return geospatialCoverage;
    }

    /**
     * Initializes product and its metadata from an Tds dataset.
     * 
     * @param catalogXml Tds catalog
     * @param datasetType Tds dataset which have a non-empty url path.
     * 
     * @throws MotuException the motu exception
     */
    private void initializeProductFromTds(DatasetType datasetType, Catalog catalogXml, boolean isSSO, CatalogData cd) throws MotuException {

        if (datasetType == null) {
            throw new MotuException("Error in initializeProductFromTds - Tds dataset is null");
        }
        if (datasetType.getUrlPath() == null) {
            throw new MotuException("Error in initializeProductFromTds - Invalid dataset branch - Tds dataset has a null url path");
        }
        if (datasetType.getUrlPath().equals("")) {
            throw new MotuException("Error in initializeProductFromTds - Invalid dataset branch - Tds dataset has an empty url path");
        }

        String productId = "";
        String tdsUrlPath = "";
        boolean newProduct = true;

        if (datasetType.getUrlPath() != null) {
            tdsUrlPath = datasetType.getUrlPath();
        }

        if (datasetType.getID() != null) {
            if (datasetType.getID().equals("")) {
                productId = tdsUrlPath;
            } else {
                productId = datasetType.getID();
            }
        } else {
            productId = tdsUrlPath;
        }

        ProductMetaData productMetaData = null;

        Product product = cd.getProducts(productId);
        if (product == null) {
            product = new Product(isSSO);
            productMetaData = new ProductMetaData();
            productMetaData.setProductId(productId);
            productMetaData.setTdsUrlPath(tdsUrlPath);
        } else {
            newProduct = false;
            productMetaData = product.getProductMetaData();
        }

        cd.getProductsLoaded().add(productId);

        productMetaData.setProductType(cd.getCurrentProductType());

        List<String> productSubTypesWork = new ArrayList<String>();
        productSubTypesWork.addAll(cd.getCurrentProductSubTypes());

        productMetaData.setProductSubTypes(productSubTypesWork);
        productMetaData.setTitle(datasetType.getName());

        // Loads href documentation of the dataset
        loadTdsHrefDoc(datasetType, productMetaData);

        // Loads time coverage of the dataset
        loadTdsTimeCoverage(datasetType, productMetaData);

        // Loads geo and depth coverage
        loadTdsGeoAndDepthCoverage(datasetType, productMetaData, cd);

        // Loads Variables vocabulary
        loadTdsVariablesVocabulary(datasetType, productMetaData, cd);

        // Loads Property meatadata
        loadTdsMetadataProperty(datasetType, productMetaData, cd);

        // Loads last date update
        loadTdsMetadataLastDate(datasetType, productMetaData);

        product.setProductMetaData(productMetaData);

        // Load OPENDAP/dods url from TDS catalog
        getUrlOpendapFromTds(datasetType, catalogXml, product, cd);

        // Load NCSS service url from TDS catalog
        getUrlNCSSFromTds(datasetType, catalogXml, product, cd);

        if (newProduct) {
            putProducts(productMetaData.getProductId(), product, cd);
        }
        cd.getSameProductTypeDataset().add(product);
    }

    /**
     * get NCSS server Url (this is optional, only OpenDAP is mandatory).
     * 
     * @param product the product
     * @param catalogXml Xml TDS catalog
     * @param datasetType dataset from which one's search url
     * 
     * @return Opendap Server Url.
     * 
     * @throws MotuException the motu exception
     */
    private String getUrlNCSSFromTds(DatasetType datasetType, Catalog catalogXml, Product product, CatalogData cd) throws MotuException {
        String tdsServiceName = "";
        String xmlNamespace = ReflectionUtils.getXmlSchemaNamespace(datasetType.getClass());
        StringBuffer xPath = new StringBuffer();
        xPath.append("//threddsMetadataGroup[name='{");
        xPath.append(xmlNamespace);
        xPath.append("}serviceName']/value");
        List<Object> listServiceNameObject = CatalogData.findJaxbElementUsingJXPath(datasetType, xPath.toString());

        for (Object objectElt : listServiceNameObject) {
            if (!(objectElt instanceof String)) {
                continue;
            }
            tdsServiceName = (String) objectElt;
            break;
        }

        if (tdsServiceName.equals("")) {
            throw new MotuException(
                    String.format("Error in getUrlNCSSFromTds - No TDS service found in TDS catalog for dataset '%s' ", datasetType.getName()));
        }

        // Search for opendap service, dods if not found
        Service tdsService = findTdsService(tdsServiceName, TDS_NCSS_SERVICE, catalogXml.getService());
        if (tdsService == null) {
            return ""; // It is not a mandatory service
        }

        // Gather NCSS URLS from TDS
        String relativeUrl = tdsService.getBase();
        URI uri = URI.create(cd.getUrlSite());
        URI opendapUri = null;
        try {
            opendapUri = new URI(uri.getScheme(), uri.getAuthority(), relativeUrl, null, null);
        } catch (URISyntaxException e) {
            throw new MotuException(
                    String.format("Error in getUrlNCSSFromTds - Uri creation: scheme='%s', authority='%s', path='%s'",
                                  uri.getScheme(),
                                  uri.getAuthority(),
                                  relativeUrl),
                    e);
        }

        // Store metadata in Product
        StringBuffer locationDataNCSS = new StringBuffer();
        locationDataNCSS.append(opendapUri.toString());
        locationDataNCSS.append(datasetType.getUrlPath());
        product.setLocationDataNCSS(locationDataNCSS.toString());

        return opendapUri.toString();
    }

    /**
     * Searches a TDS service from a service name into a list of TDS service.
     * 
     * @param tdsServiceName service name to search
     * @param listTdsService list of TDS services
     * 
     * @return a Service object if found, otherwhise null.
     */
    private Service findTdsService(String tdsServiceName, List<Service> listTdsService) {

        Service serviceFound = null;

        for (Service service : listTdsService) {
            if (service.getName().equalsIgnoreCase(tdsServiceName)) {
                serviceFound = service;
                break;
            }

            serviceFound = findTdsService(tdsServiceName, service.getService());
            if (serviceFound != null) {
                break;
            }
        }
        return serviceFound;

    }

    /**
     * Searches a TDS service from a service name and a service type into a list of TDS service. Service name
     * can be a compounded services, search is made also in child services
     * 
     * @param tdsServiceName service name to search
     * @param tdsServiceType service type to search (see InvCatalog.1.0.xsd)
     * @param listTdsService list of TDS services
     * 
     * @return a Service object if found, otherwhise null.
     */
    private Service findTdsService(String tdsServiceName, String tdsServiceType, List<Service> listTdsService) {

        // search service with name
        Service serviceFound = findTdsService(tdsServiceName, listTdsService);

        if (serviceFound == null) {
            return null;
        }

        if (serviceFound.getServiceType().equalsIgnoreCase(tdsServiceType)) {
            return serviceFound;
        }

        // search service with type
        List<Service> listTdsServiceChild = serviceFound.getService();

        serviceFound = findTdsServiceType(tdsServiceType, listTdsServiceChild);

        return serviceFound;

    }

    /**
     * Searches a TDS service from a service type into a list of TDS service.
     * 
     * @param tdsServiceType service type to search
     * @param listTdsService list of TDS services
     * 
     * @return a Service object if found, otherwhise null.
     */
    private Service findTdsServiceType(String tdsServiceType, List<Service> listTdsService) {

        Service serviceFound = null;

        for (Service service : listTdsService) {
            if (service.getServiceType().equalsIgnoreCase(tdsServiceType)) {
                serviceFound = service;
                break;
            }

            serviceFound = findTdsServiceType(tdsServiceType, service.getService());
            if (serviceFound != null) {
                break;
            }
        }
        return serviceFound;

    }

    /**
     * get Opendap (or Dods) server Url.
     * 
     * @param product the product
     * @param catalogXml Xml TDS catalog
     * @param datasetType dataset from which one's search url
     * 
     * @return Opendap Server Url.
     * 
     * @throws MotuException the motu exception
     */
    private String getUrlOpendapFromTds(DatasetType datasetType, Catalog catalogXml, Product product, CatalogData cd) throws MotuException {

        String tdsServiceName = "";
        String xmlNamespace = ReflectionUtils.getXmlSchemaNamespace(datasetType.getClass());
        StringBuffer xPath = new StringBuffer();
        xPath.append("//threddsMetadataGroup[name='{");
        xPath.append(xmlNamespace);
        xPath.append("}serviceName']/value");
        List<Object> listServiceNameObject = CatalogData.findJaxbElementUsingJXPath(datasetType, xPath.toString());

        for (Object objectElt : listServiceNameObject) {
            if (!(objectElt instanceof String)) {
                continue;
            }
            tdsServiceName = (String) objectElt;
            break;
        }

        if (tdsServiceName.equals("")) {
            throw new MotuException(
                    String.format("Error in getUrlOpendapFromTds - No TDS service found in TDS catalog for dataset '%s' ", datasetType.getName()));
        }

        // Search for opendap service, dods if not found
        Service tdsService = findTdsService(tdsServiceName, TDS_OPENDAP_SERVICE, catalogXml.getService());
        if (tdsService == null) {
            tdsService = findTdsService(tdsServiceName, TDS_DODS_SERVICE, catalogXml.getService());
        }

        // One of the two is mandatory
        if (tdsService == null) {
            throw new MotuException(
                    String.format("Error in getUrlOpendapFromTds - TDS service '%s' found in TDS catalog for dataset '%s' has neither 'opendap' nor 'dods' service type",
                                  tdsServiceName,
                                  datasetType.getName()));
        }

        // Gather URLS from TDS
        String relativeUrl = tdsService.getBase();
        URI uri = URI.create(cd.getUrlSite());
        URI opendapUri = null;
        try {
            opendapUri = new URI(uri.getScheme(), uri.getAuthority(), relativeUrl, null, null);
        } catch (URISyntaxException e) {
            throw new MotuException(
                    String.format("Error in getUrlOpendapFromTds - Uri creation: scheme='%s', authority='%s', path='%s'",
                                  uri.getScheme(),
                                  uri.getAuthority(),
                                  relativeUrl),
                    e);
        }

        // Store metadata in Product
        StringBuffer locationData = new StringBuffer();
        locationData.append(opendapUri.toString());
        locationData.append(datasetType.getUrlPath());
        product.setLocationData(locationData.toString());
        product.setTdsServiceType(tdsService.getServiceType().toLowerCase());

        return opendapUri.toString();
    }

    /**
     * Loads Tsd products documentations from Tds catalog.
     * 
     * @param datasetType dataset (from Tds catalog) from which information will be loaded
     * @param productMetaData metadata in which to record href documentation
     */
    private void loadTdsHrefDoc(DatasetType datasetType, ProductMetaData productMetaData) {

        if (productMetaData.getDocumentations() == null) {
            productMetaData.setDocumentations(new ArrayList<DocMetaData>());
        }
        productMetaData.clearDocumentations();

        List<Object> listDocObject = CatalogData.findJaxbElement(datasetType.getThreddsMetadataGroup(), DocumentationType.class);

        for (Object objectElt : listDocObject) {

            if (!(objectElt instanceof DocumentationType)) {
                continue;
            }
            DocumentationType documentation = (DocumentationType) objectElt;

            String serviceName = documentation.getTypeDoc();
            if (serviceName == null) {
                continue;
            }
            String href = documentation.getHref();
            if (href.equals("")) {
                continue;
            }
            DocMetaData docMetaData = new DocMetaData();
            docMetaData.setResource(href);
            docMetaData.setTitle(serviceName.toLowerCase());

            productMetaData.addDocumentations(docMetaData);

        }

    }

    /**
     * Load tds time coverage.
     * 
     * @param datasetType the dataset type
     * @param productMetaData the product meta data
     * 
     * @throws MotuException the motu exception
     */
    private void loadTdsTimeCoverage(DatasetType datasetType, ProductMetaData productMetaData) throws MotuException {
        List<Object> listTimeCoverageObject = CatalogData.findJaxbElement(datasetType.getThreddsMetadataGroup(), TimeCoverageType.class);

        productMetaData.setTimeCoverage(null);

        for (Object objectElt : listTimeCoverageObject) {

            if (!(objectElt instanceof TimeCoverageType)) {
                continue;
            }
            TimeCoverageType timeCoverage = (TimeCoverageType) objectElt;

            List<JAXBElement<?>> startOrEndOrDuration = timeCoverage.getStartOrEndOrDuration();

            Date start = getPartOfTimeCoverage(XML_TAG_START, startOrEndOrDuration);
            Date end = getPartOfTimeCoverage(XML_TAG_END, startOrEndOrDuration);
            productMetaData.setTimeCoverage(start, end);

            productMetaData.setTimeCoverageResolution(timeCoverage.getResolution());

        }
    }

    /**
     * Gets the part of time coverage.
     * 
     * @param xmlTagName the xml tag name
     * @param listJaxbElement the list jaxb element
     * 
     * @return the part of time coverage
     * 
     * @throws MotuException the motu exception
     */
    private Date getPartOfTimeCoverage(String xmlTagName, List<JAXBElement<?>> listJaxbElement) throws MotuException {

        List<Object> listStartOrEndOrDuration = CatalogData.findJaxbElement(xmlTagName, listJaxbElement);
        Date date = null;

        for (Object o : listStartOrEndOrDuration) {
            if (!(o instanceof DateTypeFormatted)) {
                continue;
            }
            DateTypeFormatted dateTypeFormatted = (DateTypeFormatted) o;
            try {
                date = NetCdfReader.parseDate(dateTypeFormatted.getValue(), dateTypeFormatted.getFormat());
            } catch (MotuInvalidDateException e) {
                throw new MotuException(String.format("Unable to get %s time (in loadTdsTimeCoverage)", xmlTagName), e);
            }
        }
        return date;
    }

    /**
     * Load tds variables vocabulary.
     * 
     * @param datasetType the dataset type
     * @param productMetaData the product meta data
     * 
     * @throws MotuException the motu exception
     */
    private void loadTdsVariablesVocabulary(DatasetType datasetType, ProductMetaData productMetaData, CatalogData cd) throws MotuException {
        if (!cd.isLoadTDSExtraMetadata()) {
            return;
        }

        List<Object> listVariablesVocabularyObject = CatalogData.findJaxbElement(datasetType.getThreddsMetadataGroup(), Variables.class);

        productMetaData.setVariablesVocabulary(null);

        for (Object objectElt : listVariablesVocabularyObject) {

            if (!(objectElt instanceof Variables)) {
                continue;
            }
            productMetaData.setVariablesVocabulary((Variables) objectElt);

        }
    }

    /**
     * Load tds metadata property.
     * 
     * @param datasetType the dataset type
     * @param productMetaData the product meta data
     * 
     * @throws MotuException the motu exception
     */
    private void loadTdsMetadataProperty(DatasetType datasetType, ProductMetaData productMetaData, CatalogData cd) throws MotuException {
        if (!cd.isLoadTDSExtraMetadata()) {
            return;
        }

        List<Object> listProperty = CatalogData.findJaxbElement(datasetType.getThreddsMetadataGroup(), Property.class);

        productMetaData.setListTDSMetaDataProperty(null);

        for (Object objectElt : listProperty) {

            if (!(objectElt instanceof Property)) {
                continue;
            }
            productMetaData.addListTDSMetaDataProperty((Property) objectElt);

        }
    }

    /**
     * Load tds last date property (inside the metadata tag).
     * 
     * @param datasetType the dataset type
     * @param productMetaData the product meta data
     * 
     * @throws MotuException the motu exception
     */
    private void loadTdsMetadataLastDate(DatasetType datasetType, ProductMetaData productMetaData) throws MotuException {
        List<Object> listProperty = CatalogData.findJaxbElement(datasetType.getThreddsMetadataGroup(), DateTypeFormatted.class);

        productMetaData.setListTDSMetaDataProperty(null);

        for (Object objectElt : listProperty) {

            if (!(objectElt instanceof DateTypeFormatted)) {
                continue;
            }

            DateTypeFormatted date = (DateTypeFormatted) objectElt;
            productMetaData.setLastUpdate(date.getValue());
        }
    }

    /**
     * Load tds geo and depth coverage.
     * 
     * @param datasetType the dataset type
     * @param productMetaData the product meta data
     * 
     * @throws MotuException the motu exception
     */
    private void loadTdsGeoAndDepthCoverage(DatasetType datasetType, ProductMetaData productMetaData, CatalogData cd) throws MotuException {
        List<Object> listGeoCoverageObject = CatalogData.findJaxbElement(datasetType.getThreddsMetadataGroup(), GeospatialCoverage.class);

        productMetaData.setGeoBBox(null);
        // productMetaData.setNorthSouthResolution(null);
        // productMetaData.setNorthSouthUnits(null);
        // productMetaData.setEastWestResolution(null);
        // productMetaData.setEastWestUnits(null);
        // productMetaData.setDepthCoverage(null);
        // productMetaData.setDepthResolution(null);
        // productMetaData.setDepthUnits(null);

        boolean foundGeospatialCoverage = false;

        for (Object objectElt : listGeoCoverageObject) {

            if (!(objectElt instanceof GeospatialCoverage)) {
                continue;
            }

            GeospatialCoverage geospatialCoverage = (GeospatialCoverage) objectElt;

            foundGeospatialCoverage = initializeGeoAndDepthCoverage(geospatialCoverage, productMetaData);

        }

        // if there is no geo coverage for this dataset, use geo coverage of the parent datasets
        if ((!foundGeospatialCoverage) && (cd.getCurrentGeospatialCoverage() != null)) {
            initializeGeoAndDepthCoverage(cd.getCurrentGeospatialCoverage(), productMetaData);
        }

    }

    /**
     * Initialize geo and depth coverage.
     * 
     * @param geospatialCoverage the geospatial coverage
     * @param productMetaData the product meta data
     * 
     * @return true, if successful
     */
    private boolean initializeGeoAndDepthCoverage(GeospatialCoverage geospatialCoverage, ProductMetaData productMetaData) {
        if (geospatialCoverage == null) {
            return false;
        }

        // Set lat/lon coverage
        ExtractCriteriaLatLon extractCriteriaLatLon = new ExtractCriteriaLatLon(geospatialCoverage);

        LatLonRect latLonRect = extractCriteriaLatLon.getLatLonRect();
        if (latLonRect != null) {
            productMetaData.setGeoBBox(new LatLonRect(latLonRect));
        } else {
            LOGGER.info("initializeGeoAndDepthCoverage - No Lat/Lon coordinates have been set in TDS configuration file.(extractCriteriaLatLon.getLatLonRect() returns null)");
        }

        SpatialRange spatialRangeNorthSouth = geospatialCoverage.getNorthsouth();
        if (spatialRangeNorthSouth != null) {
            productMetaData.setNorthSouthResolution(CatalogData.getResolution(spatialRangeNorthSouth));
            productMetaData.setNorthSouthUnits(CatalogData.getUnits(spatialRangeNorthSouth));
        } else {
            LOGGER.info("initializeGeoAndDepthCoverage - No North/South resolution has been set in TDS configuration file.(geospatialCoverage.getNorthsouth() returns null)");
        }

        SpatialRange spatialRangeEastWest = geospatialCoverage.getEastwest();
        if (spatialRangeEastWest != null) {
            productMetaData.setEastWestResolution(CatalogData.getResolution(spatialRangeEastWest));
            productMetaData.setEastWestUnits(CatalogData.getUnits(spatialRangeEastWest));
        } else {
            LOGGER.info("initializeGeoAndDepthCoverage - No East/West resolution has been set in TDS configuration file.(geospatialCoverage.getEastwest() returns null)");
        }

        // Set depth coverage
        SpatialRange spatialRangeUpDown = geospatialCoverage.getUpdown();
        if (spatialRangeUpDown != null) {
            double min = spatialRangeUpDown.getStart();
            double max = min + spatialRangeUpDown.getSize();

            productMetaData.setDepthCoverage(new MinMax(min, max));
            productMetaData.setDepthResolution(CatalogData.getResolution(geospatialCoverage.getUpdown()));
            productMetaData.setDepthUnits(CatalogData.getUnits(geospatialCoverage.getUpdown()));

        } else {
            LOGGER.info("initializeGeoAndDepthCoverage - No Up/Down  has been set in TDS configuration file.(geospatialCoverage.getUpdown() returns null)");
        }

        return true;
    }

    /**
     * Removes n elements form the end of the list catalog ref sub paths.
     * 
     * @param numberToRemove the n elements to remove.
     */
    private void removeListCatalogRefSubPaths(CatalogData catalogData, int numberToRemove) {
        for (int i = 0; i < numberToRemove; i++) {
            catalogData.getListCatalogRefSubPaths().remove(catalogData.getListCatalogRefSubPaths().size() - 1);
        }

    }

    /**
     * .
     * 
     * @param catalog
     * @throws MotuException
     */
    public CatalogData loadTdsCatalog(CatalogService catalogService) throws MotuException {
        try {
            Catalog catalogXml = getCatalogFromTDS(catalogService);
            CatalogData catalogData = new CatalogData();
            catalogData.setUrlSite(catalogService.getUrlSite());
            catalogData.setCasAuthentication(false);
            catalogData.setTitle(catalogXml.getName());

            List<JAXBElement<? extends DatasetType>> list = catalogXml.getDataset();
            for (Iterator<JAXBElement<? extends DatasetType>> it = list.iterator(); it.hasNext();) {
                JAXBElement<? extends DatasetType> o = it.next();
                // System.out.println(o.getDeclaredType().getName());

                DatasetType datasetType = o.getValue();
                if (datasetType != null) {
                    if (datasetType instanceof CatalogRef) {
                        catalogData.setCurrentProductType("");
                        int numberSubPaths = loadTdsCatalogRef((CatalogRef) datasetType, catalogService.getCasAuthentication(), catalogData);
                        removeListCatalogRefSubPaths(catalogData, numberSubPaths);
                    } else if (datasetType instanceof DatasetType) {
                        catalogData.setCurrentProductType("");
                        List<Product> sameProductTypeDataset = new ArrayList<Product>();
                        loadTdsProducts(datasetType, catalogXml, catalogService.getCasAuthentication(), catalogData);
                        if (sameProductTypeDataset.size() > 0) {
                            catalogData.getListProductTypeDataset().add(sameProductTypeDataset);
                        }
                    }
                }

            }

            // Remove products that are not anymore in the catalog
            catalogData.productsKeySet().retainAll(catalogData.getProductsLoaded());

            return catalogData;
        } catch (Exception e) {
            throw new MotuException("Error while reading catalog from TDS", e);
        }

    }

    private Catalog getCatalogFromTDS(CatalogService catalogService) throws MalformedURLException, IOException, MotuCasException, JAXBException {
        return getCatalogFromTDS(getCatalogURL(catalogService), catalogService.getCasAuthentication());
    }

    private Catalog getCatalogFromTDS(String catalogUrl, boolean useSSO_) throws MalformedURLException, IOException, MotuCasException, JAXBException {
        Catalog catalog = null;
        URL url = new URL(getUrlWithSSO(catalogUrl, useSSO_));
        URLConnection conn = url.openConnection();
        InputStream in = conn.getInputStream();
        catalog = (Catalog) JAXBTDSModel.getInstance().getUnmarshallerTdsModel().unmarshal(in);
        in.close();
        return catalog;
    }

}
