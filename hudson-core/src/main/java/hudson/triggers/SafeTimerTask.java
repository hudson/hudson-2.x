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

package hudson.triggers;

import org.acegisecurity.context.SecurityContextHolder;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import java.util.logging.Level;

import hudson.security.ACL;

/**
 * {@link Timer} wrapper so that a fatal error in {@link TimerTask}
 * won't terminate the timer.
 *
 * <p>
 * {@link Trigger#timer} is a shared timer instance that can be used inside Hudson to
 * schedule a recurring work.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.124
 * @see Trigger#timer
 */
public abstract class SafeTimerTask extends TimerTask {
    public final void run() {
        // background activity gets system credential,
        // just like executors get it.
        SecurityContextHolder.getContext().setAuthentication(ACL.SYSTEM);

        try {
            doRun();
        } catch(Throwable t) {
            LOGGER.log(Level.SEVERE, "Timer task "+this+" failed",t);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    protected abstract void doRun() throws Exception;

    private static final Logger LOGGER = Logger.getLogger(SafeTimerTask.class.getName());
}
