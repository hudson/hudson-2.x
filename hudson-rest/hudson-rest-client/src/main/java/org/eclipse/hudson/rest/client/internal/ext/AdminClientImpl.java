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

import org.eclipse.hudson.rest.client.ext.AdminClient;
import org.eclipse.hudson.rest.client.internal.HudsonClientExtensionSupport;

import static javax.ws.rs.core.MediaType.TEXT_XML;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;

/**
 * {@link AdminClient} implementation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class AdminClientImpl
    extends HudsonClientExtensionSupport
    implements AdminClient
{
    protected UriBuilder uri() {
        return getClient().uri().path("admin");
    }

    public String getConfig() {
        ClientResponse resp = resource(uri().path("config")).accept(TEXT_XML).get(ClientResponse.class);
        try {
            ensureStatus(resp, OK);
            return resp.getEntity(String.class);
        }
        finally {
            close(resp);
        }
    }

    public void quietDown(final boolean toggle) {
        ClientResponse resp = resource(uri().queryParam("toggle", toggle).path("quiet-down")).get(ClientResponse.class);
        try {
            ensureStatus(resp, NO_CONTENT);
        }
        finally {
            close(resp);
        }
    }

    public void reloadConfig() {
        ClientResponse resp = resource(uri().path("config").path("reload")).get(ClientResponse.class);
        try {
            ensureStatus(resp, NO_CONTENT);
        }
        finally {
            close(resp);
        }
    }

    public void restart(final boolean safe) {
        ClientResponse resp = resource(uri().queryParam("safe", safe).path("restart")).get(ClientResponse.class);
        try {
            ensureStatus(resp, NO_CONTENT);
        }
        finally {
            close(resp);
        }
    }
}
