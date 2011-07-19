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

package hudson.model;

import hudson.triggers.SafeTimerTask;
import hudson.triggers.Trigger;
import hudson.ExtensionPoint;
import hudson.Extension;
import hudson.ExtensionList;

import java.util.logging.Logger;
import java.util.Random;
import java.util.Timer;

/**
 * Extension point to perform a periodic task in Hudson (through {@link Timer}.)
 *
 * <p>
 * This extension point is useful if your plugin needs to perform some work in the background periodically
 * (for example, monitoring, batch processing, garbage collection, etc.)
 *
 * <p>
 * Put {@link Extension} on your class to have it picked up and registered automatically, or
 * manually insert this to {@link Trigger#timer}.
 *
 * <p>
 * This class is designed to run a short task. Implementations whose periodic work takes a long time
 * to run should extend from {@link AsyncPeriodicWork} instead. 
 *
 * @author Kohsuke Kawaguchi
 * @see AsyncPeriodicWork
 */
public abstract class PeriodicWork extends SafeTimerTask implements ExtensionPoint {
    protected final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Gets the number of milliseconds between successive executions.
     *
     * <p>
     * Hudson calls this method once to set up a recurring timer, instead of
     * calling this each time after the previous execution completed. So this class cannot be
     * used to implement a non-regular recurring timer.
     *
     * <p>
     * IOW, the method should always return the same value.
     */
    public abstract long getRecurrencePeriod();

    /**
     * Gets the number of milliseconds til the first execution.
     *
     * <p>
     * By default it chooses the value randomly between 0 and {@link #getRecurrencePeriod()}
     */
    public long getInitialDelay() {
        return Math.abs(new Random().nextLong())%getRecurrencePeriod();
    }

    /**
     * Returns all the registered {@link PeriodicWork}s.
     */
    public static ExtensionList<PeriodicWork> all() {
        return Hudson.getInstance().getExtensionList(PeriodicWork.class);
    }

    // time constants
    protected static final long MIN = 1000*60;
    protected static final long HOUR =60*MIN;
    protected static final long DAY = 24*HOUR;
}
