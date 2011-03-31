package fr.cls.atoll.motu.library.misc.utils;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.management.Descriptor;
import javax.management.IntrospectionException;
import javax.management.JMException;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.modelmbean.RequiredModelMBean;

import org.apache.log4j.Logger;

import fr.cls.atoll.motu.api.message.xml.StatusModeResponse;

public class MBeanUtils {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(MBeanUtils.class);

    // http://marxsoftware.blogspot.com/2008/07/jmx-model-mbean.html

    public static  ModelMBean createRawModelMBean(StatusModeResponse statusModeResponse) {
        RequiredModelMBean modelmbean = null;
        try {
            final ModelMBeanInfoSupport modelMBeanInfo = new ModelMBeanInfoSupport(StatusModeResponse.class.getName(), "Request status.", null, // attributes
                    null, // constructors
                    buildModelMBeanOperationInfo(),
                    null, // notifications
                    buildDescriptor());
            modelmbean = new RequiredModelMBean(modelMBeanInfo);
            setModelMBeanManagedResource(modelmbean, statusModeResponse);

        } catch (Exception e) {
            // JMX supervision should never alters Motu behaviour, so we don't let exception propagation
            LOG.error("Failed to create Raw Model MBean (Motu will still continue to start)", e);
        }

        return modelmbean;
    }

    /**
     * Construct the meta information for the SimpleCalculator ModelMBean operations and the operations'
     * parameters.
     * 
     * @return
     */
    public static ModelMBeanOperationInfo[] buildModelMBeanOperationInfo() {
        //  
        // Build the PARAMETERS and OPERATIONS meta information for "add".
        //  

        final MBeanParameterInfo msgParameterInfo = new MBeanParameterInfo("msg", String.class.getSimpleName(), "Response message");

        final ModelMBeanOperationInfo getMsgOperationInfo = new ModelMBeanOperationInfo("getMsg", "get the response msg",
        // new MBeanParameterInfo[] { msgParameterInfo },
                null,
                String.class.getSimpleName(),
                ModelMBeanOperationInfo.INFO);

        final MBeanParameterInfo codeParameter = new MBeanParameterInfo("code", Integer.TYPE.toString(), "Response code");

        final ModelMBeanOperationInfo getCodeOperationInfo = new ModelMBeanOperationInfo("getCode", "get the response code",
        // new MBeanParameterInfo[] { codeParameter },
                new MBeanParameterInfo[] {},
                Integer.TYPE.toString(),
                ModelMBeanOperationInfo.INFO);

        return new ModelMBeanOperationInfo[] { getMsgOperationInfo, getCodeOperationInfo };
    }

    /**
     * Build descriptor.
     * 
     * @return Generated descriptor.
     */
    public static Descriptor buildDescriptor() {
        final Descriptor descriptor = new DescriptorSupport();
        descriptor.setField("name", "ModelMBeanInTheRaw");
        descriptor.setField("descriptorType", "mbean");
        return descriptor;
    }

    /**
     * Set the provided ModelMBean to manage the SimpleCalculator resource.
     * 
     * @param modelMBeanToManageResource ModelMBean to manage the SimpleCalculator resource.
     */
    public static void setModelMBeanManagedResource(final ModelMBean modelMBeanToManageResource, StatusModeResponse statusModeResponse) {
        try {
            modelMBeanToManageResource.setManagedResource(statusModeResponse, "ObjectReference");
        } catch (Exception e) {
            // JMX supervision should never alters Motu behaviour, so we don't let exception propagation
            LOG.error("Failed to set Model MBean Managed Resource (Motu will still continue to start)", e);
        }
    }

    /**
     * Register the provided ModelMBean in the MBeanServer using the provided ObjectName String.
     * 
     * @param modelMBean ModelMBean to be registered with the MBeanServer.
     * @param objectNameString ObjectName of the registered ModelMBean.
     */
    public static void registerModelMBean(final ModelMBean modelMBean, final String objectNameString) {
        try {
            // works well in tomcat
            final MBeanServer platform = ManagementFactory.getPlatformMBeanServer();
            // ObjectName name = new ObjectName(MessageFormat.format(OBJECT_NAME_PATTERN, "EndedRequests",
            // requestId));
            final ObjectName objectName = new ObjectName(objectNameString);
            platform.registerMBean(modelMBean, objectName);
        } catch (Exception e) {
            // JMX supervision should never alters Motu behaviour, so we don't let exception propagation
            LOG.error("Failed to register Model MBean (Motu will still continue to start)", e);
        }
    }

    // END http://marxsoftware.blogspot.com/2008/07/jmx-model-mbean.html

    // http://blogs.sun.com/jmxetc/entry/dynamicmbeans,_modelmbeans,_and_pojos...

    public static  ModelMBean makeModelMBean(Object resource) throws JMException, InvalidTargetObjectTypeException {

        final Method[] methods = resource.getClass().getMethods();

        final List<Method> operations = new ArrayList<Method>();
        final List<Method> getters = new ArrayList<Method>();
        final Map<String, Method> setters = new LinkedHashMap<String, Method>();

        for (Method method : methods) {
            // don't want to expose getClass(), hashCode(), equals(), etc...
            if (method.getDeclaringClass().equals(Object.class))
                continue;

            if (method.getName().startsWith("get") && !method.getName().equals("get") && !method.getName().equals("getClass")
                    && method.getParameterTypes().length == 0 && method.getReturnType() != void.class) {
                getters.add(method);
            }
            if (method.getName().startsWith("set") && !method.getName().equals("set") && method.getParameterTypes().length == 1
                    && method.getReturnType().equals(void.class)) {
                setters.put(method.getName(), method);
            }

            operations.add(method);
        }

        final List<ModelMBeanAttributeInfo> attrinfo = new ArrayList<ModelMBeanAttributeInfo>();

        for (Method getter : getters) {

            final String attrName = getter.getName().substring(3);
            final String setterMethod = "set" + attrName; // construct setter method

            // Check whether there's a setter and if so removes it from the
            // setter's map.
            //
            Method setter = setters.remove(setterMethod);

            if (setter != null) {
                // If there's a setter, it must have the same "type" than
                // the getter
                if (!getter.getReturnType().equals(setter.getParameterTypes()[0])) {
                    System.err.println("Warning: setter " + setter.getName() + " doesn't have the expected type: setter ignored.");
                    setter = null;
                }
            }

            attrinfo.add(makeAttribute(getter, setter));
        }

        // check if there are setters for which there was no getter
        //
        for (Method setter : setters.values()) {
            // It would be unusual to have a setter with no getter!
            System.err.println("Warning: setter " + setter.getName() + " has no corresponding getter!");
            attrinfo.add(makeAttribute(null, setter));
        }

        final ModelMBeanAttributeInfo[] attrs = attrinfo.toArray(new ModelMBeanAttributeInfo[attrinfo.size()]);

        final int opcount = operations.size();
        final ModelMBeanOperationInfo[] ops = new ModelMBeanOperationInfo[opcount];
        for (int i = 0; i < opcount; i++) {
            final Method m = operations.get(i);
            ops[i] = new ModelMBeanOperationInfo(m.getName(), m);
        }

        ModelMBeanInfo mmbi = new ModelMBeanInfoSupport(resource.getClass().getName(), resource.getClass().getName(), attrs, null, // constructors
                ops,
                null); // notifications
        ModelMBean mmb = new RequiredModelMBean(mmbi);
        mmb.setManagedResource(resource, "ObjectReference");
        return mmb;
    }

    public static  ModelMBeanAttributeInfo makeAttribute(Method getter, Method setter) throws IntrospectionException {
        final String attrName;
        if (getter != null)
            attrName = getter.getName().substring(3);
        else
            attrName = setter.getName().substring(3);

        final List<String> descriptors = new ArrayList<String>();
        descriptors.add("name=" + attrName);
        descriptors.add("descriptorType=attribute");
        if (getter != null) {
            descriptors.add("getMethod=" + getter.getName());
        }
        if (setter != null) {
            descriptors.add("setMethod=" + setter.getName());
        }

        final Descriptor attrD = new DescriptorSupport(descriptors.toArray(new String[descriptors.size()]));

        return new ModelMBeanAttributeInfo(attrName, attrName, getter, setter, attrD);
    }

    // END http://blogs.sun.com/jmxetc/entry/dynamicmbeans,_modelmbeans,_and_pojos...

    /**
     * Registers to the MBean platform the managed beans.
     *
     * @param object the object
     * @param objectNameString the object name string
     */
    public static void registerMBean(final Object object, final String objectNameString) {
        try {
            // works well in tomcat
            final MBeanServer platform = ManagementFactory.getPlatformMBeanServer();
            // ObjectName name = new ObjectName(MessageFormat.format(OBJECT_NAME_PATTERN, "EndedRequests",
            // requestId));
            final ObjectName objectName = new ObjectName(objectNameString);
            platform.registerMBean(object, objectName);
        } catch (Exception e) {
            // JMX supervision should never alters Motu behaviour, so we don't let exception propagation
            LOG.error("Failed to register Model MBean (Motu will still continue to start)", e);
        }
    }

}
