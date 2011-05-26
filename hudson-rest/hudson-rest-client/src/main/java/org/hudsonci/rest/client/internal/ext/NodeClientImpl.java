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

package org.hudsonci.rest.client.internal.ext;

import org.hudsonci.rest.model.NodeDTO;
import org.hudsonci.rest.model.NodesDTO;
import com.sun.jersey.api.client.ClientResponse;

import javax.ws.rs.core.UriBuilder;

import org.hudsonci.rest.client.ext.NodeClient;
import org.hudsonci.rest.client.internal.HudsonClientExtensionSupport;

import java.util.List;

import static javax.ws.rs.core.Response.Status.*;

/**
 * {@link org.hudsonci.rest.client.ext.NodeClient} implementation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class NodeClientImpl
    extends HudsonClientExtensionSupport
    implements NodeClient
{
    protected UriBuilder uri() {
        return getClient().uri().path("nodes");
    }

    public List<NodeDTO> getNodes() {
        ClientResponse resp = resource(uri()).get(ClientResponse.class);
        try {
            ensureStatus(resp, OK);
            return resp.getEntity(NodesDTO.class).getNodes();
        }
        finally {
            close(resp);
        }
    }

    protected UriBuilder nodeUri(final String nodeName) {
        assert nodeName != null;
        return uri().path(nodeName);
    }

    public NodeDTO getNode(final String nodeName) {
        ClientResponse resp = resource(nodeUri(nodeName)).get(ClientResponse.class);
        try {
            ensureStatus(resp, OK);
            return resp.getEntity(NodeDTO.class);
        }
        finally {
            close(resp);
        }
    }
}
