/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.builder.internal;

import com.google.common.collect.ImmutableList;
import com.sonatype.matrix.maven.model.MavenCoordinatesDTO;
import com.sonatype.matrix.maven.model.state.ArtifactDTO;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Stores records of {@link ArtifactDTO}s.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
 */
public class ArtifactRegistry
{
    // Matrix has 1550 to 1670 fingerprinted artifacts (pom+packaging).
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
