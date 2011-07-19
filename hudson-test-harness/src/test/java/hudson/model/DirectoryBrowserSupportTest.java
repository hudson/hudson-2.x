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

package hudson.model;

import hudson.Functions;
import hudson.tasks.Shell;
import hudson.tasks.BatchFile;
import hudson.Launcher;
import org.jvnet.hudson.test.Email;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.TestBuilder;

import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class DirectoryBrowserSupportTest extends HudsonTestCase {
    /**
     * Double dots that appear in file name is OK.
     */
    @Email("http://www.nabble.com/Status-Code-400-viewing-or-downloading-artifact-whose-filename-contains-two-consecutive-periods-tt21407604.html")
    public void testDoubleDots() throws Exception {
        // create a problematic file name in the workspace
        FreeStyleProject p = createFreeStyleProject();
        if(Functions.isWindows())
            p.getBuildersList().add(new BatchFile("echo > abc..def"));
        else
            p.getBuildersList().add(new Shell("touch abc..def"));
        p.scheduleBuild2(0).get();

        // can we see it?
        new WebClient().goTo("job/"+p.getName()+"/ws/abc..def","application/octet-stream");

        // TODO: implement negative check to make sure we aren't serving unexpected directories.
        // the following trivial attempt failed. Someone in between is normalizing.
//        // but this should fail
//        try {
//            new WebClient().goTo("job/" + p.getName() + "/ws/abc/../", "application/octet-stream");
//        } catch (FailingHttpStatusCodeException e) {
//            assertEquals(400,e.getStatusCode());
//        }
    }

    /**
     * Also makes sure '\\' in the file name for Unix is handled correctly.
     */
    @Email("http://www.nabble.com/Status-Code-400-viewing-or-downloading-artifact-whose-filename-contains-two-consecutive-periods-tt21407604.html")
    public void testDoubleDots2() throws Exception {
        if(Functions.isWindows())  return; // can't test this on Windows

        // create a problematic file name in the workspace
        FreeStyleProject p = createFreeStyleProject();
        p.getBuildersList().add(new Shell("touch abc\\\\def.bin"));
        p.scheduleBuild2(0).get();

        // can we see it?
        new WebClient().goTo("job/"+p.getName()+"/ws/abc%5Cdef.bin","application/octet-stream");
    }

    public void testNonAsciiChar() throws Exception {
        // create a problematic file name in the workspace
        FreeStyleProject p = createFreeStyleProject();
        p.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
                build.getWorkspace().child("\u6F22\u5B57.bin").touch(0); // Kanji
                return true;
            }
        }); // Kanji
        p.scheduleBuild2(0).get();

        // can we see it?
        new WebClient().goTo("job/"+p.getName()+"/ws/%e6%bc%a2%e5%ad%97.bin","application/octet-stream");
    }
}
