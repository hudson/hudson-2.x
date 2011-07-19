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

package hudson.remoting;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.CancellationException;

/**
 * {@link Future} implementation whose computation is carried out elsewhere.
 *
 * Call the {@link #set(Object)} method or {@link #set(Throwable)} method to set the value to the future.
 * 
 * @author Kohsuke Kawaguchi
 */
public class AsyncFutureImpl<V> implements Future<V> {
    /**
     * Setting this field to true will indicate that the computation is completed.
     *
     * <p>
     * One of the following three fields also needs to be set at the same time.
     */
    private boolean completed;

    private V value;
    private Throwable problem;
    private boolean cancelled;

    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public synchronized boolean isDone() {
        return completed;
    }

    public synchronized V get() throws InterruptedException, ExecutionException {
        while(!completed)
            wait();
        if(problem!=null)
            throw new ExecutionException(problem);
        if(cancelled)
            throw new CancellationException();
        return value;
    }

    public synchronized V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if(!completed)
            wait(unit.toMillis(timeout));
        if(!completed)
            throw new TimeoutException();
        if(cancelled)
            throw new CancellationException();
        return get();
    }

    public synchronized void set(V value) {
        completed = true;
        this.value = value;
        notifyAll();
    }

    public synchronized void set(Throwable problem) {
        completed = true;
        this.problem = problem;
        notifyAll();
    }

    /**
     * Marks this task as cancelled.
     */
    public synchronized void setAsCancelled() {
        completed = true;
        cancelled = true;
        notifyAll();
    }
}
