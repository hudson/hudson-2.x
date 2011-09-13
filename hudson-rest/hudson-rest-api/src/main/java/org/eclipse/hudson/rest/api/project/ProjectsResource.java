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

import hudson.XmlFile;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.Run;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.eclipse.hudson.rest.api.build.BuildConverter;
import org.eclipse.hudson.rest.api.internal.PermissionsFactory;
import org.eclipse.hudson.rest.api.internal.ProjectBuildHelper;
import org.eclipse.hudson.rest.api.internal.ResourceSupport;
import org.eclipse.hudson.rest.model.PermissionsDTO;
import org.eclipse.hudson.rest.model.build.BuildsDTO;
import org.eclipse.hudson.rest.model.project.ProjectDTO;
import org.eclipse.hudson.rest.model.project.ProjectsDTO;
import org.eclipse.hudson.service.ProjectService;
import org.eclipse.hudson.service.SecurityService;
import org.eclipse.hudson.utils.io.Closer;

import static com.google.common.base.Preconditions.checkNotNull;
import static hudson.model.Item.BUILD;
import static hudson.model.Item.CONFIGURE;
import static hudson.model.Item.DELETE;
import static hudson.model.Item.EXTENDED_READ;
import static hudson.model.Item.READ;
import static javax.ws.rs.core.MediaType.TEXT_XML;
import static javax.ws.rs.core.Response.Status.CONFLICT;

/**
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Path("/projects")
public class ProjectsResource
    extends ResourceSupport
{
    private final ProjectBuildHelper support;

    private final ProjectService projectService;

    private final SecurityService securityService;

    private final ProjectConverter projectx;

    private final BuildConverter buildx;

    private final PermissionsFactory permissions;

    @Inject
    public ProjectsResource(final ProjectBuildHelper support,
                            final SecurityService securityService,
                            final ProjectService projectService,
                            final ProjectConverter projectx,
                            final BuildConverter buildx,
                            final PermissionsFactory permissions)
    {
        this.support = checkNotNull(support);
        this.securityService = checkNotNull(securityService);
        this.projectService = checkNotNull(projectService);
        this.projectx = checkNotNull(projectx);
        this.buildx = checkNotNull(buildx);
        this.permissions = checkNotNull(permissions);
    }

    @GET
    public ProjectsDTO getProjects() {
        ProjectsDTO projects = new ProjectsDTO();
        log.debug("Listing projects");

        // ProjectServiceImpl checks perms.
        for (AbstractProject<?, ?> project : projectService.getAllProjects()) {
            ProjectDTO dto = projectx.convert(project);
            projects.getProjects().add(dto);
        }

        return projects;
    }

    // FIXME: Change to PUT? Since this is not idempotent, or POST?
    @GET
    @Path("copy")
    public ProjectDTO copyProject(@QueryParam("source") String projectName, @QueryParam("target") String targetProjectName) throws IOException {
        checkNotNull(projectName);
        checkNotNull(targetProjectName);

        projectName = support.decodeProjectName(projectName);
        targetProjectName = support.decodeProjectName(targetProjectName);

        log.debug("Copying project: {} -> {}", projectName, targetProjectName);

        // TODO verify that this impl is not affected by security
        if (projectService.projectExists(targetProjectName)) {
            // FIXME: Include content to explain what the conflict is
            throw new WebApplicationException(CONFLICT);
        }

        AbstractProject source = support.getProject(projectName);
        AbstractProject target = projectService.copyProject(source, targetProjectName);

        return projectx.convert(target);
    }

    @POST
    @Consumes(TEXT_XML)
    @Path("{projectName}")
    public ProjectDTO createProject(@PathParam("projectName") String projectName, final InputStream configXml) throws IOException {
        checkNotNull(projectName);
        checkNotNull(configXml);

        projectName = support.decodeProjectName(projectName);

        log.debug("Creating project: {}", projectName);

        if (projectService.projectExists(projectName)) {
            // FIXME: Include content to explain what the conflict is
            throw new WebApplicationException(CONFLICT);
        }

        InputStream input = new BufferedInputStream(configXml);
        try {
            projectService.createProjectFromXML(projectName, input);
        }
        finally {
            Closer.close(input);
        }
        AbstractProject<?,?> project = projectService.getProject(projectName);
        return projectx.convert(project);
    }

    @GET
    @Path("{projectName}")
    public ProjectDTO getProject(final @PathParam("projectName") String projectName) {
        log.debug("Fetching project: {}", projectName);

        AbstractProject<?,?> project = support.getProject(projectName);

        return projectx.convert(project);
    }

    @DELETE
    @Path("{projectName}")
    public void deleteProject(final @PathParam("projectName") String projectName) throws Exception {
        log.debug("Deleting project: {}", projectName);

        AbstractProject project = support.getProject(projectName);
        project.checkPermission(DELETE);
        project.delete();
    }

    @GET
    @Path("{projectName}/config")
    @Produces({ TEXT_XML })
    public String getProjectConfig(final @PathParam("projectName") String projectName) throws IOException {
        log.debug("Fetching project configuration: {}", projectName);

        AbstractProject project = support.getProject(projectName);
        project.checkPermission(EXTENDED_READ);
        XmlFile file = project.getConfigFile();
        return file.asString();
    }

    // TODO: Add POST to update project config, requires re-implementing the
    // update project muck, Hudson only exposes stapler
    @GET
    @Path("{projectName}/enable")
    public Response enableProject(final @PathParam("projectName") String projectName, final @QueryParam("enable") @DefaultValue("true") boolean enable) {
        log.debug("Project {}: {}", enable ? "enabled" : "disabled", projectName);

        AbstractProject project = support.getProject(projectName);
        project.checkPermission(CONFIGURE);

        try {
            if (enable) {
                project.enable();
            }
            else {
                project.disable();
            }
            return Response.noContent().build();
        }
        catch (Exception e) {
            log.error("Failed to enable/disable project", e);
            return Response.serverError().build();
        }
    }

    @GET
    @Path("{projectName}/schedule")
    public Response scheduleBuild(final @PathParam("projectName") String projectName) {
        log.debug("Scheduling build: {}", projectName);

        AbstractProject project = support.getProject(projectName);
        if (!project.isBuildable()) {
            throw new RuntimeException("Project not buildable: " + projectName);
        }
        project.checkPermission(BUILD);

        // TODO: Support delay (as query param)

        project.scheduleBuild(new Cause.UserCause());

        return Response.noContent().build();
    }

    //
    // TODO: Expose workspace
    //

    @GET
    @Path("{projectName}/builds")
    public BuildsDTO getBuilds(final @PathParam("projectName") String projectName) {
        log.debug("Listing builds for project: {}", projectName);

        // securityService.checkPermission(Permission.READ); or Hudson.READ? 

        AbstractProject<?, ?> project = support.getProject(projectName);
        project.checkPermission(READ); // FIXME: This is already done by ProjectServiceImpl.findProjectByFullName(), though not done when returning a multiconfig project

        BuildsDTO builds = new BuildsDTO();

        for (Run<?, ?> build : project.getBuilds()) {
            if (build.hasPermission(READ)) {
                builds.getBuilds().add(buildx.convert(build));
            }
        }

        return builds;
    }

    @GET
    @Path("{projectName}/permissions")
    public PermissionsDTO getPermissions(final @PathParam("projectName") String projectName) {
        log.debug("Getting permissions for project: {}", projectName);

        AbstractProject project = support.getProject(projectName);
        project.checkPermission(READ);
        return permissions.create(project);
    }
}
