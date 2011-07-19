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

package org.eclipse.hudson.legacy.maven.plugin;

import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Notifier;
import hudson.tasks.BuildStepMonitor;
import hudson.Launcher;
import hudson.tasks.Publisher;

import java.io.IOException;

import org.eclipse.hudson.legacy.maven.plugin.reporters.MavenArtifactRecord;

/**
 * {@link Publisher} for Maven projects to deploy artifacts to a Maven repository
 * after the fact.
 *
 * <p>
 * When a build breaks in the middle, this is a convenient way to prevent
 * modules from being deployed partially. This can be combined with promoted builds
 * plugin to deploy artifacts after testing, for example. 
 *
 * @author Kohsuke Kawaguchi
 */
public class MavenRedeployer extends Notifier {
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        MavenArtifactRecord mar = build.getAction(MavenArtifactRecord.class);
        if(mar==null) {
            if(build.getResult().isBetterThan(Result.FAILURE)) {
                listener.getLogger().println("There's no record of artifact information. Is this really a Maven build?");
                build.setResult(Result.FAILURE);
            }
            // failed
            return true;
        }

        listener.getLogger().println("TODO");
        
        return true;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    public BuildStepDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends BuildStepDescriptor {
        public boolean isApplicable(Class jobType) {
            return AbstractMavenProject.class.isAssignableFrom(jobType);
        }

        public String getDisplayName() {
            return Messages.MavenRedeployer_DisplayName();
        }
    }
}
