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

import fr.cls.atoll.motu.library.inventory.Access;
import fr.cls.atoll.motu.library.inventory.CatalogOLA;
import fr.cls.atoll.motu.library.inventory.Inventory;
import fr.cls.atoll.motu.library.inventory.Resource;
import fr.cls.atoll.motu.library.inventory.ResourceOLA;
import fr.cls.atoll.motu.library.inventory.ResourcesOLA;
import fr.cls.atoll.motu.library.misc.cas.util.AssertionUtils;
import fr.cls.atoll.motu.library.misc.cas.util.AuthentificationHolder;
import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDateException;
import fr.cls.atoll.motu.library.misc.intfce.Organizer;
import fr.cls.atoll.motu.library.misc.intfce.Organizer.Format;
import fr.cls.atoll.motu.library.misc.metadata.DocMetaData;
import fr.cls.atoll.motu.library.misc.metadata.ProductMetaData;
import fr.cls.atoll.motu.library.misc.netcdf.NetCdfReader;
import fr.cls.atoll.motu.library.misc.opendap.server.Dataset;
import fr.cls.atoll.motu.library.misc.opendap.server.Service;
import fr.cls.atoll.motu.library.misc.tds.server.CatalogRef;
import fr.cls.atoll.motu.library.misc.tds.server.DatasetType;
import fr.cls.atoll.motu.library.misc.tds.server.DateTypeFormatted;
import fr.cls.atoll.motu.library.misc.tds.server.DocumentationType;
import fr.cls.atoll.motu.library.misc.tds.server.Metadata;
import fr.cls.atoll.motu.library.misc.tds.server.Property;
import fr.cls.atoll.motu.library.misc.tds.server.SpatialRange;
import fr.cls.atoll.motu.library.misc.tds.server.TimeCoverageType;
import fr.cls.atoll.motu.library.misc.tds.server.Variables;
import fr.cls.atoll.motu.library.misc.utils.ReflectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.log4j.Logger;

import ucar.ma2.MAMath.MinMax;
import ucar.unidata.geoloc.LatLonRect;

// CSOFF: MultipleStringLiterals : avoid message in constants declaration and trace log.

/**
 * This class implements a product's catalog .
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class CatalogData {

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(CatalogData.class);

    /**
     * Enumeration for available type of catalog.
     */
    public enum CatalogType {

        /** Opendap catalog. */
        OPENDAP(0),

        /** Tds catalog. */
        TDS(1),

        /** Ftp catalog (ftp, scft, griFtp). */
        FTP(2);
        
        private final int value;

        CatalogType(int v) {
            value = v;
        }

        /**
         * Value.
         * 
         * @return the int
         */
        public int value() {
            return value;
        }

          /**
           * From value.
           *
           * @param v the v
           * @return the catalog type
           */
          public static CatalogType fromValue(int v) {
            for (CatalogType c : CatalogType.values()) {
                if (c.value == v) {
                    return c;
                }
            }
            throw new IllegalArgumentException(String.valueOf(v));
        }

        /**
         * Values to string.
         *
         * @return the string
         */
        public static String valuesToString() {
            StringBuffer stringBuffer = new StringBuffer();
            for (Format c : Format.values()) {
                stringBuffer.append(c.toString());
                stringBuffer.append(" ");
            }
            return stringBuffer.toString();
        }
        
        /**
         * Gets the default.
         *
         * @return the default
         */
        public static CatalogType getDefault() {
            return TDS;
        }
        
        
    }

    /** ServiceName XML tag element. */
    static private final String XML_TAG_START = "start";

    /** ServiceName XML tag element. */
    static private final String XML_TAG_END = "end";

    /** ServiceName XML tag element. */
    static public final String XML_TAG_SERVICENAME = "serviceName";

    /** OpenDAP TDS Service Type. */
    static public final String TDS_OPENDAP_SERVICE = "opendap";

    /** DODS TDS Service Type. */
    static public final String TDS_DODS_SERVICE = "dods";

    /**
     * Default constructor.
     */
    public CatalogData() {
        init();
    }

    /**
     * Initialization.
     */
    private void init() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("init() - entering");
        }

        productsMap = new HashMap<String, Product>();

        if (LOG.isDebugEnabled()) {
            LOG.debug("init() - exiting");
        }
    }

    /**
     * Load ftp catalog.
     * 
     * @param path the path
     * 
     * @throws MotuException the motu exception
     */
    public void loadFtpCatalog(String path) throws MotuException {

        // Set to store product id that are in the catalog.
        if (productsLoaded == null) {
            productsLoaded = new HashSet<String>();
        }
        productsLoaded.clear();

        // Products map are not cleared, it always refresh :
        // - Products that have previously loaded are refresh
        // - Products that are newly inserted in the catalog are insert in the
        // products map
        // - Products that are not anymore in the catalog are removed from the
        // products map
        // clearProducts();

        CatalogOLA catalogOLA = Organizer.getCatalogOLA(path);
        // --------------------------
        // -------- Loads dataset
        // --------------------------
        ResourcesOLA resourcesOLA = catalogOLA.getResourcesOLA();

        this.title = catalogOLA.getName();

        for (ResourceOLA resourceOLA : resourcesOLA.getResourceOLA()) {
            currentProductType = resourceOLA.getUrn().toString();
            loadFtpInventory(resourceOLA.getInventoryUrl().toString());
        }

        // Remove products that are not anymore in the catalog
        productsKeySet().retainAll(productsLoaded);

    }

    /**
     * Load ftp inventory.
     * 
     * @param xmlUri the xml uri
     * 
     * @throws MotuException the motu exception
     */
    public Product loadFtpInventory(String xmlUri) throws MotuException {

        Inventory inventoryOLA = Organizer.getInventoryOLA(xmlUri);

        Resource resource = inventoryOLA.getResource();
        Access access = resource.getAccess();

        ProductMetaData productMetaData = null;

        String productId = inventoryOLA.getResource().getUrn().toString();

        boolean newProduct = true;

        Product product = getProducts(productId);

        if (product == null) {
            product = new Product(this.casAuthentification);
            productMetaData = new ProductMetaData();
            productMetaData.setProductId(productId);

        } else {
            newProduct = false;
            productMetaData = product.getProductMetaData();
        }

        product.setProductMetaData(productMetaData);

        product.setLocationMetaData(xmlUri);

        productsLoaded.add(productId);

        product.loadInventoryGlobalMetaData(inventoryOLA);

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
            throw new MotuException(String.format("Invalid URI '%s' in file '%s' at '%s.urlPath' tag.attribute", accessUri, xmlUri, access.getClass()
                    .toString()), e);
        }

        product.setLocationData(accessUri.toString());

        List<DataFile> dataFiles = CatalogData.loadFtpDataFiles(inventoryOLA);

        product.setDataFiles(dataFiles);

        if (newProduct) {
            putProducts(productMetaData.getProductId(), product);
        }

        return product;

    }

    /**
     * Load ftp data files.
     * 
     * @param inventoryOLA the inventory ola
     * 
     * @return the list< data file>
     */
    public static List<DataFile> loadFtpDataFiles(Inventory inventoryOLA) {

        if (inventoryOLA.getFiles().getFile().isEmpty()) {
            return null;
        }

        List<DataFile> dataFiles = new ArrayList<DataFile>();

        for (fr.cls.atoll.motu.library.inventory.File file : inventoryOLA.getFiles().getFile()) {
            DataFile dataFile = new DataFile();
            dataFile.setName(file.getName());
            if (file.getWeight() == null) {
                continue;
            }
            dataFile.setPath(file.getPath().toString());
            dataFile.setStartCoverageDate(file.getStartCoverageDate());
            dataFile.setEndCoverageDate(file.getEndCoverageDate());
            dataFile.setWeight(file.getWeight().doubleValue());

            dataFiles.add(dataFile);
        }

        DataFileComparator dataFileComparator = new DataFileComparator();
        Collections.sort(dataFiles, dataFileComparator);

        return dataFiles;
    }

    /**
     * Loads an Opendap catalog..
     * 
     * @param path path of the Xml catalog file.
     * 
     * @throws MotuException the motu exception
     */
    public void loadOpendapCatalog(String path) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("loadOpendapCatalog() - entering");
        }

        // Products map are not cleared, it always refresh :
        // - Products that have previously loaded are refresh
        // - Products that are newly inserted in the catalog are insert in the
        // products map
        // - Products that are not anymore in the catalog are removed from the
        // products map
        // clearProducts();

        // Set to store product id that are in the catalog.
        if (productsLoaded == null) {
            productsLoaded = new HashSet<String>();
        }
        productsLoaded.clear();

        fr.cls.atoll.motu.library.misc.opendap.server.Catalog catalogXml = loadConfigOpendap(path);
        // --------------------------
        // -------- Loads dataset
        // --------------------------
        // ignores top level dataset
        fr.cls.atoll.motu.library.misc.opendap.server.Dataset topLevelDataset = catalogXml.getDataset();

        List<Object> list = topLevelDataset.getDatasetOrCatalogRef();
        // int countDataset = list.size();
        this.title = topLevelDataset.getName();

        listProductTypeDataset = new ArrayList<List<Product>>();

        for (Iterator<Object> it = list.iterator(); it.hasNext();) {
            Object o = it.next();

            if (o != null) {
                if (o instanceof fr.cls.atoll.motu.library.misc.opendap.server.Dataset) {
                    this.currentProductType = "";
                    // this.currentProductSubTypes = null;
                    getCurrentProductSubTypes();

                    sameProductTypeDataset = new ArrayList<Product>();

                    loadOpendapProducts((fr.cls.atoll.motu.library.misc.opendap.server.Dataset) o);

                    if (sameProductTypeDataset.size() > 0) {
                        listProductTypeDataset.add(sameProductTypeDataset);
                    }

                }
            }
        }

        // Remove products that are not anymore in the catalog
        productsKeySet().retainAll(productsLoaded);

        // for(Iterator<List> it = listProductTypeDataset.iterator();
        // it.hasNext();) {
        // sameProductTypeDataset = it.next();
        // System.out.println(sameProductTypeDataset.size());
        //             
        // for(int i = 0 ; i < sameProductTypeDataset.size(); i++) {
        // Product p = sameProductTypeDataset.get(i);
        // if (i == 0) {
        // List<String> subTypes = p.getProductMetaData().getProductSubTypes();
        // System.out.println(p.getProductMetaData().getProductType());
        // int indent = 0;
        // for (Iterator<String> it2 = subTypes.iterator() ; it2.hasNext();) {
        // indent ++;
        // for (int d = 0; d < indent; d++) {
        // System.out.print("-");
        // }
        // System.out.println(it2.next());
        // }
        // }
        // System.out.println("name and location");
        // System.out.println(p.getProductMetaData().getTitle());
        // System.out.println(p.getLocationData());
        // System.out.println("--------");
        // }
        // }
        // this.currentProductType = "";
        // this.currentProductSubTypes = null;

        if (LOG.isDebugEnabled()) {
            LOG.debug("loadOpendapCatalog() - exiting");
        }
    }

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
    protected int loadTdsCatalogRef(CatalogRef catalogRef) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("loadTdsCatalogRef() - entering");
        }

        if (catalogRef == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("loadTdsCatalogRef() - exiting");
            }
            return 0;
        }
        if (catalogRef.getHref().equals("")) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("loadTdsCatalogRef() - exiting");
            }
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
        location.append(getUrlSite());
        for (String path : listCatalogRefSubPaths) {
            location.append(path);
            location.append("/");
        }
        location.append(catalogXmlName);

        loadTdsSubCatalog(location.toString());

        if (LOG.isDebugEnabled()) {
            LOG.debug("loadTdsCatalogRef() - exiting");
        }

        return catalogHrefSplit.length - 1;
    }

    /**
     * Initialize Tds catalog loading.
     */
    public void loadTdsCatalogInit() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("loadTdsCatalogInit() - entering");
        }

        // Set to store product id that are in the catalog.
        if (productsLoaded == null) {
            productsLoaded = new HashSet<String>();
        }
        productsLoaded.clear();

        listProductTypeDataset = new ArrayList<List<Product>>();
        listCatalogRefSubPaths = new ArrayList<String>();
        this.currentProductType = "";
        // this.currentProductSubTypes = null;
        getCurrentProductSubTypes();

        sameProductTypeDataset = new ArrayList<Product>();

        if (LOG.isDebugEnabled()) {
            LOG.debug("loadTdsCatalogInit() - exiting");
        }
    }

    /**
     * Loads an Tds top level catalog.
     * 
     * @param path path of the Xml top level catalog file.
     * 
     * @throws MotuException the motu exception
     */
    public void loadTdsCatalog(String path) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("loadTdsCatalog() - entering");
        }

        loadTdsCatalogInit();

        // Products map are not cleared, it always refresh :
        // - Products that have previously loaded are refresh
        // - Products that are newly inserted in the catalog are insert in the
        // products map
        // - Products that are not anymore in the catalog are removed from the
        // products map
        // clearProducts();

        fr.cls.atoll.motu.library.misc.tds.server.Catalog catalogXml = loadConfigTds(path);
        // --------------------------
        // -------- Loads dataset
        // --------------------------
        List<JAXBElement<? extends DatasetType>> list = catalogXml.getDataset();

        this.title = catalogXml.getName();

        for (Iterator<JAXBElement<? extends DatasetType>> it = list.iterator(); it.hasNext();) {
            JAXBElement<? extends DatasetType> o = it.next();
            // System.out.println(o.getDeclaredType().getName());

            DatasetType datasetType = o.getValue();
            if (datasetType != null) {
                if (datasetType instanceof CatalogRef) {
                    // System.out.println("is CatalogRef");

                    this.currentProductType = "";
                    getCurrentProductSubTypes();
                    sameProductTypeDataset = new ArrayList<Product>();

                    int numberSubPaths = loadTdsCatalogRef((CatalogRef) datasetType);
                    removeListCatalogRefSubPaths(numberSubPaths);

                } else if (datasetType instanceof DatasetType) {
                    // System.out.println("is DatasetType");

                    this.currentProductType = "";
                    getCurrentProductSubTypes();
                    sameProductTypeDataset = new ArrayList<Product>();

                    loadTdsProducts(datasetType, catalogXml);

                    if (sameProductTypeDataset.size() > 0) {
                        listProductTypeDataset.add(sameProductTypeDataset);
                    }
                }
            }

        }

        // Remove products that are not anymore in the catalog
        productsKeySet().retainAll(productsLoaded);

        // for(Iterator<List> it = listProductTypeDataset.iterator();
        // it.hasNext();) {
        // sameProductTypeDataset = it.next();
        // System.out.println(sameProductTypeDataset.size());
        //             
        // for(int i = 0 ; i < sameProductTypeDataset.size(); i++) {
        // Product p = sameProductTypeDataset.get(i);
        // if (i == 0) {
        // List<String> subTypes = p.getProductMetaData().getProductSubTypes();
        // System.out.println(p.getProductMetaData().getProductType());
        // int indent = 0;
        // for (Iterator<String> it2 = subTypes.iterator() ; it2.hasNext();) {
        // indent ++;
        // for (int d = 0; d < indent; d++) {
        // System.out.print("-");
        // }
        // System.out.println(it2.next());
        // }
        // }
        // System.out.println("name and location");
        // System.out.println(p.getProductMetaData().getTitle());
        // System.out.println(p.getLocationData());
        // System.out.println("--------");
        // }
        // }
        // this.currentProductType = "";
        // this.currentProductSubTypes = null;

        if (LOG.isDebugEnabled()) {
            LOG.debug("loadTdsCatalog() - exiting");
        }
    }

    /**
     * Loads an Tds top level catalog.
     * 
     * @param path path of the Xml top level catalog file.
     * 
     * @throws MotuException the motu exception
     */
    public void loadTdsSubCatalog(String path) throws MotuException {

        fr.cls.atoll.motu.library.misc.tds.server.Catalog catalogXml = loadConfigTds(path);
        // --------------------------
        // -------- Loads dataset
        // --------------------------
        List<JAXBElement<? extends DatasetType>> list = catalogXml.getDataset();

        for (Iterator<JAXBElement<? extends DatasetType>> it = list.iterator(); it.hasNext();) {
            JAXBElement<? extends DatasetType> o = it.next();
            // System.out.println(o.getDeclaredType().getName());

            DatasetType datasetType = o.getValue();
            if (datasetType != null) {
                if (datasetType instanceof CatalogRef) {
                    // System.out.println("is CatalogRef");
                    int numberSubPaths = loadTdsCatalogRef((CatalogRef) datasetType);
                    removeListCatalogRefSubPaths(numberSubPaths);
                } else if (datasetType instanceof DatasetType) {
                    // System.out.println("is DatasetType");
                    getCurrentProductSubTypes();

                    loadTdsProducts(datasetType, catalogXml);
                }
            }

        }

        // Remove products that are not anymore in the catalog
        productsKeySet().retainAll(productsLoaded);

        // for(Iterator<List> it = listProductTypeDataset.iterator();
        // it.hasNext();) {
        // sameProductTypeDataset = it.next();
        // System.out.println(sameProductTypeDataset.size());
        //             
        // for(int i = 0 ; i < sameProductTypeDataset.size(); i++) {
        // Product p = sameProductTypeDataset.get(i);
        // if (i == 0) {
        // List<String> subTypes = p.getProductMetaData().getProductSubTypes();
        // System.out.println(p.getProductMetaData().getProductType());
        // int indent = 0;
        // for (Iterator<String> it2 = subTypes.iterator() ; it2.hasNext();) {
        // indent ++;
        // for (int d = 0; d < indent; d++) {
        // System.out.print("-");
        // }
        // System.out.println(it2.next());
        // }
        // }
        // System.out.println("name and location");
        // System.out.println(p.getProductMetaData().getTitle());
        // System.out.println(p.getLocationData());
        // System.out.println("--------");
        // }
        // }
        // this.currentProductType = "";
        // this.currentProductSubTypes = null;

    }

    /**
     * Loads Opendap products from Opendap catalog.
     * 
     * @param dataset dataset (from Opendap catalog) from which information will be loaded Dataset contains a
     *            list of datasets, recursivity is use to load.
     * 
     * @throws MotuException the motu exception
     */
    private void loadOpendapProducts(Dataset dataset) throws MotuException {
        if (dataset == null) {
            return;
        }

        // When Url path of the dataset is not null or not empty,
        // we are at the last level --> Create Product and add to the list.
        if (dataset.getUrlPath() != null) {
            if (!dataset.getUrlPath().equals("")) {

                initializeProductFromOpendap(dataset);

                return;
            }
        }

        // Saves - the product type at the top level product (on the first call,
        // level 0)
        // - the product sub-type, if not top level product
        // (type or sub-type correspond to the dataser name)
        if (currentProductType.equals("")) {
            currentProductType = dataset.getName();
        } else {
            getCurrentProductSubTypes().add(dataset.getName());
        }

        List<Object> list = dataset.getDatasetOrCatalogRef();

        for (Iterator<Object> it = list.iterator(); it.hasNext();) {
            Object o = it.next();

            if (o != null) {
                if (o instanceof Dataset) {

                    loadOpendapProducts((Dataset) o);

                }
            }
        }
        if (sameProductTypeDataset.size() > 0) {
            listProductTypeDataset.add(sameProductTypeDataset);
            sameProductTypeDataset = new ArrayList<Product>();
        }

        int last = currentProductSubTypes.size() - 1;
        if (last >= 0) {
            currentProductSubTypes.remove(last);
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
    private void loadTdsProducts(DatasetType datasetType, fr.cls.atoll.motu.library.misc.tds.server.Catalog catalogXml) throws MotuException {
        if (datasetType == null) {
            return;
        }

        // When Url path of the dataset is not null or not empty,
        // we are at the last level --> Create Product and add to the list.
        if (datasetType.getUrlPath() != null) {
            if (!datasetType.getUrlPath().equals("")) {

                initializeProductFromTds(datasetType, catalogXml);

                return;
            }
        }

        currentGeospatialCoverage = findTdsGeoAndDepthCoverage(datasetType);

        // Saves - the product type at the top level product (on the first call,
        // level 0)
        // - the product sub-type, if not top level product
        // (type or sub-type correspond to the dataser name)
        if (currentProductType.equals("")) {
            currentProductType = datasetType.getName();
        } else {
            getCurrentProductSubTypes().add(datasetType.getName());
        }

        List<JAXBElement<? extends DatasetType>> list = datasetType.getDataset();

        for (Iterator<JAXBElement<? extends DatasetType>> it = list.iterator(); it.hasNext();) {
            JAXBElement<? extends DatasetType> o = it.next();

            DatasetType datasetTypeChild = o.getValue();
            if (datasetTypeChild != null) {
                if (datasetTypeChild instanceof fr.cls.atoll.motu.library.misc.tds.server.CatalogRef) {
                    // System.out.println("is CatalogRef");
                    int numberSubPaths = loadTdsCatalogRef((CatalogRef) datasetTypeChild);
                    removeListCatalogRefSubPaths(numberSubPaths);

                } else if (datasetType instanceof fr.cls.atoll.motu.library.misc.tds.server.DatasetType) {
                    // System.out.println("is DatasetType");
                    loadTdsProducts(datasetTypeChild, catalogXml);
                }
            }
        }

        currentGeospatialCoverage = null;

        if (sameProductTypeDataset.size() > 0) {
            listProductTypeDataset.add(sameProductTypeDataset);
            sameProductTypeDataset = new ArrayList<Product>();
        }

        int last = currentProductSubTypes.size() - 1;
        if (last >= 0) {
            currentProductSubTypes.remove(last);
        }

    }

    /**
     * Initializes product and its metadata from an Opendap dataset.
     * 
     * @param dataset Opendap dataset which have a non-empty url path.
     * 
     * @throws MotuException the motu exception
     */
    private void initializeProductFromOpendap(Dataset dataset) throws MotuException {

        if (dataset == null) {
            throw new MotuException("Error in intializeProductFromOpendap - Opendap dataset is null");
        }
        if (dataset.getUrlPath() == null) {
            throw new MotuException("Error in intializeProductFromOpendap - Invalid dataset branch - Opendap dataset has a null url path");
        }
        if (dataset.getUrlPath().equals("")) {
            throw new MotuException("Error in intializeProductFromOpendap - Invalid dataset branch - Opendap dataset has an empty url path");
        }

        String productId = "";
        boolean newProduct = true;

        if (dataset.getID() != null) {
            productId = dataset.getID();
        } else {
            productId = dataset.getUrlPath();
        }

        ProductMetaData productMetaData = null;

        Product product = getProducts(productId);
        if (product == null) {
            product = new Product(this.casAuthentification);
            productMetaData = new ProductMetaData();
            productMetaData.setProductId(productId);
        } else {
            newProduct = false;
            productMetaData = product.getProductMetaData();
        }

        productsLoaded.add(productId);

        productMetaData.setProductType(currentProductType);

        List<String> productSubTypesWork = new ArrayList<String>();
        productSubTypesWork.addAll(currentProductSubTypes);

        productMetaData.setProductSubTypes(productSubTypesWork);
        productMetaData.setTitle(dataset.getName());

        // Loads href documentation of the dataset
        loadOpendapHrefDoc(dataset, productMetaData);

        product.setProductMetaData(productMetaData);

        StringBuffer locationData = new StringBuffer();
        locationData.append(urlSite);
        locationData.append(dataset.getUrlPath());
        product.setLocationData(locationData.toString());

        if (newProduct) {
            putProducts(productMetaData.getProductId(), product);
        }
        sameProductTypeDataset.add(product);
        // reinitialize
        // currentProductMetaData = null;

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
    private String getUrlOpendapFromTds(DatasetType datasetType, fr.cls.atoll.motu.library.misc.tds.server.Catalog catalogXml, Product product)
            throws MotuException {
        // Search Opendap service for this dataset and get its url
        // Don't use getServiceName --> deprecated - see XML Sschema InvCatalog.1.0.n.xsd
        // String tdsServiceName = datasetType.getServiceName();

        String tdsServiceName = "";
        //List<Object> listServiceNameObject = CatalogData.findJaxbElement(datasetType.getThreddsMetadataGroup(), XML_TAG_SERVICENAME);
        String xmlNamespace = ReflectionUtils.getXmlSchemaNamespace(datasetType.getClass());
        StringBuffer xPath = new StringBuffer();
        xPath.append("//threddsMetadataGroup[name='{");
        xPath.append(xmlNamespace);
        xPath.append("}serviceName']/value");
        
        //List<Object> listServiceNameObject = CatalogData.findJaxbElementUsingJXPath(datasetType, "//threddsMetadataGroup[name='{http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0}serviceName']/value");
        List<Object> listServiceNameObject = CatalogData.findJaxbElementUsingJXPath(datasetType, xPath.toString());
        
        
        for (Object objectElt : listServiceNameObject) {
            if (!(objectElt instanceof String)) {
                continue;
            }
            tdsServiceName = (String) objectElt;
            break;
        }

        if (tdsServiceName.equals("")) {
            throw new MotuException(String.format("Error in getUrlOpendapFromTds - No TDS service found in TDS catalog for dataset '%s' ",
                                                  datasetType.getName()));
        }
        // search 'opendap' seruce type. If not found search 'dods' service type.
        fr.cls.atoll.motu.library.misc.tds.server.Service tdsService = findTdsService(tdsServiceName, TDS_OPENDAP_SERVICE, catalogXml.getService());

        if (tdsService == null) {
            tdsService = findTdsService(tdsServiceName, TDS_DODS_SERVICE, catalogXml.getService());
        }

        if (tdsService == null) {
            throw new MotuException(
                    String
                            .format("Error in getUrlOpendapFromTds - TDS service '%s' found in TDS catalog for dataset '%s' has neither 'opendap' nor 'dods' service type",
                                    tdsServiceName,
                                    datasetType.getName()));
        }
        String relativeUrl = tdsService.getBase();
        URI uri = URI.create(urlSite);
        URI opendapUri = null;
        try {
            opendapUri = new URI(uri.getScheme(), uri.getAuthority(), relativeUrl, null, null);
        } catch (URISyntaxException e) {
            throw new MotuException(String.format("Error in getUrlOpendapFromTds - Uri creation: scheme='%s', authority='%s', path='%s'", uri
                    .getScheme(), uri.getAuthority(), relativeUrl), e);
        }

        StringBuffer locationData = new StringBuffer();
        locationData.append(opendapUri.toString());
        locationData.append(datasetType.getUrlPath());

        product.setLocationData(locationData.toString());
        product.setTdsServiceType(tdsService.getServiceType().toLowerCase());

        return opendapUri.toString();
    }

    /**
     * Initializes product and its metadata from an Tds dataset.
     * 
     * @param catalogXml Tds catalog
     * @param datasetType Tds dataset which have a non-empty url path.
     * 
     * @throws MotuException the motu exception
     */
    private void initializeProductFromTds(DatasetType datasetType, fr.cls.atoll.motu.library.misc.tds.server.Catalog catalogXml) throws MotuException {

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
        boolean newProduct = true;

        if (datasetType.getID() != null) {
            if (datasetType.getID().equals("")) {
                productId = datasetType.getUrlPath();
            } else {
                productId = datasetType.getID();
            }
        } else {
            productId = datasetType.getUrlPath();
        }

        ProductMetaData productMetaData = null;

        Product product = getProducts(productId);
        if (product == null) {
            product = new Product(this.casAuthentification);
            productMetaData = new ProductMetaData();
            productMetaData.setProductId(productId);
        } else {
            newProduct = false;
            productMetaData = product.getProductMetaData();
        }

        productsLoaded.add(productId);

        productMetaData.setProductType(currentProductType);

        List<String> productSubTypesWork = new ArrayList<String>();
        productSubTypesWork.addAll(currentProductSubTypes);

        productMetaData.setProductSubTypes(productSubTypesWork);
        productMetaData.setTitle(datasetType.getName());

        // Loads href documentation of the dataset
        loadTdsHrefDoc(datasetType, productMetaData);

        // Loads time coverage of the dataset
        loadTdsTimeCoverage(datasetType, productMetaData);

        // Loads geo and depth coverage
        loadTdsGeoAndDepthCoverage(datasetType, productMetaData);

        // Loads Variables vocabulary
        loadTdsVariablesVocabulary(datasetType, productMetaData);

        // Loads Property meatadata
        loadTdsMetadataProperty(datasetType, productMetaData);

        product.setProductMetaData(productMetaData);

        // // Get Opendap (Dods) url of the dataset.
        // StringBuffer locationData = new StringBuffer();
        // locationData.append(getUrlOpendapFromTds(datasetType, catalogXml));
        // locationData.append(datasetType.getUrlPath());
        //
        // product.setLocationData(locationData.toString());

        getUrlOpendapFromTds(datasetType, catalogXml, product);

        if (newProduct) {
            putProducts(productMetaData.getProductId(), product);
        }
        sameProductTypeDataset.add(product);
        // reinitialize
        // currentProductMetaData = null;

    }

    /**
     * Searches a TDS service from a service name into a list of TDS service.
     * 
     * @param tdsServiceName service name to search
     * @param listTdsService list of TDS services
     * 
     * @return a Service object if found, otherwhise null.
     */
    private fr.cls.atoll.motu.library.misc.tds.server.Service findTdsService(String tdsServiceName,
                                                                             List<fr.cls.atoll.motu.library.misc.tds.server.Service> listTdsService) {

        fr.cls.atoll.motu.library.misc.tds.server.Service serviceFound = null;

        for (fr.cls.atoll.motu.library.misc.tds.server.Service service : listTdsService) {
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
     * Searches a TDS service from a service type into a list of TDS service.
     * 
     * @param tdsServiceType service type to search
     * @param listTdsService list of TDS services
     * 
     * @return a Service object if found, otherwhise null.
     */
    private fr.cls.atoll.motu.library.misc.tds.server.Service findTdsServiceType(String tdsServiceType,
                                                                                 List<fr.cls.atoll.motu.library.misc.tds.server.Service> listTdsService) {

        fr.cls.atoll.motu.library.misc.tds.server.Service serviceFound = null;

        for (fr.cls.atoll.motu.library.misc.tds.server.Service service : listTdsService) {
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
     * Searches a TDS service from a service name and a service type into a list of TDS service. Service name
     * can be a compounded services, search is made also in child services
     * 
     * @param tdsServiceName service name to search
     * @param tdsServiceType service type to search (see InvCatalog.1.0.xsd)
     * @param listTdsService list of TDS services
     * 
     * @return a Service object if found, otherwhise null.
     */
    private fr.cls.atoll.motu.library.misc.tds.server.Service findTdsService(String tdsServiceName,
                                                                             String tdsServiceType,
                                                                             List<fr.cls.atoll.motu.library.misc.tds.server.Service> listTdsService) {

        // search service with name
        fr.cls.atoll.motu.library.misc.tds.server.Service serviceFound = findTdsService(tdsServiceName, listTdsService);

        if (serviceFound == null) {
            return null;
        }

        if (serviceFound.getServiceType().equalsIgnoreCase(tdsServiceType)) {
            return serviceFound;
        }

        // search service with type
        List<fr.cls.atoll.motu.library.misc.tds.server.Service> listTdsServiceChild = serviceFound.getService();

        serviceFound = findTdsServiceType(tdsServiceType, listTdsServiceChild);

        return serviceFound;

    }

    /**
     * Searches objects from a jaxbElement object list according to a specific class .
     * 
     * @param listObject list in which one searches
     * @param classObject class to search
     * 
     * @return a list that contains object corresponding to classObject parameter (can be empty)
     */
    static public List<Object> findJaxbElement(List<Object> listObject, Class<?> classObject) {

        if (listObject == null) {
            return null;
        }

        List<Object> listObjectFound = new ArrayList<Object>();

        for (Object elt : listObject) {
            if (elt == null) {
                continue;
            }

            if (classObject.isInstance(elt)) {
                listObjectFound.add(elt);
            }

            if (!(elt instanceof JAXBElement)) {
                continue;
            }

            JAXBElement<?> jabxElement = (JAXBElement<?>) elt;

            // System.out.println(jabxElement.getClass().getName());
            // System.out.println(jabxElement.getDeclaredType().getName());

            Object objectElt = jabxElement.getValue();

            if (classObject.isInstance(objectElt)) {
                listObjectFound.add(objectElt);
            }
        }

        return listObjectFound;

    }
    static public List<Object> findJaxbElementUsingJXPath(Object object, String xPath) {

        List<Object> listObjectFound = new ArrayList<Object>();
        
        JXPathContext context = JXPathContext.newContext(object);
        context.setLenient(true);
        //Object oo = context.getValue("//threddsMetadataGroup[name='{http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0}serviceName']/value");
        //Object oo = context.getValue("//threddsMetadataGroup[name='{http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0}serviceName']/value");
        //Iterator it = context.iterate("//threddsMetadataGroup[name='{http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0}serviceName']/value");
        Iterator<?> it = context.iterate(xPath);
        while (it.hasNext()) {
            listObjectFound.add(it.next());
        }
        return listObjectFound;
    }
    /**
     * Search object from a jaxbElement object list according to a specific tag name.
     * 
     * @param listObject list in which one searches
     * @param tagName tag name to search (ignore case)
     * 
     * @return a list that contains object corresponding to tagName parameter (can be empty)
     */
    static public List<Object> findJaxbElement(List<Object> listObject, String tagName) {

        List<Object> listObjectFound = new ArrayList<Object>();
        
        for (Object elt : listObject) {
            if (elt == null) {
                continue;
            }
            if (!(elt instanceof JAXBElement)) {
                continue;
            }

            JAXBElement<?> jabxElement = (JAXBElement<?>) elt;

            if (!jabxElement.getName().getLocalPart().equalsIgnoreCase(tagName)) {
                continue;
            }

            listObjectFound.add(jabxElement.getValue());
        }

        return listObjectFound;

    }

    /**
     * Find jaxb element.
     * 
     * @param tagName the tag name
     * @param listJaxbElement the list jaxb element
     * 
     * @return the list< object>
     */
    static public List<Object> findJaxbElement(String tagName, List<JAXBElement<?>> listJaxbElement) {

        List<Object> listObjectFound = new ArrayList<Object>();

        for (Object elt : listJaxbElement) {
            if (elt == null) {
                continue;
            }
            if (!(elt instanceof JAXBElement)) {
                continue;
            }

            JAXBElement<?> jabxElement = (JAXBElement<?>) elt;

            if (!jabxElement.getName().getLocalPart().equalsIgnoreCase(tagName)) {
                continue;
            }

            listObjectFound.add(jabxElement.getValue());
        }

        return listObjectFound;

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
     * Load tds time coverage.
     * 
     * @param datasetType the dataset type
     * @param productMetaData the product meta data
     * 
     * @throws MotuException the motu exception
     */
    private void loadTdsTimeCoverage(DatasetType datasetType, ProductMetaData productMetaData) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("loadTdsTimeCoverage(DatasetType, ProductMetaData) - entering");
        }

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

        if (LOG.isDebugEnabled()) {
            LOG.debug("loadTdsTimeCoverage(DatasetType, ProductMetaData) - exiting");
        }
    }

    /**
     * Load tds variables vocabulary.
     * 
     * @param datasetType the dataset type
     * @param productMetaData the product meta data
     * 
     * @throws MotuException the motu exception
     */
    private void loadTdsVariablesVocabulary(DatasetType datasetType, ProductMetaData productMetaData) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("loadTdsVariableVocabulary(DatasetType, ProductMetaData) - entering");
        }

        if (!isLoadTDSExtraMetadata()) {
            LOG.debug("loadTdsVariablesVocabulary is not processed because 'loadTDSExtraMetadata' flag is set to false - exiting");
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

        if (LOG.isDebugEnabled()) {
            LOG.debug("loadTdsVariableVocabulary(DatasetType, ProductMetaData) - exiting");
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
    private void loadTdsMetadataProperty(DatasetType datasetType, ProductMetaData productMetaData) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("loadTdsMetadataProperty(DatasetType, ProductMetaData) - entering");
        }

        if (!isLoadTDSExtraMetadata()) {
            LOG.debug("loadTdsMetadataProperty is not processed because 'loadTDSExtraMetadata' flag is set to false - exiting");
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

        if (LOG.isDebugEnabled()) {
            LOG.debug("loadTdsMetadataProperty(DatasetType, ProductMetaData) - exiting");
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
    private void loadTdsGeoAndDepthCoverage(DatasetType datasetType, ProductMetaData productMetaData) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("loadTdsGeoCoverage(DatasetType, ProductMetaData) - entering");
        }

        List<Object> listGeoCoverageObject = CatalogData.findJaxbElement(datasetType.getThreddsMetadataGroup(),
                                                                         fr.cls.atoll.motu.library.misc.tds.server.GeospatialCoverage.class);

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

            if (!(objectElt instanceof fr.cls.atoll.motu.library.misc.tds.server.GeospatialCoverage)) {
                continue;
            }

            fr.cls.atoll.motu.library.misc.tds.server.GeospatialCoverage geospatialCoverage = (fr.cls.atoll.motu.library.misc.tds.server.GeospatialCoverage) objectElt;

            foundGeospatialCoverage = initializeGeoAndDepthCoverage(geospatialCoverage, productMetaData);

        }

        // if there is no geo coverage for this dataset, use geo coverage of the parent datasets
        if ((!foundGeospatialCoverage) && (currentGeospatialCoverage != null)) {

            initializeGeoAndDepthCoverage(currentGeospatialCoverage, productMetaData);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("loadTdsGeoCoverage(DatasetType, ProductMetaData) - exiting");
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
    private boolean initializeGeoAndDepthCoverage(fr.cls.atoll.motu.library.misc.tds.server.GeospatialCoverage geospatialCoverage,
                                                  ProductMetaData productMetaData) {
        if (geospatialCoverage == null) {
            return false;
        }

        // Set lat/lon coverage
        ExtractCriteriaLatLon extractCriteriaLatLon = new ExtractCriteriaLatLon(geospatialCoverage);
        productMetaData.setGeoBBox(new LatLonRect(extractCriteriaLatLon.getLatLonRect()));

        productMetaData.setNorthSouthResolution(CatalogData.getResolution(geospatialCoverage.getNorthsouth()));
        productMetaData.setNorthSouthUnits(CatalogData.getUnits(geospatialCoverage.getNorthsouth()));

        productMetaData.setEastWestResolution(CatalogData.getResolution(geospatialCoverage.getEastwest()));
        productMetaData.setEastWestUnits(CatalogData.getUnits(geospatialCoverage.getEastwest()));

        // Set depth coverage
        SpatialRange spatialRangeUpDown = geospatialCoverage.getUpdown();
        if (spatialRangeUpDown != null) {
            double min = spatialRangeUpDown.getStart();
            double max = min + spatialRangeUpDown.getSize();

            productMetaData.setDepthCoverage(new MinMax(min, max));
        }

        productMetaData.setDepthResolution(CatalogData.getResolution(geospatialCoverage.getUpdown()));
        productMetaData.setDepthUnits(CatalogData.getUnits(geospatialCoverage.getUpdown()));

        return true;
    }

    /**
     * Gets the resolution.
     * 
     * @param spatialRange the spatial range
     * 
     * @return the resolution
     */
    public static Double getResolution(SpatialRange spatialRange) {
        if (spatialRange == null) {
            return null;
        }
        return spatialRange.getResolution();

    }

    /**
     * Gets the units.
     * 
     * @param spatialRange the spatial range
     * 
     * @return the units
     */
    public static String getUnits(SpatialRange spatialRange) {
        if (spatialRange == null) {
            return null;
        }
        return spatialRange.getUnits();

    }

    /**
     * Find tds geo and depth coverage.
     * 
     * @param datasetType the dataset type
     * 
     * @return the fr.cls.atoll.motu.library.misc.tds.server. geospatial coverage
     * 
     * @throws MotuException the motu exception
     */
    private fr.cls.atoll.motu.library.misc.tds.server.GeospatialCoverage findTdsGeoAndDepthCoverage(DatasetType datasetType) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findTdsGeoAndDepthCoverage(DatasetType) - entering");
        }

        fr.cls.atoll.motu.library.misc.tds.server.GeospatialCoverage geospatialCoverage = null;

        List<Object> listMetadataObject = CatalogData.findJaxbElement(datasetType.getThreddsMetadataGroup(),
                                                                      fr.cls.atoll.motu.library.misc.tds.server.Metadata.class);
        if (listMetadataObject == null) {
            return geospatialCoverage;
        }
        for (Object objectMetadataElt : listMetadataObject) {

            if (!(objectMetadataElt instanceof fr.cls.atoll.motu.library.misc.tds.server.Metadata)) {
                continue;
            }
            Metadata metadata = (Metadata) objectMetadataElt;
            List<Object> listGeoCoverageObject = CatalogData.findJaxbElement(metadata.getThreddsMetadataGroup(),
                                                                             fr.cls.atoll.motu.library.misc.tds.server.GeospatialCoverage.class);
            for (Object objectElt : listGeoCoverageObject) {

                if (!(objectElt instanceof fr.cls.atoll.motu.library.misc.tds.server.GeospatialCoverage)) {
                    continue;
                }

                geospatialCoverage = (fr.cls.atoll.motu.library.misc.tds.server.GeospatialCoverage) objectElt;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("findTdsGeoAndDepthCoverage(DatasetType) - exiting");
        }
        return geospatialCoverage;

    }

    /**
     * Loads Opendap products documentations from Opendap catalog.
     * 
     * @param dataset dataset (from Opendap catalog) from which information will be loaded
     * @param productMetaData metadata in which to record href documentation
     */
    private void loadOpendapHrefDoc(Dataset dataset, ProductMetaData productMetaData) {
        List<Object> list = dataset.getServiceOrDocumentationOrMetadata();

        if (productMetaData.getDocumentations() == null) {
            productMetaData.setDocumentations(new ArrayList<DocMetaData>());
        }
        productMetaData.clearDocumentations();

        for (Iterator<Object> it = list.iterator(); it.hasNext();) {
            Object o = it.next();
            if (o == null) {
                continue;
            }
            if (!(o instanceof Service)) {
                continue;
            }
            Service service = (Service) o;

            String serviceName = service.getName();
            if (serviceName == null) {
                continue;
            }
            String base = service.getBase();
            if (base.equals("")) {
                continue;
            }
            DocMetaData docMetaData = new DocMetaData();
            docMetaData.setResource(base);
            docMetaData.setTitle(serviceName.toLowerCase());

            productMetaData.addDocumentations(docMetaData);
        }
    }

    /**
     * Loads et creates objects from an Xml Opendap catalog configuration file.
     * 
     * @param path path of the Xml catalog file.
     * 
     * @return a Catalog object corresponding to the Xml file.
     * 
     * @throws MotuException the motu exception
     */
    private fr.cls.atoll.motu.library.misc.opendap.server.Catalog loadConfigOpendap(String path) throws MotuException {

        InputStream in;

        fr.cls.atoll.motu.library.misc.opendap.server.Catalog catalogXml;
        try {
            // JAXBContext jc = JAXBContext.newInstance(OPENDAP_SCHEMA_PACK_NAME);
            // Unmarshaller unmarshaller = jc.createUnmarshaller();
            URL url = new URL(path);
            URLConnection conn = url.openConnection();
            in = conn.getInputStream();
            synchronized (Organizer.getUnmarshallerOpendapConfig()) {
                catalogXml = (fr.cls.atoll.motu.library.misc.opendap.server.Catalog) Organizer.getUnmarshallerOpendapConfig().unmarshal(in);
            }
        } catch (Exception e) {
            throw new MotuException("Error in loadConfigOpendap", e);
        }
        if (catalogXml == null) {
            throw new MotuException(String.format("Unable to load Opendap configuration (in loadConfigOpendap, cataloXml is null) - url : %s", path));
        }
        try {
            in.close();
        } catch (IOException io) {
            io.getMessage();
        }

        return catalogXml;
    }

    /**
     * Loads et creates objects from an Xml TDS catalog configuration file.
     * 
     * @param path path of the Xml catalog file.
     * 
     * @return a Catalog object corresponding to the Xml file.
     * 
     * @throws MotuException the motu exception
     */
    private fr.cls.atoll.motu.library.misc.tds.server.Catalog loadConfigTds(String path) throws MotuException {

        InputStream in;

        fr.cls.atoll.motu.library.misc.tds.server.Catalog catalogXml;
        try {
            // JAXBContext jc = JAXBContext.newInstance(TDS_SCHEMA_PACK_NAME);
            // Unmarshaller unmarshaller = jc.createUnmarshaller();

            String newPath = path;
            if (casAuthentification) {
                newPath = AssertionUtils.addCASTicket(path);
                if (!AssertionUtils.hasCASTicket(newPath)) {
                    newPath = AssertionUtils.addCASTicket(path, AuthentificationHolder.getUser());
                    // throw new MotuException(
                    // "Unable to load TDS configuration. TDS has been declared as CASified, but the Motu application is not. \nTo access this TDS, the Motu Application must be CASified.");
                }
            } else {
                newPath = path;
            }

            URL url = new URL(newPath);
            URLConnection conn = url.openConnection();
            in = conn.getInputStream();
            synchronized (Organizer.getUnmarshallerTdsConfig()) {
                catalogXml = (fr.cls.atoll.motu.library.misc.tds.server.Catalog) Organizer.getUnmarshallerTdsConfig().unmarshal(in);
            }
        } catch (Exception e) {
            throw new MotuException("Error in loadConfigTds", e);
        }
        if (catalogXml == null) {
            throw new MotuException(String.format("Unable to load Tds configuration (in loadConfigTds, cataloXml is null) - url : %s", path));
        }
        try {
            in.close();
        } catch (IOException io) {
            io.getMessage();
        }

        return catalogXml;
    }

    /**
     * Removes n elements form the end of the list catalog ref sub paths.
     * 
     * @param numberToRemove the n elements to remove.
     */
    public void removeListCatalogRefSubPaths(int numberToRemove) {
        for (int i = 0; i < numberToRemove; i++) {
            listCatalogRefSubPaths.remove(listCatalogRefSubPaths.size() - 1);
        }

    }

    /**
     * Initialize product's collection.
     * 
     * @param catalogLocation URL of a XML file containing the catalog.
     */
    public void listProducts(String catalogLocation) {

    }

    /** The catalog title. */
    private String title = "";

    /**
     * Gets the title.
     * 
     * @return the title
     * 
     * @uml.property name="title"
     */

    public String getTitle() {
        return this.title;
    }

    /**
     * Sets the title.
     * 
     * @param title the title to set
     * 
     * @uml.property name="title"
     */
    public void setTitle(String title) {
        this.title = title;
    }

    fr.cls.atoll.motu.library.misc.tds.server.GeospatialCoverage currentGeospatialCoverage = null;

    /** The current product. */
    private Product currentProduct = null;

    /**
     * Gets the current product.
     * 
     * @return the currentProduct.
     * 
     * @uml.property name="currentProduct"
     */
    public Product getCurrentProduct() {
        return this.currentProduct;
    }

    /**
     * Sets the current product.
     * 
     * @param currentProduct the currentProduct to set.
     * 
     * @uml.property name="currentProduct"
     */
    public void setCurrentProduct(Product currentProduct) {
        this.currentProduct = currentProduct;
    }

    /** The current product metadata. */
    private ProductMetaData currentProductMetaData = null;

    /**
     * Gets the current product meta data.
     * 
     * @return the currentProductMetaData
     * 
     * @uml.property name="currentProductMetaData"
     */
    public ProductMetaData getCurrentProductMetaData() {
        return this.currentProductMetaData;
    }

    /**
     * Sets the current product meta data.
     * 
     * @param currentProductMetaData the currentProductMetaData to set
     * 
     * @uml.property name="currentProductMetaData"
     */
    public void setCurrentProductMetaData(ProductMetaData currentProductMetaData) {
        this.currentProductMetaData = currentProductMetaData;
    }

    /** The current product type. */
    private String currentProductType = "";

    /**
     * Gets the current product type.
     * 
     * @return the currentProductType
     * 
     * @uml.property name="currentProductType"
     */
    protected String getCurrentProductType() {
        return this.currentProductType;
    }

    /**
     * Sets the current product type.
     * 
     * @param currentProductType the currentProductType to set
     * 
     * @uml.property name="currentProductType"
     */
    protected void setCurrentProductType(String currentProductType) {
        this.currentProductType = currentProductType;
    }

    /** The current product sub-types. */
    private List<String> currentProductSubTypes = null;

    /**
     * Gets the current product sub types.
     * 
     * @return the currentProductSubTypes
     */
    protected List<String> getCurrentProductSubTypes() {
        if (currentProductSubTypes == null) {
            currentProductSubTypes = new ArrayList<String>();
        }

        return this.currentProductSubTypes;
    }

    /**
     * Sets the current product sub types.
     * 
     * @param currentProductSubTypes the currentProductSubTypes to set
     */
    protected void setCurrentProductSubTypes(List<String> currentProductSubTypes) {
        this.currentProductSubTypes = currentProductSubTypes;
    }

    /** List contains lists of products from the catalog, group product of the same type/subtypes. */
    protected ArrayList<List<Product>> listProductTypeDataset = null;

    /**
     * Gets the list product type dataset.
     * 
     * @return the listProductTypeDataset
     */
    public List<List<Product>> getListProductTypeDataset() {
        return this.listProductTypeDataset;
    }

    /**
     * Sets the list product type dataset.
     * 
     * @param listProductTypeDataset the listProductTypeDataset to set
     */
    public void setListProductTypeDataset(ArrayList<List<Product>> listProductTypeDataset) {
        this.listProductTypeDataset = listProductTypeDataset;
    }

    /** List contains products from the catalog, which have the same type/subtypes. */
    protected List<Product> sameProductTypeDataset = null;

    /**
     * Gets the same product type dataset.
     * 
     * @return the sameProductTypeDataset
     */
    public List<Product> getSameProductTypeDataset() {
        return this.sameProductTypeDataset;
    }

    /**
     * Sets the same product type dataset.
     * 
     * @param sameProductTypeDataset the sameProductTypeDataset to set
     */
    public void setSameProductTypeDataset(List<Product> sameProductTypeDataset) {
        this.sameProductTypeDataset = sameProductTypeDataset;
    }

    /** The products map. */
    private Map<String, Product> productsMap;

    /**
     * Getter of the property <tt>products</tt>.
     * 
     * @return Returns the productsMap.
     * 
     * @uml.property name="products"
     */
    public Map<String, Product> getProducts() {
        return this.productsMap;
    }

    /**
     * Returns a set view of the keys contained in this map.
     * 
     * @return a set view of the keys contained in this map.
     * 
     * @see java.util.Map#keySet()
     * @uml.property name="products"
     */
    public Set<String> productsKeySet() {
        return this.productsMap.keySet();
    }

    /**
     * Returns a collection view of the values contained in this map.
     * 
     * @return a collection view of the values contained in this map.
     * 
     * @see java.util.Map#values()
     * @uml.property name="products"
     */
    public Collection<Product> productsValues() {
        return this.productsMap.values();
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified key.
     * 
     * @param key key whose presence in this map is to be tested.
     * 
     * @return <tt>true</tt> if this map contains a mapping for the specified key.
     * 
     * @see java.util.Map#containsKey(Object)
     * @uml.property name="products"
     */
    public boolean productsContainsKey(String key) {
        return this.productsMap.containsKey(key);
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the specified value.
     * 
     * @param value value whose presence in this map is to be tested.
     * 
     * @return <tt>true</tt> if this map maps one or more keys to the specified value.
     * 
     * @see java.util.Map#containsValue(Object)
     * @uml.property name="products"
     */
    public boolean productsContainsValue(Product value) {
        return this.productsMap.containsValue(value);
    }

    /**
     * Returns the value to which this map maps the specified key.
     * 
     * @param key key whose associated value is to be returned.
     * 
     * @return the value to which this map maps the specified key, or <tt>null</tt> if the map contains no
     *         mapping for this key.
     * 
     * @see java.util.Map#get(Object)
     * @uml.property name="products"
     */
    public Product getProducts(String key) {
        if (key == null) {
            return null;
        }
        return this.productsMap.get(key.trim());
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     * 
     * @return <tt>true</tt> if this map contains no key-value mappings.
     * 
     * @see java.util.Map#isEmpty()
     * @uml.property name="products"
     */
    public boolean isProductsEmpty() {
        return this.productsMap.isEmpty();
    }

    /**
     * Returns the number of key-value mappings in this map.
     * 
     * @return the number of key-value mappings in this map.
     * 
     * @see java.util.Map#size()
     * @uml.property name="products"
     */
    public int productsSize() {
        return this.productsMap.size();
    }

    /**
     * Setter of the property <tt>products</tt>.
     * 
     * @param value the productsMap to set.
     * 
     * @uml.property name="products"
     */
    public void setProducts(Map<String, Product> value) {
        this.productsMap = value;
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
    public Product putProducts(String key, Product value) {
        if (key == null) {
            return null;
        }
        return this.productsMap.put(key.trim(), value);
    }

    /**
     * Removes the mapping for this key from this map if it is present (optional operation).
     * 
     * @param key key whose mapping is to be removed from the map.
     * 
     * @return previous value associated with specified key, or <tt>null</tt> if there was no mapping for key.
     * 
     * @see java.util.Map#remove(Object)
     * @uml.property name="products"
     */
    public Product removeProducts(String key) {
        return this.productsMap.remove(key);
    }

    /**
     * Removes all mappings from this map (optional operation).
     * 
     * @see java.util.Map#clear()
     * @uml.property name="products"
     */
    public void clearProducts() {
        this.productsMap.clear();
    }

    /** List contains each ath element to a Xml Tds catalog. */
    protected List<String> listCatalogRefSubPaths = null;

    /** URL where the catalog is stored. */
    private String urlSite = "";

    /**
     * Getter of the property <tt>urlSite</tt>.
     * 
     * @return Returns the urlSite.
     * 
     * @uml.property name="urlSite"
     */
    public String getUrlSite() {
        return this.urlSite;
    }

    /**
     * Setter of the property <tt>urlSite</tt>.
     * 
     * @param urlSite The urlSite to set.
     * 
     * @uml.property name="urlSite"
     */
    public void setUrlSite(String urlSite) {
        if (!urlSite.endsWith("/")) {
            this.urlSite = urlSite + "/";
        } else {
            this.urlSite = urlSite;
        }
    }

    /** Does Service needs CAS authentification to access catalog resources and data. */
    protected boolean casAuthentification = false;

    /**
     * Checks if is cas authentification.
     * 
     * @return true, if is cas authentification
     */
    public boolean isCasAuthentification() {
        return casAuthentification;
    }

    /**
     * Sets the cas authentification.
     * 
     * @param casAuthentification the new cas authentification
     */
    public void setCasAuthentification(boolean casAuthentification) {
        this.casAuthentification = casAuthentification;
    }

    /**
     * Temporary variable use to set product id loaded in the catalog. When catalog is loaded only product
     * that are in this set are retained in the products map.
     */
    private Set<String> productsLoaded = null;

    public Set<String> getProductsLoaded() {
        return productsLoaded;
    }

    public void setProductsLoaded(Set<String> productsLoaded) {
        this.productsLoaded = productsLoaded;
    }

    private boolean loadTDSExtraMetadata = false;

    public boolean isLoadTDSExtraMetadata() {
        return loadTDSExtraMetadata;
    }

    public void setLoadTDSExtraMetadata(boolean loadTDSExtraMetadata) {
        this.loadTDSExtraMetadata = loadTDSExtraMetadata;
    }

}
// CSON: MultipleStringLiterals
