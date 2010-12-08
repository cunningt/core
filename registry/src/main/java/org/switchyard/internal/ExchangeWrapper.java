package org.switchyard.internal;

import java.io.Serializable;

import javax.xml.namespace.QName;

import org.switchyard.Context;
import org.switchyard.Exchange;
import org.switchyard.ExchangePattern;
import org.switchyard.Message;
import org.switchyard.ServiceDomain;

public class ExchangeWrapper implements Serializable {
    private static final long serialVersionUID = -8671429058316279085L;

    private String _id;
    //private Context _context;
    private ExchangePattern _exchangePattern;
    private QName _serviceName;
    private Message _message;
 
    public ExchangeWrapper(Exchange exchange) {
	_id = exchange.getId();
	//_context = exchange.getContext();
	_serviceName = exchange.getService();
	_message = exchange.getMessage();
	_exchangePattern = exchange.getPattern();	
    }
 
    /*
    public Context getContext() {
        return _context;
    }
    
    public void setContext(Context context) {
        _context = context;
    }
    */
    
    public String getId() {
        return _id;
    }
    
    public void setId(String id) {
        _id = id;
    }
    
    public ExchangePattern getExchangePattern() {
        return _exchangePattern;
    }
    
    public void setExchangePattern(ExchangePattern exchangePattern) {
        _exchangePattern = exchangePattern;
    }
    
    public QName getServiceName() {
        return _serviceName;
    }
    
    public void setServiceName(QName serviceName) {
        _serviceName = serviceName;
    }
    
    public Message getMessage() {
        return _message;
    }
    
    public void setMessage(Message message) {
        _message = message;
    }
}