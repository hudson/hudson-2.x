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

package org.hudsonci.rest.client.ext;

import org.hudsonci.rest.model.project.ProjectDTO;
import org.hudsonci.rest.model.project.ProjectReferenceDTO;
import org.hudsonci.rest.model.PermissionDTO;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.hudsonci.rest.client.HudsonClient;

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

    // HACK: This is added to test by UUID reference bits, should eventually only allow one method.
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
