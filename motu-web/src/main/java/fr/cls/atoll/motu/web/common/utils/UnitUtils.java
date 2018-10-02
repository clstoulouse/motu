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
 * 
 * 
 *          There are really 1024 bytes in a kilobyte. The reason for this is because computers are based on
 *          the binary system. That means hard drives and memory are measured in powers of 2 2^0 = 1, 2^1 = 2,
 *          2^2 = 4, ..., 2^10 = 1024 Therefore, 2^10, or 1024 bytes compose one kilobyte. 1024*1024=1048576
 * 
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
        return sizeInByte / 1024;
    }

    public static double byteToMegaByte(double sizeInByte) {
        return sizeInByte / 1048576; // 1024*1024=1048576 2^10=1024
    }

    public static long kilobyteToByte(long sizeInKilobyte) {
        return sizeInKilobyte * 1024;
    }

    public static double kilobyteToMegabyte(double sizeInKilobyte) {
        return sizeInKilobyte / 1024;
    }

    public static double megabyteToByte(double sizeInMegabyte) {
        return sizeInMegabyte * 1048576;//
    }

}
