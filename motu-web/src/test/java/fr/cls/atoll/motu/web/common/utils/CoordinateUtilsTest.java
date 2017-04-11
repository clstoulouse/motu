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

    @Test
    public void testGetLongitudeJustGreaterThanLongitudeMin() {
        assertTrue(CoordinateUtils.getLongitudeGreaterOrEqualsThanLongitudeMin(-180, 100) == 180);
        assertTrue(CoordinateUtils.getLongitudeGreaterOrEqualsThanLongitudeMin(0, 100) == 360);
        assertTrue(CoordinateUtils.getLongitudeGreaterOrEqualsThanLongitudeMin(360, -180) == 0);
        assertTrue(CoordinateUtils.getLongitudeGreaterOrEqualsThanLongitudeMin(-180, -50) == 180);
        assertTrue(CoordinateUtils.getLongitudeGreaterOrEqualsThanLongitudeMin(-180, 180) == 180);
        assertTrue(CoordinateUtils.getLongitudeGreaterOrEqualsThanLongitudeMin(180, -180) == 180);
    }

    @Test
    public void testGetLongitudeJustLowerThanLongitudeMax() {
        assertTrue(CoordinateUtils.getLongitudeJustLowerThanLongitudeMax(-180, 100) == -180);
        assertTrue(CoordinateUtils.getLongitudeJustLowerThanLongitudeMax(-180, 180) == -180);
        assertTrue(CoordinateUtils.getLongitudeJustLowerThanLongitudeMax(-180, 181) == 180);
    }

}
