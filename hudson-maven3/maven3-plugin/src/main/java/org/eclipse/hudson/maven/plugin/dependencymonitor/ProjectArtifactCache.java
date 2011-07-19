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

package org.eclipse.hudson.maven.plugin.dependencymonitor;

import com.google.inject.ImplementedBy;

import org.eclipse.hudson.maven.plugin.dependencymonitor.internal.ProjectArtifactCacheImpl;
import org.eclipse.hudson.maven.model.MavenCoordinatesDTO;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import java.util.Collection;


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
