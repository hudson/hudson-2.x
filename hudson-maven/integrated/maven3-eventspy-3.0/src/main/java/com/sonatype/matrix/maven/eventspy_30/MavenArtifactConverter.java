/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.eventspy_30;

import com.sonatype.matrix.maven.model.MavenCoordinatesDTO;
import com.sonatype.matrix.maven.model.state.ArtifactDTO;

import org.sonatype.aether.artifact.Artifact;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Convert Maven based artifacts to our artifact model. 
 * 
 * @author Jamie Whitehouse
 * @since 1.1
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
