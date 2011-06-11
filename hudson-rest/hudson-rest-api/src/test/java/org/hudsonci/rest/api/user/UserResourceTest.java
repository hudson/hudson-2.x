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

import hudson.model.User;
import hudson.security.Permission;

import org.hudsonci.rest.api.user.UserConverter;
import org.hudsonci.rest.api.user.UserResource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import org.hudsonci.rest.model.UserDTO;
import org.hudsonci.service.NodeService;
import org.hudsonci.service.ProjectService;
import org.hudsonci.service.SecurityService;
import org.hudsonci.service.SystemService;

@RunWith(MockitoJUnitRunner.class)
public class UserResourceTest
{
    @Mock
    private User hudsonUser;

    @Mock
    private SecurityService securityService;

    @Mock
    private UserConverter userx;

    @Test
    public void testGetUserSecurity()
    {
        when(securityService.getUser("foo")).thenReturn(
                this.hudsonUser);

        UserResource res = new UserResource(securityService,userx);
        UserDTO dto = res.getUser("foo");
        
        verify(this.hudsonUser, times(1)).
                checkPermission(Permission.READ);
    }
}
