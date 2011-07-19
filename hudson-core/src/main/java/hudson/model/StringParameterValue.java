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
 * {@link ParameterValue} created from {@link StringParameterDefinition}.
 */
public class StringParameterValue extends ParameterValue {
    @Exported(visibility=4)
    public final String value;

    @DataBoundConstructor
    public StringParameterValue(String name, String value) {
        this(name, value, null);
    }

    public StringParameterValue(String name, String value, String description) {
        super(name, description);
        this.value = value;
    }

    /**
     * Exposes the name/value as an environment variable.
     */
    @Override
    public void buildEnvVars(AbstractBuild<?,?> build, EnvVars env) {
        env.put(name,value);
        env.put(name.toUpperCase(Locale.ENGLISH),value); // backward compatibility pre 1.345
    }

    @Override
    public VariableResolver<String> createVariableResolver(AbstractBuild<?, ?> build) {
        return new VariableResolver<String>() {
            public String resolve(String name) {
                return StringParameterValue.this.name.equals(name) ? value : null;
            }
        };
    }
    

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		StringParameterValue other = (StringParameterValue) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

    @Override
    public String toString() {
    	return "(StringParameterValue) " + getName() + "='" + value + "'";
    }
}
