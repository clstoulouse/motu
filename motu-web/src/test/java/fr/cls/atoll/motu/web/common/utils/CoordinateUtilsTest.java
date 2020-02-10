package fr.cls.atoll.motu.web.common.utils;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CoordinateUtilsTest {

    @Test
    public void testGetLongitudeM180P180() {
        assertTrue(CoordinateUtils.getLongitudeM180P180(-180.5) == 179.5);
        assertTrue(CoordinateUtils.getLongitudeM180P180(-180) == -180);
        assertTrue(CoordinateUtils.getLongitudeM180P180(0) == 0);
        assertTrue(CoordinateUtils.getLongitudeM180P180(180) == 180);
        assertTrue(CoordinateUtils.getLongitudeM180P180(181.5) == -178.5);
        assertTrue(CoordinateUtils.getLongitudeM180P180(200) == -160);
        assertTrue(CoordinateUtils.getLongitudeM180P180(360) == 0);
        assertTrue(CoordinateUtils.getLongitudeM180P180(380) == 20);
    }

    /**
     * Test method for
     * {@link fr.cls.atoll.motu.web.common.utils.CoordinateUtils#findMinDepthIndex(double[], double)}.
     */
    @Test
    public void testFindMinDepthIndex() {

        double[] depths = { 0.18234, 0.235, 0.24, 0.245, 1.1 };

        assertTrue(CoordinateUtils.findMinDepthIndex(depths, 0.15) == 0);
        assertTrue(CoordinateUtils.findMinDepthIndex(depths, 0.18234) == 0);
        assertTrue(CoordinateUtils.findMinDepthIndex(depths, 0.24) == 2);
        assertTrue(CoordinateUtils.findMinDepthIndex(depths, 0.2401) == 2);
        assertTrue(CoordinateUtils.findMinDepthIndex(depths, 0.239) == 1);
        assertTrue(CoordinateUtils.findMinDepthIndex(depths, 2) == -1);
        assertTrue(CoordinateUtils.findMinDepthIndex(depths, 0.24275) == 2);

    }

    /**
     * Test method for
     * {@link fr.cls.atoll.motu.web.common.utils.CoordinateUtils#findMaxDepthIndex(double[], double)}.
     */
    @Test
    public void testFindMaxDepthIndex() {

        double[] depths = { 0.18234, 0.235, 0.24, 0.245, 1.1 };

        assertTrue(CoordinateUtils.findMaxDepthIndex(depths, 0.16) == -1);
        assertTrue(CoordinateUtils.findMaxDepthIndex(depths, 0.17) == 0);
        assertTrue(CoordinateUtils.findMaxDepthIndex(depths, 0.19) == 0);
        assertTrue(CoordinateUtils.findMaxDepthIndex(depths, 0.191) == 0);
        assertTrue(CoordinateUtils.findMaxDepthIndex(depths, 0.219) == 0);
        assertTrue(CoordinateUtils.findMaxDepthIndex(depths, 0.22) == 1);
        assertTrue(CoordinateUtils.findMaxDepthIndex(depths, 0.24275) == 3);
        assertTrue(CoordinateUtils.findMaxDepthIndex(depths, 1.1) == 4);

    }
}
