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

package hudson.model.queue;

import hudson.model.Queue.Task;
import hudson.model.Action;
import hudson.model.Queue;

import java.util.List;

/**
 * An action interface that allows action data to be folded together.
 *
 * <p>
 * {@link Action} can implement this optional marker interface to be notified when
 * the {@link Task} that it's added to the queue with is determined to be "already in the queue".
 *
 * <p>
 * This is useful for passing on parameters to the task that's already in the queue.
 *
 * @author mdonohue
 * @since 1.300-ish.
 */
public interface FoldableAction extends Action {
    /**
     * Notifies that the {@link Task} that "owns" this action (that is, the task for which this action is submitted)
     * is considered as a duplicate.
     *
     * @param item
     *      The existing {@link Queue.Item} in the queue against which we are judged as a duplicate. Never null.
     * @param owner
     *      The {@link Task} with which this action was submitted to the queue. Never null.
     * @param otherActions
     *      Other {@link Action}s that are submitted with the task. (One of them is this {@link FoldableAction}.)
     *      Never null.
     */
    void foldIntoExisting(Queue.Item item, Task owner, List<Action> otherActions);
}
