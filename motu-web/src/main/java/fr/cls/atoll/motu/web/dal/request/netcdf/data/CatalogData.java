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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.library.inventory.Inventory;
import fr.cls.atoll.motu.web.common.format.OutputFormat;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ProductMetaData;
import fr.cls.atoll.motu.web.dal.tds.ncss.model.GeospatialCoverage;
import fr.cls.atoll.motu.web.dal.tds.ncss.model.SpatialRange;

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
    private static final Logger LOG = LogManager.getLogger();

    public static final String FTP_MISSING_FILE_REGEXP = "unknown.*";

    /** Does Service needs CAS authentication to access catalog resources and data. */
    private boolean casAuthentication = false;

    /** List contains lists of products from the catalog, group product of the same type/subtypes. */
    private List<List<Product>> listProductTypeDataset = null;

    /** List contains products from the catalog, which have the same type/subtypes. */
    protected List<Product> sameProductTypeDataset = null;
    /** The products map. Key is product id */
    private Map<String, Product> productsMap;
    /**
     * Temporary variable use to set product id loaded in the catalog. When catalog is loaded only product
     * that are in this set are retained in the products map.
     */
    private Set<String> productsLoaded = null;
    /** List contains each ath element to a Xml Tds catalog. */
    protected List<String> listCatalogRefSubPaths = null;

    /** URL where the catalog is stored. */
    private String urlSite = "";

    /** The products map. Key is product tds url path */
    private Map<String, Product> productsByTdsUrlMap;

    /** The current product sub-types. */
    private List<String> currentProductSubTypes;

    /** The current product metadata. */
    private ProductMetaData currentProductMetaData;

    /** The catalog title. */
    private String title = "";

    private boolean loadTDSExtraMetadata = false;

    private String currentProductType = "";

    private Product currentProduct;

    private GeospatialCoverage currentGeospatialCoverage;

    /**
     * Enumeration for available type of catalog.
     */
    public enum CatalogType {

        /** Opendap catalog. */
        OPENDAP(0),

        /** Tds catalog. */
        TDS(1),

        /** Ftp catalog (ftp, scft, griFtp). */
        FILE(2);

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
            for (OutputFormat c : OutputFormat.values()) {
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
    static public final String XML_TAG_SERVICENAME = "serviceName";

    /**
     * Default constructor.
     */
    public CatalogData() {
        currentProductSubTypes = new ArrayList<String>();
        sameProductTypeDataset = new ArrayList<Product>();
        listCatalogRefSubPaths = new ArrayList<String>();
        productsLoaded = new HashSet<String>();
        listProductTypeDataset = new ArrayList<List<Product>>();
        productsMap = new HashMap<String, Product>();
        productsByTdsUrlMap = new HashMap<String, Product>();
    }

    /**
     * Match ftp missing file.
     * 
     * @param name the name
     * @return the matcher
     */
    public static Matcher matchFTPMissingFile(String name) {

        Pattern pattern = Pattern.compile(CatalogData.FTP_MISSING_FILE_REGEXP);
        Matcher matcher = pattern.matcher(name);
        if (!(matcher.find())) {
            return null;
        }
        return matcher;

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
            String fileName = file.getName();
            dataFile.setName(file.getName());
            // Missing files are file whose name matches CatalogData.FTP_MISSING_FILE_REGEXP
            // or weight is null
            if (CatalogData.matchFTPMissingFile(fileName) != null) {
                continue;
            }
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
     * Initialize product's collection.
     * 
     * @param catalogLocation URL of a XML file containing the catalog.
     */
    public void listProducts(String catalogLocation) {

    }

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

    /**
     * Gets the current product type.
     * 
     * @return the currentProductType
     * 
     * @uml.property name="currentProductType"
     */
    public String getCurrentProductType() {
        return this.currentProductType;
    }

    /**
     * Sets the current product type.
     * 
     * @param currentProductType the currentProductType to set
     * 
     * @uml.property name="currentProductType"
     */
    public void setCurrentProductType(String currentProductType) {
        this.currentProductType = currentProductType;
    }

    /**
     * Gets the current product sub types.
     * 
     * @return the currentProductSubTypes
     */
    public List<String> getCurrentProductSubTypes() {
        return this.currentProductSubTypes;
    }

    /**
     * Sets the current product sub types.
     * 
     * @param currentProductSubTypes the currentProductSubTypes to set
     */
    public void setCurrentProductSubTypes(List<String> currentProductSubTypes) {
        this.currentProductSubTypes = currentProductSubTypes;
    }

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
     * @param listProductTypeDataset the product list.
     */
    public void setListProductTypeDataset(List<List<Product>> listProductTypeDataset) {
        this.listProductTypeDataset = listProductTypeDataset;
    }

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
        Product product = this.productsMap.get(key);
        if (product == null) {
            return null;
        }

        this.productsByTdsUrlMap.clear();
        return this.productsMap.remove(key);
    }

    /**
     * Removes all mappings from this map (optional operation).
     * 
     * @see java.util.Map#clear()
     * @uml.property name="products"
     */
    public void clearProducts() {
        this.productsByTdsUrlMap.clear();
        this.productsMap.clear();
    }

    /**
     * Gets the products by tds url map.
     * 
     * @return the products by tds url map
     */
    public Map<String, Product> getProductsByTdsUrl() {
        return productsByTdsUrlMap;
    }

    /**
     * Gets the products by tds url map.
     * 
     * @param key the key
     * @return the products by tds url map
     */
    public Product getProductsByTdsUrl(String key) {
        if (key == null) {
            return null;
        }
        return this.productsByTdsUrlMap.get(key.trim());
    }

    /**
     * Sets the products by tds url map.
     * 
     * @param productsByTdsUrlMap the products by tds url map
     */
    public void setProductsByTdsUrl(Map<String, Product> productsByTdsUrlMap) {
        this.productsByTdsUrlMap = productsByTdsUrlMap;
    }

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

    public Set<String> getProductsLoaded() {
        return productsLoaded;
    }

    public void setProductsLoaded(Set<String> productsLoaded) {
        this.productsLoaded = productsLoaded;
    }

    public boolean isLoadTDSExtraMetadata() {
        return loadTDSExtraMetadata;
    }

    public void setLoadTDSExtraMetadata(boolean loadTDSExtraMetadata) {
        this.loadTDSExtraMetadata = loadTDSExtraMetadata;
    }

    /**
     * Valeur de listCatalogRefSubPaths.
     * 
     * @return la valeur.
     */
    public List<String> getListCatalogRefSubPaths() {
        return listCatalogRefSubPaths;
    }

    /**
     * Valeur de listCatalogRefSubPaths.
     * 
     * @param listCatalogRefSubPaths the listCatalogRefSubPaths
     */
    public void setListCatalogRefSubPaths(List<String> listCatalogRefSubPaths) {
        this.listCatalogRefSubPaths = listCatalogRefSubPaths;
    }

    /**
     * Valeur de currentGeospatialCoverage.
     * 
     * @return la valeur.
     */
    public GeospatialCoverage getCurrentGeospatialCoverage() {
        return currentGeospatialCoverage;
    }

    /**
     * Valeur de currentGeospatialCoverage.
     * 
     * @param currentGeospatialCoverage nouvelle valeur.
     */
    public void setCurrentGeospatialCoverage(GeospatialCoverage currentGeospatialCoverage) {
        this.currentGeospatialCoverage = currentGeospatialCoverage;
    }

}
// CSON: MultipleStringLiterals
