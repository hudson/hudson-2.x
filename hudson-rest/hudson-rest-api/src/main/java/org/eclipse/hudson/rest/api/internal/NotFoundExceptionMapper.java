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

package org.eclipse.hudson.rest.api.internal;

import org.eclipse.hudson.service.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.Response.Status.*;

/**
 * Generates NOT_FOUND responses for {@link NotFoundException}s.
 *
 * The {@link NotFoundException#getMessage} is used to supply information in the response about what was not found.
 *
 * @author Peter Lynch
 * @since 2.1.0
 */
@Provider
public class NotFoundExceptionMapper
    implements ExceptionMapper<NotFoundException>
{
    private static final Logger log = LoggerFactory.getLogger(NotFoundExceptionMapper.class);

    public Response toResponse(final NotFoundException cause) {
        checkNotNull(cause);
        String message = cause.getMessage();
        log.debug("Generating NOT_FOUND response: {}", message);
        return Response.status(NOT_FOUND).entity(cause.getMessage()).build();
    }
}
