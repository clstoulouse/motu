package fr.cls.atoll.motu.web.common.utils;

public class CoordinateUtils {

    /**
     * Return a longitude in the bounds [-180;180] using modulo 360
     * 
     * @param longitude
     * @return
     */
    public static double getLongitudeM180P180(double longitude) {
        double newLong = longitude % 360;
        if (newLong > 180) {
            newLong -= 360;
        }
        if (newLong < -180) {
            newLong += 360;
        }
        return newLong;
    }

    /**
     * .
     * 
     * @param longitude
     * @param longitudeMin Has to be greater than -180
     * @return
     */
    public static double getLongitudeGreaterOrEqualsThanLongitudeMin(double longitude, double longitudeMin) {
        double newLong = getLongitudeM180P180(longitude);
        if (longitude != (longitudeMin + 360)) {
            while (newLong > longitudeMin) {
                newLong -= 360;
            }
            while (newLong < longitudeMin) {
                newLong += 360;
            }
        }
        return newLong;
    }

    public static double getLongitudeJustLowerThanLongitudeMax(double longitude, double axisXMax) {
        double newLong = getLongitudeM180P180(longitude);
        if (longitude != (axisXMax - 360.0)) {
            while (newLong < axisXMax) {
                newLong += 360.0;
            }
            while (newLong > axisXMax) {
                newLong -= 360.0;
            }
        }
        return newLong;
    }
}
