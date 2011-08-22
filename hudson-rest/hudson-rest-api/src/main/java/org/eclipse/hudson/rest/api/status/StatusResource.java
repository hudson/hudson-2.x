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

package org.eclipse.hudson.rest.api.status;

import hudson.model.Hudson;
import hudson.model.User;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.eclipse.hudson.rest.api.internal.ResourceSupport;
import org.eclipse.hudson.rest.api.user.UserConverter;
import org.eclipse.hudson.rest.model.StatusDTO;
import org.eclipse.hudson.service.SecurityService;
import org.eclipse.hudson.service.SystemService;

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
