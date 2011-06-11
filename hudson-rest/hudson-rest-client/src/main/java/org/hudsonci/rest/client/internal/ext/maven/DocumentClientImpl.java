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

import org.hudsonci.maven.model.config.DocumentDTO;
import org.hudsonci.maven.model.config.DocumentsDTO;
import com.sun.jersey.api.client.ClientResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.hudsonci.rest.client.ext.maven.DocumentClient;
import org.hudsonci.rest.client.internal.HudsonClientExtensionSupport;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;

/**
 * Default implementation for
 * {@link org.hudsonci.rest.client.ext.maven.DocumentClient}
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
