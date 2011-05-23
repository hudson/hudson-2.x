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

import hudson.model.Hudson;

import org.hudsonci.service.SecurityService;
import org.hudsonci.service.SystemService;
import org.hudsonci.service.internal.SystemServiceImpl;
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
