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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import fr.cls.atoll.motu.library.misc.exception.MotuException;

/**
 * Class that handles the configuration resources fetching. For each resource, the search is performed on
 * external directories, then inside the classpath.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:jcarbou@cls.fr">Jerome Carbou</a>
 */
public class ConfigLoader {
    private static final String CONFIG_LOADER = "configLoader.properties";

    public static final ConfigLoader instance = new ConfigLoader();

    /**
     * Retourne l'instance unique .
     * 
     * @return instance
     */
    public static final ConfigLoader getInstance() {
        return instance;
    }

    public Set pathList = new TreeSet();

    /**
     * Constructeur.
     */
    private ConfigLoader() {
        super();
        InputStream in = null;
        in = ResourcesLoader.getAsStream(CONFIG_LOADER);
        if (in != null) {
            Properties properties = new Properties();
            try {
                properties.load(in);
                PropertiesUtilities.filterPropertiesByPrefix(properties, "path");
                for (Iterator iter = properties.values().iterator(); iter.hasNext();) {
                    String path = (String) iter.next();
                    addPath(path);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Rajout dans les paths, le contenu de la propriété systeme
        // config.loader.path si elle a été positionnée
        String configLoaderPath = System.getProperty("config.loader.path");
        if (configLoaderPath != null) {
            String[] paths = configLoaderPath.split(";");
            for (int i = 0; i < paths.length; ++i) {
                addPath(paths[i]);
            }
        }
    }

    /**
     * Ajoute un répertoire externe.
     * 
     * @param path
     */
    public void addPath(String path) {
        try {
            path = PropertiesUtilities.replaceSystemVariable(path);
        } catch (NoSuchElementException ex) {
            path = null;
        }
        if (path != null) {
            pathList.add(path);
        }
    }

    /**
     * Recherche la resource dans les répertoires externes puis dans le classpath.
     * 
     * @param resourceName nom de la ressource
     * @param loader classLoader à utiliser
     * @return la resource trouvée
     * @throws IOException en cas d'erreur d'accès à la resource
     */
    public URL get(String resourceName, ClassLoader loader) throws IOException {
        for (Iterator it = pathList.iterator(); it.hasNext();) {
            String path = (String) it.next();
            File file = new File(path, resourceName);
            if (file.exists()) {
                return file.toURI().toURL();
            }
        }

        URL url = ResourcesLoader.get(loader, resourceName);

        if (url == null) {
            File file = new File(resourceName);
            if (file.exists()) {
                url = file.toURI().toURL();
            }
        }
        return url;
    }

    /**
     * Recherche la resource dans les répertoires externes puis dans le classpath.
     * 
     * @param resourceName nom de la ressource
     * @return la resource trouvée
     * @throws IOException en cas d'erreur d'accès à la resource
     */
    public URL get(String resourceName) throws IOException {
        return get(resourceName, getClass().getClassLoader());
    }

    /**
     * Recherche la resource dans les répertoires externes puis dans le classpath et remplace les variables
     * systemes.
     * 
     * @param resourceName nom de la ressource
     * @param loader classLoader à utiliser
     * @return la resource trouvée
     * @throws IOException en cas d'erreur d'accès à la resource
     * @throws MotuException 
     */
    public InputStream getAsStream(String resourceName, ClassLoader loader) throws IOException, MotuException {
        InputStream inputStream = getAsStreamWithtoutReplacingSystemVariable(resourceName, loader);
        if (inputStream == null) {
            throw new MotuException(String.format("ERROR - ConfigLoader#getAsStream - Resource name '%s' doesn't exist or can't be loaded", resourceName));
        }
        
        return PropertiesUtilities.replaceSystemVariable(getAsStreamWithtoutReplacingSystemVariable(resourceName, loader));
    }

    /**
     * Recherche la resource dans les répertoires externes puis dans le classpath et remplace les variables
     * systemes.
     * 
     * @param resourceName nom de la ressource
     * @return la resource trouvée
     * @throws IOException en cas d'erreur d'accès à la resource
     * @throws MotuException 
     */
    public InputStream getAsStream(String resourceName) throws IOException, MotuException {
        return getAsStream(resourceName, getClass().getClassLoader());
    }

    /**
     * Recherche la resource dans les répertoires externes puis dans le classpath.
     * 
     * @param resourceName nom de la ressource
     * @param loader classLoader à utiliser
     * @return la resource trouvée
     * @throws IOException en cas d'erreur d'accès à la resource
     */
    private InputStream getAsStreamWithtoutReplacingSystemVariable(String resourceName, ClassLoader loader) throws IOException {
        for (Iterator it = pathList.iterator(); it.hasNext();) {
            String path = (String) it.next();
            File file = new File(path, resourceName);
            if (file.exists()) {
                return new FileInputStream(file);
            }
        }

        InputStream in = ResourcesLoader.getAsStream(loader, resourceName);

        if (in == null) {
            File file = new File(resourceName);
            if (file.exists()) {
                in = new FileInputStream(resourceName);
            }
        }
        return in;
    }
}
