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

import org.eclipse.hudson.rest.common.Constants;
import org.eclipse.hudson.rest.model.fault.FaultBuilder;
import org.eclipse.hudson.rest.model.fault.FaultDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generates {@link FaultDTO} responses for generic {@link Exception}s.
 *
 * This should catch unhandled cases where the REST API can not properly handle a request.
 * The logged ID can be used to match up client/server to allow for better debugging.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Provider
public class GenericExceptionMapper
    implements ExceptionMapper<Exception>
{
    private static final Logger log = LoggerFactory.getLogger(GenericExceptionMapper.class);

    // TODO: May need to handle Throwable here to also catch Error instances

    public Response toResponse(final Exception cause) {
        checkNotNull(cause);

        final FaultDTO fault = FaultBuilder.build(cause);

        log.warn("Generating fault response (generic); ID: {}", fault.getId(), cause);

        return Response.serverError().entity(fault).type(Constants.FAULT_v1_JSON_TYPE).build();
    }
}

