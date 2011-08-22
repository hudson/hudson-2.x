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

package org.eclipse.hudson.rest.api.node;

import hudson.model.Node;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.eclipse.hudson.rest.api.internal.ResourceSupport;
import org.eclipse.hudson.rest.model.NodeDTO;
import org.eclipse.hudson.rest.model.NodesDTO;
import org.eclipse.hudson.service.NodeService;

import static com.google.common.base.Preconditions.checkNotNull;

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
