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

package org.eclipse.hudson.inject.internal;

import org.eclipse.hudson.inject.Smoothie;
import org.eclipse.hudson.inject.SmoothieContainer;
import org.eclipse.hudson.inject.injecto.internal.InjectomaticAspectHelper;
import org.eclipse.hudson.inject.internal.extension.ExtensionModule;

import hudson.model.Hudson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.guice.bean.reflect.ClassSpace;

/**
 * Bootstraps the container.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.397
 */
public class SmoothieContainerBootstrap
{
    private static final Logger log = LoggerFactory.getLogger(SmoothieContainerBootstrap.class);

    public SmoothieContainer bootstrap() {
        return bootstrap(getClass().getClassLoader(), Hudson.class, Smoothie.class);
    }

    public SmoothieContainer bootstrap(final ClassLoader classLoader, final Class... types) {
        log.info("Bootstrapping Smoothie");

        // Build the root space for the given types
        ClassSpace space = new ClassSpaceFactory().create(classLoader, types);

        // Start up the container
        SmoothieContainer container = new SmoothieContainerImpl(new ExtensionModule(space, true));
        Smoothie.setContainer(container);

        // Enable aspect-based injection
        InjectomaticAspectHelper.setEnabled(true);

        return container;
    }
}
