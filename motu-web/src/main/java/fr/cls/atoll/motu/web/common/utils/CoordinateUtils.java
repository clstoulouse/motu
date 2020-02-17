package fr.cls.atoll.motu.web.common.utils;

import java.util.Arrays;

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

    /**
     * Compute the maximum index to include at a 1cm tolerance, the 'from' depth', in the input sorted
     * 'depths' array in meter in parameter .
     * 
     * @param depths The sorted array in meter.
     * @param from The depth from which to start.
     * @return The minimum index or -1 if not found
     */
    public static int findMinDepthIndex(double[] depths, double from) {
        int first = Arrays.binarySearch(depths, from);
        if (first < 0) {
            // Extract insertion point
            first = -1 - first;
            // Check at 1 cm for rounding tolerance
            if (first != 0 && (Math.floor(from * 100) - Math.floor(100 * depths[first - 1])) <= 1) {
                first--;
            }
        }
        if (first == depths.length) {
            first = -1;
        }
        return first;
    }

    /**
     * Compute the maximum index to include at a 1cm tolerance, the 'to' depth', in the input sorted 'depths'
     * array in meter in parameter .
     * 
     * @param depths The sorted array in meter.
     * @param to The depth to dig to.
     * @return The maximum index or -1 if not found
     */
    public static int findMaxDepthIndex(double[] depths, double to) {
        int last = Arrays.binarySearch(depths, to);
        if (last < 0) {
            // Extract insertion point
            last = -1 - last;
            // Check at 1 cm for rounding tolerance
            if (last == depths.length || Math.floor(100 * depths[last]) - Math.floor(100 * to) > 1) {
                last--;
            }
        } else if (last == 0 && Math.floor(100 * depths[0]) - Math.floor(100 * to) > 1) {
            last = -1;
        } else if (last == depths.length) {
            last--;
        }
        return last;
    }
}
