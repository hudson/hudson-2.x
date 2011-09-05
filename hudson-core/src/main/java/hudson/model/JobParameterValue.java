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
*    Kohsuke Kawaguchi, Tom Huybrechts
 *     
 *
 *******************************************************************************/ 

package hudson.model;

import hudson.EnvVars;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Locale;
import java.util.Map;

public class JobParameterValue extends ParameterValue {
    //TODO: review and check whether we can do it private
    public final Job job;

    @DataBoundConstructor
    public JobParameterValue(String name, Job job) {
        super(name);
        this.job = job;
    }

    public Job getJob() {
        return job;
    }

    /**
     * Exposes the name/value as an environment variable.
     */
    @Override
    public void buildEnvVars(AbstractBuild<?,?> build, EnvVars env) {
        // TODO: check with Tom if this is really what he had in mind
        env.put(name,job.toString());
        env.put(name.toUpperCase(Locale.ENGLISH),job.toString()); // backward compatibility pre 1.345
    }
}
