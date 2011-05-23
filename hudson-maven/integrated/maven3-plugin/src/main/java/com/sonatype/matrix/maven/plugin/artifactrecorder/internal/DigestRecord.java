/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */

package com.sonatype.matrix.maven.plugin.artifactrecorder.internal;

import com.sonatype.matrix.maven.model.state.ArtifactDTO;

import java.io.Serializable;

/**
 * Data structure to hold information about collected {@link ArtifactDTO}s.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
 */
public class DigestRecord implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private final ArtifactDTO artifact;
    private final String filename;
    private final String digest;

    public DigestRecord(final ArtifactDTO artifact, final String filename, final String digest) {
        this.artifact = artifact;
        this.filename = filename;
        this.digest = digest;
    }

    public ArtifactDTO getArtifact() {
        return artifact;
    }

    public String getFilename() {
        return filename;
    }

    public String getDigest() {
        return digest;
    }
}