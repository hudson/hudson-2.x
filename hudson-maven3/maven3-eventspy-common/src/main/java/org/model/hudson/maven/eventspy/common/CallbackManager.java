/*******************************************************************************
 *
 * Copyright (c) 2010-2011 Sonatype, Inc.
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

package org.model.hudson.maven.eventspy.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;

/**
 * Manages the {@link Callback} instance.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class CallbackManager
{
    private static final Logger log = LoggerFactory.getLogger(CallbackManager.class);

    /**
     * Gate to block access to the callback until it has been installed.
     */
    private static final CountDownLatch gate = new CountDownLatch(1);

    private static Callback instance;

    /**
     * Get the {@link Callback} instance. If no instance is installed will wait until its available.
     *
     * @return The callback instance; never null
     *
     * @throws RuntimeException Callback not installed in time, or interrupted while waiting for callback installation.
     */
    public static Callback get(final long timeout, final TimeUnit unit) {
        checkArgument(timeout > 0);
        checkNotNull(unit);
        try {
            if (gate.await(timeout, unit)) {
                // sanity check, at this point the holder should have been installed
                checkState(instance != null, "Callback missing after installation");
                return instance;
            }
            throw new RuntimeException(format("Callback not installed in expected time-window: %d %s", timeout, unit));
        }
        catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while waiting for callback", e);
        }
    }

    /**
     * Sets the {@link Callback} instance.  Notifies getter if it is waiting.
     * Instance can only be set once.
     */
    public static void set(final Callback target) {
        checkNotNull(target);

        // Do now allow re-setting the callback, this is per-jvm session
        checkState(instance == null, "Duplicate installation of callback instance is forbidden");

        instance = target;
        log.debug("Callback installed: {}", instance);

        gate.countDown();
    }
}
