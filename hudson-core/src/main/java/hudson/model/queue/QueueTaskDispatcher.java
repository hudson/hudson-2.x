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

import hudson.Extension;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.model.Queue;
import hudson.model.Queue.Task;

/**
 * Vetos the execution of a task on a node
 *
 * <p>
 * To register your dispatcher implementations, put @{@link Extension} on your subtypes.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.360
 */
public abstract class QueueTaskDispatcher implements ExtensionPoint {
    /**
     * Called whenever {@link Queue} is considering to execute the given task on a given node.
     *
     * <p>
     * Implementations can return null to indicate that the assignment is fine, or it can return
     * a non-null instance to block the execution of the task on the given node.
     *
     * <p>
     * Queue doesn't remember/cache the response from dispatchers, and instead it'll keep asking.
     * The upside of this is that it's very easy to block execution for a limited time period (
     * as you just need to return null when it's ready to execute.) The downside of this is that
     * the decision needs to be made quickly.
     *
     * <p>
     * Vetos are additive. When multiple {@link QueueTaskDispatcher}s are in the system,
     * the task won't run on the given node if any one of them returns a non-null value.
     * (This relationship is also the same with built-in check logic.)
     */
    public abstract CauseOfBlockage canTake(Node node, Task task);

    /**
     * All registered {@link QueueTaskDispatcher}s.
     */
    public static ExtensionList<QueueTaskDispatcher> all() {
        return Hudson.getInstance().getExtensionList(QueueTaskDispatcher.class);
    }
}
