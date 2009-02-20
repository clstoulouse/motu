/**
 * 
 */
package fr.cls.atoll.motu.data;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2008. <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-02-20 13:00:25 $
 * 
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

}
