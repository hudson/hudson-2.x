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
 *
 *******************************************************************************/ 

package hudson.model;

import org.kohsuke.stapler.StaplerRequest;
import hudson.cli.CLICommand;

import java.io.IOException;

/**
 * Convenient base class for {@link ParameterDefinition} whose value can be represented in a context-independent single string token.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class SimpleParameterDefinition extends ParameterDefinition {
    protected SimpleParameterDefinition(String name) {
        super(name);
    }

    protected SimpleParameterDefinition(String name, String description) {
        super(name, description);
    }

    /**
     * Creates a {@link ParameterValue} from the string representation.
     */
    public abstract ParameterValue createValue(String value);

    @Override
    public final ParameterValue createValue(StaplerRequest req) {
        String[] value = req.getParameterValues(getName());
        if (value == null) {
            return getDefaultParameterValue();
        } else if (value.length != 1) {
            throw new IllegalArgumentException("Illegal number of parameter values for " + getName() + ": " + value.length);
        } else {
            return createValue(value[0]);
        }
    }

    @Override
    public final ParameterValue createValue(CLICommand command, String value) throws IOException, InterruptedException {
        return createValue(value);
    }
}
