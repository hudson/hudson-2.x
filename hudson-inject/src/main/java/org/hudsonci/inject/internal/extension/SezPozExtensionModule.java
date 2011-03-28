/**
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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
package org.hudsonci.inject.internal.extension;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.Scopes;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import hudson.Extension;
import hudson.ExtensionPoint;
import hudson.model.Descriptor;
import net.java.sezpoz.SpaceIndex;
import net.java.sezpoz.SpaceIndexItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.guice.bean.reflect.ClassSpace;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Guice {@link Module} that binds types based on SezPoz index.
 *
 * @since 1.397
 */
@SuppressWarnings( { "unchecked", "rawtypes" } )
public final class SezPozExtensionModule
    implements Module
{
    private static final Logger log = LoggerFactory.getLogger(SezPozExtensionModule.class);

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassSpace space;

    private final boolean globalIndex;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public SezPozExtensionModule( final ClassSpace space, final boolean globalIndex )
    {
        this.space = checkNotNull(space);
        this.globalIndex = globalIndex;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void configure( final Binder binder )
    {
        for ( final SpaceIndexItem item : SpaceIndex.load( Extension.class, Object.class, space, globalIndex ) )
        {
            try
            {
                switch ( item.kind() )
                {
                    case TYPE:
                    {
                        final Class impl = (Class) item.element();
                        binder.bind( impl ).in( Scopes.SINGLETON );
                        bindHierarchy( binder, Key.get( impl ) );

                        break;
                    }
                    case METHOD:
                    {
                        final Method method = (Method) item.element();
                        final Named name = Names.named( method.getDeclaringClass().getName() + '.' + method.getName() );
                        final Key key = Key.get( method.getReturnType(), name );
                        bindProvider(binder, item, key);

                        break;
                    }
                    case FIELD:
                    {
                        final Field field = (Field) item.element();
                        final Named name = Names.named( field.getDeclaringClass().getName() + '.' + field.getName() );
                        final Key key = Key.get( field.getType(), name );
                        bindProvider(binder, item, key);

                        break;
                    }
                    default:
                        break;
                }
            }
            catch ( final Throwable e )
            {
                log.error("Failed to bind item: {}", item, e);
            }
        }
    }

    private void bindProvider(final Binder binder, final SpaceIndexItem item, final Key key) {
        binder.bind(key).toProvider(new Provider() {
            public Object get() {
                try {
                    return item.instance();
                } catch (final InstantiationException e) {
                    throw new ProvisionException(e.toString(), e);
                }
            }
        }).in(Scopes.SINGLETON);
        bindHierarchy(binder, key);
    }

    private static void bindHierarchy( final Binder binder, final Key rootKey )
    {
        final Class root = rootKey.getTypeLiteral().getRawType();

        Annotation qualifier = rootKey.getAnnotation();
        if ( null == qualifier )
        {
            qualifier = Names.named( root.getName() );
        }

        for ( Class clazz = root; clazz != Object.class; clazz = clazz.getSuperclass() )
        {
            if ( clazz != root && ExtensionPoint.class.isAssignableFrom( clazz ) )
            {
                binder.bind( clazz ).annotatedWith( qualifier ).to( rootKey );
            }

            if ( Descriptor.class.isAssignableFrom( clazz ) )
            {
                binder.bind( Descriptor.class ).annotatedWith( qualifier ).to( rootKey );
            }

            for ( final Class<?> iface : clazz.getInterfaces() )
            {
                if ( ExtensionPoint.class.isAssignableFrom( iface ) )
                {
                    binder.bind( iface ).annotatedWith( qualifier ).to( rootKey );
                }
            }
        }
    }
}
