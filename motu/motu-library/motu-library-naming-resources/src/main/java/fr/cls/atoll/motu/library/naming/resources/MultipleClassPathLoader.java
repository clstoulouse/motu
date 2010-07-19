package fr.cls.atoll.motu.library.naming.resources;

import java.io.File;
import java.net.URI;
import java.util.StringTokenizer;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.loader.WebappLoader;

public class MultipleClassPathLoader extends WebappLoader {
    private static org.apache.juli.logging.Log LOGGER = org.apache.juli.logging.LogFactory.getLog(MultipleClassPathLoader.class);

    /**
     * <code>;</code> separated list of additional path elements.
     */
    private String virtualClasspath;

    /**
     * Construct a new WebappLoader with no defined parent class loader (so that the actual parent will be the
     * system class loader).
     */
    public MultipleClassPathLoader() {
        super();
    }

    /**
     * Construct a new WebappLoader with the specified class loader to be defined as the parent of the
     * ClassLoader we ultimately create.
     * 
     * @param parent The parent class loader
     */
    public MultipleClassPathLoader(ClassLoader parent) {
        super(parent);
    }

    /**
     * <code>virtualClasspath</code> attribute that will be automatically set from the <code>Context</code>
     * <code>virtualClasspath</code> attribute from the context xml file.
     * 
     * @param path <code>;</code> separated list of path elements.
     */
    public void setVirtualClasspath(String path) {
        virtualClasspath = path;
    }

    @Override
    public void start() throws LifecycleException {
        // just add any jar/directory set in virtual classpath to the
        // repositories list before calling start on the standard WebappLoader
        StringTokenizer tkn = new StringTokenizer(virtualClasspath, ";");
        while (tkn.hasMoreTokens()) {
            File file = new File(tkn.nextToken());
            if (!file.exists()) {
                LOGGER.warn("File " + file.getPath() + " doesn't exist.");
                continue;
            }
            if (!file.isDirectory()) {
                LOGGER.warn("File " + file.getPath() + " is not a directory.");
                continue;
            }

            URI uriFile = new File(file, "/").toURI();
            LOGGER.info("Adding classpath " + file + " to context " + getContainer());
            this.addRepository(uriFile.toString());
        }

        // We set a custom class for class loading. This is actually a simple class derivating from
        // WebAppClassLoader with no more but for a future usage.
        this.setLoaderClass(CustomWebAppClassLoader.class.getCanonicalName());

        super.start();
    }
}
