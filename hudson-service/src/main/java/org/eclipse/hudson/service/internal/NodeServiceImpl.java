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

package org.eclipse.hudson.service.internal;

import static org.eclipse.hudson.service.internal.ServicePreconditions.*;

import com.google.common.base.Preconditions;

import hudson.model.Computer;
import hudson.model.Node;
import hudson.security.Permission;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.eclipse.hudson.service.NodeNotFoundException;
import org.eclipse.hudson.service.NodeService;
import org.eclipse.hudson.service.SecurityService;

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
