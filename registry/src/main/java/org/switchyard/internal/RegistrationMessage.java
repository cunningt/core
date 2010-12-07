package org.switchyard.internal;

import java.io.Serializable;
import javax.xml.namespace.QName;

public class RegistrationMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private QName _name;
    private RegistrationAction _action;
    private String _domainName;
    
    RegistrationMessage(QName name, RegistrationAction action) {
        _name = name;
        _action = action;
    }
    
    RegistrationMessage(QName name, RegistrationAction action, String domainName) {
	_name = name;
	_action = action;
	_domainName = domainName;
    }
    
    public QName getName() {
        return _name;
    }

    public void setName(QName name) {
        _name = name;
    }

    public RegistrationAction getAction() {
        return _action;
    }

    public void setAction(RegistrationAction action) {
        _action = action;
    }        

    public String getDomainName() {
	return _domainName;
    }
    
    public void setDomainName(String domainName) {
	_domainName = domainName;
    }
}
