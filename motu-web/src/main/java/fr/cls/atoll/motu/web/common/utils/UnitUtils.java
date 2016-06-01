package fr.cls.atoll.motu.web.common.utils;

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
public class UnitUtils {

    public static double toMegaBytes(long sizeInBits) {
        return sizeInBits / (1024 * 1024);
    }

    public static double toBytes(double sizeInMegaBytes) {
        return sizeInMegaBytes * (1024 * 1024);
    }

}
