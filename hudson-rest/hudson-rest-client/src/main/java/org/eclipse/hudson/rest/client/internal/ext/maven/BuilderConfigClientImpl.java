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

package org.eclipse.hudson.rest.client.internal.ext.maven;

import org.eclipse.hudson.rest.client.ext.maven.BuilderConfigClient;
import org.eclipse.hudson.rest.client.internal.HudsonClientExtensionSupport;
import org.eclipse.hudson.maven.model.config.BuildConfigurationDTO;
import com.sun.jersey.api.client.ClientResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;


import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;

/**
 * Client implementation for {@link BuilderConfigClient}
 */
public class BuilderConfigClientImpl
        extends HudsonClientExtensionSupport
        implements BuilderConfigClient {

    @Override
    protected UriBuilder uri() {
        return getClient().uri().path("plugin/maven3-plugin").path("builderConfig");
    }

    public BuildConfigurationDTO getBuilderConfiguration(final String projectName, final int index) {
        ClientResponse resp = resource(uri().path(projectName).path(String.valueOf(index))).get(ClientResponse.class);
        try {
            ensureStatus(resp, OK);
            return resp.getEntity(BuildConfigurationDTO.class);
        }
        finally {
            close(resp);
        }
    }

    public void setBuilderConfiguration(String projectName, int index, BuildConfigurationDTO config) {
        ClientResponse resp = resource(uri().path(projectName).path(String.valueOf(index))).type(MediaType.APPLICATION_JSON).put(ClientResponse.class,config);
        try {
            ensureStatus(resp, NO_CONTENT);
        }
        finally {
            close(resp);
        }
    }
}
