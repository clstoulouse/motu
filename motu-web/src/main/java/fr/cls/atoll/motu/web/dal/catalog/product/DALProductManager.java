package fr.cls.atoll.motu.web.dal.catalog.product;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.ExceptionUtils;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuExceptionBase;
import fr.cls.atoll.motu.web.bll.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.web.bll.request.model.RequestProduct;
import fr.cls.atoll.motu.web.dal.catalog.product.metadata.opendap.OpenDapProductMetadataReader;
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
public class DALProductManager implements IDALProductManager {

    /**
     * {@inheritDoc}
     * 
     * @throws MotuExceptionBase
     */
    @Override
    public double getProductDataSizeRequestInMegabyte(RequestProduct rp) throws MotuException {
        double productDataSize = -1d;
        try {
            checkCatalogType(rp);
            productDataSize = rp.getDatasetManager().computeAmountDataSize();
        } catch (Exception e) {
            throw new MotuException(ExceptionUtils.getErrorType(e), e);
        }

        return productDataSize;
    }

    /**
     * Check catalog type.
     * 
     * @throws MotuException
     * @throws MotuNotImplementedException
     */
    protected void checkCatalogType(RequestProduct rp) throws MotuException {
        String catalogType = BLLManager.getInstance().getCatalogManager()
                .getCatalogType(BLLManager.getInstance().getConfigManager().getConfigService(rp.getExtractionParameters().getServiceName()));
        if (catalogType.toUpperCase().equals("FILE")) {
            rp.getProduct().setMediaKey(catalogType);
        }
    }

    @Override
    public ProductMetaData updateMetadata(String catalogType, String productId, String locationData, ProductMetaData pmd) throws MotuException {
        if (!"FILE".equals(catalogType.toUpperCase())) {
            return new OpenDapProductMetadataReader(productId, locationData).loadMetaData(pmd);
        } else {
            return null;
        }
    }
}
