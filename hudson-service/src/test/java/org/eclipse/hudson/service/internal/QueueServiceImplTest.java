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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.*;
import hudson.model.Hudson;

import org.eclipse.hudson.service.SecurityService;
import org.eclipse.hudson.service.internal.QueueServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Hudson.class})
public class QueueServiceImplTest {

    private Hudson hudson;

    private QueueServiceImpl queueService;

    @Mock
    private SecurityService security;

    @Before
    public void setUp() throws Exception {
        mockStatic(Hudson.class); // static methods
        hudson = mock(Hudson.class); // final and native

        MockitoAnnotations.initMocks(this);
        queueService = new QueueServiceImpl(security);
        queueService.setHudson(hudson);
    }

    private QueueServiceImpl getInst() {
        return queueService;
    }

    @Test
    public void setupProperly() {
        assertThat(getInst(),notNullValue());
        assertThat(hudson,notNullValue());
    }

    @Test
    public void getQueueSecurity() {
        getInst().getQueue();
        Mockito.verify(security).checkPermission(Hudson.ADMINISTER);
    }

}
