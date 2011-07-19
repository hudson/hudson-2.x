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

import org.eclipse.hudson.inject.injecto.Injectomatic;
import org.sonatype.inject.EagerSingleton;

import javax.inject.Inject;
import javax.inject.Named;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Helper to register injectable types for Hudson when the container starts.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.397
 */
@Named
@EagerSingleton
public class RegisteredTypeInstaller
{
    @Inject
    public RegisteredTypeInstaller(final Injectomatic injecto) {
        checkNotNull(injecto);
        injecto.register(hudson.model.Descriptor.class);
        injecto.register(hudson.model.Describable.class);
        injecto.register(hudson.Plugin.class);
        injecto.register(hudson.ExtensionPoint.class);
    }
}
