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

package org.eclipse.hudson.rest.client.internal;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import org.eclipse.hudson.rest.client.HudsonClient;
import org.eclipse.hudson.rest.common.ProjectNameCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Support for {@link HudsonClient.Extension} implementations.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public abstract class HudsonClientExtensionSupport
    implements HudsonClient.Extension
{
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private HudsonClient client;

    private MediaType[] accept;

    public void init(final HudsonClient client) {
        checkNotNull(client);
        checkState(this.client == null);
        log.trace("Initialized w/client: {}", client);
        this.client = client;
    }

    protected HudsonClient getClient() {
        checkState(client != null);
        return client;
    }

    public void open() throws Exception {
        // Cache accept types in open, client instance is not yet fully available in constructor
        // FIXME: see if ^^^ is still valid or not
        List<MediaType> tmp = getClient().getOptions().getEncoding().getAccept();
        this.accept = tmp.toArray(new MediaType[tmp.size()]);
    }

    public void close() throws Exception {
        // empty
    }

    protected abstract UriBuilder uri();

    protected WebResource.Builder resource(final URI uri) {
        assert uri != null;
        return getClient().resource(uri).accept(accept);
    }

    protected WebResource.Builder resource(final UriBuilder uri) {
        return resource(uri.build());
    }

    //
    // Helpers
    //

    protected void close(final ClientResponse response) {
        ResponseUtil.close(response);
    }

    protected String encodeProjectName(final String projectName) {
        return new ProjectNameCodec().encode(projectName);
    }

    protected Response.StatusType ensureStatus(final ClientResponse response, final Response.StatusType... expected) {
        return ResponseUtil.ensureStatus(response, expected);
    }

    protected boolean isStatus(final ClientResponse response, final Response.StatusType expected) {
        return ResponseUtil.isStatus(response, expected);
    }
}
