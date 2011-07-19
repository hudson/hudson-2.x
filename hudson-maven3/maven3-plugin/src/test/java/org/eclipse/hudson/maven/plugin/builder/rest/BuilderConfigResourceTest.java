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

package org.eclipse.hudson.maven.plugin.builder.rest;

import org.eclipse.hudson.maven.plugin.builder.MavenBuilderService;
import org.eclipse.hudson.maven.plugin.builder.rest.BuilderConfigResource;
import org.eclipse.hudson.rest.common.ProjectNameCodec;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link BuilderConfigResource}
 * @author plynch
 */
@RunWith(MockitoJUnitRunner.class)
public class BuilderConfigResourceTest {

    @Mock
    private MavenBuilderService service;

    private BuilderConfigResource instance;

    @Before
    public void setUp() throws Exception {
        instance = new BuilderConfigResource(service, new ProjectNameCodec());
    }

    @Test
    public void getBuilderConfigurationProjectNameNull() {
         try {
            instance.getBuilderConfiguration(null, 1);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));
        }
    }

    @Test
    public void getBuilderConfigurationIndexNegative() {
        try {
            instance.getBuilderConfiguration("foo", -1);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));
        }
    }


    // TODO: project found but unsupported
    // TODO: multi config project, builder not found
}
