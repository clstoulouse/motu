package fr.cls.atoll.motu.web.dal.catalog.product;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import fr.cls.atoll.motu.library.misc.exception.MotuExceedingCapacityException;
import fr.cls.atoll.motu.library.misc.exception.MotuExceptionBase;
import fr.cls.atoll.motu.library.misc.exception.MotuInconsistencyException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDateException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDateRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDepthException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidDepthRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidLatLonRangeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidLatitudeException;
import fr.cls.atoll.motu.library.misc.exception.MotuInvalidLongitudeException;
import fr.cls.atoll.motu.library.misc.exception.MotuMarshallException;
import fr.cls.atoll.motu.library.misc.exception.MotuNoVarException;
import fr.cls.atoll.motu.library.misc.exception.MotuNotImplementedException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfAttributeException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfVariableException;
import fr.cls.atoll.motu.library.misc.exception.NetCdfVariableNotFoundException;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.ProductSizeRequest;
import fr.cls.atoll.motu.web.dal.catalog.product.metadata.opendap.OpenDapProductMetadataReader;
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
    public ProductMetaData getMetadata(String productId, String locationData, boolean useSSO) throws MotuException {
        return new OpenDapProductMetadataReader(productId, locationData, useSSO).loadMetaData();
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
                                            List<String> listDepthCoverage) throws MotuExceptionBase {
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
                                    List<String> listDepthCoverage) throws MotuExceptionBase {

        double productDataSize = -1d;

        try {
            ProductSizeRequest.computeAmountDataSize(product, listVar, listTemporalCoverage, listLatLongCoverage, listDepthCoverage);

            productDataSize = product.getAmountDataSizeAsBytes();
        } catch (NetCdfAttributeException e) {
            // Do nothing;
        } catch (Exception e) {
            throw new MotuExceptionBase(e);
        }

        return productDataSize;
    }
}
