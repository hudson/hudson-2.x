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

package org.eclipse.hudson.rest.api.project;

import hudson.model.AbstractProject;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.eclipse.hudson.rest.api.internal.ResourceSupport;
import org.eclipse.hudson.rest.model.project.ProjectDTO;
import org.eclipse.hudson.service.ProjectService;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Path("/project")
public class ProjectResource
    extends ResourceSupport
{
    private final ProjectService projectService;

    private final ProjectConverter projectx;

    @Inject
    public ProjectResource(final ProjectService projectService, final ProjectConverter projectx) {
        this.projectService = checkNotNull(projectService);
        this.projectx = checkNotNull(projectx);
    }

    //
    // WORK AROUND: This is added to test by UUID reference bits, should eventually only allow one method.
    //

    @GET
    @Path("{projectId}")
    public ProjectDTO getProject(final @PathParam("projectId") String projectId) {
        checkNotNull(projectId);
        log.debug("Fetching project-by-id: {}", projectId);

        UUID id = UUID.fromString(projectId);
        AbstractProject project = projectService.getProject(id);
        return projectx.convert(project);
    }
}
