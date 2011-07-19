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

import com.google.common.base.Preconditions;

import org.eclipse.hudson.rest.client.ext.maven.BuilderDefaultConfigClient;
import org.eclipse.hudson.rest.client.internal.HudsonClientExtensionSupport;
import org.eclipse.hudson.maven.model.config.BuildConfigurationDTO;
import com.sun.jersey.api.client.ClientResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;


import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
/**
 *
 * @author plynch
 */
public class BuilderDefaultConfigClientImpl
        extends HudsonClientExtensionSupport
        implements BuilderDefaultConfigClient {

    @Override
    protected UriBuilder uri() {
        return getClient().uri().path("plugin/maven3-plugin").path("builderDefaultConfig");
    }

    public BuildConfigurationDTO getBuilderDefaultConfiguration() {
        ClientResponse resp = resource(uri()).get(ClientResponse.class);
        try {
            ensureStatus(resp, OK);
            return resp.getEntity(BuildConfigurationDTO.class);
        }
        finally {
            close(resp);
        }
    }

    public void setBuilderDefaultConfiguration(BuildConfigurationDTO defaults) {
        Preconditions.checkNotNull(defaults);
        ClientResponse resp = resource(uri()).type(MediaType.APPLICATION_XML).put(ClientResponse.class,defaults);
        try {
            ensureStatus(resp, NO_CONTENT);
        }
        finally {
            close(resp);
        }
    }

    public void resetBuilderDefaultConfiguration() {
        ClientResponse resp = resource(uri()).delete(ClientResponse.class);
        try {
            ensureStatus(resp, NO_CONTENT);
        }
        finally {
            close(resp);
        }
    }
}
