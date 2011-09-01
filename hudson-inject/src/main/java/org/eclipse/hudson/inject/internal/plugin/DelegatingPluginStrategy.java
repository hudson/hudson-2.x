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

package org.eclipse.hudson.inject.internal.plugin;

import com.google.inject.Key;
import com.google.inject.name.Names;

import org.eclipse.hudson.inject.Smoothie;
import org.eclipse.hudson.inject.internal.HudsonModule;

import hudson.PluginManager;
import hudson.model.Hudson;
import hudson.ExtensionComponent;
import hudson.PluginStrategy;
import hudson.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Delegates to {@link PluginStrategy} implementation bound in Guice context.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.397
 */
public class DelegatingPluginStrategy
    implements PluginStrategy
{
    private static final Logger log = LoggerFactory.getLogger(DelegatingPluginStrategy.class);

    private final PluginStrategy delegate;

    public DelegatingPluginStrategy(final PluginManager plugins) {
        // WORK AROUND: Need to configure the PluginManager instance so that bootstrap components can wire correctly
        HudsonModule.bind(plugins);
        this.delegate = Smoothie.getContainer().get(Key.get(PluginStrategy.class, Names.named("default")));
        log.debug("Delegate: {}", delegate);
    }

    public PluginStrategy getDelegate() {
        return delegate;
    }

    public PluginWrapper createPluginWrapper(final File archive) throws IOException {
        return getDelegate().createPluginWrapper(archive);
    }

    public void load(final PluginWrapper plugin) throws IOException {
        getDelegate().load(plugin);
    }

    public void initializeComponents(final PluginWrapper plugin) {
        getDelegate().initializeComponents(plugin);
    }

    public <T> List<ExtensionComponent<T>> findComponents(final Class<T> type, final Hudson hudson) {
        return getDelegate().findComponents(type, hudson);
    }
}
