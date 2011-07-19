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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import hudson.init.InitMilestone;
import hudson.model.Hudson;

import org.eclipse.hudson.service.NodeService;
import org.eclipse.hudson.service.ProjectService;
import org.eclipse.hudson.service.SecurityService;
import org.eclipse.hudson.service.SystemService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import org.eclipse.hudson.rest.api.status.StatusConverter;
import org.eclipse.hudson.rest.api.status.StatusResource;
import org.eclipse.hudson.rest.api.user.UserConverter;
import org.eclipse.hudson.rest.model.InitLevelDTO;
import org.eclipse.hudson.rest.model.StatusDTO;

@RunWith(MockitoJUnitRunner.class)
public class StatusResourceTest
{
    @Mock
    private SystemService systemService;

    @Mock
    private SecurityService securityService;

    @Mock
    private StatusConverter statusx;

    @Mock
    private UserConverter userx;

    private SystemService mockSystemService()
    {
        when(this.systemService.getUrl()).thenReturn(
                "http://localhost:1234");
        when(this.systemService.getInitLevel()).thenReturn(
                InitMilestone.COMPLETED);
        when(this.systemService.getSystemMessage()).thenReturn("OK");
        when(this.systemService.getVersion()).thenReturn("1.1,2010");

        return systemService;
    }

    @Test
    public void testGetStatusSecurity()
    {
        StatusResource res = new StatusResource(mockSystemService(),
                securityService, statusx, userx);
        StatusDTO dto = res.getStatus();
        verify(securityService, Mockito.times(1)).checkPermission(
                Hudson.READ);
    }

}
