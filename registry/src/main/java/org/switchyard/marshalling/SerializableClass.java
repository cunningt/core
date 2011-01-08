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

import org.switchyard.Context;
import org.switchyard.Exchange;
import org.switchyard.Message;
import org.switchyard.internal.RegistrationMessage;


public enum SerializableClass {
    EXCHANGE(0), MESSAGE(1), CONTEXT(2), REGISTRATION_MESSAGE(3);
    private static final Class[] _classes = { Exchange.class, Message.class, Context.class,
        RegistrationMessage.class};
    private byte _code;

    private SerializableClass(int c) {
      _code = (byte) c;
    }

    public byte getByte() {
        return _code;
    }

    public static Class[] getAvailableClasses() {
        return _classes;
    }

    public Class getSerializableClass() {
        return _classes[_code];
    }

    public static SerializableClass getSerializableClass(byte b) {
        switch (b) {
        case 0: return SerializableClass.EXCHANGE;
        case 1: return SerializableClass.MESSAGE;
        case 2: return SerializableClass.CONTEXT;
        case 3: return SerializableClass.REGISTRATION_MESSAGE;
        default: throw new RuntimeException("Cannot find serializable class for byte " + b);
        }
    }

    public static Class getCorrespondingClass(byte b) {
        return _classes[b];
    }
}