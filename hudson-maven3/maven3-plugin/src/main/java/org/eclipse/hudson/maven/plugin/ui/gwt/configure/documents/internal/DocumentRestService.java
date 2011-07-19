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

package org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.internal;

import org.eclipse.hudson.maven.model.config.DocumentDTO;
import org.eclipse.hudson.maven.model.config.DocumentsDTO;
import org.eclipse.hudson.maven.plugin.documents.rest.DocumentResource;
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
