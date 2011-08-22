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

package org.eclipse.hudson.maven.plugin.builder.rest;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.eclipse.hudson.maven.model.state.BuildStateDTO;
import org.eclipse.hudson.maven.model.state.BuildStatesDTO;
import org.eclipse.hudson.maven.plugin.Constants;
import org.eclipse.hudson.maven.plugin.builder.BuildStateNotFoundException;
import org.eclipse.hudson.maven.plugin.builder.MavenBuilderService;
import org.eclipse.hudson.rest.common.ProjectNameCodec;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static org.eclipse.hudson.rest.common.RestPreconditions.checkBuildNumber;
import static org.eclipse.hudson.rest.common.RestPreconditions.checkBuilderIndex;
import static org.eclipse.hudson.rest.common.RestPreconditions.checkProjectName;

/**
 * Provides access to {@link BuildStateDTO} resources.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
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

