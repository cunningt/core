package org.switchyard.internal;

import java.io.Serializable;
import javax.xml.namespace.QName;

public class RegistrationMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private QName _name;
    private RegistrationAction _action;
    private ServiceRegistration registration;
    
    RegistrationMessage(QName name, RegistrationAction action) {
        _name = name;
        _action = action;
    }
    
    public QName getName() {
        return _name;
    }

    public void set_name(QName name) {
        _name = name;
    }

    public RegistrationAction getAction() {
        return _action;
    }

    public void setAction(RegistrationAction action) {
        _action = action;
    }    
    
    public ServiceRegistration getRegistration() {
        return registration;
    }

    public void setRegistration(ServiceRegistration registration) {
        this.registration = registration;
    }    
}
