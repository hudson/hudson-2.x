/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.hudsonci.rest.client.internal.jersey;

import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ProxyServer;
import com.ning.http.client.Realm;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;

import org.hudsonci.rest.client.OpenOptions;
import org.hudsonci.rest.common.JacksonProvider;
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
