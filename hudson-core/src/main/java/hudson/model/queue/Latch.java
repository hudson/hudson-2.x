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

import hudson.AbortException;

/**
 * A concurrency primitive that waits for N number of threads to synchronize.
 * If any of the threads are interrupted while waiting for the completion of the condition,
 * then all the involved threads get interrupted.
 *
 * @author Kohsuke Kawaguchi
 */
class Latch {
    private final int n;
    private int i=0;
    /**
     * If the synchronization on the latch is aborted/interrupted,
     * point to the stack trace where that happened. If null,
     * no interruption happened.
     */
    private Exception interrupted;

    public Latch(int n) {
        this.n = n;
    }

    public synchronized void abort(Throwable cause) {
        interrupted = new AbortException();
        if (cause!=null)
            interrupted.initCause(cause);
        notifyAll();
    }


    public synchronized void synchronize() throws InterruptedException {
        check(n);

        try {
            onCriteriaMet();
        } catch (Error e) {
            abort(e);
            throw e;
        } catch (RuntimeException e) {
            abort(e);
            throw e;
        }

        check(n*2);
    }

    private void check(int threshold) throws InterruptedException {
        i++;
        if (i==threshold) {
            notifyAll();
        } else {
            while (i<threshold && interrupted==null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    interrupted = e;
                    notifyAll();
                    throw e;
                }
            }
        }

        // all of us either leave normally or get interrupted
        if (interrupted!=null)
            throw (InterruptedException)new InterruptedException().initCause(interrupted);
    }

    protected void onCriteriaMet() throws InterruptedException {}
}
