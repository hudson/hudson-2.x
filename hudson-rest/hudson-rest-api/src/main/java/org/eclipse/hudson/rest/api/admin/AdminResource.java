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

package org.eclipse.hudson.rest.api.admin;

import hudson.XmlFile;
import hudson.lifecycle.RestartNotSupportedException;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.eclipse.hudson.rest.api.internal.ResourceSupport;
import org.eclipse.hudson.service.SystemService;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.MediaType.TEXT_XML;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

/**
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Path("/admin")
public class AdminResource
    extends ResourceSupport
{
    private final SystemService systemService;

    @Inject
    public AdminResource(final SystemService systemService) {
        this.systemService = checkNotNull(systemService);
    }

    @GET
    @Path("config")
    @Produces({TEXT_XML})
    public Response getConfig() {
        XmlFile file = systemService.getConfigFile();
        log.debug("Config file: {}", file);

        if (!file.exists()) {
            throw new WebApplicationException(NOT_FOUND); // FIXME: "System configuration has not yet been saved to disk");
        }

        try {
            String xml = file.asString();
            return Response.ok(xml).build();
        }
        catch (IOException e) {
            throw new WebApplicationException(e);
        }
    }

    // TODO: Add @POST to update the configuration

    @GET
    @Path("quiet-down")
    public Response quietDown(final @QueryParam("toggle") @DefaultValue("true") boolean toggle) {
        systemService.doQuietDown(toggle);
        return Response.noContent().build();
    }

    @GET
    @Path("config/reload")
    public Response reloadConfiguration() throws IOException {
        systemService.doReload();
        return Response.noContent().build();
    }

    @GET
    @Path("restart")
    public Response restart(final @QueryParam("safe") @DefaultValue("false") boolean safe) throws RestartNotSupportedException {
        systemService.doRestart(safe);
        return Response.noContent().build();
    }

    // TODO: Add stop/abort|kill and make sure that restart bits work correctly
}

