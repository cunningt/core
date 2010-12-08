package org.switchyard.internal;

import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.switchyard.Message;
import org.switchyard.MessageBuilder;
import org.switchyard.message.DefaultMessage;

public class DefaultMessageWrapper implements Serializable {
    private static final long serialVersionUID = 380103027061648991L;
    private transient DefaultMessage _message;
    
    public DefaultMessageWrapper(Message message) {
        _message = (DefaultMessage) message;
    }
    
    public Message getMessage() {
        return _message;
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        MessageBuilder mb = MessageBuilder.newInstance(DefaultMessage.class);
        mb.writeMessage(_message, out);
    }
    
    private void readObject(ObjectInputStream in) 
        throws IOException, ClassNotFoundException {
        MessageBuilder mb = MessageBuilder.newInstance(DefaultMessage.class);
        _message = (DefaultMessage) mb.readMessage(in);
    }
}
