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
 *    Winston Prakash
 *      
 *
 *******************************************************************************/ 

package hudson.views;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.MyViewsProperty;

/**
 * Extension point for adding a MyViewsTabBar header to Projects {@link MyViewsProperty}.
 *
 * <p>
 * This object must have the <tt>myViewTabs.jelly</tt>. This view
 * is called once when the My Views main panel is built.
 * The "views" attribute is set to the "Collection of views".
 *
 * <p>
 * There also must be a default constructor, which is invoked to create a My Views TabBar in
 * the default configuration.
 *
 * @author Winston Prakash
 * @since 1.378
 * @see MyViewsTabBarDescriptor
 */
public abstract class MyViewsTabBar extends AbstractDescribableImpl<MyViewsTabBar> implements ExtensionPoint {
    /**
     * Returns all the registered {@link ListViewColumn} descriptors.
     */
    public static DescriptorExtensionList<MyViewsTabBar, Descriptor<MyViewsTabBar>> all() {
        return Hudson.getInstance().<MyViewsTabBar, Descriptor<MyViewsTabBar>>getDescriptorList(MyViewsTabBar.class);
    }

    public MyViewsTabBarDescriptor getDescriptor() {
        return (MyViewsTabBarDescriptor)super.getDescriptor();
    }
}
