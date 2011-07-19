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

import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

import org.eclipse.hudson.maven.plugin.dependencymonitor.DependencyMonitor;
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
 * @since 2.1.0
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

