/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.hudsonci.maven.eventspy.common;

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
