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
import hudson.model.Describable;
import hudson.model.AbstractProject.AbstractProjectDescriptor;

/**
 * {@link Descriptor} for {@link BuildWrapper}.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.150
 */
public abstract class BuildWrapperDescriptor extends Descriptor<BuildWrapper> {
    protected BuildWrapperDescriptor(Class<? extends BuildWrapper> clazz) {
        super(clazz);
    }

    /**
     * Infers the type of the corresponding {@link Describable} from the outer class.
     * This version works when you follow the common convention, where a descriptor
     * is written as the static nested class of the describable class.
     *
     * @since 1.278
     */
    protected BuildWrapperDescriptor() {
    }

    /**
     * Returns true if this task is applicable to the given project.
     *
     * @return
     *      true to allow user to configure this post-promotion task for the given project.
     * @see AbstractProjectDescriptor#isApplicable(Descriptor) 
     */
    public abstract boolean isApplicable(AbstractProject<?,?> item);
}

