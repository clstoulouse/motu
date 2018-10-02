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

    private UnitUtils() {
    }

    public static double bitToByte(double sizeInBit) {
        return sizeInBit / 8;
    }

    public static double byteToBit(double sizeInByte) {
        return sizeInByte * 8;
    }

    public static double bitToMegaByte(double sizeInBit) {
        return byteToMegaByte(bitToByte(sizeInBit));
    }

    public static Double bytetoKilobyte(double sizeInByte) {
        return sizeInByte / 1000;
    }

    public static Long bytetoKilobyte(long sizeInByte) {
        return sizeInByte / 1000;
    }

    public static double byteToMegaByte(double sizeInByte) {
        return sizeInByte / 1000000;
    }

    public static long kilobyteToByte(long sizeInKilobyte) {
        return sizeInKilobyte * 1024;
    }

    public static double kilobyteToMegabyte(double sizeInKilobyte) {
        return sizeInKilobyte / 1000;
    }

    public static double megabyteToByte(double sizeInMegabyte) {
        return sizeInMegabyte * 1000000;
    }

}
