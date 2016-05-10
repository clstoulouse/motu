package fr.cls.atoll.motu.web.dal;

import fr.cls.atoll.motu.web.dal.config.DALConfigManager;
import fr.cls.atoll.motu.web.dal.config.IDALConfigManager;

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

    public static IDALManager getInstance() {
        if (s_instance == null) {
            s_instance = new DALManager();
        }
        return s_instance;
    }

    public DALManager() {
        dalConfigManager = new DALConfigManager();
    }

    @Override
    public void init() {
    }

    @Override
    public IDALConfigManager getConfigManager() {
        return dalConfigManager;
    }

}
