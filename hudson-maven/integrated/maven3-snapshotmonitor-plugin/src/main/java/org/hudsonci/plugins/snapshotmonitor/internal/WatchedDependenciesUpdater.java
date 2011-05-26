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

package org.hudsonci.plugins.snapshotmonitor.internal;

import com.google.common.collect.Sets;
import org.hudsonci.utils.tasks.MetaProject;

import org.hudsonci.maven.plugin.dependencymonitor.ArtifactsExtractor;
import org.hudsonci.maven.plugin.dependencymonitor.ArtifactsPair;
import org.hudsonci.maven.plugin.dependencymonitor.ProjectArtifactCache;
import org.hudsonci.plugins.snapshotmonitor.SnapshotMonitor;
import org.hudsonci.plugins.snapshotmonitor.SnapshotTrigger;
import org.sonatype.gossip.support.MuxLoggerFactory;
import org.hudsonci.utils.tasks.TaskListenerLogger;
import org.hudsonci.maven.model.MavenCoordinatesDTO;
import org.hudsonci.maven.model.MavenCoordinatesDTOHelper;
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

