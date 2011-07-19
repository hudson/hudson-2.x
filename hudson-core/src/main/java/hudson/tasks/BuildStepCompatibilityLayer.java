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

package hudson.tasks;

import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Action;
import hudson.model.Project;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.CheckPoint;
import hudson.Launcher;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * Provides compatibility with {@link BuildStep} before 1.150
 * so that old plugin binaries can continue to function with new Hudson.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.150
 * @deprecated since 1.150
 */
public abstract class BuildStepCompatibilityLayer implements BuildStep {
//
// new definitions >= 1.150
//
    public boolean prebuild(AbstractBuild<?,?> build, BuildListener listener) {
        if (build instanceof Build)
            return prebuild((Build)build,listener);
        else
            return true;
    }

    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        if (build instanceof Build)
            return perform((Build)build,launcher,listener);
        else
            return true;
    }

    public Action getProjectAction(AbstractProject<?, ?> project) {
        if (project instanceof Project)
            return getProjectAction((Project) project);
        else
            return null;
    }

    public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
        // delegate to getJobAction (singular) for backward compatible behavior
        Action a = getProjectAction(project);
        if (a==null)    return Collections.emptyList();
        return Collections.singletonList(a);
    }


//
// old definitions < 1.150
//
    /**
     * @deprecated
     *      Use {@link #prebuild(AbstractBuild, BuildListener)} instead.
     */
    public boolean prebuild(Build<?,?> build, BuildListener listener) {
        return true;
    }

    /**
     * @deprecated
     *      Use {@link #perform(AbstractBuild, Launcher, BuildListener)} instead.
     */
    public boolean perform(Build<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated
     *      Use {@link #getProjectAction(AbstractProject)} instead.
     */
    public Action getProjectAction(Project<?,?> project) {
        return null;
    }
}
