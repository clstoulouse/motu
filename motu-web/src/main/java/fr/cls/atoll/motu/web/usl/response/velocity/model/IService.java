package fr.cls.atoll.motu.web.usl.response.velocity.model;

import fr.cls.atoll.motu.web.usl.response.velocity.model.catalog.ICatalog;

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
public interface IService extends ICommonService {

    String getGroup();

    String getDescription();

    String getNameEncoded();

    String getCatalogType();

    ICatalog getCatalog();

    boolean isDownloadOnTop();
}
