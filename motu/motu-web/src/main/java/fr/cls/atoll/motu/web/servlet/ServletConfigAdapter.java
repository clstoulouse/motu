package fr.cls.atoll.motu.web.servlet;

import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class ServletConfigAdapter implements ServletConfig {

    private final ServletConfig sc;

    /**
     * @return the sc
     */
    public ServletConfig getSc() {
        return sc;
    }

    /**
     * @param sc
     */
    public ServletConfigAdapter(ServletConfig sc) {
        this.sc = sc;
    }

    @Override
    public String getInitParameter(String name) {
        return sc.getInitParameter(name);
    }

    @Override
    public Enumeration getInitParameterNames() {
        return sc.getInitParameterNames();
    }

    @Override
    public ServletContext getServletContext() {
        return sc.getServletContext();
    }

    @Override
    public String getServletName() {
        return sc.getServletName();
    }

}
