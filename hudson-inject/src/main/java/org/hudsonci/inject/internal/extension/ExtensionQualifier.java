/**
 * The MIT License
 *
 * Copyright (c) 2011 Sonatype, Inc. All rights reserved.
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
