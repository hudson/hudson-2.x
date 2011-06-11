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

package org.hudsonci.rest.api.admin;

import javax.inject.Inject;
import org.hudsonci.service.SecurityService;
import org.hudsonci.service.SystemService;
import hudson.XmlFile;
import hudson.lifecycle.RestartNotSupportedException;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.hudsonci.rest.api.internal.ResourceSupport;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;

/**
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
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

