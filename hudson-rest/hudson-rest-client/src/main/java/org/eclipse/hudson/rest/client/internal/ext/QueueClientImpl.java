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

package org.eclipse.hudson.rest.client.internal.ext;

import com.sun.jersey.api.client.ClientResponse;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.hudson.rest.client.ext.QueueClient;
import org.eclipse.hudson.rest.client.internal.HudsonClientExtensionSupport;

import static javax.ws.rs.core.Response.Status.*;

/**
 * {@link QueueClient} implementation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class QueueClientImpl
    extends HudsonClientExtensionSupport
    implements QueueClient
{
    @Override
    protected UriBuilder uri() {
        return getClient().uri().path("queue");
    }
    
    public void clear() {
        ClientResponse resp = resource(uri().path("clear")).get(ClientResponse.class);
        try {
            ensureStatus(resp, NO_CONTENT);
        }
        finally {
            close(resp);
        }
    }
}
