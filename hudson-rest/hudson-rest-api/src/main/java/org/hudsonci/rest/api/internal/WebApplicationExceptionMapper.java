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
