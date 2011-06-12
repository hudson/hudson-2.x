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

import org.hudsonci.rest.common.ProjectNameCodec;

import org.hudsonci.maven.plugin.builder.MavenBuilderService;
import org.hudsonci.maven.plugin.builder.rest.BuilderConfigResource;
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
