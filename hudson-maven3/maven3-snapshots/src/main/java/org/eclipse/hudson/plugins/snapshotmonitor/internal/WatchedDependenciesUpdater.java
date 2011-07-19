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

package org.eclipse.hudson.plugins.snapshotmonitor.internal;

import org.eclipse.hudson.maven.plugin.dependencymonitor.ArtifactsExtractor;
import org.eclipse.hudson.maven.plugin.dependencymonitor.ArtifactsPair;
import org.eclipse.hudson.maven.plugin.dependencymonitor.ProjectArtifactCache;
import com.google.common.collect.Sets;

import org.eclipse.hudson.maven.model.MavenCoordinatesDTOHelper;
import org.eclipse.hudson.utils.tasks.MetaProject;
import org.eclipse.hudson.utils.tasks.TaskListenerLogger;

import org.eclipse.hudson.plugins.snapshotmonitor.SnapshotMonitor;
import org.eclipse.hudson.plugins.snapshotmonitor.SnapshotTrigger;
import org.sonatype.gossip.support.MuxLoggerFactory;
import org.eclipse.hudson.maven.model.MavenCoordinatesDTO;
import hudson.matrix.MatrixRun;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.triggers.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Hook to update watched dependencies once a build has completed.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Singleton
public class WatchedDependenciesUpdater
    extends RunListener<AbstractBuild>
{
    private static final Logger log = LoggerFactory.getLogger(WatchedDependenciesUpdater.class);

    private final SnapshotMonitor snapshotMonitor;

    private final ArtifactsExtractor extractor;

    private final ProjectArtifactCache projectArtifactCache;

    @Inject
    public WatchedDependenciesUpdater(final SnapshotMonitor snapshotMonitor,
                                      final ArtifactsExtractor extractor,
                                      final ProjectArtifactCache projectArtifactCache)
    {
        super(AbstractBuild.class);
        this.snapshotMonitor = checkNotNull(snapshotMonitor);
        this.extractor = checkNotNull(extractor);
        this.projectArtifactCache = checkNotNull(projectArtifactCache);
    }

    @Override
    public void onCompleted(final AbstractBuild build, final TaskListener listener) {
        assert build != null;

        Logger logger = new TaskListenerLogger(listener);
        Logger muxlog = MuxLoggerFactory.create(log, logger);

        Result result = build.getResult();
        if (result.isWorseThan(Result.UNSTABLE)) {
            muxlog.debug("Skipping watched dependency update for build: {} due to result: {}", build, result);
            return;
        }

        SnapshotTrigger trigger = findSnapshotTrigger(build);
        if (trigger == null) {
            muxlog.debug("Skipping watched dependency update; build not configured with trigger: {}", build);
            return;
        }

        try {
            ArtifactsPair artifacts = extractor.extract(build);
            if (artifacts != null) {
                logger.info("Updating watched dependencies");
                Collection<MavenCoordinatesDTO> watched = Sets.newHashSet();

                for (MavenCoordinatesDTO dep : artifacts.consumed) {
                    // Ignore all non-SNAPSHOTs
                    if (!dep.isSnapshot()) {
                        continue;
                    }

                    // Ignore pom artifacts
                    if ("pom".equals(dep.getType())) {
                        continue;
                    }

                    // Ignore artifacts which are produced internally (maybe)
                    if (trigger.isExcludeInternallyProduced()) {
                        if (projectArtifactCache.isProduced(dep)) {
                            muxlog.debug("Artifact is produced internally; skipping: {}", dep);
                            continue;
                        }
                    }

                    logger.info("  {}", dep.toString(MavenCoordinatesDTOHelper.RenderStyle.GAV));
                    watched.add(dep);
                }

                snapshotMonitor.update(build, watched);
            }
        }
        catch (IOException e) {
            muxlog.error("Failed to update watched dependencies for build: {}", build, e);
        }
    }

    private SnapshotTrigger findSnapshotTrigger(final AbstractBuild build) {
        AbstractProject project;
        if (build instanceof MatrixRun) {
            project = MatrixRun.class.cast(build).getParentBuild().getParent();
        }
        else {
            project = build.getProject();
        }

        for (Trigger trigger : new MetaProject(project).getTriggers()) {
            if (trigger instanceof SnapshotTrigger) {
                return (SnapshotTrigger)trigger;
            }
        }

        return null;
    }
}

