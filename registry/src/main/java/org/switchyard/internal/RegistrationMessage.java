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

import java.io.Serializable;
import javax.xml.namespace.QName;

/**
 * @author <a href="mailto:tcunning@redhat.com">Tom Cunningham</a>
 */
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
