/*******************************************************************************
 *
 * Copyright (c) 2010-2011 Sonatype, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *     
 *
 *******************************************************************************/ 

package org.eclipse.hudson.inject.internal.extension;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import hudson.Extension;
import hudson.ExtensionFinder.Sezpoz;
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
        for ( final SpaceIndexItem<Extension, Object> item : SpaceIndex.load( Extension.class, Object.class, space, globalIndex ) )
        {
            try
            {
                // ignore the legacy SezPoz ExtensionFinder
                if ( !Sezpoz.class.equals( item.element() ) )
                {
                    bindItem( binder, item );
                }
            }
            catch ( final Throwable e )
            {
                if (item.annotation().optional()) {
                    log.debug("Failed to bind optional extension: {}", item, e);
                } else {
                    log.warn("Failed to bind extension: {}", item, e);
                }
            }
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private void bindItem( final Binder binder, final SpaceIndexItem<Extension, ?> item )
        throws InstantiationException
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
                final String name = method.getDeclaringClass().getName() + '.' + method.getName();
                final ExtensionQualifier qualifier = new ExtensionQualifierImpl( item.annotation(), name );
                bindProvider( binder, item, Key.get( method.getReturnType(), qualifier ) );
                break;
            }
            case FIELD:
            {
                final Field field = (Field) item.element();
                final String name = field.getDeclaringClass().getName() + '.' + field.getName();
                final ExtensionQualifier qualifier = new ExtensionQualifierImpl( item.annotation(), name );
                bindProvider( binder, item, Key.get( field.getType(), qualifier ) );
                break;
            }
            default:
                break;
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
