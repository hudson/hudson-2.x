/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.hudsonci.rest.client.ext;

import org.hudsonci.rest.model.build.BuildDTO;
import org.hudsonci.rest.model.build.ChangesDTO;
import org.hudsonci.rest.model.build.TestsDTO;
import org.hudsonci.rest.model.build.BuildEventDTO;
import org.hudsonci.rest.model.build.ConsoleDTO;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.hudsonci.rest.client.HudsonClient;

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
