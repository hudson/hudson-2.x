/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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

package org.hudsonci.inject.internal.plugin;

import com.google.inject.Key;
import com.google.inject.name.Names;
import org.hudsonci.inject.Smoothie;
import org.hudsonci.inject.internal.HudsonModule;
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
        // HACK: Need to configure the PluginManager instance so that bootstrap components can wire correctly
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
