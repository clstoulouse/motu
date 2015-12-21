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
package fr.cls.atoll.motu.web.servlet.listener;

import fr.cls.atoll.motu.library.misc.utils.ConfigLoader;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.parsers.FactoryConfigurationError;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Bootstrap listener for custom Log4J initialization in a web environment.
 * <p>
 * This listener has to be registered in <code>web.xml</code> with the following property:
 * <p>
 * <i>log4jConfigLocation</i>: Location of the log4j config file.
 * 
 * (C) Copyright 2009-2010, by CLS (Collecte Localisation Satellites)
 * 
 * @version $Revision: 1.1 $ - $Date: 2009-03-18 12:18:22 $
 * @author <a href="mailto:ccamel@cls.fr">Christophe Camel</a>
 * @author <a href="mailto:jcarbou@cls.fr">Jerome Carbou</a>
 */
public class Log4JConfigListener implements ServletContextListener {
    /** Parameter specifying the location of the log4j config file */
    public static final String CONFIG_LOCATION_PARAM = "log4jConfigLocation";

    /** Default log4j file name. */
    public static final String DEFAULT_CONFIG_FILE_NAME = "Log4J.xml";

    /**
     * Load the configuration file and initialize the log4j platform.
     * 
     * @param filename name of the configuration file.
     */
    public void loadConfiguration(String filename) {
    }

    /**
     * Shut down log4j, properly releasing all file locks and resetting the web app root system property.
     * 
     * @param servletContext the current ServletContext
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().log("Shutting down log4j");
        LogManager.shutdown();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        final String paramValue = sce.getServletContext().getInitParameter(CONFIG_LOCATION_PARAM);

        final String configFilename = (paramValue == null ? DEFAULT_CONFIG_FILE_NAME : paramValue);

        URL url = null;
        try {
            if (configFilename.indexOf("file:") == 0) {
                url = new URL(configFilename);
            } else {
                url = ConfigLoader.getInstance().get(configFilename);
            }
            if (url == null) {
                url = new URL(configFilename);
            }
            if (url != null) {
                DOMConfigurator.configure(url);
            } else {
                System.err.println(configFilename + " configuration failure.");
                return;
            }
            Log log = LogFactory.getLog(Log4JConfigListener.class);
            log.info(configFilename + " configuration success.");
        } catch (FactoryConfigurationError ex) {
            System.err.println(configFilename + " configuration failure.");
        } catch (IOException e) {
            System.err.println("File \"" + configFilename + "\" not found in file system.");
        }

    }
}
