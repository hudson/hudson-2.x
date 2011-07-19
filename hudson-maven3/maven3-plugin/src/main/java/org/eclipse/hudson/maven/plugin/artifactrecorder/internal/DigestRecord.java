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

package org.eclipse.hudson.maven.plugin.artifactrecorder.internal;

import org.eclipse.hudson.maven.model.state.ArtifactDTO;

import java.io.Serializable;

/**
 * Data structure to hold information about collected {@link ArtifactDTO}s.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
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
