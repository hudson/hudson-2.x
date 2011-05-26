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

package org.hudsonci.rest.client.internal;

import org.hudsonci.rest.client.OpenOptions;

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
