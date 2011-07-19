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

import com.google.common.collect.Sets;

import org.eclipse.hudson.maven.plugin.builder.BuildStateRecord;
import org.eclipse.hudson.maven.plugin.dependencymonitor.ArtifactsExtractor;
import org.eclipse.hudson.maven.plugin.dependencymonitor.ArtifactsPair;
import org.eclipse.hudson.maven.model.MavenCoordinatesDTO;
import org.eclipse.hudson.maven.model.state.ArtifactDTO;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixProject;
import hudson.matrix.MatrixRun;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link ArtifactsExtractor}.
 *
 * Handles multi-configuration project/build artifact extraction.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class ArtifactsExtractorImpl
    implements ArtifactsExtractor
{
    private static final Logger log = LoggerFactory.getLogger(ArtifactsExtractorImpl.class);

    // TODO: If we want to expose the raw extraction, then make a new ArtifactExtractor component impl

    /**
     * {@inheritDoc}
     *
     * When project is a multi-configuration {@link MatrixProject} the artifacts for all configurations are returned.
     * When project is a multi-configuration {@link MatrixConfiguration} {@code null} is always returned.
     */
    public ArtifactsPair extract(final AbstractProject project) {
        checkNotNull(project);

        if (project instanceof MatrixConfiguration) {
            return null;
        }

        AbstractBuild build = (AbstractBuild) project.getLastSuccessfulBuild();
        return build != null ? extract(build) : null;
    }

    /**
     * {@inheritDoc}
     *
     * When build is a multi-configuration {@link MatrixRun} {@code null} is always returned.
     */
    public ArtifactsPair extract(final AbstractBuild build) {
        checkNotNull(build);

        if (build instanceof MatrixRun) {
            return null;
        }

        log.debug("Extracting artifacts from build: {}", build);

        Set<MavenCoordinatesDTO> produced = Sets.newHashSet();
        Set<MavenCoordinatesDTO> consumed = Sets.newHashSet();

        if (build instanceof MatrixBuild) {
            for (MatrixRun run : MatrixBuild.class.cast(build).getRuns()) {
                log.debug("Including artifacts from multi-config run: {}", run);
                extractFromBuild(run, produced, consumed);
            }
        }
        else {
            extractFromBuild(build, produced, consumed);
        }

        // Make sure we don't include any produced artifacts in the consumed list
        consumed.removeAll(produced);

        return new ArtifactsPair(produced, consumed);
    }

    private void extractFromBuild(final AbstractBuild build, final Set<MavenCoordinatesDTO> produced, final Set<MavenCoordinatesDTO> consumed) {
        assert build != null;
        assert produced != null;
        assert consumed != null;

        List<BuildStateRecord> records = build.getActions(BuildStateRecord.class);
        if (!records.isEmpty()) {
            for (BuildStateRecord record : records) {
                for (ArtifactDTO artifact : record.getState().getArtifacts()) {
                    if (artifact.getCreatedProject() != null) {
                        produced.add(artifact.getCoordinates());
                    }
                    else {
                        consumed.add(artifact.getCoordinates());
                    }
                }
            }
        }
    }
}
