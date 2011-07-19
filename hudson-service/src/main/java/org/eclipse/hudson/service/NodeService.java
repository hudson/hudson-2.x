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

package org.eclipse.hudson.service;

import hudson.model.Node;

import java.util.List;

import org.acegisecurity.AccessDeniedException;
import org.eclipse.hudson.service.internal.NodeServiceImpl;

import com.google.inject.ImplementedBy;

/**
 * {@link hudson.model.Node} related services.
 *
 * @since 2.1.0
 */
@ImplementedBy(NodeServiceImpl.class)
public interface NodeService {
    /**
     * Get the node with the given name.
     *
     * @param nodeName the name of the node to get
     * @return the node
     * @throws NodeNotFoundException if node cannot be found
     * @throws NullPointerException if nodeName is null
     * @throws AccessDeniedException if context does not have
     * {@link hudson.model.Item#READ} permission to access the master node
     */
    Node getNode(final String nodeName);

    /**
     * Find the node with the given name
     *
     * @param nodeName the name of the node to find
     * @return the node if found, else null
     * @throws NullPointerException if nodeName is null
     * @throws AccessDeniedException if context does not have
     * {@link hudson.model.Item#READ} permission to access the master node
     */
    Node findNode(final String nodeName);

    /**
     *
     * @return the master Hudson node, if permissions
     * {@link hudson.model.Item#READ}.
     * @throws AccessDeniedException if context does not have
     * {@link hudson.model.Item#READ} permission to access the master node
     */
    Node getMasterNode();

    /**
     * @return all {@link Node}s in the system, including the master, that have
     * {@link hudson.model.Item#READ} permission.
     */
    List<Node> getAllNodes();

    /**
     * @returns all {@link Node}s in the system, excluding {@link hudson.model.Hudson}
     * instance itself which represents the master, that have
     * {@link hudson.model.Item#READ} permission.
     */
    List<Node> getNodes();

    /**
     * @return the current node, if the context has
     * {@link hudson.model.Item#READ} permission
     * @throws AccessDeniedException if context does not have
     * {@link hudson.model.Item#READ} permission to access the master node
     */
    Node getCurrentNode();
}
