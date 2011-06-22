/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     
 *
 *******************************************************************************/ 

package hudson.util;

import hudson.ChannelTestCase;
import hudson.Functions;
import hudson.remoting.Callable;
import hudson.remoting.VirtualChannel;
import hudson.util.ProcessTree.OSProcess;
import hudson.util.ProcessTree.ProcessCallable;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author Kohsuke Kawaguchi
 */
public class ProcessTreeTest extends ChannelTestCase {
    static class Tag implements Serializable {
        ProcessTree tree;
        OSProcess p;
        int id;
        private static final long serialVersionUID = 1L;
    }

    public void testRemoting() throws Exception {
        // disabled under Win because of errors like:
        // org.jvnet.winp.WinpException: Failed to open process error=5 at .\envvar-cmdline.cpp:53
        // org.jvnet.winp.WinpException: Failed to read environment variable table error=299 at .\envvar-cmdline.cpp:114
        // It seems it's impossible to call getEnvironmentVariables on "privileged" processes which can vary on different Win versions.
        // we can use something like
        // if (pid == 0 || pid == 4 || pid == 1100 || pid == 5980 || pid == 5496 || pid == 1500) continue;
        // to exclude these pids, but it's not excellent solution
        if (Functions.isWindows())     return;

        Tag t = french.call(new MyCallable());

        // make sure the serialization preserved the reference graph
        assertSame(t.p.getTree(), t.tree);

        // verify that some remote call works
        t.p.getEnvironmentVariables();

        // it should point to the same object
        assertEquals(t.id,t.p.getPid());

        t.p.act(new ProcessCallableImpl());
    }

    private static class MyCallable implements Callable<Tag, IOException>, Serializable {
        public Tag call() throws IOException {
            Tag t = new Tag();
            t.tree = ProcessTree.get();
            if (t.tree.iterator().hasNext()) {
                t.p = t.tree.iterator().next();
                t.id = t.p.getPid();
            }
            return t;
        }

        private static final long serialVersionUID = 1L;
    }

    private static class ProcessCallableImpl implements ProcessCallable<Void> {
        public Void invoke(OSProcess process, VirtualChannel channel) throws IOException {
            assertNotNull(process);
            assertNotNull(channel);
            return null;
        }
    }
}
