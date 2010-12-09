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

import java.io.Serializable;

import javax.xml.namespace.QName;

import org.switchyard.Exchange;
import org.switchyard.ExchangePattern;
import org.switchyard.Message;

/**
 * @author <a href="mailto:tcunning@redhat.com">Tom Cunningham</a>
 */
public class SerializableExchangeWrapper implements Serializable {
    public static final String EXCHANGE_ID = "org.switchyard.exchange.id";
    private static final long serialVersionUID = -8671429058316279085L;

    private ExchangePattern _exchangePattern;
    private QName _serviceName;
    private SerializableMessageWrapper _messageWrapper;
    private SerializableContextWrapper _context;

    /**
     * Wrapper constructor.
     * @param exchange exchange
     */
    public SerializableExchangeWrapper(Exchange exchange) {
        _context = new SerializableContextWrapper(exchange.getContext());
        _context.setProperty(EXCHANGE_ID, exchange.getId());
        _exchangePattern = exchange.getPattern();
        _serviceName = exchange.getService();
        _messageWrapper = new SerializableMessageWrapper(exchange.getMessage());
    }

    /**
     * Context getter.
     * @return context
     */
    public SerializableContextWrapper getContext() {
        return _context;
    }

    /**
     * Context setter.
     * @param context context
     */
    public void setContext(SerializableContextWrapper context) {
        _context = context;
    }

    /**
     * Exchange ID getter.
     * @return id
     */
    public String getId() {
        return (String) _context.getProperty(EXCHANGE_ID);
    }

    /**
     * Exchange ID setter.
     * @param id id
     */
    public void setId(String id) {
        _context.setProperty(EXCHANGE_ID, id);
    }

    /**
     * Exchange pattern getter.
     * @return exchange pattern
     */
    public ExchangePattern getExchangePattern() {
        return _exchangePattern;
    }

    /**
     * Exchange pattern setter.
     * @param exchangePattern exchange pattern
     */
    public void setExchangePattern(ExchangePattern exchangePattern) {
        _exchangePattern = exchangePattern;
    }

    /**
     * Service name getter.
     * @return service name
     */
    public QName getServiceName() {
        return _serviceName;
    }

    /**
     * Service name setter.
     * @param serviceName service name
     */
    public void setServiceName(QName serviceName) {
        _serviceName = serviceName;
    }

    /**
     * Message getter.
     * @return message
     */
    public Message getMessage() {
        return _messageWrapper.getMessage();
    }

    /**
     * Message setter.
     * @param message message
     */
    public void setMessage(Message message) {
        _messageWrapper = new SerializableMessageWrapper(message);
    }
}
