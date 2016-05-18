package fr.cls.atoll.motu.web.bll;

import fr.cls.atoll.motu.web.bll.config.IBLLConfigManager;
import fr.cls.atoll.motu.web.bll.request.IBLLRequestManager;
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
public interface IBLLManager {

    /**
     * .
     */
    void init();

    /**
     * .
     * 
     * @return
     */
    IBLLConfigManager getConfigManager();

    /**
     * .
     * 
     * @return
     */
    IBLLRequestManager getRequestManager();

    /**
     * .
     * 
     * @return
     */
    IBLLUserManager getUserManager();

}
