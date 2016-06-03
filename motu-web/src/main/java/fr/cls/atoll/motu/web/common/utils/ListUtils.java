package fr.cls.atoll.motu.web.common.utils;

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
public class ListUtils {

    public static boolean isNullOrEmpty(List<?> value) {
        return value == null || value.size() <= 0;
    }

}
