/**
 * The MIT License
 *
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.sonatype.matrix.smoothie.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.sonatype.matrix.smoothie.SmoothieContainer;
import com.sonatype.matrix.smoothie.internal.extension.ExtensionLocator;
import com.sonatype.matrix.smoothie.internal.extension.SezPozExtensionModule;
import com.sonatype.matrix.smoothie.internal.extension.SmoothieExtensionLocator;
import com.sonatype.matrix.smoothie.internal.plugin.PluginClassLoader;
import com.sonatype.matrix.smoothie.internal.plugin.SmoothiePluginStrategy;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.core.JVM;
import hudson.ClassicPluginStrategy;
import hudson.PluginStrategy;
import hudson.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.guice.bean.binders.SpaceModule;
import org.sonatype.guice.bean.binders.WireModule;
import org.sonatype.guice.bean.locators.DefaultBeanLocator;
import org.sonatype.guice.bean.locators.MutableBeanLocator;
import org.sonatype.guice.bean.locators.QualifiedBean;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * {@link SmoothieContainer} implementation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.2
 */
public class SmoothieContainerImpl
    implements SmoothieContainer
{
    private static final Logger log = LoggerFactory.getLogger(SmoothieContainerImpl.class);

    private final MutableBeanLocator locator = new DefaultBeanLocator();

    private final Injector root;

    private final Map<PluginWrapper,Injector> injectors = new HashMap<PluginWrapper,Injector>();

    private final ReflectionProvider reflection = new JVM().bestReflectionProvider();

    public SmoothieContainerImpl(final Module... modules) {
        root = Guice.createInjector(new WireModule(new BootModule(modules)));

        // HACK:
        log.info("Root bindings:");
        for (Map.Entry<Key<?>,Binding<?>> entry : root.getAllBindings().entrySet()) {
            log.info("  {} -> {}", entry.getKey(), entry.getValue());
        }

        locator.add(root);
    }

    /**
     * Not officially part of {@link SmoothieContainer} API, exposed for {@link com.sonatype.matrix.smoothie.injecto.Injectomatic}.
     *
     * @since 1.1
     */
    public Injector rootInjector() {
        return root;
    }

    /**
     * Bindings for bootstrapping container bits.  Scan path needs to be configured as additional module.
     */
    private class BootModule
        extends AbstractModule
    {
        private final Module[] modules;

        private BootModule(final Module[] modules) {
            assert modules != null;
            this.modules = modules;
        }

        @Override
        protected void configure() {
            bind(MutableBeanLocator.class).toInstance(locator);
            bind(SmoothieContainer.class).toInstance(SmoothieContainerImpl.this);
            bind(ReflectionProvider.class).toInstance(reflection);
            bind(PluginStrategy.class).annotatedWith(Names.named("default")).to(SmoothiePluginStrategy.class);
            bind(ExtensionLocator.class).annotatedWith(Names.named("default")).to(SmoothieExtensionLocator.class);
            install(new HudsonModule());
            
            for (Module module : modules) {
                install(module);
            }
        }
    }

    /**
     * Bindings and classspace for plugins.
     */
    private class PluginModule
        extends AbstractModule
    {
        private final PluginWrapper plugin;

        private PluginModule(final PluginWrapper plugin) {
            assert plugin != null;
            this.plugin = plugin;
        }

        @Override
        protected void configure() {
            bind(MutableBeanLocator.class).toInstance(locator);
            bind(SmoothieContainer.class).toInstance(SmoothieContainerImpl.this);
            bind(ReflectionProvider.class).toInstance(reflection);

            ClassSpace space = createClassSpace();
            install(new SpaceModule(space));
            install(new SezPozExtensionModule(space));
            install(new HudsonModule());
        }

        private ClassSpace createClassSpace() {
            URLClassSpace space;
            if (plugin.classLoader instanceof PluginClassLoader) {
                PluginClassLoader cl = (PluginClassLoader) plugin.classLoader;
                space = new URLClassSpace(cl, cl.getURLs()); // urls logged from PluginWrapperFactory
            }
            else {
                // This should never happen with SmoothiePluginStrategy
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

        Injector injector = Guice.createInjector(new WireModule(new PluginModule(plugin)));

        locator.add(injector);
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

    public <Q extends Annotation, T> Iterable<QualifiedBean<Q, T>> locate(final Key<T> key) {
        return locator.locate(key, null);
    }

    public <T> T get(final Key<T> key) {
        Iterator<QualifiedBean<Annotation,T>> iter = locate(key).iterator();
        assert iter != null;

        if (iter.hasNext()) {
            QualifiedBean<Annotation,T> bean = iter.next();
            log.debug("Found: {}", bean);

            T value = bean.getValue();

            // Complain if there is more than one instance
            if (iter.hasNext()) {
                log.warn("More than one instance bound for key: {}", key);
                while (iter.hasNext()) {
                    bean = iter.next();
                    log.warn("  {}", bean);
                }
            }

            return value;
        }

        throw new RuntimeException("No object bound for key: " + key);
    }
}