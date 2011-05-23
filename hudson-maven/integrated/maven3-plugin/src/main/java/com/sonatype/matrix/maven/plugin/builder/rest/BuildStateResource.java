/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.builder.rest;

import com.sonatype.matrix.maven.model.state.BuildStateDTO;
import com.sonatype.matrix.maven.model.state.BuildStatesDTO;
import com.sonatype.matrix.maven.plugin.Constants;
import com.sonatype.matrix.maven.plugin.builder.BuildStateNotFoundException;
import com.sonatype.matrix.maven.plugin.builder.MavenBuilderService;
import com.sonatype.matrix.rest.common.ProjectNameCodec;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sonatype.matrix.rest.common.RestPreconditions.checkBuildNumber;
import static com.sonatype.matrix.rest.common.RestPreconditions.checkBuilderIndex;
import static com.sonatype.matrix.rest.common.RestPreconditions.checkProjectName;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

/**
 * Provides access to {@link BuildStateDTO} resources.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
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

