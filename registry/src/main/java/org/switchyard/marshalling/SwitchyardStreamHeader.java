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

import java.io.IOException;
import java.io.StreamCorruptedException;

import org.jboss.marshalling.ByteInput;
import org.jboss.marshalling.ByteOutput;
import org.jboss.marshalling.StreamHeader;

public class SwitchyardStreamHeader implements StreamHeader {
    private SerializableClass _serializedClass;

    public SwitchyardStreamHeader(){
    }

    public SwitchyardStreamHeader(SerializableClass sclass) {
        _serializedClass = sclass;
    }

    public SerializableClass getSerializableClass() {
        return _serializedClass;
    }

    @Override
    public void readHeader(ByteInput input) throws IOException {
        byte b1 = (byte) input.read();

        if ((b1 < 0) || (b1 > (SerializableClass.getAvailableClasses().length))) {
            throw new StreamCorruptedException("invalid stream header");
        }

        _serializedClass = SerializableClass.getSerializableClass(b1);
    }

    @Override
    public void writeHeader(ByteOutput output) throws IOException {
        output.write(_serializedClass.getByte());
    }
}