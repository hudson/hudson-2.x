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

package org.eclipse.hudson.utils.io;

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
