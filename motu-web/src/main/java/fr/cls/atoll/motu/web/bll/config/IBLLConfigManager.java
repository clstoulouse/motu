package fr.cls.atoll.motu.web.bll.config;

import fr.cls.atoll.motu.library.misc.configuration.MotuConfig;

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
public interface IBLLConfigManager {

    /**
     * .
     * 
     * @return null if no cas server is used, otherwise the Cas server url
     */
    String getCasServerUrl();

    boolean isCasActivated();

    /**
     * .
     * 
     * @return
     */
    IBLLQueueServerConfigManager getQueueServerConfigManager();

    /**
     * .
     * 
     * @return
     */
    boolean isStatusAsFile();

    /**
     * .
     * 
     * @return
     */
    MotuConfig getMotuConfig();

    /**
     * .
     * 
     * @return
     */
    String getProductDownloadHttpUrl();
}
