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

package org.hudsonci.maven.plugin.dependencymonitor.internal;

import com.google.common.collect.Sets;
import org.hudsonci.maven.model.MavenCoordinatesDTO;
import org.hudsonci.maven.model.state.ArtifactDTO;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixProject;
import hudson.matrix.MatrixRun;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import org.hudsonci.maven.plugin.builder.BuildStateRecord;
import org.hudsonci.maven.plugin.dependencymonitor.ArtifactsExtractor;
import org.hudsonci.maven.plugin.dependencymonitor.ArtifactsPair;
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
