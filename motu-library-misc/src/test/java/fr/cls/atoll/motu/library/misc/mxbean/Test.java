package fr.cls.atoll.motu.library.misc.mxbean;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.MessageFormat;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import fr.cls.atoll.motu.library.misc.queueserver.RequestManagement;

public class Test {
    
    public static void main(String[] args) {
        Long l = 1301495560484L;
        final String jmxUrlString = "service:jmx:rmi:///jndi/rmi://atoll-dev.cls.fr:7200/jmxrmi";

        System.out.println(l.toString());
        try {
            final JMXServiceURL jmxUrl = new JMXServiceURL(jmxUrlString);
            final JMXConnector connector = JMXConnectorFactory.connect(jmxUrl);
            final MBeanServerConnection mbsc = connector.getMBeanServerConnection();
            //MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName =  new ObjectName(MessageFormat.format(RequestManagement.OBJECT_NAME_PATTERN, "EndedRequests", l.toString()));
            
            Object memoryUsage = mbsc.getAttribute(objectName, "Usage");
            System.out.println(memoryUsage.getClass());

        } catch (MalformedObjectNameException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (AttributeNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstanceNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MBeanException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ReflectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
