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

package org.eclipse.hudson.rest.client.internal.cometd;

import com.ning.http.client.AsyncHttpClient;
import org.cometd.Bayeux;
import org.cometd.Client;
import org.cometd.Message;
import org.cometd.MessageListener;
import org.cometd.client.ext.AckExtension;
import org.eclipse.hudson.rest.client.OpenOptions;
import org.eclipse.hudson.rest.client.internal.AsyncHttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.eclipse.hudson.utils.common.Varargs.$;

/**
 * Creates {@link BayeuxClient} instances.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class BayeuxClientFactory
{
    private static final Logger log = LoggerFactory.getLogger(BayeuxClientFactory.class);

    private final AsyncHttpClientFactory ahcFactory;

    @Inject
    public BayeuxClientFactory(final AsyncHttpClientFactory ahcFactory) {
        this.ahcFactory = checkNotNull(ahcFactory);
    }

    public BayeuxClient create(URI uri, final OpenOptions opts) throws Exception {
        checkNotNull(uri);
        checkNotNull(opts);

        // WORK AROUND: The cometd client freaks out when using http[s] w/o a port explicitly configured
        //       this is reported to be fixed in cometd 1.1.3 (as yet unreleased)

        if ("https".equals(uri.getScheme()) && uri.getPort() == -1) {
            uri = UriBuilder.fromUri(uri).port(443).build();
        }
        else if ("http".equals(uri.getScheme()) && uri.getPort() == -1) {
            uri = UriBuilder.fromUri(uri).port(80).build();
        }

        log.info("Creating client for URI: {}", uri);

        AsyncHttpClient httpClient = ahcFactory.create(opts);

        final BayeuxClient client = new BayeuxClient(httpClient, uri.toString());
        client.setBackoffIncrement(3000);
        client.addExtension(new AckExtension());
        client.addListener(new MessageListener()
        {
            private boolean connected;

            //
            // FIXME: This logs as the factory, and is a little confusing, change it
            //

            public void deliver(final Client from, final Client to, final Message message) {
                log.debug("Delivering: {}; {} -> {}", $(message, from, to));

                if (Bayeux.META_CONNECT.equals(message.getChannel())) {
                    boolean wasConnected = connected;

                    Object tmp = message.get(Bayeux.SUCCESSFUL_FIELD);
                    connected = Boolean.parseBoolean(String.valueOf(tmp));

                    if (!wasConnected && connected) {
                        log.warn("Connection to server established; re-subscribing");
                        //
                        // FIXME: This should be done in the client... grrr, sync issues, potential hole
                        //
                        for (String channel : client.getSubscriptions()) {
                            client.subscribe(channel);
                        }
                    }
                    else if (wasConnected && !connected) {
                        log.warn("Connection to server broken");
                    }
                }
            }
        });

        return client;
    }
}
