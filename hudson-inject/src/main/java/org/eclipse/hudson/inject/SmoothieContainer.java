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

import com.google.inject.Injector;
import com.google.inject.Key;
import hudson.PluginWrapper;
import org.sonatype.inject.BeanEntry;

import java.lang.annotation.Annotation;

/**
 * Smoothie container.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.397
 */
public interface SmoothieContainer
{
    /**
     * Register injection for a plugin.
     */
    void register(PluginWrapper plugin);

    /**
     * Returns the injector for a plugin.
     */
    Injector injector(PluginWrapper plugin);

    /**
     * Locate components.
     */
    <Q extends Annotation, T> Iterable<BeanEntry<Q,T>> locate(Key<T> key);

    /**
     * Get a component instance.
     */
    <T> T get(Key<T> key);
}
