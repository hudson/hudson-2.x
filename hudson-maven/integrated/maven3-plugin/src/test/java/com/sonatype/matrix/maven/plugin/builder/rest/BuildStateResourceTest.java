/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.builder.rest;

import com.sonatype.matrix.maven.plugin.builder.BuildStateNotFoundException;
import com.sonatype.matrix.maven.plugin.builder.BuildStateRecord;
import com.sonatype.matrix.maven.plugin.builder.MavenBuilderService;
import com.sonatype.matrix.maven.plugin.builder.internal.MavenBuilderServiceImpl;
import com.sonatype.matrix.rest.common.ProjectNameCodec;
import com.sonatype.matrix.service.BuildService;
import com.sonatype.matrix.service.DescriptorService;
import com.sonatype.matrix.service.ProjectService;
import com.sonatype.matrix.service.SecurityService;
import hudson.model.AbstractBuild;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link BuildStateResource}
 * @author plynch
 */
@RunWith(MockitoJUnitRunner.class)
public class BuildStateResourceTest {

    @Mock
    private DescriptorService descriptorService;

    @Mock
    private SecurityService securityService;

    @Mock
    private ProjectService projectService;

    @Mock
    private BuildService buildService;

    @Mock
    private AbstractBuild build;

    private BuildStateResource instance;

    @Before
    public void setUp() throws Exception {
        MavenBuilderService mavenBuilderService = new MavenBuilderServiceImpl(securityService, descriptorService, projectService, buildService);
        instance = new BuildStateResource(mavenBuilderService, new ProjectNameCodec());
    }

    private void assertStatus(WebApplicationException cause, Response.Status expected) {
        assertThat(cause.getResponse().getStatus(), equalTo(expected.getStatusCode()));
    }

    private void assertStatusBadRequest(WebApplicationException cause) {
        assertStatus(cause, Response.Status.BAD_REQUEST);
    }

    // getBuildStates -----------------
    @Test
    public void getBuildStatesProjectArgNull() {
        try {
            instance.getBuildStates(null, 1);
        }
        catch (WebApplicationException e) {
            assertStatusBadRequest(e);
        }
    }

    @Test
    public void getBuildStatesBuildnumberNegative() {
        try{
            instance.getBuildStates("projectName", -1);
        } catch(WebApplicationException e) {
            assertStatusBadRequest(e);
        }
    }

    @Test
    public void getBuildStatesBuildnumberZero() {
        try{
            instance.getBuildStates("projectName", 0);
        } catch(WebApplicationException e) {
            assertStatusBadRequest(e);
        }
    }

    // getBuildState ------------------
    @Test(expected = BuildStateNotFoundException.class)
    @SuppressWarnings("unchecked")
    public void testGetBuildStateIndexNotFound() {
        List<BuildStateRecord> records = Collections.emptyList();
        when(build.getActions(BuildStateRecord.class)).thenReturn(records);
        when(buildService.getBuild("projectName", 1)).thenReturn(build);

        //try {
        instance.getBuildState("projectName", 1, 2);
        //    fail("WebApplicationException expected for index not found");
        //} catch (WebApplicationException e) {
        //    assertStatus(e, Response.Status.NOT_FOUND);
        //}
    }

    @Test
    public void testGetBuildStateIndexNegative() {
        try{
            instance.getBuildState("projectName", 1, -1);
        } catch(WebApplicationException e) {
            assertStatusBadRequest(e);
        }
    }

    @Test
    public void testGetBuildStateProjectNull() {
        try {
            instance.getBuildState(null, 1, 1);
        }
        catch (WebApplicationException e) {
            assertStatusBadRequest(e);
        }
    }

//    /**
//     * An example of testing with mocks and RESTEasy.
//     *
//     * Problem with this is it is not much better than testing as regular method invocation.
//     *
//     * We explicitly register our own exception mappers which could be different than real container so it is not a
//     * a poor mans integration test.
//     *
//     * @throws Exception
//     */
//    @Test
//    public void getBuildStatesProjectNotFound404() throws Exception {
//        // make instance under test as normal
//        //List<BuildStateRecord> records = Collections.emptyList();
//        //when(build.getActions(BuildStateRecord.class)).thenReturn(records);
//        when(support.getBuild("foo", 1)).thenThrow(new ProjectNotFoundException("foo not found"));
//
//        // invoke the resource
//        SingletonResource resourceFactory = new SingletonResource(instance);
//        Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();
//        dispatcher.getRegistry().addResourceFactory(resourceFactory);
//
//        dispatcher.getProviderFactory().addExceptionMapper(NotFoundExceptionMapper.class);
//
//        MockHttpRequest request = MockHttpRequest.get(Constants.URI_PREFIX + "/buildState/foo/1");
//        MockHttpResponse response = new MockHttpResponse();
//        dispatcher.invoke(request, response);
//
//        // verify we got what we wanted
//        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
//
//    }
}