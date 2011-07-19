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

import junit.framework.TestSuite;
import junit.framework.TestResult;

/**
 * {@link TestSuite} that requires some set up and tear down for executing nested tests.
 *
 * <p>
 * The failure in the set up or tear down will be reported as a failure.
 *
 * @author Kohsuke Kawaguchi
 */
public class GroupedTest extends TestSuite {
    @Override
    public int countTestCases() {
        return super.countTestCases()+1;
    }

    @Override
    public void run(TestResult result) {
        try {
            setUp();
            try {
                runGroupedTests(result);
            } finally {
                tearDown();
            }
            // everything went smoothly. report a successful test to make the ends meet
            runTest(new FailedTest(getClass(),null),result);
        } catch (Throwable e) {
            // something went wrong
            runTest(new FailedTest(getClass(),e),result);
        }
    }

    /**
     * Executes the nested tests.
     */
    protected void runGroupedTests(TestResult result) throws Exception {
        super.run(result);
    }

    protected void setUp() throws Exception {
    }
    protected void tearDown() throws Exception {
    }
}
