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

package org.hudsonci.inject;

import com.google.inject.Injector;
import com.google.inject.Key;
import hudson.PluginWrapper;
import org.sonatype.inject.BeanEntry;

import java.lang.annotation.Annotation;

/**
 * Smoothie container.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.397
 */
public interface SmoothieContainer
{
    /**
     * Register injection for a plugin.
     */
    void register(PluginWrapper plugin);

    /**
     * Returns the injector for a plugin.
     */
    Injector injector(PluginWrapper plugin);

    /**
     * Locate components.
     */
    <Q extends Annotation, T> Iterable<BeanEntry<Q,T>> locate(Key<T> key);

    /**
     * Get a component instance.
     */
    <T> T get(Key<T> key);
}