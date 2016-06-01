package fr.cls.atoll.motu.web.bll.catalog.product;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.DALManager;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;
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
public class BLLProductManager implements IBLLProductManager {

    /**
     * {@inheritDoc}
     * 
     * @throws MotuException
     */
    @Override
    // FIXME plac supprimer le productId
    public ProductMetaData getProductMetaData(String productId, String locationData) throws MotuException {
        return DALManager.getInstance().getCatalogManager().getProductManager()
                .getMetadata(productId, locationData, BLLManager.getInstance().getConfigManager().getMotuConfig().getUseAuthentication());
    }

    /**
     * {@inheritDoc}
     * 
     * @throws MotuException
     */
    @Override
    public Product getProduct(String locationData) throws MotuException {
        return BLLManager.getInstance().getConfigManager().getProduct(locationData);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws MotuException
     */
    @Override
    public Product getProduct(String serviceName, String productId) throws MotuException {
        ConfigService cs = BLLManager.getInstance().getConfigManager().getConfigService(serviceName);
        CatalogData cd = BLLManager.getInstance().getCatalogManager().getCatalogData(cs);
        Product p = cd.getProducts().get(productId);

        return p;
    }

}
