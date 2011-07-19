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
*    Kohsuke Kawaguchi, Martin Eigenbrodt
 *     
 *
 *******************************************************************************/ 

package hudson.views;

import hudson.Extension;
import hudson.model.StatusIcon;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Shows the status icon for item. It's colored ball for jobs.
 *
 * @see StatusIcon
 */
public class StatusColumn extends ListViewColumn {
    @DataBoundConstructor
    public StatusColumn() {
    }

    @Extension
    public static class DescriptorImpl extends ListViewColumnDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.StatusColumn_DisplayName();
        }
    }

}
