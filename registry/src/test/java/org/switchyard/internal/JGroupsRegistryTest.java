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

import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.switchyard.MockHandler;
import org.switchyard.ServiceDomain;

/**
 * @author <a href="mailto:tcunning@redhat.com">Tom Cunningham</a>
 */
public class JGroupsRegistryTest {
    @Before
    public void setUp() throws Exception {
        System.setProperty(ServiceDomains.REGISTRY_CLASS_NAME, 
            JGroupsRegistry.class.getName());
        System.setProperty(ServiceDomains.ENDPOINT_PROVIDER_CLASS_NAME, 
            DistributedEndpointProvider.class.getName()); 
    }
    
    @After
    public void tearDown() throws Exception {
        System.clearProperty(ServiceDomains.REGISTRY_CLASS_NAME);
        System.clearProperty(ServiceDomains.ENDPOINT_PROVIDER_CLASS_NAME);
    }
    
    @Test
    public void testSystemPropertiesSet() throws Exception  {
        final String DOMAIN1 = "properties";
    
        Assert.assertEquals(System.getProperty(ServiceDomains.REGISTRY_CLASS_NAME), JGroupsRegistry.class.getName());
        Assert.assertEquals(System.getProperty(ServiceDomains.ENDPOINT_PROVIDER_CLASS_NAME), DistributedEndpointProvider.class.getName());               
        
        ServiceDomain domain = ServiceDomains.createDomain(DOMAIN1);
        final QName serviceName = new QName("inOutFault");
        // Provide the service
        MockHandler provider = new MockHandler().forwardInToFault();
        ServiceRegistration sr = (ServiceRegistration) domain.registerService(serviceName, provider);
        sr.unregister();
    }    
}
