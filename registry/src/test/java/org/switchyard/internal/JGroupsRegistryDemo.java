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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.switchyard.BaseHandler;
import org.switchyard.Exchange;
import org.switchyard.ExchangeHandler;
import org.switchyard.ExchangePattern;
import org.switchyard.Message;
import org.switchyard.MessageBuilder;
import org.switchyard.ServiceDomain;

/**
 * @author <a href="mailto:tcunning@redhat.com">Tom Cunningham</a>
 */
public class JGroupsRegistryDemo {
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
        domain = ServiceDomains.createDomain(JGroupsRegistryDemo.class.getName());
        loop();
    }

    private void prompt() {
        System.out.println();
        System.out.print("[(r)egister/(u)nregister/(e)xchange <service>- ");
        System.out.flush();
    }

    private void loop() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            try {
                prompt();

                String line = in.readLine();
                if (!line.isEmpty()) {
                    if ((line.startsWith("r"))) {
                        String[] array = line.split(" ");

                        final QName serviceName = new QName(array[1]);
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

                        List<ServiceRegistration> services = null;
                        if (registrations.containsKey(serviceName)) {
                            services = registrations.get(serviceName);
                        } else {
                            services = new ArrayList<ServiceRegistration>();
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
                    } else if (line.startsWith("e")) {
                        String[] array = line.split(" ");

                        final QName serviceName = new QName(array[1]);

                        Exchange exchange = domain.createExchange(serviceName, ExchangePattern.IN_OUT);
                	Message message = MessageBuilder.newInstance().buildMessage();
                	message.setContent("foo");
                	exchange.send(message);
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
        JGroupsRegistryDemo jgsrt = new JGroupsRegistryDemo();
        try {
            jgsrt.setUp();
            jgsrt.loop();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}