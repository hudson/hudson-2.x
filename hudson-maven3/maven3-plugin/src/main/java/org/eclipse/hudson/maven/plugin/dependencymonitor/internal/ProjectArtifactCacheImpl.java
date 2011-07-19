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

package org.eclipse.hudson.maven.plugin.dependencymonitor.internal;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import org.eclipse.hudson.maven.plugin.dependencymonitor.ArtifactsExtractor;
import org.eclipse.hudson.maven.plugin.dependencymonitor.ArtifactsPair;
import org.eclipse.hudson.maven.plugin.dependencymonitor.ProjectArtifactCache;
import org.eclipse.hudson.service.ProjectService;
import org.eclipse.hudson.maven.model.MavenCoordinatesDTO;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.eclipse.hudson.utils.common.Varargs.$;

/**
 * Default implementation of {@link ProjectArtifactCache}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Singleton
public class ProjectArtifactCacheImpl
    implements ProjectArtifactCache
{
    private static final Logger log = LoggerFactory.getLogger(ProjectArtifactCacheImpl.class);

    private final ProjectService projectService;

    private final ArtifactsExtractor artifactsExtractor;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    // TODO: Implement a softref cache to free up memory, rebuilding cache from last project if no data has been cached

    // TODO: Add filter or flag to allow optionally only caching SNAPSHOT artifact details, ATM all artifacts are cached

    /**
     * Map of artifact producing projects to their produced artifacts.
     */
    private final Multimap<AbstractProject,MavenCoordinatesDTO> projectProducedArtifacts = HashMultimap.create();

    /**
     * Map of artifact consuming projects to their consumed artifacts.
     */
    private final Multimap<AbstractProject,MavenCoordinatesDTO> projectConsumedArtifacts = HashMultimap.create();

    @Inject
    public ProjectArtifactCacheImpl(final ProjectService projectService, final ArtifactsExtractor artifactsExtractor) {
        this.projectService = checkNotNull(projectService);
        this.artifactsExtractor = checkNotNull(artifactsExtractor);
    }

    /**
     * Returns <em>locked</em> read lock.
     */
    private Lock readLock() {
        Lock tmp = lock.readLock();
        tmp.lock();
        return tmp;
    }

    /**
     * Returns <em>locked</em> write lock.
     */
    private Lock writeLock() {
        Lock tmp = lock.readLock();
        tmp.lock();
        return tmp;
    }

    public ArtifactsPair getArtifacts(final AbstractProject project) {
        checkNotNull(project);
        Lock lock = readLock();
        try {
            return new ArtifactsPair(getProducedArtifacts(project), getConsumedArtifacts(project));
        }
        finally {
            lock.unlock();
        }
    }

    public Collection<MavenCoordinatesDTO> getProducedArtifacts(final AbstractProject project) {
        checkNotNull(project);
        Lock lock = readLock();
        try {
            return ImmutableSet.copyOf(projectProducedArtifacts.get(project));
        }
        finally {
            lock.unlock();
        }
    }

    public Collection<MavenCoordinatesDTO> getConsumedArtifacts(final AbstractProject project) {
        checkNotNull(project);
        Lock lock = readLock();
        try {
            return ImmutableSet.copyOf(projectConsumedArtifacts.get(project));
        }
        finally {
            lock.unlock();
        }
    }

    public Collection<AbstractProject> getArtifactProducers() {
        Lock lock = readLock();
        try {
            return ImmutableSet.copyOf(projectProducedArtifacts.keySet());
        }
        finally {
            lock.unlock();
        }
    }

    public Collection<AbstractProject> getArtifactConsumers() {
        Lock lock = readLock();
        try {
            return ImmutableSet.copyOf(projectConsumedArtifacts.keySet());
        }
        finally {
            lock.unlock();
        }
    }

    private Collection<AbstractProject> projectsContaining(final Multimap<AbstractProject, MavenCoordinatesDTO> source,
                                                           final MavenCoordinatesDTO artifact)
    {
        assert source != null;
        assert artifact != null;
        Set<AbstractProject> projects = Sets.newHashSet();
        for (AbstractProject project : source.keySet()) {
            if (source.containsEntry(project, artifact)) {
                projects.add(project);
            }
        }
        return projects;
    }

    public Collection<AbstractProject> getProducersOf(final MavenCoordinatesDTO artifact) {
        checkNotNull(artifact);
        Lock lock = readLock();
        try {
            return projectsContaining(projectProducedArtifacts, artifact);
        }
        finally {
            lock.unlock();
        }
    }

    public Collection<AbstractProject> getConsumersOf(final MavenCoordinatesDTO artifact) {
        checkNotNull(artifact);
        Lock lock = readLock();
        try {
            return projectsContaining(projectConsumedArtifacts, artifact);
        }
        finally {
            lock.unlock();
        }
    }

    public void clear() {
        log.debug("Clearing");

        Lock lock = writeLock();
        try {
            projectProducedArtifacts.clear();
            projectConsumedArtifacts.clear();
        }
        finally {
            lock.unlock();
        }
    }

    public void rebuild() {
        log.debug("Rebuilding");

        Lock lock = writeLock();
        try {
            clear();

            for (AbstractProject project : projectService.getAllProjects()) {
                ArtifactsPair artifacts = artifactsExtractor.extract(project);
                if (artifacts != null) {
                    updateArtifacts(project, artifacts);
                    if (log.isDebugEnabled()) {
                        log.debug("Cached artifacts for: {}, produced: {}, consumed: {}",
                            $(project, artifacts.produced.size(), artifacts.consumed.size()));
                    }
                }
            }
        }
        finally {
            lock.unlock();
        }
    }

    public boolean updateArtifacts(final AbstractBuild build) {
        checkNotNull(build);
        log.debug("Updating artifacts for build: {}", build);

        ArtifactsPair artifacts = artifactsExtractor.extract(build);
        if (artifacts == null) {
            log.debug("Build produced and consumed no artifacts");
            return false;
        }

        AbstractProject project = build.getProject();
        return updateArtifacts(project, artifacts);
    }

    public boolean updateArtifacts(final AbstractProject project, final ArtifactsPair artifacts) {
        checkNotNull(project);
        checkNotNull(artifacts);

        if (log.isDebugEnabled()) {
            log.debug("Updating produced and consumed artifacts for project: {}", project);

            if (log.isTraceEnabled()) {
                if (artifacts.produced != null) {
                    log.trace("  Produced:");
                    for (MavenCoordinatesDTO artifact : artifacts.produced) {
                        log.trace("    {}", artifact);
                    }
                }
                if (artifacts.produced != null) {
                    log.trace("  Consumed:");
                    for (MavenCoordinatesDTO artifact : artifacts.consumed) {
                        log.trace("    {}", artifact);
                    }
                }
            }
        }

        Lock lock = writeLock();
        try {
            boolean changed = false;
            if (updateArtifacts(projectProducedArtifacts, project, artifacts.produced)) {
                changed = true;
            }
            if (updateArtifacts(projectConsumedArtifacts, project, artifacts.consumed)) {
                changed = true;
            }
            return changed;
        }
        finally {
            lock.unlock();
        }
    }

    private boolean updateArtifacts(final Multimap<AbstractProject, MavenCoordinatesDTO> collection,
                                    final AbstractProject project,
                                    final Collection<MavenCoordinatesDTO> artifacts)
    {
        assert collection != null;
        Collection<MavenCoordinatesDTO> removed = collection.replaceValues(project, artifacts);
        return CollectionsHelper.differs(artifacts, removed);
    }

    public void purgeArtifacts(final AbstractProject project) {
        checkNotNull(project);
        log.debug("Puring artifacts for project: {}", project);

        Lock lock = writeLock();
        try {
            projectProducedArtifacts.removeAll(project);
            projectConsumedArtifacts.removeAll(project);
        }
        finally {
            lock.unlock();
        }
    }

    private boolean projectsContain(final Multimap<AbstractProject, MavenCoordinatesDTO> source, final MavenCoordinatesDTO artifact) {
        assert source != null;
        assert artifact != null;
        for (AbstractProject project : source.keySet()) {
            if (source.containsEntry(project, artifact)) {
                return true;
            }
        }
        return false;
    }

    public boolean isProduced(final MavenCoordinatesDTO artifact) {
        checkNotNull(artifact);

        Lock lock = readLock();
        try {
            return projectsContain(projectProducedArtifacts, artifact);
        }
        finally {
            lock.unlock();
        }
    }

    public boolean isConsumed(final MavenCoordinatesDTO artifact) {
        checkNotNull(artifact);

        Lock lock = readLock();
        try {
            return projectsContain(projectConsumedArtifacts, artifact);
        }
        finally {
            lock.unlock();
        }
    }
}
