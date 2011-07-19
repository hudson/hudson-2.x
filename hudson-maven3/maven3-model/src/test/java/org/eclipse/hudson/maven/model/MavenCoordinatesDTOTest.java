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

package org.eclipse.hudson.maven.model;

import org.eclipse.hudson.maven.model.MavenCoordinatesDTO;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link MavenCoordinatesDTO}.
 */
public class MavenCoordinatesDTOTest
{
    @Test
    public void testToString() {
        MavenCoordinatesDTO ga = new MavenCoordinatesDTO().withGroupId( "gid" ).withArtifactId( "aid" );
        assertThat("{g=gid,a=aid}", equalTo(ga.toString()));

        MavenCoordinatesDTO gav = new MavenCoordinatesDTO().withGroupId( "gid" ).withArtifactId( "aid" ).withVersion( "version" );
        assertThat("{g=gid,a=aid,v=version}", equalTo(gav.toString()));

        MavenCoordinatesDTO gatc = new MavenCoordinatesDTO().withGroupId( "gid" ).withArtifactId( "aid" ).withType( "type" ).withClassifier( "classifier" );
        assertThat("{g=gid,a=aid,t=type,c=classifier}", equalTo(gatc.toString()));
    }

    @Test
    public void isIdenticalBasedOnAllAttributes()
    {
        MavenCoordinatesDTO original =
            new MavenCoordinatesDTO().withGroupId( "gid" ).withArtifactId( "aid" ).withType( "type" ).withClassifier( "classifier" );
        MavenCoordinatesDTO duplicate =
            new MavenCoordinatesDTO().withGroupId( "gid" ).withArtifactId( "aid" ).withType( "type" ).withClassifier( "classifier" );

        verifyIdentity( original, duplicate );
    }

    @Test
    public void isIdenticalWithMissingClassifier()
    {
        MavenCoordinatesDTO original =
            new MavenCoordinatesDTO().withGroupId( "gid" ).withArtifactId( "aid" ).withType( "type" );
        MavenCoordinatesDTO duplicate =
            new MavenCoordinatesDTO().withGroupId( "gid" ).withArtifactId( "aid" ).withType( "type" );

        verifyIdentity( original, duplicate );
    }

    @Test
    public void normalizationOptionalAttributes() {
        MavenCoordinatesDTO coords = new MavenCoordinatesDTO()
            .withGroupId("")
            .withArtifactId("")
            .withClassifier("")
            .withType("")
            .withVersion("").normalize();

        // groupId and artifactId are required, so normalize should not touch them
        assertEquals("", coords.getGroupId());
        assertEquals("", coords.getArtifactId());

        // These should turn blank string into null
        assertNull(coords.getClassifier());
        assertNull(coords.getType());
        assertNull(coords.getVersion());
    }

    /**
     * NOTE: this modifies the duplicate to verify non-identity (since it's not cloneable and this is a simple test).
     */
    private void verifyIdentity( MavenCoordinatesDTO original, MavenCoordinatesDTO duplicate )
    {
        assertThat( original, equalTo( duplicate ) );
        assertThat( original.hashCode(), equalTo( duplicate.hashCode() ) );

        duplicate.withArtifactId( "not-the-same" );

        assertThat( original, not( equalTo( duplicate ) ) );
        assertThat( original.hashCode(), not( equalTo( duplicate.hashCode() ) ) );
    }

    @Test
    public void testIsSnapshot() {
        MavenCoordinatesDTO c1 = new MavenCoordinatesDTO().withVersion("1-SNAPSHOT");
        assertTrue(c1.isSnapshot());

        MavenCoordinatesDTO c2 = new MavenCoordinatesDTO().withVersion("1");
        assertFalse(c2.isSnapshot());

        MavenCoordinatesDTO c3 = new MavenCoordinatesDTO().withVersion("1-SNAPSHOT-1");
        assertFalse(c3.isSnapshot());
    }
}
