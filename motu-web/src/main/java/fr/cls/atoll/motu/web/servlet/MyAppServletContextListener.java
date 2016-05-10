package fr.cls.atoll.motu.web.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.catalina.Container;
import org.apache.catalina.ServerFactory;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.deploy.FilterMap;

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
public class MyAppServletContextListener implements ServletContextListener {

    /** {@inheritDoc} */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        StandardEngine engine = (StandardEngine) ServerFactory.getServer().findService("Catalina").getContainer();
        Container container = engine.findChild(engine.getDefaultHost());
        StandardContext context = (StandardContext) container.findChild(sce.getServletContext().getContextPath());

        // TODO read from motu config the parameter to know if disableCasAuthentication
        if (true)
            disableCasAuthentication(context);
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

    /** {@inheritDoc} */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

}
