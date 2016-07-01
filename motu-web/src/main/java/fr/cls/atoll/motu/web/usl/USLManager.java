package fr.cls.atoll.motu.web.usl;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.usl.request.IUSLRequestManager;
import fr.cls.atoll.motu.web.usl.request.USLRequestManager;
import fr.cls.atoll.motu.web.usl.response.velocity.VelocityTemplateManager;
import fr.cls.atoll.motu.web.usl.user.IUSLUserManager;
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
public class USLManager implements IUSLManager {

    private static IUSLManager s_instance;
    private USLUserManager userManager;
    private IUSLRequestManager requestManager;

    public static IUSLManager getInstance() {
        if (s_instance == null) {
            s_instance = new USLManager();
        }
        return s_instance;
    }

    private USLManager() {
        userManager = new USLUserManager();
        requestManager = new USLRequestManager();
    }

    @Override
    public void init() throws MotuException {
        try {
            VelocityTemplateManager.getInstance().init();
            requestManager.init();
        } catch (Exception e) {
            throw new MotuException("Error while initializing velocity template", e);
        }
    }

    /**
     * Valeur de userManager.
     * 
     * @return la valeur.
     */
    @Override
    public IUSLUserManager getUserManager() {
        return userManager;
    }

    /**
     * Valeur de requestManager.
     * 
     * @return la valeur.
     */
    @Override
    public IUSLRequestManager getRequestManager() {
        return requestManager;
    }

}
