/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Tom Huybrechts
 *     
 *
 *******************************************************************************/ 

package hudson.slaves;

import hudson.model.Node;
import hudson.model.EnvironmentSpecific;
import hudson.model.TaskListener;
import java.io.IOException;

/**
 * Represents any concept that can be adapted for a node.
 *
 * Mainly for documentation purposes.
 *
 * @author huybrechts
 * @since 1.286
 * @see EnvironmentSpecific
 * @param <T>
 *      Concrete type that represents the thing that can be adapted.
 */
public interface NodeSpecific<T extends NodeSpecific<T>> {
    /**
     * Returns a specialized copy of T for functioning in the given node.
     */
    T forNode(Node node, TaskListener log) throws IOException, InterruptedException;
}
