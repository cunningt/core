package org.switchyard.internal;

import javax.xml.namespace.QName;

import org.jgroups.Address;
import org.switchyard.HandlerChain;
import org.switchyard.ServiceDomain;
import org.switchyard.spi.Endpoint;
import org.switchyard.spi.ServiceRegistry;

public class RemoteServiceRegistration extends ServiceRegistration {
    private Address _address;
    
    RemoteServiceRegistration(QName serviceName, Endpoint endpoint,
            HandlerChain handlers, ServiceRegistry registry,
            ServiceDomain domain) {
	super(serviceName, endpoint, handlers, registry, domain);
    }

    public Address getAddress() {
	return _address;
    }
    
    public void setAddress(Address address) {
	_address = address;
    }
}
