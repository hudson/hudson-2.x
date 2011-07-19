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
*    Kohsuke Kawaguchi, Luca Domenico Milanesio, Tom Huybrechts
 *     
 *
 *******************************************************************************/ 

package hudson.model;

import hudson.EnvVars;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

import java.util.Locale;
import java.util.Map;

import hudson.util.VariableResolver;

/**
 * {@link ParameterValue} created from {@link BooleanParameterDefinition}.
 */
public class BooleanParameterValue extends ParameterValue {
    @Exported(visibility=4)
    public final boolean value;

    @DataBoundConstructor
    public BooleanParameterValue(String name, boolean value) {
        this(name, value, null);
    }

    public BooleanParameterValue(String name, boolean value, String description) {
        super(name, description);
        this.value = value;
    }

    /**
     * Exposes the name/value as an environment variable.
     */
    @Override
    public void buildEnvVars(AbstractBuild<?,?> build, EnvVars env) {
        env.put(name,Boolean.toString(value));
        env.put(name.toUpperCase(Locale.ENGLISH),Boolean.toString(value)); // backward compatibility pre 1.345
    }

    @Override
    public VariableResolver<String> createVariableResolver(AbstractBuild<?, ?> build) {
        return new VariableResolver<String>() {
            public String resolve(String name) {
                return BooleanParameterValue.this.name.equals(name) ? Boolean.toString(value) : null;
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BooleanParameterValue that = (BooleanParameterValue) o;

        if (value != that.value) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (value ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
    	return "(BooleanParameterValue) " + getName() + "='" + value + "'";
    }
}
