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

package org.eclipse.hudson.inject.injecto.internal;

import com.google.inject.Key;

import org.eclipse.hudson.inject.Smoothie;
import org.eclipse.hudson.inject.injecto.Injectomatic;
import org.eclipse.hudson.inject.injecto.InjectomaticAware;
import org.eclipse.hudson.inject.internal.OID;
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
