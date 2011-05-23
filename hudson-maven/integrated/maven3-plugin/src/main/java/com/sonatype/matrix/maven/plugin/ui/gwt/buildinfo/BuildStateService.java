/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */

package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo;

import com.sonatype.matrix.maven.model.state.BuildStatesDTO;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Rest service to retrieve build states.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
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
