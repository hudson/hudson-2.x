/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.documents.rest;

import com.sonatype.matrix.maven.plugin.documents.DuplicateDocumentException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.Response.Status;

/**
 * Maps {@link DuplicateDocumentException} to a REST {@link Status#CONFLICT} response.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@Provider
public class DuplicateDocumentExceptionMapper
    implements ExceptionMapper<DuplicateDocumentException>
{
    @Override
    public Response toResponse(final DuplicateDocumentException cause) {
        checkNotNull(cause);
        return Response.status(Status.CONFLICT).entity(cause.getMessage()).build();
    }
}