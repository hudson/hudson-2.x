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

package org.hudsonci.rest.api.internal;

import org.hudsonci.service.SystemService;
import javax.inject.Inject;
import hudson.init.InitMilestone;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hudsonci.rest.common.Constants;

import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.*;
import static org.hudsonci.rest.common.Constants.HUDSON_HEADER;

/**
 * Resource to help clients figure out if they can talk to the server or not.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Path("handshake")
@Produces({TEXT_PLAIN})
public class HandshakeResource
    extends ResourceSupport
{
    private final SystemService systemService;

    @Inject
    public HandshakeResource(final SystemService systemService)
    {
        this.systemService = systemService;
        String versions = System.getProperty(getClass().getName() + ".acceptVersionsOverride");
        if (versions != null) {
            log.warn("Override accept versions: {}", versions);
            acceptVersions = versions.split(",");
        }
        else {
            acceptVersions =  new String[] { getApiVersion() };
        }
    }

    //
    // FIXME: May actually want to implement this as in the RestServlet, so that its forced on all requests?
    // FIXME: ... or maybe that is bad... I'm not sure.  Probably also need validation on the client to make
    // FIXME: ... sure that a servers response is a supported version.
    // FIXME: Maybe need to implement a serial version ID on each REST object to determine readability?
    //

    private final String[] acceptVersions;

    private String getApiVersion() {
        return org.hudsonci.rest.api.Version.get().getVersion();
    }

    @GET
    public String handshake(final @HeaderParam(HUDSON_HEADER) String details) {
        log.debug("Handshake: {}", details);

        if (details == null) {
            // If there is no details about our client, then don't even bother
            throw new WebApplicationException(BAD_REQUEST);
        }

        if (systemService.getInitLevel() != InitMilestone.COMPLETED) {
            // Hudson has not finished initializing yet
            throw new WebApplicationException(SERVICE_UNAVAILABLE);
        }

        //
        // FIXME: This is fairly crappy handshaking...
        //

        String id = null;
        String version = null;

        // Pick off the Client's ID and its version
        String[] parts = details.split(";");
        for (String part : parts) {
            if (part.startsWith("client=")) {
                version = part.substring(7, part.length());
            }
            if (part.startsWith("id=")) {
                id = part.substring(3, part.length());
            }
        }

        // If we have an ID and version then validate the version and return the ID for success
        if (id != null && version != null) {
            for (String acceptVersion : acceptVersions) {
                if (acceptVersion.equals(version)) {
                    // Successful handshake response
                    return id;
                }
            }

            // Bad version, puke
            throw new WebApplicationException(Response.status(CONFLICT).type(TEXT_PLAIN).entity(String.format("Client version is unsupported: %s", version)).build());
        }

        // No ID, no version... no access, handshake protocol violation
        throw new WebApplicationException(BAD_REQUEST);
    }
}
