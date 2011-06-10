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

package org.hudsonci.service.internal;

import static org.hudsonci.service.internal.ServicePreconditions.*;

import com.google.common.base.Preconditions;

import hudson.model.Computer;
import hudson.model.Node;
import hudson.security.Permission;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.hudsonci.service.NodeNotFoundException;
import org.hudsonci.service.NodeService;
import org.hudsonci.service.SecurityService;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of {@link NodeService}
 *
 * @since 2.1.0
 */
@Named
@Singleton
public class NodeServiceImpl extends ServiceSupport implements NodeService {

    private final SecurityService security;

    @Inject
    public NodeServiceImpl(final SecurityService security) {
        this.security = Preconditions.checkNotNull(security);
    }

    public Node getNode(String nodeName) {
        Node node = findNode(nodeName);
        if (node == null) {
            throw new NodeNotFoundException(String.format("Node %s not found.", nodeName));
        }
        return node;
    }

    public Node findNode(final String nodeName) {
        log.debug("findNode(\"{}\")", nodeName);
        checkNodeName(nodeName);
        Node node;
        // Provide the master node, if the name is ""
        if ("".equals(nodeName)) {
            node = getHudson();
        } else {
            node = getHudson().getNode(nodeName);
        }
        if (node != null) {
            this.security.checkPermission(node, Permission.READ);
        }
        return node;
    }

    public Node getMasterNode() {
        Node masterNode = getHudson();
        this.security.checkPermission(masterNode, Permission.READ);
        return masterNode;
    }

    public List<Node> getAllNodes() {
        List<Node> nodesToReturn = getNodes();
        Node masterNode = getHudson();
        if (this.security.hasPermission(masterNode, Permission.READ)) {
            nodesToReturn.add(masterNode);
        }
        return nodesToReturn;
    }

    public List<Node> getNodes() {
        List<Node> nodesToCheck = getHudson().getNodes();
        List<Node> nodesToReturn = new ArrayList<Node>(nodesToCheck.size());

        for (hudson.model.Node node : nodesToCheck) {
            if (this.security.hasPermission(node, Permission.READ)) {
                nodesToReturn.add(node);
            }
        }
        return nodesToReturn;
    }

    public Node getCurrentNode() {
        Node node = Computer.currentComputer().getNode();
        this.security.checkPermission(node, Permission.READ);
        return node;
    }

}
