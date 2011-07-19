/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc.
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

package hudson.model.queue;

import hudson.model.Executor;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.Queue.Executable;
import hudson.model.Queue.Task;
import hudson.model.ResourceActivity;

import java.io.IOException;

/**
 * A component of {@link Task} that represents a computation carried out by a single {@link Executor}.
 *
 * A {@link Task} consists of a number of {@link SubTask}.
 *
 * <p>
 * Plugins are encouraged to extend from {@link AbstractSubTask}
 * instead of implementing this interface directly, to maintain
 * compatibility with future changes to this interface.
 *
 * @since 1.377
 */
public interface SubTask extends ResourceActivity {
    /**
     * If this task needs to be run on a node with a particular label,
     * return that {@link Label}. Otherwise null, indicating
     * it can run on anywhere.
     */
    Label getAssignedLabel();

    /**
     * If the previous execution of this task run on a certain node
     * and this task prefers to run on the same node, return that.
     * Otherwise null.
     */
    Node getLastBuiltOn();

    /**
     * Estimate of how long will it take to execute this task.
     * Measured in milliseconds.
     *
     * @return -1 if it's impossible to estimate.
     */
    long getEstimatedDuration();

    /**
     * Creates {@link Executable}, which performs the actual execution of the task.
     */
    Executable createExecutable() throws IOException;

    /**
     * Gets the {@link Task} that this subtask belongs to.
     */
    Task getOwnerTask();

    /**
     * If a subset of {@link SubTask}s of a {@link Task} needs to be collocated with other {@link SubTask}s,
     * those {@link SubTask}s should return the equal object here. If null, the execution unit isn't under a
     * colocation constraint.
     */
    Object getSameNodeConstraint();
}
