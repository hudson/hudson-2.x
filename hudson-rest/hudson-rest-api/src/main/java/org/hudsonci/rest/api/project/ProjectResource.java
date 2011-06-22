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

package org.hudsonci.rest.api.project;

import javax.inject.Inject;
import javax.inject.Named;
import org.hudsonci.rest.model.project.ProjectDTO;
import org.hudsonci.service.ProjectService;
import hudson.model.AbstractProject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.hudsonci.rest.api.internal.ResourceSupport;

import java.util.UUID;

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
    // HACK: This is added to test by UUID reference bits, should eventually only allow one method.
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
