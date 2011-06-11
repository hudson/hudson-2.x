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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import hudson.init.InitMilestone;
import hudson.model.Hudson;

import org.hudsonci.rest.api.status.StatusConverter;
import org.hudsonci.rest.api.status.StatusResource;
import org.hudsonci.rest.api.user.UserConverter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import org.hudsonci.rest.model.InitLevelDTO;
import org.hudsonci.rest.model.StatusDTO;
import org.hudsonci.service.NodeService;
import org.hudsonci.service.ProjectService;
import org.hudsonci.service.SecurityService;
import org.hudsonci.service.SystemService;

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
