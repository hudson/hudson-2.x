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

package org.hudsonci.utils.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * I usually put these into something like "ResourceManager" and use it to
 * do all the loading. This manager can then cope with the exceptions and
 * logging so each part of the application doesn't have to.
 * 
 * Having a separate manager for it also makes it easier to mock in tests to
 * verify interactions.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public class FileUtil
{
    public static File canonicalize(final File file) {
        checkNotNull(file);
        try {
            return file.getCanonicalFile();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static URL getResource( String resource )
    {
        return Thread.currentThread().getContextClassLoader().getResource( resource );
    }

    public static File getResourceAsFile( String resource )
    {
        return new File( getResource( resource ).getFile() );
    }

    /**
     * Gets a resource relative to the given class.
     */
    public static File getResourceAsFile( Class clazz, String resource )
    {
        return new File( clazz.getResource( resource ).getFile() );
    }
    
    /**
     * Gets a resource using the current threads classloader.
     */
    public static InputStream getResourceAsStream( String resource )
    {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream( resource );
    }

    /**
     * Gets a resource using the given classes classloader.
     */
    public static InputStream getResourceAsStream( Class clazz, String resource )
    {
        return clazz.getClassLoader().getResourceAsStream( resource );
    }
}
