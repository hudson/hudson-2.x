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
import org.eclipse.hudson.rest.model.build.BuildDTO;
import org.eclipse.hudson.rest.model.build.ChangesDTO;
import org.eclipse.hudson.rest.model.build.TestsDTO;
import org.eclipse.hudson.rest.model.build.BuildEventDTO;
import org.eclipse.hudson.rest.model.build.ConsoleDTO;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;


import java.io.InputStream;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Path("/projects/{projectName}/{buildNumber:\\d*}")
@Produces({APPLICATION_JSON, APPLICATION_XML})
public interface BuildClient
    extends HudsonClient.Extension
{
    // FIXME: Should be on projectclient, due to the path on this intf, it can not live here
    List<BuildDTO> getBuilds(String projectName);

    @GET
    BuildDTO getBuild(@PathParam("projectName") String projectName, @PathParam("buildNumber") int buildNumber);

    @PUT
    @Path("stop")
    void stopBuild(@PathParam("projectName") String projectName, @PathParam("buildNumber") int buildNumber);

    @GET
    @Path("keep")
    void keepBuild(@PathParam("projectName") String projectName, @PathParam("buildNumber") int buildNumber, @QueryParam("release") boolean release);

    @DELETE
    void deleteBuild(@PathParam("projectName") String projectName, @PathParam("buildNumber") int buildNumber);

    @GET
    @Path("changes")
    ChangesDTO getChanges(@PathParam("projectName") String projectName, @PathParam("buildNumber") int buildNumber);

    @GET
    @Path("tests")
    TestsDTO getTests(@PathParam("projectName") String projectName, @PathParam("buildNumber") int buildNumber);

    @GET
    @Path("console")
    ConsoleDTO getConsole(@PathParam("projectName") String projectName, @PathParam("buildNumber") int buildNumber);

    @GET
    @Path("console/content")
    @Produces({ TEXT_PLAIN })
    InputStream getConsoleContent(@PathParam("projectName") String projectName, @PathParam("buildNumber") int buildNumber, @QueryParam("offset") long offset, @QueryParam("length") long length);

    // FIXME: Not really REST stuff
    interface BuildListener
    {
        void buildStarted(BuildEventDTO event);

        void buildStopped(BuildEventDTO event);
    }
    
    void addBuildListener(BuildListener listener);

    void removeBuildListener(BuildListener listener);
}
