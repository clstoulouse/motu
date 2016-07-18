package fr.cls.atoll.motu.web.bll.cache;

import fr.cls.atoll.motu.api.message.xml.ProductMetadataInfo;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Pierre LACOSTE
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public interface IDescribeProductCacheManager {

    void init();

    ProductMetadataInfo getDescribeProduct(String productId);
}
