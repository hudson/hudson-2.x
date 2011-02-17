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
import com.google.inject.Scopes;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import hudson.Extension;
import hudson.ExtensionPoint;
import hudson.model.Descriptor;

/**
 * Binds Hudson {@link ExtensionPoint} component types.
 *
 * @since 1.1
 */
final class ExtensionTypeBinder
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Binder binder;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public ExtensionTypeBinder( final Binder binder )
    {
        this.binder = binder;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public void bindExtension( final Extension extension, final Class componentType )
    {
        final Named qualifiedName = Names.named( componentType.getName() );
        for ( Class clazz = componentType; clazz != Object.class; clazz = clazz.getSuperclass() )
        {
            if ( ExtensionPoint.class.isAssignableFrom( clazz ) )
            {
                binder.bind( clazz ).annotatedWith( qualifiedName ).to( componentType ).in( Scopes.SINGLETON );
            }

            if ( Descriptor.class.isAssignableFrom( clazz ) )
            {
                binder.bind( Descriptor.class ).annotatedWith( qualifiedName ).to( componentType ).in( Scopes.SINGLETON );
            }

            for ( final Class iface : clazz.getInterfaces() )
            {
                if ( ExtensionPoint.class.isAssignableFrom( iface ) )
                {
                    binder.bind( iface ).annotatedWith( qualifiedName ).to( componentType ).in( Scopes.SINGLETON );
                }
            }
        }
    }
}
