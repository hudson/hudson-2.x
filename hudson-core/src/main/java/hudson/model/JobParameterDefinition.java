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

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class JobParameterDefinition extends SimpleParameterDefinition {

    @DataBoundConstructor
    public JobParameterDefinition(String name) {
        super(name);
    }

    // @Extension --- not live yet
    public static class DescriptorImpl extends ParameterDescriptor {
        @Override
        public String getDisplayName() {
            return "Project Parameter";
        }

        @Override
        public ParameterDefinition newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return req.bindJSON(JobParameterDefinition.class, formData);
        }

    }

    @Override
    public ParameterValue createValue(StaplerRequest req, JSONObject jo) {
        return req.bindJSON(JobParameterValue.class, jo);
    }

    public ParameterValue createValue(String value) {
        return new JobParameterValue(getName(),Hudson.getInstance().getItemByFullName(value,Job.class));
    }
}
