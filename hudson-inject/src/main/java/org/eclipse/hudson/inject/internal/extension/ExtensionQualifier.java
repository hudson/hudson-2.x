/*******************************************************************************
 *
 * Copyright (c) 2011 Sonatype, Inc.
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

import hudson.Extension;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * Runtime JSR330 @{@link Qualifier} annotation that wraps an @{@link Extension}.
 */
@Qualifier
@Retention( RetentionPolicy.RUNTIME )
@interface ExtensionQualifier
{
    Extension extension();

    String elementName();
}

final class ExtensionQualifierImpl
    implements ExtensionQualifier
{
    private final Extension extension;

    private final String elementName;

    public ExtensionQualifierImpl( final Extension extension, final String elementName )
    {
        this.extension = extension;
        this.elementName = elementName;
    }

    public Extension extension()
    {
        return extension;
    }

    public String elementName()
    {
        return elementName;
    }

    public Class<? extends Annotation> annotationType()
    {
        return ExtensionQualifier.class;
    }

    @Override
    public boolean equals( final Object rhs )
    {
        if ( this == rhs )
        {
            return true;
        }
        if ( rhs instanceof ExtensionQualifier )
        {
            final ExtensionQualifier qualifier = (ExtensionQualifier) rhs;
            return extension.equals( qualifier.extension() ) && elementName.equals( qualifier.elementName() );
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return ( 127 * "extension".hashCode() ^ extension.hashCode() )
            + ( 127 * "elementName".hashCode() ^ elementName.hashCode() );
    }

    @Override
    public String toString()
    {
        return String.format( "@%s(extension=%s, elementName=%s)", annotationType().getName(), extension, elementName );
    }
}
