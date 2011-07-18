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
 *
 *******************************************************************************/ 

package hudson.model;

import hudson.tasks.BuildWrapper;
import hudson.util.DescribableList;

/**
 * {@link AbstractProject} that has associated {@link BuildWrapper}s.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.335
 */
public interface BuildableItemWithBuildWrappers extends BuildableItem {
    /**
     * {@link BuildableItemWithBuildWrappers} needs to be an instance of
     * {@link AbstractProject}.
     *
     * <p>
     * This method must be always implemented as {@code (AbstractProject)this}, but
     * defining this method emphasizes the fact that this cast must be doable.
     */
    AbstractProject<?,?> asProject();

    /**
     * {@link BuildWrapper}s associated with this {@link AbstractProject}.
     *
     * @return
     *      can be empty but never null. This list is live, and changes to it will be reflected
     *      to the project configuration.
     */
    DescribableList<BuildWrapper,Descriptor<BuildWrapper>> getBuildWrappersList();
}

