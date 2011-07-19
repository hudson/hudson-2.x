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

import hudson.model.Hudson;

import org.eclipse.hudson.service.SecurityService;
import org.eclipse.hudson.service.SystemService;
import org.eclipse.hudson.service.internal.SystemServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Tests for {@link SystemServiceImpl}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Hudson.class)
public class SystemServiceImplTest
{

    private Hudson hudson;

    private SystemServiceImpl system;

    @Before
    public void setUp() throws Exception {
        mockStatic(Hudson.class);
        hudson = mock(Hudson.class);

        SecurityService security = mock(SecurityService.class);
        system = new SystemServiceImpl(security);
        system.setHudson(hudson);
    }

    @Test
    public void getUrlReturnsValidWhenHudsonInstanceExists() {
        assertEquals(SystemService.DEFAULT_URL, system.getUrl());
    }

    @Test
    public void getUrlFromHudson() {
        when(system.getUrl()).thenReturn("http://www.foobar.com:8070");
        assertEquals("http://www.foobar.com:8070", system.getUrl());
    }

    @Test
    public void getUrlFromHudsonWithSlashAtEnd() {
        when(system.getUrl()).thenReturn("http://www.foobar.com:8070/");
        assertEquals("http://www.foobar.com:8070", system.getUrl());
    }

    /**
     * Default Url should not end with slash, as if it did getUrl would do extra processing
     */
    @Test
    public void verifyDefaultUrlDoesNotEndWithSlash() {
        assertFalse(SystemService.DEFAULT_URL.endsWith("/"));
    }

    @Test(expected = IllegalStateException.class)
    public void getInstallationDirectoryThrowsIllegalStateException() {
        system.getInstallationDirectory();
    }

    @Test(expected = IllegalStateException.class)
    public void getLogDirectoryThrowsIllegalStateException() {
        system.getLogDirectory();
    }

    @Test
    public void getWorkingDirectory() {
        File file = new File("/foo/biz/bat");
        when(hudson.getRootDir()).thenReturn(file);
        assertEquals(file, system.getWorkingDirectory());
    }

}
