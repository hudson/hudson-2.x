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

package org.hudsonci.rest.api.build;

import org.hudsonci.service.BuildService;
import hudson.model.AbstractProject;
import org.hudsonci.service.ProjectService;
import javax.inject.Inject;
import javax.inject.Named;
import org.hudsonci.utils.io.OffsetLimitInputStream;
import org.hudsonci.rest.model.build.BuildDTO;
import org.hudsonci.rest.model.build.ChangesDTO;
import org.hudsonci.rest.model.build.TestsDTO;
import org.hudsonci.rest.model.build.ConsoleDTO;
import hudson.model.AbstractBuild;
import hudson.tasks.test.AbstractTestResultAction;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.hudsonci.rest.api.internal.ProjectBuildHelper;
import org.hudsonci.rest.api.internal.ResourceSupport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hudsonci.utils.common.Varargs.$;
import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;

/**
 * Provides access to a build.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Path("/projects/{projectName}/{buildNumber:\\d*}")
public class BuildResource
    extends ResourceSupport
{
    private final ProjectService projectService;

    private final BuildService buildService;

    private final ProjectBuildHelper support;

    private final BuildConverter buildx;

    private final ChangesConverter changesx;

    private final TestsConverter testsx;

    @Inject
    public BuildResource(ProjectService projectService,
                         BuildService buildService,
                         ProjectBuildHelper support,
                         BuildConverter buildx,
                         ChangesConverter changesx,
                         TestsConverter testsx)
    {
        this.projectService = checkNotNull(projectService);
        this.buildService = checkNotNull(buildService);
        this.support = checkNotNull(support);
        this.buildx = checkNotNull(buildx);
        this.changesx = checkNotNull(changesx);
        this.testsx = checkNotNull(testsx);
    }

    @GET
    public BuildDTO getBuild(final @PathParam("projectName") String projectName, final @PathParam("buildNumber") int buildNumber) {
        log.debug("Fetching build: {} #{}", projectName, buildNumber);
        AbstractBuild build = support.getBuild(projectName, buildNumber);
        return buildx.convert(build);
    }

    @DELETE
    public void deleteBuild(final @PathParam("projectName") String projectName, final @PathParam("buildNumber") int buildNumber) {
        log.debug("Deleting build: {} #{}", projectName, buildNumber);
        AbstractProject<?, ?> project = support.getProject(projectName);
        buildService.deleteBuild(project, buildNumber);
    }

    @GET
    @Path("keep")
    public Response keepBuild(final @PathParam("projectName") String projectName,
                              final @PathParam("buildNumber") int buildNumber,
                              final @QueryParam("release") @DefaultValue("false") boolean release)
    {
        log.debug("{} build: {} #{}", new Object[] { release ? "Releasing" : "Keeping", projectName, buildNumber });
        AbstractProject<?, ?> project = support.getProject(projectName);
        buildService.keepBuild(project, buildNumber, release);
        return Response.noContent().build();
    }

    @PUT
    @Path("stop")
    public void stopBuild(final @PathParam("projectName") String projectName, final @PathParam("buildNumber") int buildNumber) {
        // FIXME what permissions are needed to stop a build?
        log.debug("Stopping build: {} #{}", projectName, buildNumber);
        AbstractProject<?, ?> project = support.getProject(projectName);
        buildService.stopBuild(project, buildNumber);
    }

    @GET
    @Path("changes")
    public ChangesDTO getChanges(final @PathParam("projectName") String projectName, final @PathParam("buildNumber") int buildNumber) {
        log.debug("Get changes: {} #{}", projectName, buildNumber);

        AbstractBuild build = support.getBuild(projectName, buildNumber);

        if (build.getChangeSet().isEmptySet()) {
            throw new WebApplicationException(NO_CONTENT);
        }

        return changesx.convert(build.getChangeSet());
    }

    @GET
    @Path("tests")
    public TestsDTO getTests(final @PathParam("projectName") String projectName, final @PathParam("buildNumber") int buildNumber) {
        log.debug("Get tests: {} #{}", projectName, buildNumber);

        AbstractBuild build = support.getBuild(projectName, buildNumber);
        AbstractTestResultAction action = build.getTestResultAction();

        if (action == null || action.getTotalCount() == 0) {
            throw new WebApplicationException(NO_CONTENT);
        }

        return testsx.convert(build.getTestResultAction());
    }

    @GET
    @Path("console")
    public ConsoleDTO getConsole(final @PathParam("projectName") String projectName, final @PathParam("buildNumber") int buildNumber) throws IOException {
        log.debug("Get console: {} #{}", projectName, buildNumber);

        AbstractBuild build = support.getBuild(projectName, buildNumber);
        File file = build.getLogFile();

        ConsoleDTO target = new ConsoleDTO();
        target.setExists(file.exists());
        if (file.exists()) {
            target.setLength(file.length());
            target.setLastModified(file.lastModified());
        }

        return target;
    }

    @GET
    @Path("console/content")
    @Produces({ TEXT_PLAIN })
    public InputStream getConsoleContent(final @PathParam("projectName") String projectName,
                                         final @PathParam("buildNumber") int buildNumber,
                                         final @QueryParam("offset") long offset,
                                         final @QueryParam("length") long length)
        throws IOException
    {
        log.debug("Get console content: {} #{} ({}:{})", $(projectName, buildNumber, offset, length));

        //
        // FIXME: This may include serialized annotation crap, need to resolve...
        //

        AbstractBuild build = support.getBuild(projectName, buildNumber);
        File file = build.getLogFile();

        return new OffsetLimitInputStream(new FileInputStream(file), offset, length);
    }
}
