/**
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.sonatype.matrix.smoothie.internal.extension;

import com.google.inject.Binder;
import com.google.inject.Module;
import org.sonatype.guice.bean.binders.SpaceModule;
import org.sonatype.guice.bean.reflect.ClassSpace;

/**
 * Configures modules to discover Hudson components.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
public final class ExtensionModule
    implements Module
{
    private final ClassSpace space;

    public ExtensionModule(final ClassSpace space) {
        assert space != null;
        this.space = space;
    }

    public void configure(final Binder binder) {
        assert binder != null;

         // Scan for @Named components
        binder.install(new SpaceModule(space));

        // Scan for @Extension components via SezPoz index
        binder.install(new SezPozExtensionModule(space));
    }
}
