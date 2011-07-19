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

package hudson.util;

/**
 * OConcurrency primitive.
 *
 * <p>
 * A {@link OneShotEvent} is like a pandora's box.
 * It starts with the closed (non-signaled) state.
 * Multiple threads can wait for the event to become the signaled state.
 *
 * <p>
 * Once the event becomes signaled, or the pandora's box is opened,
 * every thread gets through freely, and there's no way to turn it back off. 
 *
 * @author Kohsuke Kawaguchi
 */
public final class OneShotEvent {
    private boolean signaled;
    private final Object lock;

    public OneShotEvent() {
        this.lock = this;
    }

    public OneShotEvent(Object lock) {
        this.lock = lock;
    }

    /**
     * Non-blocking method that signals this event.
     */
    public void signal() {
        synchronized (lock) {
            if(signaled)        return;
            this.signaled = true;
            lock.notifyAll();
        }
    }

    /**
     * Blocks until the event becomes the signaled state.
     *
     * <p>
     * This method blocks infinitely until a value is offered.
     */
    public void block() throws InterruptedException {
        synchronized (lock) {
            while(!signaled)
                lock.wait();
        }
    }

    /**
     * Blocks until the event becomes the signaled state.
     *
     * <p>
     * If the specified amount of time elapses,
     * this method returns null even if the value isn't offered.
     */
    public void block(long timeout) throws InterruptedException {
        synchronized (lock) {
            if(!signaled)
                lock.wait(timeout);
        }
    }

    /**
     * Returns true if a value is offered.
     */
    public boolean isSignaled() {
        synchronized (lock) {
            return signaled;
        }
    }
}
