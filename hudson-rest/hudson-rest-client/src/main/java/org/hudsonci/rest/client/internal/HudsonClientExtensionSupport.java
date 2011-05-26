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

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import org.hudsonci.rest.client.HudsonClient;
import org.hudsonci.rest.common.ProjectNameCodec;
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
