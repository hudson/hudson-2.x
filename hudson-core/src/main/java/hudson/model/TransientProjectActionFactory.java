/*******************************************************************************
 *
 * Copyright (c) 2004-2009, Oracle Corporation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *        
 *
 *******************************************************************************/ 

package hudson.model;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.tasks.BuildStep;

import java.util.Collection;

/**
 * Extension point for inserting transient {@link Action}s into {@link AbstractProject}s.
 *
 * <p>
 * Actions of projects are primarily determined by {@link BuildStep}s that are associated by configurations,
 * but sometimes it's convenient to be able to add actions across all or many projects, without being invoked
 * through configuration. This extension point provides such a mechanism.
 *
 * Actions of {@link AbstractProject}s are transient &mdash; they will not be persisted, and each time Hudson starts
 * or the configuration of the job changes, they'll be recreated. Therefore, to maintain persistent data
 * per project, you'll need to do data serialization by yourself. Do so by storing a file
 * under {@link AbstractProject#getRootDir()}.
 *
 * <p>
 * To register your implementation, put {@link Extension} on your subtype.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.327
 * @see Action
 */
public abstract class TransientProjectActionFactory implements ExtensionPoint {
    /**
     * Creates actions for the given project.
     *
     * @param target
     *      The project for which the action objects are requested. Never null.
     * @return
     *      Can be empty but must not be null.
     */
    public abstract Collection<? extends Action> createFor(AbstractProject target);

    /**
     * Returns all the registered {@link TransientProjectActionFactory}s.
     */
    public static ExtensionList<TransientProjectActionFactory> all() {
        return Hudson.getInstance().getExtensionList(TransientProjectActionFactory.class);
    }
}
