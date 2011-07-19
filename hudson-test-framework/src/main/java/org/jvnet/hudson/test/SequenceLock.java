/*******************************************************************************
 *
 * Copyright (c) 2004-2010, Oracle Corporation.
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

package org.jvnet.hudson.test;

/**
 * Lock mechanism to let multiple threads execute phases sequentially.
 *
 * @author Kohsuke Kawaguchi
 */
public class SequenceLock {
    /**
     * Currently executing phase N.
     */
    private int n;

    /**
     * This thread is executing the phase
     */
    private Thread t;

    private boolean aborted;

    /**
     * Blocks until all the previous phases are completed, and returns when the specified phase <i>i</i> is started.
     * If the calling thread was executing an earlier phase, that phase is marked as completed.
     *
     * @throws IllegalStateException
     *      if the sequential lock protocol is aborted, or the thread that owns the current phase has quit.
     */
    public synchronized void phase(int i) throws InterruptedException {
        done(); // mark the previous phase done
        while (i!=n) {
            if (aborted)
                throw new IllegalStateException("SequenceLock aborted");
            if (t!=null && !t.isAlive())
                throw new IllegalStateException("Owner thread of the current phase has quit"+t);
            if (i<n)
                throw new IllegalStateException("Phase "+i+" is already completed");
            wait();
        }

        t = Thread.currentThread();
    }

    /**
     * Marks the current phase completed that the calling thread was executing.
     *
     * <p>
     * This is only necessary when the thread exits the last phase, as {@link #phase(int)} call implies the
     * {@link #done()} call.
     */
    public synchronized void done() {
        if (t==Thread.currentThread()) {
            // phase N done
            n++;
            t = null;
            notifyAll();
        }
    }

    /**
     * Tell all the threads that this sequencing was aborted.
     * Everyone waiting for future phases will receive an error.
     *
     * <p>
     * Calling this method from the finally block prevents a dead lock if one of the participating thread
     * aborts with an exception, as without the explicit abort operation, other threads will block forever
     * for a phase that'll never come. 
     */
    public synchronized void abort() {
        aborted = true;
        notifyAll();
    }
}
