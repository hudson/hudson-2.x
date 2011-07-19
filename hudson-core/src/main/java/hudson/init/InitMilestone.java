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

package hudson.init;

import org.jvnet.hudson.reactor.Executable;
import org.jvnet.hudson.reactor.Milestone;
import org.jvnet.hudson.reactor.TaskBuilder;
import org.jvnet.hudson.reactor.TaskGraphBuilder;

/**
 * Various key milestone in the initialization process of Hudson.
 *
 * <p>
 * Plugins can use these milestones to execute their initialization at the right moment
 * (in addition to defining their own milestones by implementing {@link Milestone}.
 *
 * <p>
 * These milestones are achieve in this order.
 * 
 * @author Kohsuke Kawaguchi
 */
public enum InitMilestone implements Milestone {
    /**
     * The very first milestone that gets achieved without doing anything.
     *
     * This is used in {@link Initializer#after()} since annotations cannot have null as the default value.
     */
    STARTED("Started initialization"),

    /**
     * By this milestone, all plugins metadata are inspected and their dependencies figured out.
     */
    PLUGINS_LISTED("Listed all plugins"),

    /**
     * By this milestone, all plugin metadata are loaded and its classloader set up.
     */
    PLUGINS_PREPARED("Prepared all plugins"),

    /**
     * By this milestone, all plugins start executing, all extension points loaded, descriptors instantiated
     * and loaded.
     *
     * <p>
     * This is a separate milestone from {@link #PLUGINS_PREPARED} since the execution
     * of a plugin often involves finding extension point implementations, which in turn
     * require all the classes from all the plugins to be loadable.
     */
    PLUGINS_STARTED("Started all plugins"),

    /**
     * By this milestone, all programmatically constructed extension point implementations
     * should be added.
     */
    EXTENSIONS_AUGMENTED("Augmented all extensions"),

    /**
     * By this milestone, all jobs and their build records are loaded from disk.
     */
    JOB_LOADED("Loaded all jobs"),

    /**
     * The very last milestone
     *
     * This is used in {@link Initializer#before()} since annotations cannot have null as the default value.
     */
    COMPLETED("Completed initialization");

    private final String message;

    InitMilestone(String message) {
        this.message = message;
    }

    /**
     * Creates a set of dummy tasks to enforce ordering among {@link InitMilestone}s.
     */
    public static TaskBuilder ordering() {
        TaskGraphBuilder b = new TaskGraphBuilder();
        InitMilestone[] v = values();
        for (int i=0; i<v.length-1; i++)
            b.add(null, Executable.NOOP).requires(v[i]).attains(v[i+1]);
        return b;
    }


    @Override
    public String toString() {
        return message;
    }
}
