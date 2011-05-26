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

package org.hudsonci.rest.client.internal.cometd;

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
