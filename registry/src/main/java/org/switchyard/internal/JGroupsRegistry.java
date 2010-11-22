/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.switchyard.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.jgroups.ChannelClosedException;
import org.jgroups.ChannelNotConnectedException;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.switchyard.HandlerChain;
import org.switchyard.Service;
import org.switchyard.ServiceDomain;
import org.switchyard.spi.Endpoint;
import org.switchyard.spi.ServiceRegistry;

/**
 * A distributed Registry that broadcasts register and unregister messages
 * to track remote services within a cluster.
 * 
 * @author tcunning
 */
public class JGroupsRegistry extends ReceiverAdapter implements ServiceRegistry {
    public static final String CLUSTER_NAME = "org.switchyard.registry.cluster";
    public static final String DEFAULT_CLUSTER = "SwitchyardCluster";
    public static final String REGISTER_MESSAGE = "register";
    public static final String UNREGISTER_MESSAGE = "unregister";
    
    private JChannel _channel;
    
    private Map<QName, List<ServiceRegistration>> _localServices = 
        new HashMap<QName, List<ServiceRegistration>>();
    
    private Map<QName, List<ServiceRegistration>> _remoteServices =
        new HashMap<QName, List<ServiceRegistration>>();
    
    /**
     * Constructor
     */
    public JGroupsRegistry() throws Exception {
        super();
        
        String clusterName = System.getProperty(CLUSTER_NAME, DEFAULT_CLUSTER);
        
        _channel = new JChannel();
        _channel.setReceiver(this);
        _channel.connect(clusterName);  
    }
    
    public void receive(Message message) {
        RegistrationMessage msg = (RegistrationMessage) message.getObject();
        QName serviceName = msg.getName();
        ServiceRegistration sr = new ServiceRegistration(serviceName, null, null, this, null);

        if (msg.getAction().equals(RegistrationAction.REGISTER)) {            
            List<ServiceRegistration> serviceList = _remoteServices.get(serviceName);
            if (serviceList == null) {
                serviceList = new LinkedList<ServiceRegistration>();
                 _remoteServices.put(serviceName, serviceList);
            }
            serviceList.add(sr);
        } else if (msg.getAction().equals(RegistrationAction.UNREGISTER)) {
            List<ServiceRegistration> serviceList = _remoteServices.get(serviceName);
            if (serviceList != null) {
                serviceList.remove(0); // need to do something smarter here than remove the first service
                if (serviceList.size() == 0) {
                    _remoteServices.remove(sr);
                }
            }
        } else if (msg.getAction().equals(RegistrationAction.POPULATE)) {
        } else {
            throw new RuntimeException("Received invalid registration message");
        }
        printRegistry();
    }    
    
    public void viewAccepted(View new_view) {
        System.out.println("** view: " + new_view);
    }
        
    /**
     * Sends a registration notification.
     * @throws ChannelClosedException 
     * @throws ChannelNotConnectedException 
     */
    public void sendRegisterNotification(QName serviceName) throws ChannelNotConnectedException, ChannelClosedException {
        RegistrationMessage regMsg = new RegistrationMessage(serviceName, RegistrationAction.REGISTER);
        Message msg=new Message(null, null, regMsg);

        _channel.send(msg);
    }
    
    /**
     * Sends an unregister notification. 
     * @throws ChannelClosedException 
     * @throws ChannelNotConnectedException 
     */
    public void sendDeleteNotification(QName serviceName) throws ChannelNotConnectedException, ChannelClosedException {
        RegistrationMessage regMsg = new RegistrationMessage(serviceName, RegistrationAction.UNREGISTER);
        Message msg=new Message(null, null, regMsg);
        
        _channel.send(msg);        
    }

    @Override
    public Service registerService(QName serviceName, Endpoint endpoint,
            HandlerChain handlers, ServiceDomain domain) {
        ServiceRegistration sr = new ServiceRegistration(
                serviceName, endpoint, handlers, this, domain);
        
        List<ServiceRegistration> serviceList = _localServices.get(serviceName);
        if (serviceList == null) {
            serviceList = new LinkedList<ServiceRegistration>();
             _localServices.put(serviceName, serviceList);
        }
        try {
            sendRegisterNotification(serviceName);
        } catch (ChannelNotConnectedException e) {
            throw new RuntimeException(e);
        } catch (ChannelClosedException e) {
            throw new RuntimeException(e);
        }
        
        serviceList.add(sr);
        
        printRegistry();
        return sr;
    }

    @Override
    public void unregisterService(Service service) {
        List<ServiceRegistration> serviceList = _localServices.get(service.getName());
        if (serviceList != null) {
            serviceList.remove(service);
            try {
                sendDeleteNotification(service.getName());
            } catch (ChannelNotConnectedException e) {
                throw new RuntimeException(e);
            } catch (ChannelClosedException e) {
                throw new RuntimeException(e);
            }
            
            printRegistry();
        }
    }

    @Override
    public List<Service> getServices() {
        LinkedList<Service> serviceList = new LinkedList<Service>();
        for (List<ServiceRegistration> services : _localServices.values()) {
            serviceList.addAll(services);
        }
        for (List<ServiceRegistration> services : _remoteServices.values()) {
            serviceList.addAll(services);
        }
        
        return serviceList;
    }

    @Override
    public List<Service> getServices(QName serviceName) {
        LinkedList<Service> serviceList = new LinkedList<Service>();
        for (List<ServiceRegistration> services : _localServices.values()) {
            serviceList.addAll(services);
        }
        
        for (List<ServiceRegistration> services : _remoteServices.values()) {
            serviceList.addAll(services);
        }
        
        return serviceList;
    }

    @Override
    public List<Service> getServicesForDomain(String domainName) {
        List<Service> domainServices = getServices();
        // Using an explicit iterator because we are removing elements
        for (Iterator<Service> i = domainServices.iterator(); i.hasNext(); ) {
            ServiceRegistration sr = (ServiceRegistration)i.next();
            // prune services that do not match the specified domain
            if (!sr.getDomain().getName().equals(domainName)) {
                i.remove();
            }
        }                
        return domainServices;
    }
    
    protected void printRegistry() {
        System.out.println("====================================");
        System.out.println("Local Services");
        System.out.println("====================================");
        for (List<ServiceRegistration> services : _localServices.values()) {
            for (ServiceRegistration sr : services) {
                System.out.println("Domain [" + sr.getDomain() + "] "
                        + "Service [" + sr.getName().toString() + "]");
            }
        }
        
        System.out.println("====================================");
        System.out.println("Remote Services");
        System.out.println("====================================");
        for (List<ServiceRegistration> services : _remoteServices.values()) {
            for (ServiceRegistration sr : services) {
                System.out.println("Domain [" + sr.getDomain() + "] "
                        + "Service [" + sr.getName().toString() + "]");
            }
        }        
        System.out.println("====================================");
        System.out.println();
    }
}
