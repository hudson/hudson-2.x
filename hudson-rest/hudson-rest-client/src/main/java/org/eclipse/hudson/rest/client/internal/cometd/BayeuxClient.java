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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Custom {@link org.cometd.client.BayeuxClient} client which remembers what was subscribed too.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class BayeuxClient
    extends org.cometd.client.BayeuxClient
{
    private final Set<String> subscriptions = new HashSet<String>();

    public BayeuxClient(final AsyncHttpClient client, final String url) {
        super(client, url);
    }

    public Set<String> getSubscriptions() {
        synchronized (subscriptions) {
            return Collections.unmodifiableSet(subscriptions);
        }
    }

    @Override
    public void subscribe(final String channel) {
        super.subscribe(channel);

        synchronized (subscriptions) {
            subscriptions.add(channel);
        }
    }

    @Override
    public void unsubscribe(final String channel) {
        super.unsubscribe(channel);
        
        synchronized (subscriptions) {
            subscriptions.remove(channel);
        }
    }
}
