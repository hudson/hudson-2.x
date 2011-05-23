/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.dependencymonitor.internal;

import com.sonatype.matrix.maven.plugin.dependencymonitor.DependencyMonitor;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Hook to update build artifacts once a build has completed.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@Named
@Singleton
public class BuildArtifactsUpdater
    extends RunListener<AbstractBuild>
{
    private static final Logger log = LoggerFactory.getLogger(BuildArtifactsUpdater.class);

    private final DependencyMonitor dependencyMonitor;

    @Inject
    public BuildArtifactsUpdater(final DependencyMonitor dependencyMonitor) {
        super(AbstractBuild.class);
        this.dependencyMonitor = checkNotNull(dependencyMonitor);
    }

    @Override
    public void onCompleted(final AbstractBuild build, final TaskListener listener) {
        assert build != null;

        Result result = build.getResult();
        if (result.isWorseThan(Result.UNSTABLE)) {
            log.debug("Skipping artifact update for build: {} due to result: {}", build, result);
            return;
        }

        dependencyMonitor.update(build, listener);
    }
}

