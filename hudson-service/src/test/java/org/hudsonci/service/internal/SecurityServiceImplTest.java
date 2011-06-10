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

package org.hudsonci.service.internal;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import hudson.security.AccessControlled;
import hudson.security.Permission;

import org.hudsonci.service.SecurityService;
import org.hudsonci.service.internal.SecurityServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SecurityServiceImplTest {

    @Mock private AccessControlled ac;

    private SecurityService securityService;

    @Before
    public void setup(){
        securityService = new SecurityServiceImpl();
    }

    @Test(expected=NullPointerException.class)
    public void checkPermissionNullArg1(){
        assertNotNull(securityService);
        securityService.checkPermission(null);
    }

    @Test(expected=NullPointerException.class)
    public void checkPermissionAccessControlledNullArg1(){
       assertNotNull(securityService);
       securityService.checkPermission(null, Permission.DELETE);
    }

    @Test(expected=NullPointerException.class)
    public void checkPermissionAccessControlledNullArg2(){
       assertNotNull(securityService);
       assertNotNull(ac);
       securityService.checkPermission(ac, null);
    }

    @Test
    public void checkPermissionAccessControlled(){
      securityService.checkPermission(ac, Permission.DELETE);
      verify(ac).checkPermission(Permission.DELETE);
    }

    @Test(expected=NullPointerException.class)
    public void hasPermissionAccessControlledNullArg1(){
       assertNotNull(securityService);
       securityService.hasPermission(null, Permission.DELETE);
    }

    @Test(expected=NullPointerException.class)
    public void hasPermissionAccessControlledNullArg2(){
       assertNotNull(securityService);
       assertNotNull(ac);
       securityService.hasPermission(ac, null);
    }

    @Test
    public void hasPermissionAccessControlled(){
      securityService.hasPermission(ac, Permission.DELETE);
      verify(ac).hasPermission(Permission.DELETE);
    }


}
