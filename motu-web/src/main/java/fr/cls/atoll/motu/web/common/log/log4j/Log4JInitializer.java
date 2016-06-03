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
package fr.cls.atoll.motu.web.common.log.log4j;

/**
 * Used to initialize log4j
 */
public class Log4JInitializer {
    /** Default log4j file name. */
    public static final String DEFAULT_CONFIG_FILE_NAME = "log4j.xml";

    public static void init(String log4jConfigLocation) {
        // final String configFilename = (log4jConfigLocation == null ? DEFAULT_CONFIG_FILE_NAME :
        // log4jConfigLocation);
        //
        // URL url = null;
        // try {
        // if (configFilename.indexOf("file:") == 0) {
        // url = new URL(configFilename);
        // } else {
        // url = Log4JInitializer.class.getClassLoader().getResource(configFilename);
        // }
        //
        // if (url != null) {
        // ((org.apache.logging.log4j.core.LoggerContext)
        // LogManager.getContext()).setConfigLocation(url.toURI());
        // Logger log = LogManager.getLogger();// LogFactory.getLog(Log4JInitializer.class);
        // log.info("Log4j initiazed successfully from file: \"" + url.toString() + "\".");
        // }
        // } catch (Exception e) {
        // System.err.println("Exception while initializing Log4j from file: \"" + configFilename + "\".");
        // e.printStackTrace();
        // }

    }
}
