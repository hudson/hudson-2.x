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

package hudson.triggers;

import hudson.FilePath;
import hudson.Launcher;
import hudson.util.OneShotEvent;
import hudson.util.StreamTaskListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.model.TaskListener;
import hudson.scm.NullSCM;
import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.HudsonTestCase;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;

/**
 * @author Alan Harder
 */
public class SCMTriggerTest extends HudsonTestCase {
    /**
     * Make sure that SCMTrigger doesn't trigger another build when a build has just started,
     * but not yet completed its SCM update.
     */
    @Bug(2671)
    public void testSimultaneousPollAndBuild() throws Exception {
        FreeStyleProject p = createFreeStyleProject();

        // used to coordinate polling and check out
        final OneShotEvent checkoutStarted = new OneShotEvent();

        p.setScm(new TestSCM(checkoutStarted));

        Future<FreeStyleBuild> build = p.scheduleBuild2(0, new Cause.UserCause());
        checkoutStarted.block();
        assertFalse("SCM-poll after build has started should wait until that build finishes SCM-update", p.pollSCMChanges(StreamTaskListener.fromStdout()));
        build.get();  // let mock build finish
    }

    private static class TestSCM extends NullSCM {
        private volatile int myRev = 1;
        private final OneShotEvent checkoutStarted;

        public TestSCM(OneShotEvent checkoutStarted) {
            this.checkoutStarted = checkoutStarted;
        }

        @Override synchronized
        public boolean pollChanges(AbstractProject project, Launcher launcher, FilePath dir, TaskListener listener) throws IOException {
            return myRev < 2;
        }

        @Override
        public boolean checkout(AbstractBuild<?,?> build, Launcher launcher, FilePath remoteDir, BuildListener listener, File changeLogFile) throws IOException, InterruptedException {
            checkoutStarted.signal();
            Thread.sleep(400);  // processing time for mock update
            synchronized (this) { if (myRev < 2) myRev = 2; }
            return super.checkout(build, launcher, remoteDir, listener, changeLogFile);
        }
    }
}
