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

package org.hudsonci.maven.plugin.ui.gwt.configure.documents.internal;

import org.hudsonci.maven.model.config.DocumentDTO;
import org.hudsonci.maven.model.config.DocumentsDTO;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;
import org.hudsonci.maven.plugin.documents.rest.DocumentResource;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 * RestyGWT service interface for {@link DocumentResource}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Path("documents")
public interface DocumentRestService
    extends RestService
{
    // NOTE: This is for testing use of media type for versioning and sub-resource method selection
    //String DOCUMENT_JSON = "application/vnd.hudsonci.maven-document-v1+json";

    @GET
    void getDocuments(@QueryParam("summary") boolean summary, MethodCallback<DocumentsDTO> callback);

    @POST
    // NOTE: Requires RestyGWT 1.1+ for @Consumes/@Produces use
    //@Consumes({DOCUMENT_JSON})
    //@Produces({DOCUMENT_JSON})
    void addDocument(DocumentDTO document, MethodCallback<DocumentDTO> callback);

    @GET
    @Path("{id}")
    void getDocument(@PathParam("id") String id, @QueryParam("summary") boolean summary, MethodCallback<DocumentDTO> callback);

    @PUT
    @Path("{id}")
    void updateDocument(@PathParam("id") String id, DocumentDTO document, MethodCallback<DocumentDTO> callback);

    @DELETE
    @Path("{id}")
    void removeDocument(@PathParam("id") String id, MethodCallback<String> callback);
}
