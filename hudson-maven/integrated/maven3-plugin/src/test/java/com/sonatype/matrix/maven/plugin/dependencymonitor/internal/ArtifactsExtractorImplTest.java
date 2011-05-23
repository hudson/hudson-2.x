/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.dependencymonitor.internal;

import com.sonatype.matrix.maven.plugin.dependencymonitor.ArtifactsPair;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixRun;
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
