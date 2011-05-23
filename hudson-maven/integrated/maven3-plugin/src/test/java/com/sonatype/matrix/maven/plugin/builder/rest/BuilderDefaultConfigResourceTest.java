/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.builder.rest;

import com.sonatype.matrix.maven.model.config.BuildConfigurationDTO;
import com.sonatype.matrix.maven.plugin.builder.MavenBuilderDescriptor;
import com.sonatype.matrix.maven.plugin.builder.MavenBuilderService;
import com.sonatype.matrix.maven.plugin.builder.internal.MavenBuilderServiceImpl;
import com.sonatype.matrix.service.BuildService;
import com.sonatype.matrix.service.DescriptorService;
import com.sonatype.matrix.service.ProjectService;
import com.sonatype.matrix.service.SecurityService;
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