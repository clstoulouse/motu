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

    public static double bitsToBytes(double sizeInBits) {
        return sizeInBits / 8;
    }

    public static double bytesToBits(double sizeInBytes) {
        return sizeInBytes * 8;
    }

    public static double bitsToMegaBytes(double sizeInBits) {
        return toMegaBytes(bitsToBytes(sizeInBits));
    }

    public static double toMegaBytes(double sizeInByte) {
        return sizeInByte / (1000000);
    }

    public static double toMegaBytes(long sizeInBits) {
        return sizeInBits / (1024 * 1024);
    }

    public static double toBytes(double sizeInMegaBytes) {
        return sizeInMegaBytes * (1024 * 1024);
    }

    /**
     * .
     * 
     * @param maxAllowedSizeInBytes
     * @return
     */
    public static Double toKBytes(double sizeInBytes) {
        return sizeInBytes / 1024;
    }

}
