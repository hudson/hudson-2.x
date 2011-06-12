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

import static org.hudsonci.rest.common.RestPreconditions.*;
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

import org.hudsonci.maven.plugin.Constants;
import org.hudsonci.maven.plugin.builder.MavenBuilder;
import org.hudsonci.maven.plugin.builder.MavenBuilderService;

import static com.google.common.base.Preconditions.checkNotNull;
import org.hudsonci.maven.model.config.BuildConfigurationDTO;
import org.hudsonci.rest.common.ProjectNameCodec;

/**
 * Provides access to {@link MavenBuilder}'s {@link BuildConfigurationDTO} resources.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
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
