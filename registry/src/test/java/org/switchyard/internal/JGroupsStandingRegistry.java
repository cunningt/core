package org.switchyard.internal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.switchyard.MockHandler;
import org.switchyard.ServiceDomain;

public class JGroupsStandingRegistry {
    ServiceDomain domain = null;
    HashMap<QName, List<ServiceRegistration>> registrations = new HashMap<QName, List<ServiceRegistration>>();
        
    @Before
    public void setUp() throws Exception {
        System.setProperty(ServiceDomains.REGISTRY_CLASS_NAME, 
            JGroupsRegistry.class.getName());
        System.setProperty(ServiceDomains.ENDPOINT_PROVIDER_CLASS_NAME, 
            DistributedEndpointProvider.class.getName()); 
        domain = ServiceDomains.getDomain();
    }
    
    @After
    public void tearDown() throws Exception {
        System.clearProperty(ServiceDomains.REGISTRY_CLASS_NAME);
        System.clearProperty(ServiceDomains.ENDPOINT_PROVIDER_CLASS_NAME);
    }
    
    @Test
    public void testLoop() {
        domain = ServiceDomains.createDomain(JGroupsStandingRegistry.class.getName());
        loop();    
    }
    
    private void loop() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            try {
                System.out.print("[(r)egister/(u)nregister <service>- "); 
                System.out.flush();
                
                String line = in.readLine();
                if (!line.isEmpty()) {
    
                    if ((line.startsWith("r"))) {
                        String[] array = line.split(" ");

                        final QName serviceName = new QName(array[1]);
                        // Provide the service
                        MockHandler provider = new MockHandler().forwardInToFault();
                        ServiceRegistration sr = 
                            (ServiceRegistration) domain.registerService(serviceName, provider);

                        List<ServiceRegistration> services = null;
                        if (registrations.containsKey(serviceName)) {
                            services = registrations.get(serviceName);
                        } else {
                            services = new 
                            
                            ArrayList<ServiceRegistration>();
                            services.add(sr);
                            registrations.put(serviceName, services);
                        }                        
                    } else if (line.startsWith("u")) {
                        String[] array = line.split(" ");
                        
                        final QName serviceName = new QName(array[1]);
                        List<ServiceRegistration> services = null;
                        if (registrations.containsKey(serviceName)) {
                            services = registrations.get(serviceName);
                            ServiceRegistration sr = services.get(0);
                            sr.unregister();
                            services.remove(0);
                        } else {
                            System.out.println();
                            System.out.println("Could not find a registered service for "
                                    + "[" + serviceName.toString() + "]");
                            System.out.println();
                        }
                    } else if (("quit".equalsIgnoreCase(line))) {
                        break;
                    } 
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }    
    
    public static void main (String args[]) {
        JGroupsStandingRegistry jgsrt = new JGroupsStandingRegistry();
        try {
            jgsrt.setUp();
            jgsrt.loop();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
}