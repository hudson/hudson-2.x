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

package org.eclipse.hudson.maven.plugin.builder.internal;

import com.google.common.collect.ImmutableList;
import org.eclipse.hudson.maven.model.MavenCoordinatesDTO;
import org.eclipse.hudson.maven.model.state.ArtifactDTO;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Stores records of {@link ArtifactDTO}s.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public class ArtifactRegistry
{
    // TODO: What's a sane starting point?  Could even size based on the number in the last build.
    // TODO: maybe use ConcurrentMap or javolution FastMap or FastTable; need to time and compare.
    private final Map<MavenCoordinatesDTO,ArtifactDTO> map = new HashMap<MavenCoordinatesDTO,ArtifactDTO>(1000);

    /**
     * Synchronizes recording of artifacts and updates to the attribute values.
     * 
     * Non-null attributes will be overwritten; collection based attributes will have values added.
     */
    public void recordArtifact(final ArtifactDTO artifact) {
        checkNotNull(artifact);
        checkNotNull(artifact.getCoordinates());
        
        synchronized (this) {
            ArtifactDTO entry = get(artifact.getCoordinates());
            if (entry == null) {
                entry = new ArtifactDTO().withCoordinates(artifact.getCoordinates());
            }
            
            // Replace unexpanded meta version if update value is expanded. 
            if (!isMetaVersionExpanded(entry) && isMetaVersionExpanded(artifact)) {
                entry.getCoordinates().withExpandedMetaVersion(artifact.getCoordinates().getExpandedMetaVersion());
            }

            // Don't overwrite existing (non-null) values.
            if (entry.getType() == null && artifact.getType() != null) {
                entry.withType(artifact.getType());
            }

            if (entry.getRepositoryFile() == null && artifact.getRepositoryFile() != null) {
                entry.withRepositoryFile(artifact.getRepositoryFile());
            }

            if (entry.getCreatedProject() == null && artifact.getCreatedProject() != null) {
                entry.withCreatedProject(artifact.getCreatedProject());
            }

            // Merge Collection updates.
            entry.withActions(artifact.getActions());
            entry.withConsumingProjects(artifact.getConsumingProjects());
            entry.withDependentProjects(artifact.getDependentProjects());

            map.put(entry.getCoordinates(), entry);
        }
    }

    private boolean isMetaVersionExpanded(ArtifactDTO entry) {
        String expandedMetaVersion = entry.getCoordinates().getExpandedMetaVersion();
        return expandedMetaVersion != null && !expandedMetaVersion.endsWith("SNAPSHOT");
    }

    /**
     * @return an immutable list of the {@link ArtifactDTO} within the registry
     */
    public Collection<ArtifactDTO> getAll()
    {
        return ImmutableList.copyOf(map.values());
    }
    
    /**
     * Returns the ArtifactDTO for the specified coordinates, or null if there is no registration.
     */
    public ArtifactDTO get(final MavenCoordinatesDTO coordinates)
    {
        // TODO: immutable, NOTE the use of this method in recordArtifact()
        return map.get(coordinates);
    }
}
