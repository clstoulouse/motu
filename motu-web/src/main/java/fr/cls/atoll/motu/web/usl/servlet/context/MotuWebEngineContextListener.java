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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.catalina.Container;
import org.apache.catalina.ServerFactory;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.deploy.FilterMap;
import org.apache.log4j.LogManager;

import fr.cls.atoll.motu.web.bll.BLLManager;
import fr.cls.atoll.motu.web.usl.USLManager;

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
public class MotuWebEngineContextListener implements ServletContextListener {
    /** Parameter specifying the location of the log4j config file */
    public static final String CONFIG_LOCATION_PARAM = "log4jConfigLocation";

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
        USLManager.getInstance().init();

        // Init Cas filters
        initCasServer(sce);
    }

    /**
     * .
     * 
     * @param sce
     */
    private void initCasServer(ServletContextEvent sce) {
        StandardEngine engine = (StandardEngine) ServerFactory.getServer().findService("Catalina").getContainer();
        Container container = engine.findChild(engine.getDefaultHost());
        StandardContext context = (StandardContext) container.findChild(sce.getServletContext().getContextPath());

        // TODO read from motu config
        if (BLLManager.getInstance().getConfigManager().getCasServerUrl() == null) {
            disableCasAuthentication(context);
        }
    }

    private void disableCasAuthentication(StandardContext context) {
        FilterMap proxyCallbackCasValidationFilter = new FilterMap();
        proxyCallbackCasValidationFilter.setFilterName("CAS Validation Filter");
        proxyCallbackCasValidationFilter.addURLPattern("/proxyCallback");

        FilterMap casAuthFilter = new FilterMap();
        casAuthFilter.setFilterName("CAS Authentication Filter");
        casAuthFilter.addURLPattern("/*");

        FilterMap casValidationFilter = new FilterMap();
        casValidationFilter.setFilterName("CAS Validation Filter");
        casValidationFilter.addURLPattern("/*");

        FilterMap casHttpServletRequestWrapperFilter = new FilterMap();
        casHttpServletRequestWrapperFilter.setFilterName("CAS HttpServletRequest Wrapper Filter");
        casHttpServletRequestWrapperFilter.addURLPattern("/*");

        FilterMap casAssertionThreadLocalFilter = new FilterMap();
        casAssertionThreadLocalFilter.setFilterName("CAS Assertion Thread Local Filter");
        casAssertionThreadLocalFilter.addURLPattern("/*");

        FilterMap casAuthorizationFilter = new FilterMap();
        casAuthorizationFilter.setFilterName("CAS Authorization Filter");
        casAuthorizationFilter.addURLPattern("/*");

        context.removeFilterMap(proxyCallbackCasValidationFilter);
        context.removeFilterMap(casAuthFilter);
        context.removeFilterMap(casValidationFilter);
        context.removeFilterMap(casHttpServletRequestWrapperFilter);
        context.removeFilterMap(casAssertionThreadLocalFilter);
        context.removeFilterMap(casAuthorizationFilter);

    }
}
