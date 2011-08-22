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

package org.eclipse.hudson.rest.api.user;

import hudson.model.User;
import hudson.security.Permission;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.eclipse.hudson.rest.api.internal.ResourceSupport;
import org.eclipse.hudson.rest.model.UserDTO;
import org.eclipse.hudson.service.SecurityService;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Path("/users")
public class UserResource
    extends ResourceSupport
{
    private final SecurityService securityService;

    private final UserConverter userx;

    @Inject
    public UserResource(final SecurityService securityService, final UserConverter userx) {
        this.securityService = checkNotNull(securityService);
        this.userx = checkNotNull(userx);
    }

    @GET
    @Path("{userName}")
    public UserDTO getUser(final @PathParam("userName") String userName) {
        checkNotNull(userName);

        log.debug("Getting user: {}", userName);
        User user = securityService.getUser(userName);
        log.debug("Getting user with ID: {}", user.getId());
        user.checkPermission(Permission.READ);

        return userx.convert(user);
    }
}
