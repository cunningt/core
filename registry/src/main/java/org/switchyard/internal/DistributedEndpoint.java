package org.switchyard.internal;

import org.jgroups.Address;
import org.switchyard.Exchange;
import org.switchyard.HandlerChain;
import org.switchyard.spi.Endpoint;

public class DistributedEndpoint implements Endpoint {
    private HandlerChain _handlerChain;
    private Address _address;
    private Proxy _proxy;
	   
    DistributedEndpoint(Proxy proxy, Address address) {
	_proxy = proxy;
	_address = address;
    }
	   
    DistributedEndpoint(HandlerChain handlerChain) {
	_handlerChain = handlerChain;
    }

    public Address getAddress() {
	return _address;
    }
    
    @Override
    public void send(Exchange exchange) {
	if ( _address != null) {
	    _proxy.send(exchange);
	} else if (_handlerChain != null) {
	    _handlerChain.handle(exchange);
	} else {
	}
    }
}