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

package org.hudsonci.rest.client.internal.ext.maven;

import org.hudsonci.maven.model.state.BuildStateDTO;
import org.hudsonci.maven.model.state.BuildStatesDTO;
import com.sun.jersey.api.client.ClientResponse;

import javax.ws.rs.core.UriBuilder;

import org.hudsonci.rest.client.ext.maven.BuildStateClient;
import org.hudsonci.rest.client.internal.HudsonClientExtensionSupport;

import java.util.Collections;
import java.util.List;

import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;

/**
 *
 * @author plynch
 */
public class  BuildStateClientImpl
        extends HudsonClientExtensionSupport
        implements BuildStateClient {

    @Override
    protected UriBuilder uri() {
        return getClient().uri().path("plugin/maven3-plugin").path("buildState");
    }

    public BuildStateDTO getBuildState(String projectName, int buildNumber, int index) {
        ClientResponse resp = resource(uri().path(projectName).path(String.valueOf(buildNumber)).path(String.valueOf(index))).get(ClientResponse.class);
        try {
            ensureStatus(resp, OK);
            return resp.getEntity(BuildStateDTO.class);
        }
        finally {
            close(resp);
        }
    }

    public List<BuildStateDTO> getBuildStates(String projectName, int buildNumber) {
        ClientResponse resp = resource(uri().path(projectName).path(String.valueOf(buildNumber))).get(ClientResponse.class);
        try {
            if (isStatus(resp, NO_CONTENT)) {
                return Collections.emptyList();
            }
            ensureStatus(resp, OK);

            return resp.getEntity(BuildStatesDTO.class).getStates();
        }
        finally {
            close(resp);
        }
    }
}
