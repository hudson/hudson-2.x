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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Passes {@link WebApplicationException}'s response as-is.
 *
 * Handles custom logging mostly and needed due to {@link GenericExceptionMapper}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Provider
public class WebApplicationExceptionMapper
    implements ExceptionMapper<WebApplicationException>
{
    private static final Logger log = LoggerFactory.getLogger(WebApplicationExceptionMapper.class);

    public Response toResponse(final WebApplicationException cause) {
        checkNotNull(cause);

        Response resp = cause.getResponse();

        if (log.isDebugEnabled()) {
            StringBuilder buff = new StringBuilder();
            buff.append(cause.getClass().getSimpleName());

            buff.append(" [");
            int code = resp.getStatus();
            buff.append(code).append("]");

            Response.Status status = Response.Status.fromStatusCode(code);
            if (status != null) {
                buff.append(" ").append(status);
            }

            if (log.isTraceEnabled()) {
                log.trace("{}", buff, cause);
            }
            else {
                log.debug("{}", buff);
            }
        }

        return resp;
    }
}
