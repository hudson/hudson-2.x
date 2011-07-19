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

import org.eclipse.hudson.inject.SmoothieContainer;
import org.eclipse.hudson.inject.internal.extension.ExtensionLocator;

import hudson.ExtensionComponent;
import hudson.ExtensionFinder;
import hudson.Plugin;
import hudson.PluginStrategy;
import hudson.PluginWrapper;
import hudson.PluginWrapper.Dependency;
import hudson.model.Hudson;
import hudson.util.IOException2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Smoothie {@link PluginStrategy}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.397
 */
@Named("default")
@Singleton
public class SmoothiePluginStrategy
    implements PluginStrategy
{
    private static final Logger log = LoggerFactory.getLogger(SmoothiePluginStrategy.class);

    private final SmoothieContainer container;

    private final PluginWrapperFactory pluginFactory;

    private final ExtensionLocator extensionLocator;
    
    private final List<ExtensionFinder> extensionFinders;

    @Inject
    public SmoothiePluginStrategy(final SmoothieContainer container,
                                  final PluginWrapperFactory pluginFactory,
                                  final @Named("default") ExtensionLocator extensionLocator,
                                  final List<ExtensionFinder> extensionFinders) {
        this.container = checkNotNull(container);
        this.pluginFactory = checkNotNull(pluginFactory);
        this.extensionLocator = checkNotNull(extensionLocator);
        this.extensionFinders = checkNotNull(extensionFinders);
    }

    private String basename(String name) {
        assert name != null;
        if (name.endsWith("/")) {
            name = name.substring(0, name.length() - 1);
        }
        int i = name.lastIndexOf("/");
        if (i != -1) {
            return name.substring(i + 1, name.length());
        }
        return name;
    }

    /**
     * Load the plugins wrapper and inject it with the {@link SmoothieContainer}.
     */
    public PluginWrapper createPluginWrapper(final File archive) throws IOException {
        checkNotNull(archive);
        PluginWrapper plugin;
        try {
            plugin = pluginFactory.create(archive);
        }
        catch (Exception e) {
            throw new IOException2(e);
        }

        if (log.isDebugEnabled()) {
            logPluginDetails(plugin);
        }

        return plugin;
    }

    private void logPluginDetails(final PluginWrapper plugin) {
        assert plugin != null;

        log.debug("Loaded plugin: {} ({})", plugin.getShortName(), plugin.getVersion());

        // Some details are not valid until the createPluginWrapper() has returned... like bundled status

        log.debug("  State: active={}, enabled={}, pinned={}, downgradable={}", new Object[] {
            plugin.isActive(),
            plugin.isEnabled(),
            plugin.isPinned(),
            plugin.isDowngradable()
        });

        // Spit out some debug/trace details about the classpath
        PluginClassLoader cl = (PluginClassLoader)plugin.classLoader;
        URL[] classpath = cl.getURLs();
        if (classpath.length > 1) {
            log.debug("  Classpath:");
            int i=0;
            boolean trace = log.isTraceEnabled();
            for (URL url : classpath) {
                // skip the classes/ dir its always there
                if (i++ == 0) {
                    continue;
                }
                // for trace still log as debug, but flip on the full URL
                log.debug("    {}", trace ? url.toString() : basename(url.getFile()));
            }
        }

        // Spit out some debug information about the plugin dependencies
        List<Dependency> dependencies = plugin.getDependencies();
        if (dependencies != null && !dependencies.isEmpty()) {
            log.debug("  Dependencies:");
            for (Dependency dependency : dependencies) {
                log.debug("    {}", dependency);
            }
        }

        dependencies = plugin.getOptionalDependencies();
        if (dependencies != null && !dependencies.isEmpty()) {
            log.debug("  Optional dependencies:");
            for (Dependency dependency : plugin.getOptionalDependencies()) {
                log.debug("    {}", dependency);
            }
        }
    }

    /**
     * Loads the optional {@link hudson.Plugin} instance, configures and starts it.
     */
    public void load(final PluginWrapper plugin) throws IOException {
        checkNotNull(plugin);
        if (log.isDebugEnabled()) {
            log.debug("Configuring plugin: {}", plugin.getShortName());
        }

        container.register(plugin);

        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(plugin.classLoader);
        try {
            Plugin instance;

            // Load the plugin instance, if one has been configured.
            if (plugin.getPluginClass() == null) {
                instance = new Plugin.DummyImpl();
            }
            else {
                try {
                    // Ask the container to construct the instance
                    Class<? extends Plugin> type = loadPluginClass(plugin);
                    instance = container.injector(plugin).getInstance(type);
                    log.trace("Plugin instance: {}", instance);
                }
                catch (Throwable e) {
                    throw new IOException2("Failed to load plugin instance for: " + plugin.getShortName(), e);
                }
            }

            plugin.setPlugin(instance);

            try {
                start(plugin);
            }
            catch (Exception e) {
                throw new IOException2("Failed to start plugin: " + plugin.getShortName(), e);
            }
        }
        finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Isolates cast compiler warning.
     */
    @SuppressWarnings({"unchecked"})
    private Class<? extends Plugin> loadPluginClass(final PluginWrapper plugin) throws ClassNotFoundException {
        assert plugin != null;
        return (Class<? extends Plugin>) plugin.classLoader.loadClass(plugin.getPluginClass());
    }

    /**
     * Configures and starts the {@link hudson.Plugin} instance.
     */
    private void start(final PluginWrapper plugin) throws Exception {
        assert plugin != null;

        if (log.isDebugEnabled()) {
            log.debug("Starting plugin: {}", plugin.getShortName());
        }

        Plugin instance = plugin.getPlugin();
        instance.setServletContext(Hudson.getInstance().servletContext);
        instance.start();
    }

    /**
     * This method of the PluginStrategy interface is completely unused.
     */
    public void initializeComponents(final PluginWrapper plugin) {
        throw new Error("Unused operation");
    }

    public <T> List<ExtensionComponent<T>> findComponents(final Class<T> type, final Hudson hudson) {
        List<ExtensionComponent<T>> components = extensionLocator.locate(type);

        for (ExtensionFinder finder : extensionFinders) {
            try {
                try {
                    components.addAll(finder._find(type, hudson));
                } catch (AbstractMethodError e) {
                    // support legacy finders that only have the old method
                    for (T instance : finder.findExtensions(type, hudson)) {
                        components.add(new ExtensionComponent<T>(instance));
                    }
                }
            } catch (Throwable e) {
                log.warn("Failed to query ExtensionFinder: "+finder, e);                    
            }
        }

        return components;
    }
}
