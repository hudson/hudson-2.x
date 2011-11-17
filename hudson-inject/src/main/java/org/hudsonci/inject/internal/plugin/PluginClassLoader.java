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

import hudson.PluginWrapper;
import org.aspectj.weaver.loadtime.WeavingURLClassLoader;

import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Plugin class-loader.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.397
 */
public class PluginClassLoader
    extends WeavingURLClassLoader
{
    private PluginWrapper plugin;

    public PluginClassLoader(final List<URL> urls, final ClassLoader parent) {
        super(urls.toArray(new URL[urls.size()]), parent);

        // Some optional classes might not exist, so want to avoid excessive tracing
        Logger.getLogger(WeavingURLClassLoader.class.getName()).setLevel(Level.OFF);
    }

    public PluginWrapper getPlugin() {
        checkState(plugin != null);
        return plugin;
    }

    void setPlugin(final PluginWrapper plugin) {
        checkState(this.plugin == null);
        this.plugin = checkNotNull(plugin);
    }

    @Override
    public String toString() {
        return "PluginClassLoader{" +
            (plugin != null ? plugin.getShortName() : "???") +
            '}';
    }
}
