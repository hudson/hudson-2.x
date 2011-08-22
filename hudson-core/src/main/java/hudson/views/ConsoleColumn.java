/*******************************************************************************
 *
 * Copyright (c) 2011, Oracle Corporation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *
 *       Nikita Levyankov
 *
 *******************************************************************************/

package hudson.views;

import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Column for console output icon
 * <p/>
 * Date: 6/23/11
 *
 * @author Nikita Levyankov
 */
public class ConsoleColumn extends ListViewColumn {
    @DataBoundConstructor
    public ConsoleColumn() {
    }

    @Extension
    public static class DescriptorImpl extends ListViewColumnDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.ConsoleColumn_DisplayName();
        }
    }
}
