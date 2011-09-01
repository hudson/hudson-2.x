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

package org.eclipse.hudson.rest.client.ext;

import org.eclipse.hudson.rest.client.HudsonClient;
import org.eclipse.hudson.rest.model.project.ProjectDTO;
import org.eclipse.hudson.rest.model.project.ProjectReferenceDTO;
import org.eclipse.hudson.rest.model.PermissionDTO;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;


import java.io.InputStream;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_XML;

/**
 * Resource operations related to projects.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Path("projects")
@Produces({APPLICATION_JSON, APPLICATION_XML})
public interface ProjectClient
    extends HudsonClient.Extension
{
    @GET
    List<ProjectDTO> getProjects();

    @POST
    @Consumes(TEXT_XML)
    @Path("{projectName}")
    ProjectDTO createProject(@PathParam("projectName") String projectName, InputStream configXml);

    @GET
    @Path("copy")
    ProjectDTO copyProject(@QueryParam("source") String projectName, @QueryParam("target") String targetProjectName);

    @GET
    @Path("{projectName}")
    ProjectDTO getProject(@PathParam("projectName") String projectName);

    // WORK AROUND: This is added to test by UUID reference bits, should eventually only allow one method.
    // FIXME: This can not live here since its accessed by a different root path than the other methods
    ProjectDTO getProject(ProjectReferenceDTO ref);

    @GET
    @Path("{projectName}/config")
    @Produces({ TEXT_XML })
    String getProjectConfig(@PathParam("projectName") String projectName);

    @DELETE
    @Path("{projectName}")
    void deleteProject(@PathParam("projectName") String projectName);

    @GET
    @Path("{projectName}/enable")
    void enableProject(@PathParam("projectName") String projectName, @QueryParam("enable") boolean enable);

    /**
     * Schedule a build of the project identified by the project name.
     *
     * @param projectName
     */
    @GET
    @Path("{projectName}/schedule")
    void scheduleBuild(@PathParam("projectName") final String projectName);

    @GET
    @Path("{projectName}/permissions")
    List<PermissionDTO> getProjectPermissions(@PathParam("projectName") String projectName);
}
