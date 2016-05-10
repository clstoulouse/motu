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

import java.util.List;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
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

    /** The url NCSS (optional) */
    protected String urlNCSS;

    /**
     * Gets the url meta data.
     * 
     * @return the url meta data
     */
    public String getUrlMetaData() {
        return urlMetaData;
    }

    /**
     * Sets the url of the NCSS service (if available)
     * 
     * @param urlMetaData the new url of the NCSS service (if available)
     */
    public void setUrlNCSS(String urlNCSS) {
        this.urlNCSS = urlNCSS;
    }

    /**
     * Gets the url of the NCSS service (if available)
     * 
     * @return the url of the NCSS service (if available)
     */
    public String getUrlNCSS() {
        return urlNCSS;
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
