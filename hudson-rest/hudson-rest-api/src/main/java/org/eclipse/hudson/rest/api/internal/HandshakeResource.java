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

package org.eclipse.hudson.rest.api.internal;

import hudson.init.InitMilestone;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.eclipse.hudson.service.SystemService;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import static org.eclipse.hudson.rest.common.Constants.HUDSON_HEADER;

/**
 * Resource to help clients figure out if they can talk to the server or not.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
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
        return org.eclipse.hudson.rest.api.Version.get().getVersion();
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
        // FIXME: This is a fairly  ineffective handshaking...
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
