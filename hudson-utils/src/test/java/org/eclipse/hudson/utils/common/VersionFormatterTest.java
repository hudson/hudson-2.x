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

package org.eclipse.hudson.utils.common;

import org.eclipse.hudson.utils.common.VersionFormatter;
import org.eclipse.hudson.utils.common.VersionSupport;
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
