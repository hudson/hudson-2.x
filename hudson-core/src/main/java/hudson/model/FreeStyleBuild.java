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

import hudson.slaves.WorkspaceList;
import hudson.slaves.WorkspaceList.Lease;

import java.io.IOException;
import java.io.File;

/**
 * @author Kohsuke Kawaguchi
 */
public class FreeStyleBuild extends Build<FreeStyleProject,FreeStyleBuild> {
    public FreeStyleBuild(FreeStyleProject project) throws IOException {
        super(project);
    }

    public FreeStyleBuild(FreeStyleProject project, File buildDir) throws IOException {
        super(project, buildDir);
    }

    @Override
    public void run() {
        run(new RunnerImpl());
    }

    protected class RunnerImpl extends Build<FreeStyleProject,FreeStyleBuild>.RunnerImpl {
        @Override
        protected Lease decideWorkspace(Node n, WorkspaceList wsl) throws IOException, InterruptedException {
            String customWorkspace = getProject().getCustomWorkspace();
            if (customWorkspace != null)
                // we allow custom workspaces to be concurrently used between jobs.
                return Lease.createDummyLease(n.getRootPath().child(getEnvironment(listener).expand(customWorkspace)));
            return super.decideWorkspace(n,wsl);
        }
    }
}
