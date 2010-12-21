package org.switchyard.testutil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;

import org.switchyard.ServiceDomain;
import org.switchyard.internal.DistributedEndpointProvider;
import org.switchyard.internal.JGroupsRegistry;
import org.switchyard.internal.ServiceDomains;
import org.switchyard.internal.ServiceRegistration;

public class SwitchyardRunner {
    ServiceDomain domain = null;
    HashMap<QName, List<ServiceRegistration>> registrations = new HashMap<QName, List<ServiceRegistration>>();

    public void setUp() throws Exception {
        System.setProperty(ServiceDomains.REGISTRY_CLASS_NAME,
            JGroupsRegistry.class.getName());
        System.setProperty(ServiceDomains.ENDPOINT_PROVIDER_CLASS_NAME,
            DistributedEndpointProvider.class.getName());
        domain = ServiceDomains.getDomain();
    }

    public void loop() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while(true) {
        }
    }

}
