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
import hudson.Launcher;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;

/**
 * No {@link SCM}.
 *
 * @author Kohsuke Kawaguchi
 */
public class NullSCM extends SCM {
    public SCMRevisionState calcRevisionsFromBuild(AbstractBuild<?, ?> build, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
        return null;
    }

    protected PollingResult compareRemoteRevisionWith(AbstractProject project, Launcher launcher, FilePath workspace, TaskListener listener, SCMRevisionState baseline) throws IOException, InterruptedException {
        return PollingResult.NO_CHANGES;
    }

    public boolean checkout(AbstractBuild<?,?> build, Launcher launcher, FilePath remoteDir, BuildListener listener, File changeLogFile) throws IOException, InterruptedException {
        return createEmptyChangeLog(changeLogFile, listener, "log");
    }

    public ChangeLogParser createChangeLogParser() {
        return new NullChangeLogParser();
    }

    @Extension
    public static class DescriptorImpl extends SCMDescriptor<NullSCM> {
        public DescriptorImpl() {
            super(null);
        }

        public String getDisplayName() {
            return Messages.NullSCM_DisplayName();
        }

        @Override
        public SCM newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new NullSCM();
        }
    }
}
