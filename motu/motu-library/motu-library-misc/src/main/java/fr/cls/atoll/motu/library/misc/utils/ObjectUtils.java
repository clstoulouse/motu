package fr.cls.atoll.motu.library.misc.utils;

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

    @SuppressWarnings("unchecked")
    public K getValue(Object object, String keyField) throws IllegalArgumentException, SecurityException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        BeanWrapper wrapper = new BeanWrapperImpl(object);
        return (K) wrapper.getPropertyValue(keyField);
    }

    public static String getValueAsString(Object object, String keyField) throws IllegalArgumentException, SecurityException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        BeanWrapper wrapper = new BeanWrapperImpl(object);
        return (String) wrapper.getPropertyValue(keyField);
    }

    public static String getValueAsString(Object object, String... keyFields) throws IllegalArgumentException, SecurityException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return ObjectUtils.getValueAsString(object, Arrays.asList(keyFields));
    }

    public static String getValueAsString(Object object, Iterable<String> keyFields) throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, SecurityException, NoSuchMethodException {
        if (!(keyFields.iterator().hasNext())) {
            return null;
        }

        BeanWrapper wrapper = new BeanWrapperImpl(object);

        StringBuilder stringBuilder = new StringBuilder();

        for (String keyField : keyFields) {
            Object value = wrapper.getPropertyValue(keyField);
            stringBuilder.append(value);
        }

        return stringBuilder.toString();

    }

}
