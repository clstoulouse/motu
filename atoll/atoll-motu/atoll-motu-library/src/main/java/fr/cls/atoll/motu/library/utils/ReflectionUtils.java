package fr.cls.atoll.motu.library.utils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.List;

import javax.xml.bind.annotation.XmlSchema;

/**
 * <br><br>Copyright : Copyright (c) 2009.
 * <br><br>Société : CLS (Collecte Localisation Satellites)
 * @author $Author: jarnaud $
 * @version $Revision: 1.3 $ - $Date: 2009-10-15 15:32:39 $
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

        for (Field field: type.getDeclaredFields()) {
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
    
    public static Enumeration makeEnumeration(final Object obj) {
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
