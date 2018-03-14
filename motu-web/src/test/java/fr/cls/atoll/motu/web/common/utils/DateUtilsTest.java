package fr.cls.atoll.motu.web.common.utils;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * . <br>
 * <br>
 * Copyright : Copyright (c) 2017 <br>
 * <br>
 * Company : CLS (Collecte Localisation Satellites)
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1456 $ - $Date: 2011-04-08 18:37:34 +0200 $
 */
public class DateUtilsTest {

    /**
     * Test method for {@link fr.cls.atoll.motu.web.common.utils.DateUtils#getDurationISO8601(long)}.
     */
    @Test
    public void testGetDurationISO8601() {
        // @formatter:off
        // Time testing
        assertTrue(DateUtils.getDurationISO8601(1).equalsIgnoreCase("PT.001"));
        assertTrue(DateUtils.getDurationISO8601(1000).equalsIgnoreCase("PT1S"));
        assertTrue(DateUtils.getDurationISO8601(60000).equalsIgnoreCase("PT1M"));
        assertTrue(DateUtils.getDurationISO8601(3600000).equalsIgnoreCase("PT1H"));

        assertTrue(DateUtils.getDurationISO8601(2 * DateUtils.ONE_SECOND_IN_MILLI + 44).equalsIgnoreCase("PT2S.044"));
        assertTrue(DateUtils.getDurationISO8601(2 * DateUtils.ONE_MINUTE_IN_MILLI + 3 * DateUtils.ONE_SECOND_IN_MILLI + 44)
                .equalsIgnoreCase("PT2M3S.044"));
        assertTrue(DateUtils
                .getDurationISO8601(2 * DateUtils.ONE_HOUR_IN_MILLI + (2 * DateUtils.ONE_MINUTE_IN_MILLI + 3 * DateUtils.ONE_SECOND_IN_MILLI + 44))
                .equalsIgnoreCase("PT2H2M3S.044"));

        // DateTime testing
        assertTrue(DateUtils
                .getDurationISO8601(6 * DateUtils.ONE_DAY_IN_MILLI
                        + (2 * DateUtils.ONE_HOUR_IN_MILLI + 2 * DateUtils.ONE_MINUTE_IN_MILLI + 3 * DateUtils.ONE_SECOND_IN_MILLI + 44))
                .equalsIgnoreCase("P6DT2H2M3S.044"));
        assertTrue(DateUtils
                .getDurationISO8601(DateUtils.ONE_MONTH_IN_MILLI + 6 * DateUtils.ONE_DAY_IN_MILLI
                        + (2 * DateUtils.ONE_HOUR_IN_MILLI + 2 * DateUtils.ONE_MINUTE_IN_MILLI + 3 * DateUtils.ONE_SECOND_IN_MILLI + 44))
                .equalsIgnoreCase("P1M6DT2H2M3S.044"));
        assertTrue(DateUtils
                .getDurationISO8601(DateUtils.ONE_YEAR_IN_MILLI
                        + (2 * DateUtils.ONE_HOUR_IN_MILLI + 2 * DateUtils.ONE_MINUTE_IN_MILLI + 3 * DateUtils.ONE_SECOND_IN_MILLI + 44))
                .equalsIgnoreCase("P1YT2H2M3S.044"));
        assertTrue(DateUtils
                .getDurationISO8601(DateUtils.ONE_YEAR_IN_MILLI + 3 * DateUtils.ONE_MONTH_IN_MILLI
                        + (2 * DateUtils.ONE_HOUR_IN_MILLI + 2 * DateUtils.ONE_MINUTE_IN_MILLI + 3 * DateUtils.ONE_SECOND_IN_MILLI + 44))
                .equalsIgnoreCase("P1Y3MT2H2M3S.044"));
        assertTrue(DateUtils
                .getDurationISO8601(DateUtils.ONE_YEAR_IN_MILLI + 3 * DateUtils.ONE_MONTH_IN_MILLI + 5 * DateUtils.ONE_DAY_IN_MILLI
                        + (2 * DateUtils.ONE_HOUR_IN_MILLI + 2 * DateUtils.ONE_MINUTE_IN_MILLI + 3 * DateUtils.ONE_SECOND_IN_MILLI + 44))
                .equalsIgnoreCase("P1Y3M5DT2H2M3S.044"));
        // @formatter:on
    }

}
