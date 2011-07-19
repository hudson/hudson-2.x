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

package org.jvnet.hudson.test.junit;

import junit.framework.TestCase;

/**
 * {@link TestCase} implementation that has already failed.
 * Used to represent a problem happened during a test suite construction.
 *
 * @author Kohsuke Kawaguchi
 */
public class FailedTest extends TestCase {
    /**
     * The failure. If null, the test will succeed, despite the class name.
     */
    private final Throwable problem;

    public FailedTest(String name, Throwable problem) {
        super(name);
        this.problem = problem;
    }

    public FailedTest(Class name, Throwable problem) {
        this(name.getName(),problem);
    }

    @Override
    protected void runTest() throws Throwable {
        if (problem!=null)
            throw problem;
    }
}
