package fr.cls.atoll.motu.web.bll;

import fr.cls.atoll.motu.web.bll.catalog.BLLCatalogManager;
import fr.cls.atoll.motu.web.bll.catalog.IBLLCatalogManager;
import fr.cls.atoll.motu.web.bll.config.BLLConfigManager;
import fr.cls.atoll.motu.web.bll.config.IBLLConfigManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.bll.request.BLLRequestManager;
import fr.cls.atoll.motu.web.bll.request.IBLLRequestManager;
import fr.cls.atoll.motu.web.bll.users.BLLUserManager;
import fr.cls.atoll.motu.web.bll.users.IBLLUserManager;

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
public class BLLManager implements IBLLManager {

    private static IBLLManager s_instance;

    private IBLLConfigManager configManager;
    private IBLLRequestManager requestManager;
    private IBLLUserManager userManager;
    private IBLLCatalogManager catalogManager;

    public static IBLLManager getInstance() {
        if (s_instance == null) {
            s_instance = new BLLManager();
        }
        return s_instance;
    }

    public BLLManager() {
        configManager = new BLLConfigManager();
        requestManager = new BLLRequestManager();
        userManager = new BLLUserManager();
        catalogManager = new BLLCatalogManager();
    }

    @Override
    public void init() throws MotuException {
        configManager.init();
        userManager.init();
        requestManager.init();
        catalogManager.init();
    }

    /** {@inheritDoc} */
    @Override
    public IBLLConfigManager getConfigManager() {
        return configManager;
    }

    /** {@inheritDoc} */
    @Override
    public IBLLRequestManager getRequestManager() {
        return requestManager;
    }

    /**
     * Valeur de userManager.
     * 
     * @return la valeur.
     */
    @Override
    public IBLLUserManager getUserManager() {
        return userManager;
    }

    /** {@inheritDoc} */
    @Override
    public IBLLCatalogManager getCatalogManager() {
        return catalogManager;
    }

}
