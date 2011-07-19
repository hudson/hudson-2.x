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

package hudson.scm;

import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.model.Node;
import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.HudsonTestCase;

/**
 * @author Kohsuke Kawaguchi
 */
public class ScmTest extends HudsonTestCase {
    /**
     * Makes sure that {@link SCM#processWorkspaceBeforeDeletion(AbstractProject, FilePath, Node)} is called
     * before a project deletion.
     */
    @Bug(2271)
    public void testProjectDeletionAndCallback() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        final boolean[] callback = new boolean[1];
        p.setScm(new NullSCM() {
            public boolean processWorkspaceBeforeDeletion(AbstractProject<?, ?> project, FilePath workspace, Node node) {
                callback[0] = true;
                return true;
            }

            private Object writeReplace() { // don't really care about save
                return new NullSCM();
            }
        });
        p.scheduleBuild2(0).get();
        p.delete();
        assertTrue(callback[0]);
    }
}
