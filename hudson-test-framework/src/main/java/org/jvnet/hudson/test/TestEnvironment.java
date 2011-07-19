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

package org.jvnet.hudson.test;

import hudson.model.Computer;
import hudson.model.Hudson;

import java.io.IOException;

/**
 * TODO: deprecate this, and just consolidate this to {@link HudsonTestCase}.
 * We can then pin down the current HudsonTestCase to the thread for easier access.
 *
 * @author Kohsuke Kawaguchi
 */
public class TestEnvironment {
    /**
     * Current test case being run.
     */
    public final HudsonTestCase testCase;

    public final TemporaryDirectoryAllocator temporaryDirectoryAllocator = new TemporaryDirectoryAllocator();

    public TestEnvironment(HudsonTestCase testCase) {
        this.testCase = testCase;
    }

    public void pin() {
        CURRENT = this;
    }

    public void dispose() throws IOException, InterruptedException {
        temporaryDirectoryAllocator.dispose();
        CURRENT = null;
    }

    /**
     * We used to use {@link InheritableThreadLocal} here, but it turns out this is not reliable,
     * especially in the {@link Computer#threadPoolForRemoting}, where threads can inherit
     * the wrong test environment depending on when it's created.
     *
     * <p>
     * Since the rest of Hudson still relies on static {@link Hudson#theInstance}, changing this
     * to a static field for now shouldn't cause any problem. 
     */
    private static TestEnvironment CURRENT;

    public static TestEnvironment get() {
        return CURRENT;
    }
}
