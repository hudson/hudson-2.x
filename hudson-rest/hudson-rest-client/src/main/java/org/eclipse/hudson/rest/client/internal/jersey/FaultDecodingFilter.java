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

package org.eclipse.hudson.rest.client.internal.jersey;

import org.eclipse.hudson.rest.model.fault.FaultException;
import org.eclipse.hudson.rest.model.fault.FaultDTO;
import org.eclipse.hudson.rest.model.fault.FaultDetailDTO;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.eclipse.hudson.rest.client.internal.ResponseUtil.isCompatible;
import static org.eclipse.hudson.rest.client.internal.ResponseUtil.isStatus;
import static org.eclipse.hudson.rest.common.Constants.FAULT_v1_JSON_TYPE;
import static org.eclipse.hudson.rest.common.Constants.FAULT_v1_XML_TYPE;

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
