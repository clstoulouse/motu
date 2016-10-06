package fr.cls.atoll.motu.web.dal.catalog.product;

import java.util.List;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuExceptionBase;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.Product;
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
     * .
     * 
     * @param productId
     * @param locationData
     * @param useSSO
     * @return
     * @throws MotuException
     */
    ProductMetaData getMetadata(String catalogType, String productId, String locationData, boolean useSSO) throws MotuException;

    /**
     * Retrieve the Size of the provided Product.
     * 
     * @return The size of the product into kylobyte
     * @throws MotuExceptionBase
     */
    double getProductDataSizeRequest(Product product,
                                     List<String> listVar,
                                     List<String> listTemporalCoverage,
                                     List<String> listLatLongCoverage,
                                     List<String> listDepthCoverage) throws MotuException;
}
