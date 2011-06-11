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

package org.hudsonci.rest.api.user;

import com.google.common.base.Preconditions;
import hudson.model.User;
import hudson.security.Permission;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;


import javax.inject.Inject;

import org.hudsonci.rest.api.internal.ResourceSupport;

import org.hudsonci.rest.model.UserDTO;
import org.hudsonci.service.SecurityService;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
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
