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
package com.sonatype.matrix.smoothie.internal.extension.old;

import com.google.inject.Binder;
import com.google.inject.Module;
import hudson.Extension;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.scanners.ClassSpaceScanner;

import javax.inject.Qualifier;

/**
 * Guice {@link Module} that binds types annotated with {@link Qualifier} or {@link Extension} annotations.
 *
 * This will not catch {@link Extension} on a method or field, use {@link com.sonatype.matrix.smoothie.internal.extension.SezPozExtensionModule} instead.
 *
 * @since 1.1
 */
public final class ExtensionModule
    implements Module
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassSpace space;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public ExtensionModule(final ClassSpace space)
    {
        this.space = space;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void configure( final Binder binder )
    {
        binder.bind( ClassSpace.class ).toInstance( space );

        new ClassSpaceScanner( space ).accept( new ExtensionTypeVisitor( binder ) );
    }
}
