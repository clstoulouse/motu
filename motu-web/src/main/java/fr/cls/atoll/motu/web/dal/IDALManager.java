package fr.cls.atoll.motu.web.dal;

import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.web.dal.config.IDALConfigManager;
import fr.cls.atoll.motu.web.dal.request.IDALRequestManager;
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
public interface IDALManager {

    /**
     * .
     * 
     * @throws MotuException
     */
    void init() throws MotuException;

    /**
     * .
     * 
     * @return
     */
    IDALConfigManager getConfigManager();

    /**
     * .
     * 
     * @return
     */
    IDALRequestManager getRequestManager();

    /**
     * .
     * 
     * @return
     */
    IDALUserManager getUserManager();

}
