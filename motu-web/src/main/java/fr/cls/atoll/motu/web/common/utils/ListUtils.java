package fr.cls.atoll.motu.web.common.utils;

import java.util.List;

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
public class ListUtils {

    private ListUtils() {
    }

    public static boolean isNullOrEmpty(List<?> value) {
        return value == null || value.isEmpty();
    }

    public static double[] findMinMax(List<double[]> dArList) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (double[] latValues : dArList) {
            for (double value : latValues) {
                if (value < min) {
                    min = value;
                } else if (value > max) {
                    max = value;
                }
            }

        }
        return new double[] { min, max };
    }
}
