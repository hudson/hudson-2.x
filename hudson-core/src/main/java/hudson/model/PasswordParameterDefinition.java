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
*    Kohsuke Kawaguchi, Romain Seguy
 *     
 *
 *******************************************************************************/ 

package hudson.model;

import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.DataBoundConstructor;
import hudson.Extension;
import hudson.util.Secret;

/**
 * Parameter whose value is a {@link Secret} and is hidden from the UI.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.319
 */
public class PasswordParameterDefinition extends SimpleParameterDefinition {

    private Secret defaultValue;

    @DataBoundConstructor
    public PasswordParameterDefinition(String name, String defaultValue, String description) {
        super(name, description);
        this.defaultValue = Secret.fromString(defaultValue);
    }

    @Override
    public ParameterValue createValue(String value) {
        return new PasswordParameterValue(getName(), value, getDescription());
    }

    @Override
    public PasswordParameterValue createValue(StaplerRequest req, JSONObject jo) {
        PasswordParameterValue value = req.bindJSON(PasswordParameterValue.class, jo);
        value.setDescription(getDescription());
        return value;
    }

    @Override
    public ParameterValue getDefaultParameterValue() {
        return new PasswordParameterValue(getName(), getDefaultValue(), getDescription());
    }

    public String getDefaultValue() {
        return Secret.toString(defaultValue);
    }

    // kept for backward compatibility
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = Secret.fromString(defaultValue);
    }

    @Extension
    public final static class ParameterDescriptorImpl extends ParameterDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.PasswordParameterDefinition_DisplayName();
        }
        
        @Override
        public String getHelpFile() {
            return "/help/parameter/string.html";
        }
    }
}
