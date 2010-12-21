package org.switchyard.testutil;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.namespace.QName;

import org.switchyard.BaseHandler;
import org.switchyard.Exchange;
import org.switchyard.ExchangeHandler;
import org.switchyard.ServiceDomain;
import org.switchyard.internal.DistributedEndpointProvider;
import org.switchyard.internal.JGroupsRegistry;
import org.switchyard.internal.ServiceDomains;
import org.switchyard.internal.ServiceRegistration;

/**
 * A loop-based standing Switchyard registry with JMX controls so that it
 * can be killed from another JVM.     This is meant as a very rudimentary
 * way to test a multi-node registry until Switchyard gets some sort of
 * lifecycle and we can use cargo to launch a container hosting Switchyard.
 *
 * @author tcunning
 */
public class SwitchyardController implements SwitchyardControllerMBean {
    private static final int DEFAULT_DELAY = 5000;
    ServiceDomain domain = null;
    private final Thread _thread;

    public SwitchyardController(Thread thread) {
        _thread = thread;
    }

    private void setUp() {
        System.setProperty(ServiceDomains.REGISTRY_CLASS_NAME,
                JGroupsRegistry.class.getName());
        System.setProperty(ServiceDomains.ENDPOINT_PROVIDER_CLASS_NAME,
                DistributedEndpointProvider.class.getName());
        domain = ServiceDomains.getDomain();

        register("foo");
        register("bar");
    }

    private void register(String service) {
        final QName serviceName = new QName(service);
        // Provide the service
        ExchangeHandler provider = new BaseHandler() {
            @Override
            public void handleMessage(Exchange event) {
                System.out.println("MESSAGE: "
                        + event.getMessage().getContent().toString());
            }
        };
        ServiceRegistration sr =
            (ServiceRegistration) domain.registerService(serviceName, provider);
    }


    @Override
    public void boot(int delay) {
        int counter = 0;

        try {
            while(!Thread.interrupted()) {
                try {
                    Thread.sleep(delay);
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    @Override
    public void stop() {
        _thread.interrupt();
        System.exit(0);
    }

    @Override
    public String getName() {
        return "Switchyard JMX Control";
    }

    @Override
    public void start() {
    }

    @Override
    public boolean isRunning() {
        return Thread.currentThread().isAlive();
    }

    public static void main(String args[]) throws Exception {

        System.out.println("SwitchyardController");

        try {
            System.out.println("JMX started");

            SwitchyardControllerMBean controller = new SwitchyardController(Thread.currentThread());
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = new ObjectName("org.switchyard:type=SwitchyardController");
            server.registerMBean(controller, name);
            controller.boot(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
