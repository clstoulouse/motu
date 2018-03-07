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
            //Time testing
        assertTrue(DateUtils.getDurationISO8601(       1).equalsIgnoreCase("PT00:00:00.001"));
        assertTrue(DateUtils.getDurationISO8601(    1000).equalsIgnoreCase("PT00:00:01"));
        assertTrue(DateUtils.getDurationISO8601(   60000).equalsIgnoreCase("PT00:01"));
        assertTrue(DateUtils.getDurationISO8601( 3600000).equalsIgnoreCase("PT01"));
        
        assertTrue(DateUtils.getDurationISO8601(  2*1000+44).equalsIgnoreCase("PT00:00:02.044"));
        assertTrue(DateUtils.getDurationISO8601( 2*60*1000+3*1000+44).equalsIgnoreCase("PT00:02:03.044"));
        assertTrue(DateUtils.getDurationISO8601(2*60*60*1000 + (2*60*1000+3*1000+44)).equalsIgnoreCase("PT02:02:03.044"));
        
            //DateTime testing
        assertTrue(DateUtils.getDurationISO8601(6*24*60*60*1000 + (2*60*60*1000 + 2*60*1000+3*1000+44)).equalsIgnoreCase("P6DT02:02:03.044"));
        assertTrue(DateUtils.getDurationISO8601(2628000000L + 6*24*60*60*1000 + (2*60*60*1000 + 2*60*1000+3*1000+44)).equalsIgnoreCase("P1M6DT02:02:03.044"));
        assertTrue(DateUtils.getDurationISO8601(365*24*60*60*1000L + (2*60*60*1000 + 2*60*1000+3*1000L+44)).equalsIgnoreCase("P1YT02:02:03.044"));
        assertTrue(DateUtils.getDurationISO8601(365*24*60*60*1000L + 3*2628000000L + (2*60*60*1000 + 2*60*1000+3*1000L+44)).equalsIgnoreCase("P1Y3MT02:02:03.044"));
        assertTrue(DateUtils.getDurationISO8601(365*24*60*60*1000L + 3*2628000000L + 5*24*60*60*1000L + (2*60*60*1000 + 2*60*1000+3*1000L+44)).equalsIgnoreCase("P1Y3M5DT02:02:03.044"));
        // @formatter:on
    }

}
