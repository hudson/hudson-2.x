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

package org.hudsonci.rest.api.status;

import hudson.model.Hudson;
import hudson.model.User;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import javax.inject.Inject;
import javax.inject.Named;

import org.hudsonci.rest.api.internal.ResourceSupport;
import org.hudsonci.rest.api.user.UserConverter;

import org.hudsonci.rest.model.StatusDTO;
import org.hudsonci.service.SecurityService;
import org.hudsonci.service.SystemService;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides the status of the server.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Path("status")
public class StatusResource
        extends ResourceSupport
{
    private final SystemService systemService;

    private final SecurityService securityService;

    private final StatusConverter statusx;

    private final UserConverter userx;

    @Inject
    public StatusResource(final SystemService systemService,
                          final SecurityService securityService, StatusConverter statusx,
                          final UserConverter userx)
    {
        this.systemService = checkNotNull(systemService);
        this.securityService = checkNotNull(securityService);
        this.statusx = checkNotNull(statusx);
        this.userx = checkNotNull(userx);
    }

    @GET
    public StatusDTO getStatus() {
        log.debug("Returning status");

        // feels like should be an aspect?
        securityService.checkPermission(Hudson.READ);

        StatusDTO status = new StatusDTO();
        status.setUrl(systemService.getUrl());
        status.setVersion(systemService.getVersion());
        status.setInitLevel(statusx.convert(systemService.getInitLevel()));
        status.setQuietingDown(systemService.isQuietingDown());
        status.setTerminating(systemService.isTerminating());
        status.setSystemMessage(systemService.getSystemMessage());

        User user = securityService.getCurrentUser();
        if (user != null) {
            status.setUser(userx.convert(user));
        }

        return status;
    }
}
