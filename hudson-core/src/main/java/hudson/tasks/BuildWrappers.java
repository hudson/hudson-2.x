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
*    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson.tasks;

import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.AbstractProject.AbstractProjectDescriptor;
import hudson.Extension;
import hudson.util.DescriptorList;

import java.util.ArrayList;
import java.util.List;

/**
 * List of all installed {@link BuildWrapper}.
 *
 * @author Kohsuke Kawaguchi
 */
public class BuildWrappers {
    /**
     * @deprecated
     *      as of 1.281. Use {@link Extension} for registration, and use {@link BuildWrapper#all()}
     *      for listing them.
     */
    public static final List<Descriptor<BuildWrapper>> WRAPPERS = new DescriptorList<BuildWrapper>(BuildWrapper.class);

    /**
     * List up all {@link BuildWrapperDescriptor}s that are applicable for the given project.
     *
     * @return
     *      The signature doesn't use {@link BuildWrapperDescriptor} to maintain compatibility
     *      with {@link BuildWrapper} implementations before 1.150.
     */
    public static List<Descriptor<BuildWrapper>> getFor(AbstractProject<?, ?> project) {
        List<Descriptor<BuildWrapper>> result = new ArrayList<Descriptor<BuildWrapper>>();
        Descriptor pd = Hudson.getInstance().getDescriptor((Class)project.getClass());

        for (Descriptor<BuildWrapper> w : BuildWrapper.all()) {
            if (pd instanceof AbstractProjectDescriptor && !((AbstractProjectDescriptor)pd).isApplicable(w))
                continue;
            if (w instanceof BuildWrapperDescriptor) {
                BuildWrapperDescriptor bwd = (BuildWrapperDescriptor) w;
                if(bwd.isApplicable(project))
                    result.add(bwd);
            } else {
                // old BuildWrapper that doesn't implement BuildWrapperDescriptor
                result.add(w);
            }
        }
        return result;
    }
}
