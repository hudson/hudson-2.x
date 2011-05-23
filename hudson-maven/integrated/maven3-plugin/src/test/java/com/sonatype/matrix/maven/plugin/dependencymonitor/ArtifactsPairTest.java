/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.dependencymonitor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link ArtifactsPair}.
 */
public class ArtifactsPairTest
{
    @Test
    public void ensureNonNull() {
        ArtifactsPair artifacts = new ArtifactsPair(null, null);
        assertNotNull(artifacts.produced);
        assertEquals(0, artifacts.produced.size());
        assertNotNull(artifacts.consumed);
        assertEquals(0, artifacts.consumed.size());
    }

    @Test
    public void ensureIsEmpty() {
        ArtifactsPair artifacts1 = new ArtifactsPair(null, null);
        assertTrue(artifacts1.isEmpty());

        ArtifactsPair artifacts2 = new ArtifactsPair();
        assertTrue(artifacts2.isEmpty());
    }
}
