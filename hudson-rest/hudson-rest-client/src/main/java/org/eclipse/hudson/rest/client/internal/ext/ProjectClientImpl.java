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

package org.eclipse.hudson.rest.client.internal.ext;

import org.eclipse.hudson.rest.client.ext.ProjectClient;
import org.eclipse.hudson.rest.client.internal.HudsonClientExtensionSupport;
import org.eclipse.hudson.rest.model.project.ProjectDTO;
import org.eclipse.hudson.rest.model.project.ProjectReferenceDTO;
import org.eclipse.hudson.rest.model.project.ProjectsDTO;
import org.eclipse.hudson.rest.model.PermissionDTO;
import org.eclipse.hudson.rest.model.PermissionsDTO;
import com.sun.jersey.api.client.ClientResponse;

import javax.ws.rs.core.UriBuilder;


import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;

/**
 * {@link org.eclipse.hudson.rest.client.ext.ProjectClient} implementation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class ProjectClientImpl
    extends HudsonClientExtensionSupport
    implements ProjectClient
{
    @Override
    protected UriBuilder uri() {
        return getClient().uri().path("projects");
    }

    public List<ProjectDTO> getProjects() {
        ClientResponse resp = resource(uri()).get(ClientResponse.class);
        try {
            ensureStatus(resp, OK, NO_CONTENT);
            if (isStatus(resp, NO_CONTENT)) {
                return Collections.emptyList();
            }
            return resp.getEntity(ProjectsDTO.class).getProjects();
        }
        finally {
            close(resp);
        }
    }

    public ProjectDTO copyProject(final String projectName, final String targetProjectName) {
        assert projectName != null;
        assert targetProjectName != null;
        URI uri = uri().path("copy").queryParam("target", encodeProjectName(targetProjectName))
                .queryParam("source", encodeProjectName(projectName)).build();
        ClientResponse resp = resource(uri).get(ClientResponse.class);
        try {
            ensureStatus(resp, OK);
            return resp.getEntity(ProjectDTO.class);
        }
        finally {
            close(resp);
        }
    }

    private UriBuilder projectUri(final String projectName) {
        assert projectName != null;
        return uri().path(encodeProjectName(projectName));
    }

    private UriBuilder projectUri(final ProjectReferenceDTO ref) {
        assert ref != null;
        return getClient().uri().path("project").path(ref.getId());
    }

    public ProjectDTO createProject(final String projectName, final InputStream configXml) {
        assert projectName != null;
        assert configXml != null;
        ClientResponse resp = resource(projectUri(projectName)).type(TEXT_XML).post(ClientResponse.class, configXml);
        try {
            ensureStatus(resp, OK);
            return resp.getEntity(ProjectDTO.class);
        }
        finally {
            close(resp);
        }
    }

    public ProjectDTO getProject(final String projectName) {
        URI uri = projectUri(projectName).build();
        ClientResponse resp = resource(uri).get(ClientResponse.class);
        try {
            ensureStatus(resp, OK);
            return resp.getEntity(ProjectDTO.class);
        }
        finally {
            close(resp);
        }
    }

    //
    // WORK AROUND: This is added to test by UUID reference bits, should eventually only allow one method.
    //

    public ProjectDTO getProject(final ProjectReferenceDTO ref) {
        URI uri = projectUri(ref).build();
        ClientResponse resp = resource(uri).get(ClientResponse.class);
        try {
            ensureStatus(resp, OK);
            return resp.getEntity(ProjectDTO.class);
        }
        finally {
            close(resp);
        }
    }

    public void deleteProject(final String projectName) {
        URI uri = projectUri(projectName).build();
        ClientResponse resp = resource(uri).delete(ClientResponse.class);
        try {
            ensureStatus(resp, NO_CONTENT);
        }
        finally {
            close(resp);
        }
    }

    public String getProjectConfig(final String projectName) {
        URI uri = projectUri(projectName).path("config").build();
        ClientResponse resp = resource(uri).accept(TEXT_XML).get(ClientResponse.class);
        try {
            ensureStatus(resp, OK);
            return resp.getEntity(String.class);
        }
        finally {
            close(resp);
        }
    }

    public void enableProject(final String projectName, final boolean enable) {
        URI uri = projectUri(projectName).path("enable").queryParam("enable", enable).build();
        ClientResponse resp = resource(uri).get(ClientResponse.class);
        try {
            ensureStatus(resp, NO_CONTENT);
        }
        finally {
            close(resp);
        }
    }

    public void scheduleBuild(final String projectName) {
        URI uri = projectUri(projectName).path("schedule").build();
        ClientResponse resp = resource(uri).get(ClientResponse.class);
        try {
            ensureStatus(resp, NO_CONTENT);
        }
        finally {
            close(resp);
        }
    }

    public List<PermissionDTO> getProjectPermissions(final String projectName) {
        URI uri = projectUri(projectName).path("permissions").build();
        ClientResponse resp = resource(uri).get(ClientResponse.class);
        try {
            ensureStatus(resp, OK);
            return resp.getEntity(PermissionsDTO.class).getPermissions();
        }
        finally {
            close(resp);
        }
    }
}
