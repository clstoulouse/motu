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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import fr.cls.atoll.motu.library.misc.exception.MotuException;

/**
 *Utilitary class that provides methods for managing system properties.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:jfarenc@cls.fr">Jean-Michel Farenc</a>
 * @author <a href="mailto:jcarbou@cls.fr">Jerome Carbou</a>
 */
public class PropertiesUtilities {
    /**
     * Replace in a string all variables (declared by <tt>${var}<tt>) with their corresponding values.
     * <p>
     * 
     * @param value string for which declared variables are replaced
     * @return the substituted string.
     */

    static public String replaceSystemVariable(String value) {
        StringBuffer substValue = new StringBuffer();
        int idx = 0;
        int startIdx;
        int endIdx;
        String varName;
        String varValue;
        while ((startIdx = value.indexOf("${", idx)) != -1) {
            endIdx = value.indexOf("}", startIdx + 2);
            if (endIdx != -1) {
                varName = value.substring(startIdx + 2, endIdx);
                varValue = System.getProperty(varName);
                if (varValue == null) {
                    throw new NoSuchElementException("no system property found for " + varName + " in " + value);
                }
                if (startIdx > 0) {
                    substValue.append(value.substring(idx, startIdx));
                }
                // Treat correctly \
                varValue = StringUtils.replace(varValue, "\\", "\\\\");
                substValue.append(varValue);
                idx = endIdx + 1;
            } else {
                substValue.append(value.substring(idx));
                idx = value.length();
            }
        }
        substValue.append(value.substring(idx));
        return substValue.toString();
    }

    /**
     * Delete from <code>props</code> every property for which the key is not prefixed by one elements from
     * <code>prefixes</code>.
     * 
     * @param props Properties
     * @param prefixes prefixes array
     * @return <code>props</code> eventyally modified.
     */
    public static Properties filterPropertiesByPrefixes(Properties props, String[] prefixes) {
        if (props == null) {
            return null;
        }
        for (Iterator iter = props.keySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            boolean pass = false;
            for (int i = 0; i < prefixes.length; i++) {
                String prefix = prefixes[i];
                if (key.startsWith(prefix)) {
                    pass = true;
                    break;
                }
            }
            if (!pass) {
                iter.remove();
            }
        }
        return props;
    }

    /**
     * Remove from <code>props</code> every properties for which key is not prefixed by the given
     * <code>prefix</code>.
     * 
     * @param props Properties to filter.
     * @param prefix Prefix used to filter
     * @return <code>props</code> eventually altered.
     */
    public static Properties filterPropertiesByPrefix(Properties props, String prefix) {
        return filterPropertiesByPrefix(props, prefix, props);
    }

    /**
     * Remove from <code>props</code> every properties for which key is not prefixed by the given
     * <code>prefix</code>.
     * 
     * @param props Properties to filter.
     * @param prefix Prefix used to filter
     * @param target properties destination
     * @return <code>props</code> eventually altered.
     */
    public static Properties filterPropertiesByPrefix(Properties props, String prefix, Properties target) {
        if (props == null) {
            return null;
        }
        boolean modify = (props == target);
        for (Iterator iter = props.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            if (key.startsWith(prefix)) {
                if (!modify) {
                    target.put(entry.getKey(), entry.getValue());
                }
            } else {
                if (modify) {
                    iter.remove();
                }
            }
        }
        return props;
    }

    /**
     * Replace from an input stream the names of system variables that follows the scheme ${var}
     * <tt> by their corresponding values.
     * 
     * @param in the input stream for which the variables has to be replaced.
     * @return a brand new imput stream with system variables replaced with values.
     * @throws IOException
     */
    static public InputStream replaceSystemVariable(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            IOUtils.copy(in, out);
        } finally {
            IOUtils.closeQuietly(out);
        }

        String inStr = out.toString();
        String outStr = replaceSystemVariable(inStr);

        return new ByteArrayInputStream(outStr.getBytes());
    }

    /**
     * Load a file from the classpath and returns a <tt>java.util.Properties</tt> instance.
     * 
     * @param propertiesFileResource property file
     * @return <tt>java.util.Properties</tt> instance loaded
     * @see java.util.Properties#load(java.io.InputStream)
     * @throws IOException if file is not a <tt>.properties</tt> file or if it is not found in the classpath.
     * @throws MotuException 
     */
    public static Properties loadFromClasspath(String propertiesFileResource) throws IOException, MotuException {
        Properties props = new Properties();
        InputStream in = ConfigLoader.getInstance().getAsStream(propertiesFileResource);
        if (in == null) {
            throw new FileNotFoundException("The resource file " + propertiesFileResource + " has not been found in the classpath");
        }
        try {
            props.load(in);
        } finally {
            in.close();
        }
        replaceSystemVariable(props);
        return props;
    }

    /**
     * Replace in the properties values the name of the system variable that follows the scheme
     * <tt>${var}<tt> with their corresponding values.
     * 
     * @param props properties to substitute.
     */
    static public void replaceSystemVariable(Properties props) {
        Iterator it;
        Map.Entry entry;
        String value;
        for (it = props.entrySet().iterator(); it.hasNext();) {
            entry = (Map.Entry) it.next();
            try {
                value = replaceSystemVariable((String) entry.getValue());
            } catch (NoSuchElementException ex) {
                throw new NoSuchElementException(ex.getMessage() + " subsitution for property " + (String) entry.getKey());
            }
            entry.setValue(value);
        }
    }
}
