package fr.cls.atoll.motu.web.usl.response.velocity.model.catalog;

import java.util.List;

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
public interface ICatalogProductMetaData {

    String getProductTypeServiceValue();

    String getProductIdEncoded();

    String getTitle();

    String getLastUpdate();

    String getProductType();

    List<String> getProductSubTypes();

    int compareSubTypes(List<String> subTypeList);

}
