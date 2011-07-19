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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Removes the beginning of a file.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public class FileBeheader
{
    /**
     * Beware, the original file is modified when chopping; you relinquish
     * all control of the original file.
     * 
     * Requires write access to the same directory as the original.
     * 
     * @param original the original file
     * @param tailToKeep the number of bytes to keep at the end of the file
     * 
     * @return the original file chopped; for convenience
     * 
     * @throws IllegalArgumentException if original is a directory
     * @throws IllegalStateException if some error occurs; it's unreasonable to try and recover from this
     */
    public static File chop( final File original, final long tailToKeep )
    {
        if ( !original.isFile() )
        {
            throw new IllegalArgumentException( "Cannot behead directories: " + original );
        }

        File truncatedFile = new File( original.getParentFile(), original.getName() + ".truncated" );

        FileChannel whole = null;
        FileChannel chopped = null;
        try
        {
            whole = new FileInputStream( original ).getChannel();
            chopped = new FileOutputStream( truncatedFile ).getChannel();

            long originalLength = original.length();
            long lengthToKeep = originalLength < tailToKeep ? 0 : originalLength - tailToKeep;
            whole.transferTo( lengthToKeep, originalLength, chopped );
        }
        catch ( IOException e )
        {
            throw new IllegalStateException( "Cannot behead file due to inconsistent internal state.", e );
        }
        finally
        {
            Closer.close( whole, chopped );
        }

        // Replace original with chopped file.
        boolean isRenamed = truncatedFile.renameTo( original );
        if ( !( isRenamed ) )
        {
            IllegalStateException renameException =
                new IllegalStateException( "Cannot replace original with chopped file to: " + original );

            String originalPath = original.getAbsolutePath();
            boolean isDeleted = original.delete();
            if ( isDeleted )
            {
                boolean isRenamedAgain = truncatedFile.renameTo( new File( originalPath ) );
                if ( !isRenamedAgain )
                {
                    throw renameException;
                }
            }
            else
            {
                throw renameException;
            }
        }

        return original;
    }

}
