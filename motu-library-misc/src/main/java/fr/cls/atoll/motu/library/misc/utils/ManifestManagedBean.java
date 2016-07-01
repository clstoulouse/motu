package fr.cls.atoll.motu.library.misc.utils;

import org.apache.log4j.Logger;

import javax.management.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.management.AttributeNotFoundException;

/**
 * Publish through the JMX protocol the MANIFEST.MF properties allowing inspection of jar dependencies of the application.
 */
public class ManifestManagedBean implements DynamicMBean {
    // Name pattern under which managed beans get registered
    private final static String OBJECT_NAME_PATTERN = "fr.cls:artefact=Motu,domain=Dependencies,element={0}";

    /**
     * Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(ManifestManagedBean.class);

    /**
     * counter instance of unclassified resources. An unclassified resource is a classpath resource laying in a directory instead
     * of a jar file.
     */
    private static int i = 0;

    /**
     * url of the manifest file in the classpath
     */
    private final URL url;

    /**
     * @return the manifest content as properties. The instance is dynamically created.
     * @throws java.io.IOException if manifest file not found or not reachable
     */
    public Properties getProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(url.openStream());
        return properties;
    }


    /**
     * Unique constructor.
     *
     * @param url the url of the manifest file within the classpath
     */
    public ManifestManagedBean(URL url) {
        this.url = url;
    }

    /**
     * Dynamic method that allows to retrieve an attribute in the manifest file.
     *
     * @param name name of the attribute to retrieve
     * @return the value of the attribute
     * @throws AttributeNotFoundException if the attribute is not found in the manifest
     * @throws MBeanException             if something else went gone (manifest file not found for example)
     */
    public String getAttribute(String name)
            throws AttributeNotFoundException, MBeanException {
        String value;
        try {
            value = getProperties().getProperty(name);
        } catch (IOException e) {
            throw new MBeanException(e);
        }
        if (value != null)
            return value;
        else
            throw new AttributeNotFoundException("No such property: " + name);
    }

    /**
     * Dynamic method to change an attribute value. The method always throw an AttributeNotFoundException since
     * manifest values are read-only.
     *
     * @param attribute the attribute to set
     * @throws AttributeNotFoundException for each invocation.
     */
    public void setAttribute(Attribute attribute)
            throws InvalidAttributeValueException, MBeanException, AttributeNotFoundException {
        throw new AttributeNotFoundException("No such property: " + attribute.getName());
    }

    /**
     * Dynamic method that allows to retrieve a set of attributes in the manifest file.
     *
     * @param names names of the attributes to retrieve
     * @return the values of the attributes
     * @throws MBeanException if something else went gone (manifest file not found for example)
     */
    public AttributeList getAttributes(String[] names) {
        AttributeList list = new AttributeList();
        for (String name : names) {
            String value = null;
            try {
                value = getProperties().getProperty(name);
            } catch (IOException e) {
                try {
                    throw new MBeanException(e);
                } catch (MBeanException e1) {
                }
            }
            if (value != null)
                list.add(new Attribute(name, value));
        }
        return list;
    }

    /**
     * Dynamic method to change a set of attribute values. The method always return an empty collection of attributes since
     * manifest properties can't be changed.
     *
     * @param list the attributes to set
     * @return an empty collection of attributes
     */
    public AttributeList setAttributes(AttributeList list) {
        AttributeList retlist = new AttributeList();
        return retlist;
    }

    /**
     * Allows an action to be invoked on the instance.
     * <p/>
     * Since no methods are available for invocation, the method always throws a ReflectionException.
     *
     * @param name The name of the action to be invoked.
     * @param args An array containing the parameters to be set when the action is
     *             invoked.
     * @param sig  An array containing the signature of the action. The class objects will
     *             be loaded through the same class loader as the one used for loading the
     *             MBean on which the action is invoked.
     * @return The object returned by the action, which represents the result of
     *         invoking the action on the MBean specified.
     * @throws ReflectionException always.
     */
    public Object invoke(String name, Object[] args, String[] sig)
            throws MBeanException, ReflectionException {
        throw new ReflectionException(new NoSuchMethodException(name));
    }

    /**
     * Provides the exposed attributes and actions of the instance.
     *
     * @return An instance of <CODE>MBeanInfo</CODE> allowing all attributes and actions
     *         exposed by this Dynamic MBean to be retrieved.
     */
    public MBeanInfo getMBeanInfo() {
        SortedSet<String> names = new TreeSet<String>();
        try {
            Properties properties = getProperties();
            for (Object name : properties.keySet())
                names.add((String) name);
        } catch (IOException e) {

        }
        MBeanAttributeInfo[] attrs = new MBeanAttributeInfo[names.size()];
        Iterator<String> it = names.iterator();
        for (int i = 0; i < attrs.length; i++) {
            String name = it.next();
            attrs[i] = new MBeanAttributeInfo(
                    name,
                    "java.lang.String",
                    "Property " + name,
                    true,   // isReadable
                    false,   // isWritable
                    false); // isIs
        }
        MBeanOperationInfo[] opers = {};
        return new MBeanInfo(
                this.getClass().getName(),
                "Property Manager MBean",
                attrs,
                null,  // constructors
                opers,
                null); // notifications
    }

    private ObjectName getName() throws MalformedObjectNameException {
        Pattern pattern = Pattern.compile(".*/(.*)\\.jar!/META-INF/MANIFEST.MF");
        Matcher matcher = pattern.matcher(url.toString());
        if (matcher.find()) {
            return new ObjectName(MessageFormat.format(OBJECT_NAME_PATTERN, matcher.group(1)));
        } else {
            return new ObjectName(MessageFormat.format(OBJECT_NAME_PATTERN, "unclassified-" + i++));
        }

    }

    /**
     * Scans through the classpath all the MANIFEST.MF files and registers an instance of  ManifestManagedBean for
     * each one.
     */
    public static void register() {
        try {
            // works well in tomcat
            final MBeanServer platform = ManagementFactory.getPlatformMBeanServer();

            Enumeration<URL> resources = ManifestManagedBean.class.getClassLoader().getResources("META-INF/MANIFEST.MF");

            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                try {
                    final ManifestManagedBean pm = new ManifestManagedBean(url);
                    platform.registerMBean(pm, pm.getName());
                } catch (Exception e) {
                    // don't let the exception propagate
                    LOG.warn("Failed to register managed bean from manifest location: " + url, e);
                }
            }
        } catch (Exception e) {
            // don't let the exception propagate
            LOG.warn("Failed to find MANIFEST within classpath", e);
        }
    }


}
