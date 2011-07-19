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

package org.eclipse.hudson.maven.eventspy_30;

import org.eclipse.hudson.maven.model.MavenCoordinatesDTO;
import org.eclipse.hudson.maven.model.state.ArtifactDTO;

import org.sonatype.aether.artifact.Artifact;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Convert Maven based artifacts to our artifact model. 
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public class MavenArtifactConverter
{
    private MavenArtifactConverter()
    {
        // non-instantiable
    }
    
    public static ArtifactDTO convertAetherArtifact(final Artifact artifact) {
        checkNotNull(artifact);
        
        MavenCoordinatesDTO coordinates = new MavenCoordinatesDTO()
            .withGroupId( artifact.getGroupId() )
            .withArtifactId( artifact.getArtifactId() )
            .withType( artifact.getExtension() )
            .withVersion( artifact.getBaseVersion() )
            .withExpandedMetaVersion( artifact.getVersion() )
            .withClassifier( artifact.getClassifier() )
            .normalize();

        String type = artifact.getProperty( "type", "undefined" );
        
        ArtifactDTO artifactDto = new ArtifactDTO()
            .withCoordinates( coordinates )
            .withType( type );

        return artifactDto;
    }
}
