/**
 * 
 */
package fr.cls.atoll.motu.library.data;

import java.util.List;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2008. <br>
 * <br>
 * Soci�t� : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.2 $ - $Date: 2009-05-27 16:02:50 $
 */

public class ProductPersistent {

    /**
     * Constructor.
     */
    public ProductPersistent() {
    }

    /** The id. */
    protected String id;

    /** The serviceType. */
    protected String serviceType;

    /** The url. */
    protected String url;

    /** The url meta data. */
    protected String urlMetaData;
    
    /**
     * Gets the url meta data.
     * 
     * @return the url meta data
     */
    public String getUrlMetaData() {
        return urlMetaData;
    }

    /**
     * Sets the url meta data.
     * 
     * @param urlMetaData the new url meta data
     */
    public void setUrlMetaData(String urlMetaData) {
        this.urlMetaData = urlMetaData;
    }

    /**
     * Gets the service type.
     * 
     * @return the service type
     */
    public String getServiceType() {
        return serviceType;
    }

    /**
     * Sets the catalog type.
     * 
     * @param serviceType the catalog type
     */
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    /**
     * Gets the id.
     * 
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id.
     * 
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the url.
     * 
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the url.
     * 
     * @param url the url
     */
    public void setUrl(String url) {
        this.url = url;
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


}
