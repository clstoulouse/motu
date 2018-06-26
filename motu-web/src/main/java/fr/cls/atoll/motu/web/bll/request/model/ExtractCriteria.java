/* 
 * Motu, a high efficient, robust and Standard compliant Web Server for Geographic
 * Data Dissemination.
 *
 * http://cls-motu.sourceforge.net/
 *
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites) - 
 * http://www.cls.fr - and  Contributors
 *
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
/**
 * 
 */
package fr.cls.atoll.motu.web.bll.request.model;

import fr.cls.atoll.motu.web.bll.exception.MotuException;
import ucar.ma2.Array;
import ucar.ma2.MAMath;

/**
 * This class introduces criterias to be apply on data (for extraction/selection and research).
 * 
 * Criteria can have different kinds : geographical coverage, temporal coverage, resolution, data kind data
 * distribution, volumetry, ....
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public abstract class ExtractCriteria {

    /**
     * Default constructor.
     */
    protected ExtractCriteria() {

    }

    /**
     * @param array array from which min an max have to be found
     * @return min an max value of the array
     */
    public static MAMath.MinMax getMinMax(Array array) {
        return MAMath.getMinMax(array);
    }

    /**
     * @param array array from which min an max have to be found
     * @return min an max value of the array
     */
    public static double[] getMinMax(double[] array) {
        double max = -Double.MAX_VALUE;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
            if (array[i] < min) {
                min = array[i];
            }
        }
        return new double[] { min, max };
    }

    /**
     * @param array values from which range is computed
     * @param value value from which the min. index have to be found
     * @return index corresponding to the smallest value nearest in the array, or -1 if array is empty.
     * @throws MotuException
     */
    public static int findMinIndex(double[] array, double value) {
        int index = -1;
        for (int i = 0; i < array.length; i++) {
            if (array[i] >= value) {
                index = i;
                break;
            }
        }

        if ((index == -1) && (array.length > 0)) {
            index = array.length - 1;
        }
        return index;
    }

    /**
     * @param array values from which range is computed
     * @param value value from which the max. index have to be found
     * @return index corresponding to the greatest value nearest in the array, or -1 if array is empty.
     * @throws MotuException
     */
    public static int findMaxIndex(double[] array, double value) {
        int index = -1;
        for (int i = array.length - 1; i >= 0; i--) {
            if (array[i] <= value) {
                index = i;
                break;
            }
        }
        if ((index == -1) && (array.length > 0)) {
            index = 0;
        }
        return index;
    }

    /**
     * @param array values from which range is computed
     * @param value value from which the index have to be found
     * @return index corresponding exactly to the value in the array, or -1 if not found
     * @throws MotuException
     */
    public static int findExactIndex(double[] array, double value) {
        int index = -1;
        for (int i = 0; i < array.length; i++) {
            if (Double.compare(array[i], value) == 0) {
                index = i;
                break;
            }
        }
        return index;
    }

}
