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

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.eclipse.hudson.maven.plugin.documents.DuplicateDocumentException;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.Response.Status;

/**
 * Maps {@link DuplicateDocumentException} to a REST {@link Status#CONFLICT} response.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Provider
public class DuplicateDocumentExceptionMapper
    implements ExceptionMapper<DuplicateDocumentException>
{
    public Response toResponse(final DuplicateDocumentException cause) {
        checkNotNull(cause);
        return Response.status(Status.CONFLICT).entity(cause.getMessage()).build();
    }
}
