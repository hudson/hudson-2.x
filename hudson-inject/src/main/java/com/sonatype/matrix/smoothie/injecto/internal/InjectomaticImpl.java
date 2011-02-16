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

package com.sonatype.matrix.smoothie.injecto.internal;

import com.google.inject.Injector;
import com.sonatype.matrix.smoothie.SmoothieContainer;
import com.sonatype.matrix.smoothie.injecto.Injectable;
import com.sonatype.matrix.smoothie.injecto.Injectomatic;
import com.sonatype.matrix.smoothie.internal.OID;
import com.sonatype.matrix.smoothie.internal.SmoothieContainerImpl;
import com.sonatype.matrix.smoothie.internal.plugin.PluginClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

/**
 * Default {@link com.sonatype.matrix.smoothie.injecto.Injectomatic} implementation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.2
 */
@Named
@Singleton
public class InjectomaticImpl
    implements Injectomatic
{
    private static final Logger log = LoggerFactory.getLogger(InjectomaticImpl.class);

    private final SmoothieContainer container;

    //
    // FIXME: Probably need to make these guys thread-safe, but not synchronized to avoid perf cost
    //

    private final Set<Class> registered = new HashSet<Class>();

    private final Set<Class> injectable = new HashSet<Class>();

    private final Set<Class> nonInjectable = new HashSet<Class>();

    @Inject
    public InjectomaticImpl(final SmoothieContainer container) {
        assert container != null;
        this.container = container;
        register(Injectable.class);
    }

    public void register(final Class type) {
        assert type != null;

        log.debug("Registering type: {}", type);
        
        registered.add(type);
    }

    public boolean isInjectable(final Class type) {
        assert type != null;

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
                // Add to injectable type cache
                injectable.add(type);
                log.trace("Detected injectable type: {}", type);
                return true;
            }
        }

        // Not injectable; add to cache
        nonInjectable.add(type);

        return false;
    }

    public void inject(final Object component) {
        assert component != null;

        Class type = component.getClass();

        if (!isInjectable(type)) {
            log.trace("Type not injectable; skipping: {}", type);
            return;
        }

        ClassLoader tmp = type.getClassLoader();
        Injector injector;
        if (tmp instanceof PluginClassLoader) {
            PluginClassLoader cl = (PluginClassLoader)tmp;
            injector = container.injector(cl.getPlugin());
        }
        else {
            // Use the root injector if we did not load from a plugin
            injector = ((SmoothieContainerImpl)container).rootInjector();
        }

        if (log.isTraceEnabled()) {
            log.trace("Injecting: {} ({})", component, OID.get(component));
        }

        injector.injectMembers(component);
    }
}