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

import org.acegisecurity.AcegiSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;

/**
 * Generates UNAUTHORIZED responses for {@link AcegiSecurityException}s.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Provider
public class AcegiSecurityExceptionMapper
    implements ExceptionMapper<AcegiSecurityException>
{
    private static final Logger log = LoggerFactory.getLogger(AcegiSecurityExceptionMapper.class);

    public Response toResponse(final AcegiSecurityException cause) {
        checkNotNull(cause);

        log.debug("Generating UNAUTHORIZED response for: {}", cause, cause);

        return Response.status(UNAUTHORIZED).type(TEXT_PLAIN).entity(cause.getMessage()).build();
    }
}
