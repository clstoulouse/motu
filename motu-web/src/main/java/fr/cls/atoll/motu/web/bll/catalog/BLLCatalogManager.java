package fr.cls.atoll.motu.web.bll.catalog;

import fr.cls.atoll.motu.web.bll.catalog.product.BLLProductManager;
import fr.cls.atoll.motu.web.bll.catalog.product.IBLLProductManager;
import fr.cls.atoll.motu.web.bll.catalog.product.cache.IProductCacheManager;
import fr.cls.atoll.motu.web.bll.catalog.product.cache.ProductCacheManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.DALManager;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;
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
public class BLLCatalogManager implements IBLLCatalogManager {

    private IBLLProductManager bllProductManager;
    private IProductCacheManager productCacheManager;

    public BLLCatalogManager() {
        bllProductManager = new BLLProductManager();
        productCacheManager = new ProductCacheManager();
    }

    /**
     * {@inheritDoc}
     * 
     * @throws MotuException
     */
    @Override
    public CatalogData getCatalogData(ConfigService cs) throws MotuException {
        return DALManager.getInstance().getCatalogManager().getCatalogData(cs);
    }

    /** {@inheritDoc} */
    @Override
    public IBLLProductManager getProductManager() {
        return bllProductManager;
    }

    /** {@inheritDoc} */
    @Override
    public void init() throws MotuException {
        productCacheManager.init();
    }

    /** {@inheritDoc} */
    @Override
    public void stop() {
        productCacheManager.stop();
    }

    @Override
    public String getCatalogType(Product product) throws MotuException {
        return DALManager.getInstance().getCatalogManager().getCatalogType(product);
    }

    @Override
    public String getCatalogType(ConfigService service) throws MotuException {
        return DALManager.getInstance().getCatalogManager().getCatalogType(service);
    }

    @Override
    public IProductCacheManager getProductCacheManager() {
        return productCacheManager;
    }

}
