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

package org.eclipse.hudson.rest.client.ext;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.eclipse.hudson.rest.client.HudsonClient;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_XML;

/**
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Path("admin")
@Produces({APPLICATION_JSON, APPLICATION_XML})
public interface AdminClient
    extends HudsonClient.Extension
{
    @GET
    @Path("config")
    @Produces({TEXT_XML})
    String getConfig();

    @GET
    @Path("quiet-down")
    void quietDown(@QueryParam("toggle") boolean toggle);

    @GET
    @Path("config/reload")
    void reloadConfig();

    @GET
    @Path("restart")
    void restart(@QueryParam("safe") boolean safe);
}
