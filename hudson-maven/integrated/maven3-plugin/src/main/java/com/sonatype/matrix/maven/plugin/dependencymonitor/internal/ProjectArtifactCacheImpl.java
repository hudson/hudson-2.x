/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.dependencymonitor.internal;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.sonatype.matrix.maven.model.MavenCoordinatesDTO;
import com.sonatype.matrix.maven.plugin.dependencymonitor.ArtifactsExtractor;
import com.sonatype.matrix.maven.plugin.dependencymonitor.ArtifactsPair;
import com.sonatype.matrix.maven.plugin.dependencymonitor.ProjectArtifactCache;
import com.sonatype.matrix.service.ProjectService;
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
import static com.sonatype.matrix.common.Varargs.$;

/**
 * Default implementation of {@link ProjectArtifactCache}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public Collection<AbstractProject> getArtifactProducers() {
        Lock lock = readLock();
        try {
            return ImmutableSet.copyOf(projectProducedArtifacts.keySet());
        }
        finally {
            lock.unlock();
        }
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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
