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
package fr.cls.atoll.motu.web.usl.servlet.context;

import java.util.TimeZone;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.catalina.Container;
import org.apache.catalina.Server;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.deploy.FilterMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import fr.cls.atoll.motu.web.dal.DALManager;
import fr.cls.atoll.motu.web.usl.USLManager;

/**
 * <br>
 * <br>
 * Copyright : Copyright (c) 2016 <br>
 * <br>
 * Société : CLS (Collecte Localisation Satellites)
 * 
 * @author Sylvain MARTY
 * @version $Revision: 1.1 $ - $Date: 2007-05-22 16:56:28 $
 */
public class MotuWebEngineContextListener implements ServletContextListener {
    /** Parameter specifying the location of the log4j config file */
    public static final String CONFIG_LOCATION_PARAM = "log4jConfigLocation";

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Shut down log4j, properly releasing all file locks and resetting the web app root system property.
     * 
     * @param servletContext the current ServletContext
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

    private void initCommonTools() {
        setDefaultTimeZoneToGMT();
    }

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        initCommonTools();

        try {
            // Init DAL and also LOG4J
            DALManager.getInstance().init();
        } catch (MotuException e) {
            LOGGER.error("Error while initializing DAL:Data Access Layer", e);
        }

        try {
            BLLManager.getInstance().init();
        } catch (MotuException e) {
            LOGGER.error("Error while initializing BLL:Business Logic Layer", e);
        }

        try {
            USLManager.getInstance().init();
        } catch (MotuException e) {
            LOGGER.error("Error while initializing USL:User Service Layer", e);
        }

        // Init Cas filters
        initCasServer(sce);
    }

    /**
     * .
     */
    private void setDefaultTimeZoneToGMT() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
    }

    /**
     * .
     * 
     * @param sce
     * @throws MalformedObjectNameException
     * @throws ReflectionException
     * @throws MBeanException
     * @throws InstanceNotFoundException
     * @throws AttributeNotFoundException
     */
    private void initCasServer(final ServletContextEvent sce) {
        // Run in a thread because (StandardEngine) server.findService("Catalina") is locked by
        // ServletContextListener
        Thread t = new Thread("Init CAS filters") {

            /** {@inheritDoc} */
            @Override
            public void run() {
                try {
                    String logCasServerInWebXML = "activated";
                    // TODO read from motu config
                    if (!BLLManager.getInstance().getConfigManager().isCasActivated()) {
                        removeAllCasFilters(getStandardContext(sce));
                        logCasServerInWebXML = "disabled";
                    }
                    LOGGER.info("CAS Server filters in web.xml: " + logCasServerInWebXML);
                } catch (Exception e) {
                    LOGGER.error("Init cas server", e);
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }

    private StandardContext getStandardContext(ServletContextEvent sce)
            throws MalformedObjectNameException, AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException {
        MBeanServer mBeanServer = MBeanServerFactory.findMBeanServer(null).get(0);
        ObjectName name = new ObjectName("Catalina", "type", "Server");
        Server server = (Server) mBeanServer.getAttribute(name, "managedResource");
        StandardEngine engine = (StandardEngine) server.findService("Catalina").getContainer();
        Container container = engine.findChild(engine.getDefaultHost());
        StandardContext context = (StandardContext) container.findChild(sce.getServletContext().getContextPath());
        return context;
    }

    private void removeAllCasFilters(StandardContext context) {
        for (FilterMap fm : context.findFilterMaps()) {
            if (fm.getFilterName().startsWith("CAS")) {
                context.removeFilterMap(fm);
            }
        }

    }
}
