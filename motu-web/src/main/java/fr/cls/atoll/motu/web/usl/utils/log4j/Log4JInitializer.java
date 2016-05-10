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
package fr.cls.atoll.motu.web.usl.utils.log4j;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.xml.DOMConfigurator;

import fr.cls.atoll.motu.library.misc.utils.ConfigLoader;

/**
 * Used to initialize log4j
 */
public class Log4JInitializer {
    /** Default log4j file name. */
    public static final String DEFAULT_CONFIG_FILE_NAME = "Log4J.xml";

    public static void init(String log4jConfigLocation) {
        final String configFilename = (log4jConfigLocation == null ? DEFAULT_CONFIG_FILE_NAME : log4jConfigLocation);

        URL url = null;
        try {
            if (configFilename.indexOf("file:") == 0) {
                url = new URL(configFilename);
            } else {
                url = ConfigLoader.getInstance().get(configFilename);

                if (url == null) {
                    url = new URL(configFilename);
                } else {
                    System.err.println(configFilename + " configuration failure.");
                    return;
                }
            }

            if (url != null) {
                DOMConfigurator.configure(url);
                Log log = LogFactory.getLog(Log4JInitializer.class);
                log.info(configFilename + " configuration success.");
            }
        } catch (FactoryConfigurationError ex) {
            System.err.println(configFilename + " configuration failure.");
        } catch (IOException e) {
            System.err.println("File \"" + configFilename + "\" not found in file system.");
        }

    }
}
