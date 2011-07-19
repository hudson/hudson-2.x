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

package org.eclipse.hudson.inject.injecto;

import com.google.inject.ImplementedBy;

import org.eclipse.hudson.inject.injecto.internal.InjectomaticImpl;

/**
 * Magically injection system.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.397
 */
@ImplementedBy(InjectomaticImpl.class)
public interface Injectomatic
{
    /**
     * Register a class as being injectable.  Any sub-classes are also considered injectable.
     */
    void register(Class type);

    /**
     * Determine if the given type is injectable.
     */
    boolean isInjectable(Class type);

    /**
     * Perform injection on the given component.
     */
    void inject(Object component);
}
