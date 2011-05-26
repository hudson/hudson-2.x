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

package org.hudsonci.service;

import hudson.model.Node;

import java.util.List;

import org.acegisecurity.AccessDeniedException;
import org.hudsonci.service.internal.NodeServiceImpl;

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
