package fr.cls.atoll.motu.library.tomcat.loader;

import java.net.URL;

import org.apache.catalina.loader.WebappClassLoader;

/**
 * Special class loader.
 * <p>
 * Actually does nothing more than ancestor. Reserved for future usages.
 * 
 * @author ccamel
 */
public class CustomWebAppClassLoader extends WebappClassLoader {

    /**
     * Constructor.
     */
    public CustomWebAppClassLoader() {
        super();
    }

    /**
     * Constructor.
     */
    public CustomWebAppClassLoader(ClassLoader parent) {
        super(parent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.catalina.loader.WebappClassLoader#findResource(java.lang.String)
     */
    @Override
    public URL findResource(String name) {
        return super.findResource(name);
    }

}
