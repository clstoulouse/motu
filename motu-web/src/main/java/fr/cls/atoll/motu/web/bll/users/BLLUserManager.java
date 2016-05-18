package fr.cls.atoll.motu.web.bll.users;

import java.util.Properties;

import fr.cls.atoll.motu.web.dal.DALManager;
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
public class BLLUserManager implements IBLLUserManager {

    private IDALUserManager dalUserManager;

    public BLLUserManager() {
        dalUserManager = DALManager.getInstance().getUserManager();
    }

    @Override
    public Properties getAuthenticationProps() {
        return dalUserManager.getAuthenticationProps();
    }

}