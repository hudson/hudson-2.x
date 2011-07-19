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
*    Kohsuke Kawaguchi, Luca Domenico Milanesio, Seiji Sogabe, Tom Huybrechts
 *     
 *
 *******************************************************************************/ 

package hudson.model;

import hudson.Extension;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Parameter whose value is a string value.
 */
public class StringParameterDefinition extends SimpleParameterDefinition {

    private String defaultValue;

    @DataBoundConstructor
    public StringParameterDefinition(String name, String defaultValue, String description) {
        super(name, description);
        this.defaultValue = defaultValue;
    }

    public StringParameterDefinition(String name, String defaultValue) {
        this(name, defaultValue, null);
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    @Override
    public StringParameterValue getDefaultParameterValue() {
        StringParameterValue v = new StringParameterValue(getName(), defaultValue, getDescription());
        return v;
    }

    @Extension
    public static class DescriptorImpl extends ParameterDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.StringParameterDefinition_DisplayName();
        }

        @Override
        public String getHelpFile() {
            return "/help/parameter/string.html";
        }
    }

    @Override
    public ParameterValue createValue(StaplerRequest req, JSONObject jo) {
        StringParameterValue value = req.bindJSON(StringParameterValue.class, jo);
        value.setDescription(getDescription());
        return value;
    }

    public ParameterValue createValue(String value) {
        return new StringParameterValue(getName(), value, getDescription());
    }
}
