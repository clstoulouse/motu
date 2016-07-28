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
package fr.cls.atoll.motu.web.dal.request.netcdf.data.vfs;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.swing.ImageIcon;

import org.apache.commons.io.IOUtils;

/**
 * Utility class that declares a set of method for loading resources from several sources.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:jcarbou@cls.fr">Jerome Carbou</a>
 */
public class ResourcesLoader {

    /**
     * Retourne l'URL de la ressource demand�e en utilisant le class loader de la classe ResourceLoader. Si la
     * ressource n'est pas trouv�e dans le classpath null est retourn�.
     * 
     * @param resource chemin COMPLET d'acc�s � la ressource (ex : fr/cls/ui/browser32.gif)
     * @return le flux d'entr�e ou null si non trouv�e
     */
    public static URL get(String resource) {
        return get(ResourcesLoader.class, resource);
    }

    /**
     * Retourne l'URL de la ressource demand�e en utilisant le class loader de la classe sp�cifi�e. Si la
     * ressource n'est pas trouv�e dans le classpath null est retourn�.
     * 
     * @param source classe � utiliser
     * @param resource chemin COMPLET d'acc�s � la ressource (ex : fr/cls/ui/browser32.gif)
     * @return le flux d'entr�e ou null si non trouv�e
     */
    public static URL get(Class source, String resource) {
        ClassLoader cl = source.getClassLoader();
        if (cl == null) {
            return ClassLoader.getSystemResource(resource);
        } else {
            return cl.getResource(resource);
        }
    }

    /**
     * Retourne l'URL de la ressource demand�e en utilisant le class loader de la classe sp�cifi�e. Si la
     * ressource n'est pas trouv�e dans le classpath null est retourn�.
     * 
     * @param loader classLoader � utiliser
     * @param resource chemin COMPLET d'acc�s � la ressource (ex : fr/cls/ui/browser32.gif)
     * @return l'URL ou null si non trouv�e
     */
    public static URL get(ClassLoader loader, String resource) {
        if (loader == null) {
            return ClassLoader.getSystemResource(resource);
        } else {
            return loader.getResource(resource);
        }
    }

    /**
     * Retourne la stream contenant la ressource demand�e en utilisant le class loader de la classe
     * ResourceLoader. Si la ressource n'est pas trouv�e dans le classpath null est retourn�.
     * 
     * @param resource chemin COMPLET d'acc�s � la ressource (ex : fr/cls/ui/browser32.gif)
     * @return le flux d'entr�e ou null si non trouv�e
     */
    public static InputStream getAsStream(String resource) {
        return getAsStream(ResourcesLoader.class, resource);
    }

    /**
     * Retourne la stream contenant la ressource demand�e en utilisant le class loader de la classe sp�cifi�e.
     * Si la ressource n'est pas trouv�e dans le classpath null est retourn�.
     * 
     * @param source classe � utiliser
     * @param resource chemin COMPLET d'acc�s � la ressource (ex : fr/cls/ui/browser32.gif)
     * @return le flux d'entr�e ou null si non trouv�e
     */
    public static InputStream getAsStream(Class source, String resource) {
        ClassLoader cl = source.getClassLoader();
        if (cl == null) {
            return ClassLoader.getSystemResourceAsStream(resource);
        } else {
            return cl.getResourceAsStream(resource);
        }
    }

    /**
     * Retourne un fichier properties contenant la ressource demand�e. Si la ressource n'est pas trouv�e dans
     * le classpath null est retourn�.
     * 
     * @param resource chemin COMPLET d'acc�s � la ressource (ex : fr/cls/ui/File.properties)
     * @return l'objet properties
     */
    public static Properties getAsProperties(String resource) {
        return getAsProperties(ResourcesLoader.class, resource);
    }

    /**
     * Retourne un fichier properties contenant la ressource demand�e. Si la ressource n'est pas trouv�e dans
     * le classpath la valeur par d�faut est retourn�e.
     * 
     * @param resource chemin COMPLET d'acc�s � la ressource (ex : fr/cls/ui/File.properties)
     * @param defaultValue valeur par d�faut
     * @return l'objet properties
     */
    public static Properties getAsProperties(String resource, Properties defaultValue) {
        return getAsProperties(ResourcesLoader.class, resource, defaultValue);
    }

    /**
     * Retourne un fichier properties contenant la ressource demand�e en utilisant le class loader de la
     * classe sp�cifi�e. Si la ressource n'est pas trouv�e dans le classpath null est retourn�.
     * 
     * @param source classe � utiliser
     * @param resource chemin COMPLET d'acc�s � la ressource (ex : fr/cls/ui/File.properties)
     * @return l'objet properties
     */
    public static Properties getAsProperties(Class source, String resource) {
        return getAsProperties(source, resource, null);
    }

    /**
     * Retourne un fichier properties contenant la ressource demand�e en utilisant le class loader de la
     * classe sp�cifi�e. Si la ressource n'est pas trouv�e dans le classpath la valeur par d�faut est
     * retourn�e.
     * 
     * @param source classe � utiliser
     * @param resource chemin COMPLET d'acc�s � la ressource (ex : fr/cls/ui/File.properties)
     * @param defaultValue valeur par d�faut
     * @return l'objet properties
     */
    public static Properties getAsProperties(Class source, String resource, Properties defaultValue) {
        InputStream in = null;

        try {
            in = getAsStream(source, resource);
            if (in != null) {
                Properties properties = new Properties();
                properties.load(in);
                return properties;
            }
        } catch (Exception e) {
            // Ignorer l'exception
        } finally {
            IOUtils.closeQuietly(in);
        }

        return defaultValue;
    }

    /**
     * Retourne la stream contenant la ressource demand�e en utilisant le class loader sp�cifi�. Si la
     * ressource n'est pas trouv�e dans le classpath null est retourn�.
     * 
     * @param loader classLoader � utiliser
     * @param resource chemin COMPLET d'acc�s � la ressource (ex : fr/cls/ui/browser32.gif)
     * @return le flux d'entr�e ou null si non trouv�e
     */
    public static InputStream getAsStream(ClassLoader loader, String resource) {
        if (loader == null) {
            return ClassLoader.getSystemResourceAsStream(resource);
        } else {
            return loader.getResourceAsStream(resource);
        }
    }

    /**
     * Retourne la ressource demand�e sous la frome d'un icon en utilisant le class loader de la classe
     * ResourceLoader. Si l'icon n'est pas trouv� dans le classpath une exception est lev�e.
     * 
     * @param resource chemin COMPLET d'acc�s � la ressource (ex : fr/cls/ui/browser32.gif)
     * @return l'icon
     */
    public static ImageIcon getAsImageIcon(String resource) {
        return getAsImageIcon(ResourcesLoader.class, resource);
    }

    /**
     * Retourne la ressource demand�e sous la frome d'un icon en utilisant le class loader de la classe
     * sp�cifi�e. Si l'icon n'est pas trouv� dans le classpath une exception est lev�e.
     * 
     * @param source classe � utiliser
     * @param resource chemin COMPLET d'acc�s � la ressource (ex : fr/cls/ui/browser32.gif)
     * @return l'icon
     */
    public static ImageIcon getAsImageIcon(Class source, String resource) {
        URL url = get(source, resource);
        if (url == null) {
            return null;
        }
        return new ImageIcon(url);
    }

    /**
     * Retourne la ressource demand�e sous la frome d'un icon en utilisant le class loader de la classe
     * sp�cifi�e. Si l'icon n'est pas trouv� dans le classpath une exception est lev�e.
     * 
     * @param loader classLoader � utiliser
     * @param resource chemin COMPLET d'acc�s � la ressource (ex : fr/cls/ui/browser32.gif)
     * @return l'icon
     */
    public static ImageIcon getAsImageIcon(ClassLoader loader, String resource) {
        return new ImageIcon(get(loader, resource));
    }

    /**
     * Retourne la ressource demand�e sous la frome d'un icon en utilisant le class loader de la classe
     * ResourceLoader. Si l'icon n'est pas trouv� dans le classpath null est retourn�.
     * 
     * @param resource chemin COMPLET d'acc�s � la ressource (ex : fr/cls/ui/browser32.gif)
     * @return l'icon ou null si non trouv�
     */
    public static ImageIcon getAsImageIconOrNull(String resource) {
        return getAsImageIconOrNull(ResourcesLoader.class, resource);
    }

    /**
     * Retourne la ressource demand�e sous la frome d'un icon en utilisant le class loader de la classe
     * sp�cifi�e. Si l'icon n'est pas trouv� dans le classpath null est retourn�.
     * 
     * @param source classe � utiliser
     * @param resource chemin COMPLET d'acc�s � la ressource (ex : fr/cls/ui/browser32.gif)
     * @return l'icon ou null si non trouv�
     */
    public static ImageIcon getAsImageIconOrNull(Class source, String resource) {
        URL url = get(source, resource);
        return (url == null) ? null : new ImageIcon(url);
    }

    /**
     * Retourne la ressource demand�e sous la frome d'un icon en utilisant le class loader de la classe
     * sp�cifi�e. Si l'icon n'est pas trouv� dans le classpath null est retourn�.
     * 
     * @param loader classLoader � utiliser
     * @param resource chemin COMPLET d'acc�s � la ressource (ex : fr/cls/ui/browser32.gif)
     * @return l'icon ou null si non trouv�
     */
    public static ImageIcon getAsImageIconOrNull(ClassLoader loader, String resource) {
        URL url = get(loader, resource);
        return (url == null) ? null : new ImageIcon(url);
    }

}
