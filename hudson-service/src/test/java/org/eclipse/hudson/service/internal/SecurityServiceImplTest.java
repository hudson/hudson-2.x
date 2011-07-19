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

package org.eclipse.hudson.service.internal;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import hudson.security.AccessControlled;
import hudson.security.Permission;

import org.eclipse.hudson.service.SecurityService;
import org.eclipse.hudson.service.internal.SecurityServiceImpl;
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
