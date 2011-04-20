/**
 * Copyright (c) 2011 Sonatype, Inc. All rights reserved.
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
