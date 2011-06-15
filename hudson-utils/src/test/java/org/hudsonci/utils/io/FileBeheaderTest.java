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
