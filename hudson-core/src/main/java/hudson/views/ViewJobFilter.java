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
 *    Kohsuke Kawaguchi, Martin Eigenbrodt
 *     
 *
 *******************************************************************************/ 

package hudson.views;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.TopLevelItem;
import hudson.model.View;

import java.util.List;

/**
 * Each ViewJobFilter contributes to or removes from the list of Jobs for a view.
 *
 * @author Jacob Robertson
 */
public abstract class ViewJobFilter implements ExtensionPoint, Describable<ViewJobFilter> {

    /**
     * Returns all the registered {@link ViewJobFilter} descriptors.
     */
    public static DescriptorExtensionList<ViewJobFilter, Descriptor<ViewJobFilter>> all() {
        return Hudson.getInstance().<ViewJobFilter, Descriptor<ViewJobFilter>>getDescriptorList(ViewJobFilter.class);
    }

    @SuppressWarnings("unchecked")
	public Descriptor<ViewJobFilter> getDescriptor() {
        return Hudson.getInstance().getDescriptorOrDie(getClass());
    }
    
    /**
     * Choose which jobs to show for a view.
     * @param added which jobs have been added so far.  This JobFilter can remove or add to this list.
     * @param all All jobs that are possible.
     * @param filteringView The view that we are filtering jobs for.
     * @return a new list based off of the jobs added so far, and all jobs available.
     */
    abstract public List<TopLevelItem> filter(List<TopLevelItem> added, List<TopLevelItem> all, View filteringView);
}
