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
    static class  Tag implements Serializable {
        ProcessTree tree;
        OSProcess p;
        int id;
        private static final long serialVersionUID = 1L;
    }
    
    public void testRemoting() throws Exception {
        
        // TODO - Move this test to JNA Native Support plugin 
        
//        Tag t = french.call(new MyCallable());
//
//        // make sure the serialization preserved the reference graph
//        assertSame(t.p.getTree(), t.tree);
//
//        // verify that some remote call works
//        t.p.getEnvironmentVariables();
//
//        // it should point to the same object
//        assertEquals(t.id,t.p.getPid());
//
//        t.p.act(new ProcessCallableImpl());
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
