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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.jboss.marshalling.ByteInput;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.Unmarshaller;
import org.jgroups.Address;
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
import org.switchyard.marshalling.MarshallerProvider;
import org.switchyard.marshalling.SerializableClass;
import org.switchyard.marshalling.SwitchyardStreamHeader;
import org.switchyard.marshalling.UnmarshallerProvider;
import org.switchyard.spi.Endpoint;
import org.switchyard.spi.ServiceRegistry;
import org.switchyard.wrapper.SerializableExchangeWrapper;
import org.switchyard.wrapper.SerializableMessageWrapper;

/**
 * @author <a href="mailto:tcunning@redhat.com">Tom Cunningham</a>
 */
public class RegistryProxy extends ReceiverAdapter {
    private final ServiceRegistry _registry;
    private final JChannel _channel;

    public static final String CLUSTER_NAME = "org.switchyard.registry.cluster";
    public static final String DEFAULT_CLUSTER = "SwitchyardCluster";

    public static final String REGISTER_MESSAGE = "register";
    public static final String UNREGISTER_MESSAGE = "unregister";

    private final MarshallerProvider _marshallerProvider;
    private final UnmarshallerProvider _unmarshallerProvider;

    private final Map<Address, List<ServiceRegistration>> _remoteServices =
        new HashMap<Address, List<ServiceRegistration>>();

    /**
     * Proxy constructor.
     * @param registry registry
     * @throws ChannelException exception
     */
    public RegistryProxy(ServiceRegistry registry) throws ChannelException {
        String clusterName = System.getProperty(CLUSTER_NAME, DEFAULT_CLUSTER);

        // Set up marshalling
        _registry = registry;
        _marshallerProvider = new MarshallerProvider();
        _unmarshallerProvider = new UnmarshallerProvider();

        _channel = new JChannel();
        _channel.setReceiver(this);
        _channel.connect(clusterName);
    }

    /**
     * Send the exchange to the endpoint.
     * @param endpoint endpoint
     * @param exchange exchange
     */
    public void send(DistributedEndpoint endpoint, Exchange exchange) {
        Address target = endpoint.getAddress();
        SerializableExchangeWrapper ew =
            new SerializableExchangeWrapper(exchange);

        SwitchyardStreamHeader streamHeader = new SwitchyardStreamHeader(SerializableClass.EXCHANGE);
        _marshallerProvider.getConfiguration().setStreamHeader(streamHeader);
        byte[] bytes = _marshallerProvider.marshal(ew);
        Message message = new Message(target, _channel.getAddress(),
                bytes);
        try {
            _channel.send(message);
        } catch (ChannelNotConnectedException e) {
            throw new RuntimeException(e);
        } catch (ChannelClosedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Process the exchange.
     * @param wrapper wrapper
     */
    public void processExchange(SerializableExchangeWrapper wrapper) {
        ServiceDomain domain = ServiceDomains.getDomain();
        Exchange exchange = domain.createExchange(wrapper.getServiceName(),
                wrapper.getExchangePattern());
        exchange.send(wrapper.getMessage());
    }

    /**
     * Process the exchange.
     * @param wrapper wrapper
     */
    public void processMessage(SerializableMessageWrapper wrapper) {
        //ServiceDomain domain = ServiceDomains.getDomain();
        //Exchange exchange = domain.createExchange(wrapper.getServiceName(),
        //        wrapper.getExchangePattern());
        //exchange.send(wrapper.getMessage());
    }


    /* (non-Javadoc)
     * @see org.jgroups.ReceiverAdapter#receive(org.jgroups.Message)
     */
    @Override
    public void receive(Message message) {

        RegistrationMessage msg = null;
        byte[] bytes = message.getBuffer();
        ByteInput byteInput = Marshalling.createByteInput(new ByteArrayInputStream(bytes));
        SwitchyardStreamHeader header = new SwitchyardStreamHeader();
        _unmarshallerProvider.getConfiguration().setStreamHeader(header);

        try {
            Unmarshaller unmarshaller = _unmarshallerProvider.getUnmarshaller(byteInput);
            SerializableClass sclass = header.getSerializableClass();
            if (sclass.getByte() == SerializableClass.EXCHANGE.getByte()) {
                SerializableExchangeWrapper wrapper = (SerializableExchangeWrapper) unmarshaller.readObject();
                unmarshaller.finish();
                processExchange(wrapper);
                return;
            } else if (sclass.getByte() == SerializableClass.MESSAGE.getByte()) {
                SerializableMessageWrapper wrapper = (SerializableMessageWrapper) unmarshaller.readObject();
                unmarshaller.finish();
                processMessage(wrapper);
                return;
            } else if (sclass.getByte() == SerializableClass.REGISTRATION_MESSAGE.getByte()) {
                msg = (RegistrationMessage) unmarshaller.readObject();
                unmarshaller.finish();
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException(cnfe);
        }

        QName serviceName = msg.getName();

        if (msg.getAction().equals(RegistrationAction.REGISTER)) {
            DistributedEndpoint de =
                new DistributedEndpoint(this, message.getSrc());
            ServiceDomain domain =
                ServiceDomains.getDomain(msg.getDomainName());

            if (_channel.getAddress().compareTo(de.getAddress()) == 0) {
                // If we are receiving this registration
                // message from ourselves, ignore it
                return;
            } else {
                ServiceRegistration sr = (ServiceRegistration)
                    _registry.registerService(serviceName, de, null, domain);

                List<ServiceRegistration> remoteList =
                    _remoteServices.get(de.getAddress());
                if (remoteList == null) {
                    remoteList = new LinkedList<ServiceRegistration>();
                    _remoteServices.put(de.getAddress(), remoteList);
                }
                remoteList.add(sr);
            }
        } else if (msg.getAction().equals(RegistrationAction.UNREGISTER)) {
            List<Service> serviceList = _registry.getServices(serviceName);
            for (Service service : serviceList) {
                ServiceRegistration sr = (ServiceRegistration) service;
                Endpoint endpoint = sr.getEndpoint();
                if (endpoint instanceof DistributedEndpoint) {
                    DistributedEndpoint distribEndpoint =
                        (DistributedEndpoint) endpoint;
                    if (message.getSrc().compareTo(
                            distribEndpoint.getAddress()) == 0) {
                        _registry.unregisterService(sr);
                    }

                    // Remove it from our remoteServices list
                    List<ServiceRegistration> remoteList =
                        _remoteServices.get(distribEndpoint.getAddress());
                    if (remoteList == null) {
                        remoteList = new LinkedList<ServiceRegistration>();
                        _remoteServices.put(distribEndpoint.getAddress(),
                                remoteList);
                    }
                    remoteList.remove(sr);
                }
            }
        } else if (msg.getAction().equals(RegistrationAction.POPULATE)) {
            if (_channel.getAddress().compareTo(message.getSrc()) == 0) {
                // If we are receiving this registration
                // message from ourselves, ignore it
                return;
            } else {
                JGroupsRegistry jgr = (JGroupsRegistry) _registry;
                List<Service> services = jgr.getLocalServices();
                for (Iterator i = services.iterator(); i.hasNext();) {
                    ServiceRegistration service = (ServiceRegistration) i.next();
                    try {
                        sendPopulationRegistration(message.getSrc(), service.getName(),
                            service.getDomain().getName());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } else {
            throw new RuntimeException("Received invalid registration message");
        }
        ((JGroupsRegistry) _registry).printRegistry();
    }

    /* (non-Javadoc)
     * @see org.jgroups.ReceiverAdapter#suspect(org.jgroups.Address)
     */
    @Override
    public void suspect(Address mbr) {
        // Remove it from our remoteServices list
        List<ServiceRegistration> remoteList = _remoteServices.get(mbr);
        if (remoteList != null) {
            for (ServiceRegistration sr : remoteList) {
                System.out.println("Unregistering service "
                        + sr.getName() + " because address "
                        + mbr.toString() + " is suspected");
                _registry.unregisterService(sr);
            }
        }
        _remoteServices.remove(mbr);

        System.out.println();
        System.out.println("** suspect: " + mbr.toString());
        System.out.println();
    }

    /* (non-Javadoc)
     * @see org.jgroups.ReceiverAdapter#viewAccepted(org.jgroups.View)
     */
    @Override
    public void viewAccepted(View new_view) {
        System.out.println();
        System.out.println("** view: " + new_view);
        System.out.println();
    }

    /**
     * Sends a registration notification.
     * @param serviceName service name
     * @param domainName domain name
     * @throws ChannelClosedException ChannelClosedException
     * @throws ChannelNotConnectedException ChannelNotConnectedException
     */
    public void sendRegisterNotification(QName serviceName, String domainName)
        throws ChannelNotConnectedException, ChannelClosedException {
        RegistrationMessage regMsg =
            new RegistrationMessage(serviceName, RegistrationAction.REGISTER);
        regMsg.setDomainName(domainName);

        SwitchyardStreamHeader streamHeader = new SwitchyardStreamHeader(SerializableClass.REGISTRATION_MESSAGE);
        _marshallerProvider.getConfiguration().setStreamHeader(streamHeader);
        byte[] bytes = _marshallerProvider.marshal(regMsg);

        Message msg = new Message(null, _channel.getAddress(), bytes);
        _channel.send(msg);
    }

    /**
     * Sends an unregister notification.
     * @throws ChannelClosedException ChannelClosedException
     * @throws ChannelNotConnectedException ChannelNotConnectedException
     */
    public void sendDeleteNotification(QName serviceName)
        throws ChannelNotConnectedException, ChannelClosedException {
        RegistrationMessage regMsg =
            new RegistrationMessage(serviceName, RegistrationAction.UNREGISTER);

        SwitchyardStreamHeader streamHeader = new SwitchyardStreamHeader(SerializableClass.REGISTRATION_MESSAGE);
        _marshallerProvider.getConfiguration().setStreamHeader(streamHeader);
        byte[] bytes = _marshallerProvider.marshal(regMsg);

        Message msg = new Message(null, _channel.getAddress(), bytes);
        _channel.send(msg);
    }

    /**
     * Sends an populate notification for other nodes to send all
     * their registered services.
     * @throws ChannelClosedException ChannelClosedException
     * @throws ChannelNotConnectedException ChannelNotConnectedException
     */
    public void sendPopulateNotification()
        throws ChannelNotConnectedException, ChannelClosedException {
        RegistrationMessage regMsg =
            new RegistrationMessage(null, RegistrationAction.POPULATE);

        SwitchyardStreamHeader streamHeader = new SwitchyardStreamHeader(SerializableClass.REGISTRATION_MESSAGE);
        _marshallerProvider.getConfiguration().setStreamHeader(streamHeader);
        byte[] bytes = _marshallerProvider.marshal(regMsg);

        Message msg = new Message(null, _channel.getAddress(), bytes);
        _channel.send(msg);
    }

    /**
     * Sends a registration notification.
     * @param serviceName service name
     * @param domainName domain name
     * @throws ChannelClosedException ChannelClosedException
     * @throws ChannelNotConnectedException ChannelNotConnectedException
     */
    public void sendPopulationRegistration(Address address, QName serviceName, String domainName)
        throws ChannelNotConnectedException, ChannelClosedException {
        RegistrationMessage regMsg =
            new RegistrationMessage(serviceName, RegistrationAction.REGISTER);
        regMsg.setDomainName(domainName);

        SwitchyardStreamHeader streamHeader = new SwitchyardStreamHeader(SerializableClass.REGISTRATION_MESSAGE);
        _marshallerProvider.getConfiguration().setStreamHeader(streamHeader);
        byte[] bytes = _marshallerProvider.marshal(regMsg);

        Message msg = new Message(address, _channel.getAddress(), bytes);
        _channel.send(msg);
    }
}