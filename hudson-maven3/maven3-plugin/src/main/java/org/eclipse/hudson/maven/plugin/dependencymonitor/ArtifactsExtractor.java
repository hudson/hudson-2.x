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

import org.eclipse.hudson.maven.plugin.dependencymonitor.internal.ArtifactsExtractorImpl;

import com.google.inject.ImplementedBy;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

/**
 * Provides {@link ArtifactsPair} extraction for projects and builds.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
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
