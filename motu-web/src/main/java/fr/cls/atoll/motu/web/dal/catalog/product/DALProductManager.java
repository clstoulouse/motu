package fr.cls.atoll.motu.web.dal.catalog.product;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import fr.cls.atoll.motu.web.bll.exception.MotuExceedingCapacityException;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.exception.MotuExceptionBase;
import fr.cls.atoll.motu.web.bll.exception.MotuInconsistencyException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDateException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDateRangeException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDepthException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidLatLonRangeException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidLatitudeException;
import fr.cls.atoll.motu.web.bll.exception.MotuInvalidLongitudeException;
import fr.cls.atoll.motu.web.bll.exception.MotuMarshallException;
import fr.cls.atoll.motu.web.bll.exception.MotuNoVarException;
import fr.cls.atoll.motu.web.bll.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfAttributeException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfVariableException;
import fr.cls.atoll.motu.web.bll.exception.NetCdfVariableNotFoundException;
import fr.cls.atoll.motu.web.dal.DALManager;
import fr.cls.atoll.motu.web.dal.catalog.product.metadata.opendap.OpenDapProductMetadataReader;
import fr.cls.atoll.motu.web.dal.request.ProductSizeRequest;
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
    public double getProductDataSizeRequest(Product product,
                                            List<String> listVar,
                                            List<String> listTemporalCoverage,
                                            List<String> listLatLongCoverage,
                                            List<String> listDepthCoverage) throws MotuException {
        return getAmountDataSize(product, listVar, listTemporalCoverage, listLatLongCoverage, listDepthCoverage);
    }

    /**
     * Gets the amount data size.
     * 
     * @param params the params
     * 
     * @return the amount data size
     * 
     * @throws NetCdfVariableNotFoundException the net cdf variable not found exception
     * @throws MotuInvalidDepthRangeException the motu invalid depth range exception
     * @throws MotuInvalidLongitudeException the motu invalid longitude exception
     * @throws NetCdfVariableException the net cdf variable exception
     * @throws MotuInconsistencyException the motu inconsistency exception
     * @throws MotuNoVarException the motu no var exception
     * @throws NetCdfAttributeException the net cdf attribute exception
     * @throws MotuInvalidDepthException the motu invalid depth exception
     * @throws MotuMarshallException the motu marshall exception
     * @throws MotuExceedingCapacityException the motu exceeding capacity exception
     * @throws MotuInvalidLatitudeException the motu invalid latitude exception
     * @throws MotuNotImplementedException the motu not implemented exception
     * @throws MotuException the motu exception
     * @throws MotuInvalidDateException the motu invalid date exception
     * @throws MotuInvalidLatLonRangeException the motu invalid lat lon range exception
     * @throws MotuInvalidDateRangeException the motu invalid date range exception
     * @throws IOException
     * @throws JAXBException
     */
    public double getAmountDataSize(Product product,
                                    List<String> listVar,
                                    List<String> listTemporalCoverage,
                                    List<String> listLatLongCoverage,
                                    List<String> listDepthCoverage) throws MotuException {

        double productDataSize = -1d;

        try {
            checkCatalogType(product);
            ProductSizeRequest.computeAmountDataSize(product, listVar, listTemporalCoverage, listLatLongCoverage, listDepthCoverage);

            productDataSize = product.getAmountDataSizeAsBytes();
        } catch (Exception e) {
            throw new MotuException(e);
        }
        // catch (NetCdfAttributeException e) {
        // // Do nothing;
        // }

        return productDataSize;
    }

    /**
     * Check catalog type.
     * 
     * @throws MotuException
     * @throws MotuNotImplementedException
     */
    protected void checkCatalogType(Product product) throws MotuException, MotuNotImplementedException {
        String catalogType = DALManager.getInstance().getCatalogManager().getCatalogType(product);
        if (catalogType.toUpperCase().equals("FILE")) {

            if (product != null) {
                long d1 = System.nanoTime();
                long d2 = System.nanoTime();

                product.setMediaKey(catalogType);

                updateFiles(product);
                // Add time here (after updateFiles), because before updateFiles
                // dataset is not still create
                product.addReadingTime((d2 - d1));
            }
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
    private void updateFiles(Product product) throws MotuException, MotuNotImplementedException {
        if (product == null) {
            throw new MotuException("Error in updateFiles - product is null");
        }

        if (product.getDataFiles() == null) {
            product.clearFiles();
        } else {
            product.updateFiles();
        }
    }
}
