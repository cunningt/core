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

import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.TestCase;

import org.switchyard.BaseHandler;
import org.switchyard.Exchange;
import org.switchyard.HandlerException;
import org.switchyard.MessageBuilder;
import org.switchyard.message.FaultMessage;

/**
 * Standing Handler.
 * 
 * @author <a href="mailto:tcunning@redhat.com">Tom Cunningham</a>
 */
public class StandingHandler extends BaseHandler {

    public LinkedBlockingQueue<Exchange> _messages = 
    	new LinkedBlockingQueue<Exchange>();
    
    public LinkedBlockingQueue<Exchange> _faults = 
    	new LinkedBlockingQueue<Exchange>();
    
    public long waitTimeout = 5000; // default of 5 seconds

    // An enum would be nicer here !!
    private boolean forwardInToOut = false;
    private boolean forwardInToFault = false;

    public StandingHandler() {
    }

    public StandingHandler forwardInToOut() {
        // An enum would be nicer here !!
        forwardInToOut = true;
        forwardInToFault = false;
        return this;
    }

    public StandingHandler forwardInToFault() {
        // An enum would be nicer here !!
        forwardInToOut = false;
        forwardInToFault = true;
        return this;
    }
    
    @Override
    public void handleMessage(Exchange exchange) throws HandlerException {
    	_messages.offer(exchange);
    	if (forwardInToOut) {
    	    System.out.println(StandingHandler.class + " forwardInToOut");
    	    exchange.send(exchange.getMessage());
    	} else if (forwardInToFault) {
    	    System.out.println(StandingHandler.class + " forwardInToFault");
    	    exchange.send(MessageBuilder.newInstance(FaultMessage.class).buildMessage());
    	}
    }

    @Override
    public void handleFault(Exchange exchange) {
    	_faults.offer(exchange);
    }

    public StandingHandler waitForMessage() {
        waitFor(_messages, 1);
        return this;
    }
    
    public StandingHandler waitForMessage(int numMessages) {
        waitFor(_faults, numMessages);
        return this;
    }

    private void waitFor(LinkedBlockingQueue<Exchange> eventQueue, int numMessages) {
        long start = System.currentTimeMillis();

        while(System.currentTimeMillis() < start + waitTimeout) {
            if(eventQueue.size() >= numMessages) {
                return;
            }
            sleep();
        }

        TestCase.fail("Timed out waiting on event queue length to be " + numMessages + " or greater.");
    }

    private void sleep() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            TestCase.fail("Failed to sleep: " + e.getMessage());
        }
    }
}