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

package org.eclipse.hudson.rest.client.internal.ext.maven;

import org.eclipse.hudson.rest.client.ext.maven.BuildStateClient;
import org.eclipse.hudson.rest.client.internal.HudsonClientExtensionSupport;
import org.eclipse.hudson.maven.model.state.BuildStateDTO;
import org.eclipse.hudson.maven.model.state.BuildStatesDTO;
import com.sun.jersey.api.client.ClientResponse;

import javax.ws.rs.core.UriBuilder;


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
