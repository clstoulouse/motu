package fr.cls.atoll.motu.web.bll.catalog;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.config.xml.model.ConfigService;
import fr.cls.atoll.motu.web.dal.request.netcdf.data.CatalogData;

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
public interface IBLLCatalogManager {

    /**
     * .
     * 
     * @param cs
     * 
     * @return
     * @throws MotuException
     */
    CatalogData getCatalogData(ConfigService cs) throws MotuException;

}
