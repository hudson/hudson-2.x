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

import javax.inject.Inject;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.hudson.rest.client.HudsonClientException;
import org.eclipse.hudson.rest.client.ext.NotificationClient;
import org.eclipse.hudson.rest.client.internal.HudsonClientExtensionSupport;
import org.eclipse.hudson.rest.client.internal.cometd.BayeuxClient;
import org.eclipse.hudson.rest.client.internal.cometd.BayeuxClientFactory;

import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link org.eclipse.hudson.rest.client.ext.NotificationClient} implementation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class NotificationClientImpl
    extends HudsonClientExtensionSupport
    implements NotificationClient
{
    public static final String BASE_COMETD_PATH = "cometd";

    private final BayeuxClientFactory clientFactory;

    private BayeuxClient bayeuxClient;

    @Inject
    public NotificationClientImpl(final BayeuxClientFactory clientFactory) {
        this.clientFactory = checkNotNull(clientFactory);
    }

    //
    // FIXME: The client ext stuff was only designed for REST bits, so the use/req of uri() is a work around
    //

    @Override
    protected UriBuilder uri() {
        return UriBuilder.fromUri(getClient().getBaseUri()).path(BASE_COMETD_PATH);
    }

    @Override
    public void close() throws Exception {
        if (bayeuxClient != null) {
            bayeuxClient.disconnect();
            bayeuxClient = null;
        }
    }

    //
    // FIXME: Probably want to expose a better API, but for now just expose the underlying client
    //

    public BayeuxClient getBayeuxClient() {
        // Lazy-init the client for now... so it will only consume resources when the feature is needed
        if (bayeuxClient == null) {
            BayeuxClient c;
            try {
                URI uri = uri().build();
                c = clientFactory.create(uri, getClient().getOptions());
                c.start();
                bayeuxClient = c;
            }
            catch (Exception e) {
                throw new HudsonClientException(e);
            }
        }
        return bayeuxClient;
    }
}
