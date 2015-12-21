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
package fr.cls.atoll.motu.library.misc.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * <p>
 * ListUtils utilities
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class ObjectUtils<K> {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(ObjectUtils.class);

    @SuppressWarnings("unchecked")
    public K getValue(Object object, String keyField) throws IllegalArgumentException, SecurityException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getValue(Object, String) - start");
        }

        BeanWrapper wrapper = new BeanWrapperImpl(object);
        K returnK = (K) wrapper.getPropertyValue(keyField);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getValue(Object, String) - end");
        }
        return returnK;
    }

    public static String getValueAsString(Object object, String keyField) throws IllegalArgumentException, SecurityException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getValueAsString(Object, String) - start");
        }

        BeanWrapper wrapper = new BeanWrapperImpl(object);
        String returnString = (String) wrapper.getPropertyValue(keyField);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getValueAsString(Object, String) - end");
        }
        return returnString;
    }

    public static String getValueAsString(Object object, String... keyFields) throws IllegalArgumentException, SecurityException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getValueAsString(Object, String) - start");
        }

        String returnString = ObjectUtils.getValueAsString(object, Arrays.asList(keyFields));
        if (LOG.isDebugEnabled()) {
            LOG.debug("getValueAsString(Object, String) - end");
        }
        return returnString;
    }

    public static String getValueAsString(Object object, Iterable<String> keyFields) throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, SecurityException, NoSuchMethodException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getValueAsString(Object, Iterable<String>) - start");
        }

        if (!(keyFields.iterator().hasNext())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getValueAsString(Object, Iterable<String>) - end");
            }
            return null;
        }

        BeanWrapper wrapper = new BeanWrapperImpl(object);

        StringBuilder stringBuilder = new StringBuilder();

        for (String keyField : keyFields) {
            Object value = wrapper.getPropertyValue(keyField);
            stringBuilder.append(value);
        }

        String returnString = stringBuilder.toString();
        if (LOG.isDebugEnabled()) {
            LOG.debug("getValueAsString(Object, Iterable<String>) - end");
        }
        return returnString;

    }

}
