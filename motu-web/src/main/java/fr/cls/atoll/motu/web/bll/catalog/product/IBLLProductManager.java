package fr.cls.atoll.motu.web.bll.catalog.product;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;

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
public interface IBLLProductManager {

    /**
     * Retrieve the product using the catalog name and the url path of the product. .
     * 
     * @param catalogName The catalog name of the product
     * @param urlPath The url path of the product
     * @return The product object if found, null otherwise
     * @throws MotuException
     */
    Product getProductFromLocation(String catalogName, String urlPath) throws MotuException;

    /**
     * Retrieve the Product using the url path of the product. .
     * 
     * @param urlPath The url path of the product
     * @return The product object if found, null otherwise
     * @throws MotuException
     */
    Product getProductFromLocation(String urlPath) throws MotuException;

    /**
     * Gets the tDS dataset id.
     * 
     * @param locationData the location data
     * 
     * @return the tDS dataset id
     */
    String datasetIdFromProductLocation(String locationData);

    /**
     * .
     * 
     * @param productFileName_
     * @return
     */
    String getProductDownloadHttpUrl(String productFileName_);

    /**
     * .
     * 
     * @param productId
     * @return
     */
    Product getProduct(String productId);

}
