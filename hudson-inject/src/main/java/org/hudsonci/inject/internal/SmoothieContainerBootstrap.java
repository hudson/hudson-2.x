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

package org.hudsonci.inject.internal;

import org.hudsonci.inject.Smoothie;
import org.hudsonci.inject.SmoothieContainer;
import org.hudsonci.inject.injecto.internal.InjectomaticAspectHelper;
import org.hudsonci.inject.internal.extension.ExtensionModule;
import hudson.model.Hudson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.guice.bean.reflect.ClassSpace;

/**
 * Bootstraps the container.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.397
 */
public class SmoothieContainerBootstrap
{
    private static final Logger log = LoggerFactory.getLogger(SmoothieContainerBootstrap.class);

    public SmoothieContainer bootstrap() {
        return bootstrap(getClass().getClassLoader(), Hudson.class, Smoothie.class);
    }

    public SmoothieContainer bootstrap(final ClassLoader classLoader, final Class... types) {
        log.info("Bootstrapping Smoothie");

        // Build the root space for the given types
        ClassSpace space = new ClassSpaceFactory().create(classLoader, types);

        // Start up the container
        SmoothieContainer container = new SmoothieContainerImpl(new ExtensionModule(space, true));
        Smoothie.setContainer(container);

        // Enable aspect-based injection
        InjectomaticAspectHelper.setEnabled(true);

        return container;
    }
}
