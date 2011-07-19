/*******************************************************************************
 *
 * Copyright (c) 2010, Oracle Corporation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *       Winston Prakash
 *
 *******************************************************************************/ 

package hudson.views;

import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * The Default MyViewsTabBar Extension for {@link MyViewsTabBar}.
 *
 * @author Winston Prakash
 * @since 1.378
 * @see MyViewsTabBar
 */
public class DefaultMyViewsTabBar extends MyViewsTabBar {
    @DataBoundConstructor
    public DefaultMyViewsTabBar() {
    }

    @Extension
    public static class DescriptorImpl extends MyViewsTabBarDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.DefaultMyViewsTabsBar_DisplayName();
            //return "Default My Views TabsBar";
        }
    }

}
