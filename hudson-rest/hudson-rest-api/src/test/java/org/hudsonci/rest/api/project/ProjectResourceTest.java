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

import org.hudsonci.rest.model.project.ProjectDTO;
import org.hudsonci.service.ProjectNotFoundException;
import org.hudsonci.service.ProjectService;
import hudson.model.AbstractProject;

import org.hudsonci.rest.api.project.ProjectConverter;
import org.hudsonci.rest.api.project.ProjectResource;
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
