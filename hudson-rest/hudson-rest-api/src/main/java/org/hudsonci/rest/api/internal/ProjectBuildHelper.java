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

package org.hudsonci.rest.api.internal;

import org.hudsonci.service.BuildService;
import org.hudsonci.service.ProjectService;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import org.hudsonci.rest.common.ProjectNameCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Helper to find projects and builds for REST resources.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Singleton
public class ProjectBuildHelper
{
    private static final Logger log = LoggerFactory.getLogger(ProjectBuildHelper.class);

    private final ProjectService projectService;

    private final BuildService buildService;

    private final ProjectNameCodec projectNameCodec;

    @Inject
    public ProjectBuildHelper(final ProjectService projectService, final BuildService buildService, final ProjectNameCodec projectNameCodec) {
        this.projectService = checkNotNull(projectService);
        this.buildService = checkNotNull(buildService);
        this.projectNameCodec = checkNotNull(projectNameCodec);
    }

    public String decodeProjectName(final String projectName) {
        return projectNameCodec.decode(projectName);
    }

    /**
     * Find the project by its un-decoded project name.
     */
    public AbstractProject<?, ?> getProject(final String projectName) {
        checkNotNull(projectName);
        return projectService.getProject(decodeProjectName(projectName));
    }

    /**
     * Finds the build in a project.
     */
    public AbstractBuild<?,?> getBuild(final String projectName, final int buildNumber) {
        AbstractProject<?, ?> project = getProject(projectName);
        return buildService.getBuild(project, buildNumber);
    }
}
