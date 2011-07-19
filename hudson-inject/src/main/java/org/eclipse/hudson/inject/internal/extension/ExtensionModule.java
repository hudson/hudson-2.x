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

package org.eclipse.hudson.inject.internal.extension;

import com.google.inject.Binder;
import com.google.inject.Module;
import org.sonatype.guice.bean.binders.SpaceModule;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.inject.BeanScanning;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Configures modules to discover Hudson components.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.397
 */
public final class ExtensionModule
    implements Module
{
    private final ClassSpace space;

    private final boolean globalIndex;

    public ExtensionModule(final ClassSpace space, final boolean globalIndex) {
        this.space = checkNotNull(space);
        this.globalIndex = globalIndex;
    }

    public void configure(final Binder binder) {
        assert binder != null;

         // Scan for @Named components using the bean index
        binder.install(new SpaceModule(space, globalIndex ? BeanScanning.GLOBAL_INDEX : BeanScanning.INDEX));

        // Scan for @Extension components via SezPoz index
        binder.install(new SezPozExtensionModule(space, globalIndex));
    }
}
