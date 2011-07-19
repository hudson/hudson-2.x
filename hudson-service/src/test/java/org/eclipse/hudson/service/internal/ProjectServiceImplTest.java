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
import hudson.model.Item;
import hudson.model.TopLevelItem;
import hudson.model.AbstractProject;
import hudson.model.Hudson;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.eclipse.hudson.service.ProjectNotFoundException;
import org.eclipse.hudson.service.ProjectService;
import org.eclipse.hudson.service.SecurityService;
import org.eclipse.hudson.service.ServiceRuntimeException;
import org.eclipse.hudson.service.SystemIntegrityViolationException;
import org.eclipse.hudson.service.internal.ProjectServiceImpl;
import org.eclipse.hudson.utils.tasks.JobUuid;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ Hudson.class, JobUuid.class })
public class ProjectServiceImplTest {

    @Mock
    private SecurityService securityService;

    @SuppressWarnings("rawtypes")
    @Mock
    private AbstractProject project;

    @Mock
    private InputStream input;

    private Hudson hudson;

    private ProjectServiceImpl projectService;

    @Before
    public void setUp() throws Exception {
        mockStatic(Hudson.class);
        hudson = PowerMockito.mock(Hudson.class);
        PowerMockito.mockStatic(JobUuid.class);

        MockitoAnnotations.initMocks(this);
        this.projectService = new ProjectServiceImpl(securityService);
        this.projectService.setHudson(hudson);
        
        assertThat(getInst(), notNullValue());
    }

    private ProjectServiceImpl getInst() {
        return projectService;
    }

    // constructors ---------------

    @SuppressWarnings("unused")
    @Test(expected = NullPointerException.class)
    public void constructorNullArg() {
        new ProjectServiceImpl(null);
    }

    // getProjectByUuid --------------

    @Test(expected = NullPointerException.class)
    public void getProjectByUUIDNullArg() {
        getInst().getProject((UUID) null);
    }

    @Test(expected = ProjectNotFoundException.class)
    public void getProjectByUUIDNotFound() {
        ProjectService inst = spy(getInst());
        UUID id = UUID.randomUUID();
        doReturn(null).when(inst).findProject(id);
        inst.getProject(id);
    }

    // getProjectByFullname ----------------

    @Test(expected = NullPointerException.class)
    public void getProjectByFullnameNullArg() {
        getInst().getProjectByFullName(null);
    }

    @Test(expected = ProjectNotFoundException.class)
    public void getProjectByFullnameProjectNotFoundException() {
        getInst().getProjectByFullName("fullname");
    }

    @Test
    public void getProjectByFullnameSecurity() {
        doReturn(project).when(hudson).getItemByFullName("fullname", AbstractProject.class);

        AbstractProject<?, ?> result = getInst().getProjectByFullName("fullname");

        assertThat(result, equalTo(project));
        Mockito.verify(securityService).checkPermission(project, Item.READ);
    }

    // getProjectByProjectName ---------

    @Test(expected = ProjectNotFoundException.class)
    public void getProjectByProjectNameProjectNotFoundException() {
        getInst().getProject("projectName");
    }

    @Test(expected = NullPointerException.class)
    public void getProjectByProjectNameNullArg() {
        getInst().getProject((String) null);
    }

    @Test
    public void getProjectByProjectNameSecurity() {
        ProjectService inst = spy(getInst());

        doReturn(project).when(hudson).getItemByFullName("projectName", AbstractProject.class);
        @SuppressWarnings("unchecked")
        Collection<String> names = Mockito.mock(Collection.class);
        when(names.contains("projectName")).thenReturn(true);
        when(inst.getProjectNames()).thenReturn(names);

        AbstractProject<?, ?> result = getInst().getProject("projectName");

        assertThat(result, equalTo(project));
        Mockito.verify(securityService).checkPermission(project, Item.READ);
    }

    // findProjectByUUID -------

    @Test(expected = NullPointerException.class)
    public void findProjectByUUIDNullArg() {
        getInst().findProject((UUID) null);
    }

    @Test
    public void findProjectByUUIDNotFound() {
        UUID id = UUID.randomUUID();
        assertThat(getInst().findProject(id), nullValue());
    }

    // findProjectByProjectName

    @Test(expected = NullPointerException.class)
    public void findProjectByProjectNameNullArg() {
        getInst().findProject((String) null);
    }

    @Test
    public void findProjectByProjectNameProjectNotFoundException() {
        assertThat(getInst().findProject("projectName"), nullValue());
    }

    @Test
    public void findProjectByProjectNameSecurity() {
        ProjectService inst = spy(getInst());

        doReturn(project).when(hudson).getItemByFullName("projectName", AbstractProject.class);
        @SuppressWarnings("unchecked")
        Collection<String> names = Mockito.mock(Collection.class);
        when(names.contains("projectName")).thenReturn(true);
        when(inst.getProjectNames()).thenReturn(names);

        AbstractProject<?, ?> result = getInst().findProject("projectName");

        assertThat(result, equalTo(project));
        Mockito.verify(securityService).checkPermission(project, Item.READ);
    }

    // findProjectByFullname ------------

    @Test(expected = NullPointerException.class)
    public void findProjectByFullnameNullArg() {
        getInst().findProjectByFullName(null);
    }

    @Test
    public void findProjectByFullnameNotFound() {
        assertThat(getInst().findProjectByFullName("fullname"), nullValue());
    }

    @Test
    public void findProjectByFullnameSecurity() {
        doReturn(project).when(hudson).getItemByFullName("fullname", AbstractProject.class);

        AbstractProject<?, ?> result = getInst().findProjectByFullName("fullname");

        assertThat(result, equalTo(project));
        Mockito.verify(securityService).checkPermission(project, Item.READ);
    }

    // getAllProjects -----------
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void getAllProjectsSecurity() {
        @SuppressWarnings("unchecked")
        List<AbstractProject> projects = Mockito.mock(List.class);
        doReturn(projects).when(hudson).getAllItems(AbstractProject.class);

        List<AbstractProject> result = getInst().getAllProjects();

        assertThat(result, equalTo(projects));

        // Hudson getAllItems has permission check.
        Mockito.verify(hudson).getAllItems(Mockito.isA(Class.class));
    }

    // copyProject -----------
    @Test(expected = NullPointerException.class)
    public void copyProjectNullArg1() {
        getInst().copyProject(null, "toProject");
    }

    @Test(expected = NullPointerException.class)
    public void copyProjectNullArg2() {
        getInst().copyProject(project, null);
    }

    @Test
    public void copyProjectSecurity() throws IOException {

        // mocks
        AbstractProject<?, ?> toProject = Mockito.mock(AbstractProject.class);
        ProjectService inst = spy(getInst());

        // make method succeed
        doReturn(false).when(inst).projectExists("toProject");
        when(hudson.copy(project, "toProject")).thenReturn(toProject);

        // test
        inst.copyProject(project, "toProject");

        // verify
        Mockito.verify(securityService).checkPermission(Item.CREATE);
        Mockito.verify(securityService).checkPermission(project, Item.EXTENDED_READ);

    }

    @Test(expected = SystemIntegrityViolationException.class)
    public void copyProjectToProjectAlreadyExists() throws IOException {
        ProjectService inst = spy(getInst());
        doReturn(true).when(inst).projectExists("toProject");
        inst.copyProject(project, "toProject");
    }

    @Test(expected = ServiceRuntimeException.class)
    public void copyProjectIOException() throws IOException {

        // mocks
        ProjectService inst = spy(getInst());

        // make method succeed
        doReturn(false).when(inst).projectExists("toProject");
        when(hudson.copy(project, "toProject")).thenThrow(new IOException());

        // test
        inst.copyProject(project, "toProject");

    }

    // createProjectfromXml ------

    @Test(expected = NullPointerException.class)
    public void createProjectFromXMLNullArg1() {
        getInst().createProjectFromXML(null, input);
    }

    @Test(expected = NullPointerException.class)
    public void createProjectFromXMLNullArg2() {
        getInst().createProjectFromXML("toProject", null);
    }

    @Test
    public void createProjectFromXMLSecurity() throws IOException {
        // mocks
        TopLevelItem toProject = Mockito.mock(TopLevelItem.class);
        ProjectService inst = spy(getInst());

        // make method succeed
        doReturn(false).when(inst).projectExists("toProject");
        when(hudson.createProjectFromXML("toProject", input)).thenReturn(toProject);

        assertThat(inst.createProjectFromXML("toProject", input), equalTo(toProject));

        Mockito.verify(securityService).checkPermission(Item.CREATE);
    }

    @Test(expected = SystemIntegrityViolationException.class)
    public void createProjectFromXMLToProjectAlreadyExists() throws IOException {
        ProjectService inst = spy(getInst());
        doReturn(true).when(inst).projectExists("toProject");
        inst.createProjectFromXML("toProject", input);
    }

    @Test(expected = ServiceRuntimeException.class)
    public void createProjectFromXMLIOException() throws IOException {

        // mocks
        ProjectService inst = spy(getInst());

        // make method succeed
        doReturn(false).when(inst).projectExists("toProject");
        when(hudson.createProjectFromXML("toProject", input)).thenThrow(new IOException());

        // test
        inst.createProjectFromXML("toProject", input);

    }

    // getProjectNames -----------
    @Test
    public void getProjectNamesSecurity() {
        // blank, security Item.READ check performed by Hudson internals
    }

    // projectExists ------------

    @Test
    public void projectExistsSecurity() {
        // blank, security Item.READ check performed by Hudson internals
    }

    @Test(expected = NullPointerException.class)
    public void projectExistsNullArg() {
        getInst().projectExists(null);
    }

    @Test
    public void projectExistsTrue() {
        ProjectService inst = spy(getInst());

        @SuppressWarnings("unchecked")
        Collection<String> names = Mockito.mock(Collection.class);
        when(names.contains("projectName")).thenReturn(true);
        when(inst.getProjectNames()).thenReturn(names);

        assertThat(inst.projectExists("projectName"), is(true));
    }

}
