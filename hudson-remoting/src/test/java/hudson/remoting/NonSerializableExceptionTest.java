/*******************************************************************************
 *
 * Copyright (c) 2004-2009, Oracle Corporation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *        
 *
 *******************************************************************************/ 

package hudson.remoting;

import java.net.SocketException;

/**
 * @author Kohsuke Kawaguchi
 */
public class NonSerializableExceptionTest extends RmiTestBase {
    /**
     * Makes sure non-serializable exceptions are gracefully handled.
     *
     * HUDSON-1041.
     */
    public void test1() throws Throwable {
        try {
            channel.call(new Failure());
        } catch (ProxyException p) {
            // verify that we got the right kind of exception
            assertTrue(p.getMessage().contains("NoneSerializableException"));
            assertTrue(p.getMessage().contains("message1"));
            ProxyException nested = p.getCause();
            assertTrue(nested.getMessage().contains("SocketException"));
            assertTrue(nested.getMessage().contains("message2"));
            assertNull(nested.getCause());
        }
    }

    private static final class NoneSerializableException extends Exception {
        private final Object o = new Object(); // this is not serializable

        private NoneSerializableException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    private static final class Failure implements Callable {
        public Object call() throws Throwable {
            throw new NoneSerializableException("message1",new SocketException("message2"));
        }
    }
}
