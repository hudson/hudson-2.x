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

package org.hudsonci.rest.api.project;

import org.hudsonci.rest.model.project.ProjectsDTO;
import org.hudsonci.service.ProjectService;
import org.hudsonci.service.SecurityService;
import org.hudsonci.service.SystemService;
import org.hudsonci.service.internal.ProjectServiceImpl;

import org.hudsonci.rest.api.build.BuildConverter;
import org.hudsonci.rest.api.internal.PermissionsFactory;
import org.hudsonci.rest.api.internal.ProjectBuildHelper;
import org.hudsonci.rest.api.project.ProjectConverter;
import org.hudsonci.rest.api.project.ProjectResource;
import org.hudsonci.rest.api.project.ProjectsResource;
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
