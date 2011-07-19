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

import com.google.inject.Injector;
import com.google.inject.spi.InjectionPoint;

import org.eclipse.hudson.inject.SmoothieContainer;
import org.eclipse.hudson.inject.injecto.Injectable;
import org.eclipse.hudson.inject.injecto.Injectomatic;
import org.eclipse.hudson.inject.internal.OID;
import org.eclipse.hudson.inject.internal.SmoothieContainerImpl;
import org.eclipse.hudson.inject.internal.plugin.PluginClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link Injectomatic} implementation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.397
 */
@Named
@Singleton
public class InjectomaticImpl
    implements Injectomatic
{
    private static final Logger log = LoggerFactory.getLogger(InjectomaticImpl.class);

    private final SmoothieContainer container;

    private final Set<Class> registered = new HashSet<Class>();

    private final Set<Class> injectable = new HashSet<Class>();

    private final Set<Class> nonInjectable = new HashSet<Class>();

    @Inject
    public InjectomaticImpl(final SmoothieContainer container) {
        this.container = checkNotNull(container);
        register(Injectable.class);
    }

    public void register(final Class type) {
        checkNotNull(type);
        log.debug("Registering type: {}", type);
        registered.add(type);
    }

    public boolean isInjectable(final Class type) {
        checkNotNull(type);

        // See if we have already cached if this type is injectable or not
        if (injectable.contains(type)) {
            return true;
        }
        else if (nonInjectable.contains(type)) {
            return false;
        }

        // See if the type is assignable from a registered type
        for (Class rtype : registered) {
            if (rtype.isAssignableFrom(type)) {
                // Check if there are any method/field injection points (this will mark ctor-only objects as non-injectable, that is okay)
                if (!InjectionPoint.forInstanceMethodsAndFields(type).isEmpty()) {
                    // Add to injectable type cache
                    injectable.add(type);
                    log.trace("Detected injectable type: {}", type);
                    return true;
                }
            }
        }

        // Not injectable; add to cache
        nonInjectable.add(type);

        return false;
    }

    public void inject(final Object component) {
        checkNotNull(component);

        Class type = component.getClass();

        if (!isInjectable(type)) {
            log.trace("Type not injectable; skipping: {}", type);
            return;
        }

        // Find the injector for the component
        ClassLoader tmp = type.getClassLoader();
        Injector injector;

        // If the component belongs to a plugin, then use the plugin's injector
        if (tmp instanceof PluginClassLoader) {
            PluginClassLoader cl = (PluginClassLoader)tmp;
            injector = container.injector(cl.getPlugin());
        }
        else {
            // Use the root injector if we did not load from a plugin
            injector = ((SmoothieContainerImpl)container).rootInjector();
        }

        if (log.isTraceEnabled()) {
            log.trace("Injecting: {}", OID.get(component));
        }

        injector.injectMembers(component);
    }
}
