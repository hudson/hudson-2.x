/*******************************************************************************
 *
 * Copyright (c) 2009, Oracle Corporation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Kohsuke Kawaguchi, Jesse Glick.
 *        
 *
 *******************************************************************************/ 

package hudson;

import hudson.util.ProcessTree;
import hudson.util.StreamTaskListener;
import hudson.remoting.Callable;
import org.apache.commons.io.FileUtils;

import java.io.File;

public class LauncherTest extends ChannelTestCase {
    //@Bug(4611)
    public void testRemoteKill() throws Exception {
        if (File.pathSeparatorChar != ':') {
            System.err.println("Skipping, currently Unix-specific test");
            return;
        }

        File tmp = File.createTempFile("testRemoteKill", "");
        tmp.delete();

        try {
            FilePath f = new FilePath(french, tmp.getPath());
            Launcher l = f.createLauncher(StreamTaskListener.fromStderr());
            Proc p = l.launch().cmds("sh", "-c", "echo $$$$ > "+tmp+"; sleep 30").stdout(System.out).stderr(System.err).start();
            while (!tmp.exists())
                Thread.sleep(100);
            long start = System.currentTimeMillis();
            p.kill();
            assertTrue(p.join()!=0);
            long end = System.currentTimeMillis();
            assertTrue("join finished promptly", (end - start < 5000));
            french.call(NOOP); // this only returns after the other side of the channel has finished executing cancellation
            Thread.sleep(2000); // more delay to make sure it's gone
            assertNull("process should be gone",ProcessTree.get().get(Integer.parseInt(FileUtils.readFileToString(tmp).trim())));

            // Manual version of test: set up instance w/ one slave. Now in script console
            // new hudson.FilePath(new java.io.File("/tmp")).createLauncher(new hudson.util.StreamTaskListener(System.err)).
            //   launch().cmds("sleep", "1d").stdout(System.out).stderr(System.err).start().kill()
            // returns immediately and pgrep sleep => nothing. But without fix
            // hudson.model.Hudson.instance.nodes[0].rootPath.createLauncher(new hudson.util.StreamTaskListener(System.err)).
            //   launch().cmds("sleep", "1d").stdout(System.out).stderr(System.err).start().kill()
            // hangs and on slave machine pgrep sleep => one process; after manual kill, script returns.
        } finally {
            tmp.delete();
        }
    }

    private static final Callable<Object,RuntimeException> NOOP = new Callable() {
        public Object call() throws Exception {
            return null;
        }
    };
}
