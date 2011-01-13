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
package org.switchyard.marshalling;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.jboss.marshalling.ByteOutput;
import org.jboss.marshalling.Marshaller;
import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;

public class MarshallerProvider {

    private final MarshallingConfiguration _configuration;
    private final MarshallerFactory _marshallerFactory;

    public MarshallerProvider() {
        _configuration = new MarshallingConfiguration();
        _marshallerFactory = Marshalling.getMarshallerFactory("river");
    }

    public MarshallingConfiguration getConfiguration() {
        return _configuration;
    }

    public byte[] marshal(Object object) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(10240);
        ByteOutput byteOutput = Marshalling.createByteOutput(baos);
        try {
            Marshaller marshaller = _marshallerFactory.createMarshaller(_configuration);
            marshaller.start(byteOutput);
            marshaller.writeObject(object);
            marshaller.finish();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        byte[] bytes = baos.toByteArray();
        return bytes;
    }


}
