/*******************************************************************************
 *
 * Copyright (c) 2009, Oracle Corporation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   Jesse Glick
 *      
 *
 *******************************************************************************/ 

package hudson.views;

import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

public class LastStableColumn extends ListViewColumn {
    @DataBoundConstructor
    public LastStableColumn() {
    }

    @Extension
    public static class DescriptorImpl extends ListViewColumnDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.LastStableColumn_DisplayName();
        }

        @Override
        public boolean shownByDefault() {
            return false;
        }
    }
}
