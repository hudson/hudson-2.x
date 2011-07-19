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

package org.eclipse.hudson.inject;

import org.eclipse.hudson.inject.Smoothie;
import org.eclipse.hudson.inject.SmoothieContainer;
import org.eclipse.hudson.inject.injecto.internal.InjectomaticAspectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * Smoothie test utilities.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class SmoothieUtil
{
    private static final Logger log = LoggerFactory.getLogger(SmoothieUtil.class);

    private static void setField(Class type, String name, Object instance, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        field.set(instance, value);
    }

    /**
     * Uses reflection to install a container instance.  This allows the container instance to be reset.
     *
     * @param container     The container to install in {@link Smoothie} or null to reset to the default.
     */
    public static void installContainer(final SmoothieContainer container) throws NoSuchFieldException, IllegalAccessException {
        setField(Smoothie.class, "container", null, container);
        if (container == null) {
            log.info("Reset container");
        }
        else {
            log.info("Installed custom container: {}", container);
        }
    }

    public static void reset() throws NoSuchFieldException, IllegalAccessException {
        installContainer(null);
        InjectomaticAspectHelper.setEnabled(false);
    }
}
