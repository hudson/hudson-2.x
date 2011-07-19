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

import org.eclipse.hudson.rest.client.ext.maven.DocumentClient;
import org.eclipse.hudson.rest.client.internal.HudsonClientExtensionSupport;
import org.eclipse.hudson.maven.model.config.DocumentDTO;
import org.eclipse.hudson.maven.model.config.DocumentsDTO;
import com.sun.jersey.api.client.ClientResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;


import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;

/**
 * Default implementation for
 * {@link org.eclipse.hudson.rest.client.ext.maven.DocumentClient}
 *
 * @author plynch
 */
public class DocumentClientImpl extends HudsonClientExtensionSupport implements DocumentClient {

    @Override
    protected UriBuilder uri() {
        return getClient().uri().path("plugin/maven3-plugin").path("documents");
    }

    public DocumentsDTO getDocuments(final boolean summary) {
        ClientResponse resp = resource(uri().queryParam("summary", summary)).get(ClientResponse.class);
        try {
            ensureStatus(resp, OK);
            return resp.getEntity(DocumentsDTO.class);
        }
        finally {
            close(resp);
        }
    }

    public DocumentDTO addDocument(DocumentDTO document) {
        checkNotNull(document, "document must not be null");
        ClientResponse resp = resource(uri()).type(MediaType.APPLICATION_JSON).post(ClientResponse.class, document);
        try {
            ensureStatus(resp, OK);
            return resp.getEntity(DocumentDTO.class);
        }
        finally {
            close(resp);
        }
    }

    public DocumentDTO getDocument(final String id, final boolean summary) {
        checkNotNull(id, "id must not be null");
        ClientResponse resp = resource(uri().path(id).queryParam("summary", summary)).get(ClientResponse.class);
        try {
            ensureStatus(resp, OK);
            return resp.getEntity(DocumentDTO.class);
        }
        finally {
            close(resp);
        }
    }

    public DocumentDTO updateDocument(final String id, final DocumentDTO document) {
        checkNotNull(id, "id must not be null");
        checkNotNull(document, "document must not be null");
        ClientResponse resp = resource(uri().path(id)).type(MediaType.APPLICATION_JSON).put(ClientResponse.class, document);
        try {
            ensureStatus(resp, OK);
            return resp.getEntity(DocumentDTO.class);
        }
        finally {
            close(resp);
        }
    }

    public void removeDocument(final String uuid) {
        checkNotNull(uuid, "id must not be null");
        ClientResponse resp = resource(uri().path(uuid)).delete(ClientResponse.class);
        try {
            ensureStatus(resp, NO_CONTENT);
        }
        finally {
            close(resp);
        }
    }
}
