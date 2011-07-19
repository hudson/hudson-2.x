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

package org.eclipse.hudson.maven.model.test;

import org.eclipse.hudson.maven.model.MavenCoordinatesDTO;
import org.eclipse.hudson.maven.model.state.ArtifactDTO;

/**
 * Various DTOs created in a known state for use in testing.
 * 
 * @author Jamie Whitehouse
 */
public class CannedDtos
{
    private CannedDtos() {
        // non-instantiable
    }

    public static ArtifactDTO fakeArtifact() {
        return new ArtifactDTO().withCoordinates(fakeCoordinates());
    }

    public static MavenCoordinatesDTO fakeCoordinates() {
        return new MavenCoordinatesDTO()
            .withGroupId("fake-groupId")
            .withArtifactId("fake-artifactId")
            .withType("fake-extension")
            .withVersion("1.0-SNAPSHOT")
            .withClassifier("fake-classifier")
            .withExpandedMetaVersion("1.0-SNAPSHOT");
    }
    
    public static MavenCoordinatesDTO fakeCoordinates(final String artifactId)
    {
        return fakeCoordinates().withArtifactId(artifactId).normalize();
    }
}
