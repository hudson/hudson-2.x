/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.builder.rest;

import static com.sonatype.matrix.rest.common.RestPreconditions.*;
import static javax.ws.rs.core.MediaType.*;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;

import static com.google.common.base.Preconditions.checkNotNull;
import com.sonatype.matrix.maven.model.config.BuildConfigurationDTO;
import com.sonatype.matrix.maven.plugin.Constants;
import com.sonatype.matrix.maven.plugin.builder.MavenBuilder;
import com.sonatype.matrix.maven.plugin.builder.MavenBuilderService;
import com.sonatype.matrix.rest.common.ProjectNameCodec;

/**
 * Provides access to {@link MavenBuilder}'s {@link BuildConfigurationDTO} resources.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@Path(Constants.URI_PREFIX + "/builderConfig/{projectName}")
@Produces({APPLICATION_JSON, APPLICATION_XML})
public class BuilderConfigResource
{
    private final MavenBuilderService mavenBuilderService;

    private final ProjectNameCodec projectNameCodec;

    @Inject
    public BuilderConfigResource(final MavenBuilderService mavenBuilderService, final ProjectNameCodec projectNameCodec) {
        this.mavenBuilderService = checkNotNull(mavenBuilderService);
        this.projectNameCodec = checkNotNull(projectNameCodec);
    }

    @GET
    public List<BuildConfigurationDTO> getBuilderConfigurations(final @PathParam("projectName") String projectName) {
        checkProjectName(projectName);
        return mavenBuilderService.getBuilderConfigurations(projectNameCodec.decode(projectName));
    }

    /**
     * @return builder configuration for the project at the specified index
     * @throws WebApplicationException status 404 when the project does not exist, project exists but the index provided
     * does not represent a builder OR status 400 if the project exists and the builder index is negative
     */
    @GET
    @Path("{index}")
    public BuildConfigurationDTO getBuilderConfiguration(final @PathParam("projectName") String projectName, final @PathParam("index") int index) {
        checkProjectName(projectName);
        checkBuilderIndex(index);
        return mavenBuilderService.getBuilderConfiguration(projectNameCodec.decode(projectName), index);
    }

    @PUT
    @Path("{index}")
    public void setBuilderConfiguration(final @PathParam("projectName") String projectName,
                                        final @PathParam("index") int index,
                                        final BuildConfigurationDTO config)
        throws IOException
    {
        checkProjectName(projectName);
        checkBuilderIndex(index);
        checkNotNull(config, BuildConfigurationDTO.class);

        mavenBuilderService.setBuilderConfiguration(projectName, index, config);

        // let fault catch IOExceptions
    }
}
