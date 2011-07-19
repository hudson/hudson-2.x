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
import org.eclipse.hudson.rest.model.fault.FaultException;
import org.eclipse.hudson.rest.model.fault.FaultDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link FaultException} mapper.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Provider
public class FaultExceptionMapper
    implements ExceptionMapper<FaultException>
{
    private static final Logger log = LoggerFactory.getLogger(FaultExceptionMapper.class);

    public Response toResponse(final FaultException cause) {
        checkNotNull(cause);

        final FaultDTO fault = cause.getFault();
        
        log.warn("Generating fault response; ID: {}", fault.getId());

        return Response.serverError().entity(fault).type(Constants.FAULT_v1_JSON_TYPE).build();
    }
}
