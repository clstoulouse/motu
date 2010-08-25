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
public class ListUtils<K> {

    public <V> Map<String, V> toMap(Collection<V> list, String... keyFields) throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {
        return toMap(list, Arrays.asList(keyFields));
    }

    public <V> Map<String, V> toMap(Collection<V> list, Iterable<String> keyFields) throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {
        if (!(keyFields.iterator().hasNext())) {
            return null;
        }

        Map<String, V> map = new HashMap<String, V>();

        for (V obj : list) {
            String key = ObjectUtils.getValueAsString(obj, keyFields);
            map.put(key, obj);
        }
        return map;
    }

    public <V> Map<K, V> toMap(Collection<V> list, String keyField) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // String accessor = convertFieldToAccessor(keyField);
        Map<K, V> map = new HashMap<K, V>();
        for (V obj : list) {
            // Method method = obj.getClass().getDeclaredMethod(accessor);
            // K key = (K) method.invoke(obj);            
            K key = new ObjectUtils<K>().getValue(obj, keyField);
            map.put(key, obj);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    public K getValue(Object object, String keyField) throws IllegalArgumentException, SecurityException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        BeanWrapper wrapper = new BeanWrapperImpl(object);
        return (K) wrapper.getPropertyValue(keyField);
    }

    public static <T> boolean isEqualList(final List<T> list1, final List<T> list2, Comparator<? super T> c, boolean sortList) {
        if (list1 == list2) {
            return true;
        }
        if (list1 == null || list2 == null || list1.size() != list2.size()) {
            return false;
        }

        if (sortList) {
            Collections.sort(list1, c);
            Collections.sort(list2, c);
        }

        Iterator<T> it1 = list1.iterator();
        Iterator<T> it2 = list2.iterator();
        T obj1 = null;
        T obj2 = null;

        while (it1.hasNext() && it2.hasNext()) {
            obj1 = it1.next();
            obj2 = it2.next();

            if ((obj1 == null) && (obj2 != null)) {
                return false;
            }
            if ((obj1 != null) && (obj2 == null)) {
                return false;
            }
            if (c.compare(obj1, obj2) != 0) {
                return false;
            }
        }

        return !(it1.hasNext() || it2.hasNext());
    }
}
