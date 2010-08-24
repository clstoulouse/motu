package fr.cls.atoll.motu.library.misc.utils;

import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
