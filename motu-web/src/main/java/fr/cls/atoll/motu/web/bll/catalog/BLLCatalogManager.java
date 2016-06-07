package fr.cls.atoll.motu.web.bll.catalog;

import fr.cls.atoll.motu.web.bll.catalog.product.BLLProductManager;
import fr.cls.atoll.motu.web.bll.catalog.product.IBLLProductManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.DALManager;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;

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

    public BLLCatalogManager() {
        bllProductManager = new BLLProductManager();
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
        // noop
    }

}
