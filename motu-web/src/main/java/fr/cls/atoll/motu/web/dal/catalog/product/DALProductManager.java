package fr.cls.atoll.motu.web.dal.catalog.product;

import fr.cls.atoll.motu.web.bll.exception.ExceptionUtils;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuExceptionBase;
import fr.cls.atoll.motu.web.bll.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.web.bll.request.model.RequestProduct;
import fr.cls.atoll.motu.web.dal.DALManager;
import fr.cls.atoll.motu.web.dal.catalog.product.metadata.opendap.OpenDapProductMetadataReader;
import fr.cls.atoll.motu.web.dal.request.extractor.DALDatasetManager;
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

    @Override
    public ProductMetaData getMetadata(String catalogType, String productId, String locationData, boolean useSSO) throws MotuException {
        if (!"FILE".equals(catalogType.toUpperCase())) {
            return new OpenDapProductMetadataReader(productId, locationData, useSSO).loadMetaData();
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @throws MotuExceptionBase
     */
    @Override
    public double getProductDataSizeRequest(RequestProduct requestProduct_) throws MotuException {
        double productDataSize = -1d;

        try {
            checkCatalogType(requestProduct_);
            productDataSize = new DALDatasetManager(requestProduct_).getAmountDataSize();
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
    protected void checkCatalogType(RequestProduct requestProduct_) throws MotuException, MotuNotImplementedException {
        String catalogType = DALManager.getInstance().getCatalogManager().getCatalogType(requestProduct_.getProduct());
        if (catalogType.toUpperCase().equals("FILE")) {
            long d1 = System.nanoTime();
            long d2 = System.nanoTime();

            requestProduct_.getProduct().setMediaKey(catalogType);

            updateFiles(requestProduct_);
            // Add time here (after updateFiles), because before updateFiles
            // dataset is not still create
            requestProduct_.getDataSetBase().getDataBaseExtractionTimeCounter().addReadingTime((d2 - d1));
        }
    }

    /**
     * Update files.
     * 
     * @param product the product
     * 
     * @throws MotuException the motu exception
     * @throws MotuNotImplementedException the motu not implemented exception
     */
    private void updateFiles(RequestProduct requestProduct_) throws MotuNotImplementedException, MotuException {
        if (requestProduct_.getProduct().getDataFiles() == null) {
            requestProduct_.clearFiles();
        } else {
            requestProduct_.updateFiles();
        }
    }
}
