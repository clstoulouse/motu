/**
 * 
 */
package fr.cls.atoll.motu.library.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.tools.generic.MathTool;

import fr.cls.atoll.motu.library.exception.MotuExceedingCapacityException;
import fr.cls.atoll.motu.library.exception.MotuException;
import fr.cls.atoll.motu.library.exception.MotuInvalidDateException;
import fr.cls.atoll.motu.library.exception.MotuInvalidDateRangeException;
import fr.cls.atoll.motu.library.exception.MotuInvalidDepthException;
import fr.cls.atoll.motu.library.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.library.exception.MotuInvalidLatLonRangeException;
import fr.cls.atoll.motu.library.exception.MotuInvalidLatitudeException;
import fr.cls.atoll.motu.library.exception.MotuInvalidLongitudeException;
import fr.cls.atoll.motu.library.exception.MotuNoVarException;
import fr.cls.atoll.motu.library.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.library.exception.NetCdfAttributeException;
import fr.cls.atoll.motu.library.exception.NetCdfVariableException;
import fr.cls.atoll.motu.library.exception.NetCdfVariableNotFoundException;
import fr.cls.atoll.motu.library.intfce.Organizer;
import fr.cls.atoll.motu.library.inventory.InventoryOLA;
import fr.cls.atoll.motu.library.metadata.ProductMetaData;
import fr.cls.commons.util.io.ConfigLoader;

// CSOFF: MultipleStringLiterals : avoid message in constants declaration and trace log.

/**
 * This class implements a service (AVISO, MERCATOR, ...).
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.3 $ - $Date: 2009-05-20 15:15:05 $
 */
public class ServiceData {

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(ServiceData.class);

    /**
     * Name of the file that contents inforamtion of how to get huge data file. With '%s' corresponding to the
     * grup name of the service in lowercase (ie aviso, mercator).
     */
    private static final String HOW_TO_GET_EXCEED_DATA_INFO_FILENAME = "howToGetExceedData.%s.info";

    /** The Constant VELOCITY_TEMPLATE_SUFFIX_FILE. */
    private static final String VELOCITY_TEMPLATE_SUFFIX_FILE = ".vm";

    /** The Constant VELOCITY_TEMPLATE_DIR. */
    private static final String VELOCITY_TEMPLATE_DIR = "velocityTemplates/";

    /** Generic service name (virtual service with default options). */
    private static final String GENERIC_SERVICE_NAME = "Generic";

    /** Generic service description (virtual service with default options). */
    private static final String GENERIC_SERVICE_DESC = "Generic service";

    /**
     * Emumeration for various HTML pages available.
     */

    private enum HTMLPage {

        /** Inventory information. */
        CATALOG_INFO,

        /** Product metadata. */
        PRODUCT_INFO,

        /** Product download. */
        PRODUCT_DOWNLOAD
    };

    /** Current Html page in use. */
    private HTMLPage currentHtmlPage = null;

    /**
     * Emumeration for available languages.
     */

    public enum Language {

        /** english. */
        UK,

        /** french. */
        FR;

        /**
         * Test if french language.
         * 
         * @param value language to test
         * 
         * @return true if language is french
         */
        public boolean isFrench(String value) {
            try {
                return isFrench(ServiceData.stringToLanguage(value));
            } catch (MotuException e) {
                return false;
            }
        }

        /**
         * Test if french language.
         * 
         * @param value language to test
         * 
         * @return true if language is french
         */
        public boolean isFrench(Language value) {
            return value.equals(FR);
        }

        /**
         * Test if english language.
         * 
         * @param value language to test
         * 
         * @return true if language is french
         */
        public boolean isEnglish(String value) {
            try {
                return isEnglish(ServiceData.stringToLanguage(value));
            } catch (MotuException e) {
                return false;
            }
        }

        /**
         * Test if english language.
         * 
         * @param value language to test
         * 
         * @return true if language is french
         */
        public boolean isEnglish(Language value) {
            return value.equals(UK);
        }

        /**
         * Test if language is known.
         * 
         * @param value language to test
         * 
         * @return true if language is known
         */
        public boolean isKnown(String value) {
            try {
                ServiceData.stringToLanguage(value);
            } catch (MotuException e) {
                return false;
            }
            return true;

        }

    };

    /** Default language. */
    public static final Language DEFAULT_LANGUAGE = Language.UK;

    // /**
    // * Emumeration for available param can be used with servlet.
    // *
    // * @param PARAM_ACTION action parameter.
    // * @param PARAM_LOGIN login parameter.
    // * @param PARAM_PWD password parameter.
    // * @param PARAM_SERVICE service parameter.
    // * @param PARAM_PRODUCT product parameter.
    // */
    // public enum ParamServlet {
    // ACTION ("action"),
    // LOGIN ("login"),
    // PWD ("pwd"),
    // SERVICE ("service"),
    // PRODUCT ("product");
    // /**
    // * Contructor.
    // * @param id id of enum
    // */
    // private ParamServlet(String id) {this.id = id;}
    // /**
    // * id of enum.
    // */
    // private String id;
    // /**
    // * @return id of enum
    // */
    // public String getId(){ return id;}
    // };
    // /**
    // * Emumeration for available action value can be used with action servlet
    // prameter.
    // *
    // * @param LIST_CATALOG list a catalog.
    // * @param LIST_PRODUCT_METADATA list product's metadata.
    // * @param PRODUCT_DOWNLOADHOME get product download home inforamtions.
    // * @param PRODUCT_DOWNLOAD download a product.
    // */
    // public enum ActionServlet {
    // LIST_CATALOG ("listcatalog"),
    // LIST_PRODUCT_METADATA ("listproductmetadata"),
    // PRODUCT_DOWNLOADHOME ("productdownloadhome"),
    // PRODUCT_DOWNLOAD ("productdownload");
    // /**
    // * Contructor.
    // * @param id id of enum
    // */
    // private ActionServlet(String id) {this.id = id;}
    // /**
    // * id of enum.
    // */
    // private String id;
    // /**
    // * @return id of enum
    // */
    // public String getId(){ return id;}
    // };

    /**
     * Default constructor.
     */
    public ServiceData() {
    }

    /**
     * Gets the global velo template name.
     * 
     * @return the global velocity template name.
     */
    protected String getGlobalVeloTemplateName() {
        StringBuffer buffer = new StringBuffer(VELOCITY_TEMPLATE_DIR);
        if (veloTemplatePrefix.equals("")) {
            buffer.append(name.toLowerCase());
        } else {
            buffer.append(veloTemplatePrefix.toLowerCase());
        }
        buffer.append("_");
        buffer.append(languageToString());
        buffer.append(VELOCITY_TEMPLATE_SUFFIX_FILE);

        return buffer.toString();
    }

    /**
     * Gets the catalog velo template name.
     * 
     * @return the catalog velocity template name.
     */
    protected String getCatalogVeloTemplateName() {
        StringBuffer buffer = new StringBuffer(VELOCITY_TEMPLATE_DIR);
        buffer.append("catalog_");
        buffer.append(languageToString());
        buffer.append(VELOCITY_TEMPLATE_SUFFIX_FILE);

        return buffer.toString();
    }

    /**
     * Gets the available services template name.
     * 
     * @param language lanfuage of the template
     * 
     * @return the available list services velocity template name.
     */
    public static String getAvailableServicesTemplateName(Language language) {
        StringBuffer buffer = new StringBuffer(VELOCITY_TEMPLATE_DIR);
        buffer.append("listServices_");
        if (language != null) {
            buffer.append(ServiceData.languageToString(language));
        } else {
            buffer.append(ServiceData.languageToString(DEFAULT_LANGUAGE));
        }
        buffer.append(VELOCITY_TEMPLATE_SUFFIX_FILE);

        return buffer.toString();
    }

    /**
     * Gets the product meta data info velo template name.
     * 
     * @return the 'product metadata info' velocity template name.
     */
    protected String getProductMetaDataInfoVeloTemplateName() {
        StringBuffer buffer = new StringBuffer(VELOCITY_TEMPLATE_DIR);
        buffer.append("product_metadata_");
        buffer.append(languageToString());
        buffer.append(VELOCITY_TEMPLATE_SUFFIX_FILE);

        return buffer.toString();
    }

    /**
     * Gets the product download info velo template name.
     * 
     * @return the 'product metadata info' velocity template name.
     */
    protected String getProductDownloadInfoVeloTemplateName() {
        StringBuffer buffer = new StringBuffer(VELOCITY_TEMPLATE_DIR);
        buffer.append("product_downloadhome_");
        buffer.append(languageToString());
        buffer.append(VELOCITY_TEMPLATE_SUFFIX_FILE);

        return buffer.toString();
    }

    /**
     * Gets the alternate language.
     * 
     * @return If current language is english, returns french, otherwise returns english.
     */
    public Language getAlternateLanguage() {

        if (language.equals(Language.UK)) {
            return Language.FR;
        }
        return Language.UK;
    }

    /**
     * Gets the alternate language as string representation.
     * 
     * @return If current language is english, returns french, otherwise returns english.
     */
    public String getAlternateLanguageAsString() {
        return ServiceData.languageToString(getAlternateLanguage());
    }

    /** Velocity template engine. */
    private VelocityEngine velocityEngine = null;

    /**
     * Getter of the property <tt>velocityEngine</tt>.
     * 
     * @return Returns the velocityEngine.
     * 
     * @uml.property name="velocityEngine"
     */
    public VelocityEngine getVelocityEngine() {
        return this.velocityEngine;
    }

    /**
     * Setter of the property <tt>velocityEngine</tt>.
     * 
     * @param velocityEngine The velocityEngine to set.
     * 
     * @uml.property name="velocityEngine"
     */
    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    /** language to use. */
    private Language language = DEFAULT_LANGUAGE;

    /**
     * Converts a language to its lowercase string representation.
     * 
     * @param language langage from which to get the string representation
     * 
     * @return language converted to a lowercase string.
     */
    public static String languageToString(Language language) {
        return language.toString().toLowerCase();
    }

    /**
     * Language to string.
     * 
     * @return the language as a lowercase string.
     */
    public String languageToString() {
        return ServiceData.languageToString(this.language);
    }

    /**
     * Converts string to Language.
     * 
     * @param language string language representation to convert.
     * 
     * @return the language object
     * 
     * @throws MotuException the motu exception
     */
    public static Language stringToLanguage(String language) throws MotuException {
        try {
            return Language.valueOf(language.toUpperCase());
        } catch (RuntimeException e) {
            throw new MotuException(String.format("%s is not a valid or not implemented language", language), e);
        }
    }

    /**
     * Gets the language.
     * 
     * @return the language
     * 
     * @uml.property name="language"
     */
    public Language getLanguage() {
        return this.language;
    }

    /**
     * Sets the language.
     * 
     * @param language the language to set
     * 
     * @uml.property name="language"
     */
    public void setLanguage(Language language) {
        if (language == null) {
            return;
        }
        this.language = language;
    }

    /**
     * Sets the language.
     * 
     * @param language the language to set
     * 
     * @throws MotuException the motu exception
     * 
     * @uml.property name="language"
     */
    public void setLanguage(String language) throws MotuException {
        if (language == null) {
            return;
        }
        if (language.equals("")) {
            return;
        }
        this.language = ServiceData.stringToLanguage(language);
    }

    /** Name of the service. */
    private String name = GENERIC_SERVICE_NAME;

    /**
     * Getter of the property <tt>name</tt>.
     * 
     * @return Returns the name.
     * 
     * @uml.property name="name"
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setter of the property <tt>name</tt>.
     * 
     * @param name The name to set.
     * 
     * @uml.property name="name"
     */
    public void setName(String name) {
        this.name = GENERIC_SERVICE_NAME;
        if (name != null) {
            this.name = name.trim();
        }
    }

    /**
     * Checks if is generic.
     * 
     * @return true, if is generic
     */
    public boolean isGeneric() {
        return this.name.equalsIgnoreCase(GENERIC_SERVICE_NAME);
    }

    /** Description of the service. */
    private String description = GENERIC_SERVICE_DESC;

    /**
     * Getter of the property <tt>description</tt>.
     * 
     * @return Returns the name.
     * 
     * @uml.property name="description"
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Setter of the property <tt>description</tt>.
     * 
     * @param description The description to set.
     * 
     * @uml.property name="description"
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /** Type of catalog. */
    private CatalogData.CatalogType catalogType = CatalogData.CatalogType.OPENDAP;

    /**
     * Getter of the property <tt>catalogType</tt>.
     * 
     * @return Returns the catalogType.
     * 
     * @uml.property name="catalogType"
     */
    public CatalogData.CatalogType getCatalogType() {
        return this.catalogType;
    }

    /**
     * Setter of the property <tt>catalogType</tt>.
     * 
     * @param catalogType The catalogType to set.
     * 
     * @uml.property name="catalogType"
     */
    public void setCatalogType(CatalogData.CatalogType catalogType) {
        this.catalogType = catalogType;
    }

    /**
     * Setter of the property <tt>catalogType</tt>.
     * 
     * @param catalogType The catalogType to set.
     * 
     * @throws MotuException the motu exception
     * 
     * @uml.property name="catalogType"
     */
    public void setCatalogType(String catalogType) throws MotuException {
        if (catalogType.compareToIgnoreCase("opendap") == 0) {
            this.catalogType = CatalogData.CatalogType.OPENDAP;
        } else if (catalogType.compareToIgnoreCase("tds") == 0) {
            this.catalogType = CatalogData.CatalogType.TDS;
        } else {
            throw new MotuException(String.format("Unknow catalog type %s ", catalogType));
        }
    }

    /** The catalog. */
    private CatalogData catalog = null;

    /**
     * Getter of the property <tt>catalog</tt>.
     * 
     * @return Returns the catalog.
     * 
     * @throws MotuException the motu exception
     * @uml.property name="catalog"
     */
    public CatalogData getCatalog() throws MotuException {
        if (this.catalog == null) {
            loadCatalogInfo();
        }
        return this.catalog;
    }

    /**
     * Setter of the property <tt>catalog</tt>.
     * 
     * @param catalog The catalog to set.
     * 
     * @uml.property name="catalog"
     */
    public void setCatalog(CatalogData catalog) {
        this.catalog = catalog;
    }

    /**
     * Opens and creates the catalog.
     */
    public void openCatalog() {

    }

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

    /** Catalog's filename. */
    private String catalogFileName = "";

    /**
     * Getter of the property <tt>catalogFileName</tt>.
     * 
     * @return Returns the catalogFileName.
     * 
     * @uml.property name="catalogFileName"
     */
    public String getCatalogFileName() {
        return this.catalogFileName;
    }

    /**
     * Setter of the property <tt>urlSite</tt>.
     * 
     * @param catalogFileName The catalogFileName to set.
     * 
     * @uml.property name="catalogFileName"
     */
    public void setCatalogFileName(String catalogFileName) {
        this.catalogFileName = catalogFileName;
    }

    // /**
    // * URL of a XML file containing the catalog.
    // * @uml.property name="catalogLocation"
    // */
    // private String catalogLocation = "";

    /**
     * Returns the catalog location (urlSite + catalogFileName).
     * 
     * @return Returns the catalog location.
     */
    public String getCatalogLocation() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(urlSite);
        stringBuffer.append(catalogFileName);
        return stringBuffer.toString();
    }

    // /**
    // * Setter of the property <tt>catalogLocation</tt>.
    // * @param catalogLocation The catalogLocation to set.
    // * @uml.property name="catalogLocation"
    // */
    // public void setCatalogLocation(String catalogLocation) {
    // this.catalogLocation = catalogLocation;
    // }

    /**
     * Loads the catalog.
     * 
     * @throws MotuException the motu exception
     */

    public void loadCatalogInfo() throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("loadCatalogInfo() - entering");
        }

        if (catalog == null) {
            catalog = new CatalogData();
            catalog.setUrlSite(urlSite);
        }

        switch (getCatalogType()) {
        case OPENDAP:
            catalog.loadOpendapCatalog(this.getCatalogLocation());
            break;
        case TDS:
            catalog.loadTdsCatalog(this.getCatalogLocation());
            break;
        case FTP:
            // catalog.loadFtpCatalog(this.getCatalogLocation());
            break;
        default:
            throw new MotuException(String.format("Unknown catalog type %d ", getCatalogType()));
            // break;
        }

        // Chargement de la map des infos persistente pour le service et ses produits
        // Synchronisation pour ne pas que plusieurs threads effectue ce chargement
        synchronized (Organizer.getServicesPersistentInstance()) {
            // On teste si le service n'est pas déjà chargé
            // car un autre thread a pu le faire juste avant dans cette méthode.

            if (!(Organizer.servicesPersistentContainsKey(this.name))) {
                ServicePersistent servicePersistent = Organizer.getServicesPersistent(this.name);

                Collection<Product> products = catalog.productsValues();
                for (Product product : products) {
                    ProductPersistent productPersistent = new ProductPersistent();

                    String productId = product.getProductId();
                    productPersistent.setId(productId);
                    productPersistent.setServiceType(product.getTdsServiceType());
                    productPersistent.setUrl(product.getLocationData());
                    servicePersistent.putProductsPersistent(productId, productPersistent);
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("loadCatalogInfo() - exiting");
        }
    }

    /**
     * Gets the catalog's informations of the current service in HTML format.
     * 
     * @param out writer in which catalog's information will be listed.
     * 
     * @throws MotuException the motu exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     */
    public void getCatalogInformationHTML(Writer out) throws MotuException, NetCdfAttributeException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCatalogInformationHTML() - entering");
        }

        loadCatalogInfo();
        writeCatalogInformationHTML(out);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getCatalogInformationHTML() - exiting");
        }
    }

    /**
     * Writes the catalog's informations of the current service in HTML format.
     * 
     * @param out writer in which catalog's information will be list.
     * 
     * @throws MotuException the motu exception
     */
    private void writeCatalogInformationHTML(Writer out) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("writeCatalogInformationHTML() - entering");
        }

        if (catalog == null) {
            throw new MotuException("Error in writeCatalogInformationHTML - catalog is null");
        }
        if (velocityEngine == null) {
            throw new MotuException("Error in writeCatalogInformationHTML - velocityEngine is null");
        }

        try {
            Template template = getGlobalVeloTemplate();

            VelocityContext context = new VelocityContext();
            // System.out.println(velocityEngine.getProperty("file.resource.loader.path"));
            context.put("body_template", getCatalogVeloTemplateName());
            context.put("service", this);

            template.merge(context, out);

            currentHtmlPage = HTMLPage.CATALOG_INFO;
            currentProduct = null;

        } catch (Exception e) {
            LOG.error("writeCatalogInformationHTML()", e);

            throw new MotuException("Error in writeCatalogInformationHTML while construct velocity template", (Throwable) e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("writeCatalogInformationHTML() - exiting");
        }
    }

    /**
     * Loads product metadata.
     * 
     * @param locationData url of the product to load metadata
     * 
     * @return product instance with loaded metadata
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     */
    public Product getProductInformationFromLocation(String locationData) throws MotuException, MotuNotImplementedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProductInformationFromLocation() - entering");
        }

        Product product = new Product();
        currentProduct = product;
        product.setLocationData(locationData);
        product.setProductIdFromLocation();

        if (Organizer.isXMLFile(locationData)) {
            this.setCatalogType(CatalogData.CatalogType.FTP);
        }
//        
//        try {
//            InventoryOLA inventoryOLA = Organizer.getInventoryOLA(locationData);
//            if (inventoryOLA != null) {
//                this.setCatalogType(CatalogData.CatalogType.FTP);
//            }
//
//        } catch (Exception e) {
//            // Not a Inventory XML file : do nothing
//            e.printStackTrace();
//        }

        getProductInformation(product);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getProductInformationFromLocation() - exiting");
        }
        return product;
    }

    /**
     * Gets the product's informations (metadata) of the current service in HTML format.
     * 
     * @param productId id of the product on which to get informations.
     * 
     * @return product instance with loaded metadata
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     */
    public Product getProductInformation(String productId) throws MotuException, NetCdfAttributeException, MotuNotImplementedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProductInformation() - entering");
        }

        Product product = getProduct(productId);

        getProductInformation(product);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getProductInformation() - exiting");
        }
        return product;
    }

    /**
     * Gets the product's informations (metadata) of the current service in HTML format.
     * 
     * @param product instance of the product on which to get informations.
     * 
     * @return product instance with loaded metadata
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     */
    public Product getProductInformation(Product product) throws MotuException, MotuNotImplementedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProductInformation() - entering");
        }

        if (product == null) {
            throw new MotuException("Error in getProductInformation - product is null");
        }

        switch (getCatalogType()) {
        case OPENDAP:
            product.loadOpendapMetaData();
            break;
        case TDS:
            product.loadOpendapMetaData();
            break;
        case FTP:
            product.loadInventoryMetaData();
            break;
        default:
            throw new MotuNotImplementedException(String.format("Unimplemented catalog type %d ", getCatalogType()));
            // break;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getProductInformation() - exiting");
        }
        return product;
    }

    /**
     * Writes the product's informations of the current service in HTML format.
     * 
     * @param product instance of product on which to get informations.
     * @param out writer in which catalog's information will be listed.
     * 
     * @throws MotuException the motu exception
     */
    public void writeProductInformationHTML(Product product, Writer out) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("writeProductInformationHTML() - entering");
        }

        if (catalog == null) {
            throw new MotuException("Error in writeProductInformationHTML - catalog is null");
        }
        if (velocityEngine == null) {
            throw new MotuException("Error in writeProductInformationHTML - velocityEngine is null");
        }

        if (product == null) {
            throw new MotuException("Error in writeProductInformationHTML - product has not been set (is null)");
        }

        try {
            Template template = getGlobalVeloTemplate();
            VelocityContext context = new VelocityContext();
            context.put("body_template", getProductMetaDataInfoVeloTemplateName());
            context.put("service", this);
            context.put("product", product);

            template.merge(context, out);

            currentHtmlPage = HTMLPage.PRODUCT_INFO;
            currentProduct = product;
        } catch (Exception e) {
            LOG.error("writeProductInformationHTML()", e);

            throw new MotuException("Error in writeCatalogInformationHTML while construct velocity template", (Throwable) e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("writeProductInformationHTML() - exiting");
        }
    }

    /**
     * Refreshes HTML page. Can be used to rewrite page when language is changed, for instance.
     * 
     * @param out writer in which page will be written.
     * 
     * @throws MotuException the motu exception
     */
    public void refreshHTML(Writer out) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("refreshHTML() - entering");
        }

        if (currentHtmlPage == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("refreshHTML() - exiting");
            }
            return;
        }

        if (currentHtmlPage.equals(HTMLPage.CATALOG_INFO)) {
            writeCatalogInformationHTML(out);
        } else if (currentHtmlPage.equals(HTMLPage.PRODUCT_INFO)) {
            writeProductInformationHTML(currentProduct, out);
        } else if (currentHtmlPage.equals(HTMLPage.PRODUCT_DOWNLOAD)) {
            // Only refresh page, so disable automatic download.
            currentProduct.setAutoDownloadTimeOut(0);
            writeProductDownloadHTML(currentProduct, out);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("refreshHTML() - exiting");
        }
    }

    /**
     * Resets current HTML page (sets current to null).
     */
    public void resetHtmlCurrentPage() {
        currentHtmlPage = null;
    }

    /**
     * Gets the product's download informations of the current service in HTML format.
     * 
     * @param out writer in which product's informations will be listed.
     * @param productId id of the product on which to get informations.
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     */
    public void getProductDownloadInfoHTML(String productId, Writer out) throws MotuException, MotuNotImplementedException, NetCdfAttributeException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProductDownloadInfoHTML() - entering");
        }

        Product product = getProduct(productId);

        getProductDownloadInfoHTML(product, out);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getProductDownloadInfoHTML() - exiting");
        }
    }

    /**
     * Gets the product's download informations of the current service in HTML format.
     * 
     * @param product instance of the product on which to get informations.
     * @param out writer in which product's informations will be listed.
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     */
    public void getProductDownloadInfoHTML(Product product, Writer out) throws MotuException, MotuNotImplementedException, NetCdfAttributeException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProductDownloadInfoHTML() - entering");
        }

        if (product == null) {
            throw new MotuException("Error in getProductDownloadInfoHTML - product is null");
        }

        getProductInformation(product);

        writeProductDownloadHTML(product, out);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getProductDownloadInfoHTML() - exiting");
        }
    }

    /**
     * Writes the product's informations of the current service in HTML format.
     * 
     * @param product instance of product on which to get informations.
     * @param out writer in which catalog's information will be listed.
     * 
     * @return true, if write product download HTML
     * 
     * @throws MotuException the motu exception
     */
    public boolean writeProductDownloadHTML(Product product, Writer out) throws MotuException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("writeProductDownloadHTML() - entering");
        }
        if (product == null) {
            if (currentProduct != null) {
                product = currentProduct;
            } else {
                return false;
            }
        }

        ProductMetaData productMetaData = product.getProductMetaData();
        if (productMetaData == null) {
            return false;
        }
        if (productMetaData.getCoordinateAxes() == null) {
            return false;
        }
        if (productMetaData.getParameterMetaDatas() == null) {
            return false;
        }

        if (velocityEngine == null) {
            throw new MotuException("Error in writeProductDownloadHTML - velocityEngine is null");
        }
        // if (product == null) {
        // throw new MotuException("Error in writeProductDownloadHTML - product has not been set (is null)");
        // }

        try {
            Template template = getGlobalVeloTemplate();

            VelocityContext context = new VelocityContext();
            context.put("body_template", getProductDownloadInfoVeloTemplateName());
            context.put("mathTool", new MathTool());
            context.put("service", this);
            context.put("product", product);

            template.merge(context, out);

            currentHtmlPage = HTMLPage.PRODUCT_DOWNLOAD;
            currentProduct = product;

        } catch (Exception e) {
            LOG.error("writeProductDownloadHTML()", e);

            throw new MotuException("Error in writeProductDownloadHTML while construct velocity template", (Throwable) e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("writeProductDownloadHTML() - exiting");
        }
        return true;
    }

    /**
     * Creates a list of {@link ExtractCriteria} objects.
     * 
     * @param criteria list of criteria (geographical coverage, temporal coverage ...)
     * @param listLatLonCoverage list contains low latitude, low longitude, high latitude, high longitude (can
     *            be empty string)
     * @param listDepthCoverage list contains low depth, high depth.
     * @param listTemporalCoverage list contains start date and end date (can be empty string)
     * 
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws MotuInvalidDateException the motu invalid date exception
     */
    public static void createCriteriaList(List<String> listTemporalCoverage,
                                          List<String> listLatLonCoverage,
                                          List<String> listDepthCoverage,
                                          List<ExtractCriteria> criteria) throws MotuInvalidDateException, MotuInvalidDepthException,
            MotuInvalidLatitudeException, MotuInvalidLongitudeException, MotuException {

        if (criteria == null) {
            throw new MotuException("Error in ServiceData createCriteriaList - criteria is null");
        }

        // distinct try/catch exception to add all correct criteria
        // and to avoid to stop on the first criteria exception
        MotuInvalidDateException exceptionDate = null;
        MotuInvalidLatitudeException exceptionLat = null;
        MotuInvalidLongitudeException exceptionLon = null;
        MotuInvalidDepthException exceptionDepth = null;

        // adds temporal criteria
        try {
            ServiceData.addCriteriaTemporal(listTemporalCoverage, criteria);
        } catch (MotuInvalidDateException e) {
            exceptionDate = e;
        }

        // adds geographical Lat/Lon criteria
        try {
            ServiceData.addCriteriaLatLon(listLatLonCoverage, criteria);
        } catch (MotuInvalidLatitudeException e) {
            exceptionLat = e;
        } catch (MotuInvalidLongitudeException e) {
            exceptionLon = e;
        }

        // adds geographical Depth criteria
        try {
            ServiceData.addCriteriaDepth(listDepthCoverage, criteria);
        } catch (MotuInvalidDepthException e) {
            exceptionDepth = e;
        }

        if (exceptionDate != null) {
            throw exceptionDate;
        } else if (exceptionLat != null) {
            throw exceptionLat;
        } else if (exceptionLon != null) {
            throw exceptionLon;
        } else if (exceptionDepth != null) {
            throw exceptionDepth;
        }
    }

    /**
     * Add a temporal criteria to a list of {@link ExtractCriteria} objects.
     * 
     * @param criteria list of criteria (geographical coverage, temporal coverage ...), if null list is
     *            created
     * @param listTemporalCoverage list contains start date and end date (can be empty string)
     * 
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     */
    public static void addCriteriaTemporal(List<String> listTemporalCoverage, List<ExtractCriteria> criteria) throws MotuInvalidDateException,
            MotuException {
        if (criteria == null) {
            throw new MotuException("Error in ServiceData addCriteriaTemporal - criteria is null");
        }
        if (listTemporalCoverage != null) {
            if (!listTemporalCoverage.isEmpty()) {
                ExtractCriteriaDatetime c = null;
                c = new ExtractCriteriaDatetime();
                criteria.add(c);
                c.setValues(listTemporalCoverage);
            }
        }
    }

    /**
     * Add a geographical Lat/Lon criteria to a list of {@link ExtractCriteria} objects.
     * 
     * @param criteria list of criteria (geographical coverage, temporal coverage ...), if null list is
     *            created
     * @param listLatLonCoverage list contains low latitude, low longitude, high latitude, high longitude (can
     *            be empty string)
     * 
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuException the motu exception
     */
    public static void addCriteriaLatLon(List<String> listLatLonCoverage, List<ExtractCriteria> criteria) throws MotuInvalidLatitudeException,
            MotuInvalidLongitudeException, MotuException {
        if (criteria == null) {
            throw new MotuException("Error in ServiceData addCriteriaLatLon - criteria is null");
        }

        if (listLatLonCoverage != null) {
            if (!listLatLonCoverage.isEmpty()) {
                criteria.add(new ExtractCriteriaLatLon(listLatLonCoverage));
            }
        }
    }

    /**
     * Add a geographical Depth criteria to a list of {@link ExtractCriteria} objects.
     * 
     * @param criteria list of criteria (geographical coverage, temporal coverage ...), if null list is
     *            created
     * @param listDepthCoverage list contains low depth, high depth.
     * 
     * @throws MotuInvalidLongitudeException
     * @throws MotuInvalidLatitudeException
     * @throws MotuException the motu exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     */
    public static void addCriteriaDepth(List<String> listDepthCoverage, List<ExtractCriteria> criteria) throws MotuInvalidDepthException,
            MotuException {
        if (criteria == null) {
            throw new MotuException("Error in ServiceData addCriteriaDepth - criteria is null");
        }
        if (listDepthCoverage != null) {
            if (!listDepthCoverage.isEmpty()) {
                criteria.add(new ExtractCriteriaDepth(listDepthCoverage));
            }
        }
    }

    // /**
    // * Compute amount data size.
    // *
    // * @param listVar the list var
    // * @param locationData the location data
    // * @param listLatLonCoverage the list lat lon coverage
    // * @param listDepthCoverage the list depth coverage
    // * @param listTemporalCoverage the list temporal coverage
    // *
    // * @return the product
    // *
    // * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
    // * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
    // * @throws MotuInvalidLongitudeException the motu invalid longitude exception
    // * @throws NetCdfVariableException the net cdf variable exception
    // * @throws MotuNoVarException the motu no var exception
    // * @throws MotuInvalidDepthException the motu invalid depth exception
    // * @throws NetCdfAttributeException the net cdf attribute exception
    // * @throws MotuExceedingCapacityException the motu exceeding capacity exception
    // * @throws MotuInvalidLatitudeException the motu invalid latitude exception
    // * @throws MotuNotImplementedException the motu not implemented exception
    // * @throws MotuException the motu exception
    // * @throws MotuInvalidDateException the motu invalid date exception
    // * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
    // * @throws MotuInvalidDateRangeException the motu invalid date range exception
    // */
    // public Product computeAmountDataSize(String locationData,
    // List<String> listVar,
    // List<String> listTemporalCoverage,
    // List<String> listLatLonCoverage,
    // List<String> listDepthCoverage) throws MotuInvalidDateException, MotuInvalidDepthException,
    // MotuInvalidLatitudeException, MotuInvalidLongitudeException, MotuException,
    // MotuInvalidDateRangeException,
    // MotuExceedingCapacityException, MotuNotImplementedException, MotuInvalidLatLonRangeException,
    // MotuInvalidDepthRangeException,
    // NetCdfVariableException, MotuNoVarException, NetCdfAttributeException, NetCdfVariableNotFoundException
    // {
    //
    // Product product = getProductInformationFromLocation(locationData);
    //
    // computeAmountDataSize(product, listVar, listTemporalCoverage, listLatLonCoverage, listDepthCoverage);
    //
    // return product;
    // }

    /**
     * Compute amount data size.
     * 
     * @param product the product
     * @param listVar the list var
     * @param listLatLonCoverage the list lat lon coverage
     * @param listDepthCoverage the list depth coverage
     * @param listTemporalCoverage the list temporal coverage
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuNoVarException the motu no var exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     */
    public void computeAmountDataSize(Product product,
                                      List<String> listVar,
                                      List<String> listTemporalCoverage,
                                      List<String> listLatLonCoverage,
                                      List<String> listDepthCoverage) throws MotuInvalidDateException, MotuInvalidDepthException,
            MotuInvalidLatitudeException, MotuInvalidLongitudeException, MotuException, MotuInvalidDateRangeException,
            MotuExceedingCapacityException, MotuNotImplementedException, MotuInvalidLatLonRangeException, MotuInvalidDepthRangeException,
            NetCdfVariableException, MotuNoVarException, NetCdfAttributeException, NetCdfVariableNotFoundException {

        product.clearExtractFilename();
        product.clearLastError();

        // Converts criteria
        List<ExtractCriteria> criteria = new ArrayList<ExtractCriteria>();
        ServiceData.createCriteriaList(listTemporalCoverage, listLatLonCoverage, listDepthCoverage, criteria);

        computeAmountDataSize(product, listVar, criteria);

    }

    /**
     * Compute amount data size.
     * 
     * @param product the product
     * @param listVar the list var
     * @param criteria the criteria
     * 
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuNoVarException the motu no var exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     */
    public void computeAmountDataSize(Product product, List<String> listVar, List<ExtractCriteria> criteria) throws MotuException,
            MotuInvalidDateRangeException, MotuExceedingCapacityException, MotuNotImplementedException, MotuInvalidDepthRangeException,
            MotuInvalidLatLonRangeException, NetCdfVariableException, MotuNoVarException, NetCdfVariableNotFoundException {

        if (product == null) {
            throw new MotuException("Error in extractData - product is null");
        }

        // updates variables collection to download
        updateVariables(product, listVar);

        // updates criteria collection
        updateCriteria(product, criteria);

        product.computeAmountDataSize();

    }

    /**
     * Extracts data from a location data (url , filename) and according to criteria (geographical and/or
     * temporal and/or logical expression).
     * 
     * @param locationData location of the data to download (url, filename)
     * @param listVar list of variables (parameters) or expressions to extract.
     * @param listTemporalCoverage list contains start date and end date (can be empty string)
     * @param listLatLonCoverage list contains low latitude, low longitude, high latitude, high longitude (can
     *            be empty string)
     * @param listDepthCoverage list contains low depth, high depth.
     * @param selectData logical expression if it's true extract th data, if it's failse ignore the data.
     * @param dataOutputFormat data output format (NetCdf, HDF, Ascii, ...).
     * @return product object corresponding to the extraction
     * @throws MotuInvalidDateException
     * @throws MotuInvalidDepthException
     * @throws MotuInvalidLatitudeException
     * @throws MotuInvalidLongitudeException
     * @throws MotuException
     * @throws MotuExceedingCapacityException
     * @throws MotuNotImplementedException
     * @throws MotuInvalidDateRangeException
     * @throws MotuInvalidDepthRangeException
     * @throws MotuInvalidLatLonRangeException
     * @throws NetCdfVariableException
     * @throws MotuNoVarException
     * @throws NetCdfAttributeException
     * @throws NetCdfVariableNotFoundException
     */
    // public Product extractData(String locationData,
    // List<String> listVar,
    // List<String> listTemporalCoverage,
    // List<String> listLatLonCoverage,
    // List<String> listDepthCoverage,
    // SelectData selectData,
    // Organizer.Format dataOutputFormat) throws MotuInvalidDateException, MotuInvalidDepthException,
    // MotuInvalidLatitudeException, MotuInvalidLongitudeException, MotuException,
    // MotuInvalidDateRangeException,
    // MotuExceedingCapacityException, MotuNotImplementedException, MotuInvalidLatLonRangeException,
    // MotuInvalidDepthRangeException,
    // NetCdfVariableException, MotuNoVarException, NetCdfAttributeException, NetCdfVariableNotFoundException
    // {
    //
    // Product product = getProductInformationFromLocation(locationData);
    //
    // extractData(product, listVar, listTemporalCoverage, listLatLonCoverage, listDepthCoverage, selectData,
    // dataOutputFormat);
    //
    // return product;
    // }
    /**
     * Extracts data from a location data (url , filename) and according to criteria (geographical and/or
     * temporal and/or logical expression).
     * 
     * @param product product to be extracted
     * @param selectData logical expression if it's true extract th data, if it's failse ignore the data.
     * @param listVar list of variables (parameters) or expressions to extract.
     * @param dataOutputFormat data output format (NetCdf, HDF, Ascii, ...).
     * @param listLatLonCoverage list contains low latitude, low longitude, high latitude, high longitude (can
     *            be empty string)
     * @param listDepthCoverage list contains low depth, high depth.
     * @param listTemporalCoverage list contains start date and end date (can be empty string)
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuNoVarException the motu no var exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     * @throws IOException
     */
    public void extractData(Product product,
                            List<String> listVar,
                            List<String> listTemporalCoverage,
                            List<String> listLatLonCoverage,
                            List<String> listDepthCoverage,
                            SelectData selectData,
                            Organizer.Format dataOutputFormat) throws MotuInvalidDateException, MotuInvalidDepthException,
            MotuInvalidLatitudeException, MotuInvalidLongitudeException, MotuException, MotuInvalidDateRangeException,
            MotuExceedingCapacityException, MotuNotImplementedException, MotuInvalidLatLonRangeException, MotuInvalidDepthRangeException,
            NetCdfVariableException, MotuNoVarException, NetCdfAttributeException, NetCdfVariableNotFoundException, IOException {

        product.clearExtractFilename();
        product.clearLastError();

        // Converts criteria
        List<ExtractCriteria> criteria = new ArrayList<ExtractCriteria>();
        ServiceData.createCriteriaList(listTemporalCoverage, listLatLonCoverage, listDepthCoverage, criteria);

        extractData(product, listVar, criteria, selectData, dataOutputFormat);

    }

    /**
     * Extract data from a product related to the current service and according to criteria (geographical
     * and/or temporal and/or logical expression).
     * 
     * @param locationData location of the data to download (url, filename)
     * @param listVar list of variables (parameters) or expressions to extract.
     * @param criteria list of criteria (geographical coverage, temporal coverage ...)
     * @param selectData logical expression if it's true extract th data, if it's failse ignore the data.
     * @param dataOutputFormat data output format (NetCdf, HDF, Ascii, ...).
     * @return product object corresponding to the extraction
     * @throws MotuException
     * @throws NetCdfAttributeException
     * @throws MotuNotImplementedException
     * @throws MotuExceedingCapacityException
     * @throws MotuNotImplementedException
     * @throws MotuInvalidDateRangeException
     * @throws MotuInvalidDepthRangeException
     * @throws NetCdfVariableException
     * @throws MotuInvalidLatLonRangeException
     * @throws MotuNoVarException
     */

    // public Product extractData(String locationData,
    // List<String> listVar,
    // List<ExtractCriteria> criteria,
    // SelectData selectData,
    // Organizer.Format dataOutputFormat) throws MotuException, NetCdfAttributeException,
    // MotuNotImplementedException,
    // NetCdfVariableException, MotuExceedingCapacityException, MotuInvalidLatLonRangeException,
    // MotuInvalidDateRangeException,
    // MotuInvalidDepthRangeException, MotuNoVarException {
    // TLog.logger().entering(this.getClass().getName(),
    // "extractData",
    // new Object[] { locationData, listVar, criteria, selectData, dataOutputFormat });
    //
    // Product product = loadProductInfo(locationData);
    // product.clearExtractFilename();
    // product.clearLastError();
    //
    // extractData(product, listVar, criteria, selectData, dataOutputFormat);
    //
    // TLog.logger().exiting(this.getClass().getName(), "extractDataHTML");
    //
    // return product;
    //
    // }
    //
    /**
     * Updates the variable collection to download.
     * 
     * @param product instance of the product to extract.
     * @param listVar list of variables (parameters) or expressions to extract.
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     */
    public void updateVariables(Product product, List<String> listVar) throws MotuException, MotuNotImplementedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateVariables() - entering");
        }

        if (product == null) {
            throw new MotuException("Error in updateVariables - product is null");
        }

        if (listVar == null) {
            product.clearVariables();
        } else {
            product.updateVariables(listVar);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("updateVariables() - exiting");
        }
    }

    /**
     * Updates the variable collection to download.
     * 
     * @param product instance of the product to extract.
     * @param criteria list of criteria (geographical coverage, temporal coverage ...)
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     */
    public void updateCriteria(Product product, List<ExtractCriteria> criteria) throws MotuException, MotuNotImplementedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateCriteria() - entering");
        }

        if (product == null) {
            throw new MotuException("Error in updateCriteria - product is null");
        }

        if (criteria == null) {
            product.clearCriteria();
        } else {
            product.updateCriteria(criteria);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("updateCriteria() - exiting");
        }
    }

    /**
     * Updates the variable collection to download.
     * 
     * @param product instance of the product to extract.
     * @param selectData logical expression if it's true extract th data, if it's failse ignore the data.
     * 
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     */
    public void updateSelectData(Product product, SelectData selectData) throws MotuException, MotuNotImplementedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateSelectData() - entering");
        }

        if (product == null) {
            throw new MotuException("Error in updateSelectData - product is null");
        }
        product.setSelectData(selectData);

        if (LOG.isDebugEnabled()) {
            LOG.debug("updateSelectData() - exiting");
        }
    }

    /**
     * Extract data from a product related to the current service and according to criteria (geographical
     * and/or temporal and/or logical expression).
     * 
     * @param product instance of the product to extract.
     * @param selectData logical expression if it's true extract th data, if it's failse ignore the data.
     * @param listVar list of variables (parameters) or expressions to extract.
     * @param dataOutputFormat data output format (NetCdf, HDF, Ascii, ...).
     * @param criteria list of criteria (geographical coverage, temporal coverage ...)
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
     * @throws IOException
     */
    public void extractData(Product product,
                            List<String> listVar,
                            List<ExtractCriteria> criteria,
                            SelectData selectData,
                            Organizer.Format dataOutputFormat) throws MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException,
            MotuNotImplementedException, MotuInvalidDepthRangeException, MotuInvalidLatLonRangeException, NetCdfVariableException,
            MotuNoVarException, NetCdfVariableNotFoundException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractData() - entering");
        }

        if (product == null) {
            throw new MotuException("Error in extractData - product is null");
        }

        // updates variables collection to download
        updateVariables(product, listVar);

        // updates criteria collection
        updateCriteria(product, criteria);

        // updates 'select data' collection
        updateSelectData(product, selectData);

        product.extractData(dataOutputFormat);

        if (LOG.isDebugEnabled()) {
            LOG.debug("extractData() - exiting");
        }
    }

    /**
     * Extract data from a product related to the current service and according to variables and criteria
     * previously defined (geographical and/or temporal and/or logical expression).
     * 
     * @param product instance of the product to extract.
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
     * @throws IOException
     */
    public void extractData(Product product, Organizer.Format dataOutputFormat) throws MotuException, MotuInvalidDateRangeException,
            MotuExceedingCapacityException, MotuNotImplementedException, MotuInvalidDepthRangeException, MotuInvalidLatLonRangeException,
            NetCdfVariableException, MotuNoVarException, NetCdfVariableNotFoundException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractData() - entering");
        }

        if (product == null) {
            throw new MotuException("Error in extractData - product is null");
        }

        product.extractData(dataOutputFormat);

        if (LOG.isDebugEnabled()) {
            LOG.debug("extractData() - exiting");
        }
    }

    // /**
    // * Extract data HTML.
    // *
    // * @param selectData the select data
    // * @param listVar the list var
    // * @param dataOutputFormat the data output format
    // * @param locationData the location data
    // * @param listLatLonCoverage the list lat lon coverage
    // * @param listDepthCoverage the list depth coverage
    // * @param listTemporalCoverage the list temporal coverage
    // * @param out the out
    // *
    // * @return the product
    // *
    // * @throws MotuExceedingCapacityException the motu exceeding capacity exception
    // * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
    // * @throws MotuInvalidLongitudeException the motu invalid longitude exception
    // * @throws NetCdfVariableException the net cdf variable exception
    // * @throws MotuInvalidLatitudeException the motu invalid latitude exception
    // * @throws MotuNotImplementedException the motu not implemented exception
    // * @throws MotuNoVarException the motu no var exception
    // * @throws MotuException the motu exception
    // * @throws MotuInvalidDepthException the motu invalid depth exception
    // * @throws NetCdfAttributeException the net cdf attribute exception
    // * @throws MotuInvalidDateException the motu invalid date exception
    // * @throws MotuInvalidDateRangeException the motu invalid date range exception
    // */
    // public Product extractDataHTML(String locationData,
    // List<String> listVar,
    // List<String> listTemporalCoverage,
    // List<String> listLatLonCoverage,
    // List<String> listDepthCoverage,
    // SelectData selectData,
    // Organizer.Format dataOutputFormat,
    // Writer out) throws MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException,
    // MotuNotImplementedException, MotuInvalidDepthRangeException, NetCdfVariableException,
    // NetCdfAttributeException, MotuNoVarException,
    // MotuInvalidDepthException, MotuInvalidDateException, MotuInvalidLatitudeException,
    // MotuInvalidLongitudeException {
    //
    // Product product = getProductInformationFromLocation(locationData);
    // extractDataHTML(product, listVar, listTemporalCoverage, listLatLonCoverage, listDepthCoverage,
    // selectData, dataOutputFormat, out);
    //
    // return product;
    //
    // }

    /**
     * Extract data from a product related to the current service and according to criteria (geographical
     * and/or temporal and/or logical expression).
     * 
     * @param product product to download (url, filename)
     * @param selectData logical expression if it's true extract th data, if it's failse ignore the data.
     * @param listVar list of variables (parameters) or expressions to extract.
     * @param dataOutputFormat data output format (NetCdf, HDF, Ascii, ...).
     * @param listLatLonCoverage list contains low latitude, low longitude, high latitude, high longitude (can
     *            be empty string)
     * @param listDepthCoverage list contains low depth, high depth.
     * @param listTemporalCoverage list contains start date and end date (can be empty string)
     * @param out writer in which catalog's information will be listed.
     * 
     * @return product object corresponding to the extraction
     * 
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws MotuNoVarException the motu no var exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     */
    public Product extractDataHTML(Product product,
                                   List<String> listVar,
                                   List<String> listTemporalCoverage,
                                   List<String> listLatLonCoverage,
                                   List<String> listDepthCoverage,
                                   SelectData selectData,
                                   Organizer.Format dataOutputFormat,
                                   Writer out) throws MotuException, MotuInvalidDateRangeException, MotuExceedingCapacityException,
            MotuNotImplementedException, MotuInvalidDepthRangeException, NetCdfVariableException, NetCdfAttributeException, MotuNoVarException,
            MotuInvalidDepthException, MotuInvalidDateException, MotuInvalidLatitudeException, MotuInvalidLongitudeException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractDataHTML() - entering");
        }

        StringBuffer stringBuffer = new StringBuffer();
        try {
            product.clearExtractFilename();
            product.clearLastError();

            // updates variables collection to download
            updateVariables(product, listVar);

            // updates 'select data' collection
            updateSelectData(product, selectData);

            // Converts criteria
            List<ExtractCriteria> criteria = new ArrayList<ExtractCriteria>();
            try {
                ServiceData.createCriteriaList(listTemporalCoverage, listLatLonCoverage, listDepthCoverage, criteria);
            } finally {
                // updates criteria collection (even if an exception is raised)
                updateCriteria(product, criteria);
            }

            extractData(product, dataOutputFormat);

        } catch (Exception e) {
            product.clearExtractFilename();
            stringBuffer.append(Organizer.getFormattedError(e, this));
        }
        // } catch (MotuInvalidDepthException e) {
        // LOG.error("extractDataHTML()", e);
        //
        // product.clearExtractFilename();
        // stringBuffer.append(e.getDepthAsString());
        // stringBuffer.append(" is an invalid depth");
        //
        // } catch (MotuInvalidDepthRangeException e) {
        // LOG.error("extractDataHTML()", e);
        //
        // product.clearExtractFilename();
        // stringBuffer.append("Depth interval ");
        // stringBuffer.append(e.getInvalidRangeAsString());
        // stringBuffer.append(" is out of range.\nDepth must intersect the interval ");
        // stringBuffer.append(e.getValidRangeAsString());
        //
        // } catch (MotuInvalidDateException e) {
        // LOG.error("extractDataHTML()", e);
        //
        // product.clearExtractFilename();
        // stringBuffer.append(e.getDateAsString());
        // stringBuffer.append(" is an invalid date");
        //
        // } catch (MotuInvalidDateRangeException e) {
        // LOG.error("extractDataHTML()", e);
        //
        // product.clearExtractFilename();
        // stringBuffer.append("Date ");
        // stringBuffer.append(e.getInvalidRangeAsString());
        // stringBuffer.append(" is out of range.\nDate must intersect the interval ");
        // stringBuffer.append(e.getValidRangeAsString());
        //
        // } catch (MotuInvalidLatitudeException e) {
        // LOG.error("extractDataHTML()", e);
        //
        // product.clearExtractFilename();
        // stringBuffer.append(e.getLatAsString());
        // stringBuffer.append(" is an invalid latitude");
        //
        // } catch (MotuInvalidLongitudeException e) {
        // LOG.error("extractDataHTML()", e);
        //
        // product.clearExtractFilename();
        // stringBuffer.append(e.getLonAsString());
        // stringBuffer.append(" is an invalid longitude");
        //
        // } catch (MotuInvalidLatLonRangeException e) {
        // LOG.error("extractDataHTML()", e);
        //
        // product.clearExtractFilename();
        // stringBuffer.append("Latitude/Longitude bounding box: ");
        // stringBuffer.append(e.getInvalidRectAsString());
        // stringBuffer.append(" is out of range.\nLatitude/Longitude bounding box must intersect: ");
        // stringBuffer.append(e.getValidRectAsString());
        //
        // } catch (MotuExceedingCapacityException e) {
        // LOG.error("extractDataHTML()", e);
        //
        // // stringBuffer.append("The size of the data to download (");
        // // stringBuffer.append(e.getActualAsString());
        // // stringBuffer.append(") exceeds the maximum allowed (");
        // product.clearExtractFilename();
        // stringBuffer.append("The size of the data to download exceeds the maximum allowed (");
        // stringBuffer.append(e.getMaxAsString());
        // stringBuffer.append(").\n");
        // getHowTogetExceededData(stringBuffer);
        //
        // } catch (MotuNoVarException e) {
        // LOG.error("extractDataHTML()", e);
        //
        // product.clearExtractFilename();
        // stringBuffer.append("You have to select at least one variable to download");
        //
        // } catch (MotuExceptionBase e) {
        // LOG.error("extractDataHTML()", e);
        //
        // product.clearExtractFilename();
        // stringBuffer.append(e.notifyException());
        // }

        if (product != null) {
            product.setLastError(stringBuffer.toString());
            // Enable automatic download.
            product.setDefaultAutoDownloadTimeOut();
            writeProductDownloadHTML(product, out);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("extractDataHTML() - exiting");
        }
        return product;
    }

    /**
     * Gets the content of the file corresponding to the constant 'HOW_TO_GET_EXCEED_DATA_INFO_FILENAME'.
     * 
     * @param stringBuffer buffer in which content will be appended.
     * 
     * @throws MotuException the motu exception
     */
    public void getHowTogetExceededData(StringBuffer stringBuffer) throws MotuException {
        // Searchs file with service group name
        String resourceFileName = String.format(HOW_TO_GET_EXCEED_DATA_INFO_FILENAME, this.group.toLowerCase());
        // URL url = Organizer.class.getClassLoader().getResource(resourceFileName);
        URL url = null;
        try {
            url = ConfigLoader.getInstance().get(resourceFileName);
        } catch (IOException e) {
            // Do nothing
        }
        // if ressource file not found - Searchs file with servicevelocity prefix
        if (url == null) {
            // Searchs file with servicevelocity prefix
            resourceFileName = String.format(HOW_TO_GET_EXCEED_DATA_INFO_FILENAME, this.veloTemplatePrefix.toLowerCase());
            // url = Organizer.class.getClassLoader().getResource(resourceFileName);
            try {
                url = ConfigLoader.getInstance().get(resourceFileName);
            } catch (IOException e) {
                // Do nothing
            }
            // if ressource file not found - return
            if (url == null) {
                return;
            }
        }

        String lineSep = System.getProperty("line.separator");
        BufferedReader reader = null;
        try {
            StringBuffer fileName = new StringBuffer();
            String protocol = url.getProtocol();
            if (protocol != null) {
                if (!protocol.equalsIgnoreCase("")) {
                    fileName.append(url.getProtocol());
                    fileName.append(":");
                }
            }
            fileName.append(url.getPath());
            URL urlFile = new URL(fileName.toString());
            URLConnection urlc = urlFile.openConnection();
            InputStream is = urlc.getInputStream();
            InputStreamReader eisr = new InputStreamReader(is);
            // Read from FileReader is to verify the point - it does
            // not work for the file that is in a jar file.
            // reader = new BufferedReader(new FileReader(fileName.toString()));
            reader = new BufferedReader(eisr);
            if (!reader.ready()) {
                throw new MotuException(String.format("Error in ServiceData - getHowTogetExceededData. File %s is not ready", url.getPath()));
            }
            String nextLine = "";
            while ((nextLine = reader.readLine()) != null) {
                stringBuffer.append(nextLine);
                stringBuffer.append(lineSep);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            // Do nothing
        } catch (IOException e) {
            throw new MotuException("Error in ServiceData - getHowTogetExceededData", e);
        }

    }

    /**
     * Get an instance of Product from a product id string representation.
     * 
     * @param productId product id to find
     * 
     * @return an instance of the found Product, or null if unknown product id.
     * 
     * @throws MotuException the motu exception
     */
    public Product getProduct(String productId) throws MotuException {

        Product product = getCatalog().getProducts(productId);

        if (product == null) {
            throw new MotuException(String.format("Unknown product id %s for service name %s ", productId, this.name));
        }
        currentProduct = product;
        return product;
    }

    /**
     * Gets the global velo template.
     * 
     * @return the Velocity global template
     * 
     * @throws MotuException the motu exception
     */
    private Template getGlobalVeloTemplate() throws MotuException {
        Template template = null;
        try {
            template = velocityEngine.getTemplate(getGlobalVeloTemplateName());
        } catch (ResourceNotFoundException e) {
            setLanguage(Language.UK);
            try {
                template = velocityEngine.getTemplate(getGlobalVeloTemplateName());
            } catch (Exception e1) {
                throw new MotuException("Error in ServiceData - getGlobalVeloTemplate", e1);
            }
        } catch (Exception e) {
            throw new MotuException("Error in ServiceData - getGlobalVeloTemplate", e);
        }
        return template;
    }

    /**
     * General velocity template prefix name. If empty, the velocity template prefix is service name in
     * lowercase
     */
    private String veloTemplatePrefix = "";

    /**
     * Getter of the property <tt>veloTemplatePrefix</tt>.
     * 
     * @return Returns the veloTemplatePrefix.
     * 
     * @uml.property name="veloTemplatePrefix"
     */
    public String getVeloTemplatePrefix() {
        return this.veloTemplatePrefix;
    }

    /**
     * Setter of the property <tt>veloTemplatePrefix</tt>.
     * 
     * @param veloTemplatePrefix The veloTemplatePrefix to set.
     * 
     * @uml.property name="veloTemplatePrefix"
     */
    public void setVeloTemplatePrefix(String veloTemplatePrefix) {
        if (veloTemplatePrefix != null) {
            this.veloTemplatePrefix = veloTemplatePrefix;
        }
    }

    /** Http base reference of the service site. */
    private String httpBaseRef = "";

    /**
     * Getter of the property <tt>httpBaseRef</tt>.
     * 
     * @return Returns the httpBaseRef.
     * 
     * @uml.property name="httpBaseRef"
     */
    public String getHttpBaseRef() {
        return this.httpBaseRef;
    }

    /**
     * Setter of the property <tt>httpBaseRef</tt>.
     * 
     * @param httpBaseRef The httpBaseRef to set.
     * 
     * @uml.property name="httpBaseRef"
     */
    public void setHttpBaseRef(String httpBaseRef) {
        if (httpBaseRef != null) {
            this.httpBaseRef = httpBaseRef;
        }
    }

    /** Group (category) of the service. */
    private String group = "";

    /**
     * Getter of the property <tt>group</tt>.
     * 
     * @return Returns the group.
     * 
     * @uml.property name="group"
     */
    public String getGroup() {
        return this.group;
    }

    /**
     * Setter of the property <tt>group</tt>.
     * 
     * @param group The group to set.
     * 
     * @uml.property name="group"
     */
    public void setGroup(String group) {
        if (group != null) {
            this.group = group;
        }
    }

    /** list of services with the same group. */
    private List<ServiceData> sameGroupServices = new ArrayList<ServiceData>();

    /**
     * Getter of the property <tt>sameGroupServices</tt>.
     * 
     * @return Returns the sameGroupServices.
     * @uml.property name="sameGroupServices"
     */
    public List<ServiceData> getSameGroupServices() {
        return this.sameGroupServices;
    }

    /**
     * Setter of the property <tt>sameGroupServices</tt>.
     * 
     * @param sameGroupServices The sameGroupServices to set.
     * @uml.property name="sameGroupServices"
     */
    public void setSameGroupServices(List<ServiceData> sameGroupServices) {
        this.sameGroupServices = sameGroupServices;
    }

    /** Current product in use. */
    private Product currentProduct = null;

    /**
     * Gets the current product.
     * 
     * @return the current product
     */
    public Product getCurrentProduct() {
        return currentProduct;
    }

}

// CSON: MultipleStringLiterals
