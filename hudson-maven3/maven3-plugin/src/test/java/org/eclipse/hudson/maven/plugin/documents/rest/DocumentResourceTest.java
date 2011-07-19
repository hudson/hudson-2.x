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

package org.eclipse.hudson.maven.plugin.documents.rest;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.hudson.maven.plugin.documents.DocumentManager;
import org.eclipse.hudson.maven.plugin.documents.rest.DocumentResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.eclipse.hudson.maven.model.config.DocumentDTO;

/**
 * Tests for {@link DocumentResource}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentResourceTest {
    @Mock
    DocumentManager documentManager;

    private DocumentResource resource;

    @Test
    public void addDocument_CheckNullArgs() {
        try {
            resource.addDocument(null);
            fail();
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));
        }
        verifyZeroInteractions(documentManager);
    }

    @Test
    public void getDocument_CheckNullArgs() {
        try {
            resource.getDocument(null, false);
            fail();
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));
        }
    }

    @Test
    public void getDocument_IdNot_A_UUID() {
        try {
            resource.getDocument("not_A_UUID", false);
            fail();
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));
        }
    }

    @Test
    public void getDocument_WithoutSummary() {
        resource.getDocument("92329d39-6f5c-4520-abfc-aab64544e172", true);
        verify(documentManager).getDocument("92329d39-6f5c-4520-abfc-aab64544e172", true);
    }

    @Test
    public void getDocument_WithSummary() {
        resource.getDocument("92329d39-6f5c-4520-abfc-aab64544e172", false);
        verify(documentManager).getDocument("92329d39-6f5c-4520-abfc-aab64544e172", false);
    }

    @Test
    public void getDocuments_WithoutSummary() {
        assertThat(resource, notNullValue());
        resource.getDocuments(false);
        verify(documentManager).getDocuments(false);
    }

    @Test
    public void getDocuments_WithSummary() {
        assertThat(resource, notNullValue());
        resource.getDocuments(true);
        verify(documentManager).getDocuments(true);
    }

    @Before
    public void init() {
        resource = new DocumentResource(documentManager);
    }

    @Test
    public void removeDocument_CheckNullArgs() {
        try {
            resource.removeDocument(null);
            fail();
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));
        }
    }

    @Test
    public void removeDocument_validArgs() {
        resource.removeDocument("92329d39-6f5c-4520-abfc-aab64544e172");
        verify(documentManager).removeDocument("92329d39-6f5c-4520-abfc-aab64544e172");
    }

    @Test
    public void removeDocument_not_a_uuid() {
        try {
            resource.removeDocument("not_a_UUID");
            fail();
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));
        }
    }

    @Test
    public void testMediaType() {
        MediaType type = new MediaType("application", "vnd.hudsonci.maven-document-v1+json");
        assertThat("application/vnd.hudsonci.maven-document-v1+json", equalTo(type.toString()));
    }

    @Test
    public void updateDocumentCheckNullArgs() {
        try {
            resource.updateDocument(null,null);
            fail();
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));
        }
    }

    @Test
    public void updateDocumentCheckNullArgs1() {
        try {
            resource.updateDocument(null, new DocumentDTO());
            fail();
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));
        }
    }

    @Test
    public void updateDocumentCheckNullArgs2() {
        try {
            resource.updateDocument("92329d39-6f5c-4520-abfc-aab64544e172", null);
            fail();
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));
        }
    }
    
    @Test
    public void updateDocument_ConflictingIds() {
        DocumentDTO dto = new DocumentDTO();
        dto.setId("92329d39-6f5c-4520-abfc-aab64544e173"); // not the same
        try {
            resource.updateDocument("92329d39-6f5c-4520-abfc-aab64544e172", dto);
            fail();
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus(), equalTo(Response.Status.CONFLICT.getStatusCode()));
        }
    }
    
    
}
