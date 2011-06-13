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

package org.hudsonci.utils.common;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 * Tests for {@link VersionFormatter}.
 * 
 * @author Jamie Whitehouse
 */
public class VersionFormatterTest
{
    @Test
    public void formattedVersionIsUnkownWhenVersionIsNull()
    {
        assertThat( VersionFormatter.asRawVersion( null, null, null ), 
                    equalTo( VersionSupport.UNKNOWN ) );

        assertThat( VersionFormatter.asCanonicalVersion( null, null, null ), 
                    equalTo( VersionSupport.UNKNOWN ) );
    }

    @Test
    public void rawVersionShouldContainSnapshotWhenVersionIsSnapshot()
    {
        assertThat( VersionFormatter.asRawVersion( "1.0-SNAPSHOT", "201009291818", "1" ), 
                    containsString( "SNAPSHOT" ) );
    }

    @Test
    public void rawVersionShouldContainAllInformationWhenVersionIsRelease()
    {
        assertThat( VersionFormatter.asRawVersion( "1.0", "201009291818", "1" ), 
                    equalTo( "1.0,201009291818#1" ) );
    }

    @Test
    public void canonicalVersionShouldNotConatainSnapshotWhenVersionIsSnapshot()
    {
        assertThat( VersionFormatter.asCanonicalVersion( "1.0-SNAPSHOT", "201009291818", "1" ),
                    not( containsString( "SNAPSHOT" ) ) );
    }

    @Test
    public void canonicalVersionShouldOnlyContainVersionNumberWhenVersionIsRelease()
    {
        assertThat( VersionFormatter.asCanonicalVersion( "1.0", "201009291818", "1" ), 
                    equalTo( "1.0" ) );
    }
}
