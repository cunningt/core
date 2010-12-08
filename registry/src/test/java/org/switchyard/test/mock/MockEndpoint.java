package org.switchyard.test.mock;

import org.switchyard.Exchange;
import org.switchyard.HandlerChain;
import org.switchyard.spi.Endpoint;

public class MockEndpoint implements Endpoint {
    
    private HandlerChain _handlerChain;
    
    MockEndpoint(HandlerChain handlerChain) {
        _handlerChain = handlerChain;
    }

    @Override
    public void send(Exchange exchange) {
        _handlerChain.handle(exchange);
    }
    
}
