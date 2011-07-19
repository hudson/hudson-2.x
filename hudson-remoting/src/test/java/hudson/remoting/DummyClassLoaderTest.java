/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson.remoting;

import junit.framework.TestCase;

/**
 * @author Kohsuke Kawaguchi
 */
public class DummyClassLoaderTest extends TestCase {
    public void testLoad() throws Throwable {
        DummyClassLoader cl = new DummyClassLoader(this.getClass().getClassLoader());
        Callable c = (Callable) cl.loadClass("hudson.remoting.test.TestCallable").newInstance();
        System.out.println(c.call());
        // make sure that the returned class is loaded from the dummy classloader
        assertTrue(((Object[])c.call())[0].toString().startsWith(DummyClassLoader.class.getName()));
    }
}
