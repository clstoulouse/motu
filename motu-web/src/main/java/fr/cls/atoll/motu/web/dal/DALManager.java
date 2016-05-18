package fr.cls.atoll.motu.web.dal;

import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.web.dal.config.DALConfigManager;
import fr.cls.atoll.motu.web.dal.config.IDALConfigManager;
import fr.cls.atoll.motu.web.dal.request.DALRequestManager;
import fr.cls.atoll.motu.web.dal.request.IDALRequestManager;
import fr.cls.atoll.motu.web.dal.users.DALUserManager;
import fr.cls.atoll.motu.web.dal.users.IDALUserManager;
import fr.cls.atoll.motu.web.usl.utils.log4j.Log4JInitializer;

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
    }

    @Override
    public void init() throws MotuException {
        // Init log4j
        Log4JInitializer.init(null);

        dalConfigManager.init();
        dalUserManager.init();
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

}
