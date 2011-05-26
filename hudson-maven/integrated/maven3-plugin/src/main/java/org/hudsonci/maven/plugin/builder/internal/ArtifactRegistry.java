/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.hudsonci.maven.plugin.builder.internal;

import com.google.common.collect.ImmutableList;
import org.hudsonci.maven.model.MavenCoordinatesDTO;
import org.hudsonci.maven.model.state.ArtifactDTO;

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
