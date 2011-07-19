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

package org.eclipse.hudson.rest.api.project;

import org.eclipse.hudson.service.ProjectService;
import org.eclipse.hudson.service.SecurityService;
import org.eclipse.hudson.service.SystemService;
import org.eclipse.hudson.service.internal.ProjectServiceImpl;
import org.eclipse.hudson.rest.api.build.BuildConverter;
import org.eclipse.hudson.rest.api.internal.PermissionsFactory;
import org.eclipse.hudson.rest.api.internal.ProjectBuildHelper;
import org.eclipse.hudson.rest.api.project.ProjectConverter;
import org.eclipse.hudson.rest.api.project.ProjectResource;
import org.eclipse.hudson.rest.api.project.ProjectsResource;
import org.eclipse.hudson.rest.model.project.ProjectsDTO;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import hudson.model.Hudson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * {@link ProjectResource} should delegate all permission checks to {@link ProjectServiceImpl} which has it's own 
 * security tests.  If we need REST security tests they should be against the deployed application.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest( Hudson.class )
@PowerMockIgnore("javax.xml.*")
public class ProjectsResourceTest
{
    @Mock
    ProjectConverter projectx;

    @Mock
    BuildConverter buildx;

    @Mock
    PermissionsFactory permissions;

    @Mock(answer = Answers.RETURNS_SMART_NULLS)
    SecurityService securitySystem;

    @Mock(answer = Answers.RETURNS_SMART_NULLS)
    ProjectService projectService;

    @Mock(answer = Answers.RETURNS_SMART_NULLS)
    SystemService systemService;

    @Mock(answer = Answers.RETURNS_SMART_NULLS)
    ProjectBuildHelper support;


    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getProjectsDefaultNotEmpty(){
        ProjectsResource res = new ProjectsResource(support,
                securitySystem, projectService, projectx, buildx,
                permissions);
        ProjectsDTO dto = res.getProjects();
        assertNotNull(dto);
        assertNotNull(dto.getProjects());
        assertEquals( 0, dto.getProjects().size() );
    }
}
