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

package org.hudsonci.maven.plugin.builder.rest;

import org.hudsonci.maven.model.state.BuildStateDTO;
import org.hudsonci.maven.model.state.BuildStatesDTO;
import org.hudsonci.rest.common.ProjectNameCodec;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.hudsonci.maven.plugin.Constants;
import org.hudsonci.maven.plugin.builder.BuildStateNotFoundException;
import org.hudsonci.maven.plugin.builder.MavenBuilderService;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hudsonci.rest.common.RestPreconditions.checkBuildNumber;
import static org.hudsonci.rest.common.RestPreconditions.checkBuilderIndex;
import static org.hudsonci.rest.common.RestPreconditions.checkProjectName;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

/**
 * Provides access to {@link BuildStateDTO} resources.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Path(Constants.URI_PREFIX + "/buildState/{projectName}/{buildNumber:\\d*}")
@Produces({APPLICATION_JSON, APPLICATION_XML})
public class BuildStateResource
{
    private final MavenBuilderService mavenBuilderService;

    private final ProjectNameCodec projectNameCodec;

    @Inject
    public BuildStateResource(final MavenBuilderService mavenBuilderService, final ProjectNameCodec projectNameCodec) {
        this.mavenBuilderService = checkNotNull(mavenBuilderService);
        this.projectNameCodec = checkNotNull(projectNameCodec);
    }

    @GET
    public BuildStatesDTO getBuildStates(final @PathParam("projectName") String projectName,
                                         final @PathParam("buildNumber") int buildNumber)
    {
        checkProjectName(projectName);
        checkBuildNumber(buildNumber);
        List<BuildStateDTO> states = mavenBuilderService.getBuildStates(projectNameCodec.decode(projectName), buildNumber);
        if (states.isEmpty()) {
            throw new BuildStateNotFoundException(projectName, buildNumber);
        }
        return new BuildStatesDTO().withStates(states);
    }

    @GET
    @Path("{index}")
    public BuildStateDTO getBuildState(final @PathParam("projectName") String projectName,
                                       final @PathParam("buildNumber") int buildNumber,
                                       final @PathParam("index") int index)
    {
        checkProjectName(projectName);
        checkBuildNumber(buildNumber);
        checkBuilderIndex(index);
        return mavenBuilderService.getBuildState(projectNameCodec.decode(projectName), buildNumber, index);
    }
}

