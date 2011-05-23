/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.documents.rest;

import static com.sonatype.matrix.rest.common.RestPreconditions.*;
import static javax.ws.rs.core.MediaType.*;

import javax.inject.Inject;
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

import com.google.common.base.Preconditions;
import com.sonatype.matrix.maven.model.config.DocumentDTO;
import com.sonatype.matrix.maven.model.config.DocumentsDTO;
import com.sonatype.matrix.maven.plugin.Constants;
import com.sonatype.matrix.maven.plugin.documents.DocumentManager;

/**
 * Provides REST access to the {@link DocumentManager}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 * @todo should we verify the document id inside document dto or make id strongly typed to uuid
 */
@Path(Constants.URI_PREFIX + "/documents")
@Produces({APPLICATION_JSON, APPLICATION_XML})
@Consumes({APPLICATION_JSON, APPLICATION_XML})
public class DocumentResource
{
    // NOTE: This is for testing use of media type for versioning and sub-resource method selection

    //public static final MediaType DOCUMENT_v1_JSON_TYPE = new MediaType("application", "vnd.sonatype.matrix.maven-document-v1+json");
    //
    //public static final String DOCUMENT_v1_JSON = "application/vnd.sonatype.matrix.maven-document-v1+json";
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
