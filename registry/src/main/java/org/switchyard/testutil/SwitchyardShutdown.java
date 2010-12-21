package org.switchyard.testutil;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * Utility class that issues a stop command to shut down a SwitchyardController
 * MBean.
 *
 * @author tcunning
 */
public class SwitchyardShutdown {
    public static void main (String args[]) {
        try {
            System.out.println("Connect to JMX service.");
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://:9998/jmxrmi");
            JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
            MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

            // Construct proxy for the the MBean object
            ObjectName mbeanName = new ObjectName("org.switchyard:type=SwitchyardController");
            SwitchyardControllerMBean mbeanProxy = JMX.newMBeanProxy(mbsc, mbeanName, SwitchyardControllerMBean.class, true);

            System.out.println("Connected to: "+mbeanProxy.getName()+", the app is "+(mbeanProxy.isRunning() ? "" : "not ")+"running");

            System.out.println("Invoke \"stop\" method");

            // Right now SwitchyardController is issuing a System.exit(0), so
            // we need to catch exceptions issued
            try {
                mbeanProxy.stop();
                jmxc.close();
            } catch (Exception e) {
            }
            // clean up and exit
            System.out.println("Done.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
