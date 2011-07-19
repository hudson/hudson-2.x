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

package hudson.model;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.DataBoundConstructor;
import net.sf.json.JSONObject;
import hudson.Extension;

/**
 * {@link ParameterDefinition} that is either 'true' or 'false'.
 *
 * @author huybrechts
 */
public class BooleanParameterDefinition extends SimpleParameterDefinition {
    private final boolean defaultValue;

    @DataBoundConstructor
    public BooleanParameterDefinition(String name, boolean defaultValue, String description) {
        super(name, description);
        this.defaultValue = defaultValue;
    }

    public boolean isDefaultValue() {
        return defaultValue;
    }

    @Override
    public ParameterValue createValue(StaplerRequest req, JSONObject jo) {
        BooleanParameterValue value = req.bindJSON(BooleanParameterValue.class, jo);
        value.setDescription(getDescription());
        return value;
    }

    public ParameterValue createValue(String value) {
        return new BooleanParameterValue(getName(),Boolean.valueOf(value),getDescription());
    }

    @Override
    public BooleanParameterValue getDefaultParameterValue() {
        return new BooleanParameterValue(getName(), defaultValue, getDescription());
    }

    @Extension
    public static class DescriptorImpl extends ParameterDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.BooleanParameterDefinition_DisplayName();
        }

        @Override
        public String getHelpFile() {
            return "/help/parameter/boolean.html";
        }
    }

}
