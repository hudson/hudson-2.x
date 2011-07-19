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

package org.eclipse.hudson.rest.client.internal.ext;

import org.eclipse.hudson.rest.client.ext.NodeClient;
import org.eclipse.hudson.rest.client.internal.HudsonClientExtensionSupport;
import org.eclipse.hudson.rest.model.NodeDTO;
import org.eclipse.hudson.rest.model.NodesDTO;
import com.sun.jersey.api.client.ClientResponse;

import javax.ws.rs.core.UriBuilder;


import java.util.List;

import static javax.ws.rs.core.Response.Status.*;

/**
 * {@link org.eclipse.hudson.rest.client.ext.NodeClient} implementation.
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
