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

package org.eclipse.hudson.maven.plugin.dependencymonitor.internal;

import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixRun;

import org.eclipse.hudson.maven.plugin.dependencymonitor.ArtifactsPair;
import org.eclipse.hudson.maven.plugin.dependencymonitor.internal.ArtifactsExtractorImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link ArtifactsExtractorImpl}.
 */
public class ArtifactsExtractorImplTest
{
    private ArtifactsExtractorImpl extractor;

    @Before
    public void setUp() throws Exception {
        extractor = new ArtifactsExtractorImpl();
    }

    @Test
    public void ensureExtractMatrixConfigurationReturnsNull() {
        ArtifactsPair artifacts = extractor.extract(mock(MatrixConfiguration.class));
        assertNull(artifacts);
    }

    @Test
    public void ensureExtractMatrixRunReturnsNull() {
        ArtifactsPair artifacts = extractor.extract(mock(MatrixRun.class));
        assertNull(artifacts);
    }
}
