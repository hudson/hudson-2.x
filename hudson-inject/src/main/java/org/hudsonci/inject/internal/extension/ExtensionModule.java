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

package org.hudsonci.inject.internal.extension;

import com.google.inject.Binder;
import com.google.inject.Module;
import org.sonatype.guice.bean.binders.SpaceModule;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.inject.BeanScanning;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Configures modules to discover Hudson components.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.397
 */
public final class ExtensionModule
    implements Module
{
    private final ClassSpace space;

    private final boolean globalIndex;

    public ExtensionModule(final ClassSpace space, final boolean globalIndex) {
        this.space = checkNotNull(space);
        this.globalIndex = globalIndex;
    }

    public void configure(final Binder binder) {
        assert binder != null;

         // Scan for @Named components using the bean index
        binder.install(new SpaceModule(space, globalIndex ? BeanScanning.GLOBAL_INDEX : BeanScanning.INDEX));

        // Scan for @Extension components via SezPoz index
        binder.install(new SezPozExtensionModule(space, globalIndex));
    }
}
