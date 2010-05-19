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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.List;

import javax.xml.bind.annotation.XmlSchema;

/**
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:dearith@cls.fr">Didier Earith</a>
 */
public class ReflectionUtils {

    /**
     * Instantiates a new reflect utils.
     */
    protected ReflectionUtils() {

    }

    /**
     * Gets the all fields.
     * 
     * @param fields the fields
     * @param type the type
     * 
     * @return the all fields
     */
    public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        if (type.getSuperclass() != null) {
            fields = getAllFields(fields, type.getSuperclass());
        }

        for (Field field : type.getDeclaredFields()) {
            fields.add(field);
        }

        return fields;
    }

    /**
     * Gets the xml schema namespace.
     * 
     * @param clazz the clazz
     * 
     * @return the xml schema namespace
     */
    public static String getXmlSchemaNamespace(Class<?> clazz) {

        AnnotatedElement pack = clazz.getPackage();
        if (pack == null) {
            return "";
        }

        XmlSchema schema = pack.getAnnotation(XmlSchema.class);
        String namespace = null;
        if (schema != null) {
            namespace = schema.namespace();
        } else {
            namespace = "";
        }
        return namespace;

    }

    /**
     * Make enumeration.
     * 
     * @param obj the obj
     * 
     * @return the enumeration<?>
     */
    @SuppressWarnings("unchecked")
    public static Enumeration<?> makeEnumeration(final Object obj) {
        Class<?> type = obj.getClass();
        if (!type.isArray()) {
            throw new IllegalArgumentException(obj.getClass().toString());
        } else {
            return (new Enumeration() {
                int size = Array.getLength(obj);

                int cursor;

                public boolean hasMoreElements() {
                    return (cursor < size);
                }

                public Object nextElement() {
                    return Array.get(obj, cursor++);
                }
            });
        }
    }

}
