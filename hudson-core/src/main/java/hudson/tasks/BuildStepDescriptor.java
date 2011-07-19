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

import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.AbstractProject.AbstractProjectDescriptor;

import java.util.List;
import java.util.ArrayList;

/**
 * {@link Descriptor} for {@link Builder} and {@link Publisher}.
 *
 * <p>
 * For compatibility reasons, plugins developed before 1.150 may not extend from this descriptor type.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.150
 */
public abstract class BuildStepDescriptor<T extends BuildStep & Describable<T>> extends Descriptor<T> {
    protected BuildStepDescriptor(Class<? extends T> clazz) {
        super(clazz);
    }

    /**
     * Infers the type of the corresponding {@link BuildStep} from the outer class.
     * This version works when you follow the common convention, where a descriptor
     * is written as the static nested class of the describable class.
     *
     * @since 1.278
     */
    protected BuildStepDescriptor() {
    }

    /**
     * Returns true if this task is applicable to the given project.
     *
     * @return
     *      true to allow user to configure this post-promotion task for the given project.
     * @see AbstractProjectDescriptor#isApplicable(Descriptor) 
     */
    public abstract boolean isApplicable(Class<? extends AbstractProject> jobType);


    /**
     * Filters a descriptor for {@link BuildStep}s by using {@link BuildStepDescriptor#isApplicable(Class)}.
     */
    public static <T extends BuildStep&Describable<T>>
    List<Descriptor<T>> filter(List<Descriptor<T>> base, Class<? extends AbstractProject> type) {
        // descriptor of the project
        Descriptor pd = Hudson.getInstance().getDescriptor((Class) type);

        List<Descriptor<T>> r = new ArrayList<Descriptor<T>>(base.size());
        for (Descriptor<T> d : base) {
            if (pd instanceof AbstractProjectDescriptor && !((AbstractProjectDescriptor)pd).isApplicable(d))
                continue;

            if (d instanceof BuildStepDescriptor) {
                BuildStepDescriptor<T> bd = (BuildStepDescriptor<T>) d;
                if(!bd.isApplicable(type))  continue;
                r.add(bd);
            } else {
                // old plugins built before 1.150 may not implement BuildStepDescriptor
                r.add(d);
            }
        }
        return r;
    }
}
