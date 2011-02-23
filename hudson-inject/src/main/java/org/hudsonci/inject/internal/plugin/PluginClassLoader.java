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

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Plugin class-loader.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.397
 */
public class PluginClassLoader
    extends URLClassLoader
{
    // FIXME: Re-implement AspectJ LTW here once 1.6.11 is released, previous release has bug with spaces in directory names.
    // FIXME: ... https://bugs.eclipse.org/bugs/show_bug.cgi?id=282379

    private PluginWrapper plugin;

    public PluginClassLoader(final List<URL> urls, final ClassLoader parent) {
        super(urls.toArray(new URL[urls.size()]), parent);
    }

    public PluginWrapper getPlugin() {
        if (plugin == null) {
            throw new IllegalStateException();
        }
        return plugin;
    }

    void setPlugin(final PluginWrapper plugin) {
        checkNotNull(plugin);
        if (this.plugin != null) {
            throw new IllegalStateException();
        }
        this.plugin = plugin;
    }

    @Override
    public String toString() {
        return "PluginClassLoader{" +
            (plugin != null ? plugin.getShortName() : "???") +
            '}';
    }
}