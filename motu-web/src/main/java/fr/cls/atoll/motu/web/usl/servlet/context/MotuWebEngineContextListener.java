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

import java.io.FileInputStream;
import java.io.IOException;
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
import org.apache.catalina.core.StandardService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.tomcat.util.descriptor.web.FilterMap;

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

    static {
        initLog4j();
    }

    private static void initLog4j() {
        String log4jConfigFolderPath = System.getProperty("motu-config-dir");
        if (log4jConfigFolderPath != null && log4jConfigFolderPath.length() > 0) {
            if (!log4jConfigFolderPath.endsWith("/")) {
                log4jConfigFolderPath += "/";
            }
        } else {
            System.err.println("Error while initializing log4j. Property is not set motu-config-dir or has a bad value");
        }
        // Do not use system property to avoid conflicts with other tomcat webapps
        // System.setProperty("log4j.configurationFile", log4jConfigFolderPath + "log4j.xml");

        try {
            ConfigurationSource source = new ConfigurationSource(new FileInputStream(log4jConfigFolderPath + "log4j.xml"));
            Configurator.initialize(null, source);
        } catch (IOException e) {
            System.err.println("Error while initializing log4j from file: " + log4jConfigFolderPath + "log4j.xml");
            e.printStackTrace();
        }
    }

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Shut down log4j, properly releasing all file locks and resetting the web app root system property.
     * 
     * @param servletContext the current ServletContext
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            try {
                LOGGER.info("Stop in progress...");
                waitForPendingOrInprogressRequests();
            } finally {
                LOGGER.info("Stop BLLManager");
                BLLManager.getInstance().stop();
                LOGGER.info("Stop DALManager");
                DALManager.getInstance().stop();
            }
        } catch (Exception e) {
            LOGGER.error("An error occured while waiting to stop motu", e);
        }
        LOGGER.info("Stop done");
    }

    private void waitForPendingOrInprogressRequests() {
        // Check if the timeout is reached
        boolean timeOut = false;
        // Store the wait status
        boolean hasWait = false;
        // Store the time of the wait begin to check the timeout
        long startWaiting = System.currentTimeMillis();
        try {
            long[] pendingAndInProgressRequestNbr = getPendingAndInProgressRequestNumber();
            long pendingRequestNbr = pendingAndInProgressRequestNbr[0];
            long inProgressRequestNbr = pendingAndInProgressRequestNbr[1];
            int totalWaitTimeInSec = 0;
            LOGGER.info("Stop: Pending=" + pendingRequestNbr + "; InProgress=" + inProgressRequestNbr);

            while ((pendingRequestNbr > 0 || inProgressRequestNbr > 0) && !timeOut) {
                // Log each 5sec
                if (totalWaitTimeInSec % 5 == 0) {
                    LOGGER.info("Stop: Waiting requests: Pending=" + pendingRequestNbr + "; InProgress=" + inProgressRequestNbr);
                }

                // Sleep 1 second before another check
                Thread.sleep(1000);
                totalWaitTimeInSec++;

                // Check if the 10 minutes timeout is reached
                timeOut = (System.currentTimeMillis() - startWaiting) > 10 * 60 * 1000;

                // Store that the finish action have to wait.
                hasWait = true;

                pendingAndInProgressRequestNbr = getPendingAndInProgressRequestNumber();
                pendingRequestNbr = pendingAndInProgressRequestNbr[0];
                inProgressRequestNbr = pendingAndInProgressRequestNbr[1];
            }
            LOGGER.info("Stop: Waiting requests: Pending=" + pendingRequestNbr + "; InProgress=" + inProgressRequestNbr);

            // If the shutdown have wait, wait 1sec to finish correctly the request.
            if (hasWait) {
                Thread.sleep(1000);
            }

        } catch (InterruptedException e) {
            LOGGER.error("An error occured while waiting to stop motu", e);
        }
    }

    private long[] getPendingAndInProgressRequestNumber() {
        return DALManager.getInstance().getRequestManager().getDalRequestStatusManager().getPendingAndInProgressDownloadRequestNumber();
    }

    private void initCommonTools() {
        setDefaultTimeZoneToGMT();
    }

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        initCommonTools();

        try {
            LOGGER.info("Start DALManager");
            // Init DAL and also LOG4J
            DALManager.getInstance().init();
        } catch (MotuException e) {
            LOGGER.error("Error while initializing DAL:Data Access Layer {}", e.getMessage(), e);
        }

        try {
            LOGGER.info("Start BLLManager");
            BLLManager.getInstance().init();
        } catch (MotuException e) {
            LOGGER.error("Error while initializing BLL:Business Logic Layer {}", e.getMessage(), e);
        }

        try {
            LOGGER.info("Start USLManager");
            USLManager.getInstance().init();
        } catch (MotuException e) {
            LOGGER.error("Error while initializing USL:User Service Layer {}", e.getMessage(), e);
        }

        // Init Cas filters
        initCasServer(sce);
    }

    /**
     * .
     */
    private void setDefaultTimeZoneToGMT() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
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
                    if (!BLLManager.getInstance().getConfigManager().isCasActivated()) {
                        removeAllCasFilters(sce);
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

    private void removeAllCasFilters(ServletContextEvent sce)
            throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException, MalformedObjectNameException {
        MBeanServer mBeanServer = MBeanServerFactory.findMBeanServer(null).get(0);
        ObjectName name = new ObjectName("Catalina", "type", "Server");
        Server server = (Server) mBeanServer.getAttribute(name, "managedResource");
        StandardService catalinaService = (StandardService) server.findService("Catalina");
        StandardEngine engine = (StandardEngine) catalinaService.getContainer();
        Container container = engine.findChild(engine.getDefaultHost());
        StandardContext context = (StandardContext) container.findChild(sce.getServletContext().getContextPath());
        for (FilterMap fm : context.findFilterMaps()) {
            if (fm.getFilterName().startsWith("CAS")) {
                context.removeFilterMap(fm);
            }
        }
    }

}
