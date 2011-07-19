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

package org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo;

import org.eclipse.hudson.maven.model.state.BuildStatesDTO;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Rest service to retrieve build states.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
@Path("buildState")
public interface BuildStateService
    extends RestService
{
    @Path("{projectName}/{buildNumber}")
    @GET
    public void getBuildStates(@PathParam("projectName") String projectName,
                               @PathParam("buildNumber") int buildNumber, MethodCallback<BuildStatesDTO> callback);
}
