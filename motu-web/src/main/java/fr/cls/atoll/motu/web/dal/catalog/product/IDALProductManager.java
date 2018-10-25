package fr.cls.atoll.motu.web.dal.catalog.product;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuExceptionBase;
import fr.cls.atoll.motu.web.bll.request.model.RequestDownloadStatus;
import fr.cls.atoll.motu.web.dal.request.netcdf.metadata.ProductMetaData;

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
public interface IDALProductManager {

    /**
     * Retrieve the Size of the provided Product.
     * 
     * @return The size of the product into megabyte
     * @throws MotuExceptionBase
     */
    double getProductDataSizeRequestInMegabyte(RequestDownloadStatus rds_) throws MotuException;

    ProductMetaData updateMetadata(String catalogType, String productId, String locationData, ProductMetaData pmd) throws MotuException;
}
