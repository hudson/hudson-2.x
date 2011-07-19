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
import hudson.model.Hudson.MasterComputer;
import hudson.util.RemotingDiagnostics;

import org.eclipse.hudson.service.SecurityService;
import org.eclipse.hudson.service.internal.ScriptServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Hudson.class, MasterComputer.class, RemotingDiagnostics.class })
public class ScriptServiceImplTest {

    private Hudson hudson;

    private ScriptServiceImpl scriptService;

    @Mock
    private SecurityService security;

    @Before
    public void setUp() throws Exception {
        mockStatic(Hudson.class); // static methods
        hudson = mock(Hudson.class); // final and native

        mockStatic(RemotingDiagnostics.class);
        mockStatic(MasterComputer.class);

        MockitoAnnotations.initMocks(this);
        scriptService = new ScriptServiceImpl(security);
        scriptService.setHudson(hudson);
    }

    private ScriptServiceImpl getInst() {
        return scriptService;
    }

    @Test
    public void setupProperly() {
        assertThat(getInst(), notNullValue());
        assertThat(hudson, notNullValue());
    }

    @Test(expected = NullPointerException.class)
    public void executeArgNull() throws Exception {
        getInst().execute(null);
    }

    @Test
    public void executeSecurity() throws Exception {
        getInst().execute("String bogus");
        Mockito.verify(security).checkPermission(Hudson.ADMINISTER);
    }

}
