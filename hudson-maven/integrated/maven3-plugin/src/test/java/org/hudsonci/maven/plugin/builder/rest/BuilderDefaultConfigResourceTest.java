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

package org.hudsonci.maven.plugin.builder.rest;

import org.hudsonci.maven.model.config.BuildConfigurationDTO;
import org.hudsonci.service.BuildService;
import org.hudsonci.service.DescriptorService;
import org.hudsonci.service.ProjectService;
import org.hudsonci.service.SecurityService;

import org.hudsonci.maven.plugin.builder.MavenBuilderDescriptor;
import org.hudsonci.maven.plugin.builder.MavenBuilderService;
import org.hudsonci.maven.plugin.builder.internal.MavenBuilderServiceImpl;
import org.hudsonci.maven.plugin.builder.rest.BuilderDefaultConfigResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link BuilderDefaultConfigResource}
 * @author plynch
 */
@RunWith(MockitoJUnitRunner.class)
public class BuilderDefaultConfigResourceTest {

    @Mock
    private DescriptorService descriptorService;

    @Mock
    private SecurityService securityService;

    @Mock
    private ProjectService projectService;

    @Mock
    private BuildService buildService;

    @Mock
    private MavenBuilderDescriptor descriptor;

    @Mock
    private BuildConfigurationDTO dto;

    private BuilderDefaultConfigResource instance;

    @Before
    public void setUp() throws Exception {
        when(descriptorService.getDescriptorByType(MavenBuilderDescriptor.class)).thenReturn(descriptor);
        when(descriptor.getDefaults()).thenReturn(dto);
        MavenBuilderService mavenBuilderService = new MavenBuilderServiceImpl(securityService, descriptorService, projectService, buildService);
        instance = new BuilderDefaultConfigResource(mavenBuilderService);
    }

    /**
    * Test of getBuilderDefaultConfiguration method, of class BuilderDefaultConfigResource.
    */
    @Test
    public void getBuilderDefaultConfigurationReturnsServiceDefault() {
        BuildConfigurationDTO result = instance.getBuilderDefaultConfiguration();
        assertEquals(dto, result);
    }

    @Test
    public void setBuilderDefaultConfigurationSetsSpecifiedDefault() {
        instance.setBuilderDefaultConfiguration(dto);
        verify(descriptor, times(1)).setDefaults(dto);
        verifyNoMoreInteractions(descriptor);
    }

    @Test(expected=NullPointerException.class)
    public void setBuilderDefaultConfigurationDefaultsNullArg() {
        instance.setBuilderDefaultConfiguration(null);
        verifyZeroInteractions(descriptorService);
    }

    @Test
    public void resetBuilderDefaultConfigurationSetsDescriptorDefaults() {
        instance.resetBuilderDefaultConfiguration();
        verify(descriptor, times(1)).setDefaults(MavenBuilderDescriptor.DEFAULTS);
        verifyNoMoreInteractions(descriptor);
    }
}
