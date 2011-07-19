/*******************************************************************************
 *
 * Copyright (c) 2009, Yahoo!, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *      
 *
 *******************************************************************************/ 

package hudson.tasks.test;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import java.io.IOException;
import java.io.Serializable;


/**
 * A class to exercise the TestResult extension mechanism.
 */
public class TrivialTestResultRecorder extends Recorder implements Serializable {
    @Override
    public boolean perform(AbstractBuild<?, ?> build,
                           Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        System.out.println("performing TrviialTestResultRecorder");
        listener.getLogger().println("perfoming TrivialTestResultRecorder");
        TrivialTestResult r = new TrivialTestResult("gubernatorial");
        TrivialTestResultAction action = new TrivialTestResultAction(build, r);
        r.setParentAction(action);
        build.getActions().add(action);
        listener.getLogger().println("done with TrivialTestResultRecorder");
        System.out.println("done with TrivialTestResultRecorder");
        return true;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
     public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        /**
         * Returns true if this task is applicable to the given project.
         *
         * @return true to allow user to configure this post-promotion task for the given project.
         * @see hudson.model.AbstractProject.AbstractProjectDescriptor#isApplicable(hudson.model.Descriptor)
         */
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        /**
         * Human readable name of this kind of configurable object.
         */
        @Override
        public String getDisplayName() {
            return "trivialtestrecorder"; 
        }
    }

}
