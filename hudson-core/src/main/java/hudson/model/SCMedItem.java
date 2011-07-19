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

import hudson.scm.PollingResult;
import hudson.scm.SCM;
import hudson.triggers.SCMTrigger;

/**
 * {@link Item}s that has associated SCM.
 *
 * @author Kohsuke Kawaguchi
 * @see SCMTrigger
 */
public interface SCMedItem extends BuildableItem {
    /**
     * Gets the {@link SCM} for this item.
     *
     * @return
     *      may return null for indicating "no SCM".
     */
    SCM getScm();

    /**
     * {@link SCMedItem} needs to be an instance of
     * {@link AbstractProject}.
     *
     * <p>
     * This method must be always implemented as {@code (AbstractProject)this}, but
     * defining this method emphasizes the fact that this cast must be doable.
     */
    AbstractProject<?,?> asProject();

    /**
     * Checks if there's any update in SCM, and returns true if any is found.
     *
     * @deprecated as of 1.346
     *      Use {@link #poll(TaskListener)} instead.
     */
    boolean pollSCMChanges( TaskListener listener );

    /**
     * Checks if there's any update in SCM, and returns true if any is found.
     *
     * <p>
     * The implementation is responsible for ensuring mutual exclusion between polling and builds
     * if necessary.
     *
     * @return never null.
     *
     * @since 1.345
     */
    public PollingResult poll( TaskListener listener );
}
