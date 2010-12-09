package org.switchyard.test.mock;

import javax.xml.namespace.QName;

import org.switchyard.HandlerChain;
import org.switchyard.Service;
import org.switchyard.ServiceDomain;
import org.switchyard.spi.Endpoint;
import org.switchyard.spi.ServiceRegistry;

public class MockService implements Service {
    private ServiceRegistry _registry;
    private ServiceDomain _domain;
    private Endpoint _endpoint;
    private QName _serviceName;
    private HandlerChain _handlers;

    public MockService(QName serviceName,
            Endpoint endpoint,
            HandlerChain handlers,
            ServiceRegistry registry,
            ServiceDomain domain) {

        _serviceName = serviceName;
        _endpoint = endpoint;
        _handlers = handlers;
        _registry = registry;
        _domain = domain;
    }

    public ServiceDomain getDomain() {
        return _domain;
    }

    public void setDomain(ServiceDomain domain) {
        _domain = domain;
    }

    public Endpoint getEndpoint() {
        return _endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this._endpoint = endpoint;
    }

    @Override
    public QName getName() {
        return _serviceName;
    }

    @Override
    public void unregister() {
        _serviceName = null;
        _endpoint = null;
        _handlers = null;
        _registry = null;
        _domain = null;
    }

}
