/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc.
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

package hudson.matrix;

import hudson.Extension;
import hudson.model.Hudson;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * {@link Axis} that selects available JDKs.
 *
 * @author Kohsuke Kawaguchi
 */
public class JDKAxis extends Axis {
    /**
     * JDK axis was used to be stored as a plain "Axis" with the name "jdk",
     * so it cannot be configured by any other name.
     */
    public JDKAxis(List<String> values) {
        super("jdk", values);
    }

    @DataBoundConstructor
    public JDKAxis(String[] values) {
        super("jdk", Arrays.asList(values));
    }

    @Override
    public boolean isSystem() {
        return true;
    }

    @Extension
    public static class DescriptorImpl extends AxisDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.JDKAxis_DisplayName();
        }

        /**
         * If there's no JDK configured, there's no point in this axis.
         */
        @Override
        public boolean isInstantiable() {
            return !Hudson.getInstance().getJDKs().isEmpty();
        }
    }
}
