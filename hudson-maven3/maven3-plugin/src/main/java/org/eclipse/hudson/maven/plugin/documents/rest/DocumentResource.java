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

import com.google.common.base.Preconditions;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.eclipse.hudson.maven.model.config.DocumentDTO;
import org.eclipse.hudson.maven.model.config.DocumentsDTO;
import org.eclipse.hudson.maven.plugin.Constants;
import org.eclipse.hudson.maven.plugin.documents.DocumentManager;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static org.eclipse.hudson.rest.common.RestPreconditions.checkDocumentId;
import static org.eclipse.hudson.rest.common.RestPreconditions.checkNotNull;
import static org.eclipse.hudson.rest.common.RestPreconditions.throwConflict;

/**
 * Provides REST access to the {@link DocumentManager}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 * @todo should we verify the document id inside document dto or make id strongly typed to uuid
 */
@Named
@Path(Constants.URI_PREFIX + "/documents")
@Produces({APPLICATION_JSON, APPLICATION_XML})
@Consumes({APPLICATION_JSON, APPLICATION_XML})
public class DocumentResource
{
    // NOTE: This is for testing use of media type for versioning and sub-resource method selection

    //public static final MediaType DOCUMENT_v1_JSON_TYPE = new MediaType("application", "vnd.hudsonci.maven-document-v1+json");
    //
    //public static final String DOCUMENT_v1_JSON = "application/vnd.hudsonci.maven-document-v1+json";
    //
    //public static final MediaType DOCUMENT_vLATEST_JSON_TYPE = DOCUMENT_v1_JSON_TYPE;
    //
    //public static final String DOCUMENT_vLATEST_JSON = DOCUMENT_v1_JSON;

    private final DocumentManager documents;

    @Inject
    public DocumentResource(final DocumentManager documents) {
        this.documents = Preconditions.checkNotNull(documents);
    }

    @GET
    @Consumes
    public DocumentsDTO getDocuments(final @QueryParam("summary") @DefaultValue("false") boolean summary) {
        return new DocumentsDTO().withDocuments(documents.getDocuments(summary));
    }

    @POST
    //@Consumes({DOCUMENT_vLATEST_JSON})
    //@Produces({DOCUMENT_vLATEST_JSON})
    public DocumentDTO addDocument(final DocumentDTO document) {
        checkNotNull(document, "Document");
        return documents.addDocument(document);
    }

    @GET
    @Path("{id}")
    @Consumes
    public DocumentDTO getDocument(final @PathParam("id") String id, final @QueryParam("summary") @DefaultValue("false") boolean summary) {
        checkDocumentId(id);
        return documents.getDocument(id, summary);
    }

    @PUT
    @Path("{id}")
    public DocumentDTO updateDocument(final @PathParam("id") String id, final DocumentDTO document) {
        checkDocumentId(id);
        checkNotNull(document, DocumentDTO.class);
        if(!id.equals(document.getId())){
            throwConflict(String.format("Document ID mismatch: %s != %s", id, document.getId()));
        }
        return documents.updateDocument(document);
    }

    @DELETE
    @Path("{id}")
    @Produces
    @Consumes
    public void removeDocument(final @PathParam("id") String id) {
        checkDocumentId(id);
        documents.removeDocument(id);
    }
}
