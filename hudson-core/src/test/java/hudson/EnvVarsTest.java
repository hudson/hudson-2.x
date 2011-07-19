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

package hudson;

import junit.framework.TestCase;

import java.util.Collections;

/**
 * @author Kohsuke Kawaguchi
 */
public class EnvVarsTest extends TestCase {
    /**
     * Makes sure that {@link EnvVars} behave in case-insensitive way.
     */
    public void test1() {
        EnvVars ev = new EnvVars(Collections.singletonMap("Path","A:B:C"));
        assertTrue(ev.containsKey("PATH"));
        assertEquals("A:B:C",ev.get("PATH"));
    }
}
