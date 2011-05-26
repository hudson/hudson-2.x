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

package org.hudsonci.maven.plugin.dependencymonitor;

import com.google.inject.ImplementedBy;
import org.hudsonci.maven.model.MavenCoordinatesDTO;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import java.util.Collection;

import org.hudsonci.maven.plugin.dependencymonitor.internal.ProjectArtifactCacheImpl;

/**
 * Provides caching of project produced and consumed artifacts.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@ImplementedBy(ProjectArtifactCacheImpl.class)
public interface ProjectArtifactCache
{
    /**
     * Clear the cache.
     */
    void clear();

    /**
     * Rebuild the cache.
     */
    void rebuild();

    /**
     * Update the cache for artifacts in the given build.
     */
    boolean updateArtifacts(AbstractBuild build);

    /**
     * Update the cache for a projects artifacts.
     */
    boolean updateArtifacts(AbstractProject project, ArtifactsPair artifacts);

    /**
     * Get the artifacts pair for a given project.
     */
    ArtifactsPair getArtifacts(AbstractProject project);

    /**
     * Purge all artifacts for the given project.
     */
    void purgeArtifacts(AbstractProject project);

    /**
     * Get artifacts produced by the given project.
     */
    Collection<MavenCoordinatesDTO> getProducedArtifacts(AbstractProject project);

    /**
     * Get artifacts consumed by the given project.
     */
    Collection<MavenCoordinatesDTO> getConsumedArtifacts(AbstractProject project);

    /**
     * Get all artifact producing projects.
     */
    Collection<AbstractProject> getArtifactProducers();

    /**
     * Get all artifact consuming projects.
     */
    Collection<AbstractProject> getArtifactConsumers();

    /**
     * Get the projects which produce the given artifact.
     */
    Collection<AbstractProject> getProducersOf(MavenCoordinatesDTO artifact);

    /**
     * Get the projects which consume the given artifact.
     */
    Collection<AbstractProject> getConsumersOf(MavenCoordinatesDTO artifact);

    /**
     * Check if the given artifact is produced by any projects.
     */
    boolean isProduced(MavenCoordinatesDTO artifact);

    /**
     * Check if the given artifact is consumed by any projects.
     */
    boolean isConsumed(MavenCoordinatesDTO artifact);
}
