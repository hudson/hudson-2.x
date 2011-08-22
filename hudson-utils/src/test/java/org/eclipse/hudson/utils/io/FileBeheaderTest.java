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
import java.util.Scanner;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

public class FileBeheaderTest
{
    private File templateLog = FileUtil.getResourceAsFile( FileBeheaderTest.class, "sample.log" );

    @Test
    public void contentIsRemovedFromBeginning()
        throws IOException
    {
        File originalLog = cloneFile( templateLog );
        long tail = originalLog.length() / 3;

        File choppedLog = FileBeheader.chop( originalLog, tail );

        String firstLine = new Scanner( choppedLog ).nextLine();
        assertThat( firstLine, not( containsString( "begin" ) ) );
        //TODO fix me
//        assertThat( choppedLog.length(), lessThanOrEqualTo( tail ) );
        assertThat( choppedLog.getName(), equalTo( originalLog.getName() ) );
        assertThat( choppedLog, sameInstance( originalLog ) );
    }

    @Test
    public void originalFileIsReplacedWithChoppedFile()
        throws IOException
    {
        File originalLog = cloneFile( templateLog );

        File choppedLog = FileBeheader.chop( originalLog, originalLog.length() );

        assertThat( choppedLog.getName(), equalTo( originalLog.getName() ) );
        assertThat( choppedLog, sameInstance( originalLog ) );
    }
    
    @Test
    public void keepsOriginalSizeWhenTailIsLargerThanOriginalSize() throws IOException
    {
        File originalLog = cloneFile( templateLog );
        long originalLength = originalLog.length();
        long tailBiggerThanOriginalSize = originalLength + 10;
        
        File choppedLog = FileBeheader.chop( originalLog, tailBiggerThanOriginalSize );
        assertThat( choppedLog.length(), equalTo( originalLength ) );
    }

    private File cloneFile( File source )
        throws IOException
    {
        File clone = new File( source.getParentFile(), System.currentTimeMillis() + "-" + source.getName() );
        FileUtils.copyFile( source, clone );
        return clone;
    }
}
