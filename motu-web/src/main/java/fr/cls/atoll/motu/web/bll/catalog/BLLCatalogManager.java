package fr.cls.atoll.motu.web.bll.catalog;

import fr.cls.atoll.motu.web.bll.catalog.product.BLLProductManager;
import fr.cls.atoll.motu.web.bll.catalog.product.IBLLProductManager;
import fr.cls.atoll.motu.web.bll.catalog.product.cache.CatalogAndProductCacheManager;
import fr.cls.atoll.motu.web.bll.catalog.product.cache.ICatalogAndProductCacheManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;

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

    private ICatalogAndProductCacheManager cacheManager;

    public BLLCatalogManager() {
        bllProductManager = new BLLProductManager();
        cacheManager = new CatalogAndProductCacheManager();
    }

    /** {@inheritDoc} */
    @Override
    public IBLLProductManager getProductManager() {
        return bllProductManager;
    }

    /** {@inheritDoc} */
    @Override
    public void init() throws MotuException {
        cacheManager.init();
    }

    /** {@inheritDoc} */
    @Override
    public void stop() {
        cacheManager.stop();
    }

    @Override
    public ICatalogAndProductCacheManager getCatalogAndProductCacheManager() {
        return cacheManager;
    }

    @Override
    public String getCatalogType(ConfigService service) throws MotuException {
        String catalogType = service.getCatalog().getType().toUpperCase();
        // This is for retrocompatibility with the motu version anterior to 3.0
        // The catalog type FTP is left and only FILE is used even if FTP is set as catalog type
        if ("FTP".equals(catalogType.toUpperCase())) {
            catalogType = "FILE";
        }

        return catalogType.toUpperCase();
    }

}
