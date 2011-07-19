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

import org.eclipse.hudson.rest.client.OpenOptions;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ProxyServer;
import com.ning.http.client.Realm.RealmBuilder;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.ning.http.client.ProxyServer.Protocol;

/**
 * Creates configured {@link AsyncHttpClient} instances from options.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class AsyncHttpClientFactory
{
    public AsyncHttpClient create(final OpenOptions options) {
        checkNotNull(options);

        AsyncHttpClientConfig.Builder config = new AsyncHttpClientConfig.Builder();
        config.setFollowRedirects(options.isFollowRedirects());

        if (options.getUsername() != null && options.getPassword() != null) {
            config.setRealm(new RealmBuilder()
                .setPrincipal(options.getUsername())
                .setPassword(options.getPassword())
                .setUsePreemptiveAuth(true).build()); // FIXME: Preemptive auth is required for now
        }

        if (options.getProxyHost() != null) {
            Protocol proto = Protocol.HTTP;
            if (options.getProxyProtocol() != null) {
                proto = Protocol.valueOf(options.getProxyProtocol().toUpperCase());

            }
            config.setProxyServer(new ProxyServer(
                proto,
                options.getProxyHost(),
                options.getProxyPort(),
                options.getProxyUsername(),
                options.getProxyPassword()));
        }

        return new AsyncHttpClient(config.build());
    }
}
