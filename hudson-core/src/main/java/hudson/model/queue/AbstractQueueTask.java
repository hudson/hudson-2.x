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

import hudson.model.Queue;
import hudson.model.Queue.Task;

import java.util.Collection;
import java.util.Collections;

/**
 * Abstract base class for {@link Queue.Task} to protect plugins
 * from new additions to the interface.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.360
 */
public abstract class AbstractQueueTask implements Queue.Task {
    public Collection<? extends SubTask> getSubTasks() {
        return Collections.singleton(this);
    }

    public final Task getOwnerTask() {
        return this;
    }
}
