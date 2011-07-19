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

import org.eclipse.hudson.service.ProjectNotFoundException;
import org.eclipse.hudson.service.ProjectService;
import org.eclipse.hudson.rest.api.project.ProjectConverter;
import org.eclipse.hudson.rest.api.project.ProjectResource;
import org.eclipse.hudson.rest.model.project.ProjectDTO;
import hudson.model.AbstractProject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProjectResourceTest
{
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final String TEST_UUID_STRING = "75eee622-c1e7-4581-8320-de84eda90277";

    private static final UUID TEST_UUID_ORIG = UUID.fromString(TEST_UUID_STRING);

    @Mock
    private ProjectService projService;

    @Test(expected=ProjectNotFoundException.class)
    public void testGetProjectThrowsProjectNotFoundExceptionWhenNonNullProjectIdNotFound()
    {
        ProjectConverter projectx = mock(ProjectConverter.class);
        // null return is the default for a mocked method but being explicit
        // about what is being tested helps
        when(this.projService.getProject(TEST_UUID_ORIG)).thenThrow(new ProjectNotFoundException("blah"));
        ProjectResource res = new ProjectResource(projService, projectx);
        // won't be found expect exception
        res.getProject(TEST_UUID_STRING);
    }

    @Test(expected = NullPointerException.class)
    public void testGetProjectNullProjectId()
    {
        ProjectConverter projectx = mock(ProjectConverter.class);
        ProjectResource res = new ProjectResource(projService, projectx);
        res.getProject(null);
    }

    @Test
    public void testGetProjectByValidIdIsFound()
    {
        // mock the minimum needed to return a DTO, don't bother test converters here
        // test converters in converter tests
        ProjectDTO dto = mock(ProjectDTO.class, RETURNS_SMART_NULLS);
        AbstractProject absProj = mock(AbstractProject.class, RETURNS_SMART_NULLS);

        when(this.projService.getProject(isA(UUID.class))).thenReturn(absProj);

        ProjectConverter projectx = mock(ProjectConverter.class, RETURNS_SMART_NULLS);
        when(projectx.convert(isA(AbstractProject.class))).thenReturn(dto);

        ProjectResource res = new ProjectResource(projService, projectx);

        ProjectDTO retdto = res.getProject(TEST_UUID_STRING);
        assertNotNull(retdto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetProjectByBadlyFormattedProjectId()
    {
        ProjectConverter projectx = mock(ProjectConverter.class);
        ProjectResource res = new ProjectResource(projService, projectx);
        res.getProject("not-a-real-uuid");
    }
}
