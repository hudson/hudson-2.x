/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Kohsuke Kawaguchi, Romain Seguy, Yahoo! Inc.
 *     
 *
 *******************************************************************************/ 

package hudson.model;

import hudson.EnvVars;
import hudson.util.Secret;
import hudson.util.VariableResolver;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Locale;

/**
 * @author Kohsuke Kawaguchi
 */
public class PasswordParameterValue extends ParameterValue {

    private final Secret value;

    // kept for backward compatibility
    public PasswordParameterValue(String name, String value) {
        this(name, value, null);
    }

    @DataBoundConstructor
    public PasswordParameterValue(String name, String value, String description) {
        super(name, description);
        this.value = Secret.fromString(value);
    }

    @Override
    public void buildEnvVars(AbstractBuild<?,?> build, EnvVars env) {
        String v = Secret.toString(value);
        env.put(name, v);
        env.put(name.toUpperCase(Locale.ENGLISH),v); // backward compatibility pre 1.345
    }

    @Override
    public VariableResolver<String> createVariableResolver(AbstractBuild<?, ?> build) {
        return new VariableResolver<String>() {
            public String resolve(String name) {
                return PasswordParameterValue.this.name.equals(name) ? Secret.toString(value) : null;
            }
        };
    }

    @Override
    public boolean isSensitive() {
        return true;
}
}
