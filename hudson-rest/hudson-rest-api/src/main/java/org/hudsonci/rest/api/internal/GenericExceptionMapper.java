/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.hudsonci.rest.api.internal;

import org.hudsonci.rest.model.fault.FaultBuilder;
import org.hudsonci.rest.model.fault.FaultDTO;

import org.hudsonci.rest.common.Constants;
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

