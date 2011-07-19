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

import java.util.Locale;

import hudson.EnvVars;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

public class RunParameterValue extends ParameterValue {

    private final String runId;

    @DataBoundConstructor
    public RunParameterValue(String name, String runId, String description) {
        super(name, description);
        this.runId = runId;
    }

    public RunParameterValue(String name, String runId) {
        super(name, null);
        this.runId = runId;
    }

    public Run getRun() {
        return Run.fromExternalizableId(runId);
    }

    public String getRunId() {
        return runId;
    }
    
    @Exported
    public String getJobName() {
    	return runId.split("#")[0];
    }
    
    @Exported
    public String getNumber() {
    	return runId.split("#")[1];
    }
    

    /**
     * Exposes the name/value as an environment variable.
     */
    @Override
    public void buildEnvVars(AbstractBuild<?,?> build, EnvVars env) {
        String value = Hudson.getInstance().getRootUrl() + getRun().getUrl();
        env.put(name, value);
        env.put(name.toUpperCase(Locale.ENGLISH),value); // backward compatibility pre 1.345

    }
    
    @Override
    public String getShortDescription() {
    	return "(RunParameterValue) " + getName() + "='" + getRunId() + "'";
    }

}
