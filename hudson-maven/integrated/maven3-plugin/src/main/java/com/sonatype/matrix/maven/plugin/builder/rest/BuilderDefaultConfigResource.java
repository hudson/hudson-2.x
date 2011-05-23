/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.builder.rest;

import com.sonatype.matrix.maven.model.config.BuildConfigurationDTO;
import com.sonatype.matrix.maven.plugin.Constants;
import com.sonatype.matrix.maven.plugin.builder.MavenBuilderDescriptor;
import com.sonatype.matrix.maven.plugin.builder.MavenBuilderService;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

/**
 * Provides access to {@link MavenBuilderDescriptor}'s default {@link BuildConfigurationDTO} resources.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@Path(Constants.URI_PREFIX + "/builderDefaultConfig")
@Produces({APPLICATION_JSON, APPLICATION_XML})
public class BuilderDefaultConfigResource
{
    private final MavenBuilderService mavenBuilderService;

    @Inject
    public BuilderDefaultConfigResource(final MavenBuilderService mavenBuilderService) {
        this.mavenBuilderService = checkNotNull(mavenBuilderService);
    }

    @GET
    public BuildConfigurationDTO getBuilderDefaultConfiguration() {
        return mavenBuilderService.getBuilderDefaultConfiguration();
    }

    @PUT
    public void setBuilderDefaultConfiguration(final BuildConfigurationDTO defaults) {
        mavenBuilderService.setBuilderDefaultConfiguration(defaults);
    }

    @DELETE
    public void resetBuilderDefaultConfiguration() {
        mavenBuilderService.resetBuilderDefaultConfiguration();
    }
}

