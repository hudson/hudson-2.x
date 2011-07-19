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

package org.eclipse.hudson.maven.plugin.dependencymonitor;

import org.eclipse.hudson.maven.plugin.dependencymonitor.ArtifactsPair;
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
