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

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.core.JVM;
import hudson.PluginManager;
import hudson.TcpSlaveAgentListener;
import hudson.model.FingerprintMap;
import hudson.model.Hudson;
import hudson.model.Queue;
import hudson.model.User;
import hudson.security.SecurityRealm;

import javax.inject.Named;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Configuration of bindings for Hudson components.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.397
 */
public class HudsonModule
    extends AbstractModule
{
    private static final ReflectionProvider reflection = new JVM().bestReflectionProvider();

    @Override
    protected void configure() {
        bind(ReflectionProvider.class).toInstance(reflection);
    }

    @Provides
    private Hudson getHudson() {
        Hudson hudson = Hudson.getInstance();
        checkState(hudson != null);
        return hudson;
    }

    private static PluginManager plugins;

    public static void bind(final PluginManager plugins) {
        HudsonModule.plugins = checkNotNull(plugins);
    }

    @Provides
    private PluginManager getPluginManager() {
        PluginManager target = plugins != null ? plugins : getHudson().getPluginManager();
        checkState(target !=null);
        return target;
    }

    // Helpers to access Hudson singletons via injection.
    // This is not exhaustive by any means and will need to mutate over time as the core is converted to use more injectable components.

    @Provides
    private SecurityRealm getSecurityRealm() {
        return getHudson().getSecurityRealm();
    }

    @Provides
    private Queue getQueue() {
        return getHudson().getQueue();
    }

    @Provides
    private TcpSlaveAgentListener getTcpSlaveAgentListener() {
        return getHudson().getTcpSlaveAgentListener();
    }
    
    @Provides
    private FingerprintMap getFingerprintMap() {
        return getHudson().getFingerprintMap();
    }

    @Provides
    @Named("current")
    private User getCurrentUser() {
        return User.current();
    }
}
