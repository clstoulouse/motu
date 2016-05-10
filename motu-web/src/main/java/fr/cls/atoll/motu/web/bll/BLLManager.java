package fr.cls.atoll.motu.web.bll;

import fr.cls.atoll.motu.web.bll.config.BLLConfigManager;
import fr.cls.atoll.motu.web.bll.config.IBLLConfigManager;

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

    public static IBLLManager getInstance() {
        if (s_instance == null) {
            s_instance = new BLLManager();
        }
        return s_instance;
    }

    public BLLManager() {
        configManager = new BLLConfigManager();
    }

    @Override
    public void init() {
    }

    /** {@inheritDoc} */
    @Override
    public IBLLConfigManager getConfigManager() {
        return configManager;
    }

}
