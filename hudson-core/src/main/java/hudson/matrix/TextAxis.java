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

package hudson.matrix;

import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;

/**
 * User-defined plain text axis.
 * 
 * @author Kohsuke Kawaguchi
 */
public class TextAxis extends Axis {
    public TextAxis(String name, List<String> values) {
        super(name, values);
    }

    public TextAxis(String name, String... values) {
        super(name, values);
    }

    @DataBoundConstructor
    public TextAxis(String name, String valueString) {
        super(name, valueString);
    }

    @Extension
    public static class DescriptorImpl extends AxisDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.TextArea_DisplayName();
        }
    }
}
