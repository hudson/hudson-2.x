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

package org.eclipse.hudson.rest.api.internal;

import org.eclipse.hudson.rest.common.ProjectNameCodec;
import org.eclipse.hudson.service.BuildService;
import org.eclipse.hudson.service.ProjectService;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

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
