package fr.cls.atoll.motu.web.usl;

import fr.cls.atoll.motu.library.misc.exception.MotuException;
import fr.cls.atoll.motu.web.usl.request.IUSLRequestManager;
import fr.cls.atoll.motu.web.usl.user.USLUserManager;

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
public interface IUSLManager {

    /**
     * .
     * 
     * @throws Exception
     */
    void init() throws MotuException;

    /**
     * .
     * 
     * @return
     */
    USLUserManager getUserManager();

    /**
     * .
     * 
     * @return
     */
    IUSLRequestManager getRequestManager();

}
