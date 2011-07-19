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

package org.eclipse.hudson.rest.client;

import com.sun.jersey.api.client.ClientResponse;

/**
 * Thrown to indicate that a response is not valid or unexpected.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class InvalidResponseException
    extends HudsonClientException
{
    private final ClientResponse response;

    public InvalidResponseException(final ClientResponse response, final String message, final Throwable cause) {
        super(message, cause);
        assert response != null;
        this.response = response;
    }

    public InvalidResponseException(final ClientResponse response, final String message) {
        this(response, message, null);
    }

    public InvalidResponseException(final ClientResponse response, final Throwable cause) {
        this(response, null, cause);
    }

    public InvalidResponseException(final ClientResponse response) {
        this(response, null, null);
    }

    public ClientResponse getResponse() {
        return response;
    }

    @Override
    public String getMessage() {
        StringBuilder buff = new StringBuilder();
        
        String msg = super.getMessage();
        if (msg != null) {
            buff.append(msg);
            buff.append(" ");
        }
        
        buff.append("[");
        buff.append(response.getStatus());
        buff.append("] ");
        buff.append(response.getClientResponseStatus().getReasonPhrase());

        return buff.toString();
    }
}
