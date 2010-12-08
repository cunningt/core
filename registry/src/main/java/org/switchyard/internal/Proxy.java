package org.switchyard.internal;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.ChannelClosedException;
import org.jgroups.ChannelException;
import org.jgroups.ChannelNotConnectedException;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.switchyard.Exchange;
import org.switchyard.Service;
import org.switchyard.ServiceDomain;
import org.switchyard.spi.Endpoint;
import org.switchyard.spi.ServiceRegistry;

public class Proxy extends ReceiverAdapter {
    private ServiceRegistry _registry;
    private JChannel _channel;
    
    public static final String CLUSTER_NAME = "org.switchyard.registry.cluster";
    public static final String DEFAULT_CLUSTER = "SwitchyardCluster";

    public static final String REGISTER_MESSAGE = "register";
    public static final String UNREGISTER_MESSAGE = "unregister";

    public Proxy (ServiceRegistry registry) throws ChannelException {
        String clusterName = System.getProperty(CLUSTER_NAME, DEFAULT_CLUSTER);
        
        _registry = registry; 
        
        _channel = new JChannel();
        _channel.setReceiver(this);
        _channel.connect(clusterName);  
    }
    
    public void send(DistributedEndpoint endpoint, Exchange exchange) {
	Address target = endpoint.getAddress();
	ExchangeWrapper ew = new ExchangeWrapper(exchange);
	Message message = new Message(target, _channel.getAddress(),
		ew);
    }	
        
    public void processExchange(ExchangeWrapper wrapper) {
	ServiceDomain domain = ServiceDomains.getDomain();
	Exchange exchange = domain.createExchange(wrapper.getServiceName(), 
		wrapper.getExchangePattern());
	exchange.send(wrapper.getMessage());
    }
    
    public void receive(Message message) {
	Object object = message.getObject();
	if (object instanceof ExchangeWrapper) {
	    ExchangeWrapper wrapper = (ExchangeWrapper) object;
	    processExchange(wrapper);
	    return;
	}
	
        RegistrationMessage msg = (RegistrationMessage) message.getObject();
        QName serviceName = msg.getName();
        
        if (msg.getAction().equals(RegistrationAction.REGISTER)) {            
            DistributedEndpoint de = new DistributedEndpoint(this, message.getSrc());
            ServiceDomain domain = ServiceDomains.getDomain(msg.getDomainName());
            
            // If we are receiving this registration message from ourselves, ignore it
            if (_channel.getAddress().compareTo(de.getAddress()) == 0) {
        	return;
            } else {
        	_registry.registerService(serviceName, de, null, domain);
            }
        } else if (msg.getAction().equals(RegistrationAction.UNREGISTER)) {
            List<Service> serviceList = _registry.getServices(serviceName);
            for (Service service : serviceList) {
        	ServiceRegistration sr = (ServiceRegistration) service;
        	Endpoint endpoint = sr.getEndpoint();
        	if (endpoint instanceof DistributedEndpoint) {
        	    DistributedEndpoint distribEndpoint = (DistributedEndpoint) endpoint;
        	    if (message.getSrc().compareTo(distribEndpoint.getAddress()) == 0) {
        		_registry.unregisterService(sr);
        	    }
        	}
            }            
        } else if (msg.getAction().equals(RegistrationAction.POPULATE)) {
        } else {
            throw new RuntimeException("Received invalid registration message");
        }
        ((JGroupsRegistry)_registry).printRegistry();
    }    

    public void viewAccepted(View new_view) {
        System.out.println("** view: " + new_view);
    }
        
    /**
     * Sends a registration notification.
     * @throws ChannelClosedException 
     * @throws ChannelNotConnectedException 
     */
    public void sendRegisterNotification(QName serviceName, String domainName) throws ChannelNotConnectedException, ChannelClosedException {
	RegistrationMessage regMsg = new RegistrationMessage(serviceName, RegistrationAction.REGISTER);
        regMsg.setDomainName(domainName);
        Message msg=new Message(null, _channel.getAddress(), regMsg);
        
        _channel.send(msg);
    }
    
    /**
     * Sends an unregister notification. 
     * @throws ChannelClosedException 
     * @throws ChannelNotConnectedException 
     */
    public void sendDeleteNotification(QName serviceName) throws ChannelNotConnectedException, ChannelClosedException {
	RegistrationMessage regMsg = new RegistrationMessage(serviceName, RegistrationAction.UNREGISTER);
        Message msg=new Message(null, _channel.getAddress(), regMsg);
        
        _channel.send(msg);        
    }	
}