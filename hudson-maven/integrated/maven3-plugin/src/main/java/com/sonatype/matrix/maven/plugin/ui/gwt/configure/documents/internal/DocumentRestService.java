/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.configure.documents.internal;

import com.sonatype.matrix.maven.model.config.DocumentDTO;
import com.sonatype.matrix.maven.model.config.DocumentsDTO;
import com.sonatype.matrix.maven.plugin.documents.rest.DocumentResource;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

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
 * @since 1.1
 */
@Path("documents")
public interface DocumentRestService
    extends RestService
{
    // NOTE: This is for testing use of media type for versioning and sub-resource method selection
    //String DOCUMENT_JSON = "application/vnd.sonatype.matrix.maven-document-v1+json";

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
