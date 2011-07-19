/*******************************************************************************
 *
 * Copyright (c) 2010, Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *      Winston Prakash
 *
 *******************************************************************************/ 

package hudson.views;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.ListView;

/**
 * Extension point for adding a ViewsTabBar header to Projects {@link ListView}.
 *
 * <p>
 * This object must have the <tt>viewTabs.jelly</tt>. This view
 * is called once when the project views main panel is built.
 * The "views" attribute is set to the "Collection of views".
 *
 * <p>
 * There also must be a default constructor, which is invoked to create a Views TabBar in
 * the default configuration.
 *
 * @author Winston Prakash
 * @since 1.381
 * @see ViewsTabBarDescriptor
 */
public abstract class ViewsTabBar extends AbstractDescribableImpl<ViewsTabBar> implements ExtensionPoint {
    /**
     * Returns all the registered {@link ViewsTabBar} descriptors.
     */
    public static DescriptorExtensionList<ViewsTabBar, Descriptor<ViewsTabBar>> all() {
        return Hudson.getInstance().<ViewsTabBar, Descriptor<ViewsTabBar>>getDescriptorList(ViewsTabBar.class);
    }

    @Override
    public ViewsTabBarDescriptor getDescriptor() {
        return (ViewsTabBarDescriptor)super.getDescriptor();
    }
}
