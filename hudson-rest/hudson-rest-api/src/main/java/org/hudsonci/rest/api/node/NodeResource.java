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

package org.hudsonci.rest.api.node;

import static com.google.common.base.Preconditions.*;
import hudson.model.Node;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import javax.inject.Inject;
import javax.inject.Named;

import org.hudsonci.rest.api.internal.ResourceSupport;

import org.hudsonci.rest.model.NodeDTO;
import org.hudsonci.rest.model.NodesDTO;
import org.hudsonci.service.NodeService;

/**
 * Access Nodes
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Path("/nodes")
public class NodeResource extends ResourceSupport {
    private final NodeService nodeService;

    private final NodeConverter nodex;

    @Inject
    public NodeResource(final NodeService nodeService, final NodeConverter nodex) {
        this.nodeService = checkNotNull(nodeService);
        this.nodex = checkNotNull(nodex);
    }

    @GET
    public NodesDTO getNodes() {

        NodesDTO nodesToReturn = new NodesDTO();
        List<Node> allNodesIncludingMaster = this.nodeService.getAllNodes();
        for (hudson.model.Node node : allNodesIncludingMaster) {
                nodesToReturn.getNodes().add(this.nodex.convert(node));
        }
        // Will always have at least one node to return - plynch: err is that really true?
        return nodesToReturn;
    }

    @GET
    @Path("{nodeName}")
    public NodeDTO getNode(final @PathParam("nodeName") String nodeName) {
        log.debug("Fetching node: {}", nodeName);

        return this.nodex.convert(this.nodeService.getNode(nodeName));
    }

}
