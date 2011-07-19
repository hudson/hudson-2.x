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

package hudson.model;

/**
 * Activity that requires certain resources for its execution.
 *
 * @author Kohsuke Kawaguchi
 */
public interface ResourceActivity {
    /**
     * Gets the list of {@link Resource}s that this task requires.
     * Used to make sure no two conflicting tasks run concurrently.
     *
     * <p>
     * This method must always return the {@link ResourceList}
     * that contains the exact same set of {@link Resource}s.
     *
     * <p>
     * If the activity doesn't lock any resources, just
     * return {@code new ResourceList()}.
     *
     * @return never null 
     */
    ResourceList getResourceList();
    
    /**
     * Used for rendering HTML.
     */
    String getDisplayName();
}
