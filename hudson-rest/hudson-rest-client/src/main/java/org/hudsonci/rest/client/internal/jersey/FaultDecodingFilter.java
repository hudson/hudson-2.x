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

package org.hudsonci.rest.client.internal.jersey;

import org.hudsonci.rest.model.fault.FaultDTO;
import org.hudsonci.rest.model.fault.FaultDetailDTO;
import org.hudsonci.rest.model.fault.FaultException;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.hudsonci.rest.client.internal.ResponseUtil.isCompatible;
import static org.hudsonci.rest.client.internal.ResponseUtil.isStatus;
import static org.hudsonci.rest.common.Constants.FAULT_v1_JSON_TYPE;
import static org.hudsonci.rest.common.Constants.FAULT_v1_XML_TYPE;

/**
 * Throws {@link FaultException} when a fault has been detected.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class FaultDecodingFilter
    extends ClientFilter
{
    private static final Logger log = LoggerFactory.getLogger(FaultDecodingFilter.class);

    public ClientResponse handle(final ClientRequest request) throws ClientHandlerException {
        checkNotNull(request);
        ClientResponse response = getNext().handle(request);

        //
        // TODO: Look for X-HudsonFault header or use content-type to determine if the entity is a fault.
        // TODO: ... content-type feels most natural, though I'm not sure how/if that will work
        //

        if (isStatus(response, INTERNAL_SERVER_ERROR) && response.hasEntity() && isCompatible(response, FAULT_v1_JSON_TYPE, FAULT_v1_XML_TYPE)) {
            FaultDTO fault = response.getEntity(FaultDTO.class);
            log.warn("Detected fault; ID: {}", fault.getId());

            if (log.isDebugEnabled()) {
                for (FaultDetailDTO detail : fault.getDetails()) {
                    log.debug("[{}] {}", detail.getType(), detail.getMessage());
                }
            }

            throw new FaultException(fault);
        }

        return response;
    }
}
