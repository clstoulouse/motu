package fr.cls.atoll.motu.web.dal;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.catalog.DALCatalogManager;
import fr.cls.atoll.motu.web.dal.catalog.IDALCatalogManager;
import fr.cls.atoll.motu.web.dal.config.DALConfigManager;
import fr.cls.atoll.motu.web.dal.config.IDALConfigManager;
import fr.cls.atoll.motu.web.dal.messageserror.DALMessagesErrorManager;
import fr.cls.atoll.motu.web.dal.messageserror.IDALMessagesErrorManager;
import fr.cls.atoll.motu.web.dal.request.DALRequestManager;
import fr.cls.atoll.motu.web.dal.request.IDALRequestManager;
import fr.cls.atoll.motu.web.dal.users.DALUserManager;
import fr.cls.atoll.motu.web.dal.users.IDALUserManager;

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
public class DALManager implements IDALManager {

    private static IDALManager s_instance;

    private IDALConfigManager dalConfigManager;
    private IDALRequestManager dalRequestManager;
    private IDALUserManager dalUserManager;
    private IDALCatalogManager dalCatalogManager;
    private IDALMessagesErrorManager dalMessagesErrorManager;

    public static IDALManager getInstance() {
        if (s_instance == null) {
            s_instance = new DALManager();
        }
        return s_instance;
    }

    public DALManager() {
        dalConfigManager = new DALConfigManager();
        dalRequestManager = new DALRequestManager();
        dalUserManager = new DALUserManager();
        dalCatalogManager = new DALCatalogManager();
        dalMessagesErrorManager = new DALMessagesErrorManager();
    }

    @Override
    public void init() throws MotuException {
        dalConfigManager.init();
        dalCatalogManager.init();
        dalMessagesErrorManager.init();
    }

    @Override
    public IDALConfigManager getConfigManager() {
        return dalConfigManager;
    }

    @Override
    public IDALRequestManager getRequestManager() {
        return dalRequestManager;
    }

    /** {@inheritDoc} */
    @Override
    public IDALUserManager getUserManager() {
        return dalUserManager;
    }

    /** {@inheritDoc} */
    @Override
    public IDALCatalogManager getCatalogManager() {
        return dalCatalogManager;
    }

    /** {@inheritDoc} */
    @Override
    public IDALMessagesErrorManager getMessagesErrorManager() {
        return dalMessagesErrorManager;
    }

}
