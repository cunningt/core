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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.jgroups.ChannelClosedException;
import org.jgroups.ChannelNotConnectedException;
import org.switchyard.HandlerChain;
import org.switchyard.Service;
import org.switchyard.ServiceDomain;
import org.switchyard.spi.Endpoint;
import org.switchyard.spi.ServiceRegistry;

/**
 * A distributed Registry that broadcasts register and unregister messages
 * to track remote services within a cluster.
 *
 * @author <a href="mailto:tcunning@redhat.com">Tom Cunningham</a>
 */
public class JGroupsRegistry implements ServiceRegistry {

    private final Map<QName, List<ServiceRegistration>> _localServices =
        new HashMap<QName, List<ServiceRegistration>>();

    private final Map<QName, List<ServiceRegistration>> _remoteServices =
        new HashMap<QName, List<ServiceRegistration>>();

    private final RegistryProxy _proxy;

    /**
     * Constructor
     */
    public JGroupsRegistry() throws Exception {
        super();
        _proxy = new RegistryProxy(this);
        _proxy.sendPopulateNotification();
    }

    @Override
    public Service registerService(QName serviceName, Endpoint endpoint,
            HandlerChain handlers, ServiceDomain domain) {
        ServiceRegistration sr = new ServiceRegistration(
                serviceName, endpoint, handlers, this, domain);
        boolean remoteFlag = false;

        if (endpoint instanceof DistributedEndpoint) {
            DistributedEndpoint de = (DistributedEndpoint) endpoint;
            remoteFlag = de.getAddress() != null;
        }

        if (remoteFlag) {
            List<ServiceRegistration> remoteList = _remoteServices.get(serviceName);
            if (remoteList == null) {
                remoteList = new LinkedList<ServiceRegistration>();
                _remoteServices.put(serviceName, remoteList);
            }
            remoteList.add(sr);
        } else {
            List<ServiceRegistration> serviceList = _localServices.get(serviceName);
            if (serviceList == null) {
                serviceList = new LinkedList<ServiceRegistration>();
                 _localServices.put(serviceName, serviceList);
            }
            try {
                _proxy.sendRegisterNotification(serviceName, domain.getName());
            } catch (ChannelNotConnectedException e) {
                throw new RuntimeException(e);
            } catch (ChannelClosedException e) {
                throw new RuntimeException(e);
            }

            serviceList.add(sr);
        }

        printRegistry();
        return sr;
    }

    @Override
    public void unregisterService(Service service) {
        // Check whether this is a remote service
        List<ServiceRegistration> remoteList = _remoteServices.get(service.getName());
        if ((remoteList != null) && (remoteList.contains(service))) {
            remoteList.remove(service);
            printRegistry();
            return;
        }

        // Not a remote service - remove the local service
        List<ServiceRegistration> serviceList = _localServices.get(service.getName());
        if (serviceList != null) {
            serviceList.remove(service);
            try {
                _proxy.sendDeleteNotification(service.getName());
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

        List<ServiceRegistration> locals = _localServices.get(serviceName);
        if (locals != null) {
            serviceList.addAll(locals);
        }

        List<ServiceRegistration> remotes = _remoteServices.get(serviceName);
        if (remotes != null) {
            serviceList.addAll(remotes);
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

    protected List<Service> getLocalServices() {
        List<Service> localServices = new ArrayList<Service>();
        // Using an explicit iterator because we are removing elements
        Collection<List<ServiceRegistration>> services = _localServices.values();
        for (Iterator i = services.iterator(); i.hasNext(); ) {
            List<ServiceRegistration> sr = (List<ServiceRegistration>) i.next();
            localServices.addAll(sr);
        }

        return localServices;
    }

    public void printRegistry() {
        System.out.println();
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