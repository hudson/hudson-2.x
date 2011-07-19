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

import hudson.model.Queue.Task;

import java.util.Collection;
import java.util.Collections;

/**
 * Convenience methods around {@link Task} and {@link SubTask}.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.377
 */
public class Tasks {

    /**
     * A pointless function to work around what appears to be a HotSpot problem. See HUDSON-5756 and bug 6933067
     * on BugParade for more details.
     */
    private static Collection<? extends SubTask> _getSubTasksOf(Task task) {
        return task.getSubTasks();
    }

    public static Collection<? extends SubTask> getSubTasksOf(Task task) {
        try {
            return _getSubTasksOf(task);
        } catch (AbstractMethodError e) {
            return Collections.singleton(task);
        }
    }

    /**
     * A pointless function to work around what appears to be a HotSpot problem. See HUDSON-5756 and bug 6933067
     * on BugParade for more details.
     */
    private static Object _getSameNodeConstraintOf(SubTask t) {
        return t.getSameNodeConstraint();
    }

    public static Object getSameNodeConstraintOf(SubTask t) {
        try {
            return _getSameNodeConstraintOf(t);
        } catch (AbstractMethodError e) {
            return null;
        }
    }

    /**
     * A pointless function to work around what appears to be a HotSpot problem. See HUDSON-5756 and bug 6933067
     * on BugParade for more details.
     */
    public static Task _getOwnerTaskOf(SubTask t) {
        return t.getOwnerTask();
    }

    public static Task getOwnerTaskOf(SubTask t) {
        try {
            return _getOwnerTaskOf(t);
        } catch (AbstractMethodError e) {
            return (Task)t;
        }
    }
}
