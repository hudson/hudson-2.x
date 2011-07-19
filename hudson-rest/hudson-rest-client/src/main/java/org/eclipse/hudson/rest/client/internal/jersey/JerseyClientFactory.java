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

import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ProxyServer;
import com.ning.http.client.Realm;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;

import org.eclipse.hudson.rest.client.OpenOptions;
import org.eclipse.hudson.rest.common.JacksonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.spice.jersey.client.ahc.AhcHttpClient;
import org.sonatype.spice.jersey.client.ahc.config.AhcConfig;
import org.sonatype.spice.jersey.client.ahc.config.DefaultAhcConfig;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides a suitable {@link Client} for use on the client-side.
 *
 * @author <a href="mailto:jfarcand@apache.org">Jeanfrancois Arcand</a>
 * @since 2.1.0
 */
@Named
@Singleton
public class JerseyClientFactory
{
    private static final Logger log = LoggerFactory.getLogger(JerseyClientFactory.class);

    private final IoCComponentProviderFactory componentProviderFactory;

    @Inject
    public JerseyClientFactory(final @Named("sisu") IoCComponentProviderFactory componentProviderFactory) {
        this.componentProviderFactory = checkNotNull(componentProviderFactory);
        log.debug("Component provider factory: {}", componentProviderFactory);
    }

    public Client create(final OpenOptions options) {
        checkNotNull(options);

        AhcConfig config = new DefaultAhcConfig();
        AsyncHttpClientConfig.Builder builder = config.getAsyncHttpClientConfigBuilder();
        if (options.getUsername() != null && options.getPassword() != null) {
            Realm realm = new Realm.RealmBuilder()
                .setScheme(Realm.AuthScheme.BASIC)
                .setUsePreemptiveAuth(true) // FIXME: ATM we must configure preemptive auth, to be replaced by session token
                .setPrincipal(options.getUsername())
                .setPassword(options.getPassword())
                .build();
            builder.setRealm(realm);
        }

        if (options.getProxyHost() != null) {
           ProxyServer proxyServer = new ProxyServer(
               ProxyServer.Protocol.HTTP,
               options.getProxyHost(),
               options.getProxyPort(),
               options.getProxyUsername(),
               options.getProxyPassword()
           );
            builder.setProxyServer(proxyServer);
        }

        config.getClasses().add(JacksonProvider.class);

        AhcHttpClient client = AhcHttpClient.create(config, componentProviderFactory);
        client.setFollowRedirects(options.isFollowRedirects());

        // Last filter added is the first filter invoked
        client.addFilter(new FaultDecodingFilter());
        client.addFilter(new LoggingFilter());

        return client;
    }
}
