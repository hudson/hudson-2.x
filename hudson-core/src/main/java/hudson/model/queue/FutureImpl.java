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
import hudson.model.Hudson;
import hudson.model.Queue;
import hudson.model.Queue.Executable;
import hudson.model.Queue.Task;
import hudson.remoting.AsyncFutureImpl;

import java.util.HashSet;
import java.util.Set;

/**
 * Created when {@link Queue.Item} is created so that the caller can track the progress of the task.
 *
 * @author Kohsuke Kawaguchi
 */
public final class FutureImpl extends AsyncFutureImpl<Executable> {
    private final Task task;

    /**
     * If the computation has started, set to {@link Executor}s that are running the build.
     */
    private final Set<Executor> executors = new HashSet<Executor>();

    public FutureImpl(Task task) {
        this.task = task;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        Queue q = Hudson.getInstance().getQueue();
        synchronized (q) {
            synchronized (this) {
                if(!executors.isEmpty()) {
                    if(mayInterruptIfRunning)
                        for (Executor e : executors)
                            e.interrupt();
                    return mayInterruptIfRunning;
                }
                return q.cancel(task);
            }
        }
    }

    synchronized void addExecutor(Executor executor) {
        this.executors.add(executor);
    }
}
