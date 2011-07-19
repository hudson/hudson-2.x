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
 * The Default ViewsTabBar Extension for {@link ViewsTabBar}.
 *
 * @author Winston Prakash
 * @since 1.378
 * @see ViewsTabBar
 */
public class DefaultViewsTabBar extends ViewsTabBar {
    @DataBoundConstructor
    public DefaultViewsTabBar() {
    }

    @Extension
    public static class DescriptorImpl extends ViewsTabBarDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.DefaultViewsTabsBar_DisplayName();
            //return "Default Views TabsBar";
        }
    }

}
