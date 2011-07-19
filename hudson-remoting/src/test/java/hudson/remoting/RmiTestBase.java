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
*    Kohsuke Kawaguchi, InfraDNA, Inc.
 *     
 *
 *******************************************************************************/ 

package hudson.remoting;

import hudson.remoting.ChannelRunner.InProcess;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Base class for remoting tests.
 * 
 * @author Kohsuke Kawaguchi
 */
@WithRunner({
    ChannelRunner.InProcess.class,
    ChannelRunner.InProcessCompatibilityMode.class,
    ChannelRunner.Fork.class
})
public abstract class RmiTestBase extends TestCase {

    protected Channel channel;
    protected ChannelRunner channelRunner = new InProcess();

    protected void setUp() throws Exception {
        System.out.println("Starting "+getName());
        channel = channelRunner.start();
    }

    protected void tearDown() throws Exception {
        channelRunner.stop(channel);
    }

    /*package*/ void setChannelRunner(Class<? extends ChannelRunner> runner) {
        try {
            this.channelRunner = runner.newInstance();
        } catch (InstantiationException e) {
            throw new Error(e);
        } catch (IllegalAccessException e) {
            throw new Error(e);
        }
    }

    public String getName() {
        return super.getName()+"-"+channelRunner.getName();
    }

    /**
     * Can be used in the suite method of the derived class to build a
     * {@link TestSuite} to run the test with all the available
     * {@link ChannelRunner} configuration.
     */
    protected static Test buildSuite(Class<? extends RmiTestBase> testClass) {
        TestSuite suite = new TestSuite();

        WithRunner wr = testClass.getAnnotation(WithRunner.class);

        for( Class<? extends ChannelRunner> r : wr.value() ) {
            suite.addTest(new ChannelTestSuite(testClass,r));
        }
        return suite;
    }
}
