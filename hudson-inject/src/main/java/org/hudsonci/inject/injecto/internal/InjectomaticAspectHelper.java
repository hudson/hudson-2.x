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

package org.hudsonci.inject.injecto.internal;

import com.google.inject.Key;
import org.hudsonci.inject.Smoothie;
import org.hudsonci.inject.injecto.Injectomatic;
import org.hudsonci.inject.injecto.InjectomaticAware;
import org.hudsonci.inject.internal.OID;
import org.aspectj.lang.JoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper for the {@link Injectomatic} aspect.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.397
 */
public class InjectomaticAspectHelper
{
    private static final Logger log = LoggerFactory.getLogger(InjectomaticAspectHelper.class);

    private static boolean enabled = false;

    private static volatile Injectomatic injecto;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(final boolean enabled) {
        InjectomaticAspectHelper.enabled = enabled;
        log.debug("Aspect-based injection {}", enabled ? "enabled" : "disabled");

        // If enabled, look up the Injectomatic instance, else clear the cache
        if (enabled) {
            injecto = Smoothie.getContainer().get(Key.get(Injectomatic.class));
        } else {
            injecto = null;
        }
    }

    /**
     * Used by InjectomaticAspect.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    static void inject(final JoinPoint joinPoint) {
        if (enabled) {
            Object target = joinPoint.getThis();
            if (log.isTraceEnabled()) {
                log.trace("Requesting injecting; join-point: {}, target: {}", joinPoint, OID.get(target));
            }
            injecto.inject(target);
        }
        else if (log.isTraceEnabled()) {
            log.trace("Aspect-based injection is disabled; ignoring join-point: {}", joinPoint);
        }
    }

    /**
     * Used by InjectomaticAspect.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    static void install(final JoinPoint joinPoint) {
        if (enabled) {
            Object target = joinPoint.getThis();
            if (log.isTraceEnabled()) {
                log.trace("Installing; join-point: {}, target: {}", joinPoint, OID.get(target));
            }
            InjectomaticAware.class.cast(target).setInjectomatic(injecto);
        }
        else if (log.isTraceEnabled()) {
            log.trace("Aspect-based injection is disabled; ignoring join-point: {}", joinPoint);
        }
    }
}
