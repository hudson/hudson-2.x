/*******************************************************************************
 *
 * Copyright (c) 2004-2009, Oracle Corporation
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

package org.jvnet.hudson.test;

import hudson.Launcher;
import hudson.Extension;
import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.tasks.Builder;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;

/**
 * {@link Builder} that captures the environment variables used during a build.
 *
 * @author Kohsuke Kawaguchi
 */
public class CaptureEnvironmentBuilder extends Builder {
	
    private EnvVars envVars;

	public EnvVars getEnvVars() {
		return envVars;
	}

	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
    	envVars = build.getEnvironment(listener);
        return true;
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<Builder> {
        public Builder newInstance(StaplerRequest req, JSONObject data) {
            throw new UnsupportedOperationException();
        }

        public String getDisplayName() {
            return "Capture Environment Variables";
        }
    }
}
