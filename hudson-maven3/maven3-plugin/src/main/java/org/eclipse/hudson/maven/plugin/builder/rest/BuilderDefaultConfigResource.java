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

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.eclipse.hudson.maven.model.config.BuildConfigurationDTO;
import org.eclipse.hudson.maven.plugin.Constants;
import org.eclipse.hudson.maven.plugin.builder.MavenBuilderDescriptor;
import org.eclipse.hudson.maven.plugin.builder.MavenBuilderService;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

/**
 * Provides access to {@link MavenBuilderDescriptor}'s default {@link BuildConfigurationDTO} resources.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
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

