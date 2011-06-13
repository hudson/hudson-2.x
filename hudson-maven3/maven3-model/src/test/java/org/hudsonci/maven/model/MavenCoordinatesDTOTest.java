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

package org.hudsonci.maven.model;

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
