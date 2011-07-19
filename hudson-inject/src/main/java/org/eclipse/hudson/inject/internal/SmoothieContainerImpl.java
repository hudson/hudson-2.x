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
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;

import org.eclipse.hudson.inject.SmoothieContainer;
import org.eclipse.hudson.inject.internal.extension.ExtensionLocator;
import org.eclipse.hudson.inject.internal.extension.ExtensionModule;
import org.eclipse.hudson.inject.internal.extension.SmoothieExtensionLocator;
import org.eclipse.hudson.inject.internal.plugin.PluginClassLoader;
import org.eclipse.hudson.inject.internal.plugin.SmoothiePluginStrategy;

import hudson.PluginStrategy;
import hudson.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.guice.bean.binders.WireModule;
import org.sonatype.guice.bean.locators.DefaultBeanLocator;
import org.sonatype.guice.bean.locators.MutableBeanLocator;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.inject.BeanEntry;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * {@link SmoothieContainer} implementation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.397
 */
public class SmoothieContainerImpl
    implements SmoothieContainer
{
    private static final Logger log = LoggerFactory.getLogger(SmoothieContainerImpl.class);

    private final MutableBeanLocator locator = new DefaultBeanLocator();

    private final Injector root;

    private final Map<PluginWrapper,Injector> injectors = new HashMap<PluginWrapper,Injector>();

    public SmoothieContainerImpl(final Module... modules) {
        this.root = createInjector(new BootModule(modules));
    }

    private Injector createInjector(final Module... modules) {
        assert modules != null;
        Injector injector = Guice.createInjector(new WireModule(modules));

        if (log.isTraceEnabled()) {
            log.trace("Created injector: {} w/bindings:", OID.get(injector));
            for (Map.Entry<Key<?>,Binding<?>> entry : injector.getAllBindings().entrySet()) {
                log.trace("  {} -> {}", entry.getKey(), entry.getValue());
            }
        }

        return injector;
    }

    /**
     * Not officially part of {@link SmoothieContainer} API, exposed for {@link org.eclipse.hudson.inject.injecto.Injectomatic}.
     *
     * @since 1.397
     */
    public Injector rootInjector() {
        return root;
    }

    /**
     * Common bindings.
     */
    private class CommonModule
        extends AbstractModule
    {
        private final Module[] modules;

        private CommonModule(final Module[] modules) {
            assert modules != null;
            this.modules = modules;
        }

        private CommonModule() {
            this(new Module[0]);
        }

        @Override
        protected void configure() {
            bind(MutableBeanLocator.class).toInstance(locator);
            bind(SmoothieContainer.class).toInstance(SmoothieContainerImpl.this);
            install(new HudsonModule());

            for (Module module : modules) {
                install(module);
            }
        }
    }

    /**
     * Bindings for bootstrapping container bits.  Scan path needs to be configured as additional module.
     */
    private class BootModule
        extends CommonModule
    {
        private BootModule(final Module[] modules) {
            super(modules);
        }

        @Override
        protected void configure() {
            bind(PluginStrategy.class).annotatedWith(Names.named("default")).to(SmoothiePluginStrategy.class);
            bind(ExtensionLocator.class).annotatedWith(Names.named("default")).to(SmoothieExtensionLocator.class);
            super.configure();
        }
    }

    // FIXME: Bootstrap probably needs to be aware of WEB-INF/lib/* bits, and should include its own ExtensionModule?


    /**
     * Bindings and class space for plugins.
     */
    private class PluginModule
        extends CommonModule
    {
        private final PluginWrapper plugin;

        private PluginModule(final PluginWrapper plugin) {
            assert plugin != null;
            this.plugin = plugin;
        }

        @Override
        protected void configure() {
            ClassSpace space = createClassSpace();
            install(new ExtensionModule(space, false));
            super.configure();
        }

        private ClassSpace createClassSpace() {
            URLClassSpace space;
            if (plugin.classLoader instanceof PluginClassLoader) {
                PluginClassLoader cl = (PluginClassLoader) plugin.classLoader;
                space = new URLClassSpace(cl, cl.getURLs()); // urls logged from PluginWrapperFactory
            }
            else {
                log.warn("Expected plugin to have PluginClassLoader; instead found: {}", plugin.classLoader.getClass().getName());
                space = new URLClassSpace(plugin.classLoader);
            }

            return space;
        }
    }

    public void register(final PluginWrapper plugin) {
        assert plugin != null;

        if (log.isTraceEnabled()) {
            log.trace("Registering plugin: {}", plugin.getShortName());
        }

        // Don't allow re-registration of plugins
        if (injectors.containsKey(plugin)) {
            throw new IllegalStateException("Plugin already registered");
        }

        Injector injector = createInjector(new PluginModule(plugin));

        injectors.put(plugin, injector);
    }

    public Injector injector(final PluginWrapper plugin) {
        assert plugin != null;
        Injector injector = injectors.get(plugin);

        // All plugins must be registered
        if (injector == null) {
            throw new IllegalStateException("Plugin not registered");
        }

        return injector;
    }

    public <Q extends Annotation, T> Iterable<BeanEntry<Q, T>> locate(final Key<T> key) {
        return locator.locate(key);
    }

    public <T> T get(final Key<T> key) {
        Iterator<BeanEntry<Annotation,T>> iter = locate(key).iterator();
        assert iter != null;

        if (iter.hasNext()) {
            BeanEntry<Annotation,T> bean = iter.next();
            log.debug("Found: {}", bean);

            T value = bean.getValue();

            // If more than one component is found, complain softly
            if (log.isDebugEnabled()) {
                if (iter.hasNext()) {
                    log.debug("More than one instance bound for key: {}", key);
                    while (iter.hasNext()) {
                        log.debug("  {}", iter.next());
                    }
                }
            }

            return value;
        }

        throw new RuntimeException("No object bound for key: " + key);
    }
}
