package fr.cls.atoll.motu.web.dal.config;

import java.util.List;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.config.stdname.xml.model.StandardName;
import fr.cls.atoll.motu.web.dal.config.xml.model.MotuConfig;

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
public interface IDALConfigManager {

    /**
     * .
     * 
     * @return
     */
    String getCasServerUrl();

    /**
     * .
     * 
     * @return
     */
    boolean isCasActivated();

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
    MotuConfig getMotuConfig();

    /**
     * .
     * 
     * @return
     */
    List<StandardName> getStandardNameList();

    /**
     * .
     * 
     * @return
     */
    String getMotuConfigurationFolderPath();

}
