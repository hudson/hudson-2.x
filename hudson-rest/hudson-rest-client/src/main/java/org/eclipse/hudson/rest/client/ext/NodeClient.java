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

import org.eclipse.hudson.rest.client.HudsonClient;
import org.eclipse.hudson.rest.model.NodeDTO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;


import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

/**
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Path("nodes")
@Produces({APPLICATION_JSON, APPLICATION_XML})
public interface NodeClient
    extends HudsonClient.Extension
{
    @GET
    List<NodeDTO> getNodes();

    @GET
    @Path("{nodeName}")
    NodeDTO getNode(@PathParam("nodeName") String nodeName);
}
