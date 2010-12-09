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
package org.switchyard.wrapper;

import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.switchyard.Message;
import org.switchyard.MessageBuilder;
import org.switchyard.message.DefaultMessage;

/**
 * @author <a href="mailto:tcunning@redhat.com">Tom Cunningham</a>
 */
public class SerializableMessageWrapper implements Serializable {
    private static final long serialVersionUID = 380103027061648991L;
    private transient DefaultMessage _message;

    /**
     * Wrapper constructor.
     * @param message message
     */
    public SerializableMessageWrapper(Message message) {
        _message = (DefaultMessage) message;
    }

    /**
     * Message getter.
     * @return message
     */
    public Message getMessage() {
        return _message;
    }

    /**
     * Write the message to the ObjectOutputStream.
     * @param out output stream
     * @throws IOException ioexception
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        MessageBuilder mb = MessageBuilder.newInstance(DefaultMessage.class);
        mb.writeMessage(_message, out);
    }

    /**
     * Read the message from the ObjectInputStream.
     * @param in inputstream
     * @throws IOException ioexception
     * @throws ClassNotFoundException classnotfoundexception
     */
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        MessageBuilder mb = MessageBuilder.newInstance(DefaultMessage.class);
        _message = (DefaultMessage) mb.readMessage(in);
    }
}
