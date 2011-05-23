/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.dependencymonitor;

import com.google.inject.ImplementedBy;
import com.sonatype.matrix.maven.plugin.dependencymonitor.internal.ArtifactsExtractorImpl;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

/**
 * Provides {@link ArtifactsPair} extraction for projects and builds.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@ImplementedBy(ArtifactsExtractorImpl.class)
public interface ArtifactsExtractor
{
    /**
     * Get the artifacts from a projects last build.
     *
     * @return {@code null} if no matching last build or build has no Maven build records.
     */
    ArtifactsPair extract(AbstractProject project);

    /**
     * Get the artifacts for a build.
     *
     * @return {@code null} if no Maven build records are available for given build.
     */
    ArtifactsPair extract(AbstractBuild build);
}
