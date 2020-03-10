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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.library.inventory.Inventory;
import fr.cls.atoll.motu.web.common.format.OutputFormat;
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

    /** ServiceName XML tag element. */
    public static final String XML_TAG_SERVICENAME = "serviceName";
    public static final String FTP_MISSING_FILE_REGEXP = "unknown.*";

    /**
     * |REAL CACHE| List contains lists of products from the catalog, group product of the same type/subtypes.
     */
    private List<List<Product>> listProductTypeDataset = null;

    /**
     * |TEMP USED TO BUILD CACHE| List contains products from the catalog, which have the same type/subtypes.
     * This list is used to build the listProductTypeDataset
     */
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

    /** |REAL CACHE| Key is product tds url path */
    private Map<String, Product> productsByTdsUrlMap;

    /** The current product sub-types. */
    private List<String> currentProductSubTypes;

    /** The catalog title. */
    private String title = "";
    /** URL where the catalog is stored. */
    private String urlSite = "";

    private boolean loadTDSExtraMetadata = false;

    private String currentProductType = "";
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

        List<DataFile> dataFiles = new ArrayList<>();

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
            dataFile.setWeight(file.getWeight().longValue());

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
     * Returns the value to which this map maps the specified key.
     * 
     * @param productId key whose associated value is to be returned.
     * 
     * @return the value to which this map maps the specified key, or <tt>null</tt> if the map contains no
     *         mapping for this key.
     * 
     * @see java.util.Map#get(Object)
     * @uml.property name="products"
     */
    public Product getProducts(String productId) {
        Product p = null;
        if (productId != null) {
            String keyTrimmed = productId.trim();
            Iterator<String> keysIt = this.productsMap.keySet().iterator();
            while (p == null && keysIt.hasNext()) {
                String k = keysIt.next();
                if (k.equalsIgnoreCase(keyTrimmed)) {
                    p = this.productsMap.get(k);
                }
            }
        }
        return p;
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
     * @param tdsURL the key
     * @return the products by tds url map
     */
    public Product getProductsByTdsUrl(String tdsURL) {
        return tdsURL == null ? null : this.getProductsByTdsUrl().get(tdsURL.trim());
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
     * @param urlSite_ The urlSite to set.
     * 
     * @uml.property name="urlSite"
     */
    public void setUrlSite(String urlSite_) {
        String lastSlash = "";
        if (!urlSite_.endsWith("/")) {
            lastSlash = "/";
        }
        this.urlSite = urlSite_ + lastSlash;
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
